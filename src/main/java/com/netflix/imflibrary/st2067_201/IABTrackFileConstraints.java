package com.netflix.imflibrary.st2067_201;

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
import com.netflix.imflibrary.utils.ErrorLogger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class IABTrackFileConstraints {

    private static final String IMF_IAB_EXCEPTION_PREFIX = "IMF IAB check: ";

    // Prevent instantiation
    public IABTrackFileConstraints() {}

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
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                if (genericDescriptor instanceof IABEssenceDescriptor) { // Support for st2067-201

                    // Section 5.2
                    CompoundDataTypes.MXFCollections.MXFCollection<UL> conformsToSpecifications = preface.getConformstoSpecificationsULs();
                    if (conformsToSpecifications == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("Preface in the IMFTrackFile represented by ID %s does not have the required conformsToSpecifications item.", packageID.toString()));
                    } else {
                        List specificationsULs = conformsToSpecifications.getEntries();
                        if (specificationsULs.size() == 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                    String.format("Preface in the IMFTrackFile represented by ID %s does not have a single UL in the conformsToSpecifications item.", packageID.toString()));
                        } else if (!specificationsULs.contains(IABEssenceDescriptor.IMF_IAB_TRACK_FILE_LEVEL0_UL)) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                    String.format("Preface in the IMFTrackFile represented by ID %s does not indicate the required IAB Level 0 Plugin UL in the conformsToSpecifications item, but to %s", packageID.toString(), specificationsULs));
                        }
                    }

                    IABEssenceDescriptor iabEssenceDescriptor = (IABEssenceDescriptor) genericDescriptor;

                    // Section 5.4
                    if (timelineTrack.getEditRateNumerator() != iabEssenceDescriptor.getSampleRate().get(0) || timelineTrack.getEditRateDenominator() != iabEssenceDescriptor.getSampleRate().get(1)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("Timeline Track Edit Rate %d/%d does not match IABEssenceDescriptor Sample Rate %d/%d in the IMFTrackFile represented by ID %s.", timelineTrack.getEditRateNumerator(), timelineTrack.getEditRateDenominator(), iabEssenceDescriptor.getSampleRate().get(0), iabEssenceDescriptor.getSampleRate().get(1), packageID.toString()));
                    }

                    // Section 5.6.1
                    int bitDepth = iabEssenceDescriptor.getQuantizationBits();
                    if (bitDepth != 24) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s indicates an Audio Bit Depth = %d, only 24 is allowed.", packageID.toString(), iabEssenceDescriptor.getQuantizationBits()));
                    }

                    // Section 5.9
                    if (!iabEssenceDescriptor.getEssenceContainerUL().equals(IABEssenceDescriptor.IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not use as Essence Container Label item the IMF Clip-Wrapped IAB Essence Container Label %s but %s.", packageID.toString(), IABEssenceDescriptor.IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL, iabEssenceDescriptor.getEssenceContainerUL().toString()));
                    }

                    if (iabEssenceDescriptor.getCodec() != null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s indicates a non-null codec: %s.", packageID.toString(), iabEssenceDescriptor.getCodec().toString()));
                    }

                    if (!iabEssenceDescriptor.getSoundEssenceCoding().equals(IABEssenceDescriptor.IMMERSIVE_AUDIO_CODING_LABEL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the Immersive Audio Coding value in its Sound Essence Coding item %s but %s.", packageID.toString(), IABEssenceDescriptor.IMMERSIVE_AUDIO_CODING_LABEL, iabEssenceDescriptor.getSoundEssenceCoding().toString()));
                    }

                    if (iabEssenceDescriptor.getElectroSpatialFormulation() != null && iabEssenceDescriptor.getElectroSpatialFormulation() != GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the multi-channel mode default value for the Electro-Spatial Formulation item : %d.", packageID.toString(), iabEssenceDescriptor.getElectroSpatialFormulation().value()));
                    }

                    if (iabEssenceDescriptor.getChannelCount() != 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the Distinguished Value of zero for the ChannelCount item : %d.", packageID.toString(), iabEssenceDescriptor.getChannelCount()));
                    }

                    if (iabEssenceDescriptor.getReferenceImageEditRate() == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s is missing the Reference Image Edit Rate item.", packageID.toString()));
                    }

                    if (iabEssenceDescriptor.getReferenceAudioAlignmentLevel() == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s is missing the Reference Audio Alignment Level item.", packageID.toString()));
                    }

                    List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
                    // Section 5.10.2
                    if (subDescriptors.size() == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not have subdescriptors", packageID.toString()));
                    } else {
                        List<InterchangeObject.InterchangeObjectBO> soundFieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (soundFieldGroupLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                    String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s has %d illegal SoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), soundFieldGroupLabelSubDescriptors.size()));
                        }

                        List<InterchangeObject.InterchangeObjectBO> audioChannelLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(AudioChannelLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (audioChannelLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                    String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s has %d illegal AudioChannelLabelSubDescriptor(s)", packageID.toString(), audioChannelLabelSubDescriptors.size()));
                        }

                        List<InterchangeObject.InterchangeObjectBO> groupOfSoundfieldGroupsLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(GroupOfSoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (groupOfSoundfieldGroupsLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                    String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s has %d illegal GroupOfSoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), groupOfSoundfieldGroupsLabelSubDescriptors.size()));
                        }

                        List<InterchangeObject.InterchangeObjectBO> iabSoundFieldLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(IABSoundfieldLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (iabSoundFieldLabelSubDescriptors.size() != 1) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                    String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s refers to %d IABSoundfieldLabelSubDescriptor, exactly 1 is required", packageID.toString(), iabSoundFieldLabelSubDescriptors.size()));
                        } else {
                            // Section 5.10.3
                            IABSoundfieldLabelSubDescriptor.IABSoundfieldLabelSubDescriptorBO iabSoundFieldLabelSubDescriptorBO = IABSoundfieldLabelSubDescriptor.IABSoundfieldLabelSubDescriptorBO.class.cast(iabSoundFieldLabelSubDescriptors.get(0));

                            if (iabSoundFieldLabelSubDescriptorBO.getMCATagName() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCATagName", packageID.toString()));
                            } else {
                                // Section 5.10.4
                                if (!iabSoundFieldLabelSubDescriptorBO.getMCATagName().equals(IABSoundfieldLabelSubDescriptor.IAB_MCA_TAG_NAME)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s does not have a valid MCATagName: %s", packageID.toString(), iabSoundFieldLabelSubDescriptorBO.getMCATagName()));
                                }
                            }

                            if (iabSoundFieldLabelSubDescriptorBO.getMCATagSymbol() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCATagSymbol", packageID.toString()));
                            } else {
                                // Section 5.10.4
                                if (!iabSoundFieldLabelSubDescriptorBO.getMCATagSymbol().equals(IABSoundfieldLabelSubDescriptor.IAB_MCA_TAG_SYMBOL)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s does not have a valid MCATagSymbol: %s", packageID.toString(), iabSoundFieldLabelSubDescriptorBO.getMCATagSymbol()));
                                }
                            }

                            if (iabSoundFieldLabelSubDescriptorBO.getMCALabelDictionnaryId() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCALabelDictionaryId", packageID.toString()));
                            } else {
                                // Section 5.10.4
                                if (!iabSoundFieldLabelSubDescriptorBO.getMCALabelDictionnaryId().equals(IABSoundfieldLabelSubDescriptor.IAB_MCA_LABEL_DICTIONNARY_ID_UL)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s does not have the required MCA Label Dictionary Id %s but %s", packageID.toString(), IABSoundfieldLabelSubDescriptor.IAB_MCA_LABEL_DICTIONNARY_ID_UL, iabSoundFieldLabelSubDescriptorBO.getMCALabelDictionnaryId().toString()));
                                }
                            }

                            if (iabSoundFieldLabelSubDescriptorBO.getRFC5646SpokenLanguage() != null &&
                                    !IMFConstraints.isSpokenLanguageRFC5646Compliant(iabSoundFieldLabelSubDescriptorBO.getRFC5646SpokenLanguage())) {
                                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in IABSoundfieldLabelSubDescriptor in the IMFTrackfile represented by ID %s is not RFC5646 compliant", iabSoundFieldLabelSubDescriptorBO.getRFC5646SpokenLanguage(), packageID.toString())));
                            }

                            if (iabSoundFieldLabelSubDescriptorBO.getMCAAudioContentKind() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCAAudioContentKind", packageID.toString()));
                            }
                            if (iabSoundFieldLabelSubDescriptorBO.getMCAAudioElementKind() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCAAudioElementKind", packageID.toString()));
                            }
                            if (iabSoundFieldLabelSubDescriptorBO.getMCATitle() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCATitle", packageID.toString()));
                            }
                            if (iabSoundFieldLabelSubDescriptorBO.getMCATitleVersion() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCATitleVersion", packageID.toString()));
                            }

                            // Section C.2
                            if (iabSoundFieldLabelSubDescriptorBO.getMCAChannelID() != null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has forbidden MCAChannelID %d", packageID.toString(), iabSoundFieldLabelSubDescriptorBO.getMCAChannelID()));
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
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                if (genericDescriptor instanceof IABEssenceDescriptor) { // Support for st2067-201
                    IABEssenceDescriptor iabEssenceDescriptor = (IABEssenceDescriptor) genericDescriptor;

                    // Section 5.7
                    if (timelineTrack.getEditRateNumerator() != indexTableSegment.getIndexEditRate().getNumerator() || timelineTrack.getEditRateDenominator() != indexTableSegment.getIndexEditRate().getDenominator()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("Timeline Track Edit Rate %d/%d does not match Index Table Segment Index Edit Rate %d/%d in the IMFTrackFile represented by ID %s.", timelineTrack.getEditRateNumerator(), timelineTrack.getEditRateDenominator(), indexTableSegment.getIndexEditRate().getNumerator(), indexTableSegment.getIndexEditRate().getDenominator(), packageID.toString()));
                    }


                }
            }
        }
    }
}
