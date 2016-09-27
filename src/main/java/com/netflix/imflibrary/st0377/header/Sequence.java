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

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.MXFDataDefinition;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Object model corresponding to Sequence descriptive metadata set defined in st377-1:2011
 */
@Immutable
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
public final class Sequence extends StructuralComponent
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + Sequence.class.getSimpleName() + " : ";
    private final SequenceBO sequenceBO;
    private final List<StructuralComponent> structuralComponents;

    private final MXFDataDefinition mxfDataDefinition;

    /**
     * Instantiates a new Sequence object
     *
     * @param sequenceBO the parsed Sequence object
     * @param structuralComponents the list of structural components referred by this Sequence object
     */
    public Sequence(SequenceBO sequenceBO, List<StructuralComponent> structuralComponents)
    {
        this.sequenceBO = sequenceBO;
        this.structuralComponents = structuralComponents;
        this.mxfDataDefinition = MXFDataDefinition.getDataDefinition(new MXFUID(this.sequenceBO.data_definition));
    }

    /**
     * Getter for the instance UID corresponding to this Sequence object
     * @return the instance UID corresponding to this Sequence object
     */
    public MXFUID getInstanceUID()
    {
        return new MXFUID(this.sequenceBO.instance_uid);
    }

    /**
     * Getter for the number of structural components referred by this Sequence object
     * @return the number of structural components referred by this Sequence object
     */
    public int getNumberOfStructuralComponents()
    {
        return this.sequenceBO.structural_components.size();
    }

    /**
     * Getter for the subset of structural components that are of type SourceClip
     *
     * @return the source clips
     */
    public List<SourceClip> getSourceClips()
    {
        List<SourceClip> sourceClips = new ArrayList<>();
        for (StructuralComponent structuralComponent : this.structuralComponents)
        {
            if (structuralComponent instanceof SourceClip)
            {
                sourceClips.add((SourceClip)structuralComponent);
            }
        }
        return sourceClips;
    }

    /**
     * Getter for the instance UID of a SourceClip structural component in the list of structural components referred by this Sequence object
     * @param index - the index in the list corresponding to the SourceClip structural component
     * @return the source clips
     */
    public MXFUID getSourceClipUID(int index)
    {
        return this.sequenceBO.structural_components.getEntries().get(index).getInstanceUID();
    }

    /**
     * Getter for the data type of this structural metadata set
     * @return the data type of this structural metadata set
     */
    public MXFDataDefinition getMxfDataDefinition()
    {
        return this.mxfDataDefinition;
    }

    /**
     * Getter for the duration of the Sequence in units of edit rate
     * @return the duration of the Sequence in units of edit rate
     */
    public Long getDuration()
    {
        return this.sequenceBO.duration;
    }

    /**
     * Getter for the list of instance UIDs of structural components referred by this sequence
     * @return the list of instance UIDs of structural components referred by this sequence
     */
    public List<MXFUID> getStructuralComponentInstanceUIDs(){
        return this.sequenceBO.getStructuralComponentInstanceUIDs();
    }

    /**
     * A method that returns a string representation of a Sequence object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.sequenceBO.toString();
    }

    /**
     * Object corresponding to a parsed Sequence descriptive metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class SequenceBO extends StructuralComponentBO
    {

        @MXFProperty(size=0, depends=true) private final CompoundDataTypes.MXFCollections.MXFCollection<StrongRef> structural_components = null;
        private final List<MXFUID> structuralComponentInstanceUIDs = new ArrayList<>();

        /**
         * Instantiates a new parsed Sequence object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public SequenceBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        Sequence.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.structural_components == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        Sequence.ERROR_DESCRIPTION_PREFIX + "structural_components is null");
            }
            else
            {
                for (StrongRef strongRef : this.structural_components.getEntries())
                {
                    structuralComponentInstanceUIDs.add(strongRef.getInstanceUID());
                }

            }

            if (this.data_definition == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        Sequence.ERROR_DESCRIPTION_PREFIX + "data_definition is null");
            }

            if (this.duration == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        Sequence.ERROR_DESCRIPTION_PREFIX + "duration is null");
            }

        }

        /**
         * Getter for an unmodifiable list of instance UIDs of structural components referred by this sequence
         * @return an unmodifiable list of instance UIDs of structural components referred by this sequence
         */
        public List<MXFUID> getStructuralComponentInstanceUIDs()
        {
            return Collections.unmodifiableList(this.structuralComponentInstanceUIDs);
        }

        /**
         * A method that returns a string representation of a SequenceBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== Sequence ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("data_definition = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.data_definition[0], this.data_definition[1], this.data_definition[2], this.data_definition[3],
                    this.data_definition[4], this.data_definition[5], this.data_definition[6], this.data_definition[7],
                    this.data_definition[8], this.data_definition[9], this.data_definition[10], this.data_definition[11],
                    this.data_definition[12], this.data_definition[13], this.data_definition[14], this.data_definition[15]));
            sb.append(String.format("duration = %d%n", this.duration));
            sb.append(this.structural_components.toString());
            return sb.toString();

        }
    }
}
