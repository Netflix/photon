package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.util.UUID;

@Test(groups = "unit")
public class IMFEssenceComponentVirtualTrackTest
{
    @Test
    public void testEssenceComponentVirtualTrack() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4.xml");
        Composition composition = new Composition(inputFile, new IMFErrorLoggerImpl());
        Assert.assertTrue(Composition.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(composition.toString().length() > 0);
        Assert.assertEquals(composition.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(composition.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(composition.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(composition.getVirtualTrackMap().size(), 4);

        IMFEssenceComponentVirtualTrack virtualTrack = composition.getVideoVirtualTrack();

        Assert.assertEquals(virtualTrack.getTrackFileResourceList().size(), 7);
    }

    @Test
    public void testEssenceComponentVirtualTrackEquivalent() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4.xml");
        Composition composition1 = new Composition(inputFile, new IMFErrorLoggerImpl());
        Composition composition2 = new Composition(inputFile, new IMFErrorLoggerImpl());


        IMFEssenceComponentVirtualTrack virtualTrack1 = composition1.getVideoVirtualTrack();
        IMFEssenceComponentVirtualTrack virtualTrack2 = composition2.getVideoVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }
}
