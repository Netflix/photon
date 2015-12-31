/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to EssenceContainerData structural metadata set defined in st377-1:2011
 */
@Immutable
public final class EssenceContainerData extends InterchangeObject
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + EssenceContainerData.class.getSimpleName() + " : ";
    private final EssenceContainerDataBO essenceContainerDataBO;
    private final GenericPackage genericPackage;

    /**
     * Instantiates a new EssenceContainerData object
     *
     * @param essenceContainerDataBO the parsed EssenceContainerData object
     * @param genericPackage the generic package referenced by this EssenceContainerData object
     */
    public EssenceContainerData(EssenceContainerDataBO essenceContainerDataBO, GenericPackage genericPackage)
    {
        this.essenceContainerDataBO = essenceContainerDataBO;
        this.genericPackage = genericPackage;
    }

    /**
     * Getter for the generic package referenced by this EssenceContainerData object
     * @return returns the generic package referenced by this Essence Container Data object
     */
    public GenericPackage getLinkedPackage()
    {
        return this.genericPackage;
    }

    /**
     * Getter for the instance UID corresponding to this EssenceContainerData set in the MXF file
     * @return returns the instanceUID corresponding to this Essence Container data object
     */
    public MXFUID getInstanceUID()
    {
        return new MXFUID(this.essenceContainerDataBO.instance_uid);
    }

    /**
     * Getter for the identifier of the package linked to this EssenceContainerData set
     * @return returns the identified of the package linked to this Essence Container data
     */
    public MXFUID getLinkedPackageUID()
    {
        return new MXFUID(this.essenceContainerDataBO.linked_package_uid);
    }

    /**
     * Getter for the ID of the Index Table linked to this EssenceContainerData set
     * @return returns the ID of the Index Table linked with this Essence Container data
     */
    public long getIndexSID()
    {
        return this.essenceContainerDataBO.index_sid;
    }

    /**
     * Getter for the ID of the Essence Container to which this EssenceContainerData set is linked
     * @return returns the ID of the essence container liked to this Essence Container data
     */
    public long getBodySID()
    {
        return this.essenceContainerDataBO.body_sid;
    }

    /**
     * A method that returns a string representation of an EssenceContainerData object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.essenceContainerDataBO.toString();
    }

    /**
     * Object corresponding to parsed EssenceContainerData structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class EssenceContainerDataBO  extends InterchangeObjectBO
    {
        @MXFField(size=32, depends=true) private final byte[] linked_package_uid = null; //PackageRef type
        @MXFField(size=4) private final Long index_sid = null;
        @MXFField(size=4) private final Long body_sid = null;

        /**
         * Instantiates a new parsed EssenceContainerData object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public EssenceContainerDataBO(MXFKLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        EssenceContainerData.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.linked_package_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        EssenceContainerData.ERROR_DESCRIPTION_PREFIX + "linked_package_uid is null");
            }

        }

        /**
         * Getter for the identifier of the package to which this EssenceContainerData set is linked
         * @return returns the identifier of the package linked to this Essence container data
         */
        public MXFUID getLinkedPackageUID()
        {
            return new MXFUID(this.linked_package_uid);
        }

        /**
         * A method that returns a string representation of an EssenceContainerDataBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== EssenceContainerData ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("linked_package_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.linked_package_uid[0], this.linked_package_uid[1], this.linked_package_uid[2], this.linked_package_uid[3],
                    this.linked_package_uid[4], this.linked_package_uid[5], this.linked_package_uid[6], this.linked_package_uid[7],
                    this.linked_package_uid[8], this.linked_package_uid[9], this.linked_package_uid[10], this.linked_package_uid[11],
                    this.linked_package_uid[12], this.linked_package_uid[13], this.linked_package_uid[14], this.linked_package_uid[15],
                    this.linked_package_uid[16], this.linked_package_uid[17], this.linked_package_uid[18], this.linked_package_uid[19],
                    this.linked_package_uid[20], this.linked_package_uid[21], this.linked_package_uid[22], this.linked_package_uid[23],
                    this.linked_package_uid[24], this.linked_package_uid[25], this.linked_package_uid[26], this.linked_package_uid[27],
                    this.linked_package_uid[28], this.linked_package_uid[29], this.linked_package_uid[30], this.linked_package_uid[31]));
            sb.append(String.format("index_sid = 0x%x(%d)%n", this.index_sid, this.index_sid));
            sb.append(String.format("body_sid = 0x%x(%d)%n", this.body_sid, this.body_sid));
            return sb.toString();

        }
    }
}
