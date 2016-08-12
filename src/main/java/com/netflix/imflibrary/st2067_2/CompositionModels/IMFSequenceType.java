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

import com.netflix.imflibrary.st2067_2.Composition;

import javax.annotation.concurrent.Immutable;
import java.util.List;

/**
 * A class that models Sequence structure of an IMF Composition Playlist.
 */
@Immutable
public final class IMFSequenceType {
    protected final String id;
    protected final String trackId;
    protected final List<? extends IMFBaseResourceType> resourceList;
    protected final Composition.SequenceTypeEnum type;

    public IMFSequenceType(String id,
                           String trackId,
                           Composition.SequenceTypeEnum type,
                           List<? extends IMFBaseResourceType> resourceList)
    {
        this.id             = id;
        this.trackId        = trackId;
        this.resourceList   = (List<IMFBaseResourceType>)resourceList;
        this.type           = type;
    }

    /**
     * Getter for the Sequence ID
     * @return a string representing the urn:uuid of the sequence
     */
    public String getId(){
        return this.id;
    }

    /**
     * Getter for the Sequence track ID
     * @return a string representing the track ID of the sequence
     */
    public String getTrackId(){
        return this.trackId;
    }

    /**
     * Getter for the Sequence type
     * @return a enum  representing sequence type
     */
    public Composition.SequenceTypeEnum getType(){
        return this.type;
    }

    /**
     * Getter for the Resource list
     * @return a list containing all the resources of the Sequence
     */
    public List<? extends IMFBaseResourceType> getResourceList(){
        return this.resourceList;
    }


}
