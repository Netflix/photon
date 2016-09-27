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
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Object model corresponding to SourcePackage structural metadata set defined in st377-1:2011
 */
@Immutable
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
public final class SourcePackage extends GenericPackage
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + SourcePackage.class.getSimpleName() + " : ";
    private final SourcePackageBO sourcePackageBO;
    private final List<GenericTrack> genericTracks;
    private final GenericDescriptor genericDescriptor;

    /**
     * Instantiates a new Source package.
     *
     * @param sourcePackageBO the parsed SourcePackage object
     * @param genericTracks the list of generic tracks referred by this SourcePackage object
     * @param genericDescriptor the generic descriptor referred by this SourcePackage object
     */
    public SourcePackage(SourcePackageBO sourcePackageBO, List<GenericTrack> genericTracks, GenericDescriptor genericDescriptor)
    {
        super(sourcePackageBO);
        this.sourcePackageBO = sourcePackageBO;
        this.genericTracks = genericTracks;
        this.genericDescriptor = genericDescriptor;
    }

    /**
     * Getter for the instance UID for this Source Package
     * @return the instance UID for this Source Package represented as a MXFUid object
     */
    public MXFUID getInstanceUID()
    {
        return new MXFUID(this.sourcePackageBO.instance_uid);
    }

    /**
     * Getter for the GenericDescriptor referred by this Source Package
     * @return the GenericDescriptor referred by this Source Package
     */
    public GenericDescriptor getGenericDescriptor()
    {
        return this.genericDescriptor;
    }

    /**
     * Getter for the list of tracks referred by this Source Package
     * @return the list of tracks referred by this Source Package represented as MXFUid objects
     */
    public List<MXFUID> getTrackInstanceUIDs()
    {
        List<MXFUID> trackInstanceUIDs = new ArrayList<MXFUID>();
        for (InterchangeObjectBO.StrongRef strongRef : this.sourcePackageBO.tracks.getEntries())
        {
            trackInstanceUIDs.add(strongRef.getInstanceUID());
        }
        return trackInstanceUIDs;
    }

    /**
     * Getter for subset of generic tracks that are of type TimelineTrack
     *
     * @return the timeline tracks
     */
    public List<TimelineTrack> getTimelineTracks()
    {
        List<TimelineTrack> timelineTracks = new ArrayList<>();
        for (GenericTrack genericTrack : this.genericTracks)
        {
            if (genericTrack instanceof TimelineTrack)
            {
                timelineTracks.add((TimelineTrack)genericTrack);
            }
        }

        return timelineTracks;
    }

    /**
     * A method that returns a string representation of a Source Package object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.sourcePackageBO.toString();
    }

    /**
     * Object corresponding to parsed MaterialPackage structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class SourcePackageBO extends GenericPackageBO
    {
        @MXFProperty(size=16, depends=true) private final StrongRef descriptor = null;

        /**
         * Instantiates a new parsed SourcePackage object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public SourcePackageBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SourcePackage.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.package_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SourcePackage.ERROR_DESCRIPTION_PREFIX + "package_uid is null");
            }

            if (this.tracks == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        SourcePackage.ERROR_DESCRIPTION_PREFIX + "tracks is null");
            }
            else
            {
                for (StrongRef strongRef : this.tracks.getEntries())
                {
                    this.genericTrackInstanceUIDs.add(strongRef.getInstanceUID());
                }
            }

        }

        /**
         * Getter for the descriptor UID referred by this Source Package
         * @return the descriptor UID referred by this Source Package
         */
        public MXFUID getDescriptorUID()
        {
            return this.descriptor.getInstanceUID();
        }

        /**
         * A method that returns a string representation of a parsed Source Package object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== SourcePackage ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("package_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.package_uid[0], this.package_uid[1], this.package_uid[2], this.package_uid[3],
                    this.package_uid[4], this.package_uid[5], this.package_uid[6], this.package_uid[7],
                    this.package_uid[8], this.package_uid[9], this.package_uid[10], this.package_uid[11],
                    this.package_uid[12], this.package_uid[13], this.package_uid[14], this.package_uid[15],
                    this.package_uid[16], this.package_uid[17], this.package_uid[18], this.package_uid[19],
                    this.package_uid[20], this.package_uid[21], this.package_uid[22], this.package_uid[23],
                    this.package_uid[24], this.package_uid[25], this.package_uid[26], this.package_uid[27],
                    this.package_uid[28], this.package_uid[29], this.package_uid[30], this.package_uid[31]));
            sb.append("================== PackageCreationDate ======================\n");
            sb.append(this.package_creation_date.toString());
            sb.append("================== PackageModifiedDate ======================\n");
            sb.append(this.package_modified_date.toString());
            sb.append(this.tracks.toString());
            sb.append(String.format("descriptor = %s%n", this.descriptor.toString()));
            return sb.toString();
        }
    }
}
