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
 * Object model corresponding to RGBAPictureEssenceDescriptor structural metadata set defined in st377-1:2011
 */
@Immutable
public final class RGBAPictureEssenceDescriptor extends GenericPictureEssenceDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + RGBAPictureEssenceDescriptor.class.getSimpleName() + " : ";
    private final RGBAPictureEssenceDescriptorBO rgbaPictureEssenceDescriptorBO;

    /**
     * Constructor for a RGBAPictureEssenceDescriptor object
     * @param rgbaPictureEssenceDescriptorBO the parsed RGBAPictureEssenceDescriptor object
     */
    public RGBAPictureEssenceDescriptor(RGBAPictureEssenceDescriptorBO rgbaPictureEssenceDescriptorBO)
    {
        this.rgbaPictureEssenceDescriptorBO = rgbaPictureEssenceDescriptorBO;
    }

    /**
     * A method that returns a string representation of a RGBAPictureEssenceDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.rgbaPictureEssenceDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed RGBAPictureEssenceDescriptor structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class RGBAPictureEssenceDescriptorBO extends GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO
    {
        /**
         * Instantiates a new parsed RGBAPictureEssenceDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public RGBAPictureEssenceDescriptorBO(MXFKLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        RGBAPictureEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a RGBAPictureEssenceDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== RGBAPictureEssenceDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            if (this.subdescriptors != null)
            {
                sb.append(this.subdescriptors.toString());
            }
            sb.append("================== SampleRate ======================\n");
            sb.append(super.sample_rate.toString());
            sb.append(String.format("essence_container = %s%n", this.essence_container.toString()));
            sb.append(String.format("frame_layout = %d%n", this.frame_layout));
            sb.append(String.format("stored_width = %d%n", this.stored_width));
            sb.append(String.format("stored_height = %d%n", this.stored_height));
            sb.append("================== AspectRatio ======================\n");
            sb.append(this.aspect_ratio.toString());
            if (this.video_line_map != null)
            {
                sb.append(this.video_line_map.toString());
            }
            sb.append(String.format("picture_essence_coding = %s%n", this.picture_essence_coding.toString()));
            return sb.toString();
        }
    }
}
