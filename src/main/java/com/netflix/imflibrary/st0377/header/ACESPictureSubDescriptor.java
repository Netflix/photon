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
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ACESPictureSubDescriptor as defined in st429-4:2006
 */
@Immutable
public final class ACESPictureSubDescriptor extends SubDescriptor {
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ACESPictureSubDescriptor.class.getSimpleName() + " : ";
    private final ACESPictureSubDescriptorBO subDescriptorBO;

    /**
     * Constructor for a ACESPictureSubDescriptor object
     * @param subDescriptorBO the parsed ACESPictureSubDescriptor object
     */
    public ACESPictureSubDescriptor(ACESPictureSubDescriptorBO subDescriptorBO){
        this.subDescriptorBO = subDescriptorBO;
    }

    /**
     * A method that returns a string representation of a ACESPictureSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.subDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed ACESPictureSubDescriptor as defined in st2067-50
     */
    @Immutable
    public static final class ACESPictureSubDescriptorBO extends SubDescriptorBO{
        @MXFProperty(size=0, charset="UTF-16") private final String aces_authoring_information = null;
        @MXFProperty(size=12) private final byte[] aces_mastering_display_primaries = null;
        @MXFProperty(size=4) private final byte[] aces_mastering_white = null;
        @MXFProperty(size=4) private final Integer aces_max_luminance = null;
        @MXFProperty(size=4) private final Integer aces_min_luminance = null;

        /**
         * Instantiates a new ACESPictureSubDescriptor ByteObject.
         *
         * @param header the header
         * @param byteProvider the mxf byte provider
         * @param localTagToUIDMap the local tag to uID map
         * @param imfErrorLogger the imf error logger
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ACESPictureSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ACESPictureSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a ACESPictureSubDescriptorB0 object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== ACESPictureSubDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("aces_authoring_information = %s", this.aces_authoring_information));
            String aces_mastering_display_primariesString = "";
            if (aces_mastering_display_primaries != null) {
                for(byte b: aces_mastering_display_primaries){
                    aces_mastering_display_primariesString = aces_mastering_display_primariesString.concat(String.format("%02x", b));
                }
            }
            sb.append(String.format("aces_mastering_display_primaries = %s", aces_mastering_display_primariesString));
            String aces_mastering_whiteString = "";
            if (aces_mastering_white != null) {
                for(byte b: aces_mastering_white){
                    aces_mastering_whiteString = aces_mastering_whiteString.concat(String.format("%02x", b));
                }
            }
            sb.append(String.format("aces_mastering_whiteString = %s", aces_mastering_whiteString));
            sb.append(String.format("aces_max_luminance = %d", this.aces_max_luminance));
            sb.append(String.format("aces_min_luminance = %d", this.aces_min_luminance));
            return sb.toString();
        }
    }
}
