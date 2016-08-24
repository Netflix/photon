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
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.Sequence;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0377.header.TimelineTrack;
import com.netflix.imflibrary.st0377.header.WaveAudioEssenceDescriptor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * This class consists exclusively of static methods that help verify the compliance of OP1A-conformant
 * (see st378:2004) MXF header partition as well as MXF partition packs (see st377-1:2011)
 * with st2067-5:2013
 */
public final class IMFConstraints
{
    private static final String IMF_ESSENCE_EXCEPTION_PREFIX = "IMF Essence Component check: ";
    private static final byte[] IMF_CHANNEL_ASSIGNMENT_UL = {0x06, 0x0e, 0x2b, 0x34, 0x04, 0x01, 0x01, 0x0d, 0x04, 0x02, 0x02, 0x10, 0x04, 0x01, 0x00, 0x00};

//to prevent instantiation
    private IMFConstraints()
    {}

    /**
     * Checks the compliance of an OP1A-conformant header partition with st2067-5:2013. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param headerPartitionOP1A the OP1A-conformant header partition
     * @return the same header partition wrapped in a HeaderPartitionIMF object
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static HeaderPartitionIMF checkIMFCompliance(MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A)
    {

        HeaderPartition headerPartition = headerPartitionOP1A.getHeaderPartition();

        Preface preface = headerPartition.getPreface();
        //check 'Operational Pattern' field in Preface
        byte[] bytes = preface.getOperationalPattern().getULAsBytes();
        if (OperationalPatternHelper.getPackageComplexity(bytes) != OperationalPatternHelper.PackageComplexity.SinglePackage)
        {
            throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Lower four bits of Operational Pattern qualifier byte = 0x%x",
                    bytes[14]));
        }

        //From 2067-5 Section 5.1.1#13, primary package identifier for Preface shall be set to the top-level file package
        if ((preface.getPrimaryPackage() == null) || (!preface.getPrimaryPackage().equals(preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage())))
        {
            throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("primary package identifier for Preface is not set to the top-level file package"));
        }

        SourcePackage filePackage;
        //From st2067-5-2013 section 5.1.3, only one essence track shall exist in the file package
        {
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            filePackage = (SourcePackage)genericPackage;

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
            }
            if (numEssenceTracks != 1)
            {
                throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Number of essence tracks in FilePackage %s = %d, which is different from 1",
                        filePackage.getInstanceUID(), numEssenceTracks));
            }
        }

        //From st2067-2-2013 section 5.3.4.1, top-level file package shall reference a Wave Audio Essence Descriptor
        //Per st2067-2-2013, section 5.3.4.2, Wave Audio Essence Descriptor shall have 'Channel Assignment' item
        {
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                MXFDataDefinition filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
                {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof WaveAudioEssenceDescriptor)
                    {
                        WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = (WaveAudioEssenceDescriptor) genericDescriptor;
//                        if ((waveAudioEssenceDescriptor.getChannelAssignmentUL() == null) ||
//                                (!waveAudioEssenceDescriptor.getChannelAssignmentUL().equals(new MXFUid(IMFConstraints.IMF_CHANNEL_ASSIGNMENT_UL))))
//                        {
//                            throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("ChannelAssignment UL for WaveAudioEssenceDescriptor = %s is different from %s",
//                                    waveAudioEssenceDescriptor.getChannelAssignmentUL(), new MXFUid(IMFConstraints.IMF_CHANNEL_ASSIGNMENT_UL)));
//                        }
                        //TODO: Enable following check once we have assets that adhere to the specification that the RFC5646 spoken language tag is present in the SoundFieldGroupLabelSubDescriptor and/or AudioChannelLableSubDescriptor
                        /*
                        if(headerPartition.getAudioEssenceSpokenLanguage() == null){
                            throw new MXFException((IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + "WaveAudioEssenceDescriptor does not have a RFC5646 spoken language indicated"));
                        }
                        */
                    }
                    else
                    {
                        throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + "Header Partition does not have a WaveAudioEssenceDescriptor set");
                    }
                }
            }
        }

        return new HeaderPartitionIMF(headerPartitionOP1A);
    }

    /**
     * Checks the compliance of partition packs found in an MXF file with st2067-5:2013. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param partitionPacks the list of partition packs for which the compliance check is performed
     */
    @SuppressWarnings({"PMD.CollapsibleIfStatements"})
    public static void checkIMFCompliance(List<PartitionPack> partitionPacks)
    {
        //From st2067-5-2013 section 5.1.5, a partition shall only have one of header metadata, essence, index table
        for (PartitionPack partitionPack : partitionPacks)
        {
            if (partitionPack.hasHeaderMetadata())
            {
                if (partitionPack.hasEssenceContainer() || partitionPack.hasIndexTableSegments())
                {
                    throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : header metadata = true, essenceContainerData = %s, indexTableSegment = %s",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasEssenceContainer(), partitionPack.hasIndexTableSegments()));
                }
            }
            else if (partitionPack.hasEssenceContainer())
            {
                if (partitionPack.hasHeaderMetadata() || partitionPack.hasIndexTableSegments())
                {
                    throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : essenceContainerData = true, header metadata = %s, indexTableSegment = %s",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasHeaderMetadata(), partitionPack.hasIndexTableSegments()));
                }
            }
            else if (partitionPack.hasIndexTableSegments())
            {
                if (partitionPack.hasEssenceContainer() || partitionPack.hasHeaderMetadata())
                {
                    throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : indexTableSegment = true, essenceContainerData = %s, header metadata = %s",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasEssenceContainer(), partitionPack.hasHeaderMetadata()));
                }
            }
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
        //get essence type
        {
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
        }

        //check if audio essence is clip-wrapped
        if (filePackageMxfDataDefinition.equals(MXFDataDefinition.SOUND))
        {
            int numPartitionsWithEssence = 0;
            for (PartitionPack partitionPack : partitionPacks)
            {
                if (partitionPack.hasEssenceContainer())
                {
                    numPartitionsWithEssence++;
                }
            }
            if (numPartitionsWithEssence != 1)
            {
                throw new MXFException(IMFConstraints.IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Number of partitions with essence = %d in MXF file with data definition = %s, which is different from 1",
                        numPartitionsWithEssence, filePackageMxfDataDefinition));
            }
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
                throw new IMFException(message, imfErrorLogger);
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
}
