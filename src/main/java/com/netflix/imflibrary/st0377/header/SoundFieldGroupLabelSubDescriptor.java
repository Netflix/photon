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
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to SoundFieldGroupLabelSubDescriptor structural metadata set defined in st377-4:2012
 */
@Immutable
public final class SoundFieldGroupLabelSubDescriptor extends SubDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + SoundFieldGroupLabelSubDescriptor.class.getSimpleName() + " : ";
    private final SoundFieldGroupLabelSubDescriptorBO soundFieldGroupLabelSubDescriptorBO;

    /**
     * Constructor for a SoundFieldGroupLabelSubDescriptor object
     * @param soundFieldGroupLabelSubDescriptorBO the parsed SoundFieldGroupLabelSubDescriptor object
     */
    public SoundFieldGroupLabelSubDescriptor(SoundFieldGroupLabelSubDescriptorBO soundFieldGroupLabelSubDescriptorBO)
    {
        this.soundFieldGroupLabelSubDescriptorBO = soundFieldGroupLabelSubDescriptorBO;
    }

    /**
     * Getter for the MCALabelDictionary ID referred by this SoundFieldGroupLabelSubDescriptor object
     * @return the MCALabelDictionary ID referred by this SoundFieldGroupLabelSubDescriptor object
     */
    public MXFUID getMCALabelDictionaryId()
    {
        return this.soundFieldGroupLabelSubDescriptorBO.mca_label_dictionary_id.getULAsMXFUid();
    }

    /**
     * Getter for the MCALink Id that links the instances of the MCALabelSubDescriptors
     * @return the MCALink Id that links the instances of the MCALabelSubDescriptors
     */
    public MXFUID getMCALinkId()
    {
        return new MXFUID(this.soundFieldGroupLabelSubDescriptorBO.mca_link_id);
    }

    /**
     * A getter for the spoken language in this SubDescriptor
     * @return string representing the spoken language as defined in RFC-5646
     */
    public String getRFC5646SpokenLanguage(){
        return this.soundFieldGroupLabelSubDescriptorBO.rfc_5646_spoken_language;
    }

    /**
     * A method that returns a string representation of a SoundFieldGroupLabelSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.soundFieldGroupLabelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed SoundFieldGroupLabelSubDescriptor structural metadata set defined in st377-4:2012
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class SoundFieldGroupLabelSubDescriptorBO extends MCALabelSubDescriptor.MCALabelSubDescriptorBO
    {
        /**
         * Instantiates a new parsed SoundFieldGroupLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public SoundFieldGroupLabelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        SoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.mca_label_dictionary_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        SoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_label_dictionary_id is null");
            }

            if (this.mca_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        SoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_link_id is null");
            }

            if (this.mca_tag_symbol == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        SoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_tag_symbol is null");
            }

        }

        /**
         * A method that returns a string representation of a SoundFieldGroupLabelSubDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== SoundFieldGroupLabelSubDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("mca_label_dictionary_id = %s%n", this.mca_label_dictionary_id.toString()));
            sb.append(String.format("mca_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.mca_link_id[0], this.mca_link_id[1], this.mca_link_id[2], this.mca_link_id[3],
                    this.mca_link_id[4], this.mca_link_id[5], this.mca_link_id[6], this.mca_link_id[7],
                    this.mca_link_id[8], this.mca_link_id[9], this.mca_link_id[10], this.mca_link_id[11],
                    this.mca_link_id[12], this.mca_link_id[13], this.mca_link_id[14], this.mca_link_id[15]));
            sb.append(String.format("mca_tag_symbol = %s%n", this.mca_tag_symbol));
            sb.append(String.format("mca_tag_name = %s%n", this.mca_tag_name));
            sb.append(String.format("mca_channel_id = %d%n", this.mca_channel_id));
            if (this.rfc_5646_spoken_language != null)
            {
                sb.append(String.format("rfc_5646_spoken_language = %s%n", this.rfc_5646_spoken_language));
            }

            return sb.toString();
        }
    }


}
