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

import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.S3TestBase;

/**
 *
 */
@Test(groups = "unit")
public class S3LocatorTest extends S3TestBase {

    @Test
    public void testExists() {
        client.putObject("testbucket", "key1", "test");
        S3Locator locator = new S3Locator(endpoint + "/testbucket/key1", configuration);
        Assert.assertTrue(locator.exists());
    }

    @Test
    public void testIsDirectory() {
        S3Locator locator = new S3Locator(endpoint + "/testbucket/dir1/", configuration);
        Assert.assertTrue(locator.isDirectory());
        locator = new S3Locator(endpoint + "/testbucket/key1", configuration);
        Assert.assertFalse(locator.isDirectory());
    }

    @Test
    public void testListEntries() {
        client.putObject("testbucket", "dir1/item1", "test");
        client.putObject("testbucket", "dir1/item1/subitem1", "test");
        client.putObject("testbucket", "dir1/item1/subitem2", "test");
        client.putObject("testbucket", "dir1/item1/subitem3", "test");
        client.putObject("testbucket", "dir1/item1/dir1/i1", "test");
        client.putObject("testbucket", "dir1/item1/dir1/i2", "test");
        client.putObject("testbucket", "dir1/item1/subitem4", "test");
        client.putObject("testbucket", "dir1/item1/subitem5", "test");
        client.putObject("testbucket", "dir1/item1/subitem6", "test");
        client.putObject("testbucket", "dir1/item2", "test");
        client.putObject("testbucket", "dir1/item3", "test");
        client.putObject("testbucket", "dir1/item4", "test");
        client.putObject("testbucket", "dir1/item5", "test");
        client.putObject("testbucket", "dir1/item6", "test");
        client.putObject("testbucket", "dir1/item7", "test");
        client.putObject("testbucket", "dir1/item8", "test");
        client.putObject("testbucket", "dir1/item9", "test");
        S3Locator locator = new S3Locator(endpoint + "/testbucket/dir1/", configuration);
        Locator[] listFiles = locator.listFiles(null, 4);
        Assert.assertEquals(listFiles.length, 10);
        Arrays.sort(listFiles, (Locator o1, Locator o2) -> o1.getAbsolutePath().compareTo(o2.getAbsolutePath()));
        Assert.assertEquals(listFiles[0].getPath(), "s3://testbucket/dir1/item1");
        Assert.assertEquals(listFiles[1].getPath(), "s3://testbucket/dir1/item1/");
        Assert.assertEquals(listFiles[2].getPath(), "s3://testbucket/dir1/item2");
        Assert.assertEquals(listFiles[3].getPath(), "s3://testbucket/dir1/item3");
        Assert.assertEquals(listFiles[4].getPath(), "s3://testbucket/dir1/item4");
        Assert.assertEquals(listFiles[5].getPath(), "s3://testbucket/dir1/item5");
        Assert.assertEquals(listFiles[6].getPath(), "s3://testbucket/dir1/item6");
        Assert.assertEquals(listFiles[7].getPath(), "s3://testbucket/dir1/item7");
        Assert.assertEquals(listFiles[8].getPath(), "s3://testbucket/dir1/item8");
        Assert.assertEquals(listFiles[9].getPath(), "s3://testbucket/dir1/item9");

        listFiles = locator.listFiles(f -> {
            return f.getName().endsWith("1");
        });
        Assert.assertEquals(listFiles.length, 2);
        Assert.assertEquals(listFiles[0].getPath(), "s3://testbucket/dir1/item1");
        Assert.assertEquals(listFiles[1].getPath(), "s3://testbucket/dir1/item1/");
        
        listFiles = new S3Locator(endpoint + "/testbucket", configuration).listFiles(null);
        Assert.assertEquals(listFiles.length, 1);
        Assert.assertEquals(listFiles[0].getPath(), "s3://testbucket/dir1/");
    }

    @Test
    public void testGetParent() {
        S3Locator locator = new S3Locator(endpoint + "/testbucket/dir1/item1", configuration);
        Assert.assertEquals(locator.getParent(), new S3Locator(endpoint + "/testbucket/dir1/", configuration));
        Assert.assertEquals(locator.getParent().getParent(), new S3Locator(endpoint + "/testbucket", configuration));
        Assert.assertNull(locator.getParent().getParent().getParent());
    }

    @Test
    public void testGetChild() {
        S3Locator locator1 = new S3Locator(endpoint + "/testbucket", configuration);
        S3Locator locator2 = new S3Locator(endpoint + "/testbucket/dir1", configuration);
        S3Locator locator3 = new S3Locator(endpoint + "/testbucket/dir1/", configuration);

        Assert.assertEquals(locator1.getChild("dir1/"), new S3Locator(endpoint + "/testbucket/dir1/", configuration));
        Assert.assertEquals(locator1.getChild("item1"), new S3Locator(endpoint + "/testbucket/item1", configuration));
        Assert.assertEquals(locator2.getChild("item1"), new S3Locator(endpoint + "/testbucket/dir1/item1", configuration));
        Assert.assertEquals(locator3.getChild("item1"), new S3Locator(endpoint + "/testbucket/dir1/item1", configuration));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals(new S3Locator(endpoint + "/testbucket/dir1/dir2/dir3/item3", configuration).getName(), "item3");
        Assert.assertEquals(new S3Locator(endpoint + "/testbucket/dir1/item2", configuration).getName(), "item2");
        Assert.assertEquals(new S3Locator(endpoint + "/testbucket/dir1/", configuration).getName(), "dir1");
        Assert.assertEquals(new S3Locator(endpoint + "/testbucket", configuration).getName(), "");
        Assert.assertEquals(new S3Locator(endpoint + "/testbucket/", configuration).getName(), "");
    }

    @Test
    public void testGetAllBytes() throws IOException {
        byte[] buffer = new byte[256];
        for (int i = 0; i < 256; i++) {
            buffer[i] = (byte) i;
        }
        S3Locator locator = new S3Locator(endpoint + "/testbucket/dir1/item3", configuration);
        final int blocks = 88;
        byte[] testBuffer = new byte[blocks * buffer.length];
        for (int i = 0; i < blocks; i++) {
            System.arraycopy(buffer, 0, testBuffer, i * buffer.length, buffer.length);
        }
        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(blocks * buffer.length);
        client.putObject("testbucket", "dir1/item3", new ByteArrayInputStream(testBuffer), objectMetadata);
        byte[] b = locator.readAllBytes();
        Assert.assertEquals(b.length, blocks * buffer.length);
        for (int i = 0; i < blocks; i++) {
            for (int j = 0; j < buffer.length; j++) {
                Assert.assertEquals(b[j + i * buffer.length], buffer[j]);
            }
        }
    }

    @Test
    public void testLength() throws IOException {
        client.putObject("testbucket", "dir1/item1", "testing12345678");
        S3Locator locator = new S3Locator(endpoint + "/testbucket/dir1/item1", configuration);
        Assert.assertEquals(locator.length(), 15);

        client.putObject("testbucket", "dir1/item1", "test");
        Assert.assertEquals(locator.length(), 4);
    }

    @Test
    public void testGetByteRangeAsStream() throws IOException {
        client.putObject("testbucket", "dir1/item1", "0123456789abcdefghijklmnopqrstuvwxyz");
        S3Locator locator = new S3Locator(endpoint + "/testbucket/dir1/item1", configuration);
        try (InputStream is = locator.getByteRangeAsStream(10, 16)) {
            byte[] b = new byte[20];
            int read = is.read(b);
            Assert.assertEquals(read, 7);
            Assert.assertEquals(new String(b, 0, 7), "abcdefg");
        }
    }
}
