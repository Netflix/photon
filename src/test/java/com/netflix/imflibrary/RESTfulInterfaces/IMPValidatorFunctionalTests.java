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
package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.IMFCompositionPlaylistUtils;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2016;
import com.netflix.imflibrary.writerTools.IMPBuilder;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import testUtils.TestHelper;

import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test(groups = "functional")
public class IMPValidatorFunctionalTests {

    private static final Logger logger = LoggerFactory.getLogger(IMFConstraints.class);

    @Test
    public void getPayloadTypeTest() throws IOException {
        //AssetMap
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.Unknown, 0L, resourceByteRangeProvider.getResourceSize());
        Assert.assertTrue(IMPValidator.getPayloadType(payloadRecord) == PayloadRecord.PayloadAssetType.AssetMap);

        //PKL
        inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.Unknown, 0L, resourceByteRangeProvider.getResourceSize());
        Assert.assertTrue(IMPValidator.getPayloadType(payloadRecord) == PayloadRecord.PayloadAssetType.PackingList);

        //CPL
        inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.Unknown, 0L, resourceByteRangeProvider.getResourceSize());
        Assert.assertTrue(IMPValidator.getPayloadType(payloadRecord) == PayloadRecord.PayloadAssetType.CompositionPlaylist);
    }

    @Test
    public void invalidPKLTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/PKL_befcd2d4-f35c-45d7-99bb-7f64b51b103c.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertEquals(errors.size(), 1);
        Assert.assertTrue(errors.get(0).getErrorDescription().contains("we only support the following schema URIs"));
    }

    @Test
    public void invalidPayloadPKLTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

        try {
             IMPValidator.validatePKL(payloadRecord);
             Assert.fail();
        } catch (IMFException e) {
            Assert.assertTrue(e.getMessage().contains("Payload asset type is"));
        }
    }

    @Test
    public void invalidPayloadAMTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

        try {
            IMPValidator.validateAssetMap(payloadRecord);
            Assert.fail();
        } catch (IMFException e) {
            Assert.assertTrue(e.getMessage().contains("Payload asset type is"));
        }
    }

    @Test
    public void invalidPayloadPKLandAMTest_01() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

        List<PayloadRecord> pklPayloads = new ArrayList<>();

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKLAndAssetMap(payloadRecord, pklPayloads);

        Assert.assertEquals(errors.size(), 1);
    }

    @Test
    public void invalidPayloadPKLandAMTest_02() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());

        List<PayloadRecord> pklPayloads = new ArrayList<>();

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKLAndAssetMap(payloadRecord, pklPayloads);

        Assert.assertEquals(errors.size(), 1);
    }

    @Test
    public void invalidPayloadPKLandAMTest_03() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());

        List<PayloadRecord> pklPayloads = new ArrayList<>();
        Path inputCPL = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProviderCPL = new FileByteRangeProvider(inputCPL);
        byte[] bytesCPL = resourceByteRangeProviderCPL.getByteRangeAsBytes(0, resourceByteRangeProviderCPL.getResourceSize() - 1);
        PayloadRecord payloadRecordCPL = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        pklPayloads.add(payloadRecordCPL);

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKLAndAssetMap(payloadRecord, pklPayloads);

        Assert.assertEquals(errors.size(), 2);
    }


    @Test
    public void validAssetMapTest() throws IOException {
        //File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/ASSETMAP.xml");
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateAssetMap(payloadRecord);
        Assert.assertEquals(errors.size(), 0);
    }

    @Test
    public void invalidAssetMapTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP_ERROR.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateAssetMap(payloadRecord);
        Assert.assertEquals(errors.size(), 5);
    }

    @Test
    public void validPKLTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertEquals(errors.size(), 0);
    }

    @Test
    public void validPKLAndAssetMapTest() throws IOException {
        Path pklInputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(pklInputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord pklPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<PayloadRecord> pklPayloadRecordList = new ArrayList<>();
        pklPayloadRecordList.add(pklPayloadRecord);

        Path assetMapInputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider assetMapResourceByteRangeProvider = new FileByteRangeProvider(assetMapInputFile);
        byte[] assetMapBytes = assetMapResourceByteRangeProvider.getByteRangeAsBytes(0, assetMapResourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord assetMapPayloadRecord = new PayloadRecord(assetMapBytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, assetMapResourceByteRangeProvider.getResourceSize());

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKLAndAssetMap(assetMapPayloadRecord, pklPayloadRecordList);
        Assert.assertEquals(errors.size(), 0);
    }

    @Test
    public void validCPLTest_2013Schema() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateCPL(payloadRecord);
        List<ErrorLogger.ErrorObject> fatalErrors = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL))
                .collect(Collectors.toList());
        Assert.assertTrue(fatalErrors.size() == 0);
    }

    @Test
    public void invalidCPLTest_2013Schema() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c" +
                        "-d1e6c6cb62b4_error.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateCPL(payloadRecord);
        List<ErrorLogger.ErrorObject> fatalErrors = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL))
                .collect(Collectors.toList());
        Assert.assertEquals(fatalErrors.size(), 27);
    }

    @Test
    public void validCPLTest_2016Schema() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4-2016Schema.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateCPL(payloadRecord);
        List<ErrorLogger.ErrorObject> fatalErrors = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL))
                .collect(Collectors.toList());
        Assert.assertTrue(fatalErrors.size() == 0);
    }


    @Test
    public void validateEssencesHeaderPartitionTest() throws IOException {
        List<PayloadRecord> essencesHeaderPartition = new ArrayList<>();

        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_LAS20.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_LAS51.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        for (PayloadRecord payload : essencesHeaderPartition) {
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payload.getPayload()),
                    0L,
                    (long)payload.getPayload().length,
                    imfErrorLogger);
            imfErrorLogger.addAllErrors(IMFConstraints.checkMXFHeaderMetadata(headerPartition));
        }

        for(ErrorLogger.ErrorObject errorObject : imfErrorLogger.getErrors()){
            logger.error(errorObject.toString());
        }
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 8);
    }

    @Test
    public void cplConformanceNegativeTest() throws IOException, SAXException, JAXBException, URISyntaxException {

        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);

        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        //PayloadRecord cplPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);

        List<PayloadRecord> essencesHeaderPartition = new ArrayList<>();

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        essencesHeaderPartition.add(payloadRecord);

        //List<ErrorLogger.ErrorObject> errors = IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, essencesHeaderPartition);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateComposition(imfCompositionPlaylist, essencesHeaderPartition);

        Assert.assertEquals(errors.size(), 9);
        //The following error occurs because we do not yet support TimedText Virtual Tracks in Photon and the EssenceDescriptor in the EDL corresponds to a TimedText Virtual Track whose entry is commented out in the CPL.
        Assert.assertTrue(errors.get(0).toString().contains("ERROR-EssenceDescriptorID 3febc096-8727-495d-8715-bb5398d98cfe in the CPL EssenceDescriptorList is not referenced by any resource in any of the Virtual Tracks in the CPL"));
    }

    @Test
    public void cplVirtualTrackConformanceNegativeTest() throws IOException, SAXException, JAXBException, URISyntaxException {

        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);

        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        //PayloadRecord cplPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);

        //List<ErrorLogger.ErrorObject> errors = IMPValidator.validateCPL(cplPayloadRecord);//Validates the CPL.
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateComposition(imfCompositionPlaylist, null);//Validates the CPL.

        Assert.assertEquals(errors.size(), 1);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord2 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        List<PayloadRecord> payloadRecords = new ArrayList<PayloadRecord>() {{ add(payloadRecord2); }};
        //errors = IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, payloadRecords);
        errors = IMPValidator.validateComposition(imfCompositionPlaylist, payloadRecords);
        Assert.assertEquals(errors.size(), 7);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord1 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        payloadRecords = new ArrayList<PayloadRecord>() {{ add(payloadRecord1); }};
        //errors = IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, payloadRecords);
        errors = IMPValidator.validateComposition(imfCompositionPlaylist, payloadRecords);
        Assert.assertEquals(errors.size(), 7);

        inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord0 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        payloadRecords = new ArrayList<PayloadRecord>() {{ add(payloadRecord0); }};
        //errors = IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, payloadRecords);
        errors = IMPValidator.validateComposition(imfCompositionPlaylist, payloadRecords);
        Assert.assertEquals(errors.size(), 4);
    }

    @Test
    public void cplVirtualTrackConformanceNegativeTestNamespaceURIInconsitencies() throws IOException, SAXException, JAXBException, URISyntaxException {

        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL-de6d2644-e84c-432d-98d5-98d89271d082.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);

        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        //PayloadRecord cplPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

        List<PayloadRecord> essencesHeaderPartition = new ArrayList<>();
        //List<ErrorLogger.ErrorObject> errors = IMPValidator.validateCPL(cplPayloadRecord);//Validates the CPL.
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateComposition(imfCompositionPlaylist, null);//Validates the CPL.
        Assert.assertEquals(errors.size(), 2);

        inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/NYCbCrLT_3840x2160x2398_full_full.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord2 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        List<PayloadRecord> payloadRecords = new ArrayList<PayloadRecord>() {{ add(payloadRecord2); }};
        //errors = IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, payloadRecords);
        errors = IMPValidator.validateComposition(imfCompositionPlaylist, payloadRecords);
        Assert.assertEquals(errors.size(), 19);

        inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/NYCbCrLT_3840x2160x2chx24bitx30.03sec.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord1 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, resourceByteRangeProvider.getResourceSize());
        payloadRecords = new ArrayList<PayloadRecord>() {{ add(payloadRecord1); }};
        errors = IMPValidator.validateComposition(imfCompositionPlaylist, payloadRecords);
        Assert.assertEquals(errors.size(), 5);
    }

    @Test
    public void cplVirtualTrackConformancePositiveTest()
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);

        Path headerPartition1 = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/NYCbCrLT_3840x2160x2chx24bitx30.03sec.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        HeaderPartition headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkMXFHeaderMetadata(headerPartitionOP1A, imfErrorLogger);
        Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage) genericPackage;
        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "NYCbCrLT_3840x2160x2chx24bitx30.03sec.mxf",
                bytes.length));


        Path headerPartition2 = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/NYCbCrLT_3840x2160x2398_full_full.mxf.hdr");
        resourceByteRangeProvider = new FileByteRangeProvider(headerPartition2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        byteProvider = new ByteArrayDataProvider(bytes);
        headerPartition = new HeaderPartition(byteProvider,
                0L,
                bytes.length,
                imfErrorLogger);
        headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
        headerPartitionIMF = IMFConstraints.checkMXFHeaderMetadata(headerPartitionOP1A, imfErrorLogger);
        preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        filePackage = (SourcePackage) genericPackage;
        packageUUID = filePackage.getPackageMaterialNumberasUUID();

        imfTrackFileMetadataMap.put(packageUUID, new IMPBuilder.IMFTrackFileMetadata(bytes,
                IMFUtils.generateSHA1Hash(new ByteArrayByteRangeProvider(bytes)),
                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                "NYCbCrLT_3840x2160x2398_full_full.mxf",
                bytes.length));

        //Create a temporary working directory under home
        Path tempPath = Files.createTempDirectory("IMFDocuments");

        IMPBuilder.buildIMP_2016("IMP",
                "Netflix",
                imfCompositionPlaylist.getEssenceVirtualTracks(),
                imfCompositionPlaylist.getEditRate(),
                "http://www.smpte-ra.org/schemas/2067-21/2016",
                imfTrackFileMetadataMap,
                tempPath);

        AtomicBoolean cplFound = new AtomicBoolean(false);
        AtomicReference<Path> cplFile = new AtomicReference<>();

        try (Stream<Path> filesStream = Files.list(tempPath)) {
            filesStream.forEach(file -> {
                if (file.getFileName().toString().contains("CPL-")) {
                    cplFound.set(true);
                    cplFile.set(file);
                }

                System.out.println(file.getFileName());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(cplFound.get() == true);

        ResourceByteRangeProvider fileByteRangeProvider = new FileByteRangeProvider(cplFile.get());
        byte[] documentBytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord = new PayloadRecord(documentBytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, 0L);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateCPL(cplPayloadRecord);
        Assert.assertEquals(errors.size(), 2);

        imfCompositionPlaylist = new IMFCompositionPlaylist(fileByteRangeProvider);
        fileByteRangeProvider = new FileByteRangeProvider(headerPartition1);
        byte[] headerPartition1_bytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        PayloadRecord headerPartition1PayloadRecord = new PayloadRecord(headerPartition1_bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, 0L);

        fileByteRangeProvider = new FileByteRangeProvider(headerPartition2);
        byte[] headerPartition2_bytes = fileByteRangeProvider.getByteRangeAsBytes(0, fileByteRangeProvider.getResourceSize()-1);
        PayloadRecord headerPartition2PayloadRecord = new PayloadRecord(headerPartition2_bytes, PayloadRecord.PayloadAssetType.EssencePartition, 0L, 0L);
        List<PayloadRecord> essencesHeaderPartitionPayloads = new ArrayList<>();
        essencesHeaderPartitionPayloads.add(headerPartition2PayloadRecord);

        //List<ErrorLogger.ErrorObject> conformanceErrors = IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, essencesHeaderPartitionPayloads);
        List<ErrorLogger.ErrorObject> conformanceErrors = IMPValidator.validateComposition(imfCompositionPlaylist, essencesHeaderPartitionPayloads);

        Assert.assertEquals(conformanceErrors.size(), 4);
    }

    @Test
    public void cplMergeabilityNegativeTest() throws IOException {

        Path cpl1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        Path cpl2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");

        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(cpl1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord1 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord1));

        resourceByteRangeProvider = new FileByteRangeProvider(cpl2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord2 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord2));

        Assert.assertEquals(errors.size(), 6);

        errors.clear();

        errors.addAll(IMFCompositionPlaylistUtils.isCPLMergeable(cplPayloadRecord1, new ArrayList<PayloadRecord>() {{
            add(cplPayloadRecord2);
        }}));

        Assert.assertEquals(errors.size(), 2);
    }

    @Test
    public void cplMergeabilityPositiveTest() throws IOException {

        Path cpl1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        Path cpl2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_supplemental.xml");

        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(cpl1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord1 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord1));

        resourceByteRangeProvider = new FileByteRangeProvider(cpl2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord2 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord2));

        Assert.assertEquals(errors.size(), 2);

        errors.clear();

        errors.addAll(IMFCompositionPlaylistUtils.isCPLMergeable(cplPayloadRecord1, new ArrayList<PayloadRecord>() {{
            add(cplPayloadRecord2);
        }}));

        Assert.assertEquals(errors.size(), 0);
    }
}
