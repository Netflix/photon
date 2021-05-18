package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.utils.FileLocator;
import java.io.File;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

@Test(groups = "unit")
public class IABCompositionTest {

    @Test
    public void compositionPositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_iabsequence.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void compositionNegativeTestMissingAudio() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_missing_audio.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(),1);
    }

    @Test
    public void compositionNegativeTestEditRateMismatchMainVideo() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_iabsequence_wrong_editrate_main.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void compositionNegativeTestWrongTrackFile() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_iabsequence_wrong_trackfile.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 6);
    }

    @Test
    public void compositionNegativeTestHomogeneous() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_homogeneous.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(),2);
    }

    @Test
    public void compositionNegativeTestNoResource() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_no_resource.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongBitDepth() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_bitdepth.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongEssenceContainer() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_essence_container_ul.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestCodecPresent() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_codec_present.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongSoundCompression() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_soundcompression.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestMissingAudioSamplingRate() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_missing_audiosamplingrate.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongElectroSpatialFormulation() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_electro_spatial_formulation.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongChannelCount() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_channel_count.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestNoSubDescriptor() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_missing_subdescriptor.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void compositionNegativeTestWrongSubDescriptor() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_subdescriptor.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void compositionNegativeTestWrongSubDescriptorValues() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_subdescriptor_values.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void correctDurationTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_iabsequence.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationComposition composition = ApplicationCompositionFactory.getApplicationComposition(new FileLocator(inputFile), imfErrorLogger);
        Composition.VirtualTrack iabTrack = composition.getVirtualTracks().stream().filter(vt -> vt.getSequenceTypeEnum() == Composition.SequenceTypeEnum.IABSequence).findFirst().orElse(null);
        Assert.assertNotNull(iabTrack);
        Assert.assertNotEquals(iabTrack.getDuration(), 0L);
        Assert.assertNotEquals(iabTrack.getDurationInTrackEditRateUnits(), 0L);
    }
}
