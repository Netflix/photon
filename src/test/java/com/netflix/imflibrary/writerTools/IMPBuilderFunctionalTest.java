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
package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import testUtils.TestHelper;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A test for the IMF Master Package Builder
 */
@Test(groups = "functional")
public class IMPBuilderFunctionalTest {

    @Test
    public void impBuilderTest_2016()
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        //File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, imfErrorLogger);

        File headerPartition1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        HeaderPartition headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage)genericPackage;
        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "Netflix_Plugfest_Oct2015.mxf",
                bytes.length));


        File headerPartition2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        byteProvider = new ByteArrayDataProvider(bytes);
        headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        filePackage = (SourcePackage)genericPackage;
        packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "Netflix_Plugfest_Oct2015_ENG20.mxf",
                bytes.length));

        File headerPartition3 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition3);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        byteProvider = new ByteArrayDataProvider(bytes);
        headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        filePackage = (SourcePackage)genericPackage;
        packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "Netflix_Plugfest_Oct2015_ENG51.mxf",
                bytes.length));

        /**
         * Create a temporary working directory under home
         */
        Path tempPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMFDocuments");
        File tempDir = tempPath.toFile();

        IMPBuilder.buildIMP_2016("IMP",
                "Netflix",
                applicationComposition.getVirtualTracks(),
                applicationComposition.getEditRate(),
                imfTrackFileMetadataMap,
                tempDir);

        boolean assetMapFound = false;
        boolean pklFound = false;
        boolean cplFound = false;
        File assetMapFile = null;
        File pklFile = null;
        File cplFile = null;

        for(File file : tempDir.listFiles()){
            if(file.getName().contains("ASSETMAP.xml")){
                assetMapFound = true;
                assetMapFile = file;
            }
            else if(file.getName().contains("PKL-")){
                pklFound = true;
                pklFile = file;
            }
            else if(file.getName().contains("CPL-")){
                cplFound = true;
                cplFile = file;
            }
        }
        Assert.assertTrue(assetMapFound == true);
        Assert.assertTrue(pklFound == true);
        Assert.assertTrue(cplFound == true);

        ResourceByteRangeProvider fileByteRangeProvider = new FileByteRangeProvider(assetMapFile);
        byte[] documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, 0L);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateAssetMap(payloadRecord);
        Assert.assertTrue(errors.size() == 0);

        fileByteRangeProvider = new FileByteRangeProvider(pklFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.PackingList, 0L, 0L);
        errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);

        fileByteRangeProvider = new FileByteRangeProvider(cplFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, 0L);
        errors = IMPValidator.validateCPL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void impBuilderTest_2013_1()
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        //File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, imfErrorLogger);

        File headerPartition1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        HeaderPartition headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage)genericPackage;
        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "Netflix_Plugfest_Oct2015.mxf",
                bytes.length));


        File headerPartition2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        byteProvider = new ByteArrayDataProvider(bytes);
        headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        filePackage = (SourcePackage)genericPackage;
        packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "Netflix_Plugfest_Oct2015_ENG20.mxf",
                bytes.length));

        File headerPartition3 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition3);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        byteProvider = new ByteArrayDataProvider(bytes);
        headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        filePackage = (SourcePackage)genericPackage;
        packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "Netflix_Plugfest_Oct2015_ENG51.mxf",
                bytes.length));

        /**
         * Create a temporary working directory under home
         */
        Path tempPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMFDocuments");
        File tempDir = tempPath.toFile();

        IMPBuilder.buildIMP_2013("IMP",
                "Netflix",
                applicationComposition.getVirtualTracks(),
                applicationComposition.getEditRate(),
                imfTrackFileMetadataMap,
                tempDir);

        boolean assetMapFound = false;
        boolean pklFound = false;
        boolean cplFound = false;
        File assetMapFile = null;
        File pklFile = null;
        File cplFile = null;

        for(File file : tempDir.listFiles()){
            if(file.getName().contains("ASSETMAP.xml")){
                assetMapFound = true;
                assetMapFile = file;
            }
            else if(file.getName().contains("PKL-")){
                pklFound = true;
                pklFile = file;
            }
            else if(file.getName().contains("CPL-")){
                cplFound = true;
                cplFile = file;
            }
        }
        Assert.assertTrue(assetMapFound == true);
        Assert.assertTrue(pklFound == true);
        Assert.assertTrue(cplFound == true);

        ResourceByteRangeProvider fileByteRangeProvider = new FileByteRangeProvider(assetMapFile);
        byte[] documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, 0L);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateAssetMap(payloadRecord);
        Assert.assertTrue(errors.size() == 0);

        fileByteRangeProvider = new FileByteRangeProvider(pklFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.PackingList, 0L, 0L);
        errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);

        fileByteRangeProvider = new FileByteRangeProvider(cplFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, 0L);
        errors = IMPValidator.validateCPL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void impBuilderTest_2013_2()
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, imfErrorLogger);

        File headerPartition1 = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/NYCbCrLT_3840x2160x2chx24bitx30.03sec.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        HeaderPartition headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage)genericPackage;
        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "NYCbCrLT_3840x2160x2chx24bitx30.03sec.mxf",
                bytes.length));


        File headerPartition2 = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/NYCbCrLT_3840x2160x2398_full_full.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        byteProvider = new ByteArrayDataProvider(bytes);
        headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
        preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        filePackage = (SourcePackage)genericPackage;
        packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "NYCbCrLT_3840x2160x2398_full_full.mxf",
                bytes.length));

        /**
         * Create a temporary working directory under home
         */
        Path tempPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMFDocuments");
        File tempDir = tempPath.toFile();

        IMPBuilder.buildIMP_2013("IMP",
                "Netflix",
                applicationComposition.getVirtualTracks(),
                applicationComposition.getEditRate(),
                imfTrackFileMetadataMap,
                tempDir);

        boolean assetMapFound = false;
        boolean pklFound = false;
        boolean cplFound = false;
        File assetMapFile = null;
        File pklFile = null;
        File cplFile = null;

        for(File file : tempDir.listFiles()){
            if(file.getName().contains("ASSETMAP.xml")){
                assetMapFound = true;
                assetMapFile = file;
            }
            else if(file.getName().contains("PKL-")){
                pklFound = true;
                pklFile = file;
            }
            else if(file.getName().contains("CPL-")){
                cplFound = true;
                cplFile = file;
            }
        }
        Assert.assertTrue(assetMapFound == true);
        Assert.assertTrue(pklFound == true);
        Assert.assertTrue(cplFound == true);

        ResourceByteRangeProvider fileByteRangeProvider = new FileByteRangeProvider(assetMapFile);
        byte[] documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, 0L);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateAssetMap(payloadRecord);
        Assert.assertTrue(errors.size() == 0);

        fileByteRangeProvider = new FileByteRangeProvider(pklFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.PackingList, 0L, 0L);
        errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);

        fileByteRangeProvider = new FileByteRangeProvider(cplFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, 0L);
        errors = IMPValidator.validateCPL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);
    }
}
