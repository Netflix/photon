package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Test(groups = "functional")
public class IMPValidatorFunctionalTests {

    @Test
    public void invalidPKLTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/PKL_befcd2d4-f35c-45d7-99bb-7f64b51b103c.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertTrue(errors.size() > 0);
    }

    @Test
    public void validAssetMapTest() throws IOException {
        //File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/ASSETMAP.xml");
        File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateAssetMap(payloadRecord);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void validPKLTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validatePKL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void validPKLAndAssetMapTest() throws IOException {
        File pklInputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(pklInputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord pklPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<PayloadRecord> pklPayloadRecordList = new ArrayList<>();
        pklPayloadRecordList.add(pklPayloadRecord);

        File assetMapInputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider assetMapResourceByteRangeProvider = new FileByteRangeProvider(assetMapInputFile);
        byte[] assetMapBytes = assetMapResourceByteRangeProvider.getByteRangeAsBytes(0, assetMapResourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord assetMapPayloadRecord = new PayloadRecord(assetMapBytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, assetMapResourceByteRangeProvider.getResourceSize());

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKLAndAssetMap(assetMapPayloadRecord, pklPayloadRecordList);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void validCPLTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/CPL_a453b63a-cf4d-454a-8c34-141f560c0100.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateCPL(payloadRecord);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void getRandomIndexPackSizeTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4K_Test_Master_Audio_002.mxf");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = IMPValidator.getRandomIndexPackSize(payloadRecord);
        Assert.assertTrue(randomIndexPackSize == 72);
    }

    @Test
    public void getAllPartitionByteOffsetsTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4K_Test_Master_Audio_002.mxf");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = IMPValidator.getRandomIndexPackSize(payloadRecord);
        Assert.assertTrue(randomIndexPackSize == 72);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;

        Assert.assertTrue(rangeStart >= 0);

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = IMPValidator.getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize);
        Assert.assertTrue(partitionByteOffsets.size() == 4);
        Assert.assertTrue(partitionByteOffsets.get(0) == 0);
        Assert.assertTrue(partitionByteOffsets.get(1) == 11868);
        Assert.assertTrue(partitionByteOffsets.get(2) == 12104);
        Assert.assertTrue(partitionByteOffsets.get(3) == 223644);
    }

    @Test
    public void validateEssencesHeaderPartitionTest() throws IOException {
        List<PayloadRecord> essencesHeaderPartition = new ArrayList<>();

        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
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

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateIMFEssenceComponentHeaderMetadata(essencesHeaderPartition);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void cplConformanceNegativeTest() throws IOException {

        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

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

        List<ErrorLogger.ErrorObject> errors = IMPValidator.isCPLConformed(cplPayloadRecord, essencesHeaderPartition);
        Assert.assertTrue(errors.size() == 1);
    }

    @Test
    public void cplMergeabilityNegativeTest() throws IOException {

        File cpl1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        File cpl2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4.xml");

        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(cpl1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord1 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord1));

        resourceByteRangeProvider = new FileByteRangeProvider(cpl2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord2 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord2));


        if(errors.size() == 0) {
            errors.addAll(IMPValidator.isCPLMergeable(cplPayloadRecord1, new ArrayList<PayloadRecord>() {{
                add(cplPayloadRecord2);
            }}));
        }

        Assert.assertTrue(errors.size() == 1);
    }

    @Test
    public void cplMergeabilityPositiveTest() throws IOException {

        File cpl1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        File cpl2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_supplemental.xml");

        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(cpl1);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord1 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord1));

        resourceByteRangeProvider = new FileByteRangeProvider(cpl2);
        bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord cplPayloadRecord2 = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        errors.addAll(IMPValidator.validateCPL(cplPayloadRecord2));


        if(errors.size() == 0) {
            errors.addAll(IMPValidator.isCPLMergeable(cplPayloadRecord1, new ArrayList<PayloadRecord>() {{
                add(cplPayloadRecord2);
            }}));
        }

        Assert.assertTrue(errors.size() == 0);
    }
}
