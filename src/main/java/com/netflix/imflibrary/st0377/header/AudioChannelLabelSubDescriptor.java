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
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.MXFUid;

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
    public MXFUid getMCALabelDictionaryId()
    {
        return this.audioChannelLabelSubDescriptorBO.mca_label_dictionary_id.getULAsMXFUid();
    }

    public MXFUid getSoundfieldGroupLinkId()
    {
        return new MXFUid(this.audioChannelLabelSubDescriptorBO.soundfield_group_link_id);
    }

    /**
     * Getter for the MCALinkId field of an AudioChannelLabelSubDescriptor object.
     * @return MCALinkId of the AudioChannelLabelSubDescriptor object
     */

    public MXFUid getMCALinkId()
    {
        return new MXFUid(this.audioChannelLabelSubDescriptorBO.mca_link_id);
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
    public static final class AudioChannelLabelSubDescriptorBO extends GenericDescriptorBO
    {

        @MXFField(size=16) private final UL mca_label_dictionary_id = null;
        @MXFField(size=16) private final byte[] mca_link_id = null; //UUID
        @MXFField(size=0, charset="UTF-16") private final String mca_tag_symbol = null;
        @MXFField(size=0, charset="UTF-16") private final String mca_tag_name = null;
        @MXFField(size=4) private final Long mca_channel_id = null;
        @MXFField(size=0, charset="ISO-8859-1") private final String rfc_5646_spoken_language = null;
        @MXFField(size=16) private final byte[] soundfield_group_link_id = null; //UUID

        /**
         * Instantiates a new parsed AudioChannelLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public AudioChannelLabelSubDescriptorBO(MXFKLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUid> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.mca_label_dictionary_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_label_dictionary_id is null");
            }

            if (this.mca_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        AudioChannelLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_link_id is null");
            }

            if (this.mca_tag_symbol == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
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
            sb.append("================== AudioChannelLabelSubDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("mca_label_dictionary_id = %s%n",this.mca_label_dictionary_id.toString()));
            sb.append(String.format("mca_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.mca_link_id[0], this.mca_link_id[1], this.mca_link_id[2], this.mca_link_id[3],
                    this.mca_link_id[4], this.mca_link_id[5], this.mca_link_id[6], this.mca_link_id[7],
                    this.mca_link_id[8], this.mca_link_id[9], this.mca_link_id[10], this.mca_link_id[11],
                    this.mca_link_id[12], this.mca_link_id[13], this.mca_link_id[14], this.mca_link_id[15]));
            sb.append(String.format("mca_tag_symbol = %s%n", this.mca_tag_symbol));
            sb.append(String.format("mca_tag_name = %s%n", this.mca_tag_name));
            if (this.soundfield_group_link_id != null)
            {
                sb.append(String.format("soundfield_group_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                        this.soundfield_group_link_id[0], this.soundfield_group_link_id[1], this.soundfield_group_link_id[2], this.soundfield_group_link_id[3],
                        this.soundfield_group_link_id[4], this.soundfield_group_link_id[5], this.soundfield_group_link_id[6], this.soundfield_group_link_id[7],
                        this.soundfield_group_link_id[8], this.soundfield_group_link_id[9], this.soundfield_group_link_id[10], this.soundfield_group_link_id[11],
                        this.soundfield_group_link_id[12], this.soundfield_group_link_id[13], this.soundfield_group_link_id[14], this.soundfield_group_link_id[15]));
            }
            if (this.mca_channel_id != null)
            {
                sb.append(String.format("mca_channel_id = %d%n", this.mca_channel_id));
            }
            if (this.rfc_5646_spoken_language != null)
            {
                sb.append(String.format("rfc_5646_spoken_language = %s%n", this.rfc_5646_spoken_language));
            }

            return sb.toString();
        }


    }

}
