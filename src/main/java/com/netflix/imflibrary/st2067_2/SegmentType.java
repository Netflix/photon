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
import java.util.List;

/**
 * A class that models a Composition's segment structure.
 */
@Immutable
public final class SegmentType {
    protected final String id;
    protected final List<SequenceType> sequenceList;

    public SegmentType(String id,
                       List<SequenceType> sequenceList){
        this.id = id;
        this.sequenceList = sequenceList;
    }

    /**
     * Getter for the Sequence ID
     * @return a string representing the urn:uuid of the segment
     */
    public String getId(){
        return this.id;
    }

    /**
     * Getter for the SequenceList
     * @return a string representing the SequenceList of the segment
     */
    public List<SequenceType> getSequenceList(){
        return this.sequenceList;
    }


}
