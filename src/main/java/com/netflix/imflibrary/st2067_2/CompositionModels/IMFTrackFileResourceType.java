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

package com.netflix.imflibrary.st2067_2.CompositionModels;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.List;

/**
 * A class that models TrackFile Resource structure of an IMF Composition Playlist.
 */
public abstract class IMFTrackFileResourceType extends BaseResourceType {
    protected final String trackFileId;
    protected final String sourceEncoding;

    public IMFTrackFileResourceType(String id,
                                    String trackFileId,
                                    List<Long> editRate,
                                    BigInteger intrinsicDuration,
                                    BigInteger entryPoint,
                                    BigInteger sourceDuration,
                                    BigInteger repeatCount,
                                    String sourceEncoding )
    {
        super(id, editRate, intrinsicDuration, entryPoint, sourceDuration, repeatCount);
        this.trackFileId = trackFileId;
        this.sourceEncoding = sourceEncoding;
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
     * A method to determine the equivalence of any 2 TrackFileResource.
     * @param other - the object to compare against
     * @return boolean indicating if the 2 TrackFileResources are equivalent/representing the same timeline
     */
    public boolean equivalent(IMFTrackFileResourceType other)
    {
        if(other == null){
            return false;
        }
        boolean result = true;
        //Compare the following fields of the track file resources that have to be equal
        //for the 2 resources to be considered equivalent/representing the same timeline.
        result &= super.equivalent( other );
        result &= trackFileId.equals(other.getTrackFileId());
        result &= sourceEncoding.equals(other.getSourceEncoding());

        return  result;
    }
}
