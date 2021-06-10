/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.imflibrary.utils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.apache.http.client.utils.URIBuilder;

/**
 * Locator Proxy that wraps up a S3 blob.
 */
public class S3Locator implements Locator {
    private static class S3Path {

        private String bucket;
        private String key;
        private String region;
        private String endpoint;
        private boolean isPathStyle;
        private URI uri;

        private S3Path(String bucket, String key, String region, String endpoint, boolean isPathStyle, URI uri) {
            this.bucket = bucket;
            this.key = key;
            this.region = region;
            this.endpoint = endpoint;
            this.isPathStyle = isPathStyle;
            this.uri = uri;
        }

        static S3Path of(URI uri) {
            try {
                final AmazonS3URI s3uri = new AmazonS3URI(uri);
                return new S3Path(s3uri.getBucket(), s3uri.getKey(), s3uri.getRegion(), null, s3uri.isPathStyle(), s3uri.getURI());
            } catch (IllegalArgumentException e) {
                final String p = uri.getPath();
                if ("/".equals(p)) {
                    return new S3Path(null, null, null, null, false, uri);
                } else {
                    // Allow non AWS style paths (I.E minio).
                    String bucket, key, endpoint;
                    int index = p.indexOf('/', 1);
                    if (index == -1) {
                        bucket = p.substring(1);
                        key = null;
                    } else if (index == (p.length() - 1)) {
                        bucket = p.substring(1, index);
                        key = null;
                    } else {
                        bucket = p.substring(1, index);
                        key = p.substring(index + 1);
                    }
                    try {
                        endpoint = new URIBuilder(uri).setPath("").build().toASCIIString();
                    } catch (URISyntaxException ex) {
                        throw new IllegalArgumentException("Invalid URI: " + ex.toString());
                    }
                    return new S3Path(bucket, key, null, endpoint, true, uri);
                }
            }
        }

        static S3Path of(String path) {
            try {
                // Assume http style paths are already URL encoded, though we need to convert + to %20.
                if (path.startsWith("http")) {
                    return of(URI.create(path.replace("+", "%20")));
                }
                // If we reach here, then the path is assumed to be s3 style, in which case we assume it is not URL encoded.
                // In this case we need to URLEncode the path before converting to a URI.
                return of (URI.create(URLEncoder.encode(path, "UTF-8")
                        .replace("%3A", ":")
                        .replace("%2F", "/")
                        .replace("+", "%20")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        private URI getUri(String key1) throws URISyntaxException {
            final URIBuilder builder = new URIBuilder(this.uri);
            if (this.isPathStyle) {
                builder.setPath("/" + this.bucket + "/" + key1);
            } else {
                builder.setPath("/" + key1);
            }
            return builder.build();
        }

        S3Path copy(String key) {
            try {
                return new S3Path(bucket, key, region, endpoint, isPathStyle, getUri(key));
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public String toString() {
            return "S3Path{" + "bucket=" + bucket + ", key=" + key + ", region=" + region + ", endpoint=" + endpoint + ", isPathStyle=" + isPathStyle + ", uri=" + uri + '}';
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.bucket);
            hash = 79 * hash + Objects.hashCode(this.key);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final S3Path other = (S3Path) obj;
            if (!Objects.equals(this.bucket, other.bucket)) {
                return false;
            }
            return Objects.equals(this.key, other.key);
        }
    }

    private final Configuration configuration;

    private final S3Path s3path;
    private final AmazonS3 s3Client;
    private final String path;

    private static AmazonS3ClientBuilder createBuilder(Configuration configuration) {
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        final String accessKey = configuration.getValue("aws.accesskey");
        final String secretKey = configuration.getValue("aws.secretkey");
        final String token = configuration.getValue("aws.token");
        final String profile = configuration.getValue("aws.profile");
        final String roleArn = configuration.getValue("aws.rolearn");
        final String externalId = configuration.getValue("aws.externalid");
        final AWSCredentialsProvider credentialsProvider;
        /**
         * The following rights will be needed:
         * 
         * s3:ListBucket, s3:GetBucketLocation on the bucket resource.
         * s3:GetObject for the keys in the bucket.
         */

        if (StringUtils.hasValue(accessKey) && StringUtils.hasValue(secretKey)) {
            if (StringUtils.hasValue(token)) {
                credentialsProvider = new AWSStaticCredentialsProvider(new BasicSessionCredentials(accessKey, secretKey, token));
            } else {
                credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
            }
        } else if (StringUtils.hasValue(profile)) {
            credentialsProvider = new ProfileCredentialsProvider(profile);
        } else {
            credentialsProvider = null;
        }
        if (StringUtils.hasValue(roleArn)) {
            final AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard();
            if (credentialsProvider != null) {
                stsBuilder.withCredentials(credentialsProvider);
            }
            final STSAssumeRoleSessionCredentialsProvider.Builder stsRoleBuilder = new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "photon-session");
            if (StringUtils.hasValue(externalId)) {
                stsRoleBuilder.withExternalId(externalId);
            }
            stsBuilder.setRegion(Regions.US_EAST_1.getName());
            stsRoleBuilder.withStsClient(stsBuilder.build());
            builder.withCredentials(stsRoleBuilder.build());
        } else if (credentialsProvider != null) {
            builder.withCredentials(credentialsProvider);
        } else if (configuration.getValue("aws.anonymous") != null) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()));
        }
        return builder;
    }

    private static AmazonS3 createClient(Configuration configuration, S3Path s3path) {
        final AmazonS3ClientBuilder builder = createBuilder(configuration);
        if (!StringUtils.hasValue(s3path.endpoint)) {
            s3path.endpoint = configuration.getValue("aws.endpoint");
        }

        if (StringUtils.hasValue(s3path.endpoint)) {
            builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3path.endpoint, null));
            builder.setPathStyleAccessEnabled(true);
        } else if (StringUtils.hasValue(s3path.region)) {
            builder.withRegion(s3path.region);
        } else {
            final AmazonS3 tmpClient = builder.withRegion(Regions.US_EAST_1).build();
            String region = null;
            try {
                region = com.amazonaws.services.s3.model.Region.fromValue(tmpClient.getBucketLocation(s3path.bucket)).toAWSRegion().getName();
                s3path.region = region;
            } catch (AmazonS3Exception e) {
                if (e.getAdditionalDetails() != null) {
                    region = e.getAdditionalDetails().get("Region");
                    s3path.region = region;
                }
            } catch (IllegalArgumentException e) {
            }
            if (region != null) {
                builder.withRegion(region);
            }
        }
        return builder.build();
    }

    private S3Locator copy(String key) {
        return new S3Locator(this.configuration, this.s3path.copy(key), this.s3Client);
    }

    private S3Locator(Configuration configuration, S3Path s3path, AmazonS3 s3Client) {
        this.configuration = configuration;
        this.s3path = s3path;
        this.s3Client = s3Client;
        this.path = "s3://" + s3path.bucket + "/" + (s3path.key != null ? s3path.key : "");
    }

    private S3Locator(Configuration configuration, S3Path s3path) {
        this(configuration, s3path, createClient(configuration, s3path));
    }

    /**
     * Instantiate a new S3Locator object from a S3 blob location.
     *
     * @param location      the S3 blob location
     * @param configuration S3 configuration.
     */
    public S3Locator(String location, Configuration configuration) {
        this(configuration, S3Path.of(location));
    }

    /**
     * Instantiate a new S3Locator object from a S3 URI location.
     *
     * @param uri           the S3 blob location
     * @param configuration S3 configuration.
     */
    public S3Locator(URI uri, Configuration configuration) {
        this(configuration, S3Path.of(uri));
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean exists() {
        if (isDirectory()) {
            final ListObjectsV2Result listObjects = s3Client.listObjectsV2(s3path.bucket, s3path.key);
            return listObjects.getObjectSummaries().size() > 0;
        } else {
            try {
                s3Client.getObjectMetadata(s3path.bucket, s3path.key);
                return true;
            } catch (AmazonS3Exception ex) {
                if (ex.getStatusCode() == 404) {
                    return false;
                }
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public ResourceByteRangeProvider getResourceByteRangeProvider() {
        return new S3ByteRangeProvider(this);
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        if (s3path.key != null) {
            return s3path.key.endsWith("/");
        }
        return true;
    }

    @Override
    public Locator[] listFiles(Filter filter) {
        return listFiles(filter, null);
    }
    
    public Locator[] listFiles(Filter filter, Integer maxKeys) {
        if (isDirectory()) {
            final List<Locator> items = new LinkedList<>();
            final Set<String> directories = new TreeSet<>();
            ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(s3path.bucket)
                .withPrefix(s3path.key)
                .withMaxKeys(maxKeys);
            ListObjectsV2Result objects = s3Client.listObjectsV2(request);
            for (;;) {
                for (S3ObjectSummary s : objects.getObjectSummaries()) {
                    final String key = s.getKey();
                    final String subKey = (s3path.key == null)?key:key.substring(s3path.key.length());
                    if (subKey.indexOf('/') > 0) {
                        int idx = subKey.indexOf('/');
                        if (idx > 0) {
                            final String dir = subKey.substring(0, idx+1);
                            if (!directories.contains(dir)) {
                                final S3Locator l = this.copy((s3path.key == null)?dir:s3path.key + dir);
                                if (filter == null || filter.accept(l)) {
                                    items.add(l);
                                }
                                directories.add(dir);
                            }
                        }
                    } else {
                        final S3Locator l = this.copy(key);
                        if (filter == null || filter.accept(l)) {
                            items.add(l);
                        }
                    }
                }
                if (objects.isTruncated()) {
                    request = new ListObjectsV2Request()
                        .withBucketName(objects.getBucketName())
                        .withPrefix(objects.getPrefix())
                        .withContinuationToken(objects.getNextContinuationToken())
                        .withDelimiter(objects.getDelimiter())
                        .withMaxKeys(objects.getMaxKeys())
                        .withEncodingType(objects.getEncodingType());
                    objects = s3Client.listObjectsV2(request);
                } else {
                    break;
                }
            }
            return items.toArray(new Locator[items.size()]);
        }
        return null;        
    }

    @Override
    public URI toURI() {
        return s3path.uri;
    }

    @Override
    public Locator getParent() {
        if (s3path.key != null) {
            int i;
            if (isDirectory()) {
                i = s3path.key.lastIndexOf("/", s3path.key.length() - 2);
            } else {
                i = s3path.key.lastIndexOf("/");
            }
            if (i == -1) {
                return this.copy(null);
            }
            return this.copy(s3path.key.substring(0, i) + "/");
        }
        return null;
    }

    @Override
    public Locator getChild(String name) {
        final String key;
        if (s3path.key == null) {
            key = name;
        } else if (isDirectory()) {
            key = s3path.key + name;
        } else {
            key = s3path.key + "/" + name;
        }
        return this.copy(key);
    }

    @Override
    public String getName() {
        if (s3path.key == null) {
            return "";
        }
        int idx1, idx2;
        if (isDirectory()) {
            idx2 = s3path.key.length() - 1;
        } else {
            idx2 = s3path.key.length();
        }
        idx1 = s3path.key.lastIndexOf("/", idx2 - 1);
        return s3path.key.substring(idx1 + 1, idx2);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        try (S3Object o = s3Client.getObject(this.s3path.bucket, this.s3path.key)) {
            long length = o.getObjectMetadata().getContentLength();
            if (length > Integer.MAX_VALUE - 8) {
                throw new IOException("Object [" + this.path + "] too large");
            }
            final S3ObjectInputStream is = o.getObjectContent();
            return Locator.toByteArray((int) length, is);
        }
    }

    @Override
    public byte[] readBytes(long start, long end) throws IOException {
        ResourceByteRangeProvider.Utilities.validateRangeRequest(length(), start, end);
        long size = (end - start + 1);
        if (size > (long) (Integer.MAX_VALUE - 8)) {
            throw new OutOfMemoryError("Required array size too large");
        }
        try (InputStream is = getByteRangeAsStream(start, end)) {
            return Locator.toByteArray((int) size, is);
        }
    }

    @Override
    public long length() {
        if (isDirectory()) {
            return 0;
        }
        try {
            return s3Client.getObjectMetadata(this.s3path.bucket, this.s3path.key).getContentLength();
        } catch (AmazonS3Exception ex) {
            if (ex.getStatusCode() == 404) {
                return 0;
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean mkdir() {
        return true;
    }

    @Override
    public String getPath() {
        return "s3://" + this.s3path.bucket + "/" + this.s3path.key;
    }

    @Override
    public String toString() {
        return "S3Locator{" + "s3path=" + s3path + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final S3Locator other = (S3Locator) obj;
        return Objects.equals(this.path, other.path);
    }

    /**
     * Gets the input stream containing the contents of this Locator specified
     * by the inclusive byte range.
     *
     * @param start The start of the inclusive byte range to download.
     * @param end The end of the inclusive byte range to download.
     *
     * @return An input stream containing the contents of this object specified
     *         by the inclusive byte range.
     *
     * @throws IOException if an I/O error occurs
     */
    public InputStream getByteRangeAsStream(long start, long end) throws IOException {
        final GetObjectRequest request = new GetObjectRequest(this.s3path.bucket, this.s3path.key);
        request.setRange(start, end);
        return s3Client.getObject(request).getObjectContent();
    }
}
