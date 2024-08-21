/*
 *
 * Copyright 2024 RheinMain University of Applied Sciences, Wiesbaden, Germany.
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

package com.netflix.imflibrary.st2067_203;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.SubDescriptor;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0377.header.InterchangeObject.InterchangeObjectBO.StrongRef;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to SADMAudioMetadataSubDescriptor structural metadata set defined in ST 2127-1
 */
@Immutable
public final class SADMAudioMetadataSubDescriptor extends SubDescriptor {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + SADMAudioMetadataSubDescriptor.class.getSimpleName() + " : ";
    private final SADMAudioMetadataSubDescriptorBO sadmAudioMetadataSubDescriptorBO;

    /**
     * Constructor for an SADMAudioMetadataSubDescriptor object
     * @param sadmAudioMetadataSubDescriptorBO the parsed MGA Soundfield label sub-descriptor object
     */
    public SADMAudioMetadataSubDescriptor(SADMAudioMetadataSubDescriptorBO sadmAudioMetadataSubDescriptorBO)
    {
        super();
        this.sadmAudioMetadataSubDescriptorBO = sadmAudioMetadataSubDescriptorBO;
    }
    /**
     * A method that returns a string representation of an SADMAudioMetadataSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.sadmAudioMetadataSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed SADMAudioMetadataSubDescriptor structural metadata set defined in st20167-201:201x
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class SADMAudioMetadataSubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {
        @MXFProperty(size=16) private final byte[] sadm_metadata_section_link_id = null; //UUID
        @MXFProperty(size=0) private final CompoundDataTypes.MXFCollections.MXFCollection<UL> sadm_profile_level_batch = null;

        /**
         * Instantiates a new parsed SADMAudioMetadataSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public SADMAudioMetadataSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.sadm_metadata_section_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SADMAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "sadm_metadata_section_link_id is null");
            }
        }

        /**
         * Accessor for the mga_link_id of this SADMAudioMetadataSubDescriptor
         * @return a byte array representing the mga_link_id for the SADMAudioMetadataSubDescriptor
         */
        public byte[] getSADMMetadataSectionLinkId(){
            return this.sadm_metadata_section_link_id.clone();
        }

        /**
         * Accessor for the adm_profile_level_batch of this ADMAudioMetadataSubDescriptor
         * @return a collection representing the adm_profile_level_batch for the ADMAudioMetadataSubDescriptor
         */
        public  CompoundDataTypes.MXFCollections.MXFCollection<UL>  getSADMProfileLevelULBatch(){
            return this.sadm_profile_level_batch;
        }

        /**
         * A method that returns a string representation of the object.
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (this.sadm_metadata_section_link_id != null)
            {
                sb.append(String.format("sadm_metadata_section_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                        this.sadm_metadata_section_link_id[0], this.sadm_metadata_section_link_id[1], this.sadm_metadata_section_link_id[2], this.sadm_metadata_section_link_id[3],
                        this.sadm_metadata_section_link_id[4], this.sadm_metadata_section_link_id[5], this.sadm_metadata_section_link_id[6], this.sadm_metadata_section_link_id[7],
                        this.sadm_metadata_section_link_id[8], this.sadm_metadata_section_link_id[9], this.sadm_metadata_section_link_id[10], this.sadm_metadata_section_link_id[11],
                        this.sadm_metadata_section_link_id[12], this.sadm_metadata_section_link_id[13], this.sadm_metadata_section_link_id[14], this.sadm_metadata_section_link_id[15]));
            }

            return sb.toString();
        }
    }
}
