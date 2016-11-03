package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.st2067_2.Colorimetry.*;


import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;

/**
 * A class that models Composition with Application 2Extended constraints from 2067-21 specification
 */
public class Application2ExtendedComposition extends AbstractApplicationComposition {
    public static final Integer MAX_YUV_IMAGE_FRAME_WIDTH = 3840;
    public static final Integer MAX_YUV_IMAGE_FRAME_HEIGHT = 2160;
    public static final Integer MAX_RGB_IMAGE_FRAME_WIDTH = 4096;
    public static final Integer MAX_RGB_IMAGE_FRAME_HEIGHT = 3112;
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
    }});

    public Application2ExtendedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        super(imfCompositionPlaylistType, ignoreSet);

        try
        {
            DOMNodeObjectModel imageEssencedescriptorDOMNode = this.getEssenceDescriptor(
                    this.getVideoVirtualTrack().getTrackResourceIds().iterator().next());

            if (imageEssencedescriptorDOMNode != null)
            {
                IMFErrorLogger app2ErrorLogger = new IMFErrorLoggerImpl();
                IMFErrorLogger app2ExtendedErrorLogger = new IMFErrorLoggerImpl();

                UUID imageEssenceDescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst()
                        .get();
                CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel =
                        new CompositionImageEssenceDescriptorModel( imageEssenceDescriptorID, imageEssencedescriptorDOMNode,
                        regXMLLibDictionary);
                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                Application2Composition.validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, imfErrorLogger);
                Application2Composition.validateImageCharacteristics(imageEssenceDescriptorModel, app2ErrorLogger);

                if(app2ErrorLogger.getNumberOfErrors() != 0) {
                    Application2ExtendedComposition.validateImageCharacteristics(imageEssenceDescriptorModel, app2ExtendedErrorLogger);
                    if(app2ExtendedErrorLogger.getNumberOfErrors() != 0) {
                        imfErrorLogger.addAllErrors(app2ErrorLogger.getErrors());
                        imfErrorLogger.addAllErrors(app2ExtendedErrorLogger.getErrors());
                    }
                }
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in Application2EComposition: %s ", e.getMessage()));
        }
    }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel, IMFErrorLogger
            imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorSpace colorSpace = imageEssenceDescriptorModel.getColorSpace();
        if( colorSpace.equals(ColorSpace.Unknown)) {
            return;
        }

        if( !colorSpace.equals(ColorSpace.RGB) && !colorSpace.equals(ColorSpace.YUV)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid ColorSpace(%s) in Application#2E Composition",
                            imageEssenceDescriptorID.toString(), colorSpace.name()));
        }

        //storedWidth
        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        if (storedWidth != null &&
                ((colorSpace.equals(ColorSpace.RGB) && storedWidth > MAX_RGB_IMAGE_FRAME_WIDTH) ||
                (colorSpace.equals(ColorSpace.YUV) && storedWidth > MAX_YUV_IMAGE_FRAME_WIDTH))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredWidth(%d) for ColorSpace(%s) as per Application#2E specification",
                            imageEssenceDescriptorID.toString(), storedWidth, colorSpace.name()));
        }

        //storedHeight
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if (storedHeight != null &&
                ((colorSpace.equals(ColorSpace.RGB) && storedHeight > MAX_RGB_IMAGE_FRAME_HEIGHT) ||
                        (colorSpace.equals(ColorSpace.YUV) && storedHeight > MAX_YUV_IMAGE_FRAME_HEIGHT))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedHeight(%d) for ColorSpace(%s) as per Application#2E specification",
                            imageEssenceDescriptorID.toString(), storedHeight, colorSpace.name()));
        }

        //PixelBitDepth
        Integer pixelBitDepth = imageEssenceDescriptorModel.getPixelBitDepth();
        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if( pixelBitDepth != null &&
                (!bitDepthsSupported.contains(pixelBitDepth) || !colorToBitDepthMap.get(color).contains(pixelBitDepth))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid PixelBitDepth(%d) for Color(%s) in Application#2E Composition",
                            imageEssenceDescriptorID.toString(), pixelBitDepth, color.name()));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per Application#2E specification",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name()));
        }

        //SampleRate
        Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
        Set<Fraction> frameRateSupported = colorSpace.equals(ColorSpace.RGB) ? rgbaSampleRateSupported : yuvSampleRateSupported;
        if (!frameRateSupported.contains(sampleRate)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid SampleRate(%s) for ColorSpace(%s) as per Application#2E specification",
                            imageEssenceDescriptorID.toString(), sampleRate.toString(), colorSpace.name()));
        }

        //Sampling
        Sampling sampling = imageEssenceDescriptorModel.getSampling();
        if((colorSpace.equals(ColorSpace.RGB) && !sampling.equals(Sampling.Sampling444)) ||
                (colorSpace.equals(ColorSpace.YUV) && !sampling.equals(Sampling.Sampling422))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Sampling(%s) for ColorSpace(%s) as per Application#2E specification",
                            imageEssenceDescriptorID.toString(), sampling.name(), colorSpace.name()));
        }

        //Quantization
        Quantization quantization = imageEssenceDescriptorModel.getQuantization();
        if((colorSpace.equals(ColorSpace.RGB) && !(quantization.equals(Quantization.QE2) || quantization.equals(Quantization.QE1))) ||
                (colorSpace.equals(ColorSpace.YUV) && !quantization.equals(Quantization.QE1))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Quantization(%s) for ColorSpace(%s) as per Application#2E specification",
                            imageEssenceDescriptorID.toString(), quantization.name(), colorSpace.name()));
        }
    }
}
