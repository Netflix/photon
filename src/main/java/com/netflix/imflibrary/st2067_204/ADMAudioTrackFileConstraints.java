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
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.AudioChannelLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.WaveAudioEssenceDescriptor;
import com.netflix.imflibrary.st0377_41.MCAContent;
import com.netflix.imflibrary.st0377_41.MCAUseClass;
import com.netflix.imflibrary.st0377.header.GroupOfSoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.Sequence;
import com.netflix.imflibrary.st0377.header.SoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.TimelineTrack;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ErrorLogger;

import jakarta.annotation.Nonnull;


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
            List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
            if (adm_CHNASubDescriptors.isEmpty() && admAudioMetadataSubDescriptors.isEmpty()) {
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
        WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = headerPartitionIMF.getWaveAudioEssenceDescriptor();
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();

        if (waveAudioEssenceDescriptor == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("IMFTrackFile represented by ID %s does not contain both a Sound Track and a Wave Audio Essence Descriptor and so is not a valid ADM Audio Track File", packageID.toString()));
            return;
        }

        //
        // "Regular" Audio Track File checks
        //
        // Section 5.3.3 st2067-2:2016 and Section 10 st0382:2007
        if (!StructuralMetadata.isAudioWaveClipWrapped(waveAudioEssenceDescriptor.getEssenceContainerUL().getULAsBytes()[14])) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("Wave Audio Essence Descriptor indicates that the Audio Essence within an ADM Audio Track File is not Wave Clip-Wrapped in the IMFTrackFile represented by ID %s.", packageID.toString()));
        }
        // ST 2067-2:2020 section 5.3.2.2
        if ( ((waveAudioEssenceDescriptor.getAudioSamplingRateNumerator()  != 48000) && (waveAudioEssenceDescriptor.getAudioSamplingRateNumerator()  != 96000)) || waveAudioEssenceDescriptor.getAudioSamplingRateDenominator() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("Audio Sample rate %d/%d does not match 48000 Hz or 96000 Hz in the IMFTrackFile represented by ID %s",  waveAudioEssenceDescriptor.getAudioSamplingRateNumerator(), waveAudioEssenceDescriptor.getAudioSamplingRateDenominator(), packageID.toString()));
        }
        // ST 2067-2:2020 section 5.3.2.3
        int bitDepth = waveAudioEssenceDescriptor.getQuantizationBits();
        if (bitDepth != 24) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s indicates an Audio Bit Depth = %d, only 24 is allowed.", packageID.toString(), waveAudioEssenceDescriptor.getQuantizationBits()));
        }
        //
        // "Standard ADM Constraints" configuration
        //
        // RIFFChunkDefinitionSubDescriptor
        //
        List<InterchangeObject.InterchangeObjectBO> riffChunkDefinitionSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(RIFFChunkDefinitionSubDescriptor.class)).collect(Collectors.toList());
        if (riffChunkDefinitionSubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s refers to %d RIFFChunkDefinitionSubDescriptor, 1 is required per ST 2067-204", packageID.toString(), riffChunkDefinitionSubDescriptors.size()));
        } else {
            // ST 2131:2026-05 Table 3
            RIFFChunkDefinitionSubDescriptor.RIFFChunkDefinitionSubDescriptorBO riffChunkDefinitionSubDescriptorsB0 = RIFFChunkDefinitionSubDescriptor.RIFFChunkDefinitionSubDescriptorBO.class.cast(riffChunkDefinitionSubDescriptors.get(0));

            if (riffChunkDefinitionSubDescriptorsB0.getRIFFChunkStreamID() == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                        String.format("RIFFChunkDefinitionSubDescriptor in the IMFTrackFile represented by ID %s is missing RIFFChunkStreamID", packageID.toString()));
            }
            if (riffChunkDefinitionSubDescriptorsB0.getRIFFChunkID() == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                        String.format("RIFFChunkDefinitionSubDescriptor in the IMFTrackFile represented by ID %s is missing RIFFChunkID", packageID.toString()));
            }
        }
        // ST 2131 section 10.2 RIFFChunkReferencesSubDescriptor
        List<InterchangeObject.InterchangeObjectBO> riffChunkReferencesSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(RIFFChunkReferencesSubDescriptor.class)).collect(Collectors.toList());
        if (riffChunkReferencesSubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one RIFFChunkReferencesSubDescriptor, the actual number is %d", packageID.toString(), riffChunkReferencesSubDescriptors.size()));
        }       
        // ST 2131 section 10.2 ADM_CHNASubDescriptor
        List<InterchangeObject.InterchangeObjectBO> adm_CHNASubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADM_CHNASubDescriptor.class)).collect(Collectors.toList());
        if (adm_CHNASubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one ADM_CHNASubDescriptor, the actual number is %d", packageID.toString(), adm_CHNASubDescriptors.size()));
        }
        //
        // ADMAudioMetadataSubDescriptor
        //
        List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
        if (admAudioMetadataSubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s refers to %d ADMAudioMetadataSubDescriptor, 1 is required per ST 2067-204", packageID.toString(), admAudioMetadataSubDescriptors.size()));
        } else {
            // ST 2131:2026-05 Table 11, ST 2067-204:2026-05 Table 1
            ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO admAudioMetadataSubDescriptorBO = ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO.class.cast(admAudioMetadataSubDescriptors.get(0));

            if (admAudioMetadataSubDescriptorBO.getRIFFChunkStreamID_link1() == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                        String.format("ADMAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing RIFFChunkStreamID_link1", packageID.toString()));
            }
            if (admAudioMetadataSubDescriptorBO.getADMProfileLevelULBatch() == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                        String.format("ADMAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing ADMProfileLevelULBatch", packageID.toString()));
            } else {
                CompoundDataTypes.MXFCollections.MXFCollection<UL> admProfileLevelULBatch = admAudioMetadataSubDescriptorBO.getADMProfileLevelULBatch();
                // TODO: Check ULs in admProfileLevelULBatch against Labels.xml for <Applications>ADMProfileLevel</Applications>
            }
        }
        //
        // Audio labeling: "Audio Labeling Framework for ADM-described Content"
        //
        List<InterchangeObject.InterchangeObjectBO> audioChannelLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(AudioChannelLabelSubDescriptor.class)).collect(Collectors.toList());
        // ST 2067-4 section 4.4.1
        if (audioChannelLabelSubDescriptors.size() != 0) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has %d illegal AudioChannelLabelSubDescriptor(s)", packageID.toString(), audioChannelLabelSubDescriptors.size()));
        }
        List<InterchangeObject.InterchangeObjectBO> soundFieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
        if (soundFieldGroupLabelSubDescriptors.size() != 0) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has %d illegal SoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), soundFieldGroupLabelSubDescriptors.size()));
        }
        List<InterchangeObject.InterchangeObjectBO> groupOfSoundFieldGroupsLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(GroupOfSoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
        if (groupOfSoundFieldGroupsLabelSubDescriptors.size() != 0) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has %d illegal GroupOfSoundFieldGroupLabelSubDescriptor(s)", packageID.toString(), groupOfSoundFieldGroupsLabelSubDescriptors.size()));
        }
        // ST 2067-204:2026-05 section 5.1
        if (waveAudioEssenceDescriptor.getChannelAssignmentUL() == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s is missing the ChannelAssignment item", packageID.toString()));
        } else if (!waveAudioEssenceDescriptor.getChannelAssignmentUL().equals(AUDIO_LABELING_FRAMEWORK_ADM_CONTENT_UL.getULAsMXFUid())) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s has an illegal value %s in the ChannelAssignment item, shall be %s", packageID.toString(), waveAudioEssenceDescriptor.getChannelAssignmentUL().toString(), AUDIO_LABELING_FRAMEWORK_ADM_CONTENT_UL.toString()));
        }
        //
        // ADMSoundfieldGroupLabelSubDescriptor
        //
        List<InterchangeObject.InterchangeObjectBO> admSoundfieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMSoundfieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
        if (admSoundfieldGroupLabelSubDescriptors.size() == 0) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s refers to zero ADMSoundfieldGroupLabelSubDescriptor, 1 is required per Soundfield Group", packageID.toString()));
        } else {
            // ST 2067-204 section 4.4.2 Table 2
            ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO admSoundfieldGroupLabelSubDescriptorBO = null;

            for (InterchangeObject.InterchangeObjectBO  sub_descriptor : admSoundfieldGroupLabelSubDescriptors) {
                admSoundfieldGroupLabelSubDescriptorBO = ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO.class.cast(sub_descriptor);
                int num_mca_adm_labels = 0;
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagName() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagName", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagName().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME)) {
                        num_mca_adm_labels++;
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagSymbol", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL)) {
                        num_mca_adm_labels++;
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCALabelDictionaryId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONARY_ID_UL)) {
                        num_mca_adm_labels++;
                    }
                }
                if ((num_mca_adm_labels > 0) && (num_mca_adm_labels < 3)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s " +
                            "uses ADM Soundfield Group Labels only in %d, but not all of the items MCALabelDictionaryID, MCATagSymbol, MCATagName", sub_descriptor.getInstanceUID().toString(), packageID.toString(), num_mca_adm_labels));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage() == null) {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing RFC5646SpokenLanguage. " + 
                        "It shall be absent if and only if the soundfield group is not associated with a primary spoken language.", sub_descriptor.getInstanceUID().toString(), packageID.toString())));
                } else {
                    if (!IMFConstraints.isSpokenLanguageRFC5646Compliant(admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage())) {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackfile represented by ID %s is not RFC5646 compliant", admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage(), sub_descriptor.getInstanceUID().toString(), packageID.toString())));
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAContent() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAContent", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    // Check against ST 377-41:2021 Table 2
                    if (MCAContent.getValueFromSymbol(admSoundfieldGroupLabelSubDescriptorBO.getMCAContent()).equals(MCAContent.Unknown)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has invalid MCA Content (%s)", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCAContent()));
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAUseClass", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                // Check against ST 377-41:2021 Table 3
                    if (MCAUseClass.getValueFromSymbol(admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass()).equals(MCAUseClass.Unknown)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has invalid MCA Use Class (%s)", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass()));
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATitle() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitle", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATitleVersion() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitleVersion", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID() != null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has forbidden MCAChannelID %d", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID()));
                }
                // ST 2067-204 Table 7
                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioProgrammeID() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing ADMAudioProgrammId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                // ST 2067-204 Table 7
                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioContentID() != null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioContentID item", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                // ST 2067-204 Table 7
                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioObjectID() != null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioObjectID item", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
            }
        }
    }
}
