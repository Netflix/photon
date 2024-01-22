/*
 *
 * Copyright 2015 Netflix, Inc.
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

package com.netflix.imflibrary;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.*;
import com.netflix.imflibrary.st2067_201.IABEssenceDescriptor;
import com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor;
import com.netflix.imflibrary.st2067_204.ADMAudioMetadataSubDescriptor;
import com.netflix.imflibrary.st2067_204.ADMAudioTrackFileConstraints;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class consists exclusively of static methods that help verify the compliance of OP1A-conformant
 * (see st378:2004) MXF header partition as well as MXF partition packs (see st377-1:2011)
 * with st2067-5:2013
 */
public final class IMFConstraints
{
    private static final String IMF_ESSENCE_EXCEPTION_PREFIX = "IMF Essence Component check: ";
    private static final byte[] IMF_CHANNEL_ASSIGNMENT_UL = {0x06, 0x0e, 0x2b, 0x34, 0x04, 0x01, 0x01, 0x0d, 0x04, 0x02, 0x02, 0x10, 0x04, 0x01, 0x00, 0x00};
    public static final String IMSC1TextProfileDesignator = "http://www.w3.org/ns/ttml/profile/imsc1/text";
    public static final String IMSC1ImageProfileDesignator = "http://www.w3.org/ns/ttml/profile/imsc1/image";
    public static final String IMSC1ImageResourceMimeMediaType = "image/png";
    public static final String IMSC1FontResourceMimeMediaType = "application/x-font-opentype";
    //to prevent instantiation
    private IMFConstraints()
    {}

    /**
     * Checks the compliance of an OP1A-conformant header partition with st2067-5:2013. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param headerPartitionOP1A the OP1A-conformant header partition
     * @param imfErrorLogger - an object for logging errors
     * @throws IOException - any I/O related error is exposed through an IOException
     * @return the same header partition wrapped in a HeaderPartitionIMF object
     */
    public static HeaderPartitionIMF checkIMFCompliance(MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        int previousNumberOfErrors = imfErrorLogger.getErrors().size();

        HeaderPartition headerPartition = headerPartitionOP1A.getHeaderPartition();

        Preface preface = headerPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage;
        filePackage = (SourcePackage)genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();
        //check 'Operational Pattern' field in Preface
        byte[] bytes = preface.getOperationalPattern().getULAsBytes();
        //Section 8.3.3 st377-1:2011 and Section 5.2 st2067-5:2013
        if (OperationalPatternHelper.getPackageComplexity(bytes) != OperationalPatternHelper.PackageComplexity.SinglePackage)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String
                    .format("IMFTrackFile represented by Id %s, Lower four bits of Operational Pattern qualifier byte = 0x%x, should be = 0x01 per the definition of OperationalPattern-1A for Package Complexity.",
                    packageID.toString(), bytes[13]));
        }

        //Section 8.3.3 st377-1:2011
        if (OperationalPatternHelper.getItemComplexity(bytes) != OperationalPatternHelper.ItemComplexity.SingleItem)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String
                    .format("IMFTrackFile represented by Id %s, Lower four bits of Operational Pattern qualifier byte = 0x%x, should be = 0x01 per the definition of OperationalPattern-1A for Item Complexity.",
                    packageID.toString(), bytes[12]));
        }

        //Section 5.1.1#13 st2067-5:2014 , primary package identifier for Preface shall be set to the top-level file package
        if ((preface.getPrimaryPackage() == null) || (!preface.getPrimaryPackage().equals(preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage())))
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Primary package identifier for Preface is not set to the top-level file package in the IMFTrackFile represented by ID %s.", packageID.toString()));
        }

        //From st2067-5:2013 section 5.1.3, only one essence track shall exist in the file package
        {
            MXFUID packageUID = filePackage.getPackageUID();
            byte[] packageUID_first16Bytes_Constrained = {0x06, 0x0a, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x01, 0x0f, 0x20, 0x13, 0x00, 0x00, 0x00};
            byte[] packageUID_first16Bytes = Arrays.copyOfRange(packageUID.getUID(), 0, packageUID_first16Bytes_Constrained.length);
            boolean result = packageUID_first16Bytes[0] == packageUID_first16Bytes_Constrained[0];
            for(int i=1; i < packageUID_first16Bytes_Constrained.length ; i++){
                result &= packageUID_first16Bytes[i] == packageUID_first16Bytes_Constrained[i];
            }
            //Section 5.1.5 st2067-2:2016
            if(!result){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PackageUID in FilePackage = %s, which does not obey the constraint that the first 16 bytes = %s in the IMFTrackFile represented by ID %s.",
                        packageUID.toString(), Utilities.serializeBytesToHexString(packageUID_first16Bytes_Constrained), packageID.toString()));
            }

            int numEssenceTracks = 0;
            MXFDataDefinition filePackageMxfDataDefinition = null;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (!filePackageMxfDataDefinition.equals(MXFDataDefinition.OTHER))
                {
                    numEssenceTracks++;
                }
                //Section 5.1.7 st2067-2:2016
                if(timelineTrack.getOrigin() != 0){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("The origin property of a timeline track in the IMFTrackFile represented by ID %s is non-zero, only 0 is allowed.", packageID));
                }
            }
            if (numEssenceTracks != 1)
            {
                //Section 5.1.3 of SMPTE st2067-5:2013
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Number of essence tracks in FilePackage %s = %d, which is different from 1, this is invalid in the IMFTrackFile represented by ID %s.",
                        filePackage.getInstanceUID(), numEssenceTracks, packageID.toString()));
            }
        }

        //From st2067-2-2013 section 5.3.4.1, top-level file package shall reference a Wave Audio Essence Descriptor
        //Per st2067-2-2013, section 5.3.4.2, Wave Audio Essence Descriptor shall have 'Channel Assignment' item
        {
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {
                Sequence sequence = timelineTrack.getSequence();
                if (sequence == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                            timelineTrack.getInstanceUID(), packageID.toString()));
                } else {
                    MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND)) {
                        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
                        List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = null;
                        if (subDescriptors.size() > 0) {
                            admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
                        }
                        if (genericDescriptor instanceof WaveAudioEssenceDescriptor && (admAudioMetadataSubDescriptors == null || admAudioMetadataSubDescriptors.size() == 0)) {
                            WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = (WaveAudioEssenceDescriptor) genericDescriptor;
                            if ((waveAudioEssenceDescriptor.getChannelAssignmentUL() == null) ||
                                    (!waveAudioEssenceDescriptor.getChannelAssignmentUL().equals(new MXFUID(IMFConstraints.IMF_CHANNEL_ASSIGNMENT_UL)))) {
                                //Section 5.3.4.2 st2067-2:2016
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("ChannelAssignment UL for WaveAudioEssenceDescriptor = %s is different from %s in the IMFTrackFile represented by ID %s.",
                                        waveAudioEssenceDescriptor.getChannelAssignmentUL(), new MXFUID(IMFConstraints.IMF_CHANNEL_ASSIGNMENT_UL), packageID.toString()));
                            }
                            //RFC-5646 spoken language is a part of the MCALabelSubDescriptor and SoundFieldGroupLabelSubdescriptors according to Section 5.3.6.5 st2067-2:2016 has language around RFC-5646 primary spoken language
                            if (headerPartition.getAudioEssenceSpokenLanguage() == null) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s does not have a RFC5646 spoken language indicated, language code shall be set in the SoundFieldGroupLabelSubDescriptor, unless the AudioEssence does not have a primary spoken language.", packageID.toString()));
                            } else {
                                //Section 6.3.6 st377-4:2012
                                if (!IMFConstraints.isSpokenLanguageRFC5646Compliant(headerPartition.getAudioEssenceSpokenLanguage())) {
                                    List<String> strings = IMFConstraints.getPrimarySpokenLanguageUnicodeString(headerPartition.getAudioEssenceSpokenLanguage());
                                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Language Code (%s) in SoundFieldGroupLabelSubdescriptor in the IMFTrackfile represented by ID %s is not RFC5646 compliant", strings, packageID.toString())));
                                }
                            }

                            //Section 5.3.3 st2067-2:2016 and Section 10 st0382:2007
                            if (!StructuralMetadata.isAudioWaveClipWrapped(waveAudioEssenceDescriptor.getEssenceContainerUL().getULAsBytes()[14])) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("WaveAudioEssenceDescriptor indicates that the Audio Essence within an Audio Track File is not Wave Clip-Wrapped in the IMFTrackFile represented by ID %s.", packageID.toString()));
                            }
                            //List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors(); Moved to top!
                            //Section 5.3.6.2 st2067-2:2016
                            if (subDescriptors.size() == 0) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                        String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s indicates a channel count of %d, however there are %d AudioChannelLabelSubdescriptors, every audio channel should refer to exactly one AudioChannelLabelSubDescriptor and vice versa.", packageID.toString(), waveAudioEssenceDescriptor.getChannelCount(), subDescriptors.size()));
                            } else {
                                //Section 5.3.6.2 st2067-2:2016
                                Map<Long, AudioChannelLabelSubDescriptor> audioChannelLabelSubDescriptorMap = headerPartition.getAudioChannelIDToMCASubDescriptorMap();
                                if (waveAudioEssenceDescriptor.getChannelCount() == 0 || waveAudioEssenceDescriptor.getChannelCount() != audioChannelLabelSubDescriptorMap.size()) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                            String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s indicates a channel count of %d, however there are %d AudioChannelLabelSubdescriptors, every audio channel should refer to exactly one AudioChannelLabelSubDescriptor and vice versa.", packageID.toString(), waveAudioEssenceDescriptor.getChannelCount(), audioChannelLabelSubDescriptorMap.size()));
                                }
                                for (Long channelID = 1L; channelID <= waveAudioEssenceDescriptor.getChannelCount(); channelID++) {
                                    //Section 5.3.6.5 st2067-2:2016
                                    if (!audioChannelLabelSubDescriptorMap.containsKey(channelID)) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                                String.format("AudioChannelLabelSubdescriptor missing for ChannelID %d, in the IMFTrackFile represented by ID %s", channelID, packageID.toString()));
                                    }
                                }
                                List<InterchangeObject.InterchangeObjectBO> soundFieldGroupLabelSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(SoundFieldGroupLabelSubDescriptor.class)).collect(Collectors.toList());
                                //Section 5.3.6.3 st2067-2:2016
                                if (soundFieldGroupLabelSubDescriptors.size() != 1) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                            String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s refers to %d SoundFieldGroupLabelSubDescriptors exactly 1 is required", packageID.toString(), soundFieldGroupLabelSubDescriptors.size()));
                                } else {
                                    SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO soundFieldGroupLabelSubDescriptorBO = SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO.class.cast(soundFieldGroupLabelSubDescriptors.get(0));
                                    //Section 5.3.6.5 st2067-2:2016
                                    if ((soundFieldGroupLabelSubDescriptorBO.getMCATitle() == null || soundFieldGroupLabelSubDescriptorBO.getMCATitle().isEmpty())
                                            || (soundFieldGroupLabelSubDescriptorBO.getMCATitleVersion() == null || soundFieldGroupLabelSubDescriptorBO.getMCATitleVersion().isEmpty())
                                            || (soundFieldGroupLabelSubDescriptorBO.getMCAAudioContentKind() == null || soundFieldGroupLabelSubDescriptorBO.getMCAAudioContentKind().isEmpty())
                                            || (soundFieldGroupLabelSubDescriptorBO.getMCAAudioElementKind() == null || soundFieldGroupLabelSubDescriptorBO.getMCAAudioElementKind().isEmpty())) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                                String.format("WaveAudioEssenceDescriptor in the IMFTrackFile represented by ID %s refers to a SoundFieldGroupLabelSubDescriptor that is missing one/all of MCATitle, MCATitleVersion, MCAAudioContentKind, MCAAudioElementKind, %n%s.", packageID.toString(), soundFieldGroupLabelSubDescriptorBO.toString()));
                                    }
                                    SoundFieldGroupLabelSubDescriptor soundFieldGroupLabelSubDescriptor = (SoundFieldGroupLabelSubDescriptor) headerPartition.getSoundFieldGroupLabelSubDescriptors()
                                            .get(0);
                                    List<InterchangeObject> audioChannelLabelSubDescriptors = headerPartition.getAudioChannelLabelSubDescriptors();
                                    //Section 6.3.2 st377-4:2012
                                    if (soundFieldGroupLabelSubDescriptor.getMCALinkId() == null) {
                                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                                String.format("SoundFieldGroupLabelSubDescriptor is missing MCALinkID, in the IMFTrackFile represented by ID %s",
                                                        packageID.toString()));
                                    } else {
                                        for (InterchangeObject interchangeObject : audioChannelLabelSubDescriptors) {
                                            AudioChannelLabelSubDescriptor audioChannelLabelSubDescriptor = AudioChannelLabelSubDescriptor.class.cast(interchangeObject);
                                            //Section 5.3.6.3 st2067-2:2016
                                            if (audioChannelLabelSubDescriptor.getSoundfieldGroupLinkId() == null) {
                                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
                                                        String.format("Audio channel with MCALinkID %s is missing SoundfieldGroupLinkId, in the IMFTrackFile represented by ID %s",
                                                                audioChannelLabelSubDescriptor.getMCALinkId() != null ? audioChannelLabelSubDescriptor.getMCALinkId().toString() : "",
                                                                packageID.toString()));
                                            }
                                            //Section 6.3.2 st377-4:2012
                                            else if (!audioChannelLabelSubDescriptor.getSoundfieldGroupLinkId()
                                                    .equals(soundFieldGroupLabelSubDescriptor.getMCALinkId())) {
                                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX +
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
//                        else {//Section 5.3.4.1 st2067-2:2016
//                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Header Partition does not have a WaveAudioEssenceDescriptor set in the IMFTrackFile represented by ID %s", packageID.toString()));
//                        }
                    } else if (filePackageMxfDataDefinition.equals(MXFDataDefinition.DATA)) {
                        if (genericDescriptor instanceof TimedTextDescriptor) {
                            TimedTextDescriptor timedTextDescriptor = TimedTextDescriptor.class.cast(genericDescriptor);

                            //Section 6.8 st2067-2:2016 and  st0 429-5 section 7
                            if (timedTextDescriptor.getEssenceContainerUL().getULAsBytes()[13] != 0x13) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints
                                        .IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid Mapping Kind in TimedText essence container UL within IMFTrackFile represented by ID %s.",
                                        packageID.toString()));
                            }

                            //https://www.w3.org/TR/ttml-imsc1/ Section 6.1
                            if (!timedTextDescriptor.getUCSEncoding().equalsIgnoreCase("UTF-8")) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints
                                        .IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid UCSEncoding(%s) in TimedTextDescriptor within trackFile represented by ID %s. Only UTF-8 is valid UCSEncoding. ",
                                        timedTextDescriptor
                                        .getUCSEncoding(),
                                        packageID.toString()));
                            }
                            //https://www.w3.org/TR/ttml-imsc1/ Section 6.3
                            if (!timedTextDescriptor.getNamespaceURI().equals(IMSC1TextProfileDesignator) && !timedTextDescriptor.getNamespaceURI().equals(IMSC1ImageProfileDesignator)) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints
                                        .IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid NamespaceURI(%s) in TimedTextDescriptor within trackFile represented by ID %s. Valid NamespaceURIs: " +
                                        "{%s}, {%s}",
                                        timedTextDescriptor.getNamespaceURI(), packageID.toString(), IMSC1TextProfileDesignator, IMSC1ImageProfileDesignator));
                            }
                            for(TimeTextResourceSubDescriptor textResourceSubDescriptor : timedTextDescriptor.getSubDescriptorList()) {
                                //Section 5.4.5 and 5.4.6 st2067-2:2016
                                if (!textResourceSubDescriptor.getMimeMediaType().equals(IMSC1ImageResourceMimeMediaType) && !textResourceSubDescriptor.getMimeMediaType().equals
                                        (IMSC1FontResourceMimeMediaType)) {
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMFConstraints
                                            .IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid MIMEMediaType(%s) in TimedTextResourceSubDescriptor within trackFile represented by ID %s. Valid " +
                                            "MIMEMediaTypes: {%s} {%s}",
                                            timedTextDescriptor.getNamespaceURI(), packageID.toString(), IMSC1ImageResourceMimeMediaType, IMSC1FontResourceMimeMediaType));
                                }
                            }
                        }
                    }
                }
            }
            //TODO: data essence core constraints check st 2067-2, 2067-5 and 429-5
            //TODO: check for data essence clip wrap


        }

        if(imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors())){
            throw new IMFException(String.format("Found fatal errors in the in the IMFTrackFile represented by ID %s that violate the IMF Core constraints.", packageID.toString()), imfErrorLogger);
        }
        return new HeaderPartitionIMF(headerPartitionOP1A);
    }

    /**
     * Checks the compliance of partition packs found in an MXF file with st2067-5:2013. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param partitionPacks the list of partition packs for which the compliance check is performed
     * @param imfErrorLogger - an object for logging errors
     */
    @SuppressWarnings({"PMD.CollapsibleIfStatements"})
    public static void checkIMFCompliance(List<PartitionPack> partitionPacks, IMFErrorLogger imfErrorLogger)
    {
        int previousNumberOfErrors = imfErrorLogger.getErrors().size();

        //From st2067-5-2013 section 5.1.5, a partition shall only have one of header metadata, essence, index table
        for (PartitionPack partitionPack : partitionPacks)
        {
            if (partitionPack.hasHeaderMetadata())
            {
                if (partitionPack.hasEssenceContainer() || partitionPack.hasIndexTableSegments())
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : header metadata = true, essenceContainerData = %s, indexTableSegment = %s, a partition shall have only one of header metadata, essence or index table.",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasEssenceContainer(), partitionPack.hasIndexTableSegments()));
                }
            }
            else if (partitionPack.hasEssenceContainer())
            {
                if (partitionPack.hasHeaderMetadata() || partitionPack.hasIndexTableSegments())
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : essenceContainerData = true, header metadata = %s, indexTableSegment = %s, a partition shall have only one of header metadata, essence or index table.",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasHeaderMetadata(), partitionPack.hasIndexTableSegments()));
                }
            }
            else if (partitionPack.hasIndexTableSegments())
            {
                if (partitionPack.hasEssenceContainer() || partitionPack.hasHeaderMetadata())
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : indexTableSegment = true, essenceContainerData = %s, header metadata = %s, a partition shall have only one of header metadata, essence or index table.",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasEssenceContainer(), partitionPack.hasHeaderMetadata()));
                }
            }
        }
        if(imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors())){
            throw new MXFException(String.format("Found fatal errors in the IMFTrackFile that violate the IMF Core constraints"), imfErrorLogger);
        }
    }

    /**
     * Checks if an MXF file containing audio essence is "clip-wrapped" (see st379:2009, st379-2:2010). A
     * runtime exception is thrown in case the MXF file contains audio essence that is not clip-wrapped
     * This method does nothing if the MXF file does not contain audio essence
     *
     * @param headerPartition the header partition
     * @param partitionPacks the partition packs
     */
    public static void checkIMFCompliance(HeaderPartition headerPartition, List<PartitionPack> partitionPacks)
    {
        Preface preface = headerPartition.getPreface();
        MXFDataDefinition filePackageMxfDataDefinition = null;
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage)genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();
        //get essence type
        {

            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                if(sequence == null){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                            timelineTrack.getInstanceUID(), packageID.toString()));
                }
                else if (!sequence.getMxfDataDefinition().equals(MXFDataDefinition.OTHER))
                {
                    filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                }
            }
        }

        //check if audio essence is clip-wrapped
        if (filePackageMxfDataDefinition != null
                && filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
        {
            int numPartitionsWithEssence = 0;
            for (PartitionPack partitionPack : partitionPacks)
            {
                if (partitionPack.hasEssenceContainer())
                {
                    numPartitionsWithEssence++;
                }
            }
            //Section 5.1.5 st2067-5:2013
            if (numPartitionsWithEssence != 1)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Number of partitions with essence = %d in MXF file with data definition = %s, which is different from 1 in the IMFTrackFile represented by ID %s.",
                        numPartitionsWithEssence, filePackageMxfDataDefinition, packageID.toString()));
            }
        }
        if(imfErrorLogger.hasFatalErrors()){
            throw new MXFException(String.format("Found fatal errors in the IMFTrackFile represented by ID %s that violate the IMF Core constraints.", packageID.toString()), imfErrorLogger);
        }
    }

    /**
     * This class wraps an OP1A-conformant MXF header partition object - wrapping can be done
     * only if the header partition object is also compliant with st2067-5:2013
     */
    public static class HeaderPartitionIMF
    {
        private final MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A;
        private final IMFErrorLogger imfErrorLogger;
        private HeaderPartitionIMF(MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A)
        {
            this.headerPartitionOP1A = headerPartitionOP1A;
            imfErrorLogger = new IMFErrorLoggerImpl();
        }

        /**
         * Gets the wrapped OP1A-conformant header partition object
         * @return returns a OP1A-conformant header partition
         */
        public MXFOperationalPattern1A.HeaderPartitionOP1A getHeaderPartitionOP1A()
        {
            return this.headerPartitionOP1A;
        }

        public boolean hasMatchingEssence(HeaderPartition.EssenceTypeEnum essenceType)
        {
            MXFDataDefinition targetMXFDataDefinition;
            if (essenceType.equals(HeaderPartition.EssenceTypeEnum.MainImageEssence))
            {
                targetMXFDataDefinition = MXFDataDefinition.PICTURE;
            }
            else if(essenceType.equals(HeaderPartition.EssenceTypeEnum.MainAudioEssence))
            {
                targetMXFDataDefinition = MXFDataDefinition.SOUND;
            }
            else if(essenceType.equals(HeaderPartition.EssenceTypeEnum.IABEssence))
            {
                targetMXFDataDefinition = MXFDataDefinition.SOUND;
            }
            else if(essenceType.equals(HeaderPartition.EssenceTypeEnum.MGASADMEssence))
            {
                targetMXFDataDefinition = MXFDataDefinition.SOUND;
            }
            else{
                targetMXFDataDefinition = MXFDataDefinition.DATA;
            }

            GenericPackage genericPackage = this.headerPartitionOP1A.getHeaderPartition().getPreface().getContentStorage().
                    getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;

            boolean hasMatchingEssence = false;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (filePackageMxfDataDefinition.equals(targetMXFDataDefinition))
                {
                    hasMatchingEssence = true;
                }
            }

            return hasMatchingEssence;

        }

        private boolean hasWaveAudioEssenceDescriptor()
        {
            GenericPackage genericPackage = this.headerPartitionOP1A.getHeaderPartition().getPreface().getContentStorage().
                    getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;

            boolean hasWaveAudioEssenceDescriptor = false;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
                {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof WaveAudioEssenceDescriptor)
                    {
                        hasWaveAudioEssenceDescriptor = true;
                        break;
                    }
                }
            }

            return hasWaveAudioEssenceDescriptor;
        }

        /**
         * Gets the first WaveAudioEssenceDescriptor (st382:2007) structural metadata set from
         * an OP1A-conformant MXF Header partition. Returns null if none is found
         * @return returns the first WaveAudioEssenceDescriptor
         */
        public @Nullable WaveAudioEssenceDescriptor getWaveAudioEssenceDescriptor()
        {
            if (!hasWaveAudioEssenceDescriptor())
            {
                return null;
            }

            WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = null;
            GenericPackage genericPackage = this.headerPartitionOP1A.getHeaderPartition().getPreface().getContentStorage().
                    getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
                {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof WaveAudioEssenceDescriptor)
                    {
                        waveAudioEssenceDescriptor = (WaveAudioEssenceDescriptor)genericDescriptor;
                        break;
                    }
                }
            }

            return waveAudioEssenceDescriptor;
        }

        /**
         * Gets the first IABEssenceDescriptor structural metadata set from
         * an OP1A-conformant MXF Header partition. Returns null if none is found
         * @return returns the first IABEssenceDescriptor
         */
        public @Nullable IABEssenceDescriptor getIABEssenceDescriptor()
        {
            IABEssenceDescriptor iabEssenceDescriptor = null;
            GenericPackage genericPackage = this.headerPartitionOP1A.getHeaderPartition().getPreface().getContentStorage().
                    getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
                {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof IABEssenceDescriptor)
                    {
                        iabEssenceDescriptor = (IABEssenceDescriptor)genericDescriptor;
                        break;
                    }
                }
            }

            return iabEssenceDescriptor;
        }

        /**
         * Gets the first MGASoundEssenceDescriptor structural metadata set from
         * an OP1A-conformant MXF Header partition. Returns null if none is found
         * @return returns the first MGASoundEssenceDescriptor
         */
        public @Nullable MGASoundEssenceDescriptor getMGASoundEssenceDescriptor()
        {
            MGASoundEssenceDescriptor mgaSoundEssenceDescriptor = null;
            GenericPackage genericPackage = this.headerPartitionOP1A.getHeaderPartition().getPreface().getContentStorage().
                    getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
                {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof MGASoundEssenceDescriptor)
                    {
                        mgaSoundEssenceDescriptor = (MGASoundEssenceDescriptor)genericDescriptor;
                        break;
                    }
                }
            }

            return mgaSoundEssenceDescriptor;
        }

        /**
         * A method that returns the IMF Essence Component type.
         * @return essenceTypeEnum an enumeration constant corresponding to the IMFEssenceComponent type
         */
        public HeaderPartition.EssenceTypeEnum getEssenceType(){
            HeaderPartition headerPartition = this.headerPartitionOP1A.getHeaderPartition();
            Preface preface = headerPartition.getPreface();
            MXFDataDefinition filePackageMxfDataDefinition = null;

            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;

            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                if (!sequence.getMxfDataDefinition().equals(MXFDataDefinition.OTHER))
                {
                    filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                }
            }
            List<HeaderPartition.EssenceTypeEnum> essenceTypes = headerPartition.getEssenceTypes();
            if(essenceTypes.size() != 1){
                StringBuilder stringBuilder = new StringBuilder();
                for(HeaderPartition.EssenceTypeEnum essenceTypeEnum : essenceTypes){
                    stringBuilder.append(String.format("%s, ", essenceTypeEnum.toString()));
                }
                String message = String.format("IMF constrains MXF essences to mono essences only, however more" +
                        " than one EssenceType was detected %s.", stringBuilder.toString());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                throw new MXFException(message, imfErrorLogger);
            }
            return essenceTypes.get(0);
        }

        /**
         * A method that returns a string representation of a HeaderPartitionIMF object
         *
         * @return string representing the object
         */
        public String toString()
        {
            return this.headerPartitionOP1A.toString();
        }
    }

    /**
     * A method to verify if the spoken language indicated in the SoundFieldGroupLabelSubDescriptor of the WaveAudioPCMDescriptor
     * is RFC-5646 compliant or not
     * @param rfc5646SpokenLanguage the language tag that needs to be verified
     * @return a boolean indicating the result of the verification check
     */
    public static boolean isSpokenLanguageRFC5646Compliant(String rfc5646SpokenLanguage){
        if(rfc5646SpokenLanguage != null){
            Matcher matcher = buildRegExpLangRFC5646().matcher(rfc5646SpokenLanguage);
            return matcher.find();
        }
        return false;
    }

    private static List<String> getPrimarySpokenLanguageUnicodeString(String rfc5646SpokenLanguage){

        Integer asciiStartRange = 0x21;
        Integer asciiEndRange = 0x7e;
        String inputString = rfc5646SpokenLanguage;
        char[] charArray = inputString.toCharArray();
        List<Integer> unicodeCodePoints = new ArrayList<>();
        for (int i=0; i<charArray.length;)
        {
            if (!Character.isHighSurrogate(charArray[i]))
            {
                int unicodeCodePoint = Character.codePointAt(charArray, i, (i+1));
                unicodeCodePoints.add(unicodeCodePoint);
                i++;
            }
            else
            {
                if ((i + 1) < charArray.length)
                {
                    int unicodeCodePoint = Character.codePointAt(charArray, i, (i+2));
                    unicodeCodePoints.add(unicodeCodePoint);
                    i += 2;
                }
                else
                {
                    throw new IllegalArgumentException(String.format("Invalid character in '%s'. Only high surrogate exists", new String(charArray)));
                }
            }
        }

        StringBuilder unicodeString = new StringBuilder();
        unicodeString.append("0x");
        StringBuilder stringBuilder = new StringBuilder();
        for (int unicodeCodePoint : unicodeCodePoints)
        {
            unicodeString.append(String.format("%02x", unicodeCodePoint));
            if(unicodeCodePoint < asciiStartRange
                    || unicodeCodePoint > asciiEndRange){
                stringBuilder.append(".");
            }
            else{
                stringBuilder.append(String.format("%s", String.copyValueOf(Character.toChars(unicodeCodePoint))));
            }
        }
        List<String> strings = new ArrayList<>();
        strings.add(unicodeString.toString());
        strings.add(stringBuilder.toString());
        return strings;
    }

    private static Pattern buildRegExpLangRFC5646()
    {
        String extLang = "([A-Za-z]{3}(-[A-Za-z]{3}){0,2})";
        String language =
                "(([a-zA-Z]{2,3}(-" + extLang + ")?)|([a-zA-Z]{5,8}))";
        String script = "([A-Za-z]{4})";
        String region = "([A-Za-z]{2}|\\d{3})";
        String variant = "([A-Za-z0-9]{5,8}|(\\d[A-Z-a-z0-9]{3}))";
        String singleton = "(\\d|[A-W]|[Y-Z]|[a-w]|[y-z])";
        String extension = "(" + singleton + "(-[A-Za-z0-9]{2,8})+)";
        String privateUse = "(x(-[A-Za-z0-9]{1,8})+)";

        String langTag =
                language + "(-" + script + ")?(-" + region + ")?(-" + variant
                        + ")*(-" + extension + ")*(-" + privateUse + ")?";

        String irregular =
                "((en-GB-oed)|(i-ami)|(i-bnn)|(i-default)|(i-enochian)|(i-hak)|(i-klingon)|(i-lux)|(i-mingo)|(i-navajo)|(i-pwn)|(i-tao)|(i-tay)|(i-tsu)|(sgn-BE-FR)|(sgn-BE-NL)|(sgn-CH-DE))";
        String regular =
                "((art-lojban)|(cel-gaulish)|(no-bok)|(no-nyn)|(zh-guoyu)|(zh-hakka)|(zh-min)|(zh-min-nan)|(zh-xiang))";
        String grandFathered = "(" + irregular + "|" + regular + ")";

        StringBuffer languageTag = new StringBuffer();
        languageTag.append("(^").append(privateUse).append("$)");
        languageTag.append('|');
        languageTag.append("(^").append(grandFathered).append("$)");
        languageTag.append('|');
        languageTag.append("(^").append(langTag).append("$)");

        return Pattern.compile(languageTag.toString());
    }
}
