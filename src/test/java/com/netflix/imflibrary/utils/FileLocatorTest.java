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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class FileLocatorTest {

    @Test
    public void testExists() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            Locator locator = new FileLocator(tmpFile);
            Assert.assertTrue(locator.exists());

            locator = locator.getParent().getChild("should not exist");
            Assert.assertFalse(locator.exists());
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testAbsolutePath() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            FileLocator locator = new FileLocator(tmpFile);
            Assert.assertEquals(locator.getAbsolutePath(), tmpFile.getAbsolutePath());
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testIsDirectory() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            Locator locator = new FileLocator(tmpFile.getParentFile());
            Assert.assertTrue(locator.isDirectory());
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testListFiles() throws IOException {
        Path dir = Files.createTempDirectory("locatorTest");
        try {
            Files.createTempFile(dir, "test", ".txt");
            Files.createTempFile(dir, "test", ".txt");
            Files.createTempFile(dir, "test", ".mxf");
            Files.createTempFile(dir, "test", ".mxf");
            Files.createTempFile(dir, "test", ".txt");

            Locator locator = new FileLocator(dir.toFile());
            Assert.assertTrue(locator.exists());
            Assert.assertTrue(locator.isDirectory());

            Locator[] files = locator.listFiles(null);
            Assert.assertEquals(files.length, 5);

            files = locator.listFiles(l -> {
                return l.getName().endsWith(".mxf");
            });
            Assert.assertEquals(files.length, 2);
        } finally {
            Files.list(dir).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException ex) {
                    Assert.fail();
                }
            });
        }
    }

    @Test
    public void testGetParent() throws IOException {
        Path dir = Files.createTempDirectory("locatorTest");
        try {
            Path tmpFile = Files.createTempFile(dir, "test", ".txt");
            Locator locator = new FileLocator(tmpFile.toFile());
            Assert.assertTrue(locator.exists());
            Locator parent = locator.getParent();
            Assert.assertTrue(parent.isDirectory());
            Assert.assertEquals(parent.getName(), dir.toFile().getName());
        } finally {
            Files.list(dir).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException ex) {
                    Assert.fail();
                }
            });
        }
    }

    @Test
    public void testGetChild() throws IOException {
        Path dir = Files.createTempDirectory("locatorTest");
        try {
            Locator locator = new FileLocator(dir.toFile());
            Assert.assertTrue(locator.exists());
            Assert.assertTrue(locator.isDirectory());

            Files.createFile(dir.resolve("test.xml"));
            Locator child = locator.getChild("test.xml");
            Assert.assertTrue(child.getParent().equals(locator));
            Assert.assertTrue(child.exists());
            Assert.assertFalse(child.isDirectory());
        } finally {
            Files.list(dir).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException ex) {
                    Assert.fail();
                }
            });
        }
    }

    @Test
    public void testName() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            FileLocator locator = new FileLocator(tmpFile);
            Assert.assertEquals(locator.getName(), tmpFile.getName());
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testReadAllBytes() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            Files.write(tmpFile.toPath(), "0123456789abcdefghijklmnopqrstuvxyz".getBytes());
            FileLocator targetFile = new FileLocator(tmpFile);
            Assert.assertEquals(new String(targetFile.readAllBytes()), "0123456789abcdefghijklmnopqrstuvxyz");
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testReadBytes() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            Files.write(tmpFile.toPath(), "0123456789abcdefghijklmnopqrstuvxyz".getBytes());
            FileLocator targetFile = new FileLocator(tmpFile);
            Assert.assertEquals(new String(targetFile.readBytes(10, 20)), "abcdefghijk");
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testLength() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            Files.write(tmpFile.toPath(), "Testing1234".getBytes(StandardCharsets.UTF_8));
            FileLocator locator = new FileLocator(tmpFile);
            Assert.assertEquals(locator.length(), tmpFile.length());
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testMkDir() throws IOException {
        Path tempDirectory = Files.createTempDirectory("test");
        try {
            FileLocator targetFile = new FileLocator(new File(tempDirectory.toFile(), "t1"));
            Assert.assertFalse(targetFile.exists());
            Assert.assertTrue(targetFile.mkdir());
            Assert.assertTrue(targetFile.isDirectory());
            Assert.assertTrue(targetFile.getFile().delete());
        } finally {
            Assert.assertTrue(tempDirectory.toFile().delete());
        }
    }

    @Test
    public void testGetPath() throws IOException {
        final File tmpFile = File.createTempFile("test_file", ".tmp");
        try {
            FileLocator locator = new FileLocator(tmpFile);
            Assert.assertEquals(locator.getPath(), locator.getFile().getPath());
        } finally {
            Assert.assertTrue(tmpFile.delete());
        }
    }

    @Test
    public void testEquals() throws IOException {
        final File tmpFile1 = File.createTempFile("test_file", ".tmp");
        final File tmpFile2 = File.createTempFile("test_file", ".tmp");
        try {
            FileLocator targetFile1 = new FileLocator(tmpFile1);
            Locator targetFile2 = Locator.of(tmpFile1.getAbsolutePath(), null);
            Assert.assertTrue(targetFile1.equals(targetFile2));
            Assert.assertTrue(targetFile2.equals(targetFile1));
            FileLocator locator = new FileLocator(tmpFile2);
            Assert.assertFalse(locator.equals(targetFile2));
            Assert.assertFalse(targetFile2.equals(locator));
        } finally {
            Assert.assertTrue(tmpFile1.delete());
            Assert.assertTrue(tmpFile2.delete());
        }
    }
}
