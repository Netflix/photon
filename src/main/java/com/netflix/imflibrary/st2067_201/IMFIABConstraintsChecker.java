package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_201.IABSoundfieldLabelSubDescriptor;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE;
import static com.netflix.imflibrary.st2067_201.IABEssenceDescriptor.IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL;
import static com.netflix.imflibrary.st2067_201.IABEssenceDescriptor.IMMERSIVE_AUDIO_CODING_LABEL;

public class IMFIABConstraintsChecker {
    private static final Integer IAB_BIT_DEPTH = 24;
    private static final short IAB_CHANNEL_COUNT = 0;

    public static List<ErrorLogger.ErrorObject> checkIABVirtualTrack(Composition.EditRate compositionEditRate,
                                                                     Composition.VirtualTrack virtualTrack,
                                                                     Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if (!virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.IABSequence)) return imfErrorLogger.getErrors();

        List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
        for(IMFBaseResourceType baseResource : virtualTrackResourceList) {
            IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(baseResource);
            DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
            if (!domNodeObjectModel.getLocalName().equals("IABEssenceDescriptor")) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                "an IAB VirtualTrack Resource does not have an IABEssenceDescriptor but %s",
                        imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getLocalName()));
            }

            if (((imfTrackFileResourceType.getEditRate().getNumerator()*compositionEditRate.getDenominator()) % (imfTrackFileResourceType.getEditRate().getDenominator()*compositionEditRate.getNumerator()) != 0)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The EditRate %s/%s of resource %s is not a multiple of the EditRate of the Main Image Virtual Track %s/%s",
                        imfTrackFileResourceType.getEditRate().getNumerator(), imfTrackFileResourceType.getEditRate().getDenominator(), imfTrackFileResourceType.getId(), compositionEditRate.getNumerator(), compositionEditRate.getDenominator()));
            }

            if (!domNodeObjectModel.getFieldAsUL("ContainerFormat").equals(IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                "an IAB VirtualTrack Resource does not use the correct Essence Container UL: %s vs. %s",
                        imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("ContainerFormat"), IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL));
            }

            if (domNodeObjectModel.getFieldAsUL("Codec") != null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                "an IAB VirtualTrack Resource shall not have a Codec item: %s",
                        imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("Codec")));
            }

            if (!domNodeObjectModel.getFieldAsUL("SoundCompression").equals(IMMERSIVE_AUDIO_CODING_LABEL)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                "an IAB VirtualTrack Resource does not use the correct Sound Compression UL: %s vs. %s",
                        imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("SoundCompression"), IMMERSIVE_AUDIO_CODING_LABEL));
            }

            if (domNodeObjectModel.getFieldAsInteger("QuantizationBits") != IAB_BIT_DEPTH.intValue()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                "an IAB VirtualTrack Resource has invalid QuantizationBits field: %s vs. %s",
                        imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsInteger("QuantizationBits"), IAB_BIT_DEPTH));
            }

            if (domNodeObjectModel.getFieldAsString("AudioSampleRate") == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                        "an IAB VirtualTrack Resource does not have an AudioSampleRate item.", imfTrackFileResourceType.getSourceEncoding()));
            }

            if (domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation") != null && domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation").intValue() != MULTI_CHANNEL_MODE.value()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                        "an IAB VirtualTrack Resource has an invalid value of ElectrospatialFormulation: %d vs. %d.", imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation"), MULTI_CHANNEL_MODE.value()));
            }

            if (domNodeObjectModel.getChildrenDOMNodes().size() == 0) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                        "an IAB VirtualTrack Resource has no SubDescriptor", imfTrackFileResourceType.getSourceEncoding()));
            } else {
                for (Map.Entry<DOMNodeObjectModel, Integer> entry : domNodeObjectModel.getChildrenDOMNodes().entrySet()) {
                    if (!entry.getKey().getLocalName().equals("SubDescriptors")) continue;

                    for (Map.Entry<DOMNodeObjectModel, Integer> subentry : entry.getKey().getChildrenDOMNodes().entrySet()) {

                        if (subentry.getKey().getLocalName().equals("AudioChannelLabelSubDescriptor") || subentry.getKey().getLocalName().equals("SoundfieldGroupLabelSubDescriptor") || subentry.getKey().getLocalName().equals("GroupOfSoundfieldGroupsLabelSubDescriptor")) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an IAB VirtualTrack Resource has forbidden %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getLocalName()));
                        } else if (subentry.getKey().getLocalName().equals("IABSoundfieldLabelSubDescriptor")) {
//                                if (subentry.getKey().getFieldAsString("MCAAudioContentKind") == null) {
//                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
//                                            "an IAB VirtualTrack Resource misses MCAAudioContentKind", imfTrackFileResourceType.getSourceEncoding()));
//                                }
//                                if (subentry.getKey().getFieldAsString("MCAAudioElementKind") == null) {
//                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
//                                            "an IAB VirtualTrack Resource misses MCAAudioElementKind", imfTrackFileResourceType.getSourceEncoding()));
//                                }
//                                if (subentry.getKey().getFieldAsString("MCATitle") == null) {
//                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
//                                            "an IAB VirtualTrack Resource misses MCATitle", imfTrackFileResourceType.getSourceEncoding()));
//                                }
//                                if (subentry.getKey().getFieldAsString("MCATitleVersion") == null) {
//                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
//                                            "an IAB VirtualTrack Resource misses MCATitleVersion", imfTrackFileResourceType.getSourceEncoding()));
//                                }
                            if (!IABSoundfieldLabelSubDescriptor.IAB_MCA_LABEL_DICTIONNARY_ID_UL.equals(subentry.getKey().getFieldAsUL("MCALabelDictionaryID"))) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                        "an IAB VirtualTrack Resource has invalid MCA Label Dictionary ID (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), IABSoundfieldLabelSubDescriptor.IAB_MCA_LABEL_DICTIONNARY_ID_UL));
                            }
                            if (!IABSoundfieldLabelSubDescriptor.IAB_MCA_TAG_SYMBOL.equals(subentry.getKey().getFieldAsString("MCATagSymbol"))) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                        "an IAB VirtualTrack Resource misses MCA Tag Symbol (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagSymbol"), IABSoundfieldLabelSubDescriptor.IAB_MCA_TAG_SYMBOL));
                            }
                            if (!IABSoundfieldLabelSubDescriptor.IAB_MCA_TAG_NAME.equals(subentry.getKey().getFieldAsString("MCATagName"))) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                        "an IAB VirtualTrack Resource misses MCA Tag Name (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagName"), IABSoundfieldLabelSubDescriptor.IAB_MCA_TAG_NAME));
                            }
                            if (subentry.getKey().getFieldAsString("MCAChannelID") != null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                        "an IAB VirtualTrack Resource has forbidden MCAChannelID %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAChannelID")));
                            }
                        }
                    }

                }
            }

        }
        return imfErrorLogger.getErrors();
    }
}
