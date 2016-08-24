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
import com.netflix.imflibrary.KLVPacket;
import org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.writerTools.IMFCPLObjectFieldsFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import testUtils.TestHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Test(groups = "functional")
public class IMFTrackFileCPLBuilderFunctionalTests {

    @Test
    public void IMFCPLFactoryTest(){
        org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType = IMFCPLObjectFieldsFactory.constructCompositionPlaylistType_2013();
        Assert.assertTrue(compositionPlaylistType.getContentTitle() != null);
        Assert.assertTrue(compositionPlaylistType.getContentVersionList() != null);
        Assert.assertTrue(compositionPlaylistType.getContentVersionList().getContentVersion() != null);
        Assert.assertTrue(compositionPlaylistType.getEssenceDescriptorList() != null);
        Assert.assertTrue(compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor() != null);
        Assert.assertTrue(compositionPlaylistType.getLocaleList() != null);
        Assert.assertTrue(compositionPlaylistType.getLocaleList().getLocale() != null);
        Assert.assertTrue(compositionPlaylistType.getSegmentList() != null);
        Assert.assertTrue(compositionPlaylistType.getSegmentList().getSegment() != null);
    }

    @Test
    public void RegXMLLibTest() throws IOException, ParserConfigurationException, TransformerException {
        /*AudioEssence*/
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, new FileByteRangeProvider(inputFile));
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = imfTrackFileReader.getEssenceDescriptors(imfErrorLogger);
        Assert.assertTrue(essenceDescriptors.size() == 1);
        List<KLVPacket.Header> subDescriptorHeaders = imfTrackFileReader.getSubDescriptorKLVHeader(essenceDescriptors.get(0), imfErrorLogger);
        /* create dom */
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        Assert.assertTrue(imfTrackFileCPLBuilder.getEssenceDescriptorAsXMLFile(document, imfTrackFileReader.getEssenceDescriptorKLVHeader(essenceDescriptors.get(0)), subDescriptorHeaders) != null);
    }

    @Test
    public void EssenceDescriptorTest() throws IOException, ParserConfigurationException, TransformerException {
        /*Audio Essence*/
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf.hdr");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        byte[] headerPartitionBytes = Files.readAllBytes(Paths.get(inputFile.toURI()));
        ByteProvider byteProvider = new ByteArrayDataProvider(headerPartitionBytes);
        HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, headerPartitionBytes.length, imfErrorLogger);
        List<InterchangeObject.InterchangeObjectBO> list = headerPartition.getEssenceDescriptors();
        List<KLVPacket.Header> essenceDescriptorHeaders = new ArrayList<>();
        for(InterchangeObject.InterchangeObjectBO descriptorBO : list){
            essenceDescriptorHeaders.add(descriptorBO.getHeader());
        }
        Assert.assertTrue(essenceDescriptorHeaders.size() == 1);

        /*Image Essence*/
        inputFile = TestHelper.findResourceByPath("CHIMERA_NETFLIX_2398.mxf.hdr");
        headerPartitionBytes = Files.readAllBytes(Paths.get(inputFile.toURI()));
        byteProvider = new ByteArrayDataProvider(headerPartitionBytes);
        headerPartition = new HeaderPartition(byteProvider, 0L, headerPartitionBytes.length, imfErrorLogger);
        list = headerPartition.getEssenceDescriptors();
        essenceDescriptorHeaders = new ArrayList<>();
        for(InterchangeObject.InterchangeObjectBO descriptorBO : list){
            essenceDescriptorHeaders.add(descriptorBO.getHeader());
        }
        Assert.assertTrue(essenceDescriptorHeaders.size() == 1);
    }
}
