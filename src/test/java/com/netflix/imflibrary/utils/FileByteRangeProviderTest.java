/*
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.imflibrary.utils;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Test(groups = "unit")
public class FileByteRangeProviderTest
{
    private Path file;
    private FileByteRangeProvider fileByteRangeProvider;

    @BeforeClass
    public void setUp() throws Exception
    {
        String keyboard = "qwertyuiopasdfghjklzxcvbnm";
        this.file = Files.createTempFile("test_file",".tmp");
        BufferedWriter fileWriter = Files.newBufferedWriter(this.file);
        try
        {
            fileWriter.write(keyboard);
        }
        finally
        {
            if (fileWriter != null)
            {
                fileWriter.close();
            }
        }
        this.fileByteRangeProvider = new FileByteRangeProvider(this.file);
    }

    @AfterClass
    public void tearDown() throws Exception
    {
        Assert.assertTrue(Files.deleteIfExists(this.file));
    }

    @Test
    public void testGetResourceSize()
    {
        Assert.assertEquals(26L, this.fileByteRangeProvider.getResourceSize());
    }

    @Test
    public void testGetByteRangeWithRangeStart() throws IOException
    {
        Path workingDirectory = Files.createTempDirectory(null);
        Path path = this.fileByteRangeProvider.getByteRange(24, workingDirectory);
        Assert.assertEquals(2L, Files.size(path));
        List<String> lines = Files.readAllLines(path);
        Assert.assertEquals("nm", lines.get(0));
    }

    @Test
    public void testGetByteRange() throws IOException
    {
        Path workingDirectory = Files.createTempDirectory(null);
        Path path = this.fileByteRangeProvider.getByteRange(3, 9, workingDirectory);
        Assert.assertEquals(7L, Files.size(path));
        List<String> lines = Files.readAllLines(path);
        Assert.assertEquals("rtyuiop", lines.get(0));
    }

}
