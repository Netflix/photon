package com.netflix.imflibrary.st2067_203;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.header.AudioChannelLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.GroupOfSoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.Sequence;
import com.netflix.imflibrary.st0377.header.SoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0377.header.TimelineTrack;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st379_2.ContainerConstraintsSubDescriptor;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;

import javax.annotation.Nonnull;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.containerConstraintsSubDescriptorUL;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MGASADMTrackFileConstraints {

    private static final String IMF_MGASADM_EXCEPTION_PREFIX = "IMF MGA S-ADM check: ";

    // Prevent instantiation
    public MGASADMTrackFileConstraints() {}

    public static void checkCompliance(IMFConstraints.HeaderPartitionIMF headerPartitionIMF, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        HeaderPartition headerPartition = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        Preface preface = headerPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage;
        filePackage = (SourcePackage) genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();

        for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {
            Sequence sequence = timelineTrack.getSequence();
            if (sequence == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_MGASADM_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                if (genericDescriptor instanceof MGASoundEssenceDescriptor) { // Support for st2067-203


                    //
                    // MGASoundEssenceDescriptor
                    //
                    MGASoundEssenceDescriptor mgaEssenceDescriptor = (MGASoundEssenceDescriptor) genericDescriptor;

                    // ST 2067-203 section 6.3.2
                    if ( (mgaEssenceDescriptor.getSampleRate().get(0) % timelineTrack.getEditRateNumerator() !=0) || timelineTrack.getEditRateDenominator() != mgaEssenceDescriptor.getSampleRate().get(1)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("Timeline Track Edit Rate %d/%d does not match MGASoundEssenceDescriptor Sample Rate %d/%d in the IMFTrackFile represented by ID %s.", timelineTrack.getEditRateNumerator(), timelineTrack.getEditRateDenominator(), mgaEssenceDescriptor.getSampleRate().get(0), mgaEssenceDescriptor.getSampleRate().get(1), packageID.toString()));
                    }

                    // ST 2067-203 Table 1
                    int bitDepth = mgaEssenceDescriptor.getQuantizationBits();
                    if (bitDepth != 24) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s indicates an Audio Bit Depth = %d, only 24 is allowed.", packageID.toString(), mgaEssenceDescriptor.getQuantizationBits()));
                    }

                    // ST 2067-203 section 5.4
                    if (!mgaEssenceDescriptor.getEssenceContainerUL().equals(MGASoundEssenceDescriptor.IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s does not use as Essence Container Label item the IMF Clip-Wrapped MGA Essence Container Label %s but %s.", packageID.toString(), MGASoundEssenceDescriptor.IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL, mgaEssenceDescriptor.getEssenceContainerUL().toString()));
                    }
                    // ST 2127-10, Section 6
                    if (!mgaEssenceDescriptor.getSoundEssenceCoding().equals(MGASoundEssenceDescriptor.MGAAudioEssenceUncompressedSoundCoding)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the Immersive Audio Coding value in its Sound Essence Coding item %s but %s.", packageID.toString(), MGASoundEssenceDescriptor.MGA_AUDIOESSENCE_UNCOMPRESSED_SOUND_ENCODING_LABEL, mgaEssenceDescriptor.getSoundEssenceCoding().toString()));
                    }

                    // ST 2067-203 Table 1
                    if (mgaEssenceDescriptor.getElectroSpatialFormulation() != null && (mgaEssenceDescriptor.getElectroSpatialFormulation() != GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the multi-channel mode default value for the Electro-Spatial Formulation item : %d.", packageID.toString(), mgaEssenceDescriptor.getElectroSpatialFormulation().value()));
                    }

                    // ST 2067-203 Table 1
                    if (mgaEssenceDescriptor.getReferenceAudioAlignmentLevel() == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s is missing the Reference Audio Alignment Level item.", packageID.toString()));
                    }

                    // ST 2067-203 Table 1
                    if (mgaEssenceDescriptor.getReferenceImageEditRate() == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s is missing the Reference Image Edit Rate item.", packageID.toString()));
                    }

                    List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
                    // Section 5.10.2
                    if (subDescriptors.size() == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s does not have subdescriptors", packageID.toString()));
                    } else {
                        List<InterchangeObject.InterchangeObjectBO> audioChannelLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(AudioChannelLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (audioChannelLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                    String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s has %d illegal AudioChannelLabelSubDescriptor(s)", packageID.toString(), audioChannelLabelSubDescriptors.size()));
                        }
                        List<InterchangeObject.InterchangeObjectBO> soundFieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (soundFieldGroupLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                    String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s has %d illegal SoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), soundFieldGroupLabelSubDescriptors.size()));
                        }
                        List<InterchangeObject.InterchangeObjectBO> groupOfSoundFieldGroupsLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(GroupOfSoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (groupOfSoundFieldGroupsLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                    String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s has %d illegal SoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), groupOfSoundFieldGroupsLabelSubDescriptors.size()));
                        }
                        
                        //
                        // ContainerConstraintsSubDescriptor (ST 379-2)
                        //
                        List<InterchangeObject.InterchangeObjectBO> containerConstraintsSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ContainerConstraintsSubDescriptor.class)).collect(Collectors.toList());
                        if (containerConstraintsSubDescriptors.isEmpty()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                                    String.format("Track File with ID %s: A ContainerConstraintsSubDescriptor shall be present per ST 379-2, but is missing", packageID.toString()));
                        } else if (containerConstraintsSubDescriptors.size() != 1) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                                    String.format("Track File with ID %s: One ContainerConstraintsSubDescriptor shall be present per ST 379-2, but %d are present", packageID.toString(), containerConstraintsSubDescriptors.size()));
                        }
                        
                        //
                        // MGASoundEssenceDescriptor
                        //
                        List<InterchangeObject.InterchangeObjectBO> mgaSoundfieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(MGASoundfieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (mgaSoundfieldGroupLabelSubDescriptors.size() == 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                    String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s refers to zero MGASoundfieldGroupLabelSubDescriptors, 1 is required per Soundfield Group", packageID.toString()));
                        } else {
                            // ST 2067-203 section 5.6.2 Table 3
                            MGASoundfieldGroupLabelSubDescriptor.MGASoundfieldGroupLabelSubDescriptorBO mgaSoundfieldGroupLabelSubDescriptorBO = null;

                            for (InterchangeObject.InterchangeObjectBO  sub_descriptor : mgaSoundfieldGroupLabelSubDescriptors) {
                                mgaSoundfieldGroupLabelSubDescriptorBO = MGASoundfieldGroupLabelSubDescriptor.MGASoundfieldGroupLabelSubDescriptorBO.class.cast(sub_descriptor);
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCATagName() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagName", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                } else {
                                    if (!mgaSoundfieldGroupLabelSubDescriptorBO.getMCATagName().equals(MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_NAME)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                                String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s does not have a valid MCATagName: %s", sub_descriptor.getInstanceUID().toString(), packageID.toString(), mgaSoundfieldGroupLabelSubDescriptorBO.getMCATagName()));
                                    }
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagSymbol", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                } else {
                                    if (!mgaSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol().equals(MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_TAG_SYMBOL)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                                String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s does not have a valid MCATagSymbol: %s", sub_descriptor.getInstanceUID().toString(), packageID.toString(), mgaSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol()));
                                    }
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCALabelDictionaryId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                } else {
                                    if (!mgaSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId().equals(MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                                String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s does not have the required MCA Label Dictionary Id %s but %s", sub_descriptor.getInstanceUID().toString(), packageID.toString(), MGASoundfieldGroupLabelSubDescriptor.MGA_MCA_LABEL_DICTIONNARY_ID_UL, mgaSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId().toString()));
                                    }
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage() != null &&
                                        !IMFConstraints.isSpokenLanguageRFC5646Compliant(mgaSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage())) {
                                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackfile represented by ID %s is not RFC5646 compliant", mgaSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage(), sub_descriptor.getInstanceUID().toString(), packageID.toString())));
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCAContent() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAContent", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAUseClass", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCATitle() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitle", sub_descriptor.getInstanceUID().toString(), sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCATitleVersion() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitleVersion", sub_descriptor.getInstanceUID().toString(), sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                // Section C.2
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID() != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has forbidden MCAChannelID %d", sub_descriptor.getInstanceUID().toString(), packageID.toString(), mgaSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID()));
                                }
                                // ST 2067-203 Table 8
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getADMAudioProgrammeId() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing ADMAudioProgrammId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                // ST 2067-203 Table 8
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getADMAudioContentId() != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioContentId item", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                // ST 2067-203 Table 8
                                if (mgaSoundfieldGroupLabelSubDescriptorBO.getADMAudioObjectId() != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGASoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioObjectId item", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                            }
                        }
                        //
                        // MGAAudioMetadataSubDescriptor
                        //
                        int mgaAudioMetadataPayloadULArrraySize = 0;
                        List<InterchangeObject.InterchangeObjectBO> mgaAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(MGAAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
                        if (mgaAudioMetadataSubDescriptors.size() != 1) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                    String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s refers to %d MGAAudioMetadataSubDescriptor, 1 is required per ST 2067-203", packageID.toString(), mgaAudioMetadataSubDescriptors.size()));
                        } else {
                            // ST 2067-203 section 5.6.2 Table 3
                            MGAAudioMetadataSubDescriptor.MGAAudioMetadataSubDescriptorBO mgaAudioMetadataSubDescriptorBO = MGAAudioMetadataSubDescriptor.MGAAudioMetadataSubDescriptorBO.class.cast(mgaAudioMetadataSubDescriptors.get(0));

                            if (mgaAudioMetadataSubDescriptorBO.getMGALinkId().length == 0) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                        String.format("MGAAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing MGALinkId", packageID.toString()));
                            }
                            if (mgaAudioMetadataSubDescriptorBO.getMGAAudioMetadataIndex() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                        String.format("MGAAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing MGAAudioMetadataIndex", packageID.toString()));
                            }
                            if (mgaAudioMetadataSubDescriptorBO.getMGAAudioMetadataIdentifier() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                        String.format("MGAAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing MGAAudioMetadataIdentifier", packageID.toString()));
                            }
                            if (mgaAudioMetadataSubDescriptorBO.getMGAAudioMetadataPayloadULArrray() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                        String.format("MGAAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing MGAAudioMetadataPayloadULArrray", packageID.toString()));
                            } else {
                                mgaAudioMetadataPayloadULArrraySize = mgaAudioMetadataSubDescriptorBO.getMGAAudioMetadataPayloadULArrray().size();
                                if (mgaAudioMetadataPayloadULArrraySize == 0) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("MGAAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s has an empty MGAAudioMetadataPayloadULArrray", packageID.toString()));
                                }
                            }
                        }
                        //
                        // SADMAudioMetadataSubDescriptor
                        //
                        List<InterchangeObject.InterchangeObjectBO> sadmAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
                        if (sadmAudioMetadataSubDescriptors.size() != mgaAudioMetadataPayloadULArrraySize) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                    String.format("MGASoundEssenceDescriptor in the IMFTrackFile represented by ID %s refers to %d SADMAudioMetadataSubDescriptor, %d are required per ST 2127-10 (one for each UL in MGAAudioMetadataPayloadULArray)", packageID.toString(), mgaAudioMetadataSubDescriptors.size(), mgaAudioMetadataPayloadULArrraySize));
                        } else if (mgaAudioMetadataPayloadULArrraySize > 0){
                            SADMAudioMetadataSubDescriptor.SADMAudioMetadataSubDescriptorBO sadmAudioMetadataSubDescriptorBO = null;

                            for (InterchangeObject.InterchangeObjectBO  sub_descriptor : sadmAudioMetadataSubDescriptors) {
                                sadmAudioMetadataSubDescriptorBO = SADMAudioMetadataSubDescriptor.SADMAudioMetadataSubDescriptorBO.class.cast(sub_descriptor);
                                if (sadmAudioMetadataSubDescriptorBO.getSADMMetadataSectionLinkId().length == 0) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                            String.format("SADMAudioMetadataSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing SADMMetadataSectionLinkId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void checkIndexEditRate(IMFConstraints.HeaderPartitionIMF headerPartitionIMF, IndexTableSegment indexTableSegment, IMFErrorLogger imfErrorLogger) {
        HeaderPartition headerPartition = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        Preface preface = headerPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage;
        filePackage = (SourcePackage) genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();

        for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {
            Sequence sequence = timelineTrack.getSequence();
            if (sequence == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_MGASADM_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                if (genericDescriptor instanceof MGASoundEssenceDescriptor) { // Support for st2067-203
                    MGASoundEssenceDescriptor mgaEssenceDescriptor = (MGASoundEssenceDescriptor) genericDescriptor;

                    // Section
                    if (timelineTrack.getEditRateNumerator() != indexTableSegment.getIndexEditRate().getNumerator() || timelineTrack.getEditRateDenominator() != indexTableSegment.getIndexEditRate().getDenominator()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_MGASADM_EXCEPTION_PREFIX +
                                String.format("Timeline Track Edit Rate %d/%d does not match Index Table Segment Index Edit Rate %d/%d in the IMFTrackFile represented by ID %s.", timelineTrack.getEditRateNumerator(), timelineTrack.getEditRateDenominator(), indexTableSegment.getIndexEditRate().getNumerator(), indexTableSegment.getIndexEditRate().getDenominator(), packageID.toString()));
                    }


                }
            }
        }
    }
}
