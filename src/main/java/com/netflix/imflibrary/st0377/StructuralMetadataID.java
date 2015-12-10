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

package com.netflix.imflibrary.st0377;

/**
 * An enumeration of all the types of Structural and Descriptive Metadata as defined in st377-1:2011
 */
public enum StructuralMetadataID {
    /**
     *
     */
    ContentStorage("ContentStorage"),

    /**
     *
     */
    EssenceContainerData("EssenceContainerData"),

    /**
     *
     */
    SourceClip("SourceClip"),

    /**
     *
     */
    TimecodeComponent("TimecodeComponent"),

    /**
     *
     */
    SourcePackage("SourcePackage"),

    /**
     *
     */
    MaterialPackage("MaterialPackage"),

    /**
     *
     */
    Preface("Preface"),

    /**
     *
     */
    Sequence("Sequence"),

    /**
     *
     */
    TimelineTrack("TimelineTrack"),

    /**
     *
     */
    WaveAudioEssenceDescriptor("WaveAudioEssenceDescriptor"),

    /**
     *
     */
    CDCIPictureEssenceDescriptor("CDCIPictureEssenceDescriptor"),

    /**
     *
     */
    RGBAPictureEssenceDescriptor("RGBAPictureEssenceDescriptor"),

    /**
     *
     *
     */
    PHDRMetadataTrackSubDescriptor("PHDRMetadataTrackSubDescriptor"),

    /**
     *
     */
    AudioChannelLabelSubDescriptor("AudioChannelLabelSubDescriptor"),

    /**
     *
     */
    MCALabelSubDescriptor("MCALabelSubDescriptor");

    private final String structuralMetadataName;
    private StructuralMetadataID(String structuralMetadataName){
        this.structuralMetadataName = structuralMetadataName;
    }

    public final String getName(){
        return this.structuralMetadataName;
    }
}
