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
import com.netflix.imflibrary.MXFDataDefinition;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to SourceClip descriptive metadata set defined in st377-1:2011
 */
@Immutable
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
public final class SourceClip extends StructuralComponent
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + SourceClip.class.getSimpleName() + " : ";
    private final SourceClipBO sourceClipBO;
    private final GenericPackage genericPackage;

    private final MXFDataDefinition mxfDataDefinition;

    /**
     * Instantiates a new SourceClip object
     *
     * @param sourceClipBO the parsed SourceClip object
     * @param genericPackage the generic package referred by this SourceClip object
     */
    public SourceClip(SourceClipBO sourceClipBO, GenericPackage genericPackage)
    {
        this.sourceClipBO = sourceClipBO;
        this.genericPackage = genericPackage;
        this.mxfDataDefinition = MXFDataDefinition.getDataDefinition(new MXFUID(this.sourceClipBO.data_definition));
    }

    /**
     * Getter for the instance UID of this SourceClip object
     * @return the instance UID of this SourceClip object
     */
    public MXFUID getInstanceUID()
    {
        return new MXFUID(this.sourceClipBO.instance_uid);
    }

    /**
     * Getter for the ID of the referenced Source Package
     * @return the ID of the referenced Source Package
     */
    public MXFUID getSourcePackageID()
    {
        return new MXFUID(this.sourceClipBO.source_package_id);
    }

    /**
     * Getter for the duration of the sequence in units of Edit Rate
     * @return the duration of the sequence in units of Edit Rate
     */
    public Long getDuration()
    {
        return this.sourceClipBO.duration;
    }

    /**
     * A method that returns a string representation of a SourceClip object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.sourceClipBO.toString();
    }

    /**
     * Object corresponding to a parsed SourceClip structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class SourceClipBO extends StructuralComponentBO
    {
        @MXFProperty(size=8) private final Long start_position = null;
        @MXFProperty(size=32, depends=true) private final byte[] source_package_id = null; //Package Ref type
        @MXFProperty(size=4) private final Long source_track_id = null;

        /**
         * Instantiates a new parsed SourceClip object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public SourceClipBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SourceClip.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.source_package_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SourceClip.ERROR_DESCRIPTION_PREFIX + "source_package_id is null");
            }

            if (this.duration == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SourceClip.ERROR_DESCRIPTION_PREFIX + "duration is null");
            }
        }

        /**
         * Getter for the ID of the referenced Source Package
         * @return the ID of the referenced Source Package
         */
        public MXFUID getSourcePackageID()
        {
            return new MXFUID(this.source_package_id);
        }

        /**
         * A method that returns a string representation of a SourceClipBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== SourceClip ======================\n");
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
            sb.append(String.format("start_position = %d%n", this.start_position));
            sb.append(String.format("source_package_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.source_package_id[0], this.source_package_id[1], this.source_package_id[2], this.source_package_id[3],
                    this.source_package_id[4], this.source_package_id[5], this.source_package_id[6], this.source_package_id[7],
                    this.source_package_id[8], this.source_package_id[9], this.source_package_id[10], this.source_package_id[11],
                    this.source_package_id[12], this.source_package_id[13], this.source_package_id[14], this.source_package_id[15],
                    this.source_package_id[16], this.source_package_id[17], this.source_package_id[18], this.source_package_id[19],
                    this.source_package_id[20], this.source_package_id[21], this.source_package_id[22], this.source_package_id[23],
                    this.source_package_id[24], this.source_package_id[25], this.source_package_id[26], this.source_package_id[27],
                    this.source_package_id[28], this.source_package_id[29], this.source_package_id[30], this.source_package_id[31]));
            sb.append(String.format("source_track_id = 0x%04x(%d)%n", this.source_track_id, this.source_track_id));
            return sb.toString();

        }
    }
}
