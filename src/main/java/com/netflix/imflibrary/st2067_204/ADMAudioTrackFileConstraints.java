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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ADMAudioTrackFileConstraints {

    public final static UL AUDIO_LABELING_FRAMEWORK_ADM_CONTENT_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.04020210.05010000");
    private static final String IMF_ADM_AUDIO_EXCEPTION_PREFIX = "IMF ADM Audio check: ";
    private static final List<String> PROHIBITED_RIFF_CHUNK_IDS = List.of("bxml", "sxml", "BW64", "ds64", "JUNK", "chna");
    
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
        // ST 2067-2:2016 section 5.3.3 and ST 382:2007 section 10
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
        // "Standard ADM Constraints" configuration: ST 2131
        //
        // RIFFChunkReferencesSubDescriptor
        //
        Set<Long> referencedStreamIDs = new HashSet<>();
        List<InterchangeObject.InterchangeObjectBO> riffChunkReferencesSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(RIFFChunkReferencesSubDescriptor.class)).collect(Collectors.toList());
        if (riffChunkReferencesSubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one RIFFChunkReferencesSubDescriptor, the actual number is %d", packageID.toString(), riffChunkReferencesSubDescriptors.size()));
        } else {
            RIFFChunkReferencesSubDescriptor.RIFFChunkReferencesSubDescriptorBO riffChunkReferencesSubDescriptor =
            RIFFChunkReferencesSubDescriptor.RIFFChunkReferencesSubDescriptorBO.class.cast(riffChunkReferencesSubDescriptors.get(0));
            for (Long streamID : riffChunkReferencesSubDescriptor.getRIFFChunkStreamIDsArray().getEntries()) {
                if (referencedStreamIDs.contains(streamID)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("The RIFFChunkReferencesSubDescriptor in the IMFTrackFile represented by ID %s references the Stream ID %d multiple times in the RIFFChunkStreamIDsArray", packageID.toString(), streamID));
                }
                referencedStreamIDs.add(streamID);
            }
        }
        //
        // RIFFChunkDefinitionSubDescriptor
        //
        Set<Long> definedStreamIDs = new HashSet<>();
        Long axmlStreamID = null;
        int axmlCount = 0;
        List<InterchangeObject.InterchangeObjectBO> riffChunkDefinitionSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(RIFFChunkDefinitionSubDescriptor.class)).collect(Collectors.toList());
        for (InterchangeObject.InterchangeObjectBO sub_descriptor : riffChunkDefinitionSubDescriptors) {
            RIFFChunkDefinitionSubDescriptor.RIFFChunkDefinitionSubDescriptorBO riffChunkDefinitionSubDescriptorBO =
            RIFFChunkDefinitionSubDescriptor.RIFFChunkDefinitionSubDescriptorBO.class.cast(sub_descriptor);
            Long streamID = riffChunkDefinitionSubDescriptorBO.getRIFFChunkStreamID();
            if (streamID != null) {
                if (definedStreamIDs.contains(streamID)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("The RIFFChunkDefinitionSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has the same RIFFChunkStreamID ('%d') as another RIFFChunkDefinitionSubDescriptor", sub_descriptor.getInstanceUID().toString(), packageID.toString(), streamID));
                }
                definedStreamIDs.add(streamID);
            }
            if (riffChunkDefinitionSubDescriptorBO.getRIFFChunkID() != null) {
                String riffChunkID = new String(riffChunkDefinitionSubDescriptorBO.getRIFFChunkID(), StandardCharsets.US_ASCII);
                if (riffChunkID.equals("axml")) {
                    axmlCount++;
                    if (axmlCount > 1) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("The RIFFChunkDefinitionSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has RIFFChunkID equal to 'axml' yet an 'axml' RIFFChunkDefinitionSubDescriptor has already been found. There shall be exactly one 'axml' RIFF Chunk and this shall be defined by one RIFFChunkDefinitionSubDescriptor.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                        axmlStreamID = null;
                    } else {
                        axmlStreamID = streamID;
                    }
                }
                // Note that riffChunkID values other than "axml" can legitimately be duplicated
                if (PROHIBITED_RIFF_CHUNK_IDS.contains(riffChunkID)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("The RIFFChunkDefinitionSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has RIFFChunkID equal to the prohibited value '%s'.", sub_descriptor.getInstanceUID().toString(), packageID.toString(), riffChunkID));
                }
            }
        }
        //
        // Validation
        //
        if (!referencedStreamIDs.equals(definedStreamIDs)){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("The set of Stream IDs referenced by the RIFFChunkReferencesSubDescriptor does not match the set of Stream IDs referenced by the RIFFChunkDefinitionSubDescriptors in the IMFTrackFile represented by ID %s.", packageID.toString()));
        }
        if (axmlCount == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                                String.format("No RIFFChunkDefinitionSubDescriptor with RIFFChunkID equal to 'axml' has been found in the IMFTrackFile represented by ID %s. There shall be exactly one 'axml' RIFF Chunk and this shall be defined by one RIFFChunkDefinitionSubDescriptor", packageID.toString()));
        }
        //
        // ADMAudioMetadataSubDescriptor
        //
        List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
        if (admAudioMetadataSubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one ADMAudioMetadataSubDescriptor, the actual number is %d", packageID.toString(), admAudioMetadataSubDescriptors.size()));
        } else {
            // ST 2131:2026-05 Table 11, ST 2067-204:2026-05 Table 1
            ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO admAudioMetadataSubDescriptorBO = ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO.class.cast(admAudioMetadataSubDescriptors.get(0));
            Long riffChunkStreamID_link1 = admAudioMetadataSubDescriptorBO.getRIFFChunkStreamID_link1();
            if (riffChunkStreamID_link1 != null && axmlStreamID != null && riffChunkStreamID_link1 != axmlStreamID) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                        String.format("The ADMAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s has RIFFChunkStreamID_link1 equal to %d but %d was expected", packageID.toString(), riffChunkStreamID_link1, axmlStreamID));
            }
            if (admAudioMetadataSubDescriptorBO.getADMProfileLevelULBatch() == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                        String.format("The ADMAudioMetadataSubDescriptor in the IMFTrackFile represented by ID %s is missing ADMProfileLevelULBatch", packageID.toString()));
            } else {
                CompoundDataTypes.MXFCollections.MXFCollection<UL> admProfileLevelULBatch = admAudioMetadataSubDescriptorBO.getADMProfileLevelULBatch();
                // TODO: Check ULs in admProfileLevelULBatch against Labels.xml for <Applications>ADMProfileLevel</Applications>
            }
        }
        //
        // ADM_CHNASubDescriptor
        //
        List<InterchangeObject.InterchangeObjectBO> adm_CHNASubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADM_CHNASubDescriptor.class)).collect(Collectors.toList());
        if (adm_CHNASubDescriptors.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s shall reference exactly one ADM_CHNASubDescriptor, the actual number is %d", packageID.toString(), adm_CHNASubDescriptors.size()));
        } else {
            ADM_CHNASubDescriptor.ADM_CHNASubDescriptorBO adm_CHNASubDescriptorBO = ADM_CHNASubDescriptor.ADM_CHNASubDescriptorBO.class.cast(adm_CHNASubDescriptors.get(0));
            // TODO: Implement checks for ADM_CHNASubDescriptor as per ST 2131:2026-05 section 8
        }
        //
        // Audio labeling
        //
        // Omit "regular" MCA SubDescriptors: ST 2067-204:2026-05 section 5.4.1
        //
        List<InterchangeObject.InterchangeObjectBO> audioChannelLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(AudioChannelLabelSubDescriptor.class)).collect(Collectors.toList());
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
        //
        // Channel Assignment element signals "Audio Labeling Framework for ADM-described Content"
        // ST 2067-204:2026-05 section 5.1
        //
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
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                    String.format("WAVE Audio Essence Descriptor in the IMFTrackFile represented by ID %s refers to zero ADMSoundfieldGroupLabelSubDescriptor. There shall be one for each of the soundfield groups described by the ADM metadata.", packageID.toString()));
        } else {
            // ST 2067-204:2026-05 section 5.4.2 Table 2
            ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO admSoundfieldGroupLabelSubDescriptorBO = null;

            for (InterchangeObject.InterchangeObjectBO sub_descriptor : admSoundfieldGroupLabelSubDescriptors) {
                admSoundfieldGroupLabelSubDescriptorBO = ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO.class.cast(sub_descriptor);
                int num_mca_adm_labels = 0;
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagName() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagName", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagName().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_NAME)) {
                        num_mca_adm_labels++;
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATagSymbol", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (admSoundfieldGroupLabelSubDescriptorBO.getMCATagSymbol().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_TAG_SYMBOL)) {
                        num_mca_adm_labels++;
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCALabelDictionaryId", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (admSoundfieldGroupLabelSubDescriptorBO.getMCALabelDictionnaryId().equals(ADMSoundfieldGroupLabelSubDescriptor.ADM_MCA_LABEL_DICTIONARY_ID_UL)) {
                        num_mca_adm_labels++;
                    }
                }
                if ((num_mca_adm_labels > 0) && (num_mca_adm_labels < 3)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s " +
                            "uses the ADM Soundfield Group Label only in %d, but not all, of the items MCALabelDictionaryID, MCATagSymbol, MCATagName", sub_descriptor.getInstanceUID().toString(), packageID.toString(), num_mca_adm_labels));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing RFC5646SpokenLanguage. " + 
                            "It shall be absent if and only if the soundfield group is not associated with a primary spoken language.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    if (!IMFConstraints.isSpokenLanguageRFC5646Compliant(admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage())) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("Language Code (%s) in ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackfile represented by ID %s is not RFC5646 compliant", admSoundfieldGroupLabelSubDescriptorBO.getRFC5646SpokenLanguage(), sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAContent() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAContent. ST 2067-204:2026-05 Table 2 states that it should be present.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                    // Check against ST 377-41
                    if (MCAContent.getValueFromSymbol(admSoundfieldGroupLabelSubDescriptorBO.getMCAContent()).equals(MCAContent.Unknown)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has invalid MCA Content (%s) according to ST 377-41. ST 2131:2026-05 section 10.2 states that the Controlled Vocabulary defined in SMPTE ST 377-41 should be adopted.", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCAContent()));
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCAUseClass. ST 2067-204:2026-05 Table 2 states that it should be present.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                } else {
                // Check against ST 377-41
                    if (MCAUseClass.getValueFromSymbol(admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass()).equals(MCAUseClass.Unknown)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has invalid MCA Use Class (%s) according to ST 377-41. ST 2131:2026-05 section 10.2 states that the Controlled Vocabulary defined in SMPTE ST 377-41 should be adopted.", sub_descriptor.getInstanceUID().toString(), packageID.toString(), admSoundfieldGroupLabelSubDescriptorBO.getMCAUseClass()));
                    }
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATitle() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitle. ST 2067-204:2026-05 Table 2 states that it should be present.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCATitleVersion() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing MCATitleVersion. ST 2067-204:2026-05 Table 2 states that it should be present.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getMCAChannelID() != null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has the forbidden item MCAChannelID. ST 2067-204:2026-05 Table 2 states that it shall not be present.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                Long riffChunkStreamID_link2 = admSoundfieldGroupLabelSubDescriptorBO.getRIFFChunkStreamID_link2();
                if (riffChunkStreamID_link2 != null && axmlStreamID != null && riffChunkStreamID_link2 != axmlStreamID) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has RIFFChunkStreamID_link2 equal to %d but %d was expected", sub_descriptor.getInstanceUID().toString(), packageID.toString(), riffChunkStreamID_link2, axmlStreamID));
                }                
                //
                // Assume Operational Mode A: ST 2067-204:2026-05 section 7
                //
                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioProgrammeID() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s is missing ADMAudioProgrammeId. ST 2067-204:2026-05 Table 7 states that it shall be present for Operational Mode A. It is assumed that Operational Mode A is in use.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioContentID() != null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioContentID item. ST 2067-204:2026-05 Table 7 states that it shall not be present for Operational Mode A. It is assumed that Operational Mode A is in use.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
                if (admSoundfieldGroupLabelSubDescriptorBO.getADMAudioObjectID() != null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ADM_AUDIO_EXCEPTION_PREFIX +
                            String.format("ADMSoundfieldGroupLabelSubDescriptor with ID %s in the IMFTrackFile represented by ID %s has an illegal ADMAudioObjectID item. ST 2067-204:2026-05 Table 7 states that it shall not be present for Operational Mode A. It is assumed that Operational Mode A is in use.", sub_descriptor.getInstanceUID().toString(), packageID.toString()));
                }
            }
        }
    }
}
