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

import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.UUID;

/**
 * A class that models an IMF Composition Playlist structure.
 */
@Immutable
public final class IMFCompositionPlaylistType {
    private final UUID id;
    private final Composition.EditRate editRate;
    private final String annotation;
    private final String issuer;
    private final String creator;
    private final String contentOriginator;
    private final String contentTitle;
    protected final List<IMFSegmentType> segmentList;
    protected final List<IMFEssenceDescriptorBaseType> essenceDescriptorList;

    public IMFCompositionPlaylistType(String id,
                                   List<Long> editRate,
                                   String annotation,
                                   String issuer,
                                   String creator,
                                   String contentOriginator,
                                   String contentTitle,
                                   List<IMFSegmentType> segmentList,
                                   List<IMFEssenceDescriptorBaseType> essenceDescriptorList )
    {
        this.id                = UUIDHelper.fromUUIDAsURNStringToUUID(id);
        this.editRate          = new Composition.EditRate(editRate);
        this.annotation        = annotation;
        this.issuer            = issuer;
        this.creator           = creator;
        this.contentOriginator = contentOriginator;
        this.contentTitle      = contentTitle;
        this.segmentList        = segmentList;
        this.essenceDescriptorList = essenceDescriptorList;
    }

    /**
     * Getter for the Composition Playlist ID
     * @return a string representing the urn:uuid of the Composition Playlist
     */
    public UUID getId(){
        return this.id;
    }

    /**
     * Getter for the EditRate of the Composition Playlist
     * @return a Composition.EditRate object of the Composition Playlist
     */
    public Composition.EditRate getEditRate(){
        return this.editRate;
    }

    /**
     * Getter for the Composition Playlist annotation
     * @return a string representing annotation of the Composition Playlist
     */
    public String getAnnotation(){
        return this.annotation;
    }

    /**
     * Getter for the Composition Playlist issuer
     * @return a string representing issuer of the Composition Playlist
     */
    public String getIssuer(){
        return this.issuer;
    }

    /**
     * Getter for the Composition Playlist creator
     * @return a string representing creator of the Composition Playlist
     */
    public String getCreator(){
        return this.creator;
    }

    /**
     * Getter for the Composition Playlist contentOriginator
     * @return a string representing contentOriginator of the Composition Playlist
     */
    public String getContentOriginator(){
        return this.contentOriginator;
    }

    /**
     * Getter for the Composition Playlist contentTitle
     * @return a string representing contentTitle of the Composition Playlist
     */
    public String getContentTitle(){
        return this.contentTitle;
    }

    /**
     * Getter for the SegmentList of the Composition Playlist
     * @return a string representing the SegmentList of the Composition Playlist
     */
    public List<IMFSegmentType> getSegmentList(){
        return this.segmentList;
    }

    /**
     * Getter for the EssenceDescriptorlist of the Composition Playlist
     * @return a string representing the EssenceDescriptorlist of the Composition Playlist
     */
    public List<IMFEssenceDescriptorBaseType> getEssenceDescriptorList(){
        return this.essenceDescriptorList;
    }

}
