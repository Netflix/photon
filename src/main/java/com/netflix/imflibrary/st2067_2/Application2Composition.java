package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;

import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st2067_2.Colorimetry.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.*;

/**
 * A class that models Composition with Application 2 constraints from 2067-20 specification
 */
public class Application2Composition extends AbstractApplicationComposition {
    public static final Integer MAX_IMAGE_FRAME_WIDTH = 1920;
    public static final Integer MAX_IMAGE_FRAME_HEIGHT = 1080;
    public static final Set<Fraction>progressiveSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24L)); add(new Fraction(25L)); add(new Fraction(30L)); add(new Fraction(50L)); add(new Fraction(60L));
        add(new Fraction(24000L, 1001L)); add(new Fraction(30000L, 1001L)); add(new Fraction(60000L, 1001L)); }});
    public static final Set<Fraction>interlaceSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24L)); add(new Fraction(30L)); add(new Fraction(30000L, 1001L)); }});
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

        try {
            DOMNodeObjectModel imageEssencedescriptorDOMNode = this.getEssenceDescriptor(
                    this.getVideoVirtualTrack().getTrackResourceIds().iterator().next());


            if (imageEssencedescriptorDOMNode != null) {
                validateImageEssenceDescriptor(imageEssencedescriptorDOMNode, this.regXMLLibDictionary, this.imfErrorLogger);
            }
        } catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in Application2Composition: %s ", e.getMessage()));
        }
    }

    public static void validateImageEssenceDescriptor(DOMNodeObjectModel imageEssencedescriptorDOMNode, RegXMLLibDictionary regXMLLibDictionary, IMFErrorLogger imfErrorLogger)
    {
        Boolean isRGBA = true;

        if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(rgbaDescriptorUL))) {
            isRGBA = true;
        } else if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(cdciDescriptorUL))) {
            isRGBA = false;
        } else  {
            //Section 6.1.2 Application#2
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid EssenceDescriptor %s in Application#2 Composition. " +
                            "Only RGBADescriptor or CDCIDescriptor are supported in Application#2", imageEssencedescriptorDOMNode.getLocalName()));
        }


        String frameLayout = imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(frameLayoutUL));
        Boolean isProgressive = true;

        if (frameLayout.equals(regXMLLibDictionary.getEnumerationNameFromValue(frameLayoutTypeUL, FullFrame.getValue()))) {
            isProgressive = true;
        } else if (frameLayout.equals(regXMLLibDictionary.getEnumerationNameFromValue(frameLayoutTypeUL, SeperateFields.getValue()))) {
            isProgressive = false;
        } else {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("FrameLayout value %s in %s not supported " +
                            "as per Application#2 Composition", frameLayout, imageEssencedescriptorDOMNode.getLocalName()));
        }


        Long storedWidth = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(storedWidthUL));
        Long storedHeight = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(storedHeightUL));

        //Dimension
        if (storedWidth == null || storedHeight == null || storedWidth < 1 ||
                (storedWidth > MAX_IMAGE_FRAME_WIDTH) ||
                storedHeight < 1 || (storedHeight > MAX_IMAGE_FRAME_HEIGHT)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid Frame dimensions %sx%s" +
                            "in Application#2 Composition", storedWidth, storedHeight));
        }

        //SampleRate
        Fraction sampleRate = imageEssencedescriptorDOMNode.getFieldAsFraction(regXMLLibDictionary.getSymbolNameFromURN(sampleRateUL));
        if (sampleRate == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("SampleRate missing in %s", imageEssencedescriptorDOMNode.getLocalName()));
        } else {
            Set<Fraction> frameRateSupported = isProgressive ? progressiveSampleRateSupported : interlaceSampleRateSupported;
            if (!frameRateSupported.contains(sampleRate)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid SampleRate %s" +
                                "for %s frame structure in Application#2 Composition", sampleRate, isProgressive ? "progressive" : "interlace"));
            }
        }

        //StoredF2Offset
        if (imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(storedF2OffsetUL)) != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("StoredF2Offset shall not be present in" +
                            "Image Essence Descriptor in Application#2 Composition"));
        }

        //SampledWidth and SampleHeight
        Long sampleWidth = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(sampledWidthUL));
        Long sampleHeight = imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(sampledHeightUL));
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) || (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("sampleWidth or sampleHeight shall not be present or " +
                            "shall be equal to storedWidth or storedHeight respectively in Image Essence Descriptor in Application#2 Composition"));
        }


        //Colorimetry
       Application2Composition.getColorimetry(imageEssencedescriptorDOMNode, regXMLLibDictionary, isRGBA, imfErrorLogger);

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
                validateJ2CLayout(jpeg2000SubdescriptorDOMNode, regXMLLibDictionary, isRGBA, bitDepthsSupported, imfErrorLogger);
            }
        }

        //BitDepth

    }

    public static void validateJ2CLayout(DOMNodeObjectModel jpeg2000SubdescriptorDOMNode, RegXMLLibDictionary regXMLLibDictionary, Boolean isRGBA, Set<Integer> bitDepthSet,
                                         IMFErrorLogger
                                         imfErrorLogger) {
        DOMNodeObjectModel j2cLayout = jpeg2000SubdescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(j2cLayoutUL));
        if (j2cLayout == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("J2CLayout missing in JPEG2000SubDescriptor"));
        } else {
            List<DOMNodeObjectModel> rgbaComponents = j2cLayout.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(rgbaComponentUL));
            if (rgbaComponents.size() == 0) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("No RGBAComponent present in J2CLayout"));
            } else {
                Integer refPixelBitDepth = null;
                Map<RGBAComponentType, Integer> componentMap = new HashMap<>();
                for (DOMNodeObjectModel domNodeObjectModel : rgbaComponents) {
                    String code = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, codeUL));
                    if (code == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("J2CLayout has a RGBAComponent with missing Code"));
                    }
                    else
                    {
                        RGBAComponentType codeValue = RGBAComponentType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(rgbaComponentKindUL, code));
                        if (codeValue == null) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("J2CLayout has a RGBAComponent with unknown Code %s", code));
                        }
                        else {
                            Integer pixelBitDepth = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, componentSizeUL));
                            if (pixelBitDepth == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("J2CLayout has a RGBAComponent %s with missing ComponentSize", code));
                            } else {
                                if(refPixelBitDepth == null) {
                                    refPixelBitDepth = pixelBitDepth;
                                }
                                if ((codeValue.equals(Null) && pixelBitDepth != 0) ||
                                        (!codeValue.equals(Null) && (!pixelBitDepth.equals(refPixelBitDepth) || !bitDepthSet.contains(pixelBitDepth)))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                            String.format("J2CLayout has a RGBAComponent %s with invalid ComponentSize %d", code, pixelBitDepth));
                                }
                            }
                            if (componentMap.containsKey(codeValue)) {
                                Integer count = componentMap.get(codeValue);
                                componentMap.put(codeValue, count + 1);
                            } else {
                                componentMap.put(codeValue, 1);
                            }
                        }
                    }
                }
                if (refPixelBitDepth != null && (!((isRGBA && componentMap.containsKey(Red) && componentMap.get(Red) == 1 && componentMap.containsKey(Blue) && componentMap.get(Blue) == 1 &&
                        componentMap.containsKey(Green) && componentMap.get(Green) == 1 && componentMap.containsKey(Null) && componentMap.get(Null)
                        == 5) ||
                        (!isRGBA && componentMap.containsKey(Luma) && componentMap.get(Luma) == 1 && componentMap.containsKey(ChromaU) && componentMap.get(ChromaU) == 1 &&
                                componentMap.containsKey(ChromaV) && componentMap.get(ChromaV) == 1 && componentMap.containsKey(Null) && componentMap.get(Null) == 5)))) {
                    String expectedLayout = isRGBA ?
                            "{ ‘R’, " + refPixelBitDepth + ", ‘G’, " + refPixelBitDepth + ", ‘B’, " + refPixelBitDepth + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }" :
                            "{ ‘Y’, " + refPixelBitDepth + ", ‘U’, " + refPixelBitDepth + ", ‘V’, " + refPixelBitDepth + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }";
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("RGBAComponent in J2CLayout does not match expected layout %s", expectedLayout));
                }

            }
        }
    }

    public static Colorimetry getColorimetry(DOMNodeObjectModel imageEssencedescriptorDOMNode, RegXMLLibDictionary regXMLLibDictionary, Boolean isRGBA, IMFErrorLogger imfErrorLogger) {
        //ColorPrimaries
        ColorPrimaries colorPrimaries = ColorPrimaries.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(colorPrimariesUL)));
        if (colorPrimaries == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid/missing ColorPrimaries in Image Essence Descriptor"));
        }

        //TransferCharacteristic
        TransferCharacteristic transferCharacteristic = TransferCharacteristic.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(transferCharacteristicUL)));
        if (transferCharacteristic == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid/missing TransferCharacteristic in Image Essence Descriptor"));
        }

        //CodingEquations
        CodingEquation codingEquation = null;
        if(!isRGBA) {
            codingEquation = CodingEquation.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(codingEquationsUL)));
            if (codingEquation == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid/missing CodingEquations in Image Essence Descriptor"));
            } else {

            }

        }

        if(colorPrimaries != null && transferCharacteristic != null) {
            Colorimetry color = Colorimetry.valueOf(colorPrimaries, transferCharacteristic);
            if(color == null || (!isRGBA && color.getCodingEquation() != null && !color.getCodingEquation().equals(codingEquation))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Invalid ColorPrimaries-TransferCharacteristic-CodingEquations combination in Image Essence Descriptor"));
            }
            return color;
        }

        return null;
    }
}
