/*
 *
 * Copyright 2024 RheinMain University of Applied Sciences, Wiesbaden, Germany.
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

package com.netflix.imflibrary.st2067_203;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377_41.*;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
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

import org.smpte_ra.ns._2067_203._2022.MGASADMSoundfieldGroupSelectorType;
import org.smpte_ra.ns._2067_203._2022.MGASADMVirtualTrackParameterSet;

import static com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE;
import static com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor.MGA_AUDIO_ESSENCE_UNCOMPRESSED_SOUND_CODING;
import static com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor.MXF_GC_CLIP_WRAPPED_MGA;

public class IMFMGASADMConstraintsChecker {
    private static final Integer MGASADM_BIT_DEPTH = 24;

    // SMPTE ST 2067-203 Operational Modes
    public static final String MGA_SADM_OPERATIONAL_MODE_A = "http://www.smpte-ra.org/ns/2067-203/2022#Operational-Mode-A";
    static final List<String> MGA_SADM_OPERATIONAL_MODES = Collections.unmodifiableList(Arrays.asList(
            MGA_SADM_OPERATIONAL_MODE_A));

    public static List<ErrorLogger.ErrorObject> checkMGASADMVirtualTrack(Composition.EditRate compositionEditRate,
                                                                     Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap,
                                                                     Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap,
                                                                     Set<String> homogeneitySelectionSet) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Iterator iterator = virtualTrackMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (!virtualTrack.getSequenceType().equals("MGASADMSignalSequence")) continue;

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            for(IMFBaseResourceType baseResource : virtualTrackResourceList) {
                IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(baseResource);
                DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
                if (!domNodeObjectModel.getLocalName().equals("MGASoundEssenceDescriptor")) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource does not have an MGASoundEssenceDescriptor but %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getLocalName()));
                }

                if (((imfTrackFileResourceType.getEditRate().getNumerator()*compositionEditRate.getDenominator()) % (imfTrackFileResourceType.getEditRate().getDenominator()*compositionEditRate.getNumerator()) != 0)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The EditRate %s/%s of resource %s is not a multiple of the EditRate of the Main Image Virtual Track %s/%s",
                            imfTrackFileResourceType.getEditRate().getNumerator(), imfTrackFileResourceType.getEditRate().getDenominator(), imfTrackFileResourceType.getId(), compositionEditRate.getNumerator(), compositionEditRate.getDenominator()));
                }

                if (!domNodeObjectModel.getFieldAsUL("ContainerFormat").equalsWithMask(MXF_GC_CLIP_WRAPPED_MGA, 0b1111111011111111)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource does not use the correct Essence Container UL: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("ContainerFormat"), MXF_GC_CLIP_WRAPPED_MGA));
                }

                if (!domNodeObjectModel.getFieldAsUL("SoundCompression").equalsWithMask(MGA_AUDIO_ESSENCE_UNCOMPRESSED_SOUND_CODING, 0b1111111011111111)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource does not use the correct Sound Compression UL: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("SoundCompression"), MGA_AUDIO_ESSENCE_UNCOMPRESSED_SOUND_CODING));
                }

                if (domNodeObjectModel.getFieldAsInteger("QuantizationBits") != MGASADM_BIT_DEPTH.intValue()) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource has invalid QuantizationBits field: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsInteger("QuantizationBits"), MGASADM_BIT_DEPTH));
                }

                if (domNodeObjectModel.getFieldAsString("AudioSampleRate") == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource does not have an AudioSampleRate item.", imfTrackFileResourceType.getSourceEncoding()));
                }

                if (domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation") != null && domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation").intValue() != MULTI_CHANNEL_MODE.value()) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                            "an MGA S-ADM VirtualTrack Resource has an invalid value of ElectrospatialFormulation: %d vs. %d.", imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsInteger("ElectrospatialFormulation"), MULTI_CHANNEL_MODE.value()));
                }

                if (domNodeObjectModel.getChildrenDOMNodes().size() == 0) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource has no SubDescriptor", imfTrackFileResourceType.getSourceEncoding()));
                } else {
                    for (Map.Entry<DOMNodeObjectModel, Integer> entry : domNodeObjectModel.getChildrenDOMNodes().entrySet()) {
                        if (!entry.getKey().getLocalName().equals("SubDescriptors")) continue;

                        for (Map.Entry<DOMNodeObjectModel, Integer> subentry : entry.getKey().getChildrenDOMNodes().entrySet()) {

                            if (subentry.getKey().getLocalName().equals("AudioChannelLabelSubDescriptor") || subentry.getKey().getLocalName().equals("SoundFieldGroupLabelSubDescriptor") || subentry.getKey().getLocalName().equals("GroupOfSoundfieldGroupsLabelSubDescriptor")) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource has forbidden %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getLocalName()));
                            } else if (subentry.getKey().getLocalName().equals("MGASoundfieldGroupLabelSubDescriptor")) {
                                if (subentry.getKey().getFieldAsUL("MCALabelDictionaryID") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCA Label Dictionary ID", imfTrackFileResourceType.getSourceEncoding()));

                                } else if (!MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL.equalsWithMask(subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), 0b1111111011111111)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource does not have the  MCA Label Dictionary ID for MGA (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL));
                                }
                                if (subentry.getKey().getFieldAsString("MCATagSymbol") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCATagSymbol", imfTrackFileResourceType.getSourceEncoding()));

                                } else if (!MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_SYMBOL.equals(subentry.getKey().getFieldAsString("MCATagSymbol"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource does not have the MCA Tag Symbol for MGA (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagSymbol"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_SYMBOL));
                                }
                                if (subentry.getKey().getFieldAsString("MCATagName") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCATagName", imfTrackFileResourceType.getSourceEncoding()));

                                } else if (!MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_NAME.equals(subentry.getKey().getFieldAsString("MCATagName"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource does not have the MCA Tag Name for MGA (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagName"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_NAME));
                                }
                                if (subentry.getKey().getFieldAsString("MCAContent") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCAContent", imfTrackFileResourceType.getSourceEncoding()));
                                } else if (MCAContent.getValueFromSymbol(subentry.getKey().getFieldAsString("MCAContent")) == MCAContent.Unknown) {
                                    // Check against ST 377-41:2021 Table 2
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource has invalid MCA Content (%s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAContent")));
                                }
                                if (subentry.getKey().getFieldAsString("MCAUseClass") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCAUseClass", imfTrackFileResourceType.getSourceEncoding()));
                                } else if (MCAUseClass.getValueFromSymbol(subentry.getKey().getFieldAsString("MCAUseClass")) == MCAUseClass.Unknown) {
                                    // Check against ST 377-41:2021 Table 3
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource has invalid MCA Use Class (%s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAUseClass")));
                                }
                                // Check against ST 2067-203:2023 Table 3
                                if (subentry.getKey().getFieldAsString("MCATitle") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCATitle", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                // Check against ST 2067-203:2023 Table 3
                                if (subentry.getKey().getFieldAsString("MCATitleVersion") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MCATitleVersion", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                if (subentry.getKey().getFieldAsString("MCAChannelID") != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource has forbidden MCAChannelID %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAChannelID")));
                                }
                            } else if (subentry.getKey().getLocalName().equals("MGAAudioMetadataSubDescriptor")) {
                                if (subentry.getKey().getFieldAsString("MGALinkID") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MGALinkID", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                if (subentry.getKey().getFieldAsInteger("MGAAudioMetadataIndex") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MGAAudioMetadataIndex", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                if (subentry.getKey().getFieldAsInteger("MGAAudioMetadataIdentifier") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MGAAudioMetadataIdentifier", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                if (subentry.getKey().getFieldsAsUL("MGAAudioMetadataPayloadULArray") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing MGAAudioMetadataPayloadULArray", imfTrackFileResourceType.getSourceEncoding()));
                                }
                            } else if (subentry.getKey().getLocalName().equals("SADMAudioMetadataSubDescriptor")) {
                                if (subentry.getKey().getFieldAsString("SADMMetadataSectionLinkID") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing SADMMetadataSectionLinkID", imfTrackFileResourceType.getSourceEncoding()));
                                }
                                if (subentry.getKey().getFieldsAsUL("SADMProfileLevelULBatch") == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource is missing SADMProfileLevelULBatch", imfTrackFileResourceType.getSourceEncoding()));
                                }
                            }
                        }

                    }
                }
            }
        }
        return imfErrorLogger.getErrors();
    }
    public static List<ErrorLogger.ErrorObject> checkMGASADMVirtualTrackParameterSet(IMFCompositionPlaylist imfCompositionPlaylist) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<MGASADMVirtualTrackParameterSet> mgaSADMVirtualTrackParameterSetList = new ArrayList<>();
        List<String> mgaSADMSignalSequenceTrackIds = new ArrayList<>();
        Map<UUID , List<String>> mgaSadmResourceHash = new LinkedHashMap<>();
        Set<Object> virtualTrackParameterSet = Collections.emptySet();
        if (imfCompositionPlaylist.getExtensionProperties() != null) {
            virtualTrackParameterSet = imfCompositionPlaylist.getExtensionProperties().getAny().stream().collect(Collectors.toSet());
            Iterator<Object> iterator = virtualTrackParameterSet.iterator();
            while (iterator != null && iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj.getClass() == MGASADMVirtualTrackParameterSet.class) {
                    MGASADMVirtualTrackParameterSet vtps = MGASADMVirtualTrackParameterSet.class.cast(obj);
                    // collect all MGASADMVirtualTrackParameterSet instances
                    mgaSADMVirtualTrackParameterSetList.add(vtps);
                    if (!MGA_SADM_OPERATIONAL_MODES.contains(vtps.getMGASADMOperationalMode())) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Value %s of Operational Mode not permitted in MGASADMVirtualTrackParameterSet", vtps.getMGASADMOperationalMode()));
                    }
                }
            }
        }
        for (IMFEssenceComponentVirtualTrack virtualTrack : imfCompositionPlaylist.getEssenceVirtualTracks()) {
            // ST 2067-203, section 6.3.4, check for a MGA S-ADM Virtual Track Parameter Set for each MGA S-ADM Virtual Track
            if (virtualTrack.getSequenceType() == "MGASADMSignalSequence") {
                mgaSADMSignalSequenceTrackIds.add(UUIDHelper.fromUUID(virtualTrack.getTrackID()));
                List<String> resource_id_list = new ArrayList<>();
                for (IMFBaseResourceType resource : virtualTrack.getResourceList()) {
                    // collect all resource IDs for a given Track ID
                    resource_id_list.add(resource.getId());
                }
                mgaSadmResourceHash.put(virtualTrack.getTrackID(), resource_id_list);

                if (imfCompositionPlaylist.getExtensionProperties() != null) {
                    int trackIdsFound = 0;
                    for (MGASADMVirtualTrackParameterSet vps : mgaSADMVirtualTrackParameterSetList) {
                        if (UUIDHelper.fromUUID(virtualTrack.getTrackID()).matches(vps.getTrackId())) trackIdsFound++;
                    }
                    if (trackIdsFound == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("No MGASADMVirtualTrackParameterSet for MGA S-ADM Virtual Track %s present", virtualTrack.getTrackID().toString()));
                    } else if (trackIdsFound > 1) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("There are %d MGASADMVirtualTrackParameterSet for MGA S-ADM Virtual Track %s present, shall be only 1", trackIdsFound, virtualTrack.getTrackID().toString()));
                    }
                } else {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("No MGASADMVirtualTrackParameterSet for MGA S-ADM Virtual Track %s present", virtualTrack.getTrackID().toString()));
                }
            }
        }
        // Check if any MGASADMVirtualTrackParameterSet items do not correspond to a MGASADMSignalSequence
        for (MGASADMVirtualTrackParameterSet vps : mgaSADMVirtualTrackParameterSetList) {
            if (mgaSADMSignalSequenceTrackIds.isEmpty()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("MGASADMVirtualTrackParameterSet for Track ID %s does not correspond to an MGA S-ADM Virtual Track", vps.getTrackId()));
            } else {
                if (!mgaSADMSignalSequenceTrackIds.contains(vps.getTrackId())) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("MGASADMVirtualTrackParameterSet for Track ID %s does not correspond to an MGA S-ADM Virtual Track", vps.getTrackId()));
                } else {
                    for (MGASADMSoundfieldGroupSelectorType mga_sg_selector: vps.getMGASADMSoundfieldGroupSelector()) {
                        if (!mgaSadmResourceHash.get(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId())).contains(mga_sg_selector.getResourceId())) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("MGASADMSoundfieldGroupSelector for Track ID %s references an unknown resource %s", vps.getTrackId(), mga_sg_selector.getResourceId()));
                        } else {
                            Optional<IMFEssenceComponentVirtualTrack> optional = imfCompositionPlaylist.getEssenceVirtualTracks().stream().filter(e->e.getTrackID().equals(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId()))).findAny();
                            if (optional.isPresent()) {
                                IMFEssenceComponentVirtualTrack virtual_track = optional.get();
                                Optional<IMFTrackFileResourceType> optional2 = virtual_track.getTrackFileResourceList().stream().filter(e->e.getId().equals(mga_sg_selector.getResourceId())).findAny();
                                if (optional2.isPresent()) {
                                    DOMNodeObjectModel essence_descriptor_dom_node = imfCompositionPlaylist.getEssenceDescriptor(UUIDHelper.fromUUIDAsURNStringToUUID(optional2.get().getTrackFileId()));
                                    List<UUID> mca_sg_link_id_list = new ArrayList<>();
                                    for (Map.Entry<DOMNodeObjectModel, Integer> entry : essence_descriptor_dom_node.getChildrenDOMNodes().entrySet()) {
                                        if (!entry.getKey().getLocalName().equals("SubDescriptors")) continue;

                                        for (Map.Entry<DOMNodeObjectModel, Integer> subentry : entry.getKey().getChildrenDOMNodes().entrySet()) {
                                            if (subentry.getKey().getLocalName().equals("MGASoundfieldGroupLabelSubDescriptor")) {
                                                mca_sg_link_id_list.addAll(subentry.getKey().getFieldsAsUUID("MCALinkID"));
                                            }
                                        }
                                    }
                                    for (String link_id : mga_sg_selector.getMGASoundfieldGroupLinkID()) {
                                        if (!mca_sg_link_id_list.contains(UUIDHelper.fromUUIDAsURNStringToUUID(link_id))) {
                                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("MGASADMSoundfieldGroupSelector for Track ID %s references unknown MGASoundfieldGroupLinkID %s", vps.getTrackId(), link_id));
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
