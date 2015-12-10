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

@Test(groups = "unit")
public class FileByteRangeProviderTest
{
    private File file;
    private FileByteRangeProvider fileByteRangeProvider;

    @BeforeClass
    public void setUp() throws Exception
    {
        String keyboard = "qwertyuiopasdfghjklzxcvbnm";
        this.file = File.createTempFile("test_file",".tmp");
        FileWriter fileWriter = new FileWriter(this.file);
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
        Assert.assertTrue(this.file.delete());
    }

    @Test
    public void testGetResourceSize()
    {
        Assert.assertEquals(26L, this.fileByteRangeProvider.getResourceSize());
    }

    @Test
    public void testGetByteRangeWithRangeStart() throws IOException
    {
        File workingDirectory = Files.createTempDirectory(null).toFile();
        File file = this.fileByteRangeProvider.getByteRange(24, workingDirectory);
        Assert.assertEquals(2L, file.length());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        Assert.assertEquals("nm", bufferedReader.readLine());
    }

    @Test
    public void testGetByteRange() throws IOException
    {
        File workingDirectory = Files.createTempDirectory(null).toFile();
        File file = this.fileByteRangeProvider.getByteRange(3, 9, workingDirectory);
        Assert.assertEquals(7L, file.length());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        Assert.assertEquals("rtyuiop", bufferedReader.readLine());
    }

}
