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
import java.util.List;
import java.util.UUID;

/**
 * A class that represents Marker Virtual Track. All the resources in this track are of type MarkerResourceType.
 */
@Immutable
public final class IMFMarkerVirtualTrack extends Composition.VirtualTrack {
    public IMFMarkerVirtualTrack(UUID trackID, String sequenceType,
                                 List<IMFMarkerResourceType> resourceList,
                                 Composition.EditRate compositionEditRate){
        super(trackID, sequenceType, resourceList, compositionEditRate);
    }

    /**
     * Getter for the resources that are a part of this virtual track
     * @return an unmodifiable list of resources of type IMFMarkerResourceType that are a part of this virtual track
     */
    public List<IMFMarkerResourceType> getMarkerResourceList()
    {
       return (List<IMFMarkerResourceType>)this.getResourceList();
    }
}
