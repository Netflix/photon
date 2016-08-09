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

import com.netflix.imflibrary.st2067_2.CompositionModels.BaseResourceType;
import com.netflix.imflibrary.st2067_2.CompositionModels.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.UUID;

/**
 * A class that represents Essence Virtual Track. All the resources in this track are of type TrackFileResourceType.
 */
@Immutable
public final class IMFEssenceComponentVirtualTrack extends Composition.VirtualTrack {
    public IMFEssenceComponentVirtualTrack(UUID trackID, Composition.SequenceTypeEnum sequenceTypeEnum,
                                           List<BaseResourceType> resourceList){
        super(trackID, sequenceTypeEnum);
        for(BaseResourceType resource : resourceList){
            IMFTrackFileResourceType trackFileResource = IMFTrackFileResourceType.class.cast(resource);
            this.resourceIds.add(UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResource.getTrackFileId()));
            this.resources.add(resource);
        }
    }

}
