package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.utils.FileByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.nio.file.Path;
import java.util.UUID;

@Test(groups = "unit")
public class IMFMarkerVirtualTrackTest
{
    @Test
    public void testMarkerVirtualTrack_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFMarkerVirtualTrack virtualTrack = imfCompositionPlaylist.getMarkerVirtualTrack();

        Assert.assertEquals(virtualTrack.getMarkerResourceList().size(), 1);
        Assert.assertEquals(virtualTrack.getMarkerResourceList().get(0).getMarkerList().size(), 19);
    }

    @Test
    public void testMarkerVirtualTrackEquivalent_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist1 = new IMFCompositionPlaylist(inputFile);
        IMFCompositionPlaylist imfCompositionPlaylist2 = new IMFCompositionPlaylist(inputFile);

        IMFMarkerVirtualTrack virtualTrack1 = imfCompositionPlaylist1.getMarkerVirtualTrack();
        IMFMarkerVirtualTrack virtualTrack2 = imfCompositionPlaylist2.getMarkerVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }

    @Test
    public void testMarkerVirtualTrack_2016() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_2016_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFMarkerVirtualTrack virtualTrack = imfCompositionPlaylist.getMarkerVirtualTrack();

        Assert.assertEquals(virtualTrack.getMarkerResourceList().size(), 1);
        Assert.assertEquals(virtualTrack.getMarkerResourceList().get(0).getMarkerList().size(), 19);
    }

    @Test
    public void testMarkerVirtualTrackEquivalent_2016() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_2016_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist1 = new IMFCompositionPlaylist(inputFile);
        IMFCompositionPlaylist imfCompositionPlaylist2 = new IMFCompositionPlaylist(inputFile);

        IMFMarkerVirtualTrack virtualTrack1 = imfCompositionPlaylist1.getMarkerVirtualTrack();
        IMFMarkerVirtualTrack virtualTrack2 = imfCompositionPlaylist2.getMarkerVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }

}
