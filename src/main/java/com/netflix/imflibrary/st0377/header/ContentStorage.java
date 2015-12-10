/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import com.netflix.imflibrary.MXFUid;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Object model corresponding to ContentStorage structural metadata set defined in st377-1:2011
 */
@Immutable
public final class ContentStorage extends InterchangeObject
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ContentStorage.class.getSimpleName() + " : ";
    private final ContentStorageBO contentStorageBO;
    private final List<GenericPackage> genericPackageList;
    private final List<EssenceContainerData> essenceContainerDataList;

    /**
     * Instantiates a new ContentStorage object
     *
     * @param contentStorageBO the parsed ContentStorage object
     * @param genericPackageList the list of generic packages referred by this ContentStorage object
     * @param essenceContainerDataList the list of essence container data sets referred by this ContentStorage object
     */
    public ContentStorage(ContentStorageBO contentStorageBO, List<GenericPackage> genericPackageList, List<EssenceContainerData> essenceContainerDataList)
    {
        this.contentStorageBO = contentStorageBO;
        this.genericPackageList = Collections.unmodifiableList(genericPackageList);
        this.essenceContainerDataList = Collections.unmodifiableList(essenceContainerDataList);
    }

    /**
     * Getter for the GenericPackageList
     * @return a List of GenericPackages
     */
    public List<GenericPackage> getGenericPackageList()
    {
        return this.genericPackageList;
    }

    public List<SourcePackage> getSourcePackageList()
    {
        List<SourcePackage> sourcePackages = new ArrayList<>();
        for (GenericPackage genericPackage : this.genericPackageList)
        {
            if (genericPackage instanceof SourcePackage)
            {
                sourcePackages.add((SourcePackage)genericPackage);
            }
        }

        return sourcePackages;
    }

    /**
     * Getter for the EssenceContainerDataList
     * @return a List of references to EssenceContainerData sets used in the MXF file
     */
    public List<EssenceContainerData> getEssenceContainerDataList()
    {
        return this.essenceContainerDataList;
    }

    /**
     * Getter for the PackageInstanceUIDs
     * @return a List of UID references to all the packages used in the MXF file
     */
    public List<MXFUid> getPackageInstanceUIDs()
    {
        List<MXFUid> packageInstanceUIDs = new ArrayList<MXFUid>();
        for (InterchangeObjectBO.StrongRef strongRef : this.contentStorageBO.packages.getEntries())
        {
            packageInstanceUIDs.add(strongRef.getInstanceUID());
        }
        return packageInstanceUIDs;
    }

    /**
     * Getter for the number of EssenceContainerData sets used in the MXF file
     * @return an integer representing the number of EssenceContainerData sets used in the MXF file
     */
    public int getNumberOfEssenceContainerDataSets()
    {
        return this.contentStorageBO.essencecontainer_data.size();
    }

    public String toString()
    {
        return this.contentStorageBO.toString();
    }

    /**
     * Object corresponding to parsed ContentStorage structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"}) @SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE"})
    public static final class ContentStorageBO extends InterchangeObjectBO
    {

        @MXFField(size=0, depends=true) private final CompoundDataTypes.MXFCollections.MXFCollection<StrongRef> packages = null;
        @MXFField(size=0, depends=true) private final CompoundDataTypes.MXFCollections.MXFCollection<StrongRef> essencecontainer_data = null;
        private final List<MXFUid> packageInstanceUIDs = new ArrayList<>();
        private final List<MXFUid> essenceContainerDataInstanceUIDs = new ArrayList<>();


        /**
         * Instantiates a new parsed ContentStorage object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ContentStorageBO(MXFKLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUid> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        ContentStorage.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.packages == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        ContentStorage.ERROR_DESCRIPTION_PREFIX + "packages is null");
            }
            else
            {
                for (StrongRef strongRef : this.packages.getEntries())
                {
                    this.packageInstanceUIDs.add(strongRef.getInstanceUID());
                }
            }

            if (this.essencecontainer_data == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        ContentStorage.ERROR_DESCRIPTION_PREFIX + "essencecontainer_data is null");
            }
            else
            {
                for (StrongRef strongRef : this.essencecontainer_data.getEntries())
                {
                    this.essenceContainerDataInstanceUIDs.add(strongRef.getInstanceUID());
                }
            }


        }

        /**
         * Getter for the PackageInstanceUIDs
         * @return a cloned List of UID references to all the packages used in the MXF file
         */
        public List<MXFUid> getPackageInstanceUIDs()
        {
            return Collections.unmodifiableList(this.packageInstanceUIDs);
        }

        /**
         * Getter for the PackageInstanceUIDs
         * @return a cloned List of UID references to all the EssenceContainerData sets used in the MXF file
         */
        public List<MXFUid> getEssenceContainerDataInstanceUIDs()
        {
            return Collections.unmodifiableList(this.essenceContainerDataInstanceUIDs);
        }

        /**
         * A method that returns a string representation of a ContentStorageBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== ContentStorage ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(this.packages.toString());
            sb.append(this.essencecontainer_data.toString());
            return sb.toString();

        }
    }
}
