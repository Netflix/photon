package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
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
import static com.netflix.imflibrary.Colorimetry.*;


/**
 * Created by svenkatrav on 11/2/16.
 */
public final class CompositionImageEssenceDescriptorModel {
    private final UUID imageEssencedescriptorID;
    private final DOMNodeObjectModel imageEssencedescriptorDOMNode;
    private final RegXMLLibDictionary regXMLLibDictionary;
    private final IMFErrorLogger imfErrorLogger;
    private final ColorModel colorModel;
    private final FrameLayoutType frameLayoutType;
    private final Integer storedWidth ;
    private final Integer storedHeight;
    private final Fraction sampleRate;
    private final Integer pixelBitDepth;
    private final Integer componentDepth;
    private final Colorimetry.Quantization quantization;
    private final Colorimetry color;
    private final Colorimetry.Sampling sampling;
    private final Integer storedOffset;
    private final Integer sampleWidth;
    private final Integer sampleHeight;
    private final CodingEquation codingEquation;
    private final TransferCharacteristic transferCharacteristic;
    private final ColorPrimaries colorPrimaries;
    private final UL essenceContainerFormatUL;

    public CompositionImageEssenceDescriptorModel(@Nonnull UUID imageEssencedescriptorID, @Nonnull DOMNodeObjectModel imageEssencedescriptorDOMNode, @Nonnull RegXMLLibDictionary regXMLLibDictionary)
    {
        this.imageEssencedescriptorDOMNode = imageEssencedescriptorDOMNode;
        this.imageEssencedescriptorID = imageEssencedescriptorID;
        this.regXMLLibDictionary = regXMLLibDictionary;
        this.imfErrorLogger = new IMFErrorLoggerImpl();

        if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(rgbaDescriptorUL))) {
          this.colorModel = ColorModel.RGB;
        } else if (imageEssencedescriptorDOMNode.getLocalName().equals(regXMLLibDictionary.getSymbolNameFromURN(cdciDescriptorUL))) {
            this.colorModel = ColorModel.YUV;
        } else {
            this.colorModel = ColorModel.Unknown;
        }

        this.frameLayoutType = FrameLayoutType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(frameLayoutTypeUL, getFieldAsString(frameLayoutUL)));

        Integer storedWidth = getFieldAsInteger(storedWidthUL);
        storedWidth = storedWidth != null ? storedWidth : -1;
        this.storedWidth = storedWidth;

        Integer storedHeight = getFieldAsInteger(storedHeightUL);
        storedHeight = storedHeight != null ? storedHeight : -1;
        this.storedHeight = storedHeight;

        Fraction sampleRate = getFieldAsFraction(sampleRateUL);
        sampleRate = sampleRate != null ? sampleRate : new Fraction(0);
        this.sampleRate = sampleRate;

        this.storedOffset = getFieldAsInteger(storedF2OffsetUL);
        this.sampleHeight = getFieldAsInteger(sampledHeightUL);
        this.sampleWidth = getFieldAsInteger(sampledWidthUL);

        this.componentDepth = getFieldAsInteger(componentDepthUL);

        this.transferCharacteristic = Colorimetry.TransferCharacteristic.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN
                (transferCharacteristicUL)));

        this.colorPrimaries = Colorimetry.ColorPrimaries.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(colorPrimariesUL)));

        if(colorModel.equals(ColorModel.YUV)) {
            this.codingEquation = Colorimetry.CodingEquation.valueOf(imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(codingEquationsUL)));
        }
        else {
            this.codingEquation = CodingEquation.None;
        }

        if(!this.colorModel.equals(ColorModel.Unknown)) {
            this.pixelBitDepth = parsePixelBitDepth(this.colorModel);
            this.quantization = parseQuantization(this.colorModel, this.pixelBitDepth);

            Colorimetry color = Colorimetry.valueOf(this.colorPrimaries, this.transferCharacteristic);
            if((colorModel.equals(ColorModel.YUV) && !color.getCodingEquation().equals(this.codingEquation))) {
                color = Colorimetry.Unknown;
            }
            this.color = color;

            this.sampling = parseSampling(this.colorModel);
        }
        else {
            this.pixelBitDepth = null;
            this.quantization = Quantization.Unknown;
            this.color = Colorimetry.Unknown;
            this.sampling = Sampling.Unknown;
        }

        this.essenceContainerFormatUL = imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(containerFormatUL));
    }


    public @Nonnull UUID getImageEssencedescriptorID() {
        return imageEssencedescriptorID;
    }

    public @Nonnull FrameLayoutType getFrameLayoutType() {
        return frameLayoutType;
    }

    public @Nonnull
    ColorModel getColorModel() {
        return colorModel;
    }

    public @Nonnull Colorimetry getColor() {
        return color;
    }

    public @Nonnull Fraction getSampleRate() {
        return sampleRate;
    }

    public Integer getComponentDepth() {
        return componentDepth;
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

    public CodingEquation getCodingEquation() {
        return codingEquation;
    }

    public ColorPrimaries getColorPrimaries() {
        return colorPrimaries;
    }

    public TransferCharacteristic getTransferCharacteristic() {
        return transferCharacteristic;
    }

    public @Nullable UL getEssenceContainerFormatUL() {
        return essenceContainerFormatUL;
    }

    private @Nullable String getFieldAsString(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nullable Integer getFieldAsInteger(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nullable Long getFieldAsLong(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsLong(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nullable Fraction getFieldAsFraction(@Nonnull String urn) {
        return imageEssencedescriptorDOMNode.getFieldAsFraction(regXMLLibDictionary.getSymbolNameFromURN(urn));
    }

    private @Nonnull Integer parsePixelBitDepth(@Nonnull ColorModel colorModel) {
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
                                                    String.format("EssenceDescriptor with ID %s and ColorModel %s has invalid number of RGBAComponent %s in J2CLayout",
                                                            imageEssencedescriptorID.toString(), colorModel.name(), e.getKey()));
                                        }
                                    } else if (!colorModel.getComponentTypeSet().contains(e.getKey())) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s and ColorModel %s has invalid RGBAComponent %s in J2CLayout",
                                                        imageEssencedescriptorID.toString(), colorModel.name(), e.getKey()));
                                    } else if (!e.getValue().equals(1)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s and ColorModel %s has more than one RGBAComponent %s in J2CLayout",
                                                        imageEssencedescriptorID.toString(), colorModel.name(), e.getKey()));
                                    }

                                }
                        );
                    }
                }
            }
        }
        return refPixelBitDepth != null ? refPixelBitDepth : 0;
    }

    private @Nonnull Quantization parseQuantization(@Nonnull ColorModel colorModel, @Nonnull Integer pixelBitDepth) {
        Quantization quantization = Quantization.Unknown;
        Long signalMin = null;
        Long signalMax = null;

        if(colorModel.equals(ColorModel.RGB)) {
            signalMin = getFieldAsLong(componentMinRefUL);
            signalMax = getFieldAsLong(componentMaxRefUL);
            if (signalMax == null || signalMin == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing ComponentMinRef/ComponentMaxRef in Image Essence Descriptor",
                                imageEssencedescriptorID.toString()));
            }
        }
        else if(colorModel.equals(ColorModel.YUV)) {
            signalMin = getFieldAsLong(blackRefLevelUL);
            signalMax = getFieldAsLong(whiteRefLevelUL);
            if (signalMax == null || signalMin == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing BlackRefLevel/WhiteRefLevel in Image Essence Descriptor",
                                imageEssencedescriptorID.toString()));
            }
        }

        if(signalMax != null && signalMin != null) {
            quantization = Colorimetry.Quantization.valueOf(pixelBitDepth, signalMin, signalMax);
            if(quantization.equals(Quantization.Unknown)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid combination of ComponentMinRef/BlackRefLevel(%d)-ComponentMaxRef/WhiteRefLevel(%d)-PixelBitDepth(%d) in Image " +
                                        "Essence Descriptor",
                                imageEssencedescriptorID.toString(), signalMin, signalMax, pixelBitDepth ));
            }
        }
        return quantization;
    }

    private @Nonnull Sampling parseSampling(@Nonnull ColorModel colorModel) {
        Colorimetry.Sampling sampling = Sampling.Unknown;
        if(colorModel.equals(ColorModel.RGB)) {
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
                if(sampling.equals(Sampling.Unknown)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s has invalid combination of HorizontalSubSampling(%d)-VerticalSubSampling(%d) in Image " +
                                            "Essence Descriptor",
                                    imageEssencedescriptorID.toString(), horizontalSubSampling, verticalSubSampling));
                }
            }
        }
        return sampling;
    }
}
