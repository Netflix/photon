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

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.annotations.MXFProperty;

/**
 * Object model corresponding to MultiChannelAudioLabelSubDescriptor structural metadata set defined in st377-4:2012
 */
public abstract class MCALabelSubDescriptor extends SubDescriptor {

    /**
     * Object corresponding to a parsed MultiChannelAudioLabelSubDescriptor structural metadata set defined in st377-4:2012
     */
    public static abstract class MCALabelSubDescriptorBO extends SubDescriptorBO{

        @MXFProperty(size=16) protected final UL mca_label_dictionary_id = null;
        @MXFProperty(size=16) protected final byte[] mca_link_id = null; //UUID type
        @MXFProperty(size=0, charset = "UTF-16") protected final String mca_tag_symbol = null; //UTF-16 String
        @MXFProperty(size=0, charset = "UTF-16") protected final String mca_tag_name = null; //UTF-16 String
        @MXFProperty(size=4) protected final Long mca_channel_id = null;
        @MXFProperty(size=0, charset="ISO-8859-1") protected final String rfc_5646_spoken_language = null; //ISO-8 String
        @MXFProperty(size=0, charset="UTF-16") protected final String mca_title = null;
        @MXFProperty(size=0, charset="UTF-16") protected final String mca_title_version = null;
        @MXFProperty(size=0, charset="UTF-16") protected final String mca_audio_content_kind = null;
        @MXFProperty(size=0, charset="UTF-16") protected final String mca_audio_element_kind = null;

        /**
         * Constructor for a parsed MCA Label Sub descriptor object
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        MCALabelSubDescriptorBO(final KLVPacket.Header header) {
            super(header);
        }

        /**
         * A method that returns a string representation of a SoundFieldGroupLabelSubDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("================== %s ======================%n", this.getClass().getSimpleName()));
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
            if (this.mca_tag_name != null)
            {
                sb.append(String.format("mca_tag_name = %s%n", this.mca_tag_name));
            }
            if (this.mca_channel_id != null)
            {
                sb.append(String.format("mca_channel_id = %d%n", this.mca_channel_id));
            }
            if (this.rfc_5646_spoken_language != null)
            {
                sb.append(String.format("rfc_5646_spoken_language = %s%n", this.rfc_5646_spoken_language));
            }
            if (this.mca_title != null)
            {
                sb.append(String.format("mca_title = %s%n", this.mca_title));
            }
            if (this.mca_title_version != null)
            {
                sb.append(String.format("mca_title_version = %s%n", this.mca_title_version));
            }
            if (this.mca_audio_content_kind != null)
            {
                sb.append(String.format("mca_audio_content_kind = %s%n", this.mca_audio_content_kind));
            }
            if (this.mca_audio_element_kind != null)
            {
                sb.append(String.format("mca_audio_element_kind = %s%n", this.mca_audio_element_kind));
            }

            return sb.toString();
        }
    }
}
