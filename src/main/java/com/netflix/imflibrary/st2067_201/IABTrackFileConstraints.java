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
import com.netflix.imflibrary.st0377_41.MCAContent;
import com.netflix.imflibrary.st0377_41.MCAUseClass;
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

    /**
     * For each MCA Content symbol, the MCA Use Class symbols permitted in combination with it -
     * SMPTE ST 377-41:2023, Table 4. Custom content ({@link MCAContent#x}, "x-" prefix) is not listed; Table 4
     * defines no combination for it. The vocabulary itself is defined by the {@link MCAContent} / {@link MCAUseClass}
     * enums (Tables 2 and 3); only the cross-table combinations are captured here.
     */
    private static final Map<MCAContent, Set<MCAUseClass>> MCA_CONTENT_TO_USE_CLASS = Map.ofEntries(
            Map.entry(MCAContent.PRM, Set.of(MCAUseClass.FCMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.SAP, Set.of(MCAUseClass.FCMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.HI, Set.of(MCAUseClass.FCMP)),
            Map.entry(MCAContent.DV, Set.of(MCAUseClass.FCMP)),
            Map.entry(MCAContent.CM, Set.of(MCAUseClass.FCMP, MCAUseClass.SING)),
            Map.entry(MCAContent.DX, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.MX, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.FX, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.FFX, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.ME, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.OP, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.MESP, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.DME, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.NDME, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.PNAR, Set.of(MCAUseClass.SING)),
            Map.entry(MCAContent.ONAR, Set.of(MCAUseClass.SING)),
            Map.entry(MCAContent.LCM, Set.of(MCAUseClass.SING)),
            Map.entry(MCAContent.VO, Set.of(MCAUseClass.SING)),
            Map.entry(MCAContent.VI, Set.of(MCAUseClass.SING)),
            Map.entry(MCAContent.MOS, Set.of(MCAUseClass.FCMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.ADR, Set.of(MCAUseClass.ICMP)),
            Map.entry(MCAContent.GRP, Set.of(MCAUseClass.ICMP)),
            Map.entry(MCAContent.CRD, Set.of(MCAUseClass.ICMP)),
            Map.entry(MCAContent.WLA, Set.of(MCAUseClass.ICMP)),
            Map.entry(MCAContent.VOC, Set.of(MCAUseClass.ICMP, MCAUseClass.SMPL)),
            Map.entry(MCAContent.FOL, Set.of(MCAUseClass.ICMP)),
            Map.entry(MCAContent.BG, Set.of(MCAUseClass.ICMP)));

    // Prevent instantiation
    public IABTrackFileConstraints() {}

    /**
     * Indicates whether the supplied MCA Content value is valid per SMPTE ST 377-41:2023, Table 2 - i.e. one of the
     * defined {@link MCAContent} symbols or a custom value whose symbol begins with "x-".
     * @param mcaContent the MCA Content value to test
     * @return true if the value is a valid MCA Content symbol
     */
    static boolean isValidMCAContent(String mcaContent) {
        return MCA_CONTENT_TO_USE_CLASS.containsKey(MCAContent.getValueFromSymbol(mcaContent)) || isCustomMCAContent(mcaContent);
    }

    /**
     * Indicates whether the supplied MCA Use Class value is valid per SMPTE ST 377-41:2023, Table 3 - i.e. one of the
     * defined {@link MCAUseClass} symbols.
     * @param mcaUseClass the MCA Use Class value to test
     * @return true if the value is a valid MCA Use Class symbol
     */
    static boolean isValidMCAUseClass(String mcaUseClass) {
        return MCAUseClass.getValueFromSymbol(mcaUseClass) != MCAUseClass.Unknown;
    }

    /**
     * Indicates whether the MCA Content / MCA Use Class combination is permitted by SMPTE ST 377-41:2023, Table 4.
     * The combination is only constrained when the content is a recognised (non-custom) symbol and the use class is
     * a recognised symbol; for custom or unknown values Table 4 defines no combination, so this returns true.
     * @param mcaContent the MCA Content value
     * @param mcaUseClass the MCA Use Class value
     * @return false only when both values are recognised and their combination is not permitted
     */
    static boolean isPermittedMCACombination(String mcaContent, String mcaUseClass) {
        MCAContent content = MCAContent.getValueFromSymbol(mcaContent);
        MCAUseClass useClass = MCAUseClass.getValueFromSymbol(mcaUseClass);
        if (!MCA_CONTENT_TO_USE_CLASS.containsKey(content) || useClass == MCAUseClass.Unknown) {
            return true;
        }
        return MCA_CONTENT_TO_USE_CLASS.get(content).contains(useClass);
    }

    private static boolean isCustomMCAContent(String mcaContent) {
        return mcaContent != null && mcaContent.startsWith("x-");
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
                            // When present, both items shall be set according to SMPTE ST 377-41:2023 (Subclauses 5.4 and 5.5)
                            // and, per SMPTE ST 377-4:2021, they may only appear as a pair: neither shall be present without the other.
                            String mcaContent = iabSoundFieldLabelSubDescriptorBO.getMCAContent();
                            String mcaUseClass = iabSoundFieldLabelSubDescriptorBO.getMCAUseClass();
                            if (mcaContent == null && mcaUseClass == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCAContent", packageID.toString()));
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_IAB_EXCEPTION_PREFIX +
                                        String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s is missing MCAUseClass", packageID.toString()));
                            } else {
                                if (mcaContent == null) {
                                    // ST 377-4:2021: MCA Use Class shall only be present when MCA Content is present.
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has MCAUseClass but is missing MCAContent, which SMPTE ST 377-4:2021 requires whenever MCAUseClass is present", packageID.toString()));
                                } else if (!isValidMCAContent(mcaContent)) {
                                    // ST 377-41:2023, Subclause 5.4 / Table 2
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has MCAContent value '%s' which is not a valid SMPTE ST 377-41:2023 (Table 2) symbol", packageID.toString(), mcaContent));
                                }
                                if (mcaUseClass == null) {
                                    // ST 377-4:2021: MCA Content shall only be present when MCA Use Class is present.
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has MCAContent but is missing MCAUseClass, which SMPTE ST 377-4:2021 requires whenever MCAContent is present", packageID.toString()));
                                } else if (!isValidMCAUseClass(mcaUseClass)) {
                                    // ST 377-41:2023, Subclause 5.5.1 / Table 3
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has MCAUseClass value '%s' which is not a valid SMPTE ST 377-41:2023 (Table 3) symbol", packageID.toString(), mcaUseClass));
                                }
                                // ST 377-41:2023, Subclause 5.5.2 / Table 4: the MCA Use Class shall be compatible with the MCA Content.
                                // Skipped for custom ("x-") content, for which Table 4 defines no combination.
                                if (mcaContent != null && mcaUseClass != null && !isPermittedMCACombination(mcaContent, mcaUseClass)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_IAB_EXCEPTION_PREFIX +
                                            String.format("IABSoundfieldLabelSubDescriptor in the IMFTrackFile represented by ID %s has an MCAContent/MCAUseClass combination '%s'/'%s' that is not permitted by SMPTE ST 377-41:2023 (Table 4)", packageID.toString(), mcaContent, mcaUseClass));
                                }
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
