package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_2.Composition.SequenceTypeEnum;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;
import com.netflix.imflibrary.utils.UUIDHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.smpte_ra.ns._2067_204._2022.ADMAudioVirtualTrackParameterSet;
import org.smpte_ra.ns._2067_204._2022.ADMSoundfieldGroupSelectorType;

import static com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE;

public class IMFADMAudioConstraintsChecker {
    private static final Integer ADM_BIT_DEPTH = 24;
    private static final short ADM_CHANNEL_COUNT = 0;

    // SMPTE ST 2067-204 Operational Modes
    public static final String ADM_AUDIO_OPERATIONAL_MODE_A = "http://www.smpte-ra.org/ns/2067-204/2022#Operational-Mode-A";
    static final List<String> ADM_AUDIO_OPERATIONAL_MODES = Collections.unmodifiableList(Arrays.asList(
            ADM_AUDIO_OPERATIONAL_MODE_A));

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
                                            "an ADM Audio VirtualTrack Resource has invalid MCA Label Dictionary ID (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONNARY_ID_UL));
                                }
                                if (!ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL.equals(subentry.getKey().getFieldAsString("MCATagSymbol"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource misses MCA Tag Symbol (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagSymbol"), ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL));
                                }
                                if (!ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME.equals(subentry.getKey().getFieldAsString("MCATagName"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource misses MCA Tag Name (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagName"), ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME));
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
    public static List<ErrorLogger.ErrorObject> checkADMAudioVirtualTrackParameterSet(ApplicationComposition applicationComposition,
            IMFErrorLogger compositionErrorLogger) {

        List<ADMAudioVirtualTrackParameterSet> admAudioVirtualTrackParameterSetList = new ArrayList<>();
        List<String> admAudioSignalSequenceTrackIds = new ArrayList<>();
        Map<UUID , List<String>> admAudioResourceHash = new LinkedHashMap<>();
        Set<Object> virtualTrackParameterSet = Collections.emptySet();
        if (applicationComposition.getExtensionProperties() != null) {
            virtualTrackParameterSet = applicationComposition.getExtensionProperties().getAny().stream().collect(Collectors.toSet());
            Iterator<Object> iterator = virtualTrackParameterSet.iterator();
            while (iterator != null && iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj.getClass() == ADMAudioVirtualTrackParameterSet.class) {
                    ADMAudioVirtualTrackParameterSet vtps = ADMAudioVirtualTrackParameterSet.class.cast(obj);
                    // collect all ADMAudioVirtualTrackParameterSet instances
                    admAudioVirtualTrackParameterSetList.add(vtps);
                    if (!ADM_AUDIO_OPERATIONAL_MODES.contains(vtps.getADMOperationalMode())) {
                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Value %s of Operational Mode not permitted in ADMAudioVirtualTrackParameterSet", vtps.getADMOperationalMode()));
                    }
                }
            }
        }
        for (IMFEssenceComponentVirtualTrack virtualTrack : applicationComposition.getEssenceVirtualTracks()) {
            // ST 2067-204, section 5.3.4, check for an ADM Audio Virtual Track Parameter Set for each ADM Audio Virtual Track
            if (virtualTrack.getSequenceTypeEnum() == SequenceTypeEnum.ADMAudioSequence) {
                admAudioSignalSequenceTrackIds.add(UUIDHelper.fromUUID(virtualTrack.getTrackID()));
                List<String> resource_id_list = new ArrayList<>();
                for (IMFBaseResourceType resource : virtualTrack.getResourceList()) {
                    // collect all resource IDs for a given Track ID
                    resource_id_list.add(resource.getId());
                }
                admAudioResourceHash.put(virtualTrack.getTrackID(), resource_id_list);

                if (applicationComposition.getExtensionProperties() != null) {
                    int trackIdsFound = 0;
                    for (ADMAudioVirtualTrackParameterSet vps : admAudioVirtualTrackParameterSetList) {
                        if (UUIDHelper.fromUUID(virtualTrack.getTrackID()).matches(vps.getTrackId())) trackIdsFound++;
                    }
                    if (trackIdsFound == 0) {
                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("No ADMAudioVirtualTrackParameterSet for ADM Audio Virtual Track %s present", virtualTrack.getTrackID().toString()));
                    } else if (trackIdsFound > 1) {
                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("There are %d ADMAudioVirtualTrackParameterSet for ADM Audio Virtual Track %s present, shall be only 1", trackIdsFound, virtualTrack.getTrackID().toString()));
                    }
                } else {
                    compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("No ADMAudioVirtualTrackParameterSet for ADM Audio Virtual Track %s present", virtualTrack.getTrackID().toString()));
                }
            }
        }
        // Check if any ADMAudioVirtualTrackParameterSet items do not correspond to an ADMAudioSequence
        for (ADMAudioVirtualTrackParameterSet vps : admAudioVirtualTrackParameterSetList) {
            if (admAudioSignalSequenceTrackIds.isEmpty()) {
                compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("ADMAudioVirtualTrackParameterSet for Track ID %s does not correspond to an ADM Audio Virtual Track", vps.getTrackId()));
            } else {
                if (!admAudioSignalSequenceTrackIds.contains(vps.getTrackId())) {
                    compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("ADMAudioVirtualTrackParameterSet for Track ID %s does not correspond to an ADM Audio Virtual Track", vps.getTrackId()));
                } else {
	                for (ADMSoundfieldGroupSelectorType adm_sg_selector: vps.getADMSoundfieldGroupSelector() ) {
	                    if (!admAudioResourceHash.get(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId())).contains(adm_sg_selector.getResourceId())) {
	                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
	                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("ADMSoundfieldGroupSelectorType for Track ID %s references unknown resource %s", vps.getTrackId(), adm_sg_selector.getResourceId()));
	                    } else {
	                        Optional<IMFEssenceComponentVirtualTrack> optional = applicationComposition.getEssenceVirtualTracks().stream().filter(e->e.getTrackID().equals(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId()))).findAny();
	                        if (optional.isPresent()) {
	                            IMFEssenceComponentVirtualTrack virtual_track = optional.get();
	                            Optional<IMFTrackFileResourceType> optional2 = virtual_track.getTrackFileResourceList().stream().filter(e->e.getId().equals(adm_sg_selector.getResourceId())).findAny();
	                            if (optional2.isPresent()) {
	                                DOMNodeObjectModel essence_descriptor_dom_node = applicationComposition.getEssenceDescriptor(UUIDHelper.fromUUIDAsURNStringToUUID(optional2.get().getTrackFileId()));
	                                List<UUID> mca_sg_link_id_list = new ArrayList<>();
	                                for (Map.Entry<DOMNodeObjectModel, Integer> entry : essence_descriptor_dom_node.getChildrenDOMNodes().entrySet()) {
	                                    if (!entry.getKey().getLocalName().equals("SubDescriptors")) continue;

	                                    for (Map.Entry<DOMNodeObjectModel, Integer> subentry : entry.getKey().getChildrenDOMNodes().entrySet()) {
	                                        if (subentry.getKey().getLocalName().equals("ADMSoundfieldGroupLabelSubDescriptor")) {
	                                            mca_sg_link_id_list.addAll(subentry.getKey().getFieldsAsUUID("MCALinkID"));
	                                        }
	                                    }
	                                }
	                                for (String link_id : adm_sg_selector.getADMSoundfieldGroupLinkID()) {
	                                    if (!mca_sg_link_id_list.contains(UUIDHelper.fromUUIDAsURNStringToUUID(link_id))) {
	                                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
	                                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("ADMSoundfieldGroupSelectorType for Track ID %s references unknown ADMSoundfieldGroupLinkID %s", vps.getTrackId(), link_id));
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
                }
            }
        }
    return compositionErrorLogger.getErrors();
    }
}
