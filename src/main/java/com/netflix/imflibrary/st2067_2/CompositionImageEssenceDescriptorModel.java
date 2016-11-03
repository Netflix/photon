package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.Null;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.Unknown;
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
    private final Integer storedWidth ;
    private final Integer storedHeight;
    private final Fraction sampleRate;
    private final Integer pixelBitDepth;
    private final Colorimetry.Quantization quantization;
    private final Colorimetry color;
    private final Colorimetry.Sampling sampling;
    private final Integer storedOffset;
    private final Integer sampleWidth;
    private final Integer sampleHeight;

    public CompositionImageEssenceDescriptorModel(@Nonnull UUID imageEssencedescriptorID, @Nonnull DOMNodeObjectModel imageEssencedescriptorDOMNode, @Nonnull RegXMLLibDictionary regXMLLibDictionary) {
        this.imageEssencedescriptorDOMNode = imageEssencedescriptorDOMNode;
        this.imageEssencedescriptorID = imageEssencedescriptorID;
        this.regXMLLibDictionary = regXMLLibDictionary;
        this.imfErrorLogger = new IMFErrorLoggerImpl();
        this.colorSpace = parseColorSpace();
        this.frameLayoutType = parseFrameLayout();
        this.storedWidth = parseStoredWidth();
        this.storedHeight = parseStoredHeight();
        this.sampleRate = parseSampleRate();
        this.storedOffset = parseStoredOffset();
        this.sampleHeight = parseSampleHeight();
        this.sampleWidth = parseSampleWidth();
        if(!this.colorSpace.equals(ColorSpace.Unknown)) {
            this.pixelBitDepth = parsePixelBitDepth(this.colorSpace);
            this.quantization = parseQuantization(this.colorSpace, this.pixelBitDepth);
            this.color = parseColorimetry(this.colorSpace);
            this.sampling = parseSampling(this.colorSpace);
        }
        else {
            this.pixelBitDepth = null;
            this.quantization = Quantization.Unknown;
            this.color = Colorimetry.Unknown;
            this.sampling = Sampling.Unknown;
        }
    }


    public @Nonnull UUID getImageEssencedescriptorID() {
        return imageEssencedescriptorID;
    }

    public @Nonnull FrameLayoutType getFrameLayoutType() {
        return frameLayoutType;
    }

    public @Nonnull ColorSpace getColorSpace() {
        return colorSpace;
    }

    public @Nonnull Colorimetry getColor() {
        return color;
    }

    public @Nonnull Fraction getSampleRate() {
        return sampleRate;
    }

    public @Nonnull Integer getPixelBitDepth() {
        return pixelBitDepth;
    }

    public @Nonnull Integer getStoredHeight() {
        return storedHeight;
    }

    public @Nonnull Integer getStoredWidth() {
        return storedWidth;
    }

    public @Nonnull Colorimetry.Quantization getQuantization() {
        return quantization;
    }

    public @Nonnull Colorimetry.Sampling getSampling() {
        return sampling;
    }

    public @Nullable Integer getStoredOffset() {
        return storedOffset;
    }

    public @Nullable Integer getSampleHeight() {
        return sampleHeight;
    }

    public @Nullable Integer getSampleWidth() {
        return sampleWidth;
    }

    public @Nonnull List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }

    private @Nullable String getFieldAsString(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nullable UL getFieldAsUL(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(urn));

    }

    private @Nullable Integer getFieldAsInteger(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nullable Fraction getFieldAsFraction(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsFraction(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nonnull ColorSpace parseColorSpace() {
        ColorSpace colorSpace = ColorSpace.Unknown;

        if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(rgbaDescriptorUL))) {
            colorSpace = ColorSpace.RGB;
        } else if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(cdciDescriptorUL))) {
            colorSpace = ColorSpace.YUV;
        }
        return colorSpace;
    }

    private @Nonnull FrameLayoutType parseFrameLayout() {
        return FrameLayoutType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(frameLayoutTypeUL, getFieldAsString(frameLayoutUL)));
    }

    private @Nonnull Integer parseStoredWidth() {
        Integer storedWidth = getFieldAsInteger(storedWidthUL);
        return storedWidth != null ? storedWidth : -1;
    }

    private @Nonnull Integer parseStoredHeight() {
        Integer storedHeight = getFieldAsInteger(storedHeightUL);
        return storedHeight != null ? storedHeight : -1;
    }

    private @Nonnull Fraction parseSampleRate() {
        //SampleRate
        Fraction sampleRate = getFieldAsFraction(sampleRateUL);
        return sampleRate != null ? sampleRate : new Fraction(0);
    }

    private @Nullable Integer parseStoredOffset() {
        //StoredF2Offset
        return getFieldAsInteger(storedF2OffsetUL);
    }

    private @Nullable Integer parseSampleWidth() {
        return getFieldAsInteger(sampledWidthUL);
    }

    private @Nullable Integer parseSampleHeight() {
        return getFieldAsInteger(sampledHeightUL);
    }

    private @Nonnull Colorimetry parseColorimetry(@Nonnull ColorSpace colorSpace) {
        //ColorPrimaries
        Colorimetry.ColorPrimaries colorPrimaries = Colorimetry.ColorPrimaries.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(colorPrimariesUL)));
        //TransferCharacteristic
        Colorimetry.TransferCharacteristic transferCharacteristic = Colorimetry.TransferCharacteristic.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN
                (transferCharacteristicUL)));

        //CodingEquations
        Colorimetry.CodingEquation codingEquation = CodingEquation.Unknown;
        if(colorSpace.equals(ColorSpace.RGB)) {
            codingEquation = CodingEquation.None;
        }
        else if(colorSpace.equals(ColorSpace.YUV)) {
            codingEquation = Colorimetry.CodingEquation.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(codingEquationsUL)));
        }

        Colorimetry color = Colorimetry.valueOf(colorPrimaries, transferCharacteristic);
        if((colorSpace.equals(ColorSpace.YUV) && !color.getCodingEquation().equals(codingEquation))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s)-CodingEquation(%s) combination in Image Essence Descriptor",
                            imageEssencedescriptorID.toString(), colorPrimaries.toString(), transferCharacteristic.toString(), codingEquation != null ? codingEquation.toString() : ""));
        }
        return color;
    }

    private @Nonnull Integer parsePixelBitDepth(@Nonnull ColorSpace colorSpace) {
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
        return refPixelBitDepth != null ? refPixelBitDepth : 0;
    }

    private @Nonnull Quantization parseQuantization(@Nonnull ColorSpace colorSpace, @Nonnull Integer pixelBitDepth) {
        Quantization quantization = Quantization.Unknown;
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
        }
        return quantization;
    }

    private @Nonnull Sampling parseSampling(@Nonnull ColorSpace colorSpace) {
        Colorimetry.Sampling sampling = Sampling.Unknown;
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
            }
        }
        return sampling;
    }
}
