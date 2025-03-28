package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.validation.ConstraintsValidatorUtils;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Test(groups = "unit")
public class IMFEssenceComponentVirtualTrackTest
{
    @Test
    public void testEssenceComponentVirtualTrack_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new FileByteRangeProvider(inputFile));
        //builder.essenceDescriptorKeyIgnoreSet(new HashSet<String>() {{ add("MCALinkID"); add("MCALabelDictionaryID"); add("RFC5646SpokenLanguage");
        //    add("AudioChannelLabelSubDescriptor"); add("SoundfieldGroupLabelSubDescriptor");}});

        Assert.assertTrue(imfCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFEssenceComponentVirtualTrack virtualTrack = imfCompositionPlaylist.getVideoVirtualTrack();

        Assert.assertEquals(virtualTrack.getTrackFileResourceList().size(), 7);
    }



    @Test
    public void testEssenceComponentVirtualTrackEquivalent_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist1 = new IMFCompositionPlaylist(inputFile);
        IMFCompositionPlaylist imfCompositionPlaylist2 = new IMFCompositionPlaylist(inputFile);

        IMFEssenceComponentVirtualTrack virtualTrack1 = imfCompositionPlaylist1.getVideoVirtualTrack();
        IMFEssenceComponentVirtualTrack virtualTrack2 = imfCompositionPlaylist2.getVideoVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }

    @Test
    public void testEssenceComponentVirtualTrack_2016() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_2016_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFEssenceComponentVirtualTrack virtualTrack = imfCompositionPlaylist.getVideoVirtualTrack();

        Assert.assertEquals(virtualTrack.getTrackFileResourceList().size(), 7);

        for( Composition.VirtualTrack track : imfCompositionPlaylist.getVirtualTracks()) {
            if(track instanceof IMFEssenceComponentVirtualTrack)
            {
                IMFEssenceComponentVirtualTrack essenseTrack = (IMFEssenceComponentVirtualTrack) track;
                for (IMFTrackFileResourceType resource : essenseTrack.getTrackFileResourceList()) {
                    Assert.assertEquals(resource.getHashAlgorithm(), "http://www.w3.org/2000/09/xmldsig#sha1");
                }
            }
        }
    }

    @Test
    public void testEssenceComponentVirtualTrackEquivalent_2016() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_2016_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml");
        IMFCompositionPlaylist imfCompositionPlaylist1 = new IMFCompositionPlaylist(inputFile);
        IMFCompositionPlaylist imfCompositionPlaylist2 = new IMFCompositionPlaylist(inputFile);


        IMFEssenceComponentVirtualTrack virtualTrack1 = imfCompositionPlaylist1.getVideoVirtualTrack();
        IMFEssenceComponentVirtualTrack virtualTrack2 = imfCompositionPlaylist2.getVideoVirtualTrack();

        Assert.assertTrue(virtualTrack1.equivalent(virtualTrack2));
    }


    @Test
    public void testZeroResourceDuration() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_zero_resource_duration_track_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4.xml");
        IMFErrorLoggerImpl errorLogger = new IMFErrorLoggerImpl();
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFEssenceComponentVirtualTrack virtualTrack = imfCompositionPlaylist.getVideoVirtualTrack();

        for(IMFBaseResourceType resource: virtualTrack.getTrackFileResourceList())
        {
            Assert.assertTrue(resource.getSourceDuration().longValue() > 0);
        }
    }

    @Test
    public void testEssenceComponentVirtualTrackAudioHomogeneityFail_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_audio_homogeneity_fail.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new FileByteRangeProvider(inputFile));

        Assert.assertTrue(imfCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));

        IMFEssenceComponentVirtualTrack audioVirtualTrack = imfCompositionPlaylist.getAudioVirtualTracks().get(0);
        Set<String> homogenietySet = new HashSet<String>() {{ add("SubDescriptors"); add("MCAChannelID"); add("MCALabelDictionaryID"); add("RFC5646SpokenLanguage");
                add("AudioChannelLabelSubDescriptor"); add("SoundfieldGroupLabelSubDescriptor");
                add("MCAAudioContentKind");}};

        IMFErrorLogger errorLogger = new IMFErrorLoggerImpl();
        errorLogger.addAllErrors(ConstraintsValidatorUtils.checkVirtualTrackHomogeneity(audioVirtualTrack, imfCompositionPlaylist.getEssenceDescriptorListMap(), homogenietySet));

        Assert.assertEquals(errorLogger.getErrors().size(), 1);

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFEssenceComponentVirtualTrack virtualTrack = imfCompositionPlaylist.getVideoVirtualTrack();

        Assert.assertEquals(virtualTrack.getTrackFileResourceList().size(), 7);
    }

    @Test
    public void testEssenceComponentVirtualTrackCodingEquationHomogeneityFail_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_coding_equation_homogeneity_fail.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new FileByteRangeProvider(inputFile));

        //builder.essenceDescriptorKeyIgnoreSet(new HashSet<String>() {{ add("MCAChannelID"); add("MCALabelDictionaryID"); add("RFC5646SpokenLanguage");
        //    add("AudioChannelLabelSubDescriptor"); add("SoundfieldGroupLabelSubDescriptor");
        //    add("MCAAudioContentKind");}});

        IMFErrorLogger errorLogger = new IMFErrorLoggerImpl();
        errorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 24000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4"));
        Assert.assertEquals(errorLogger.getErrors().size(), 8);

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 4);

        IMFEssenceComponentVirtualTrack virtualTrack = imfCompositionPlaylist.getVideoVirtualTrack();

        Assert.assertEquals(virtualTrack.getTrackFileResourceList().size(), 7);
    }

    @Ignore
    @Test
    public void testEssenceComponentVirtualTrackRGBACodingEquationHomogeneityIgnore_2013() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85_rgba_coding_equation.xml");

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new FileByteRangeProvider(inputFile));
        //builder.essenceDescriptorKeyIgnoreSet(new HashSet<String>() {{ add("MCAChannelID"); add("MCALabelDictionaryID"); add("RFC5646SpokenLanguage");
        //    add("AudioChannelLabelSubDescriptor"); add("SoundfieldGroupLabelSubDescriptor");
        //    add("MCAAudioContentKind");}});

        IMFErrorLogger errorLogger = new IMFErrorLoggerImpl();
        errorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertTrue(imfCompositionPlaylist.isCompositionPlaylist(new FileByteRangeProvider(inputFile)));
        Assert.assertTrue(imfCompositionPlaylist.toString().length() > 0);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getNumerator().longValue(), 60000);
        Assert.assertEquals(imfCompositionPlaylist.getEditRate().getDenominator().longValue(), 1001);
        Assert.assertEquals(imfCompositionPlaylist.getUUID(), UUID.fromString("0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85"));
        Assert.assertEquals(errorLogger.getErrors().size(), 3);

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 3);

        IMFEssenceComponentVirtualTrack virtualTrack = imfCompositionPlaylist.getVideoVirtualTrack();

        Assert.assertEquals(virtualTrack.getTrackFileResourceList().size(), 2);
    }

}
