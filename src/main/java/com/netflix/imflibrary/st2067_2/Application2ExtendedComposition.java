package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.utils.Fraction;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;

/**
 * A class that models Composition with Application 2Extended constraints from 2067-21 specification
 */
public class Application2ExtendedComposition extends AbstractApplicationComposition {
    public static final String SCHEMA_URI_APP2E_2014 = "http://www.smpte-ra.org/schemas/2067-21/2014";
    public static final String SCHEMA_URI_APP2E_2016 = "http://www.smpte-ra.org/schemas/2067-21/2016";
    public static final String SCHEMA_URI_APP2E_2020 = "http://www.smpte-ra.org/ns/2067-21/2020";
    public static final Integer MAX_YUV_IMAGE_FRAME_WIDTH = 3840;
    public static final Integer MAX_YUV_IMAGE_FRAME_HEIGHT = 2160;
    public static final Integer MAX_RGB_IMAGE_FRAME_WIDTH = 4096;
    public static final Integer MAX_RGB_IMAGE_FRAME_HEIGHT = 3112;
    public static final UL JPEG2000PICTURECODINGSCHEME = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.04010107.04010202.03010000");

    public static final Set<Fraction>rgbaSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24)); add(new Fraction(25)); add(new Fraction(30)); add(new Fraction(50)); add(new Fraction(60)); add(new Fraction(120));
        add(new Fraction(24000, 1001)); add(new Fraction(30000, 1001)); add(new Fraction(60000, 1001)); }});
    public static final Set<Fraction>yuvSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24)); add(new Fraction(25)); add(new Fraction(30)); add(new Fraction(50)); add(new Fraction(60));
        add(new Fraction(24000, 1001)); add(new Fraction(30000, 1001)); add(new Fraction(60000, 1001)); }});
    public static final Map<Colorimetry, Set<Integer>>colorToBitDepthMap = Collections.unmodifiableMap(new HashMap<Colorimetry, Set<Integer>>() {{
        put(Colorimetry.Unknown, new HashSet<Integer>(){{ }});
        put(Colorimetry.Color3, new HashSet<Integer>(){{ add(8); add(10); }});
        put(Colorimetry.Color4, new HashSet<Integer>(){{ add(8); add(10); }});
        put(Colorimetry.Color5, new HashSet<Integer>(){{ add(10); add(12); }});
        put(Colorimetry.Color6, new HashSet<Integer>(){{ add(10); add(12); add(16);}});
        put(Colorimetry.Color7, new HashSet<Integer>(){{ add(10); add(12); add(16); }});
    }});
    public static final Set<Integer>bitDepthsSupported = Collections.unmodifiableSet(new HashSet<Integer>() {{
        add(8); add(10); add(12); add(16); }});

    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
        add("SignalStandard");
        add("ActiveFormatDescriptor");
        add("VideoLineMap");
        add("AlphaTransparency");
        add("PixelLayout");
        add("ActiveHeight");
        add("ActiveWidth");
        add("ActiveXOffset");
        add("ActiveYOffset");
    }});

    public Application2ExtendedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public Application2ExtendedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);

        try
        {
            CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = getCompositionImageEssenceDescriptorModel();

            if (imageEssenceDescriptorModel != null)
            {
                IMFErrorLogger app2ErrorLogger = new IMFErrorLoggerImpl();
                IMFErrorLogger app2ExtendedErrorLogger = new IMFErrorLoggerImpl();

                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                Application2Composition.validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_2E_COMPOSITION_TYPE,
                        imfErrorLogger);
                Application2Composition.validateImageCharacteristics(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_2_COMPOSITION_TYPE,
                        app2ErrorLogger);

                if(app2ErrorLogger.getNumberOfErrors() != 0) {
                    Application2ExtendedComposition.validateImageCharacteristics(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_2E_COMPOSITION_TYPE,
                            app2ExtendedErrorLogger);
                    if(app2ExtendedErrorLogger.getNumberOfErrors() != 0) {
                        imfErrorLogger.addAllErrors(app2ExtendedErrorLogger.getErrors());
                    }
                }
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in APPLICATION_2E_COMPOSITION_TYPE: %s ", e.getMessage()));
        }
    }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                    ApplicationCompositionType applicationCompositionType,
                                                    IMFErrorLogger imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( !colorModel.equals(ColorModel.RGB) && !colorModel.equals(ColorModel.YUV)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
            return;
        }

        //storedWidth
        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        if (((colorModel.equals(ColorModel.RGB) && storedWidth > MAX_RGB_IMAGE_FRAME_WIDTH) ||
                (colorModel.equals(ColorModel.YUV) && storedWidth > MAX_YUV_IMAGE_FRAME_WIDTH))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredWidth(%d) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, colorModel.name(), applicationCompositionType.toString()));
        }

        //storedHeight
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if (((colorModel.equals(ColorModel.RGB) && storedHeight > MAX_RGB_IMAGE_FRAME_HEIGHT) ||
                        (colorModel.equals(ColorModel.YUV) && storedHeight > MAX_YUV_IMAGE_FRAME_HEIGHT))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedHeight(%d) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), storedHeight, colorModel.name(), applicationCompositionType.toString()));
        }

        //PixelBitDepth
        Integer pixelBitDepth = imageEssenceDescriptorModel.getPixelBitDepth();
        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if( !bitDepthsSupported.contains(pixelBitDepth) || !colorToBitDepthMap.get(color).contains(pixelBitDepth)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid PixelBitDepth(%d) for Color(%s) as per %s",
                            imageEssenceDescriptorID.toString(), pixelBitDepth, color.name(), applicationCompositionType.toString()));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), applicationCompositionType.toString()));
        }

        //SampleRate
        Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
        Set<Fraction> frameRateSupported = colorModel.equals(ColorModel.RGB) ? rgbaSampleRateSupported : yuvSampleRateSupported;
        if (!frameRateSupported.contains(sampleRate)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid SampleRate(%s) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), sampleRate.toString(), colorModel.name(), applicationCompositionType.toString()));
        }

        //Sampling
        Sampling sampling = imageEssenceDescriptorModel.getSampling();
        if((colorModel.equals(ColorModel.RGB) && !sampling.equals(Sampling.Sampling444)) ||
                (colorModel.equals(ColorModel.YUV) && !sampling.equals(Sampling.Sampling422))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Sampling(%s) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), sampling.name(), colorModel.name(), applicationCompositionType.toString()));
        }

        //Quantization
        Quantization quantization = imageEssenceDescriptorModel.getQuantization();
        if((colorModel.equals(ColorModel.RGB) && !(quantization.equals(Quantization.QE2) || quantization.equals(Quantization.QE1))) ||
                (colorModel.equals(ColorModel.YUV) && !quantization.equals(Quantization.QE1))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Quantization(%s) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), quantization.name(), colorModel.name(), applicationCompositionType.toString()));
        }

        //Coding
        UL pictureEssenceCoding = imageEssenceDescriptorModel.getPictureEssenceCodingUL();

        if(pictureEssenceCoding.equalsWithMask(JPEG2000PICTURECODINGSCHEME, 0b1111111011111100)) {
            boolean validProfile = false;

            if (pictureEssenceCoding.getByte(14) == 0x01) {
                switch (pictureEssenceCoding.getByte(15)) {
                    case 0x11: /* JPEG2000BroadcastContributionSingleTileProfileLevel1 */
                    case 0x12: /* JPEG2000BroadcastContributionSingleTileProfileLevel2 */
                    case 0x13: /* JPEG2000BroadcastContributionSingleTileProfileLevel3 */
                    case 0x14: /* JPEG2000BroadcastContributionSingleTileProfileLevel4 */
                    case 0x15: /* JPEG2000BroadcastContributionSingleTileProfileLevel5 */
                    case 0x16: /* JPEG2000BroadcastContributionMultiTileReversibleProfileLevel6 */
                    case 0x17: /* JPEG2000BroadcastContributionMultiTileReversibleProfileLevel7 */
                        validProfile = true;
                        break;
                    default:
                }
            } else if (pictureEssenceCoding.getByte(14) == 0x02) {
                switch (pictureEssenceCoding.getByte(15)) {
                    case 0x03: /* J2K_2KIMF_SingleTileLossyProfile_M1S1 */
                    case 0x05: /* J2K_2KIMF_SingleTileLossyProfile_M2S1 */
                    case 0x07: /* J2K_2KIMF_SingleTileLossyProfile_M3S1 */
                    case 0x09: /* J2K_2KIMF_SingleTileLossyProfile_M4S1 */
                    case 0x0a: /* J2K_2KIMF_SingleTileLossyProfile_M4S2 */
                    case 0x0c: /* J2K_2KIMF_SingleTileLossyProfile_M5S1 */
                    case 0x0d: /* J2K_2KIMF_SingleTileLossyProfile_M5S2 */
                    case 0x0e: /* J2K_2KIMF_SingleTileLossyProfile_M5S3 */
                    case 0x10: /* J2K_2KIMF_SingleTileLossyProfile_M6S1 */
                    case 0x11: /* J2K_2KIMF_SingleTileLossyProfile_M6S2 */
                    case 0x12: /* J2K_2KIMF_SingleTileLossyProfile_M6S3 */
                    case 0x13: /* J2K_2KIMF_SingleTileLossyProfile_M6S4 */
                        validProfile = true;
                        break;
                    default:
                }
            } else if (pictureEssenceCoding.getByte(14) == 0x03) {
                switch (pictureEssenceCoding.getByte(15)) {
                    case 0x03: /* J2K_4KIMF_SingleTileLossyProfile_M1S1 */
                    case 0x05: /* J2K_4KIMF_SingleTileLossyProfile_M2S1 */
                    case 0x07: /* J2K_4KIMF_SingleTileLossyProfile_M3S1 */
                    case 0x09: /* J2K_4KIMF_SingleTileLossyProfile_M4S1 */
                    case 0x0a: /* J2K_4KIMF_SingleTileLossyProfile_M4S2 */
                    case 0x0c: /* J2K_4KIMF_SingleTileLossyProfile_M5S1 */
                    case 0x0d: /* J2K_4KIMF_SingleTileLossyProfile_M5S2 */
                    case 0x0e: /* J2K_4KIMF_SingleTileLossyProfile_M5S3 */
                    case 0x10: /* J2K_4KIMF_SingleTileLossyProfile_M6S1 */
                    case 0x11: /* J2K_4KIMF_SingleTileLossyProfile_M6S2 */
                    case 0x12: /* J2K_4KIMF_SingleTileLossyProfile_M6S3 */
                    case 0x13: /* J2K_4KIMF_SingleTileLossyProfile_M6S4 */
                    case 0x15: /* J2K_4KIMF_SingleTileLossyProfile_M7S1 */
                    case 0x16: /* J2K_4KIMF_SingleTileLossyProfile_M7S2 */
                    case 0x17: /* J2K_4KIMF_SingleTileLossyProfile_M7S3 */
                    case 0x18: /* J2K_4KIMF_SingleTileLossyProfile_M7S4 */
                    case 0x19: /* J2K_4KIMF_SingleTileLossyProfile_M7S5 */
                    case 0x1b: /* J2K_4KIMF_SingleTileLossyProfile_M8S1 */
                    case 0x1c: /* J2K_4KIMF_SingleTileLossyProfile_M8S2 */
                    case 0x1d: /* J2K_4KIMF_SingleTileLossyProfile_M8S3 */
                    case 0x1e: /* J2K_4KIMF_SingleTileLossyProfile_M8S4 */
                    case 0x1f: /* J2K_4KIMF_SingleTileLossyProfile_M8S5 */
                    case 0x20: /* J2K_4KIMF_SingleTileLossyProfile_M8S6 */
                        validProfile = true;
                        break;
                    default:
                }
            } else if (pictureEssenceCoding.getByte(14) == 0x05) {
                switch (pictureEssenceCoding.getByte(15)) {
                    case 0x02: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M1S0 */
                    case 0x04: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M2S0 */
                    case 0x06: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M3S0 */
                    case 0x08: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M4S0 */
                    case 0x0b: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M5S0 */
                    case 0x0f: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M6S0 */
                        validProfile = true;
                        break;
                    default:
                }
            } else if (pictureEssenceCoding.getByte(14) == 0x06) {
                switch (pictureEssenceCoding.getByte(15)) {
                    case 0x02: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M1S0 */
                    case 0x04: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M2S0 */
                    case 0x06: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M3S0 */
                    case 0x08: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M4S0 */
                    case 0x0b: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M5S0 */
                    case 0x0f: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M6S0 */
                    case 0x14: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M7S0 */
                    case 0x1a: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M8S0 */
                        validProfile = true;
                        break;
                    default:
                }
            }

            if (! validProfile) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Invalid JPEG 2000 profile: %s", pictureEssenceCoding.toString()
                ));
            }

        } else {

            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                String.format("Image codec must be JPEG 2000. Found %s instead.", pictureEssenceCoding.toString()
            ));

        }

    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_2E_COMPOSITION_TYPE;
    }

}
