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
package testUtils;

import com.adobe.testing.s3mock.S3MockApplication;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.netflix.imflibrary.utils.Locator;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 */
public class S3TestBase {

    private static class S3Mock {
        private static Map<String, Object> getProperties() {
            final Map<String, Object> args = new HashMap<>();
            args.put(S3MockApplication.PROP_SECURE_CONNECTION, false);
            args.put(S3MockApplication.PROP_SILENT, true);
            return args;
        }
        private final Map<String, Object> properties;
        private S3MockApplication s3mock;
        
        public S3Mock() {
            this.properties = getProperties();
        }
        
        public void start() {
            s3mock = S3MockApplication.start(properties);
        }

        public void shutdown() {
            s3mock.stop();
        }
        
        public String getServiceEndpoint() {
            final boolean isSecureConnection = (boolean) properties.getOrDefault(S3MockApplication.PROP_SECURE_CONNECTION, true);
            return isSecureConnection ? "https://localhost:" + s3mock.getPort(): "http://localhost:" + s3mock.getHttpPort();
        }
    }
    
    protected Locator.Configuration configuration;
    protected AmazonS3 client;
    protected String endpoint;
    protected S3Mock api;

    @BeforeClass
    public void beforeClass() {
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.ERROR);
        api = new S3Mock();
        api.start();
        endpoint = api.getServiceEndpoint();

        client = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, null))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("access", "secret")))
                .build();
        configuration = mock(Locator.Configuration.class);
        when(configuration.getValue("aws.accesskey")).thenReturn("access");
        when(configuration.getValue("aws.secretkey")).thenReturn("secret");
    }

    @AfterClass
    public void afterClass() {
        client.shutdown();
        api.shutdown();
    }

    @BeforeMethod
    public void beforeMethod() {
        client.createBucket("testbucket");
    }

    @AfterMethod
    public void afterMethod() {
        final ListObjectsV2Result objects = client.listObjectsV2("testbucket");
        objects.getObjectSummaries().forEach(s -> {
            client.deleteObject(s.getBucketName(), s.getKey());
        });
        client.deleteBucket("testbucket");
    }

    public void s3uploadFiles(File inputDirectory, String targetBucket, String keyPrefix) {
        for (File f : inputDirectory.listFiles()) {
            client.putObject(targetBucket, keyPrefix + inputDirectory.getName() + "/" + f.getName(), f);
        }
    }
}
