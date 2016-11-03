package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.Null;
import static com.netflix.imflibrary.st2067_2.Colorimetry.*;


/**
 * Created by svenkatrav on 11/2/16.
 */
public final class CompositionImageEssenceDescriptorModel {
    private final UUID imageEssencedescriptorID;
    private final DOMNodeObjectModel imageEssencedescriptorDOMNode;
    private final RegXMLLibDictionary regXMLLibDictionary;
    private final IMFErrorLogger imfErrorLogger;
    private final ColorSpace colorSpace;
    private final FrameLayoutType frameLayoutType;
    private final Long storedWidth ;
    private final Long storedHeight;
    private final Fraction sampleRate;
    private final Integer pixelBitDepth;
    private final Colorimetry.Quantization quantization;
    private final Colorimetry color;
    private final Colorimetry.Sampling sampling;

    public CompositionImageEssenceDescriptorModel(UUID imageEssencedescriptorID, DOMNodeObjectModel imageEssencedescriptorDOMNode, RegXMLLibDictionary regXMLLibDictionary) {
        this.imageEssencedescriptorDOMNode = imageEssencedescriptorDOMNode;
        this.imageEssencedescriptorID = imageEssencedescriptorID;
        this.regXMLLibDictionary = regXMLLibDictionary;
        this.imfErrorLogger = new IMFErrorLoggerImpl();

        this.colorSpace = parseColorSpace();
        this.frameLayoutType = parseFrameLayout();
        this.storedWidth = parseStoredWidth();
        this.storedHeight = parseStoredHeight();
        this.sampleRate = parseSampleRate();
        this.pixelBitDepth = colorSpace != null ? parsePixelBitDepth(this.colorSpace) : null;
        this.quantization = (colorSpace != null && this.pixelBitDepth != null) ? parseQuantization(this.colorSpace, this.pixelBitDepth) : null;
        this.color = colorSpace != null ? parseColorimetry(this.colorSpace) : null;
        this.sampling = colorSpace != null ? parseSampling(this.colorSpace) : null;
    }


    public UUID getImageEssencedescriptorID() {
        return imageEssencedescriptorID;
    }

    public FrameLayoutType getFrameLayoutType() {
        return frameLayoutType;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public Colorimetry getColor() {
        return color;
    }

    public Fraction getSampleRate() {
        return sampleRate;
    }

    public Integer getPixelBitDepth() {
        return pixelBitDepth;
    }

    public Long getStoredHeight() {
        return storedHeight;
    }

    public Long getStoredWidth() {
        return storedWidth;
    }

    public Colorimetry.Quantization getQuantization() {
        return quantization;
    }

    public Colorimetry.Sampling getSampling() {
        return sampling;
    }

    private String getFieldAsString(String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private UL getFieldAsUL(String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(urn));

    }

    private Long getFieldAsNumber(String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsNumber(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private Integer getFieldAsInteger(String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private Fraction getFieldAsFraction(String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsFraction(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    /**
     * Getter for the errors
     *
     * @return List of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }


    public ColorSpace parseColorSpace() {
        ColorSpace colorSpace = null;

        if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(rgbaDescriptorUL))) {
            colorSpace = ColorSpace.RGB;
        } else if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(cdciDescriptorUL))) {
            colorSpace = ColorSpace.YUV;
        } else {
            //Section 6.1.2 Application#2
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s is missing RGBADescriptor/CDCIDescriptor in Application#2 Composition." +
                            imageEssencedescriptorID.toString()));
        }
        return colorSpace;
    }

    public FrameLayoutType parseFrameLayout() {
        String layout = getFieldAsString(frameLayoutUL);

        if(layout == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s is missing frameLayout in Application#2 Composition.",
                            imageEssencedescriptorID.toString()));
        }
        FrameLayoutType frameLayoutType = FrameLayoutType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(frameLayoutTypeUL, layout));

        if(frameLayoutType == null){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid frameLayout %s in Application#2 Composition.",
                            imageEssencedescriptorID.toString(), layout));
        }

        return frameLayoutType;
    }

    public Long parseStoredWidth() {
        Long storedWidth = getFieldAsNumber(storedWidthUL);

        //Dimension
        if (storedWidth == null || storedWidth < 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredWidth %d in Application#2 Composition.",
                            imageEssencedescriptorID.toString(), storedWidth != null ? storedWidth:0));
        }
        return storedWidth;
    }

    public Long parseStoredHeight() {
        Long storedHeight = getFieldAsNumber(storedHeightUL);

        //Dimension
        if (storedHeight == null || storedHeight < 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredHeight %d in Application#2 Composition.",
                            imageEssencedescriptorID.toString(), storedHeight != null ? storedHeight:0));
        }
        return storedHeight;
    }

    public Fraction parseSampleRate() {
        //SampleRate
        Fraction sampleRate = getFieldAsFraction(sampleRateUL);
        if (sampleRate == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s is missing SampleRate in Application#2 Composition.",
                            imageEssencedescriptorID.toString()));
        }

        return sampleRate;
    }

    public void parseStoredOffset() {
        //StoredF2Offset
        if (getFieldAsString(storedF2OffsetUL) != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has StoredF2Offset which shall not be present in Application#2 Composition",
                            imageEssencedescriptorID.toString()));
        }
    }

    public void parseSampleDimensions() {
        //SampledWidth and SampleHeight
        Long sampleWidth = getFieldAsNumber(sampledWidthUL);
        Long sampleHeight = getFieldAsNumber(sampledHeightUL);
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) || (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sampleWidth %d or sampleHeight %d in Application#2 Composition",
                            imageEssencedescriptorID.toString(), sampleWidth != null ? sampleWidth : 0, sampleHeight != null ? sampleHeight : 0));
        }
    }

    public Colorimetry parseColorimetry(ColorSpace colorSpace) {
        //ColorPrimaries
        Colorimetry.ColorPrimaries colorPrimaries = Colorimetry.ColorPrimaries.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(colorPrimariesUL)));
        if (colorPrimaries == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid/missing ColorPrimaries in Image Essence Descriptor",
                            imageEssencedescriptorID.toString()));
        }

        //TransferCharacteristic
        Colorimetry.TransferCharacteristic transferCharacteristic = Colorimetry.TransferCharacteristic.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(transferCharacteristicUL)));
        if (transferCharacteristic == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid/missing TransferCharacteristic in Image Essence Descriptor",
                            imageEssencedescriptorID.toString()));
        }

        //CodingEquations
        Colorimetry.CodingEquation codingEquation = null;
        if(colorSpace.equals(ColorSpace.YUV)) {
            codingEquation = Colorimetry.CodingEquation.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(codingEquationsUL)));
            if (codingEquation == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s  has invalid/missing CodingEquations in Image Essence Descriptor",
                                imageEssencedescriptorID.toString()));
            }
        }

        if(colorPrimaries != null && transferCharacteristic != null) {
            Colorimetry color = Colorimetry.valueOf(colorPrimaries, transferCharacteristic);
            if(color == null || (colorSpace.equals(ColorSpace.YUV) && color.getCodingEquation() != null && !color.getCodingEquation().equals(codingEquation))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s)-CodingEquation(%s) combination in Image Essence Descriptor",
                                imageEssencedescriptorID.toString(), colorPrimaries.toString(), transferCharacteristic.toString(), codingEquation != null ? codingEquation.toString() : ""));
            }
            return color;
        }

        return null;
    }

    public Integer parsePixelBitDepth(ColorSpace colorSpace) {
        Integer refPixelBitDepth = null;
        DOMNodeObjectModel subDescriptors = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
        if (subDescriptors == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s is missing SubDescriptors", imageEssencedescriptorID.toString()));

        } else {
            DOMNodeObjectModel jpeg2000SubdescriptorDOMNode = subDescriptors.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(jpeg2000SubDescriptorUL));

            if (jpeg2000SubdescriptorDOMNode == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing JPEG2000SubDescriptor in SubDescriptors",
                                imageEssencedescriptorID.toString()));
            } else {
                DOMNodeObjectModel j2cLayout = jpeg2000SubdescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(j2cLayoutUL));
                if (j2cLayout == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s is missing J2CLayout in JPEG2000SubDescriptor",
                                    imageEssencedescriptorID.toString()));
                } else {
                    List<DOMNodeObjectModel> rgbaComponents = j2cLayout.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(rgbaComponentUL));
                    if (rgbaComponents.size() == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("EssenceDescriptor with ID %s is missing RGBAComponent in J2CLayout",
                                        imageEssencedescriptorID.toString()));
                    } else {
                        Map<RGBAComponentType, Integer> componentMap = new HashMap<>();
                        for (DOMNodeObjectModel domNodeObjectModel : rgbaComponents) {
                            String code = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, codeUL));
                            if (code == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("EssenceDescriptor with ID %s has an RGBAComponent with missing Code",
                                                imageEssencedescriptorID.toString()));
                            } else {
                                RGBAComponentType codeValue = RGBAComponentType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(rgbaComponentKindUL, code));
                                if (codeValue == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                            String.format("EssenceDescriptor with ID %s has an RGBAComponent with unknown Code %s",
                                                    imageEssencedescriptorID.toString(), code));
                                } else {
                                    Integer pixelBitDepth = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, componentSizeUL));
                                    if (pixelBitDepth == null) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s has an RGBAComponent %s with missing ComponentSize",
                                                        imageEssencedescriptorID.toString(), code));
                                    } else {
                                        if (refPixelBitDepth == null) {
                                            refPixelBitDepth = pixelBitDepth;
                                        }
                                        if ((codeValue.equals(Null) && pixelBitDepth != 0) ||
                                                (!codeValue.equals(Null) && (!pixelBitDepth.equals(refPixelBitDepth)))) {
                                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                    String.format("EssenceDescriptor with ID %s has an RGBAComponent %s with invalid ComponentSize %d",
                                                            imageEssencedescriptorID.toString(), code, pixelBitDepth));
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
                        componentMap.entrySet().stream().forEach(
                                e -> {
                                    if (e.getKey().equals(RGBAComponentType.Null)) {
                                        if (!e.getValue().equals(5)) {
                                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                    String.format("EssenceDescriptor with ID %s and ColorSpace %s has invalid number of RGBAComponent %s in J2CLayout",
                                                            imageEssencedescriptorID.toString(), colorSpace.name(), e.getKey()));
                                        }
                                    } else if (!colorSpace.getComponentTypeSet().contains(e.getKey())) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s and ColorSpace %s has invalid RGBAComponent %s in J2CLayout",
                                                        imageEssencedescriptorID.toString(), colorSpace.name(), e.getKey()));
                                    } else if (!e.getValue().equals(1)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s and ColorSpace %s has more than one RGBAComponent %s in J2CLayout",
                                                        imageEssencedescriptorID.toString(), colorSpace.name(), e.getKey()));
                                    }

                                }
                        );
                    }
                }
            }
        }
        return refPixelBitDepth;
    }

    public Quantization parseQuantization(ColorSpace colorSpace, Integer pixelBitDepth) {
        Quantization quantization = null;
        Integer signalMin = null;
        Integer signalMax = null;

        if(colorSpace.equals(ColorSpace.RGB)) {
            signalMin = getFieldAsInteger(componentMinRefUL);
            signalMax = getFieldAsInteger(componentMaxRefUL);
            if (signalMax == null || signalMin == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing ComponentMinRef/ComponentMaxRef in Image Essence Descriptor",
                                imageEssencedescriptorID.toString()));
            }
        }
        else if(colorSpace.equals(ColorSpace.YUV)) {
            signalMin = getFieldAsInteger(blackRefLevelUL);
            signalMax = getFieldAsInteger(whiteRefLevelUL);
            if (signalMax == null || signalMin == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing BlackRefLevel/WhiteRefLevel in Image Essence Descriptor",
                                imageEssencedescriptorID.toString()));
            }
        }

        if(signalMax != null && signalMin != null) {
            quantization = Colorimetry.Quantization.valueOf(pixelBitDepth, signalMin, signalMax);
            if(quantization == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is invalid combination of ComponentMinRef/BlackRefLevel(%d)-ComponentMaxRef/WhiteRefLevel(%d)-PixelBitDepth(%d) in Image " +
                                "Essence Descriptor",
                                imageEssencedescriptorID.toString(), signalMin, signalMax, pixelBitDepth ));
            }
            else if(colorSpace.equals(ColorSpace.YUV) && !quantization.equals(Quantization.QE1) ) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is invalid combination of ColorSpace(%s)-Quantization(%s) in Image " +
                                        "Essence Descriptor",
                                imageEssencedescriptorID.toString(), colorSpace.name(), quantization.name() ));
            }
        }
        return quantization;
    }

    public Sampling parseSampling(ColorSpace colorSpace) {
        Colorimetry.Sampling sampling = null;
        if(colorSpace.equals(ColorSpace.RGB)) {
            return Colorimetry.Sampling.Sampling444;
        }
        else {
            Integer horizontalSubSampling   = getFieldAsInteger(horizontalSubSamplingUL);
            Integer verticalSubSampling     = getFieldAsInteger(verticalSubSamplingUL);
            if (horizontalSubSampling == null || verticalSubSampling == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing HorizontalSubSampling/VerticalSubSampling in Image Essence Descriptor",
                                imageEssencedescriptorID.toString()));
            }
            else {
                sampling = Colorimetry.Sampling.valueOf(horizontalSubSampling, verticalSubSampling);
                if(sampling == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s is invalid HorizontalSubSampling(%d)/VerticalSubSampling(%d) in Image Essence Descriptor",
                                    imageEssencedescriptorID.toString(), horizontalSubSampling, verticalSubSampling));
                }
            }
        }
        return sampling;
    }
}
