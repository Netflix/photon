package com.netflix.imflibrary.utils;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class S3FileLocator implements FileLocator {
    private static final AmazonS3 s3Client = new AmazonS3Client(DefaultAWSCredentialsProviderChain.getInstance());

    private String bucket;
    private String key;

    public S3FileLocator(String url) {
        AmazonS3URI s3URI = new AmazonS3URI(url);
        this.bucket = s3URI.getBucket();
        this.key = s3URI.getKey();
    }

    public S3FileLocator(URI url) {
        AmazonS3URI s3URI = new AmazonS3URI(url);
        this.bucket = s3URI.getBucket();
        this.key = s3URI.getKey();
    }

    public S3FileLocator(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }

    public static void main(String args[]) throws IOException {
        S3FileLocator fileLocator = new S3FileLocator("s3://oz-zl-api-staging-dev/mao/deliverables/chloes-bag-imf-package-mao-base/");

        System.out.println(fileLocator.isDirectory());

        for (S3FileLocator fl : fileLocator.listFiles(null)) {
            System.out.println(fl.toURI());
        }
        System.out.println("dsada");
    }

    public String getBucket() {
        return this.bucket;
    }

    public String getKey() {
        return this.key;
    }

    public URI toURI() throws IOException {
        try {
            return new URI("s3://" + this.bucket + "/" + this.key);
        } catch (URISyntaxException x) {
            throw new Error(x);
        }
    }

    public String getAbsolutePath() {
        return "s3://" + this.bucket + "/" + this.key;
    }

    public String getPath() {
        return this.getAbsolutePath();
    }

    public String getName() {
        String[] parts = this.key.split("/");
        return parts[parts.length - 1];
    }

    public boolean exists() {
        return true;
    }

    /**
     * Tests whether the file denoted by this abstract pathname is a
     * directory.
     *
     * @return <code>true</code> if and only if the file denoted by this
     * abstract pathname exists <em>and</em> is a directory;
     * <code>false</code> otherwise
     */
    @Override
    public boolean isDirectory() {
        return this.key.charAt(this.key.length() - 1) == '/';
    }

    /**
     * Returns the top level keys in a s3 folder
     * @return
     */
    public S3FileLocator[] listFiles(FilenameFilter filenameFilter) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(this.bucket)
                .withPrefix(this.key)
                .withDelimiter("/");

        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        ArrayList<S3FileLocator> fileLocators = new ArrayList<S3FileLocator>();
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            S3FileLocator fl = new S3FileLocator(objectSummary.getBucketName(), objectSummary.getKey());
            if (filenameFilter == null || filenameFilter.accept(null, fl.getName())) {
                fileLocators.add(fl);
            }
        }

        return fileLocators.toArray(new S3FileLocator[0]);
    }
}
