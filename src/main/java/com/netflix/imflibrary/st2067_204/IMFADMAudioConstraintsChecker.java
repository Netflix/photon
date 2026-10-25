/*
 *
 * Copyright 2026 RheinMain University of Applied Sciences, Wiesbaden, Germany.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.st0377_41.*;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
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

public class IMFADMAudioConstraintsChecker {
    private static final Integer ADM_BIT_DEPTH = 24;

    // SMPTE ST 2067-204 Operational Modes
    public static final String ADM_AUDIO_OPERATIONAL_MODE_A = "http://www.smpte-ra.org/ns/2067-204/2022#Operational-Mode-A";
    static final List<String> ADM_AUDIO_OPERATIONAL_MODES = Collections.unmodifiableList(Arrays.asList(
            ADM_AUDIO_OPERATIONAL_MODE_A));

    public static List<ErrorLogger.ErrorObject> checkADMAudioVirtualTrack(Composition.EditRate compositionEditRate,
                                                                     Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap,
                                                                     Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap,
                                                                     Set<String> homogeneitySelectionSet) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Iterator iterator = virtualTrackMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (!virtualTrack.getSequenceType().equals("ADMAudioSequence")) continue;

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            for(IMFBaseResourceType baseResource : virtualTrackResourceList) {
                IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(baseResource);
                DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
                if (!domNodeObjectModel.getLocalName().equals("WAVEPCMDescriptor")) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ADM Audio VirtualTrack Resource does not have a WAVEPCMDescriptor but %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getLocalName()));
                }

                if (((imfTrackFileResourceType.getEditRate().getNumerator() != 48000) && (imfTrackFileResourceType.getEditRate().getNumerator() != 96000)) || (imfTrackFileResourceType.getEditRate().getDenominator() != 1)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The EditRate %s/%s of resource %s is not 48000/1 or 96000/1",
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
                                int num_mca_adm_labels = 0;
                                if (subentry.getKey().getFieldAsUL("MCALabelDictionaryID") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCA Label Dictionary ID", imfTrackFileResourceType.getSourceEncoding()));

                                } else if (ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONARY_ID_UL.equalsWithMask(subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), 0b1111111011111111)) {//)(subentry.getKey().getFieldAsUL("MCALabelDictionaryID"))) {
                                    num_mca_adm_labels++;
                                }
                                if (subentry.getKey().getFieldAsString("MCATagSymbol") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCATagSymbol", imfTrackFileResourceType.getSourceEncoding()));

                                } else if (ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL.equals(subentry.getKey().getFieldAsString("MCATagSymbol"))) {
                                    num_mca_adm_labels++;
                                }
                                if (subentry.getKey().getFieldAsString("MCATagName") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCATagName", imfTrackFileResourceType.getSourceEncoding()));

                                } else if (ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME.equals(subentry.getKey().getFieldAsString("MCATagName"))) {
                                    num_mca_adm_labels++;
                                }
                                if ((num_mca_adm_labels > 0) && (num_mca_adm_labels < 3)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource uses ADM Soundfield Group Labels only in %d, but not all of the items MCALabelDictionaryID, MCATagSymbol, MCATagName", imfTrackFileResourceType.getSourceEncoding(), num_mca_adm_labels));
                                }
                                if (subentry.getKey().getFieldAsString("MCAContent") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCAContent", imfTrackFileResourceType.getSourceEncoding()));
                                } else if (MCAContent.getValueFromSymbol(subentry.getKey().getFieldAsString("MCAContent")) == MCAContent.Unknown) {
                                    // Check against ST 377-41:2021 Table 2
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource has invalid MCA Content (%s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAContent")));
                                }
                                if (subentry.getKey().getFieldAsString("MCAUseClass") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCAUseClass", imfTrackFileResourceType.getSourceEncoding()));
                                } else if (MCAUseClass.getValueFromSymbol(subentry.getKey().getFieldAsString("MCAUseClass")) == MCAUseClass.Unknown) {
                                    // Check against ST 377-41:2021 Table 3
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource has invalid MCA Use Class (%s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAUseClass")));
                                }
                                // Check against ST 2067-203:2023 Table 3
                                if (subentry.getKey().getFieldAsString("MCATitle") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCATitle", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                // Check against ST 2067-203:2023 Table 3
                                if (subentry.getKey().getFieldAsString("MCATitleVersion") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an ADM Audio VirtualTrack Resource is missing MCATitleVersion", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                if (subentry.getKey().getFieldAsString("MCAChannelID") != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
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
    public static List<ErrorLogger.ErrorObject> checkADMAudioVirtualTrackParameterSet(IMFCompositionPlaylist imfCompositionPlaylist) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
    	List<ADMAudioVirtualTrackParameterSet> admAudioVirtualTrackParameterSetList = new ArrayList<>();
        List<String> admAudioSignalSequenceTrackIds = new ArrayList<>();
        Map<UUID , List<String>> admAudioResourceHash = new LinkedHashMap<>();
        Set<Object> virtualTrackParameterSet = Collections.emptySet();
        if (imfCompositionPlaylist.getExtensionProperties() != null) {
            virtualTrackParameterSet = imfCompositionPlaylist.getExtensionProperties().getAny().stream().collect(Collectors.toSet());
            Iterator<Object> iterator = virtualTrackParameterSet.iterator();
            while (iterator != null && iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj.getClass() == ADMAudioVirtualTrackParameterSet.class) {
                    ADMAudioVirtualTrackParameterSet vtps = ADMAudioVirtualTrackParameterSet.class.cast(obj);
                    // collect all ADMAudioVirtualTrackParameterSet instances
                    admAudioVirtualTrackParameterSetList.add(vtps);
                    if (!ADM_AUDIO_OPERATIONAL_MODES.contains(vtps.getADMOperationalMode())) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Value %s of Operational Mode not permitted in ADMAudioVirtualTrackParameterSet", vtps.getADMOperationalMode()));
                    }
                }
            }
        }
        for (IMFEssenceComponentVirtualTrack virtualTrack : imfCompositionPlaylist.getEssenceVirtualTracks()) {
            // ST 2067-204, section 5.3.4, check for an ADM Audio Virtual Track Parameter Set for each ADM Audio Virtual Track
            if (virtualTrack.getSequenceType().equals("ADMAudioSequence")) {
                admAudioSignalSequenceTrackIds.add(UUIDHelper.fromUUID(virtualTrack.getTrackID()));
                List<String> resource_id_list = new ArrayList<>();
                for (IMFBaseResourceType resource : virtualTrack.getResourceList()) {
                    // collect all resource IDs for a given Track ID
                    resource_id_list.add(resource.getId());
                }
                admAudioResourceHash.put(virtualTrack.getTrackID(), resource_id_list);

                if (imfCompositionPlaylist.getExtensionProperties() != null) {
                    int trackIdsFound = 0;
                    for (ADMAudioVirtualTrackParameterSet vps : admAudioVirtualTrackParameterSetList) {
                        if (UUIDHelper.fromUUID(virtualTrack.getTrackID()).matches(vps.getTrackId())) trackIdsFound++;
                    }
                    if (trackIdsFound == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("No ADMAudioVirtualTrackParameterSet for ADM Audio Virtual Track %s present", virtualTrack.getTrackID().toString()));
                    } else if (trackIdsFound > 1) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("There are %d ADMAudioVirtualTrackParameterSet for ADM Audio Virtual Track %s present, shall be only 1", trackIdsFound, virtualTrack.getTrackID().toString()));
                    }
                } else {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("No ADMAudioVirtualTrackParameterSet for ADM Audio Virtual Track %s present", virtualTrack.getTrackID().toString()));
                }
            }
        }
        // Check if any ADMAudioVirtualTrackParameterSet items do not correspond to an ADMAudioSequence
        for (ADMAudioVirtualTrackParameterSet vps : admAudioVirtualTrackParameterSetList) {
            if (admAudioSignalSequenceTrackIds.isEmpty()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMAudioVirtualTrackParameterSet for Track ID %s does not correspond to an ADM Audio Virtual Track", vps.getTrackId()));
            } else {
                if (!admAudioSignalSequenceTrackIds.contains(vps.getTrackId())) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMAudioVirtualTrackParameterSet for Track ID %s does not correspond to an ADM Audio Virtual Track", vps.getTrackId()));
                } else {
	                for (ADMSoundfieldGroupSelectorType adm_sg_selector: vps.getADMSoundfieldGroupSelector() ) {
	                    if (!admAudioResourceHash.get(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId())).contains(adm_sg_selector.getResourceId())) {
	                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
	                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMSoundfieldGroupSelectorType for Track ID %s references unknown resource %s", vps.getTrackId(), adm_sg_selector.getResourceId()));
	                    } else {
	                        Optional<IMFEssenceComponentVirtualTrack> optional = imfCompositionPlaylist.getEssenceVirtualTracks().stream().filter(e->e.getTrackID().equals(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId()))).findAny();
	                        if (optional.isPresent()) {
	                            IMFEssenceComponentVirtualTrack virtual_track = optional.get();
	                            Optional<IMFTrackFileResourceType> optional2 = virtual_track.getTrackFileResourceList().stream().filter(e->e.getId().equals(adm_sg_selector.getResourceId())).findAny();
	                            if (optional2.isPresent()) {
	                                DOMNodeObjectModel essence_descriptor_dom_node = imfCompositionPlaylist.getEssenceDescriptor(UUIDHelper.fromUUIDAsURNStringToUUID(optional2.get().getTrackFileId()));
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
	                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
	                                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMSoundfieldGroupSelectorType for Track ID %s references unknown ADMSoundfieldGroupLinkID %s", vps.getTrackId(), link_id));
	                                    }
	                                }
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
