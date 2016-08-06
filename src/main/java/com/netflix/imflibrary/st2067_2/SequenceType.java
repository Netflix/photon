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

package com.netflix.imflibrary.st2067_2;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.List;

/**
 * A class that models a Composition's sequence structure.
 */
@Immutable
public final class SequenceType {
    protected final String id;
    protected final String trackId;
    protected final List<BaseResourceType> resourceList;

    public SequenceType(String id,
                        String trackId,
                        List<BaseResourceType> resourceList)
    {
        this.id             = id;
        this.trackId        = trackId;
        this.resourceList   = resourceList;
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
     * Getter for the Resource List
     * @return a string representing the track ID of the sequence
     */
    public List<BaseResourceType> getResourceList(){
        return this.resourceList;
    }


}
