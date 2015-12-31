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
import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to PHDRMetaDataTrackSubDescriptor structural metadata set
 */
@Immutable
public final class PHDRMetaDataTrackSubDescriptor extends SubDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + PHDRMetaDataTrackSubDescriptor.class.getSimpleName() + " : ";
    private final PHDRMetaDataTrackSubDescriptorBO phdrMetaDataTrackSubDescriptorBO;

    /**
     * Instantiates a new PHDR meta data track sub descriptor.
     *
     * @param phdrMetaDataTrackSubDescriptorBO the phdr meta data track sub descriptor bO
     */
    public PHDRMetaDataTrackSubDescriptor(PHDRMetaDataTrackSubDescriptorBO phdrMetaDataTrackSubDescriptorBO)
    {
        this.phdrMetaDataTrackSubDescriptorBO = phdrMetaDataTrackSubDescriptorBO;
    }

    /**
     * A method that returns a string representation of a PHDRMetaDataTrackSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.phdrMetaDataTrackSubDescriptorBO.toString();
    }

    /**
     * The type PHDR meta data track sub descriptor ByteObject.
     */
    @Immutable
    public static final class PHDRMetaDataTrackSubDescriptorBO extends SubDescriptorBO
    {
        /**
         * Instantiates a new PHDR meta data track sub descriptor ByteObject.
         *
         * @param header the header
         * @param byteProvider the mxf byte provider
         * @param localTagToUIDMap the local tag to uID map
         * @param imfErrorLogger the imf error logger
         * @throws IOException the iO exception
         */
        public PHDRMetaDataTrackSubDescriptorBO(MXFKLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        PHDRMetaDataTrackSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a PHDRMetaDataTrackSubDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== PHDRMetaDataTrackSubdescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            return sb.toString();

        }
    }

}
