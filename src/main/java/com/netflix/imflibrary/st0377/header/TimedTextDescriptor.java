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
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Object model corresponding to TimedTextDescriptor structural metadata set defined in st429-5:2009
 */
@Immutable
public final class TimedTextDescriptor extends GenericDataEssenceDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + TimedTextDescriptor.class.getSimpleName() + " : ";
    private final TimedTextDescriptorBO timedTextDescriptorBO;
    private final List<TimeTextResourceSubDescriptor> subDescriptorList;

    /**
     * Constructor for a TimedTextDescriptor object
     * @param timedTextDescriptorBO the parsed TimedTextDescriptor object
     * @param subDescriptorList List containing TimeTextResourceSubDescriptor objects
     */
    public TimedTextDescriptor(TimedTextDescriptorBO timedTextDescriptorBO, List<TimeTextResourceSubDescriptor> subDescriptorList)
    {
        this.timedTextDescriptorBO = timedTextDescriptorBO;
        this.subDescriptorList = Collections.unmodifiableList(subDescriptorList);
    }

    /**
     * Getter for the TimeTextResourceSubDescriptor list
     * @return a list containing TimeTextResourceSubDescriptors
     */
    public List<TimeTextResourceSubDescriptor> getSubDescriptorList() {
        return subDescriptorList;
    }

    /**
     * Getter for the UCSEncoding
     * @return a String representing the ISO/IEC 10646-1encoding of the essence data
     */
    public String getUCSEncoding() {
        return this.timedTextDescriptorBO.ucs_encoding;
    }

    /**
     * Getter for the NamespaceURI
     * @return a String representing the URI value which is the XML namespace name of the top-level
     * XML element in the essence data
     */
    public String getNamespaceURI(){
        return this.timedTextDescriptorBO.namespace_uri;
    }

    /**
     * Getter for the Essence Container UL of this FileDescriptor
     * @return a UL representing the Essence Container
     */
    public UL getEssenceContainerUL(){
        return this.timedTextDescriptorBO.getEssenceContainerUL();
    }

    /**
     * A method that returns a string representation of a TimedTextDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.timedTextDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed TimedTextDescriptor structural metadata set defined in st429-5:2009
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class TimedTextDescriptorBO extends FileDescriptorBO
    {
        @MXFProperty(size=16) private final UL resource_id = null;
        @MXFProperty(size=0, charset="UTF-16") private final String ucs_encoding = null;
        @MXFProperty(size=0, charset="UTF-16") private final String namespace_uri = null;



        /**
         * Instantiates a new parsed TimedTextDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public TimedTextDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        TimedTextDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        /**
         * A method that returns a string representation of a TimedTextDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== TimedTextDescriptor ======================\n");
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
            return sb.toString();
        }
    }
}
