package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.header.AudioChannelLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.WaveAudioEssenceDescriptor;
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

public final class ADMAudioTrackFileConstraints {

    public final static UL AUDIO_LABELING_FRAMEWORK_ADM_CONTENT_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.04020210.05010000");
    private static final String IMF_ADM_AUDIO_EXCEPTION_PREFIX = "IMF ADM Audio check: ";

    // Prevent instantiation
    public ADMAudioTrackFileConstraints() {}

    public static void checkComplianceFromIMFTrackFileReader(IMFConstraints.HeaderPartitionIMF headerPartitionIMF, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        HeaderPartition headerPartition = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
        if (subDescriptors.isEmpty()) {
            // Assume this is not ADM Audio file, return silently when called from IMFTrackFileReader
            return;
        } else {
            List<InterchangeObject.InterchangeObjectBO> adm_CHNASubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADM_CHNASubDescriptor.class)).collect(Collectors.toList());
            List<InterchangeObject.InterchangeObjectBO> admSoundfieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMSoundfieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
            if (adm_CHNASubDescriptors.isEmpty() && admSoundfieldGroupLabelSubDescriptors.isEmpty()) {
                // Assume this is not an ADM Audio file, return silently when called from IMFTrackFileReader
                return;
            } else {
                checkCompliance(headerPartitionIMF, imfErrorLogger);
            }
        }
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
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
                if (genericDescriptor instanceof WaveAudioEssenceDescriptor) { // Support for st2067-204


                    //
                    // WaveAudioEssenceDescriptor
                    //
                    WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = (WaveAudioEssenceDescriptor) genericDescriptor;

                    // ST 2067-2:2020 section 5.3.2.2
                    if ( ((waveAudioEssenceDescriptor.getAudioSamplingRateNumerator()  != 48000) && (waveAudioEssenceDescriptor.getAudioSamplingRateNumerator()  != 96000)) || waveAudioEssenceDescriptor.getAudioSamplingRateDenominator() != 1) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("Audio Sample rate %d/%d does not match 48 000 Hz oder 96 000 Hz in the IMFTrackFile represented by ID %s.",  waveAudioEssenceDescriptor.getAudioSamplingRateNumerator(), waveAudioEssenceDescriptor.getAudioSamplingRateDenominator(), packageID.toString()));
                    }
                    // ST 2067-2:2020 section 5.3.2.3
                    int bitDepth = waveAudioEssenceDescriptor.getQuantizationBits();
                    if (bitDepth != 24) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s indicates an Audio Bit Depth = %d, only 24 is allowed.", packageID.toString(), waveAudioEssenceDescriptor.getQuantizationBits()));
                    }
                    // ST 2067-204 section 4.1
                    if (!waveAudioEssenceDescriptor.getChannelAssignmentUL().equals(AUDIO_LABELING_FRAMEWORK_ADM_CONTENT_UL.getULAsMXFUid())) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has an illegal value %s in the ChannelAssignment item, shall be %s", packageID.toString(), waveAudioEssenceDescriptor.getChannelAssignmentUL().toString(), AUDIO_LABELING_FRAMEWORK_ADM_CONTENT_UL.toString()));
                    }
                    // ST 2067-2:2020 section 5.3.5
                    if (waveAudioEssenceDescriptor.getReferenceAudioAlignmentLevel() == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s is missing the Reference Audio Alignment Level item.", packageID.toString()));
                    }

                    // ST 2067-2:2020 section 5.3.5
                    if (waveAudioEssenceDescriptor.getReferenceImageEditRate() == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s is missing the Reference Image Edit Rate item.", packageID.toString()));
                    }

                    // Section 5.10.2
                    if (subDescriptors.size() == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s does not have subdescriptors", packageID.toString()));
                    } else {
                        List<InterchangeObject.InterchangeObjectBO> audioChannelLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(AudioChannelLabelSubDescriptor.class)).collect(Collectors.toList());
                        // ST 2067-4 section 4.4.1
                        if (audioChannelLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has %d illegal AudioChannelLabelSubDescriptor(s)", packageID.toString(), audioChannelLabelSubDescriptors.size()));
                        }
                        List<InterchangeObject.InterchangeObjectBO> soundFieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (soundFieldGroupLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has %d illegal SoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), soundFieldGroupLabelSubDescriptors.size()));
                        }
                        List<InterchangeObject.InterchangeObjectBO> groupOfSoundFieldGroupsLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(GroupOfSoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (groupOfSoundFieldGroupsLabelSubDescriptors.size() != 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has %d illegal GroupOfSoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), groupOfSoundFieldGroupsLabelSubDescriptors.size()));
                        }
                        
                        // ST 2131 section 10.2 ADM_CHNASubDescriptor
                        List<InterchangeObject.InterchangeObjectBO> adm_CHNASubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADM_CHNASubDescriptor.class)).collect(Collectors.toList());
                        if (adm_CHNASubDescriptors.isEmpty()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one ADM_CHNASubDescriptor, the actual number is %d", packageID.toString(), adm_CHNASubDescriptors.size()));
                        }
                        // ST 2131 section 10.2 RIFFChunkReferencesSubDescriptor
                        List<InterchangeObject.InterchangeObjectBO> riffChunkReferencesSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(RIFFChunkReferencesSubDescriptor.class)).collect(Collectors.toList());
                        if (riffChunkReferencesSubDescriptors.size() != 1) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one RIFFChunkReferencesSubDescriptor, the actual number is %d", packageID.toString(), riffChunkReferencesSubDescriptors.size()));
                        }
                        
                        //
                        // ADMSoundfieldGroupLabelSubDescriptor
                        //
                        List<InterchangeObject.InterchangeObjectBO> admSoundfieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMSoundfieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                        if (admSoundfieldGroupLabelSubDescriptors.size() == 0) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s refers to zero ADMSoundfieldGroupLabelSubDescriptor, 1 is required per Soundfield Group", packageID.toString()));
                        } else {
                            // ST 2067-204 section 4.4.2 Table 2
                            ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO admSoundfieldGroupLabelSubDescriptorBO = null;

                            for (InterchangeObject.InterchangeObjectBO  sub_descriptor : admSoundfieldGroupLabelSubDescriptors) {
                                admSoundfieldGroupLabelSubDescriptorBO = ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO.class.cast(sub_descriptor);
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagName() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagName", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                } else {
                                    if (!admSoundfieldGroupLabelSubDescriptorBO.getMCATagName().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                                String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s does not have a valid MCATagName: %s", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCATagName()));
                                    }
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagSymbol", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                } else {
                                    if (!admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                                String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s does not have a valid MCATagSymbol: %s", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol()));
                                    }
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCALabelDictionaryId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                } else {
                                    if (!admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONNARY_ID_UL)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                                String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s does not have the required MCA Label Dictionary Id %s but %s", sub_descriptor.getInstanceUID().toString(), packageID.toString(), ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONNARY_ID_UL, admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId().toString()));
                                    }
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage() != null &&
                                        !IMFConstraints.isSpokenLanguageRFC5646Compliant(admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage())) {
                                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackfile represented by ID %s is not RFC5646 compliant", admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage(), sub_descriptor.getInstanceUID().toString(), packageID.toString())));
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAContent() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAContent", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAUseClass", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATitle() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitle", sub_descriptor.getInstanceUID().toString(), sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATitleVersion() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitleVersion", sub_descriptor.getInstanceUID().toString(), sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID() != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has forbidden MCAChannelID %d", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID()));
                                }
                                // ST 2067-204 Table 7
                                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioProgrammeId() == null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing ADMAudioProgrammId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                // ST 2067-204 Table 7
                                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioContentId() != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioContentId item", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                                // ST 2067-204 Table 7
                                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioObjectId() != null) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioObjectId item", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                                }
                            }
                        }
                        //
                        // ADMAudioMetadataSubDescriptor
                        //
                        int admAudioMetadataPayloadULArrraySize = 0;
                        List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
                        if (admAudioMetadataSubDescriptors.size() != 1) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s refers to %d ADMAudioMetadataSubDescriptor, 1 is required per ST 2067-204", packageID.toString(), admAudioMetadataSubDescriptors.size()));
                        } else {
                            // ST 2067-204 section 5.6.2 Table 3
                            ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO admAudioMetadataSubDescriptorBO = ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO.class.cast(admAudioMetadataSubDescriptors.get(0));

                            if (admAudioMetadataSubDescriptorBO.getRIFFChunkStreamID_link1() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                        String.format("ADMAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing MGALinkId", packageID.toString()));
                            }
                            if (admAudioMetadataSubDescriptorBO.getADMProfileLevelULBatch() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                        String.format("ADMAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing ADMProfileLevelULBatch", packageID.toString()));
                            }
                        }
                    }
                }
            }
        }
    }
}
