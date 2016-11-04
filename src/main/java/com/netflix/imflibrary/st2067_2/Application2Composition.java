package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;

import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st2067_2.Colorimetry.*;

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
        add(new Fraction(24)); add(new Fraction(30)); add(new Fraction(30000, 1001)); }});
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

        DOMNodeObjectModel imageEssencedescriptorDOMNode = this.getEssenceDescriptor(
                this.getVideoVirtualTrack().getTrackResourceIds().iterator().next());

        if (imageEssencedescriptorDOMNode != null) {
            UUID imageEssenceDescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst()
                    .get();
            try {
                CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = new CompositionImageEssenceDescriptorModel( imageEssenceDescriptorID, imageEssencedescriptorDOMNode,
                        regXMLLibDictionary);
                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, imfErrorLogger);
                validateImageCharacteristics(imageEssenceDescriptorModel, imfErrorLogger);
            } catch (Exception e) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Exception %s in validating EssenceDescriptor with ID %s as per Application#2 specification", e.getMessage(), imageEssenceDescriptorID.toString()));
            }
        }
    }

    public static void validateGenericPictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel, IMFErrorLogger
            imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();
        ColorSpace colorSpace = imageEssenceDescriptorModel.getColorSpace();
        if( colorSpace.equals(ColorSpace.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Picture Essence Descriptor as per Application#2 Composition",
                            imageEssenceDescriptorID.toString()));
            return;
        }


        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        Integer sampleWidth = imageEssenceDescriptorModel.getSampleWidth();
        Integer sampleHeight = imageEssenceDescriptorModel.getSampleHeight();
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) ||
                (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sampleWidth(%d) or sampleHeight(%d) as per Application#2 Composition",
                            imageEssenceDescriptorID.toString(), sampleWidth != null ? sampleWidth : 0, sampleHeight != null ? sampleHeight : 0));
        }

        if( imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid StoredOffset as per Application#2 Composition",
                            imageEssenceDescriptorID.toString()));
        }

        ColorPrimaries colorPrimaries = imageEssenceDescriptorModel.getColorPrimaries();
        if(colorPrimaries.equals(ColorPrimaries.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries in Image Essence Descriptor",
                            imageEssenceDescriptorID.toString()));
        }

        TransferCharacteristic transferCharacteristic = imageEssenceDescriptorModel.getTransferCharacteristic();
        if(transferCharacteristic.equals(TransferCharacteristic.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid TransferCharacteristic in Image Essence Descriptor",
                            imageEssenceDescriptorID.toString()));
        }

        CodingEquation codingEquation = imageEssenceDescriptorModel.getCodingEquation();
        if(codingEquation.equals(CodingEquation.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid CodingEquation in Image Essence Descriptor",
                            imageEssenceDescriptorID.toString()));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(color.equals(Colorimetry.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s)-CodingEquation(%s) combination in Image Essence Descriptor",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), transferCharacteristic.name(), codingEquation.name()));
        }
   }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel, IMFErrorLogger
            imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorSpace colorSpace = imageEssenceDescriptorModel.getColorSpace();
        if( !colorSpace.equals(ColorSpace.RGB) && !colorSpace.equals(ColorSpace.YUV)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Picture Essence Descriptor as per Application#2 Composition",
                            imageEssenceDescriptorID.toString()));
            return;
        }

        //storedWidth
        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        if (storedWidth > MAX_IMAGE_FRAME_WIDTH) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredWidth(%d) as per Application#2 specification",
                            imageEssenceDescriptorID.toString(), storedWidth));
        }

        //storedHeight
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if (storedHeight > MAX_IMAGE_FRAME_HEIGHT) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedHeight(%d) as per Application#2 specification",
                            imageEssenceDescriptorID.toString(), storedHeight));
        }

        //PixelBitDepth
        Integer pixelBitDepth = imageEssenceDescriptorModel.getPixelBitDepth();
        if( !(pixelBitDepth.equals(8) || pixelBitDepth.equals(10))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid PixelBitDepth(%d) in Application#2 Composition",
                            imageEssenceDescriptorID.toString(), pixelBitDepth));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame) && !frameLayoutType.equals(FrameLayoutType.SeparateFields)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per Application#2 specification",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name()));
        }
        else {
            //SampleRate
            Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
            Set<Fraction> frameRateSupported = frameLayoutType.equals(FrameLayoutType.FullFrame) ? progressiveSampleRateSupported : interlaceSampleRateSupported;
            if (!frameRateSupported.contains(sampleRate)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has Invalid SampleRate(%s) for frame structure %s as per Application#2 specification",
                                imageEssenceDescriptorID.toString(), sampleRate.toString(), frameLayoutType.name()));
            }
        }

        //Sampling
        Sampling sampling = imageEssenceDescriptorModel.getSampling();
        if(frameLayoutType.equals(FrameLayoutType.SeparateFields) && !sampling.equals(Sampling.Sampling422) ) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid combination of FrameLayOut(%s) for Sampling(%s)  as per Application#2 specification",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), sampling.name() ));
        }

        //Quantization
        Quantization quantization = imageEssenceDescriptorModel.getQuantization();
        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if((sampling.equals(Sampling.Sampling422) &&
                !(quantization.equals(Quantization.QE1) && colorSpace.equals(ColorSpace.YUV))) ||
                (quantization.equals(Quantization.QE2) &&
                        !(colorSpace.equals(ColorSpace.RGB) && color.equals(Colorimetry.Color3)))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid combination of quantization(%s)-Sampling(%s)-colorSpace(%s)-color(%s) as per Application#2 specification",
                            imageEssenceDescriptorID.toString(), quantization.name(), sampling.name(), colorSpace.name(), color.name() ));
        }
    }
}
