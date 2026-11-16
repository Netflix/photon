/*
 *
 * Copyright 2026 Netflix, Inc.
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

package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.*;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RFC5646SpokenLanguage;
import com.netflix.imflibrary.utils.Utilities;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * ST 2067-2 core constraints for Wave Audio Track Files.
 * Applied through the (edition specific) core constraints validators, never implicitly.
 */
public final class IMFCoreConstraintsAudio {
    private static final String IMF_ESSENCE_EXCEPTION_PREFIX = "IMF Essence Component check: ";
    private static final byte[] IMF_CHANNEL_ASSIGNMENT_UL = {0x06, 0x0e, 0x2b, 0x34, 0x04, 0x01, 0x01, 0x0d, 0x04, 0x02, 0x02, 0x10, 0x04, 0x01, 0x00, 0x00};

    private IMFCoreConstraintsAudio() {}

    public static void checkWaveAudioTrackFile(@Nonnull HeaderPartition headerPartition, @Nonnull WaveAudioEssenceDescriptor waveAudioEssenceDescriptor,
                                               @Nonnull UUID packageID, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        if ((waveAudioEssenceDescriptor.getChannelAssignmentUL() == null) ||
                (!waveAudioEssenceDescriptor.getChannelAssignmentUL().equalsWithMask(new MXFUID(IMF_CHANNEL_ASSIGNMENT_UL), 0b1111111011111111))) {
            //Section 5.3.4.2 st2067-2:2016
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("ChannelAssignment UL for WaveAudioEssenceDescriptor = %s is different from %s in the IMFTrackFile represented by ID %s.",
                    waveAudioEssenceDescriptor.getChannelAssignmentUL(), new MXFUID(IMF_CHANNEL_ASSIGNMENT_UL), packageID.toString()));
        }
        //RFC-5646 spoken language is a part of the MCALabelSubDescriptor and SoundFieldGroupLabelSubdescriptors according to Section 5.3.6.5 st2067-2:2016 has language around RFC-5646 primary spoken language
        if (headerPartition.getAudioEssenceSpokenLanguage() == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s does not have a RFC5646 spoken language indicated, language code shall be set in the SoundFieldGroupLabelSubDescriptor, unless the AudioEssence does not have a primary spoken language.", packageID.toString()));
        } else {
            //Section 6.3.6 st377-4:2012
            if (!RFC5646SpokenLanguage.isCompliant(headerPartition.getAudioEssenceSpokenLanguage())) {
                List<String> strings = RFC5646SpokenLanguage.getPrimarySpokenLanguageUnicodeString(headerPartition.getAudioEssenceSpokenLanguage());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in SoundFieldGroupLabelSubdescriptor in the IMFTrackfile represented by ID %s is not RFC5646 compliant", strings, packageID.toString()));
            }
        }

        //Section 5.3.3 st2067-2:2016 and Section 10 st0382:2007
        if (!StructuralMetadata.isAudioWaveClipWrapped(waveAudioEssenceDescriptor.getEssenceContainerUL().getULAsBytes()[14])) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("WaveAudioEssenceDescriptor indicates that the Audio Essence within an Audio Track File is not Wave Clip-Wrapped in the IMFTrackFile represented by ID %s.", packageID.toString()));
        }
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
        //Section 5.3.6.2 st2067-2:2016
        if (subDescriptors.size() == 0) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                    String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s indicates a channel count of %d, however there are %d AudioChannelLabelSubdescriptors, every audio channel should refer to exactly one AudioChannelLabelSubDescriptor and vice versa.", packageID.toString(), waveAudioEssenceDescriptor.getChannelCount(), subDescriptors.size()));
        } else {
            //Section 5.3.6.2 st2067-2:2016
            Map<Long, AudioChannelLabelSubDescriptor> audioChannelLabelSubDescriptorMap = headerPartition.getAudioChannelIDToMCASubDescriptorMap();
            if (waveAudioEssenceDescriptor.getChannelCount() == 0 || waveAudioEssenceDescriptor.getChannelCount() != audioChannelLabelSubDescriptorMap.size()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                        String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s indicates a channel count of %d, however there are %d AudioChannelLabelSubdescriptors, every audio channel should refer to exactly one AudioChannelLabelSubDescriptor and vice versa.", packageID.toString(), waveAudioEssenceDescriptor.getChannelCount(), audioChannelLabelSubDescriptorMap.size()));
            }
            for (Long channelID = 1L; channelID <= waveAudioEssenceDescriptor.getChannelCount(); channelID++) {
                //Section 5.3.6.5 st2067-2:2016
                if (!audioChannelLabelSubDescriptorMap.containsKey(channelID)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                            String.format("AudioChannelLabelSubdescriptor missing for ChannelID %d, in the IMFTrackFile represented by ID %s", channelID, packageID.toString()));
                }
            }
            List<InterchangeObject.InterchangeObjectBO> soundFieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
            //Section 5.3.6.3 st2067-2:2016
            if (soundFieldGroupLabelSubDescriptors.size() != 1) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                        String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s refers to %d SoundFieldGroupLabelSubDescriptors exactly 1 is required", packageID.toString(), soundFieldGroupLabelSubDescriptors.size()));
            } else {
                SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO soundFieldGroupLabelSubDescriptorBO = SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO.class.cast(soundFieldGroupLabelSubDescriptors.get(0));
                //Section 5.3.6.5 st2067-2:2016
                if ((soundFieldGroupLabelSubDescriptorBO.getMCATitle() == null || soundFieldGroupLabelSubDescriptorBO.getMCATitle().isEmpty())
                        || (soundFieldGroupLabelSubDescriptorBO.getMCATitleVersion() == null || soundFieldGroupLabelSubDescriptorBO.getMCATitleVersion().isEmpty())
                        || (soundFieldGroupLabelSubDescriptorBO.getMCAAudioContentKind() == null || soundFieldGroupLabelSubDescriptorBO.getMCAAudioContentKind().isEmpty())
                        || (soundFieldGroupLabelSubDescriptorBO.getMCAAudioElementKind() == null || soundFieldGroupLabelSubDescriptorBO.getMCAAudioElementKind().isEmpty())) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                            String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s refers to a SoundFieldGroupLabelSubDescriptor that is missing one/all of MCATitle, MCATitleVersion, MCAAudioContentKind, MCAAudioElementKind, %n%s.", packageID.toString(), soundFieldGroupLabelSubDescriptorBO.toString()));
                }
                SoundFieldGroupLabelSubDescriptor soundFieldGroupLabelSubDescriptor = (SoundFieldGroupLabelSubDescriptor) headerPartition.getSoundFieldGroupLabelSubDescriptors()
                        .get(0);
                List<InterchangeObject> audioChannelLabelSubDescriptors = headerPartition.getAudioChannelLabelSubDescriptors();
                //Section 6.3.2 st377-4:2012
                if (soundFieldGroupLabelSubDescriptor.getMCALinkId() == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                            String.format("SoundFieldGroupLabelSubDescriptor is missing MCALinkID, in the IMFTrackFile represented by ID %s",
                                    packageID.toString()));
                } else {
                    for (InterchangeObject interchangeObject : audioChannelLabelSubDescriptors) {
                        AudioChannelLabelSubDescriptor audioChannelLabelSubDescriptor = AudioChannelLabelSubDescriptor.class.cast(interchangeObject);
                        //Section 5.3.6.3 st2067-2:2016
                        if (audioChannelLabelSubDescriptor.getSoundfieldGroupLinkId() == null) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                                    String.format("Audio channel with MCALinkID %s is missing SoundfieldGroupLinkId, in the IMFTrackFile represented by ID %s",
                                            audioChannelLabelSubDescriptor.getMCALinkId() != null ? audioChannelLabelSubDescriptor.getMCALinkId().toString() : "",
                                            packageID.toString()));
                        }
                        //Section 6.3.2 st377-4:2012
                        else if (!audioChannelLabelSubDescriptor.getSoundfieldGroupLinkId()
                                .equals(soundFieldGroupLabelSubDescriptor.getMCALinkId())) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                                    String.format("Audio channel with MCALinkID %s refers to wrong SoundfieldGroupLinkId %s, Should refer to %s, in the IMFTrackFile represented by ID %s",
                                            audioChannelLabelSubDescriptor.getMCALinkId() != null ? audioChannelLabelSubDescriptor.getMCALinkId().toString() : "",
                                            audioChannelLabelSubDescriptor.getSoundfieldGroupLinkId().toString(),
                                            soundFieldGroupLabelSubDescriptor.getMCALinkId().toString(),
                                            packageID.toString()));
                        }
                    }
                }
            }
        }

        int audioSampleRate = waveAudioEssenceDescriptor.getAudioSamplingRateNumerator() / waveAudioEssenceDescriptor.getAudioSamplingRateDenominator();
        //Section 5.3.2.2 st2067-2:2016
        if (audioSampleRate != 48000
                && audioSampleRate != 96000) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                    String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s seems to indicate an Audio Sample Rate = %f, only 48000 and 96000 are allowed.", packageID.toString(), (double) waveAudioEssenceDescriptor.getAudioSamplingRateNumerator() / waveAudioEssenceDescriptor.getAudioSamplingRateDenominator()));
        }

        int bitDepth = waveAudioEssenceDescriptor.getQuantizationBits();
        //Section 5.3.2.3 st2067-2:2016
        if (bitDepth != 24) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                    String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s seems to indicate an Audio Bit Depth = %d, only 24 is allowed.", packageID.toString(), waveAudioEssenceDescriptor.getQuantizationBits()));
        }
    }
}
