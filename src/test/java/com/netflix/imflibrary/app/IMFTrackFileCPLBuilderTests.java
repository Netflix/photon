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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType;
import org.smpte_ra.schemas.st2067_2_2013.LocaleType;
import org.testng.Assert;
import org.testng.annotations.Test;

import testUtils.TestHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;


@Test(groups = "unit")
public class IMFTrackFileCPLBuilderTests {
    private static final Logger log = LoggerFactory.getLogger(IMFTrackFileCPLBuilderTests.class);

    @Test
    public void IMFTrackFileCPLBuilderTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Assert.assertTrue(imfTrackFileCPLBuilder.getCompositionPlaylist(imfErrorLogger).length() > 0);
    }

    @Test
    public void testMainEnsureRangeFileDeleted() throws Exception {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        String[] params = {
                inputFile.getAbsolutePath(),
                workingDirectory.getAbsolutePath()
        };
        IMFTrackFileCPLBuilder.main(params);
        File byteRangeFile = new File(workingDirectory, "range");
        assert !byteRangeFile.exists();
    }

    @Test(expectedExceptions = FileNotFoundException.class, expectedExceptionsMessageRegExp = "File .* does not exist")
    public void testMainFileMissing() throws Exception {
        File workingDirectory = Files.createTempDirectory(null).toFile();
        String[] params = {
                "/bad/path.mxf",
                workingDirectory.getAbsolutePath()
        };
        IMFTrackFileCPLBuilder.main(params);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Invalid parameters.*")
    public void testMainSingleParam() throws Exception {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        String[] params = {
                inputFile.getAbsolutePath(),
        };
        IMFTrackFileCPLBuilder.main(params);
    }

    @Test
    public void testBuildSampleCompositionTimeCode() throws IOException {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
        imfTrackFileCPLBuilder.buildSampleCompositionTimeCode(new IMFErrorLoggerImpl());
        assert imfTrackFileCPLBuilder.cplRoot.getCompositionTimecode().getTimecodeRate().equals(BigInteger.valueOf(48000));
        assert imfTrackFileCPLBuilder.cplRoot.getCompositionTimecode().getTimecodeStartAddress().equals("00:00:00:00");
        assert imfTrackFileCPLBuilder.cplRoot.getCompositionTimecode().isTimecodeDropFrame() == false;
    }

    @Test
    public void testBuildSampleLocaleList() throws Exception {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
        imfTrackFileCPLBuilder.buildSampleLocaleList();
        LocaleType locale = imfTrackFileCPLBuilder.cplRoot.getLocaleList().getLocale().get(0);
        assert locale.getAnnotation().getLanguage().equals("en");
        assert locale.getLanguageList().getLanguage().get(0).equals("en");
        assert locale.getRegionList().getRegion().get(0).equals("US");
        ContentMaturityRatingType rating = locale.getContentMaturityRatingList().getContentMaturityRating().get(0);
        assert rating.getAgency().equals("None");
        assert rating.getAudience().getScope().equals("General");
        assert rating.getAudience().getValue().equals("None");
        assert rating.getRating().equals("None");
    }

    @Test
    public void testUsage() {
        assert IMFTrackFileCPLBuilder.usage().contains("Usage");
        assert IMFTrackFileCPLBuilder.usage().contains("<inputFilePath> <workingDirectory>");
    }

}
