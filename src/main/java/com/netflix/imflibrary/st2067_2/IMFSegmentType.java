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
import java.util.Collections;
import java.util.List;

/**
 * A class that models Segment structure of an IMF Composition Playlist.
 */
@Immutable
public final class IMFSegmentType {
    protected final String id;
    protected final List<IMFSequenceType> sequenceList;

    public IMFSegmentType(String id,
                          List<IMFSequenceType> sequenceList){
        this.id = id;
        this.sequenceList = Collections.unmodifiableList(sequenceList);
    }

    /**
     * Getter for the Segment ID
     * @return a string representing the urn:uuid of the segment
     */
    public String getId(){
        return this.id;
    }

    /**
     * Getter for the Sequence list
     * @return a list containing all the sequences of the Segment
     */
    public List<IMFSequenceType> getSequenceList(){
        return this.sequenceList;
    }


}
