/*
 *
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
 *
 */
package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Test(groups = "functional")
public class MXFUtilsFunctionalTests {

    private static final Logger logger = LoggerFactory.getLogger(MXFUtilsFunctionalTests.class);

    @Test
    public void getRandomIndexPackSizeTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("IMFTrackFiles/TearsOfSteel_4k_Test_Master_Audio_002.mxf");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = MXFUtils.getRandomIndexPackSize(payloadRecord);
        Assert.assertTrue(randomIndexPackSize == 72);
    }

    @Test
    public void getAllPartitionByteOffsetsTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("IMFTrackFiles/TearsOfSteel_4k_Test_Master_Audio_002.mxf");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = MXFUtils.getRandomIndexPackSize(payloadRecord);
        Assert.assertTrue(randomIndexPackSize == 72);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;

        Assert.assertTrue(rangeStart >= 0);

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = MXFUtils.getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize);
        Assert.assertTrue(partitionByteOffsets.size() == 4);
        Assert.assertTrue(partitionByteOffsets.get(0) == 0);
        Assert.assertTrue(partitionByteOffsets.get(1) == 11868);
        Assert.assertTrue(partitionByteOffsets.get(2) == 12104);
        Assert.assertTrue(partitionByteOffsets.get(3) == 223644);
    }

}
