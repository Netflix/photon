package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor;
import com.netflix.imflibrary.st2067_203.MGASoundfieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;
import com.netflix.imflibrary.utils.UUIDHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE;
import static com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor.IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL;
import static com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor.MGA_AUDIOESSENCE_UNCOMPRESSED_SOUND_ENCODING_LABEL;

public class IMFADMAudioConstraintsChecker {
    private static final Integer ADM_BIT_DEPTH = 24;
    private static final short ADM_CHANNEL_COUNT = 0;

    public static List<ErrorLogger.ErrorObject> checkADMAudioVirtualTrack(Composition.EditRate compositionEditRate,
                                                                     Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap,
                                                                     Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap,
                                                                     RegXMLLibDictionary regXMLLibDictionary,
                                                                     Set<String> homogeneitySelectionSet) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Iterator iterator = virtualTrackMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (!virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.ADMAudioSequence)) continue;

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            for(IMFBaseResourceType baseResource : virtualTrackResourceList) {
                IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(baseResource);
                DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
                if (!domNodeObjectModel.getLocalName().equals("WAVEPCMDescriptor")) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ADM Audio VirtualTrack Resource does not have a WAVEPCMDescriptor but %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getLocalName()));
                }

                if (((imfTrackFileResourceType.getEditRate().getNumerator()*compositionEditRate.getDenominator()) % (imfTrackFileResourceType.getEditRate().getDenominator()*compositionEditRate.getNumerator()) != 0)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The EditRate %s/%s of resource %s is not 48000/ or 96000/1",
                            imfTrackFileResourceType.getEditRate().getNumerator(), imfTrackFileResourceType.getEditRate().getDenominator(), imfTrackFileResourceType.getId()));
                }

                if (domNodeObjectModel.getFieldAsInteger("QuantizationBits") != ADM_BIT_DEPTH.intValue()) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ADM Audio VirtualTrack Resource has invalid QuantizationBits field: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsInteger("QuantizationBits"), ADM_BIT_DEPTH));
                }

                if (domNodeObjectModel.getFieldAsString("AudioSampleRate") == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ADM Audio VirtualTrack Resource does not have an AudioSampleRate item.", imfTrackFileResourceType.getSourceEncoding()));
                }

                if (domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation") != null && domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation").intValue() != MULTI_CHANNEL_MODE.value()) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                            "an ADM Audio VirtualTrack Resource has an invalid value of ElectrospatialFormulation: %d vs. %d.", imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation"), MULTI_CHANNEL_MODE.value()));
                }

                if (domNodeObjectModel.getChildrenDOMNodes().size() == 0) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ADM Audio VirtualTrack Resource has no SubDescriptor", imfTrackFileResourceType.getSourceEncoding()));
                } else {
                    for (Map.Entry<DOMNodeObjectModel, Integer> entry : domNodeObjectModel.getChildrenDOMNodes().entrySet()) {
                        if (!entry.getKey().getLocalName().equals("SubDescriptors")) continue;

                        for (Map.Entry<DOMNodeObjectModel, Integer> subentry : entry.getKey().getChildrenDOMNodes().entrySet()) {

                            if (subentry.getKey().getLocalName().equals("AudioChannelLabelSubDescriptor") || subentry.getKey().getLocalName().equals("SoundFieldGroupLabelSubDescriptor") || subentry.getKey().getLocalName().equals("GroupOfSoundfieldGroupsLabelSubDescriptor")) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ADM Audio VirtualTrack Resource has forbidden %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getLocalName()));
                            } else if (subentry.getKey().getLocalName().equals("ADMSoundfieldGroupLabelSubDescriptor")) {
                                if (!ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONNARY_ID_UL.equals(subentry.getKey().getFieldAsUL("MCALabelDictionaryID"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource has invalid MCA Label Dictionary ID (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL));
                                }
                                if (!ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL.equals(subentry.getKey().getFieldAsString("MCATagSymbol"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource misses MCA Tag Symbol (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagSymbol"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_SYMBOL));
                                }
                                if (!ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME.equals(subentry.getKey().getFieldAsString("MCATagName"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource misses MCA Tag Name (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagName"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_NAME));
                                }
                                if (subentry.getKey().getFieldAsString("MCAChannelID") != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource has forbidden MCAChannelID %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAChannelID")));
                                }
                            }
                        }

                    }
                }
            }
        }
        return imfErrorLogger.getErrors();
    }
}
