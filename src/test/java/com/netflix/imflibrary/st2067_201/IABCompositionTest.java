package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.Composition;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;

@Test(groups = "unit")
public class IABCompositionTest {

    @Test
    public void compositionPositiveTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_iabsequence.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void compositionPositiveTestNonZeroChannelCount() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_non_zero_essence_channelcount.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void compositionNegativeTestMissingAudio() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_missing_audio.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(),1);
    }

    @Test
    public void compositionNegativeTestEditRateMismatchMainVideo() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_iabsequence_wrong_editrate_main.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void compositionNegativeTestWrongTrackFile() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_iabsequence_wrong_trackfile.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        // Changing expected error count as the channel count is now being ignored as of
        // SMPTE ST 2067-201:2021, 5.9 IAB Essence Descriptor Constraints
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void compositionNegativeTestHomogeneous() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_homogeneous.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(),2);
    }

    @Test
    public void compositionNegativeTestNoResource() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_no_resource.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void compositionNegativeTestWrongBitDepth() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_bitdepth.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongEssenceContainer() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_essence_container_ul.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestCodecPresent() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_codec_present.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongSoundCompression() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_soundcompression.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestMissingAudioSamplingRate() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_missing_audiosamplingrate.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongElectroSpatialFormulation() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_electro_spatial_formulation.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestNoSubDescriptor() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_missing_subdescriptor.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongSubDescriptor() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_subdescriptor.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void compositionNegativeTestWrongSubDescriptorValues() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_subdescriptor_values.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void correctDurationTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_iabsequence.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Composition.VirtualTrack iabTrack = imfCompositionPlaylist.getVirtualTracks().stream().filter(vt -> vt.getSequenceType() == "IABSequence").findFirst().orElse(null);
        Assert.assertNotNull(iabTrack);
        Assert.assertNotEquals(iabTrack.getDuration(), 0L);
        Assert.assertNotEquals(iabTrack.getDurationInTrackEditRateUnits(), 0L);
    }
}
