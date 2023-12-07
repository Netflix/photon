package com.netflix.imflibrary.st2067_203;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.AbstractApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_2.Composition.SequenceTypeEnum;
import com.netflix.imflibrary.st2067_2.Composition.VirtualTrack;
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

import org.smpte_ra.ns._2067_203._2022.MGASADMSoundfieldGroupSelectorType;
import org.smpte_ra.ns._2067_203._2022.MGASADMVirtualTrackParameterSet;

import static com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE;
import static com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor.IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL;
import static com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor.MGA_AUDIOESSENCE_UNCOMPRESSED_SOUND_ENCODING_LABEL;

public class IMFMGASADMConstraintsChecker {
    private static final Integer MGASADM_BIT_DEPTH = 24;

    // SMPTE ST 2067-203 Operational Modes
    public static final String MGA_SADM_OPERATIONAL_MODE_A = "http://www.smpte-ra.org/ns/2067-203/2022#Operational-Mode-A";
    static final List<String> MGA_SADM_OPERATIONAL_MODES = Collections.unmodifiableList(Arrays.asList(
            MGA_SADM_OPERATIONAL_MODE_A));

    public static List<ErrorLogger.ErrorObject> checkMGASADMVirtualTrack(Composition.EditRate compositionEditRate,
                                                                     Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap,
                                                                     Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap,
                                                                     RegXMLLibDictionary regXMLLibDictionary,
                                                                     Set<String> homogeneitySelectionSet) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Iterator iterator = virtualTrackMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (!virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MGASADMSignalSequence)) continue;

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

                if (!domNodeObjectModel.getFieldAsUL("ContainerFormat").equals(IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource does not use the correct Essence Container UL: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("ContainerFormat"), IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL));
                }

                if (!domNodeObjectModel.getFieldAsUL("SoundCompression").equals(MGA_AUDIOESSENCE_UNCOMPRESSED_SOUND_ENCODING_LABEL)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an MGA S-ADM VirtualTrack Resource does not use the correct Sound Compression UL: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("SoundCompression"), MGA_AUDIOESSENCE_UNCOMPRESSED_SOUND_ENCODING_LABEL));
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
                                if (!MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL.equals(subentry.getKey().getFieldAsUL("MCALabelDictionaryID"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource has invalid MCA Label Dictionary ID (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsUL("MCALabelDictionaryID"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL));
                                }
                                if (!MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_SYMBOL.equals(subentry.getKey().getFieldAsString("MCATagSymbol"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource misses MCA Tag Symbol (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagSymbol"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_SYMBOL));
                                }
                                if (!MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_NAME.equals(subentry.getKey().getFieldAsString("MCATagName"))) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource misses MCA Tag Name (%s vs. %s)", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCATagName"), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_NAME));
                                }
                                if (subentry.getKey().getFieldAsString("MCAChannelID") != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("EssenceDescriptor ID %s referenced by " +
                                            "an MGA S-ADM VirtualTrack Resource has forbidden MCAChannelID %s", imfTrackFileResourceType.getSourceEncoding(), subentry.getKey().getFieldAsString("MCAChannelID")));
                                }
                            }
                        }

                    }
                }
            }
        }
        return imfErrorLogger.getErrors();
    }
    public static List<ErrorLogger.ErrorObject> checkMGASADMVirtualTrackParameterSet(ApplicationComposition applicationComposition,
            IMFErrorLogger compositionErrorLogger) {

        List<MGASADMVirtualTrackParameterSet> mgaSADMVirtualTrackParameterSetList = new ArrayList<>();
        List<String> mgaSADMSignalSequenceTrackIds = new ArrayList<>();
        Map<UUID , List<String>> mgaSadmResourceHash = new LinkedHashMap<>();
        Set<Object> virtualTrackParameterSet = Collections.emptySet();
        if (applicationComposition.getExtensionProperties() != null) {
            virtualTrackParameterSet = applicationComposition.getExtensionProperties().getAny().stream().collect(Collectors.toSet());
            Iterator<Object> iterator = virtualTrackParameterSet.iterator();
            while (iterator != null && iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj.getClass() == MGASADMVirtualTrackParameterSet.class) {
                    MGASADMVirtualTrackParameterSet vtps = MGASADMVirtualTrackParameterSet.class.cast(obj);
                    // collect all MGASADMVirtualTrackParameterSet instances
                    mgaSADMVirtualTrackParameterSetList.add(vtps);
                    if (!MGA_SADM_OPERATIONAL_MODES.contains(vtps.getMGASADMOperationalMode())) {
                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Value %s of Operational Mode not permitted in MGASADMVirtualTrackParameterSet", vtps.getMGASADMOperationalMode()));
                    }
                }
            }
        }
        for (IMFEssenceComponentVirtualTrack virtualTrack : applicationComposition.getEssenceVirtualTracks()) {
            // ST 2067-203, section 6.3.4, check for a MGA S-ADM Virtual Track Parameter Set for each MGA S-ADM Virtual Track
            if (virtualTrack.getSequenceTypeEnum() == SequenceTypeEnum.MGASADMSignalSequence) {
                mgaSADMSignalSequenceTrackIds.add(UUIDHelper.fromUUID(virtualTrack.getTrackID()));
                List<String> resource_id_list = new ArrayList<>();
                for (IMFBaseResourceType resource : virtualTrack.getResourceList()) {
                    // collect all resource IDs for a given Track ID
                    resource_id_list.add(resource.getId());
                }
                mgaSadmResourceHash.put(virtualTrack.getTrackID(), resource_id_list);

                if (applicationComposition.getExtensionProperties() != null) {
                    int trackIdsFound = 0;
                    for (MGASADMVirtualTrackParameterSet vps : mgaSADMVirtualTrackParameterSetList) {
                        if (UUIDHelper.fromUUID(virtualTrack.getTrackID()).matches(vps.getTrackId())) trackIdsFound++;
                    }
                    if (trackIdsFound == 0) {
                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("No MGASADMVirtualTrackParameterSet for MGA S-ADM Virtual Track %s present", virtualTrack.getTrackID().toString()));
                    } else if (trackIdsFound > 1) {
                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("There are %d MGASADMVirtualTrackParameterSet for MGA S-ADM Virtual Track %s present, shall be only 1", trackIdsFound, virtualTrack.getTrackID().toString()));
                    }
                } else {
                    compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("No MGASADMVirtualTrackParameterSet for MGA S-ADM Virtual Track %s present", virtualTrack.getTrackID().toString()));
                }
            }
        }
        // Check if any MGASADMVirtualTrackParameterSet items do not correspond to a MGASADMSignalSequence
        for (MGASADMVirtualTrackParameterSet vps : mgaSADMVirtualTrackParameterSetList) {
            if (mgaSADMSignalSequenceTrackIds.isEmpty()) {
                compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("MGASADMVirtualTrackParameterSet for Track ID %s does not correspond to an MGA S-ADM Virtual Track", vps.getTrackId()));
            } else {
                if (!mgaSADMSignalSequenceTrackIds.contains(vps.getTrackId())) {
                    compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("MGASADMVirtualTrackParameterSet for Track ID %s does not correspond to an MGA S-ADM Virtual Track", vps.getTrackId()));
                } else {
	                for (MGASADMSoundfieldGroupSelectorType mga_sg_selector: vps.getMGASADMSoundfieldGroupSelector()) {
	                    if (!mgaSadmResourceHash.get(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId())).contains(mga_sg_selector.getResourceId())) {
	                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
	                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("MGASADMSoundfieldGroupSelector for Track ID %s references an unknown resource %s", vps.getTrackId(), mga_sg_selector.getResourceId()));
	                    } else {
	                        Optional<IMFEssenceComponentVirtualTrack> optional = applicationComposition.getEssenceVirtualTracks().stream().filter(e->e.getTrackID().equals(UUIDHelper.fromUUIDAsURNStringToUUID(vps.getTrackId()))).findAny();
	                        if (optional.isPresent()) {
	                            IMFEssenceComponentVirtualTrack virtual_track = optional.get();
	                            Optional<IMFTrackFileResourceType> optional2 = virtual_track.getTrackFileResourceList().stream().filter(e->e.getId().equals(mga_sg_selector.getResourceId())).findAny();
	                            if (optional2.isPresent()) {
	                                DOMNodeObjectModel essence_descriptor_dom_node = applicationComposition.getEssenceDescriptor(UUIDHelper.fromUUIDAsURNStringToUUID(optional2.get().getTrackFileId()));
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
	                                        compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
	                                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("MGASADMSoundfieldGroupSelector for Track ID %s references unknown MGASoundfieldGroupLinkID %s", vps.getTrackId(), link_id));
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
