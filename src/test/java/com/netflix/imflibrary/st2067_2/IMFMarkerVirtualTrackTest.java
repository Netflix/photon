package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.FileLocator;
import java.io.File;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

@Test(groups = "unit")
public class IMFMarkerVirtualTrackTest
{
    @Test
    public void testMarkerVirtualTrack_2013() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), new IMFErrorLoggerImpl());
        Assert.assertTrue(ApplicationComposition.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(applicationComposition.toString().length() > 0);
        Assert.assertEquals(applicationComposition.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(applicationComposition.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(applicationComposition.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(applicationComposition.getVirtualTracks().size(), 4);

        IMFMarkerVirtualTrack virtualTrack = applicationComposition.getMarkerVirtualTrack();

        Assert.assertEquals(virtualTrack.getMarkerResourceList().size(), 1);
        Assert.assertEquals(virtualTrack.getMarkerResourceList().get(0).getMarkerList().size(), 19);
    }

    @Test
    public void testMarkerVirtualTrackEquivalent_2013() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        ApplicationComposition applicationComposition1 = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), new IMFErrorLoggerImpl());
        ApplicationComposition applicationComposition2 = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), new IMFErrorLoggerImpl());


        IMFMarkerVirtualTrack virtualTrack1 = applicationComposition1.getMarkerVirtualTrack();
        IMFMarkerVirtualTrack virtualTrack2 = applicationComposition2.getMarkerVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }

    @Test
    public void testMarkerVirtualTrack_2016() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_2016_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), new IMFErrorLoggerImpl());
        Assert.assertTrue(ApplicationComposition.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(applicationComposition.toString().length() > 0);
        Assert.assertEquals(applicationComposition.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(applicationComposition.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(applicationComposition.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(applicationComposition.getVirtualTracks().size(), 4);

        IMFMarkerVirtualTrack virtualTrack = applicationComposition.getMarkerVirtualTrack();

        Assert.assertEquals(virtualTrack.getMarkerResourceList().size(), 1);
        Assert.assertEquals(virtualTrack.getMarkerResourceList().get(0).getMarkerList().size(), 19);
    }

    @Test
    public void testMarkerVirtualTrackEquivalent_2016() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_2016_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        ApplicationComposition applicationComposition1 = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), new IMFErrorLoggerImpl());
        ApplicationComposition applicationComposition2 = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), new IMFErrorLoggerImpl());


        IMFMarkerVirtualTrack virtualTrack1 = applicationComposition1.getMarkerVirtualTrack();
        IMFMarkerVirtualTrack virtualTrack2 = applicationComposition2.getMarkerVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }

}
