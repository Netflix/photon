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

import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.MXFUid;
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Object model corresponding to GenericPackage structural metadata set defined in st377-1:2011
 */
public abstract class GenericPackage extends InterchangeObject
{

    private final GenericPackageBO genericPackageBO;

    public GenericPackage(GenericPackageBO genericPackageBO)
    {
        this.genericPackageBO = genericPackageBO;
    }

    /**
     * Getter for the immutable unique packager identifier which is a basic UMID (see st0330:2011)
     * @return the immutable unique packager identifier represented as a MXFUid object
     */
    public MXFUid getPackageUID()
    {
        return new MXFUid(this.genericPackageBO.package_uid);
    }

    /**
     * Getter for the material number part of the immutable unique package identifier
     * @return the material number part as a UUID
     */
    public UUID getPackageMaterialNumberasUUID()
    {
        long mostSignificantBits = ByteBuffer.wrap(this.genericPackageBO.package_uid, 16, 8).getLong();
        long leastSignificantBits = ByteBuffer.wrap(this.genericPackageBO.package_uid, 24, 8).getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public abstract static class GenericPackageBO extends InterchangeObjectBO
    {
        private static final byte[] NULL_PACKAGE_UID = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        /**
         * The Package _ uid.
         */
        @MXFField(size=32) protected final byte[] package_uid = null; //PackageID type
        /**
         * The Package _ creation _ date.
         */
        @MXFField(size=0) protected final CompoundDataTypes.Timestamp package_creation_date = null;
        /**
         * The Package _ modified _ date.
         */
        @MXFField(size=0) protected final CompoundDataTypes.Timestamp package_modified_date = null;
        /**
         * The Tracks.
         */
        @MXFField(size=0, depends=true) protected final CompoundDataTypes.MXFCollections.MXFCollection<StrongRef> tracks = null;

        /**
         * The Generic track instance uI ds.
         */
        protected final List<MXFUid> genericTrackInstanceUIDs = new ArrayList<>();

        /**
         * Instantiates a new Generic package ByteObject.
         *
         * @param header the header
         */
        GenericPackageBO(MXFKLVPacket.Header header)
        {
            super(header);
        }

        /**
         * Gets package uID.
         *
         * @return the package uID
         */
        public MXFUid getPackageUID()
        {
            return new MXFUid(this.package_uid);
        }

        /**
         * Gets null package uID.
         *
         * @return the null package uID
         */
        public static MXFUid getNullPackageUID()
        {
            return new MXFUid(GenericPackageBO.NULL_PACKAGE_UID);
        }

        /**
         * Gets generic track instance uI ds.
         *
         * @return the generic track instance uI ds
         */
        public List<MXFUid> getGenericTrackInstanceUIDs()
        {
            return Collections.unmodifiableList(this.genericTrackInstanceUIDs);
        }
    }
}
