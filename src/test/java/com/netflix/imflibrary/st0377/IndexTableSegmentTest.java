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

package com.netflix.imflibrary.st0377;

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IndexTableSegmentTest
{
    @Test
    public void indexTableSegmentTest() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("Netflix_Ident_23976_3840x2160_177AR.mxf.idx");
        byte[] bytes = Files.readAllBytes(inputFile);
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        KLVPacket.Header header = new KLVPacket.Header(byteProvider, 0L);
        IndexTableSegment indexTableSegment = new IndexTableSegment(byteProvider, header);
        Assert.assertTrue(indexTableSegment.toString().length() > 0);
        Assert.assertEquals(indexTableSegment.getIndexEntries().size(), 96);
        Assert.assertEquals(indexTableSegment.getIndexEntries().get(1).getStreamOffset(), 28127L);
    }
}
