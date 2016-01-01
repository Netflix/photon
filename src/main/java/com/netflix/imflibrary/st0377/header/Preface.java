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
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to Preface structural metadata set defined in st377-1:2011
 */
@Immutable
public final class Preface extends InterchangeObject
{

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + Preface.class.getSimpleName() + " : ";

    private final PrefaceBO prefaceBO;
    private final GenericPackage primaryPackage;
    private final ContentStorage contentStorage;

    /**
     * Instantiates a new Preface.
     *
     * @param prefaceBO the parsed Preface object
     * @param primaryPackage the primary package referred by this Preface object
     * @param contentStorage the ContentStorage object referred by this Preface object
     */
    public Preface(PrefaceBO prefaceBO, GenericPackage primaryPackage, ContentStorage contentStorage)
    {
        this.prefaceBO = prefaceBO;
        this.primaryPackage = primaryPackage;
        this.contentStorage = contentStorage;
    }

    /**
     * Getter for the PrimaryPackage object referred by this Preface object in the MXF file
     * @return the PrimaryPackage object referred by this Preface object in the MXF file
     */
    public GenericPackage getPrimaryPackage()
    {
        return this.primaryPackage;
    }

    /**
     * Getter for the ContentStorage object referred by this Preface object in the MXF file
     * @return the ContentStorage object referred by this Preface object in the MXF file
     */
    public ContentStorage getContentStorage()
    {
        return this.contentStorage;
    }

    /**
     * Getter for the OperationalPattern that this MXF file complies to
     * @return the OperationalPattern that this MXF file complies to
     */
    public UL getOperationalPattern()
    {
        return this.prefaceBO.operational_pattern;
    }

    /**
     * Getter for a list of labels of EssenceContainers used in or referenced by this MXF file
     * @return list of labels of EssenceContainers used in or referenced by this MXF file
     */
    public int getNumberOfEssenceContainerULs()
    {
        return this.prefaceBO.essencecontainers.size();
    }

    /**
     * A method that returns a string representation of a Preface object
     * @return string representing the object
     */
    public String toString()
    {
        return this.prefaceBO.toString();
    }

    /**
     * Object corresponding to a parsed Preface structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class PrefaceBO extends InterchangeObjectBO
    {

        @MXFField(size=8) private final byte[] last_modified_date = null;
        @MXFField(size=2) private final byte[] version = null;
        @MXFField(size=16, depends=true) private final byte[] primary_package = null; //In-file Weak Ref
        @MXFField(size=16, depends=true) private final StrongRef content_storage = null;
        @MXFField(size=16) private final UL operational_pattern = null;
        @MXFField(size=0) private final CompoundDataTypes.MXFCollections.MXFCollection<UL> essencecontainers = null;
        @MXFField(size=0) private final CompoundDataTypes.MXFCollections.MXFCollection<UL> dm_schemes = null;

        /**
         * Instantiates a new parsed Preface object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public PrefaceBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        Preface.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.content_storage == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        Preface.ERROR_DESCRIPTION_PREFIX + "content_storage is null");
            }

            if (this.operational_pattern == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        Preface.ERROR_DESCRIPTION_PREFIX + "operational_pattern is null");
            }

            if (this.essencecontainers == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        Preface.ERROR_DESCRIPTION_PREFIX + "essencecontainers is null");
            }
        }

        /**
         * Getter for the PrimaryPackage structural metadata set instance UID in this MXF file
         * @return the PrimaryPackage structural metadata set instance UID in this MXF file
         */
        public @Nullable
        MXFUID getPrimaryPackageInstanceUID()
        {
            if (this.primary_package != null)
            {
                return new MXFUID(this.primary_package);
            }
            return null;
        }

        /**
         * Getter for the ContentStorage structural metadata set instance UID in this MXF file
         * @return the ContentStorage structural metadata set instance UID in this MXF file
         */
        public @Nullable
        MXFUID getContentStorageInstanceUID()
        {
            if (this.content_storage != null)
            {
                return this.content_storage.getInstanceUID();
            }
            return null;
        }

        /**
         * A method that returns a string representation of a PrefaceBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== Preface ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("last_modified_date = 0x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.last_modified_date[0], this.last_modified_date[1], this.last_modified_date[2], this.last_modified_date[3],
                    this.last_modified_date[4], this.last_modified_date[5], this.last_modified_date[6], this.last_modified_date[7]));
            sb.append(String.format("version = 0x%02x%02x%n", this.version[0], this.version[1]));
            sb.append(String.format("content_storage = %s%n", this.content_storage.toString()));
            sb.append(String.format("operational_pattern = %s%n", this.operational_pattern.toString()));
            sb.append(this.essencecontainers.toString());
            sb.append(this.dm_schemes.toString());
            return sb.toString();
        }
    }
}
