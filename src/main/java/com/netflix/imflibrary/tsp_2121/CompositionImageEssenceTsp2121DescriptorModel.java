package com.netflix.imflibrary.tsp_2121;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.Null;


public final class CompositionImageEssenceTsp2121DescriptorModel extends CompositionImageEssenceDescriptorModel {

    public CompositionImageEssenceTsp2121DescriptorModel(@Nonnull UUID imageEssenceDescriptorID,
                                                         @Nonnull DOMNodeObjectModel imageEssenceDescriptorDOMNode,
                                                         @Nonnull RegXMLLibDictionary regXMLLibDictionary) {
        super(imageEssenceDescriptorID, imageEssenceDescriptorDOMNode, regXMLLibDictionary);
    }

    @Override
    protected @Nonnull Integer parsePixelBitDepth(@Nonnull Colorimetry.ColorModel colorModel) {
        Integer refPixelBitDepth = null;
        DOMNodeObjectModel subDescriptors = getImageEssenceDescriptorDOMNode().getDOMNode(getRegXMLLibDictionary().getSymbolNameFromURN(subdescriptorsUL));
        if (subDescriptors == null) {
            Integer componentDepth = getComponentDepth();
            if (componentDepth == null || componentDepth == 0) {
                getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing SubDescriptors and missing Component Depth", getImageEssencedescriptorID().toString()));
            }
            refPixelBitDepth = componentDepth;
        } else {
            DOMNodeObjectModel jpeg2000SubdescriptorDOMNode = subDescriptors.getDOMNode(getRegXMLLibDictionary().getSymbolNameFromURN(jpeg2000SubDescriptorUL));

            if (jpeg2000SubdescriptorDOMNode == null) {
                getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing JPEG2000SubDescriptor in SubDescriptors",
                                getImageEssencedescriptorID().toString()));
            } else {
                DOMNodeObjectModel j2cLayout = jpeg2000SubdescriptorDOMNode.getDOMNode(getRegXMLLibDictionary().getSymbolNameFromURN(j2cLayoutUL));
                if (j2cLayout == null) {
                    getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s is missing J2CLayout in JPEG2000SubDescriptor",
                                    getImageEssencedescriptorID().toString()));
                } else {
                    List<DOMNodeObjectModel> rgbaComponents = j2cLayout.getDOMNodes(getRegXMLLibDictionary().getSymbolNameFromURN(rgbaComponentUL));
                    if (rgbaComponents.size() == 0) {
                        getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("EssenceDescriptor with ID %s is missing RGBAComponent in J2CLayout",
                                        getImageEssencedescriptorID().toString()));
                    } else {
                        Map<RGBAComponentType, Integer> componentMap = new HashMap<>();
                        for (DOMNodeObjectModel domNodeObjectModel : rgbaComponents) {
                            String code = domNodeObjectModel.getFieldAsString(getRegXMLLibDictionary().getTypeFieldNameFromURN(rgbaComponentUL, codeUL));
                            if (code == null) {
                                getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("EssenceDescriptor with ID %s has an RGBAComponent with missing Code",
                                                getImageEssencedescriptorID().toString()));
                            } else {
                                RGBAComponentType codeValue = RGBAComponentType.valueOf(getRegXMLLibDictionary().getEnumerationValueFromName(rgbaComponentKindUL, code));
                                Integer pixelBitDepth = domNodeObjectModel.getFieldAsInteger(getRegXMLLibDictionary().getTypeFieldNameFromURN(rgbaComponentUL, componentSizeUL));
                                if (pixelBitDepth == null) {
                                    getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                            String.format("EssenceDescriptor with ID %s has an RGBAComponent %s with missing ComponentSize",
                                                    getImageEssencedescriptorID().toString(), code));
                                } else {
                                    if (refPixelBitDepth == null) {
                                        refPixelBitDepth = pixelBitDepth;
                                    }
                                    if ((codeValue.equals(Null) && pixelBitDepth != 0) ||
                                            (!codeValue.equals(Null) && (!pixelBitDepth.equals(refPixelBitDepth)))) {
                                        getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s has an RGBAComponent %s with invalid ComponentSize %d",
                                                        getImageEssencedescriptorID().toString(), code, pixelBitDepth));
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
                                            getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                    String.format("EssenceDescriptor with ID %s and ColorModel %s has invalid number of RGBAComponent %s in J2CLayout",
                                                            getImageEssencedescriptorID().toString(), colorModel.name(), e.getKey()));
                                        }
                                    } else if (!colorModel.getComponentTypeSet().contains(e.getKey())) {
                                        getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s and ColorModel %s has invalid RGBAComponent %s in J2CLayout",
                                                        getImageEssencedescriptorID().toString(), colorModel.name(), e.getKey()));
                                    } else if (!e.getValue().equals(1)) {
                                        getImfErrorLogger().addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                                String.format("EssenceDescriptor with ID %s and ColorModel %s has more than one RGBAComponent %s in J2CLayout",
                                                        getImageEssencedescriptorID().toString(), colorModel.name(), e.getKey()));
                                    }

                                }
                        );
                    }
                }
            }
        }
        return refPixelBitDepth != null ? refPixelBitDepth : 0;
    }
}
