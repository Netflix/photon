package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2013;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Test(groups = "unit")
public class CompositionTest
{
    @Test
    public void testCompositionPlaylist() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("test_mapped_file_set/CPL_682feecb-7516-4d93-b533-f40d4ce60539.xml");
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(inputFile, new IMFErrorLoggerImpl());
        Assert.assertTrue(ApplicationComposition.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(applicationComposition.toString().length() > 0);
        Assert.assertEquals(applicationComposition.getEditRate().getNumerator().longValue(), 24);
        Assert.assertEquals(applicationComposition.getEditRate().getDenominator().longValue(), 1);
        Assert.assertEquals(applicationComposition.getUUID(), UUID.fromString("682feecb-7516-4d93-b533-f40d4ce60539"));

        UUID uuid = UUID.fromString("586286d2-c45f-4b2f-ad76-58eecd0202b4");
        Assert.assertEquals(applicationComposition.getVirtualTracks().size(), 2);
        Composition.VirtualTrack virtualTrack = applicationComposition.getVideoVirtualTrack();
        Assert.assertEquals(virtualTrack.getSequenceTypeEnum(), Composition.SequenceTypeEnum.MainImageSequence);

        Assert.assertTrue(applicationComposition.getAnnotation().length() > 0);
        Assert.assertTrue(applicationComposition.getIssuer().length() > 0);
        Assert.assertTrue(applicationComposition.getCreator().length() > 0);
        Assert.assertTrue(applicationComposition.getContentOriginator().length() > 0);
        Assert.assertTrue(applicationComposition.getContentTitle().length() > 0);
    }

    @Test
    public void compositionPositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/MERIDIAN_Netflix_Photon_161006/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertTrue(imfErrorLogger.getErrors().size() == 0);
    }

    @Test
    public void compositionNegativeTestInconsistentURI() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_InconsistentNamespaceURI.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertTrue(imfErrorLogger.getErrors().size() == 11);
    }

    @Test
    public void virtualTracksEquivalenceTest(){
        String trackFileId1 = IMFUUIDGenerator.getInstance().getUrnUUID();
        String trackFileId2 = IMFUUIDGenerator.getInstance().getUrnUUID();
        String sourceEncoding = IMFUUIDGenerator.getInstance().generateUUID().toString();
        byte[] hash = new byte[16];
        String hashAlgorithm = CompositionPlaylistBuilder_2013.defaultHashAlgorithm;
        List<Long> editRate = new ArrayList<>();
        editRate.add(24000L);
        editRate.add(1001L);
        BigInteger intrinsicDuration = new BigInteger("100");

        IMFTrackFileResourceType imfTrackFileResourceType1 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("50"),
                        new BigInteger("2"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType2 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("50"),
                        new BigInteger("50"),
                        new BigInteger("2"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType3 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId2,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("50"),
                        new BigInteger("2"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType4 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("100"),
                        new BigInteger("2"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType5 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("50"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType6 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("100"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType7 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("50"),
                        new BigInteger("50"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType8 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId2,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("50"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType9 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId2,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("50"),
                        new BigInteger("50"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);
        IMFTrackFileResourceType imfTrackFileResourceType10 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId2,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("100"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);

        IMFTrackFileResourceType imfTrackFileResourceType11 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("0"),
                        new BigInteger("25"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);

        IMFTrackFileResourceType imfTrackFileResourceType12 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("25"),
                        new BigInteger("25"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);

        IMFTrackFileResourceType imfTrackFileResourceType13 =
                new IMFTrackFileResourceType(IMFUUIDGenerator.getInstance().generateUUID().toString(),
                        trackFileId1,
                        editRate,
                        intrinsicDuration,
                        new BigInteger("50"),
                        new BigInteger("50"),
                        new BigInteger("1"),
                        sourceEncoding,
                        hash,
                        hashAlgorithm);

        List<IMFTrackFileResourceType> resourceList1 = new ArrayList<>();
        resourceList1.add(imfTrackFileResourceType1);
        resourceList1.add(imfTrackFileResourceType2);
        resourceList1.add(imfTrackFileResourceType3);

        List<IMFTrackFileResourceType> resourceList2 = new ArrayList<>();
        resourceList2.add(imfTrackFileResourceType4);
        resourceList2.add(imfTrackFileResourceType3);

        List<IMFTrackFileResourceType> resourceList3 = new ArrayList<>();
        resourceList3.add(imfTrackFileResourceType5);
        resourceList3.add(imfTrackFileResourceType6);
        resourceList3.add(imfTrackFileResourceType7);
        resourceList3.add(imfTrackFileResourceType3);

        List<IMFTrackFileResourceType> resourceList4 = new ArrayList<>();
        resourceList4.add(imfTrackFileResourceType8);
        resourceList4.add(imfTrackFileResourceType9);
        resourceList4.add(imfTrackFileResourceType3);

        List<IMFTrackFileResourceType> resourceList5 = new ArrayList<>();
        resourceList5.add(imfTrackFileResourceType10);
        resourceList5.add(imfTrackFileResourceType3);

        List<IMFTrackFileResourceType> resourceList6 = new ArrayList<>();
        resourceList6.add(imfTrackFileResourceType3);
        resourceList6.add(imfTrackFileResourceType1);
        resourceList6.add(imfTrackFileResourceType2);

        List<IMFTrackFileResourceType> resourceList7 = new ArrayList<>();
        resourceList7.add(imfTrackFileResourceType11);
        resourceList7.add(imfTrackFileResourceType12);
        resourceList7.add(imfTrackFileResourceType13);

        List<IMFTrackFileResourceType> resourceList8 = new ArrayList<>();
        resourceList8.add(imfTrackFileResourceType6);

        List<IMFTrackFileResourceType> resourceList9 = new ArrayList<>();
        resourceList9.add(imfTrackFileResourceType11);
        resourceList9.add(imfTrackFileResourceType12);
        resourceList9.add(imfTrackFileResourceType13);
        resourceList9.add(imfTrackFileResourceType3);


        List<IMFTrackFileResourceType> resourceList10 = new ArrayList<>();
        resourceList10.add(imfTrackFileResourceType6);
        resourceList10.add(imfTrackFileResourceType3);

        Composition.EditRate compositionEditRate = new Composition.EditRate(editRate);
        Composition.VirtualTrack virtualTrack1 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList1, compositionEditRate);
        Composition.VirtualTrack virtualTrack2 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList2, compositionEditRate);
        Composition.VirtualTrack virtualTrack3 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList3, compositionEditRate);
        Composition.VirtualTrack virtualTrack4 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList4, compositionEditRate);
        Composition.VirtualTrack virtualTrack5 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList5, compositionEditRate);
        Composition.VirtualTrack virtualTrack6 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList6, compositionEditRate);
        Composition.VirtualTrack virtualTrack7 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList7, compositionEditRate);
        Composition.VirtualTrack virtualTrack8 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList8, compositionEditRate);
        Composition.VirtualTrack virtualTrack9 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList9, compositionEditRate);
        Composition.VirtualTrack virtualTrack10 = new IMFEssenceComponentVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(), Composition.SequenceTypeEnum.MainImageSequence, resourceList10, compositionEditRate);

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2) == false);
        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack3) == true);
        Assert.assertTrue(virtualTrack4.equivalent(virtualTrack5) == true);
        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack6) == false);
        Assert.assertTrue(virtualTrack7.equivalent(virtualTrack8) == true);
        Assert.assertTrue(virtualTrack9.equivalent(virtualTrack10) == true);
    }
}
