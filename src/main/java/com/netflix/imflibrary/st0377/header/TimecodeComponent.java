/*
 *
 *  * Copyright 2015 Netflix, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *         http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
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
 * Object model corresponding to TimecodeComponent structural metadata set defined in st377-1:2011
 */
@Immutable
public final class TimecodeComponent extends StructuralComponent
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + TimecodeComponent.class.getSimpleName() + " : ";
    private final TimecodeComponentBO timecodeComponentBO;

    /**
     * Constructor for a TimecodeComponent structural metadata set
     * @param timecodeComponentBO the parsed TimeCode Component object
     */
    public TimecodeComponent(TimecodeComponentBO timecodeComponentBO)
    {
        this.timecodeComponentBO = timecodeComponentBO;
    }

    /**
     * Getter for the instance UID for this TimecodeComponent structural metadata set
     * @return the instance UID for this TimecodeComponent structural metadata set
     */
    public MXFUID getInstanceUID()
    {
        return new MXFUID(this.timecodeComponentBO.instance_uid);
    }

    /**
     * A method that returns a string representation of a TimecodeComponent object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.timecodeComponentBO.toString();
    }

    /**
     * Object corresponding to parsed TimecodeComponent structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class TimecodeComponentBO extends StructuralComponentBO
    {
        /**
         * Instantiates a new parsed TimecodeComponent object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public TimecodeComponentBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        TimecodeComponent.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a TimecodeComponentBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== TimecodeComponent ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("data_definition = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.data_definition[0], this.data_definition[1], this.data_definition[2], this.data_definition[3],
                    this.data_definition[4], this.data_definition[5], this.data_definition[6], this.data_definition[7],
                    this.data_definition[8], this.data_definition[9], this.data_definition[10], this.data_definition[11],
                    this.data_definition[12], this.data_definition[13], this.data_definition[14], this.data_definition[15]));
            sb.append(String.format("duration = %d%n", this.duration));
            return sb.toString();
        }
    }
}
