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
import com.netflix.imflibrary.st0377.header.SoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to MGASoundfieldGroupLabelSubDescriptor structural metadata set defined in ST 2127-1
 */
@Immutable
public final class MGASoundfieldGroupLabelSubDescriptor extends SoundFieldGroupLabelSubDescriptor {

    public static final String MGA_MCA_TAG_SYMBOL = "MGASf";
    public static final String MGA_MCA_TAG_NAME = "MGA Soundfield";
    public static final UL MGA_MCA_LABEL_DICTIONNARY_ID_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.03020222.00000000");
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + MGASoundfieldGroupLabelSubDescriptor.class.getSimpleName() + " : ";
    private final MGASoundfieldGroupLabelSubDescriptorBO mgaSoundfieldLabelSubDescriptorBO;

    /**
     * Constructor for an MGASoundfieldGroupLabelSubDescriptor object
     * @param mgaSoundfieldLabelSubDescriptorBO the parsed MGA Soundfield label sub-descriptor object
     */
    public MGASoundfieldGroupLabelSubDescriptor(MGASoundfieldGroupLabelSubDescriptorBO mgaSoundfieldLabelSubDescriptorBO)
    {
        super(mgaSoundfieldLabelSubDescriptorBO);
        this.mgaSoundfieldLabelSubDescriptorBO = mgaSoundfieldLabelSubDescriptorBO;
    }

    /**
     * A getter for the spoken language in this SubDescriptor
     * @return string representing the spoken language as defined in RFC-5646
     */
    public String getRFC5646SpokenLanguage(){
        return this.mgaSoundfieldLabelSubDescriptorBO.getRFC5646SpokenLanguage();
    }
    /**
     * A method that returns a string representation of an MGASoundfieldGroupLabelSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.mgaSoundfieldLabelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed MGASoundfieldGroupLabelSubDescriptor structural metadata set defined in ST 2127-1
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class MGASoundfieldGroupLabelSubDescriptorBO extends SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO
    {

        @MXFProperty(size=16) private final byte[] mga_metadata_section_link_id = null; //UUID
        @MXFProperty(size=0, charset = "UTF-16") protected final String adm_audio_programme_id = null; //UTF-16 String
        @MXFProperty(size=0, charset = "UTF-16") protected final String adm_audio_content_id = null; //UTF-16 String
        @MXFProperty(size=0, charset = "UTF-16") protected final String adm_audio_object_id = null; //UTF-16 String

        /**
         * Instantiates a new parsed MGASoundfieldGroupLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public MGASoundfieldGroupLabelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header); 
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);
            if (this.mga_metadata_section_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        MGASoundfieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_metadata_section_link_id is null");
            }

        }

        /**
         * Accessor for the adm_audio_programm_id of this MGASoundfieldGroupLabelSubDescriptor
         * @return a byte array representing the adm_audio_programm_id for the MGASoundfieldGroupLabelSubDescriptor
         */
        public String getADMAudioProgrammeId(){
            return this.adm_audio_programme_id;
        }

        /**
         * Accessor for the adm_audio_content_id of this MGASoundfieldGroupLabelSubDescriptor
         * @return a byte array representing the adm_audio_content_id for the MGASoundfieldGroupLabelSubDescriptor
         */
        public String getADMAudioContentId(){
            return this.adm_audio_content_id;
        }

        /**
         * Accessor for the adm_audio_object_id of this MGASoundfieldGroupLabelSubDescriptor
         * @return a byte array representing the adm_audio_object_id for the MGASoundfieldGroupLabelSubDescriptor
         */
        public String getADMAudioObjectId(){
            return this.adm_audio_object_id;
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
            if (this.mga_metadata_section_link_id != null)
            {
                sb.append(String.format("mga_metadata_section_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                        this.mga_metadata_section_link_id[0], this.mga_metadata_section_link_id[1], this.mga_metadata_section_link_id[2], this.mga_metadata_section_link_id[3],
                        this.mga_metadata_section_link_id[4], this.mga_metadata_section_link_id[5], this.mga_metadata_section_link_id[6], this.mga_metadata_section_link_id[7],
                        this.mga_metadata_section_link_id[8], this.mga_metadata_section_link_id[9], this.mga_metadata_section_link_id[10], this.mga_metadata_section_link_id[11],
                        this.mga_metadata_section_link_id[12], this.mga_metadata_section_link_id[13], this.mga_metadata_section_link_id[14], this.mga_metadata_section_link_id[15]));
            }

            return sb.toString();
        }
    }
}
