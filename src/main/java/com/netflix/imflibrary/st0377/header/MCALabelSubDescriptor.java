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
import com.netflix.imflibrary.annotations.MXFField;

/**
 * Object model corresponding to MultiChannelAudioLabelSubDescriptor structural metadata set defined in st377-4:2012
 */
public abstract class MCALabelSubDescriptor extends SubDescriptor {

    /**
     * Object corresponding to a parsed MultiChannelAudioLabelSubDescriptor structural metadata set defined in st377-4:2012
     */
    public static abstract class MCALabelSubDescriptorBO extends SubDescriptorBO{

        @MXFField(size=16) protected final UL mca_label_dictionary_id = null;
        @MXFField(size=16) protected final byte[] mca_link_id = null; //UUID type
        @MXFField(size=0, charset = "UTF-16") protected final String mca_tag_symbol = null; //UTF-16 String
        @MXFField(size=0, charset = "UTF-16") protected final String mca_tag_name = null; //UTF-16 String
        @MXFField(size=4) protected final Long mca_channel_id = null;
        @MXFField(size=0, charset="ISO-8859-1") protected final String rfc_5646_spoken_language = null; //ISO-8 String
        @MXFField(size=0, charset="UTF-16") protected final String mca_title = null;
        @MXFField(size=0, charset="UTF-16") protected final String mca_title_version = null;
        @MXFField(size=0, charset="UTF-16") protected final String mca_audio_content_kind = null;
        @MXFField(size=0, charset="UTF-16") protected final String mca_audio_element_kind = null;

        /**
         * Constructor for a parsed MCA Label Sub descriptor object
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        MCALabelSubDescriptorBO(final KLVPacket.Header header) {
            super(header);
        }
    }
}
