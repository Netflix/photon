package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;

import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.*;

/**
 * A class that models Composition with Application 2Extended constraints from 2067-21 specification
 */
public class Application2ExtendedComposition extends AbstractApplicationComposition {
    public static final Integer MAX_YUV_IMAGE_FRAME_WIDTH = 3840;
    public static final Integer MAX_YUV_IMAGE_FRAME_HEIGHT = 2160;
    public static final Integer MAX_RGB_IMAGE_FRAME_WIDTH = 4096;
    public static final Integer MAX_RGB_IMAGE_FRAME_HEIGHT = 3112;
    public static final Set<Fraction>rgbaSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24L)); add(new Fraction(25L)); add(new Fraction(30L)); add(new Fraction(50L)); add(new Fraction(60L)); add(new Fraction(120L));
        add(new Fraction(24000L, 1001L)); add(new Fraction(30000L, 1001L)); add(new Fraction(60000L, 1001L)); }});
    public static final Set<Fraction>yuvSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24L)); add(new Fraction(25L)); add(new Fraction(30L)); add(new Fraction(50L)); add(new Fraction(60L));
        add(new Fraction(24000L, 1001L)); add(new Fraction(30000L, 1001L)); add(new Fraction(60000L, 1001L)); }});
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
                Application2Composition.validateImageEssenceDescriptor(imageEssencedescriptorDOMNode, this.regXMLLibDictionary, app2ErrorLogger);
                if(app2ErrorLogger.getNumberOfErrors() != 0) {
                    validateImageEssenceDescriptor(imageEssencedescriptorDOMNode, this.regXMLLibDictionary, this.imfErrorLogger);
                }
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in Application2EComposition: %s ", e.getMessage()));
        }
    }

    public static void validateImageEssenceDescriptor(DOMNodeObjectModel imageEssencedescriptorDOMNode, RegXMLLibDictionary regXMLLibDictionary, IMFErrorLogger imfErrorLogger) {
        Boolean isRGBA = null;
        if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(rgbaDescriptorUL))) {
            isRGBA = true;
        } else if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(cdciDescriptorUL))) {
            isRGBA = false;
        }

        if (isRGBA == null) {
            //Section 6.1.2 Application#2
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid EssenceDescriptor %s in Application#2 Composition. " +
                            "Only RGBADescriptor or CDCIDescriptor are supported in Application#2E", imageEssencedescriptorDOMNode.getLocalName()));
        } else {

            String frameLayout = imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(frameLayoutUL));
            if (!frameLayout.equals(regXMLLibDictionary.getEnumerationNameFromValue(frameLayoutTypeUL, FullFrame.getValue()))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("FrameLayout value %s in %s not supported " +
                                "as per Application#2E Composition", frameLayout, imageEssencedescriptorDOMNode.getLocalName()));
            }


            Long storedWidth = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(storedWidthUL));
            Long storedHeight = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(storedHeightUL));

            //Dimension
            if (storedWidth == null || storedHeight == null || storedWidth < 1 ||
                    (storedWidth > MAX_RGB_IMAGE_FRAME_WIDTH && isRGBA) || (storedWidth > MAX_YUV_IMAGE_FRAME_WIDTH && !isRGBA) ||
                    storedHeight < 1 || (storedHeight > MAX_RGB_IMAGE_FRAME_HEIGHT && isRGBA) || (storedHeight > MAX_YUV_IMAGE_FRAME_HEIGHT && !isRGBA)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid Frame dimensions %sx%s" +
                                "in Application#2E Composition", storedWidth, storedHeight));
            }

            //SampleRate
            Fraction sampleRate = imageEssencedescriptorDOMNode.getFieldAsFraction(regXMLLibDictionary.getSymbolNameFromURN(sampleRateUL));
            if (sampleRate == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("SampleRate missing in %s", imageEssencedescriptorDOMNode.getLocalName()));
            } else {
                Set<Fraction> frameRateSupported = isRGBA ? rgbaSampleRateSupported : yuvSampleRateSupported;
                if (!frameRateSupported.contains(sampleRate)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid SampleRate %s" +
                                    "for %s Descriptor in Application#2E Composition", sampleRate, isRGBA ? "RGBA" : "CDCI"));
                }
            }

            //StoredF2Offset
            if (imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(storedF2OffsetUL)) != null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("StoredF2Offset shall not be present in" +
                                "Image Essence Descriptor in Application#2E Composition"));
            }

            //SampledWidth and SampleHeight
            Long sampleWidth = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(sampledWidthUL));
            Long sampleHeight = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(sampledHeightUL));
            if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) || (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("sampleWidth or sampleHeight shall not be present or " +
                                "shall be equal to storedWidth or storedHeight respectively in Image Essence Descriptor in Application#2E Composition"));
            }

            Colorimetry color = Application2Composition.getColorimetry(imageEssencedescriptorDOMNode, regXMLLibDictionary, isRGBA, imfErrorLogger);

            DOMNodeObjectModel subDescriptors = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
            if (subDescriptors == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("SubDescriptors missing in %s", imageEssencedescriptorDOMNode.getLocalName()));

            } else {
                DOMNodeObjectModel jpeg2000SubdescriptorDOMNode = subDescriptors.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(jpeg2000SubDescriptorUL));

                if (jpeg2000SubdescriptorDOMNode == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("JPEG2000SubDescriptor missing in SubDescriptors"));
                } else {
                    Set<Integer> validBitDepthSet = color != null ? colorToBitDepthMap.get(color) : bitDepthsSupported;
                    Application2Composition.validateJ2CLayout(jpeg2000SubdescriptorDOMNode, regXMLLibDictionary, isRGBA, validBitDepthSet, imfErrorLogger);
                }
            }
        }
    }
}
