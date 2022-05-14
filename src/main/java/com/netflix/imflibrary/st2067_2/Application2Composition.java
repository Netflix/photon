package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0422.JP2KContentKind;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.utils.Fraction;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.Colorimetry.CodingEquation;
import static com.netflix.imflibrary.Colorimetry.ColorModel;
import static com.netflix.imflibrary.Colorimetry.ColorPrimaries;
import static com.netflix.imflibrary.Colorimetry.Quantization;
import static com.netflix.imflibrary.Colorimetry.Sampling;
import static com.netflix.imflibrary.Colorimetry.TransferCharacteristic;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;

/**
 * A class that models Composition with Application 2 constraints from 2067-20 specification
 */
public class Application2Composition extends AbstractApplicationComposition {
    public static final String SCHEMA_URI_APP2_2013 = "http://www.smpte-ra.org/schemas/2067-20/2013";
    public static final String SCHEMA_URI_APP2_2016 = "http://www.smpte-ra.org/schemas/2067-20/2016";
    public static final UL JPEG2000PICTURECODINGSCHEME = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.04010107.04010202.03010000");
    public static final Integer MAX_IMAGE_FRAME_WIDTH = 1920;
    public static final Integer MAX_IMAGE_FRAME_HEIGHT = 1080;
    public static final Set<Fraction>progressiveSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24)); add(new Fraction(25)); add(new Fraction(30)); add(new Fraction(50)); add(new Fraction(60));
        add(new Fraction(24000, 1001)); add(new Fraction(30000, 1001)); add(new Fraction(60000, 1001)); }});
    public static final Set<Fraction>interlaceSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(50)); add(new Fraction(60)); add(new Fraction(60000, 1001)); }});
    public static final Set<Integer>bitDepthsSupported = Collections.unmodifiableSet(new HashSet<Integer>() {{
        add(8); add(10); }});
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

    public Application2Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public Application2Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);

        try
        {
            CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = getCompositionImageEssenceDescriptorModel();

            if (imageEssenceDescriptorModel != null)
            {
                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                Application2Composition.validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_2_COMPOSITION_TYPE,
                        imfErrorLogger);
                Application2Composition.validateImageCharacteristics(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_2_COMPOSITION_TYPE,
                        imfErrorLogger);
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in APPLICATION_2_COMPOSITION_TYPE: %s ", e.getMessage()));
        }
    }

    public static void validateGenericPictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                               ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                               IMFErrorLogger
            imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( colorModel.equals(ColorModel.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
            return;
        }

        Integer componentDepth = imageEssenceDescriptorModel.getComponentDepth();
        if (colorModel.equals(ColorModel.YUV) && componentDepth == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s is missing component depth required per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if ((storedWidth <= 0) || (storedHeight <= 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedWidth(%d) or storedHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, storedHeight, applicationCompositionType.toString()));
        }

        Integer sampleWidth = imageEssenceDescriptorModel.getSampleWidth();
        Integer sampleHeight = imageEssenceDescriptorModel.getSampleHeight();
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) ||
                (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sampleWidth(%d) or sampleHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), sampleWidth != null ? sampleWidth : 0, sampleHeight != null ? sampleHeight : 0,
                            applicationCompositionType.toString()));
        }

        if( imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid StoredOffset as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        ColorPrimaries colorPrimaries = imageEssenceDescriptorModel.getColorPrimaries();
        if(colorPrimaries.equals(ColorPrimaries.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        TransferCharacteristic transferCharacteristic = imageEssenceDescriptorModel.getTransferCharacteristic();
        if(transferCharacteristic.equals(TransferCharacteristic.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid TransferCharacteristic as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        CodingEquation codingEquation = imageEssenceDescriptorModel.getCodingEquation();
        if(codingEquation.equals(CodingEquation.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid CodingEquation as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(color.equals(Colorimetry.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s)-CodingEquation(%s) combination as per %s",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), transferCharacteristic.name(), codingEquation.name(), applicationCompositionType.toString()));
        }

        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        UL essenceContainerFormatUL = imageEssenceDescriptorModel.getEssenceContainerFormatUL();
        if(essenceContainerFormatUL != null) {
            JP2KContentKind contentKind = JP2KContentKind.valueOf(essenceContainerFormatUL.getULAsBytes()[14]);
            if ((frameLayoutType.equals(FrameLayoutType.FullFrame) && !contentKind.equals(JP2KContentKind.P1)) ||
                    (frameLayoutType.equals(FrameLayoutType.SeparateFields) && !contentKind.equals(JP2KContentKind.I1))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid JPEG-2000 ContentKind (%s) indicated by the ContainerFormat as per %s",
                                imageEssenceDescriptorID.toString(), contentKind, applicationCompositionType.toString()));
            }
        }
   }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                    ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
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
        if (storedWidth > MAX_IMAGE_FRAME_WIDTH) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredWidth(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, applicationCompositionType.toString()));
        }

        //storedHeight
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if (storedHeight > MAX_IMAGE_FRAME_HEIGHT) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedHeight, applicationCompositionType.toString()));
        }

        //PixelBitDepth
        Integer pixelBitDepth = imageEssenceDescriptorModel.getPixelBitDepth();
        if( !(pixelBitDepth.equals(8) || pixelBitDepth.equals(10))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid PixelBitDepth(%d) as per %s",
                            imageEssenceDescriptorID.toString(), pixelBitDepth, applicationCompositionType.toString()));
        }
        Integer componentDepth = imageEssenceDescriptorModel.getComponentDepth();
        if (colorModel.equals(ColorModel.YUV) && !pixelBitDepth.equals(componentDepth)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has a PixelBitDepth(%d) not matching Component Depth (%d) as per %s",
                            imageEssenceDescriptorID.toString(), pixelBitDepth, componentDepth, applicationCompositionType.toString()));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame) && !frameLayoutType.equals(FrameLayoutType.SeparateFields)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), applicationCompositionType.toString()));
        }
        else {
            //SampleRate
            Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
            Set<Fraction> frameRateSupported = frameLayoutType.equals(FrameLayoutType.FullFrame) ? progressiveSampleRateSupported : interlaceSampleRateSupported;
            if (!frameRateSupported.contains(sampleRate)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has Invalid SampleRate(%s) for frame structure %s as per %s",
                                imageEssenceDescriptorID.toString(), sampleRate.toString(), frameLayoutType.name(), applicationCompositionType.toString()));
            }
        }

        //Sampling
        Sampling sampling = imageEssenceDescriptorModel.getSampling();
        if(frameLayoutType.equals(FrameLayoutType.SeparateFields) && !sampling.equals(Sampling.Sampling422) ) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid combination of FrameLayOut(%s) for Sampling(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), sampling.name(), applicationCompositionType.toString()));
        }

        //Quantization
        Quantization quantization = imageEssenceDescriptorModel.getQuantization();
        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if((sampling.equals(Sampling.Sampling422) &&
                !(quantization.equals(Quantization.QE1) && colorModel.equals(ColorModel.YUV))) ||
                (quantization.equals(Quantization.QE2) &&
                        !(colorModel.equals(ColorModel.RGB) && color.equals(Colorimetry.Color3)))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid combination of quantization(%s)-Sampling(%s)-colorModel(%s)-color(%s) as per %s",
                            imageEssenceDescriptorID.toString(), quantization.name(), sampling.name(), colorModel.name(), color.name(), applicationCompositionType.toString()));
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
        return ApplicationCompositionType.APPLICATION_2_COMPOSITION_TYPE;
    }

}
