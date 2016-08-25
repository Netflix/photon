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

package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.MXFException;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.Mockito.*;

@Test(groups = "unit")
public class IMFTrackFileReaderTest
{
    @Test
    public void IMFTrackFileReaderTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        Assert.assertTrue(imfTrackFileReader.toString().length() > 0);
    }

    @Test(expectedExceptions = MXFException.class, expectedExceptionsMessageRegExp = "RandomIndexPackSize = .*")
    public void badRandomIndexPackLength() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        ResourceByteRangeProvider resourceByteRangeProvider = mock(ResourceByteRangeProvider.class);
        when(resourceByteRangeProvider.getResourceSize()).thenReturn(16L);
        when(resourceByteRangeProvider.getByteRange(anyLong(), anyLong(), any(File.class))).thenReturn(inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        imfTrackFileReader.getRandomIndexPack(imfErrorLogger);
    }
}
