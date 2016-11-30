package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.*;

import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.Colorimetry.*;

/**
 * A class that models Composition with Application 2 constraints from 2067-20 specification
 */
public class Application2Composition extends AbstractApplicationComposition {
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
    }});

    public Application2Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {

        super(imfCompositionPlaylistType, ignoreSet);

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
            Byte contentKind = essenceContainerFormatUL.getULAsBytes()[14];
            if ((frameLayoutType.equals(FrameLayoutType.FullFrame) && contentKind != 6) ||
                    (frameLayoutType.equals(FrameLayoutType.SeparateFields) && contentKind != 3)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid ContentKind(%d) indicated by the ContainerFormat as per %s",
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
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_2_COMPOSITION_TYPE;
    }

}
