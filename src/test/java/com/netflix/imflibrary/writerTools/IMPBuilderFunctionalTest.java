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
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A test for the IMF Master Package Builder
 */
@Test(groups = "functional")
public class IMPBuilderFunctionalTest {

    @DataProvider(name = "cplList")
    private Object[][] getCplList() {
        return new Object[][] {
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml", "2013", true, 1},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_duplicate_source_encoding_element.xml", "2013", true, 1},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml", "2013", false, 0},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_duplicate_source_encoding_element.xml", "2013", false, 0},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml", "2016", true, 1},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_duplicate_source_encoding_element.xml", "2016", true, 1},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml", "2016", false, 0},
                {"TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_duplicate_source_encoding_element.xml", "2016", false, 0}
        };
    }

    @Test(dataProvider = "cplList")
    public void impBuilderTest(String cplFilePath, String schemaVersion, boolean useHeaderPartition, int expectedCPLErrors)
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        File inputFile = TestHelper.findResourceByPath(cplFilePath);
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, imfErrorLogger);
        buildIMPAndValidate(applicationComposition, schemaVersion, expectedCPLErrors, useHeaderPartition, imfErrorLogger);
    }


    private void buildIMPAndValidate(ApplicationComposition applicationComposition, String schemaVersion, int cplErrorsExpected, boolean useHeaderPartition, IMFErrorLogger imfErrorLogger)
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        /**
         * Create a temporary working directory under home
         */
        Path tempPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMFDocuments");
        File tempDir = tempPath.toFile();

        if(useHeaderPartition) {
            if (schemaVersion.equals("2016")) {
                IMPBuilder.buildIMP_2016("IMP",
                        "Netflix",
                        applicationComposition.getEssenceVirtualTracks(),
                        applicationComposition.getEditRate(),
                        "http://www.smpte-ra.org/schemas/2067-21/2016",
                        buildTrackFileMetadataMap(imfErrorLogger),
                        tempDir);
            } else if (schemaVersion.equals("2013")) {
                IMPBuilder.buildIMP_2013("IMP",
                        "Netflix",
                        applicationComposition.getEssenceVirtualTracks(),
                        applicationComposition.getEditRate(),
                        "http://www.smpte-ra.org/schemas/2067-21/2016",
                        buildTrackFileMetadataMap(imfErrorLogger),
                        tempDir);
            }
        } else {
            if (schemaVersion.equals("2016")) {
                IMPBuilder.buildIMP_2016("IMP",
                        "Netflix",
                        applicationComposition.getEssenceVirtualTracks(),
                        applicationComposition.getEditRate(),
                        "http://www.smpte-ra.org/schemas/2067-21/2016",
                        buildTrackFileInfoMap(imfErrorLogger),
                        tempDir,
                        applicationComposition.getEssenceDescriptorDomNodeMap());
            } else if (schemaVersion.equals("2013")) {
                IMPBuilder.buildIMP_2013("IMP",
                        "Netflix",
                        applicationComposition.getEssenceVirtualTracks(),
                        applicationComposition.getEditRate(),
                        "http://www.smpte-ra.org/schemas/2067-21/2016",
                        buildTrackFileInfoMap(imfErrorLogger),
                        tempDir,
                        applicationComposition.getEssenceDescriptorDomNodeMap());
            }
        }

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
        Assert.assertEquals(errors.size(), 0);

        fileByteRangeProvider = new FileByteRangeProvider(pklFile);
        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.PackingList, 0L, 0L);
        errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertEquals(errors.size(), 0);

        fileByteRangeProvider = new FileByteRangeProvider(cplFile);

        documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, 0L);
        errors = IMPValidator.validateCPL(payloadRecord);
        Assert.assertEquals(errors.size(), cplErrorsExpected);
    }

    private Map<UUID, IMPBuilder.IMFTrackFileMetadata> buildTrackFileMetadataMap(IMFErrorLogger imfErrorLogger) throws IOException, NoSuchAlgorithmException {
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        ResourceByteRangeProvider resourceByteRangeProvider;
        List<String> fileNames = Arrays.asList("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015.mxf.hdr", "TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr", "TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");
        for(String fileName: fileNames) {
            File headerPartition1 = TestHelper.findResourceByPath(fileName);
            resourceByteRangeProvider = new FileByteRangeProvider(headerPartition1);
            byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
            ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
            HeaderPartition headerPartition = new HeaderPartition(byteProvider,
                    0L,
                    bytes.length,
                    imfErrorLogger);
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
            Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage) genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

            imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                    IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                    fileName,
                    bytes.length));
        }
        return imfTrackFileMetadataMap;
    }

    private Map<UUID, IMPBuilder.IMFTrackFileInfo> buildTrackFileInfoMap(IMFErrorLogger imfErrorLogger) throws IOException, NoSuchAlgorithmException {
        Map<UUID, IMPBuilder.IMFTrackFileInfo> uuidimfTrackFileInfoMap = new HashMap<>();
        String hash1 = "yCsxE1M6xmEGkVoXAWWjvfq2VHM=";
        uuidimfTrackFileInfoMap.put(UUIDHelper.fromUUIDAsURNStringToUUID("urn:uuid:ec9f8003-655e-438a-b30a-d7700ec4cb6f"),
                new IMPBuilder.IMFTrackFileInfo(hash1.getBytes(),
                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                    "Netflix_Plugfest_Oct2015.mxf",
                    10517511198L, false));

            String hash2 = "9zit4G2zsmwpLqwXwFEJTu7UG50=";
        uuidimfTrackFileInfoMap.put(UUIDHelper.fromUUIDAsURNStringToUUID("urn:uuid:7be07495-1aaa-4a69-8b92-3ec162122b34"),
                new IMPBuilder.IMFTrackFileInfo(hash2.getBytes(),
                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                    "Netflix_Plugfest_Oct2015_ENG20.mxf",
                    94532279L, false));

            String hash3 = "9zit4G2zsmwpLqwXwFEJTu7UG50=";
        uuidimfTrackFileInfoMap.put(UUIDHelper.fromUUIDAsURNStringToUUID("urn:uuid:c808001c-da54-4295-a721-dcaa00659699"),
                new IMPBuilder.IMFTrackFileInfo(hash3.getBytes(),
                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                    "Netflix_Plugfest_Oct2015_ENG51.mxf",
                    94532279L, false));
        return uuidimfTrackFileInfoMap;
    }

}
