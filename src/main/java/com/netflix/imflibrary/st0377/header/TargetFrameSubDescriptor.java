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
 * Object model corresponding to TargetFrameSubDescriptor as defined in st429-4:2006
 */
@Immutable
public final class TargetFrameSubDescriptor extends SubDescriptor {
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + TargetFrameSubDescriptor.class.getSimpleName() + " : ";
    private final TargetFrameSubDescriptorBO subDescriptorBO;

    /**
     * Constructor for a TargetFrameSubDescriptor object
     * @param subDescriptorBO the parsed TargetFrameSubDescriptor object
     */
    public TargetFrameSubDescriptor(TargetFrameSubDescriptorBO subDescriptorBO){
        this.subDescriptorBO = subDescriptorBO;
    }

    /**
     * A method that returns a string representation of a TargetFrameSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.subDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed TargetFrameSubDescriptor as defined in st429-4-2006
     */
    @Immutable
    public static final class TargetFrameSubDescriptorBO extends SubDescriptorBO{
        @MXFProperty(size=16) private final UL ancillary_resource_uid = null;
        @MXFProperty(size=0, charset="UTF-16") private final String media_type = null;
        @MXFProperty(size=8) private final Long target_frame_index = null;
        @MXFProperty(size=16) private final UL target_frame_transfer_characteristic = null;
        @MXFProperty(size=16) private final UL color_primaries = null;
        @MXFProperty(size=4) private final Integer max_ref = null;
        @MXFProperty(size=4) private final Integer min_ref = null;
        @MXFProperty(size=4) private final Integer essence_stream_id = null;
        @MXFProperty(size=16) private final UL aces_picture_subdescriptor_uid = null;
        @MXFProperty(size=16) private final UL viewing_environment = null;

        /**
         * Instantiates a new TargetFrameSubDescriptor ByteObject.
         *
         * @param header the header
         * @param byteProvider the mxf byte provider
         * @param localTagToUIDMap the local tag to uID map
         * @param imfErrorLogger the imf error logger
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public TargetFrameSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        TargetFrameSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a TargetFrameSubDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== TargetFrameSubDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            if (this.ancillary_resource_uid != null) sb.append(String.format("ancillary_resource_uid = %s%n", this.ancillary_resource_uid.toString()));
            sb.append(String.format("media_type = %s%n", this.media_type));
            sb.append(String.format("target_frame_index = %d%n", this.target_frame_index));
            if (this.target_frame_transfer_characteristic != null) sb.append(String.format("target_frame_transfer_characteristic = %s%n", this.target_frame_transfer_characteristic.toString()));
            if (this.color_primaries != null) sb.append(String.format("color_primaries = %s%n", this.color_primaries.toString()));
            sb.append(String.format("max_ref = %d%n", this.max_ref));
            sb.append(String.format("min_ref = %d%n", this.min_ref));
            sb.append(String.format("essence_stream_id = %d%n", this.essence_stream_id));
            if (this.aces_picture_subdescriptor_uid != null) sb.append(String.format("aces_picture_subdescriptor_uid = %s%n", this.aces_picture_subdescriptor_uid.toString()));
            if (this.viewing_environment != null) sb.append(String.format("viewing_environment = %s%n", this.viewing_environment.toString()));
            return sb.toString();
        }
    }
}
