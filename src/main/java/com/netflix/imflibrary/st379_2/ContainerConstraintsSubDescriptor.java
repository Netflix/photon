/*
 *
 * Copyright 2020 RheinMain University of Applied Sciences, Wiesbaden, Germany.
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

package com.netflix.imflibrary.st379_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.st0377.header.SubDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ContainerConstraintsSubDescriptor as defined in ST 379-2
 */
@Immutable
public final class ContainerConstraintsSubDescriptor extends SubDescriptor {
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ContainerConstraintsSubDescriptor.class.getSimpleName() + " : ";
    private final ContainerConstraintsSubDescriptorBO subDescriptorBO;

    /**
     * Constructor for a ContainerConstraintsSubDescriptor object
     * @param subDescriptorBO the parsed ContainerConstraintsSubDescriptor object
     */
    public ContainerConstraintsSubDescriptor(ContainerConstraintsSubDescriptorBO subDescriptorBO){
        this.subDescriptorBO = subDescriptorBO;
    }

    /**
     * A method that returns a string representation of a ContainerConstraintsSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.subDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed ContainerConstraintsSubDescriptor as defined in st429-4-2006
     */
    @Immutable
    public static final class ContainerConstraintsSubDescriptorBO extends SubDescriptorBO{

        /**
         * Instantiates a new ContainerConstraintsSubDescriptor ByteObject.
         *
         * @param header the header
         * @param byteProvider the mxf byte provider
         * @param localTagToUIDMap the local tag to uID map
         * @param imfErrorLogger the imf error logger
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ContainerConstraintsSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    ContainerConstraintsSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a ContainerConstraintsSubDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== ContainerConstraintsSubDescriptor ======================\n");
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
