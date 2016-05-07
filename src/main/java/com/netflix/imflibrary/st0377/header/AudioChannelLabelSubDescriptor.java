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
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to AudioChannelLabelSubDescriptor structural metadata set defined in st377-4:2012
 */
@Immutable
public final class AudioChannelLabelSubDescriptor extends GenericDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + AudioChannelLabelSubDescriptor.class.getSimpleName() + " : ";
    private final AudioChannelLabelSubDescriptorBO audioChannelLabelSubDescriptorBO;

    /**
     * Constructor for an AudioChannelLabelSubDescriptor object
     * @param audioChannelLabelSubDescriptorBO the parsed Audio channel label sub-descriptor object
     */
    public AudioChannelLabelSubDescriptor(AudioChannelLabelSubDescriptorBO audioChannelLabelSubDescriptorBO)
    {
        this.audioChannelLabelSubDescriptorBO = audioChannelLabelSubDescriptorBO;
    }

    /**
     * Getter for the MCALabelDictionaryId of an AudioChannelLabelSubDescriptor object.
     * @return MCALabelDictionaryId of the AudioChannelLabelSubDescriptor object
     */
    public MXFUID getMCALabelDictionaryId()
    {
        return this.audioChannelLabelSubDescriptorBO.mca_label_dictionary_id.getULAsMXFUid();
    }

    public MXFUID getSoundfieldGroupLinkId()
    {
        return new MXFUID(this.audioChannelLabelSubDescriptorBO.soundfield_group_link_id);
    }

    /**
     * Getter for the MCALinkId field of an AudioChannelLabelSubDescriptor object.
     * @return MCALinkId of the AudioChannelLabelSubDescriptor object
     */

    public MXFUID getMCALinkId()
    {
        return new MXFUID(this.audioChannelLabelSubDescriptorBO.mca_link_id);
    }

    /**
     * A method that returns a string representation of an AudioChannelLabelSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.audioChannelLabelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed AudioChannelLabelSubDescriptor structural metadata set defined in st377-4:2012
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class AudioChannelLabelSubDescriptorBO extends MCALabelSubDescriptor.MCALabelSubDescriptorBO
    {

        @MXFProperty(size=16) private final byte[] soundfield_group_link_id = null; //UUID

        /**
         * Instantiates a new parsed AudioChannelLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public AudioChannelLabelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.mca_label_dictionary_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_label_dictionary_id is null");
            }

            if (this.mca_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_link_id is null");
            }

            if (this.mca_tag_symbol == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_tag_symbol is null");
            }
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
            if (this.soundfield_group_link_id != null)
            {
                sb.append(String.format("soundfield_group_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                        this.soundfield_group_link_id[0], this.soundfield_group_link_id[1], this.soundfield_group_link_id[2], this.soundfield_group_link_id[3],
                        this.soundfield_group_link_id[4], this.soundfield_group_link_id[5], this.soundfield_group_link_id[6], this.soundfield_group_link_id[7],
                        this.soundfield_group_link_id[8], this.soundfield_group_link_id[9], this.soundfield_group_link_id[10], this.soundfield_group_link_id[11],
                        this.soundfield_group_link_id[12], this.soundfield_group_link_id[13], this.soundfield_group_link_id[14], this.soundfield_group_link_id[15]));
            }

            return sb.toString();
        }


    }

}
