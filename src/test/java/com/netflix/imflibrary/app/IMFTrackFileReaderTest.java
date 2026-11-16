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
import com.netflix.imflibrary.validation.EssenceValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

@Test(groups = "unit")
public class IMFTrackFileReaderTest
{
    @Test
    public void IMFTrackFileReaderTest() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        Path workingDirectory = Files.createTempDirectory(null);
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        Assert.assertTrue(imfTrackFileReader.toString().length() > 0);
    }

    @Test(expectedExceptions = MXFException.class, expectedExceptionsMessageRegExp = "RandomIndexPackSize = .*")
    public void badRandomIndexPackLength() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        Path workingDirectory = Files.createTempDirectory(null);
        ResourceByteRangeProvider resourceByteRangeProvider = mock(ResourceByteRangeProvider.class);
        when(resourceByteRangeProvider.getResourceSize()).thenReturn(16L);
        when(resourceByteRangeProvider.getByteRange(anyLong(), anyLong(), any(Path.class))).thenReturn(inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        imfTrackFileReader.getRandomIndexPack(imfErrorLogger);
    }

    @Test
    public void unrecognizedSequenceNamespaceIsReported() throws IOException
    {
        // no validator is registered for this namespace, so the essence kind specific checks cannot run: the reader must say so
        String unrecognizedNamespace = "http://www.smpte-ra.org/schemas/2067-2/2025";
        Path inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        new IMFTrackFileReader(Files.createTempDirectory(null), new FileByteRangeProvider(inputFile),
                EssenceValidationContext.forSequenceNamespace(unrecognizedNamespace)).getHeaderPartitionIMF(imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().stream()
                .filter(e -> e.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING
                        && e.getErrorDescription().contains("Namespace not supported: " + unrecognizedNamespace))
                .count(), 1);

        // without a context (or without a sequence namespace) there is nothing to report
        for (EssenceValidationContext context : new EssenceValidationContext[] {null, EssenceValidationContext.none()}) {
            IMFErrorLogger quietLogger = new IMFErrorLoggerImpl();
            IMFTrackFileReader reader = context == null
                    ? new IMFTrackFileReader(Files.createTempDirectory(null), new FileByteRangeProvider(inputFile))
                    : new IMFTrackFileReader(Files.createTempDirectory(null), new FileByteRangeProvider(inputFile), context);
            reader.getHeaderPartitionIMF(quietLogger);
            Assert.assertTrue(quietLogger.getErrors().stream().noneMatch(e -> e.getErrorDescription().contains("Namespace not supported")));
        }
    }
}
