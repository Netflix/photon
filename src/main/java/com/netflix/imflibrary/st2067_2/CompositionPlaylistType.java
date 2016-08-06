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

import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models a Composition's segment structure.
 */
@Immutable
public final class CompositionPlaylistType {
    private final UUID id;
    private final Composition.EditRate editRate;
    private final String annotation;
    private final String issuer;
    private final String creator;
    private final String contentOriginator;
    private final String contentTitle;
    protected final List<SegmentType> segmentList;

    public CompositionPlaylistType(String id,
                                   List<Long> editRate,
                                   String annotation,
                                   String issuer,
                                   String creator,
                                   String contentOriginator,
                                   String contentTitle,
                                   List<SegmentType> segmentList)
    {
        this.id                = UUIDHelper.fromUUIDAsURNStringToUUID(id);
        this.editRate          = new Composition.EditRate(editRate);
        this.annotation        = annotation;
        this.issuer            = issuer;
        this.creator           = creator;
        this.contentOriginator = contentOriginator;
        this.contentTitle      = contentTitle;
        this.segmentList        = segmentList;
    }

    /**
     * Getter for the Sequence ID
     * @return a string representing the urn:uuid of the segment
     */
    public UUID getId(){
        return this.id;
    }

    /**
     * Getter for the EditRate of the Composition
     * @return a Composition.EditRate object of the Composition
     */
    public Composition.EditRate getEditRate(){
        return this.editRate;
    }

    /**
     * Getter for the Composition annotation
     * @return a string representing annotation of the Composition
     */
    public String getAnnotation(){
        return this.annotation;
    }

    /**
     * Getter for the Composition issuer
     * @return a string representing issuer of the Composition
     */
    public String getIssuer(){
        return this.issuer;
    }

    /**
     * Getter for the Composition creator
     * @return a string representing creator of the Composition
     */
    public String getCreator(){
        return this.creator;
    }

    /**
     * Getter for the Composition contentOriginator
     * @return a string representing contentOriginator of the Composition
     */
    public String getContentOriginator(){
        return this.contentOriginator;
    }

    /**
     * Getter for the Composition contentTitle
     * @return a string representing contentTitle of the Composition
     */
    public String getContentTitle(){
        return this.contentTitle;
    }

    /**
     * Getter for the SegmentList
     * @return a string representing the SegmentList of the composition
     */
    public List<SegmentType> getSegmentList(){
        return this.segmentList;
    }


}
