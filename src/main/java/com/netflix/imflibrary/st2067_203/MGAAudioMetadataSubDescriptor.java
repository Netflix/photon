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
 * Object model corresponding to MGAAudioMetadataSubDescriptor structural metadata set defined in ST 2127-1
 */
@Immutable
public final class MGAAudioMetadataSubDescriptor extends SubDescriptor {

    public static final String MGA_MCA_TAG_SYMBOL = "MGASf";
    public static final String MGA_MCA_TAG_NAME = "MGA Soundfield";
    public static final UL MGA_MCA_LABEL_DICTIONNARY_ID_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.03020222.00000000");
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + MGAAudioMetadataSubDescriptor.class.getSimpleName() + " : ";
    private final MGAAudioMetadataSubDescriptorBO mgaAudioMetadataSubDescriptorBO;

    /**
     * Constructor for an MGAAudioMetadataSubDescriptor object
     * @param mgaAudioMetadataSubDescriptorBO the parsed MGA Soundfield label sub-descriptor object
     */
    public MGAAudioMetadataSubDescriptor(MGAAudioMetadataSubDescriptorBO mgaAudioMetadataSubDescriptorBO)
    {
        super();
        this.mgaAudioMetadataSubDescriptorBO = mgaAudioMetadataSubDescriptorBO;
    }
    /**
     * A method that returns a string representation of an MGAAudioMetadataSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.mgaAudioMetadataSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed MGAAudioMetadataSubDescriptor structural metadata set defined in st20167-201:201x
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class MGAAudioMetadataSubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {

        @MXFProperty(size=16) private final byte[] mga_link_id = null; //UUID
        @MXFProperty(size=1) private final Short mga_audio_metadata_index = null; //UUID
        @MXFProperty(size=1) private final Short mga_audio_metadata_identifier = null; //UUID
        @MXFProperty(size=0) private final CompoundDataTypes.MXFCollections.MXFCollection<UL> mga_audio_metadata_payload_ul_array = null;

        /**
         * Instantiates a new parsed MGAAudioMetadataSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public MGAAudioMetadataSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        MGAAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.mga_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        MGAAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_link_id is null");
            }

            if (this.mga_audio_metadata_index == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        MGAAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_audio_metadata_index is null");
            }

            if (this.mga_audio_metadata_identifier == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        MGAAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_audio_metadata_identifier is null");
            }

            if (this.mga_audio_metadata_payload_ul_array == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        MGAAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_audio_metadata_payload_ul_array is null");
            }
        }

        /**
         * Accessor for the mga_link_id of this MGAAudioMetadataSubDescriptor
         * @return a byte array representing the mga_link_id for the MGAAudioMetadataSubDescriptor
         */
        public byte[] getMGALinkId(){
            return this.mga_link_id.clone();
        }

        /**
         * Accessor for the mga_audio_metadata_index of this MGAAudioMetadataSubDescriptor
         * @return a short integer representing the mga_audio_metadata_index for the MGAAudioMetadataSubDescriptor
         */
        public Short getMGAAudioMetadataIndex(){
            return this.mga_audio_metadata_index;
        }

        /**
         * Accessor for the mga_audio_metadata_identifier of this MGAAudioMetadataSubDescriptor
         * @return a short integer representing the mga_audio_metadata_identifier for the MGAAudioMetadataSubDescriptor
         */
        public Short getMGAAudioMetadataIdentifier(){
            return this.mga_audio_metadata_identifier;
        }

        /**
         * Accessor for the mga_audio_metadata_payload_ul_array of this MGAAudioMetadataSubDescriptor
         * @return a short integer representing the mga_audio_metadata_payload_ul_array for the MGAAudioMetadataSubDescriptor
         */
        public CompoundDataTypes.MXFCollections.MXFCollection<UL> getMGAAudioMetadataPayloadULArrray(){
            return this.mga_audio_metadata_payload_ul_array;
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
            if (this.mga_link_id != null)
            {
                sb.append(String.format("mga_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                        this.mga_link_id[0], this.mga_link_id[1], this.mga_link_id[2], this.mga_link_id[3],
                        this.mga_link_id[4], this.mga_link_id[5], this.mga_link_id[6], this.mga_link_id[7],
                        this.mga_link_id[8], this.mga_link_id[9], this.mga_link_id[10], this.mga_link_id[11],
                        this.mga_link_id[12], this.mga_link_id[13], this.mga_link_id[14], this.mga_link_id[15]));
            }

            return sb.toString();
        }
    }
}
