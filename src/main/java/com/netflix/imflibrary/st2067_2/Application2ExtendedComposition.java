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

                UUID imageEssencedescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst()
                        .get();
                CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel =
                        new CompositionImageEssenceDescriptorModel( imageEssencedescriptorID, imageEssencedescriptorDOMNode,
                        regXMLLibDictionary);
                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                Application2Composition.validateImageEssenceDescriptor(imageEssenceDescriptorModel, app2ErrorLogger);
                if(app2ErrorLogger.getNumberOfErrors() != 0) {
                    validateImageEssenceDescriptor(imageEssenceDescriptorModel, app2ExtendedErrorLogger);
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

    public static void validateImageEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel, IMFErrorLogger
            imfErrorLogger)
    {
        ColorSpace colorSpace = imageEssenceDescriptorModel.getColorSpace();
        Long storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Long storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        UUID imageEssencedescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        if( colorSpace == null || (!colorSpace.equals(ColorSpace.RGB) && !colorSpace.equals(ColorSpace.YUV))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid ColorSpace %s as per Application#2E specification",
                            imageEssencedescriptorID.toString(), colorSpace != null ? colorSpace.name() : ""));
        } else {
            //Dimension
            if ((storedWidth != null && storedWidth > MAX_RGB_IMAGE_FRAME_WIDTH && colorSpace.equals(ColorSpace.RGB)) ||
                    (storedWidth != null && storedWidth > MAX_YUV_IMAGE_FRAME_WIDTH && colorSpace.equals(ColorSpace.YUV)) ||
                    (storedHeight != null && storedHeight > MAX_RGB_IMAGE_FRAME_HEIGHT && colorSpace.equals(ColorSpace.RGB)) ||
                    (storedHeight != null && storedHeight > MAX_YUV_IMAGE_FRAME_HEIGHT && colorSpace.equals(ColorSpace.YUV))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid Frame dimensions %sx%s as per Application#2E specification",
                                imageEssencedescriptorID.toString(), storedWidth, storedHeight));
            }

            //SampleRate
            Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
            if (sampleRate != null) {
                Set<Fraction> frameRateSupported = colorSpace.equals(ColorSpace.RGB) ? rgbaSampleRateSupported : yuvSampleRateSupported;
                if (!frameRateSupported.contains(sampleRate)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s invalid SampleRate %s for %s Descriptor as per Application#2E specification",
                                    imageEssencedescriptorID.toString(), sampleRate, colorSpace.equals(ColorSpace.RGB) ? "RGBA" : "CDCI"));
                }
            }

            //FrameLayout
            FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
            if (frameLayoutType != null) {
                if (!frameLayoutType.equals(FrameLayoutType.FullFrame)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s has invalid StoredWidth %d or StoredHeight %d as per Application#2E specification",
                                    imageEssencedescriptorID.toString(), storedWidth != null ? storedWidth : 0,
                                    storedHeight != null ? storedHeight : 0));
                }
            }
        }
    }
}
