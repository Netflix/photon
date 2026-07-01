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

import static com.netflix.imflibrary.IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL;
import static com.netflix.imflibrary.IMFErrorLogger.IMFErrors.ErrorLevels.WARNING;

@Test(groups = "unit")
public class IABCompositionTest {

    // These tests assert the specific errors each fixture is expected to report (via TestHelper.assertHasError)
    // rather than an exact error count, so they validate the constraint under test and do not break when unrelated
    // validations add or remove reported issues.

    @Test
    public void compositionPositiveTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_iabsequence.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        // Valid composition: only the recommended-metadata warnings (absent MCA Content / MCA Use Class) may be present.
        TestHelper.assertNoErrorAtOrAbove(imfErrorLogger.getErrors(), NON_FATAL);
    }

    @Test
    public void compositionPositiveTestNonZeroChannelCount() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_non_zero_essence_channelcount.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertNoErrorAtOrAbove(imfErrorLogger.getErrors(), NON_FATAL);
    }

    @Test
    public void compositionNegativeTestMissingAudio() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_missing_audio.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "does not contain a single main audio sequence in its first segment");
    }

    @Test
    public void compositionNegativeTestEditRateMismatchMainVideo() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_iabsequence_wrong_editrate_main.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "is not a multiple of the EditRate of the Main Image Virtual Track");
    }

    @Test
    public void compositionNegativeTestWrongTrackFile() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_iabsequence_wrong_trackfile.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "does not have an IABEssenceDescriptor but WAVEPCMDescriptor");
    }

    @Test
    public void compositionNegativeTestHomogeneous() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_homogeneous.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "is not homogeneous based on a comparison of the EssenceDescriptors");
    }

    @Test
    public void compositionNegativeTestNoResource() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_no_resource.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "does not have any associated resources this is invalid");
    }

    @Test
    public void compositionNegativeTestWrongBitDepth() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_bitdepth.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has invalid QuantizationBits field");
    }

    @Test
    public void compositionNegativeTestWrongEssenceContainer() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_essence_container_ul.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "does not use the correct Essence Container UL");
    }

    @Test
    public void compositionNegativeTestCodecPresent() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_codec_present.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "shall not have a Codec item");
    }

    @Test
    public void compositionNegativeTestWrongSoundCompression() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_soundcompression.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "does not use the correct Sound Compression UL");
    }

    @Test
    public void compositionNegativeTestMissingAudioSamplingRate() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_missing_audiosamplingrate.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "does not have an AudioSampleRate item");
    }

    @Test
    public void compositionNegativeTestWrongElectroSpatialFormulation() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_electro_spatial_formulation.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has an invalid value of ElectrospatialFormulation");
    }

    @Test
    public void compositionNegativeTestNoSubDescriptor() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_missing_subdescriptor.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has no SubDescriptor");
    }

    @Test
    public void compositionNegativeTestWrongSubDescriptor() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_subdescriptor.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has forbidden SoundfieldGroupLabelSubDescriptor");
        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has forbidden AudioChannelLabelSubDescriptor");
    }

    @Test
    public void compositionNegativeTestWrongSubDescriptorValues() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_wrong_subdescriptor_values.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has invalid MCA Label Dictionary ID");
        TestHelper.assertHasError(imfErrorLogger.getErrors(), "misses MCA Tag Symbol");
        TestHelper.assertHasError(imfErrorLogger.getErrors(), "misses MCA Tag Name");
        TestHelper.assertHasError(imfErrorLogger.getErrors(), "has forbidden MCAChannelID");
    }

    @Test
    public void compositionPositiveTestMCAContentUseClass() throws IOException {
        // Valid MCA Content / MCA Use Class value and a permitted combination (PRM/FCMP) per SMPTE ST 377-41:2023.
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_valid_mca_content_useclass.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        // Both items present and consistent, so the composition validates with no errors at all.
        TestHelper.assertNoErrorAtOrAbove(imfErrorLogger.getErrors(), WARNING);
    }

    @Test
    public void compositionNegativeTestMCAContentValue() throws IOException {
        // MCAContent value not in SMPTE ST 377-41:2023, Table 2.
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_mca_content_value.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "has MCAContent value 'BOGUS' which is not a valid SMPTE ST 377-41:2023 (Table 2) symbol");
    }

    @Test
    public void compositionNegativeTestMCAUseClassValue() throws IOException {
        // MCAUseClass value not in SMPTE ST 377-41:2023, Table 3.
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_mca_use_class_value.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "has MCAUseClass value 'XYZ' which is not a valid SMPTE ST 377-41:2023 (Table 3) symbol");
    }

    @Test
    public void compositionNegativeTestMCAContentWithoutUseClass() throws IOException {
        // MCAContent present but MCAUseClass absent: SMPTE ST 377-4:2021 requires the two to appear as a pair.
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_mca_content_without_useclass.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "has MCAContent but is missing MCAUseClass, which SMPTE ST 377-4:2021 requires whenever MCAContent is present");
    }

    @Test
    public void compositionNegativeTestMCAUseClassWithoutContent() throws IOException {
        // MCAUseClass present but MCAContent absent: SMPTE ST 377-4:2021 requires the two to appear as a pair.
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_mca_useclass_without_content.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "has MCAUseClass but is missing MCAContent, which SMPTE ST 377-4:2021 requires whenever MCAUseClass is present");
    }

    @Test
    public void compositionNegativeTestMCAContentUseClassCombination() throws IOException {
        // Individually valid values but a combination disallowed by SMPTE ST 377-41:2023, Table 4 (PRM/ICMP).
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/IAB/CPL/IAB_CPL_invalid_mca_combination.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        TestHelper.assertHasError(imfErrorLogger.getErrors(),
                "has an MCAContent/MCAUseClass combination 'PRM'/'ICMP' that is not permitted by SMPTE ST 377-41:2023 (Table 4)");
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
