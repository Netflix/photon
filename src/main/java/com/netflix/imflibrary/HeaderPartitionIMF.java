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

package com.netflix.imflibrary;

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
 * This class wraps an OP1A-conformant MXF header partition object - wrapping can be done
 * only if the header partition object is also compliant with st2067-5:2013
 */
public class HeaderPartitionIMF
{
    private final MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A;
    private final IMFErrorLogger imfErrorLogger;
    /**
     * Wraps an OP1A-conformant header partition. Callers are expected to have verified IMF compliance
     * (see IMFTrackFileStructureConstraints) before wrapping.
     */
    public static HeaderPartitionIMF wrap(MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A)
    {
        return new HeaderPartitionIMF(headerPartitionOP1A);
    }

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
