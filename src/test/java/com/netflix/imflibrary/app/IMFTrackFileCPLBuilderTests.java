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
import org.testng.Assert;
import org.testng.annotations.Test;

import testUtils.TestHelper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Test(groups = "unit")
public class IMFTrackFileCPLBuilderTests {

    @Test
    public void IMFTrackFileCPLBuilderTest() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        Path workingDirectory = Files.createTempDirectory(null);
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Assert.assertTrue(Files.size(imfTrackFileCPLBuilder.getCompositionPlaylist(imfErrorLogger)) > 0);
    }
}
