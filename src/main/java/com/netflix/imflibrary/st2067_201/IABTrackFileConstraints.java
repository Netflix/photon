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

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class IABTrackFileConstraints {

    private static final String IMF_IAB_EXCEPTION_PREFIX = "IMF IAB check: ";

    /** Valid MCA Use Class symbols - SMPTE ST 377-41:2023, Table 3. */
    static final Set<String> MCA_USE_CLASS_VALUES = Set.of("FCMP", "ICMP", "SMPL", "SING");

    /**
     * Valid MCA Content symbols (keys) and, for each, the MCA Use Class symbols permitted in combination with it -
     * SMPTE ST 377-41:2023, Table 2 (vocabulary) and Table 4 (allowed combinations). Custom content ("x-" prefix,
     * Table 2) is not listed; Table 4 defines no combination for it.
     */
    static final Map<String, Set<String>> MCA_CONTENT_TO_USE_CLASS = Map.ofEntries(
            Map.entry("PRM", Set.of("FCMP", "SMPL")),
            Map.entry("SAP", Set.of("FCMP", "SMPL")),
            Map.entry("HI", Set.of("FCMP")),
            Map.entry("DV", Set.of("FCMP")),
            Map.entry("CM", Set.of("FCMP", "SING")),
            Map.entry("DX", Set.of("ICMP", "SMPL")),
            Map.entry("MX", Set.of("ICMP", "SMPL")),
            Map.entry("FX", Set.of("ICMP", "SMPL")),
            Map.entry("FFX", Set.of("ICMP", "SMPL")),
            Map.entry("ME", Set.of("ICMP", "SMPL")),
            Map.entry("OP", Set.of("ICMP", "SMPL")),
            Map.entry("MESP", Set.of("ICMP", "SMPL")),
            Map.entry("DME", Set.of("ICMP", "SMPL")),
            Map.entry("NDME", Set.of("ICMP", "SMPL")),
            Map.entry("PNAR", Set.of("SING")),
            Map.entry("ONAR", Set.of("SING")),
            Map.entry("LCM", Set.of("SING")),
            Map.entry("VO", Set.of("SING")),
            Map.entry("VI", Set.of("SING")),
            Map.entry("MOS", Set.of("FCMP", "SMPL")),
            Map.entry("ADR", Set.of("ICMP")),
            Map.entry("GRP", Set.of("ICMP")),
            Map.entry("CRD", Set.of("ICMP")),
            Map.entry("WLA", Set.of("ICMP")),
            Map.entry("VOC", Set.of("ICMP", "SMPL")),
            Map.entry("FOL", Set.of("ICMP")),
            Map.entry("BG", Set.of("ICMP")));

    // Prevent instantiation
    public IABTrackFileConstraints() {}

    /**
     * Indicates whether the supplied MCA Content value is valid per SMPTE ST 377-41:2023, Table 2 - i.e. one of the
     * defined symbols or a custom value whose symbol begins with "x-".
     * @param mcaContent the MCA Content value to test
     * @return true if the value is a valid MCA Content symbol
     */
    static boolean isValidMCAContent(String mcaContent) {
        return MCA_CONTENT_TO_USE_CLASS.containsKey(mcaContent) || mcaContent.startsWith("x-");
    }

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
                        } else if (!UL.containsIgnoreVersion(specificationsULs, IABEssenceDescriptor.IMF_IAB_TRACK_FILE_LEVEL0_UL)) {
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
                    if (!iabEssenceDescriptor.getEssenceContainerUL().equalsIgnoreVersion(IABEssenceDescriptor.IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not use as Essence Container Label item the IMF Clip-Wrapped IAB Essence Container Label %s but %s.", packageID.toString(), IABEssenceDescriptor.IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL, iabEssenceDescriptor.getEssenceContainerUL().toString()));
                    }

                    if (iabEssenceDescriptor.getCodec() != null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s indicates a non-null codec: %s.", packageID.toString(), iabEssenceDescriptor.getCodec().toString()));
                    }

                    if (!iabEssenceDescriptor.getSoundEssenceCoding().equalsIgnoreVersion(IABEssenceDescriptor.IMMERSIVE_AUDIO_CODING_LABEL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the Immersive Audio Coding value in its Sound Essence Coding item %s but %s.", packageID.toString(), IABEssenceDescriptor.IMMERSIVE_AUDIO_CODING_LABEL, iabEssenceDescriptor.getSoundEssenceCoding().toString()));
                    }

                    if (iabEssenceDescriptor.getElectroSpatialFormulation() != null && iabEssenceDescriptor.getElectroSpatialFormulation() != GenericSoundEssenceDescriptor.ElectroSpatialFormulation.MULTI_CHANNEL_MODE) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                String.format("IABEssenceDescriptor in the IMFTrackFile represented by ID %s does not indicate the multi-channel mode default value for the Electro-Spatial Formulation item : %d.", packageID.toString(), iabEssenceDescriptor.getElectroSpatialFormulation().value()));
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
                                if (!iabSoundFieldLabelSubDescriptorBO.getMCALabelDictionnaryId().equalsIgnoreVersion(IABSoundfieldLabelSubDescriptor.IAB_MCA_LABEL_DICTIONNARY_ID_UL)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s does not have the required MCA Label Dictionary Id %s but %s", packageID.toString(), IABSoundfieldLabelSubDescriptor.IAB_MCA_LABEL_DICTIONNARY_ID_UL, iabSoundFieldLabelSubDescriptorBO.getMCALabelDictionnaryId().toString()));
                                }
                            }

                            if (iabSoundFieldLabelSubDescriptorBO.getRFC5646SpokenLanguage() != null &&
                                    !IMFConstraints.isSpokenLanguageRFC5646Compliant(iabSoundFieldLabelSubDescriptorBO.getRFC5646SpokenLanguage())) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in IABSoundfieldLabelSubDescriptor in the IMFTrackfile represented by ID %s is not RFC5646 compliant", iabSoundFieldLabelSubDescriptorBO.getRFC5646SpokenLanguage(), packageID.toString()));
                            }

                            // Section 5.10.3 (ST 2067-201:2026): MCA Content and MCA Use Class should be present.
                            // They supersede MCA Audio Content Kind and MCA Audio Element Kind, whose use is no longer
                            // recommended (NOTE 3 of 5.10.3); accordingly, absence of the legacy items is no longer flagged.
                            // When present, both items shall be set according to SMPTE ST 377-41:2023 (Subclauses 5.4 and 5.5).
                            String mcaContent = iabSoundFieldLabelSubDescriptorBO.getMCAContent();
                            String mcaUseClass = iabSoundFieldLabelSubDescriptorBO.getMCAUseClass();
                            if (mcaContent == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCAContent", packageID.toString()));
                            } else if (!isValidMCAContent(mcaContent)) {
                                // ST 377-41:2023, Subclause 5.4 / Table 2
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has MCAContent value '%s' which is not a valid SMPTE ST 377-41:2023 (Table 2) symbol", packageID.toString(), mcaContent));
                            }
                            if (mcaUseClass == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCAUseClass", packageID.toString()));
                            } else if (!MCA_USE_CLASS_VALUES.contains(mcaUseClass)) {
                                // ST 377-41:2023, Subclause 5.5.1 / Table 3
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has MCAUseClass value '%s' which is not a valid SMPTE ST 377-41:2023 (Table 3) symbol", packageID.toString(), mcaUseClass));
                            }
                            // ST 377-41:2023, Subclause 5.5.2 / Table 4: the MCA Use Class shall be compatible with the MCA Content.
                            // Skipped for custom ("x-") content, for which Table 4 defines no combination.
                            if (mcaContent != null && mcaUseClass != null
                                    && MCA_CONTENT_TO_USE_CLASS.containsKey(mcaContent)
                                    && MCA_USE_CLASS_VALUES.contains(mcaUseClass)
                                    && !MCA_CONTENT_TO_USE_CLASS.get(mcaContent).contains(mcaUseClass)) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has an MCAContent/MCAUseClass combination '%s'/'%s' that is not permitted by SMPTE ST 377-41:2023 (Table 4)", packageID.toString(), mcaContent, mcaUseClass));
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

                        // Section 5.10.2 / Annex E (ST 2067-201:2026): IAB Channel SubDescriptors are permitted
                        // (one per channel of each BedDefinition is recommended). Photon cannot cross-check the count
                        // against the bitstream bed channels without decoding the essence, but it validates the items
                        // of each instance that is present.
                        List<InterchangeObject.InterchangeObjectBO> iabChannelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(IABChannelSubDescriptor.class)).collect(Collectors.toList());
                        for (InterchangeObject.InterchangeObjectBO subDescriptorBO : iabChannelSubDescriptors) {
                            IABChannelSubDescriptor.IABChannelSubDescriptorBO iabChannelSubDescriptorBO = IABChannelSubDescriptor.IABChannelSubDescriptorBO.class.cast(subDescriptorBO);

                            // Annex E.2.1: IABBedMetaID, IABChannelID and IABAudioDescription are required items.
                            if (iabChannelSubDescriptorBO.getIABBedMetaID() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABChannelSubDescriptor in the IMFTrackFile represented by ID %s is missing the required IABBedMetaID item", packageID.toString()));
                            }
                            if (iabChannelSubDescriptorBO.getIABChannelID() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABChannelSubDescriptor in the IMFTrackFile represented by ID %s is missing the required IABChannelID item", packageID.toString()));
                            }
                            if (iabChannelSubDescriptorBO.getIABAudioDescription() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABChannelSubDescriptor in the IMFTrackFile represented by ID %s is missing the required IABAudioDescription item", packageID.toString()));
                            } else {
                                // Annex E.2.5: IABAudioDescriptionText is present if and only if the most significant
                                // bit of IABAudioDescription is set.
                                boolean expectsText = iabChannelSubDescriptorBO.isAudioDescriptionTextExpected();
                                boolean hasText = iabChannelSubDescriptorBO.getIABAudioDescriptionText() != null;
                                if (expectsText && !hasText) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABChannelSubDescriptor in the IMFTrackFile represented by ID %s has the IABAudioDescription text-present bit set but is missing IABAudioDescriptionText", packageID.toString()));
                                } else if (!expectsText && hasText) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABChannelSubDescriptor in the IMFTrackFile represented by ID %s has IABAudioDescriptionText present but the IABAudioDescription text-present bit is not set", packageID.toString()));
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
