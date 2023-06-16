/*
 *
 * Copyright 2016 Netflix, Inc.
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

package com.netflix.imflibrary.st2067_2;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * A class that models TrackFile Resource structure of an IMF Composition Playlist.
 */
@Immutable
public final class IMFTrackFileResourceType extends IMFBaseResourceType {
    private final String trackFileId;
    private final String sourceEncoding;
    private final byte[] hash;
    private final String hashAlgorithm;

    public IMFTrackFileResourceType(String id,
                                    String trackFileId,
                                    List<Long> editRate,
                                    BigInteger intrinsicDuration,
                                    BigInteger entryPoint,
                                    BigInteger sourceDuration,
                                    BigInteger repeatCount,
                                    String sourceEncoding,
                                    byte[] hash,
                                    String hashAlgorithm )
    {
        super(id, editRate, intrinsicDuration, entryPoint, sourceDuration, repeatCount);
        this.trackFileId = trackFileId;
        this.sourceEncoding = sourceEncoding;
        this.hash = (hash == null) ? null : Arrays.copyOf(hash, hash.length);
        this.hashAlgorithm = hashAlgorithm;
    }

    /**
     * Getter for the Track Resource's track file Id
     * @return a string representing the urn:uuid of the Track Resource's track file Id
     */
    public String getTrackFileId(){
        return this.trackFileId;
    }

    /**
     * Getter for the SourceEncoding of the Track's Resource
     * @return a String representing the Track Resource's SourceEncoding
     */
    public String getSourceEncoding(){
        return this.sourceEncoding;
    }

    /**
     * Getter for the Hash of this Resource
     * @return a byte[] copy of the hash
     */
    @Nullable
    public byte[] getHash(){
        return (this.hash == null) ? null : Arrays.copyOf(this.hash, this.hash.length);
    }

    /**
     * Getter for the HashAlgorithm used in creating the hash of this resource
     * @return a String representing the HashAlgorithm
     */
    @Nullable
    public String getHashAlgorithm(){
        return this.hashAlgorithm;
    }


    /**
     * A method to determine the equivalence of any 2 TrackFileResource.
     * @param other - the object to compare against
     * @return boolean indicating if the 2 TrackFileResources are equivalent/representing the same timeline
     */
    @Override
    public boolean equivalent(IMFBaseResourceType other)
    {
        if(other == null || !(other instanceof IMFTrackFileResourceType)){
            return false;
        }

        IMFTrackFileResourceType otherTrackFileResource = IMFTrackFileResourceType.class.cast(other);

        boolean result = true;
        //Compare the following fields of the track file resources that have to be equal
        //for the 2 resources to be considered equivalent/representing the same timeline.
        result &= super.equivalent( otherTrackFileResource );
        result &= trackFileId.equals(otherTrackFileResource.getTrackFileId());

        return  result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IMFTrackFileResourceType otherImfTrackFileResourceType = (IMFTrackFileResourceType) o;
        boolean result = equivalent(otherImfTrackFileResourceType);
        result &= sourceEncoding.equals(otherImfTrackFileResourceType.getSourceEncoding());
        result &= Arrays.equals(hash, otherImfTrackFileResourceType.getHash());
        result &= hashAlgorithm.equals(otherImfTrackFileResourceType.getHashAlgorithm());

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        // hash = 31 * hash * id.hashCode();
        hash = 31 * hash * trackFileId.hashCode();
        hash = 31 * hash * editRate.hashCode();
        hash = 31 * hash * intrinsicDuration.hashCode();
        hash = 31 * hash * entryPoint.hashCode();
        hash = 31 * hash * sourceDuration.hashCode();
        hash = 31 * hash * repeatCount.hashCode();
        hash = 31 * hash * sourceEncoding.hashCode();
        hash = 31 * hash * Arrays.hashCode(getHash());
        hash = 31 * hash * hashAlgorithm.hashCode();
        return hash;
    }

}
