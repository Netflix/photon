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

package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.SubDescriptor;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to the IABChannelSubDescriptor structural metadata set defined in
 * Annex E of SMPTE ST 2067-201:2026. The sub-descriptor replicates, at the MXF level, the per-channel
 * metadata of the BedDefinition structures carried inside the Immersive Audio Bitstream, so that MXF
 * parsers can expose it without decoding the bitstream.
 */
@Immutable
public final class IABChannelSubDescriptor extends SubDescriptor {

    /** Most significant bit of IABAudioDescription. When set, IABAudioDescriptionText is present (Annex E.2.5). */
    private static final short IAB_AUDIO_DESCRIPTION_TEXT_PRESENT_MASK = (short) 0x80;

    private final IABChannelSubDescriptorBO iabChannelSubDescriptorBO;

    /**
     * Constructor for an IABChannelSubDescriptor object
     * @param iabChannelSubDescriptorBO the parsed IAB Channel sub-descriptor object
     */
    public IABChannelSubDescriptor(IABChannelSubDescriptorBO iabChannelSubDescriptorBO)
    {
        super();
        this.iabChannelSubDescriptorBO = iabChannelSubDescriptorBO;
    }

    /**
     * A method that returns a string representation of an IABChannelSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.iabChannelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed IABChannelSubDescriptor structural metadata set defined in
     * Annex E of SMPTE ST 2067-201:2026.
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class IABChannelSubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {
        @MXFProperty(size=4) private final Long iab_bed_meta_id = null; //Uint32, IAB MetaID of the associated BedDefinition
        @MXFProperty(size=4) private final Long iab_channel_id = null; //Uint32, channel within a bed and its routing destination
        @MXFProperty(size=1) private final Short iab_audio_description = null; //Uint8, top-level audio description
        @MXFProperty(size=0, charset="UTF-16") private final String iab_audio_description_text = null; //UTF-16 String, optional

        private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + IABChannelSubDescriptor.class.getSimpleName() + " : ";

        /**
         * Instantiates a new parsed IABChannelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public IABChannelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABChannelSubDescriptorBO.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.iab_bed_meta_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABChannelSubDescriptorBO.ERROR_DESCRIPTION_PREFIX + "iab_bed_meta_id is null");
            }

            if (this.iab_channel_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABChannelSubDescriptorBO.ERROR_DESCRIPTION_PREFIX + "iab_channel_id is null");
            }

            if (this.iab_audio_description == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABChannelSubDescriptorBO.ERROR_DESCRIPTION_PREFIX + "iab_audio_description is null");
            }
        }

        /**
         * Accessor for the IABBedMetaID of this IABChannelSubDescriptor
         * @return the IAB MetaID of the associated BedDefinition, or null if absent
         */
        public Long getIABBedMetaID(){
            return this.iab_bed_meta_id;
        }

        /**
         * Accessor for the IABChannelID of this IABChannelSubDescriptor
         * @return the channel id within the bed, or null if absent
         */
        public Long getIABChannelID(){
            return this.iab_channel_id;
        }

        /**
         * Accessor for the IABAudioDescription of this IABChannelSubDescriptor
         * @return the top-level audio description, or null if absent
         */
        public Short getIABAudioDescription(){
            return this.iab_audio_description;
        }

        /**
         * Accessor for the IABAudioDescriptionText of this IABChannelSubDescriptor
         * @return the custom audio description text, or null if absent
         */
        public String getIABAudioDescriptionText(){
            return this.iab_audio_description_text;
        }

        /**
         * Indicates whether IABAudioDescriptionText is expected to be present, i.e. whether the most
         * significant bit of IABAudioDescription is set (Annex E.2.5 of SMPTE ST 2067-201:2026).
         * @return true if the IABAudioDescriptionText item is required to be present
         */
        boolean isAudioDescriptionTextExpected(){
            return this.iab_audio_description != null
                    && (this.iab_audio_description & IAB_AUDIO_DESCRIPTION_TEXT_PRESENT_MASK) != 0;
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
            if (this.iab_bed_meta_id != null)
            {
                sb.append(String.format("iab_bed_meta_id = %d%n", this.iab_bed_meta_id));
            }
            if (this.iab_channel_id != null)
            {
                sb.append(String.format("iab_channel_id = %d%n", this.iab_channel_id));
            }
            if (this.iab_audio_description != null)
            {
                sb.append(String.format("iab_audio_description = %d%n", this.iab_audio_description));
            }
            if (this.iab_audio_description_text != null)
            {
                sb.append(String.format("iab_audio_description_text = %s%n", this.iab_audio_description_text));
            }
            return sb.toString();
        }
    }
}
