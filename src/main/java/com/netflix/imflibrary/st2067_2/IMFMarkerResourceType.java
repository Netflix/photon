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

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A class that models Marker resource structure of an IMF Composition Playlist.
 */
@Immutable
public final class IMFMarkerResourceType extends IMFBaseResourceType {
    private final List<IMFMarkerType> markerList;

    public IMFMarkerResourceType(String id,
                                 List<Long> editRate,
                                 BigInteger intrinsicDuration,
                                 BigInteger entryPoint,
                                 BigInteger sourceDuration,
                                 BigInteger repeatCount,
                                 List<IMFMarkerType> markerList )
    {
        super(id, editRate, intrinsicDuration, entryPoint, sourceDuration, repeatCount);
        markerList.sort(Comparator.comparing(IMFMarkerType::getOffset));

        this.markerList = Collections.unmodifiableList(markerList);
    }

    /**
     * Getter for the Marker list
     * @return a list containing all the Markers of the resource
     */
    public List<IMFMarkerType> getMarkerList(){
        return this.markerList;
    }

    /**
     * A method to determine the equivalence of any 2 Marker resource.
     * @param other - the object to compare against
     * @return boolean indicating if the two Marker resources are equivalent/representing the same timeline
     */
    @Override
    public boolean equivalent(IMFBaseResourceType other)
    {
        if(other == null || !(other instanceof IMFMarkerResourceType)){
            return false;
        }

        IMFMarkerResourceType otherMarkerResource = IMFMarkerResourceType.class.cast(other);

        boolean result = true;
        result &= super.equivalent( otherMarkerResource );

        List<IMFMarkerType> otherMarkerList = otherMarkerResource.getMarkerList();
        if(otherMarkerList.size() != this.markerList.size()){
            return false;
        }
        for(int i=0; i< this.markerList.size(); i++){
            IMFMarkerType thisMarker = this.markerList.get(i);
            IMFMarkerType otherMarker = otherMarkerList.get(i);

            result &= thisMarker.equivalent(otherMarker);
        }
        return  result;
    }
}
