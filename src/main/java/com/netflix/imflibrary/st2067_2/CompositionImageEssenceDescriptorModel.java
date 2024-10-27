package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.CodingEquation;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.ColorPrimaries;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.Colorimetry.TransferCharacteristic;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.J2KHeaderParameters;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final UL pictureEssenceCodingUL;
    // items constrained in 2065-5
    private final Integer signalStandard;
    private final Integer sampledXOffset;
    private final Integer sampledYOffset;
    private final Integer displayWidth;
    private final Integer displayHeight;
    private final Integer displayXOffset;
    private final Integer displayYOffset;
    private final Integer displayF2Offset;
    private final Fraction imageAspectRatio;
    private final Integer activeFormatDescriptor;
    private final Integer alphaTransparency;
    private final Integer imageAlignmentOffset;
    private final Integer imageStartOffset;
    private final Integer imageEndOffset;
    private final Integer fieldDominance;
    private final UL codingEquations;
    private final Integer componentMinRef;
    private final Integer componentMaxRef;
    private final Integer alphaMinRef;
    private final Integer alphaMaxRef;
    private final Integer scanningDirection;
    private final String palette;
    private final String paletteLayout;


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

        this.essenceContainerFormatUL = imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(containerFormatUL));

        this.pictureEssenceCodingUL = imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(GenericPictureEssenceDescriptor.pictureEssenceCodingUL));
        
        // begin Items constrained in ST2065-5
        this.signalStandard = getFieldAsInteger(signalStandardUL);
        this.sampledXOffset = getFieldAsInteger(sampledXOffsetUL);
        this.sampledYOffset = getFieldAsInteger(sampledYOffsetUL);
        Integer displayWidth = getFieldAsInteger(displayWidthUL);
        this.displayWidth = displayWidth != null ? displayWidth : -1;
        Integer displayHeight = getFieldAsInteger(displayHeightUL);
        this.displayHeight = displayHeight != null ? displayHeight : -1;
        this.displayXOffset = getFieldAsInteger(displayXOffsetUL);
        this.displayYOffset = getFieldAsInteger(displayYOffsetUL);
        this.displayF2Offset = getFieldAsInteger(displayF2OffsetUL);
        this.imageAspectRatio = getFieldAsFraction(imageAspectRatioUL);
        this.activeFormatDescriptor = getFieldAsInteger(activeFormatDescriptorUL);
        this.alphaTransparency = getFieldAsInteger(alphaTransparencyUL);
        this.imageAlignmentOffset = getFieldAsInteger(imageAlignmentOffsetUL);
        this.imageStartOffset = getFieldAsInteger(imageStartOffsetUL);
        this.imageEndOffset = getFieldAsInteger(imageEndOffsetUL);
        this.fieldDominance = getFieldAsInteger(fieldDominanceUL);
        this.codingEquations = imageEssencedescriptorDOMNode.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(codingEquationsUL));
        this.componentMinRef = getFieldAsInteger(componentMinRefUL);
        this.componentMaxRef = getFieldAsInteger(componentMaxRefUL);
        this.alphaMinRef = getFieldAsInteger(alphaMinRefUL);
        this.alphaMaxRef = getFieldAsInteger(alphaMaxRefUL);
        this.scanningDirection = getFieldAsInteger(scanningDirectionUL);

        this.palette = getFieldAsString(paletteUL);
        this.paletteLayout = getFieldAsString(paletteLayoutUL);
        // end Items constrained in ST2065-5

        UL MXFGCFrameWrappedACESPictures = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.0d010301.02190100"); // MXF-GC Frame-wrapped ACES Pictures per 2065-5
        if(!this.colorModel.equals(ColorModel.Unknown)) {
            if ((this.essenceContainerFormatUL != null) && getEssenceContainerFormatUL().equals(MXFGCFrameWrappedACESPictures) ) { // App #5
                if(colorModel.equals(ColorModel.RGB)) {
                    this.pixelBitDepth = null;
                    this.quantization = Quantization.Unknown;
                    this.color = Colorimetry.valueOf(this.colorPrimaries, this.transferCharacteristic);
                    this.sampling = Sampling.Unknown;
                    parseApp5SubDescriptors();
                    parseApp5PixelLayout();
                } else {
                    this.pixelBitDepth = null;
                    this.quantization = Quantization.Unknown;
                    this.color = Colorimetry.Unknown;
                    this.sampling = Sampling.Unknown;
                }
            } else  { // App #2/#2E
                this.pixelBitDepth = parsePixelBitDepth(this.colorModel);
                this.quantization = parseQuantization(this.colorModel, this.pixelBitDepth);

                Colorimetry color = Colorimetry.valueOf(this.colorPrimaries, this.transferCharacteristic);
                if((colorModel.equals(ColorModel.YUV) && !color.getCodingEquation().equals(this.codingEquation))) {
                    color = Colorimetry.Unknown;
                }
                this.color = color;

                this.sampling = parseSampling(this.colorModel);
            }
            parseVideoLineMap();
            this.j2kParameters = J2KHeaderParameters.fromDOMNode(imageEssencedescriptorDOMNode);
        }
        else {
            this.pixelBitDepth = null;
            this.quantization = Quantization.Unknown;
            this.color = Colorimetry.Unknown;
            this.sampling = Sampling.Unknown;
        }

    }

    public J2KHeaderParameters getJ2KHeaderParameters() {
        return this.j2kParameters;
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

    public @Nullable UL getPictureEssenceCodingUL() {
        return pictureEssenceCodingUL;
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

    public @Nullable Integer getSignalStandard() {
        return signalStandard;
    }
        
    public @Nullable Integer getSampledXOffset() {
        return sampledXOffset;
    }
    
    public @Nullable Integer getSampledYOffset() {
        return sampledYOffset;
    }
    
    public @Nonnull Integer getDisplayWidth() {
        return displayWidth;
    }
    
    public @Nonnull Integer getDisplayHeight() {
        return displayHeight;
    }
    
    public @Nullable Integer getDisplayXOffset() {
        return displayXOffset;
    }

    public @Nullable Integer getDisplayYOffset() {
        return displayYOffset;
    }

    public @Nullable Integer getDisplayF2Offset() {
        return displayF2Offset;
    }

    public @Nullable Fraction getImageAspectRatio() {
        return imageAspectRatio;
    }

    public @Nullable Integer getActiveFormatDescriptor() {
        return activeFormatDescriptor;
    }

    public @Nullable Integer getAlphaTransparency() {
        return alphaTransparency;
    }

    public @Nullable Integer getImageAlignmentOffset() {
        return imageAlignmentOffset;
    }

    public @Nullable Integer getImageStartOffset() {
        return imageStartOffset;
    }

    public @Nullable Integer getImageEndOffset() {
        return imageEndOffset;
    }

    public @Nullable Integer getFieldDominance() {
        return fieldDominance;
    }

    public @Nullable UL getCodingEquations() {
        return codingEquations;
    }

    public @Nullable Integer getComponentMinRef() {
        return componentMinRef;
    }

    public @Nullable Integer getComponentMaxRef() {
        return componentMaxRef;
    }

    public @Nullable Integer getAlphaMinRef() {
        return alphaMinRef;
    }

    public @Nullable Integer getAlphaMaxRef() {
        return alphaMaxRef;
    }

    public @Nullable Integer getScanningDirection() {
        return scanningDirection;
    }

    public @Nullable String getPalette() {
        return palette;
    }

    public @Nullable String getPaletteLayout() {
        return paletteLayout;
    }

    public enum ProgressionOrder {
        LRCP((short)0x00000000),    /* Layer-resolution level-component-position progression */
        RLCP((short)0b00000001),    /* Resolution level-layer-component-position progression */
        RPCL((short)0b00000010),    /* Resolution level-position-component-layer progression */
        PCRL((short)0b00000011),    /* Position-component-resolution level-layer progression */
        CPRL((short)0b00000100);    /* Component-position-resolution level-layer progression */

        private final short value;
        ProgressionOrder(short value) {
            this.value = value;
        }
        public short value() {
            return value;
        }
    }

    J2KHeaderParameters j2kParameters;

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

    private void parseApp5SubDescriptors() {
        DOMNodeObjectModel subDescriptors = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
        if (subDescriptors != null) {
            List<DOMNodeObjectModel> acesPictureSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorUL));
            List<DOMNodeObjectModel> targetFrameSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(targetFrameSubDescriptorUL));
            List<DOMNodeObjectModel> containerConstraintsSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(containerConstraintsSubDescriptorUL));
            if (!acesPictureSubDescriptors.isEmpty()) {
                for (DOMNodeObjectModel domNodeObjectModel : acesPictureSubDescriptors) {
                    String authoring_information = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(acesAuthoringInformationUL));
                    if ((authoring_information == null) || authoring_information.isEmpty()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                                String.format("ACES Picture SubDescriptor (ID %s): Optional item ACES Authoring Information is not present or empty", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString()));
                    }
                    DOMNodeObjectModel  primaries = domNodeObjectModel.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayPrimariesUL));
                    DOMNodeObjectModel  whitePoint = domNodeObjectModel.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayWhitePointChromaticityUL));
                    Integer maxLum = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayDisplayMaximumLuminanceUL));
                    Integer minLum = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayDisplayMinimumLuminanceUL));
                    if (!(((primaries == null) && (whitePoint == null) && (maxLum == null) && (minLum == null))
                            || ((primaries != null) && (whitePoint != null) && (maxLum != null) && (minLum != null)))) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("ACES Picture SubDescriptor (ID %s) shall have either all or none of the following elements: Mastering Display Primaries, White Point, Maximum Luminance, Minimum Luminance", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString()));
                    }
                }
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("INFO (can be ignored): EssenceDescriptor with ID %s: No ACESPictureSubDescriptor found", imageEssencedescriptorID.toString()));
            }
            if (!targetFrameSubDescriptors.isEmpty()) {
                for (DOMNodeObjectModel domNodeObjectModel : targetFrameSubDescriptors) {
                    String missing_items = "";
                    Set<UUID> targetFrameAncillaryResourceID = domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(targetFrameAncillaryResourceIDUL));
                    // Check for missing required items
                    if (targetFrameAncillaryResourceID.isEmpty()) {
                        missing_items += "TargetFrameAncillaryResourceID, ";
                    } else {
                        //TODO Check it targetFrameAncillaryResourceID belongs to an existing GSP
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                                String.format("INFO (can be ignored): Target FrameSubDescriptor (ID %s) references an Ancillary Resource (ID %s), but Ancillary Resources cannot be checked yet", 
                                        domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), targetFrameAncillaryResourceID.toString()));
                    }
                    String media_type = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(mediaTypeUL));
                    if (media_type == null) {
                        missing_items += "MediaType, ";
                    }
                    Long index = domNodeObjectModel.getFieldAsLong(regXMLLibDictionary.getSymbolNameFromURN(targetFrameIndexUL));
                    if (index == null) {
                        missing_items += "TargetFrameIndex, ";
                    }
                    UL transfer = domNodeObjectModel.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(targetFrameTransferCharacteristicUL));
                    if (transfer == null) {
                        missing_items += "TargetFrameTransferCharacteristic, ";
                    }
                    UL color = domNodeObjectModel.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(targetFrameColorPrimariesUL));
                    if (color == null) {
                        missing_items += "TargetFrameColorPrimaries, ";
                    }
                    Integer max_ref = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(targetFrameComponentMaxRefUL));
                    if (max_ref == null) {
                        missing_items += "TargetFrameComponentMaxRef, ";
                    }
                    Integer min_ref = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(targetFrameComponentMinRefUL));
                    if (min_ref == null) {
                        missing_items += "TargetFrameComponentMinRef, ";
                    }
                    Integer stream_id = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(targetFrameEssenceStreamIDUL));
                    if (stream_id == null) {
                        missing_items += "TargetFrameEssenceStreamID";
                    }
                    if (!missing_items.isEmpty()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("Target FrameSubDescriptor (ID %s): is missing required item(s): %s", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), missing_items));
                    }

                    // Check if acesPictureSubDescriptorInstanceID references an existing ACESPictureSubDescriptor Instance ID
                    Set<UUID> acesPictureSubDescriptorInstanceID = domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorInstanceIDUL));
                    if (!acesPictureSubDescriptorInstanceID.isEmpty()) {
                        if (acesPictureSubDescriptors.isEmpty()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("Target FrameSubDescriptor (ID %s) references an ACESPictureSubDescriptorInstanceID (%s) but no ACESPictureSubDescriptor is present", 
                                            domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), acesPictureSubDescriptorInstanceID.toString()));
                        } else {
                            if (acesPictureSubDescriptors.stream().noneMatch(e -> e.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).equals(acesPictureSubDescriptorInstanceID))) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("Target FrameSubDescriptor (ID %s) references an ACESPictureSubDescriptor but no ACESPictureSubDescriptor with Instance ID (%s) is present", 
                                                domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), acesPictureSubDescriptorInstanceID.toString()));
                            }
                        }
                        
                    }
                    
                    if ((max_ref != null) && (min_ref != null)) {
                        if (max_ref <= min_ref) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("Target FrameSubDescriptor (ID %s): TargetFrameComponentMaxRef (%d) is less than or equal to TargetFrameComponentMaxRef (%d)", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), max_ref, min_ref));
                        }
                    }
                }
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("INFO (can be ignored): EssenceDescriptor with ID %s: No TargetFrameSubDescriptor found", imageEssencedescriptorID.toString()));
            }
            if (containerConstraintsSubDescriptors.isEmpty()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("EssenceDescriptor with ID %s: A ContainerConstraintsSubDescriptor shall be present per ST 379-2, but is missing", imageEssencedescriptorID.toString()));
            }
        } else {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                    String.format("INFO (can be ignored): EssenceDescriptor with ID %s: No ACESPictureSubDescriptor and no TargetFrameSubDescriptor found", imageEssencedescriptorID.toString()));
        }
        return;
    }

    private void parseVideoLineMap() {
        DOMNodeObjectModel videoLineMap = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(videoLineMapUL));
        if (videoLineMap == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall have a Video Line Map item",
                            imageEssencedescriptorID.toString()));
        }
    }



    private void parseApp5PixelLayout() {
        DOMNodeObjectModel pixelLayout = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(pixelLayoutUL));
        if (pixelLayout == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall have a Pixel Layout item",
                            imageEssencedescriptorID.toString()));
        } else {
            List<DOMNodeObjectModel> rgbaComponents = pixelLayout.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(rgbaComponentUL));
            if (rgbaComponents.size() == 0) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing RGBAComponent in Pixel Layout",
                                imageEssencedescriptorID.toString()));
            } else {
                Map<RGBAComponentType, Integer> componentMap = new LinkedHashMap<>();
                List<RGBAComponentType> componentList = new ArrayList<>();
                for (DOMNodeObjectModel domNodeObjectModel : rgbaComponents) {
                    String code = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, codeUL));
                    if (code == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("EssenceDescriptor with ID %s has a Pixel Layout item with an RGBAComponent with missing Code",
                                        imageEssencedescriptorID.toString()));
                    } else {
                        RGBAComponentType codeValue = RGBAComponentType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(rgbaComponentKindUL, code));
                        Integer pixelBitDepth = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, componentSizeUL));
                        if (pixelBitDepth == null) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("EssenceDescriptor with ID %s has a Pixel Layout item with an RGBAComponent %s with missing Depth",
                                            imageEssencedescriptorID.toString(), code));
                        } else {
                            if ((codeValue.equals(Null) && pixelBitDepth != 0) ||
                                    (!codeValue.equals(Null) && (!pixelBitDepth.equals(253)))) { // In App#5, all components are of type HALF FLOAT
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("EssenceDescriptor with ID %s has a Pixel Layout item %s with invalid Depth value %d (expected depth value: 253)",
                                                imageEssencedescriptorID.toString(), code, pixelBitDepth));
                            }
                        }
                        if (componentMap.containsKey(codeValue)) {
                            Integer count = componentMap.get(codeValue);
                            componentMap.put(codeValue, count + 1);
                        } else {
                            componentMap.put(codeValue, 1);
                            componentList.add(codeValue);
                        }

                    }
                }
                boolean error = true;
                if (componentList.size() >= 4) { // ABGR or BGR per 2067-50 plus Null
                    if (componentList.get(0).toString().equals("Alpha")) { // ABGR per 2067-50
                        if (componentList.get(1).toString().equals("Blue") || componentList.get(2).toString().equals("Green") || componentList.get(3).toString().equals("Red") )
                            error = false;
                    } else if (componentList.get(0).toString().equals("Blue")) { // BGR per 2067-50
                        if (componentList.get(1).toString().equals("Green") || componentList.get(2).toString().equals("Red"))
                            error = false;
                    }
                }
                if (error) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s has incorrect PixelLayout %s",
                                    imageEssencedescriptorID.toString(), componentList.toString()));
                }
                componentMap.entrySet().stream().forEach(
                        e -> {
                            if (e.getKey().equals(RGBAComponentType.Null)) {
                                if (!(e.getValue().equals(5) || e.getValue().equals(4))) { // ABGR or BGR per 2067-50
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                            String.format("EssenceDescriptor with ID %s and ColorModel %s has invalid number of RGBAComponent %s in J2CLayout",
                                                    imageEssencedescriptorID.toString(), colorModel.name(), e.getKey()));
                                }
                            } else if (!e.getValue().equals(1)) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("EssenceDescriptor with ID %s has more than one RGBAComponent %s in Pixel Layout",
                                                imageEssencedescriptorID.toString(), e.getKey()));
                            }

                        }
                        );
            }
        }
    }
}
