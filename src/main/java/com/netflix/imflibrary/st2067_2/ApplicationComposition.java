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

import com.netflix.imflibrary.utils.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.*;

/**
 * This interface represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
public interface ApplicationComposition {
    /**
     * A method that returns a string representation of a Composition object
     *
     * @return string representing the object
     */
    public String toString();

    /**
     * Getter for the composition edit rate as specified in the Composition XML file
     *
     * @return the edit rate associated with the Composition
     */
    public Composition.EditRate getEditRate();

    /**
     * Getter method for Annotation child element of CompositionPlaylist
     *
     * @return value of Annotation child element or null if it is not exist
     */
    public
    @Nullable
    String getAnnotation();

    /**
     * Getter method for Issuer child element of CompositionPlaylist
     *
     * @return value of Issuer child element or null if it is not exist
     */
    public
    @Nullable
    String getIssuer();

    /**
     * Getter method for Creator child element of CompositionPlaylist
     *
     * @return value of Creator child element or null if it is not exist
     */
    public
    @Nullable
    String getCreator();

    /**
     * Getter method for ContentOriginator child element of CompositionPlaylist
     *
     * @return value of ContentOriginator child element or null if it is not exist
     */
    public
    @Nullable
    String getContentOriginator();

    /**
     * Getter method for ContentTitle child element of CompositionPlaylist
     *
     * @return value of ContentTitle child element or null if it is not exist
     */
    public
    @Nullable
    String getContentTitle();

    /**
     * Getter for the UUID corresponding to this Composition document
     *
     * @return the uuid of this Composition object
     */
    public UUID getUUID();

    /**
     * Getter for the CoreConstraintsURI corresponding to this CompositionPlaylist
     *
     * @return the uri for the CoreConstraints schema for this CompositionPlaylist
     */
    public String getCoreConstraintsVersion();

    /**
     * Getter for the essence VirtualTracks in this Composition
     *
     * @return a list of essence virtual tracks that are a part of this composition or an empty list if there are none
     * track
     */
    @Nullable
    public List<IMFEssenceComponentVirtualTrack> getEssenceVirtualTracks();

    /**
     * Getter for the video VirtualTrack in this Composition
     *
     * @return the video virtual track that is a part of this composition or null if there is not video virtual track
     */
    @Nullable
    public IMFEssenceComponentVirtualTrack getVideoVirtualTrack();

    /**
     * Getter for the audio VirtualTracks in this Composition
     *
     * @return a list of audio virtual tracks that are a part of this composition or an empty list if there are none
     */
    public List<IMFEssenceComponentVirtualTrack> getAudioVirtualTracks();

    /**
     * Getter for the marker VirtualTrack in this Composition
     *
     * @return the marker virtual track that is a part of this composition or null if there is no marker virtual track
     */
    @Nullable
    public IMFMarkerVirtualTrack getMarkerVirtualTrack();


    /**
     * Getter for the errors in Composition
     *
     * @return List of errors in Composition.
     */
    public List<ErrorLogger.ErrorObject> getErrors();

    /**
     * A utility method to retrieve the VirtualTracks within a Composition.
     *
     * @return A list of VirtualTracks in the Composition.
     */
    @Nonnull
    public List<? extends Composition.VirtualTrack> getVirtualTracks();

    /**
     * A utility method to retrieve the EssenceDescriptors within a Composition.
     *
     * @return A list of EssenceDescriptors in the Composition.
     */
    @Nonnull
    public List<DOMNodeObjectModel> getEssenceDescriptors();

    /**
     * A utility method to retrieve the EssenceDescriptors within a Composition based on the name.
     *
     * @param  descriptorName EssenceDescriptor name
     * @return A list of DOMNodeObjectModels representing EssenceDescriptors with given name.
     */
    @Nonnull List<DOMNodeObjectModel> getEssenceDescriptors(String descriptorName);

    /**
     * A utility method to retrieve the EssenceDescriptor within a Composition for a Resource with given track file ID.
     *
     * @param trackFileId the track file id of the resource
     * @return  the DOMNodeObjectModel representing the EssenceDescriptor
     */
    @Nullable DOMNodeObjectModel getEssenceDescriptor(UUID trackFileId);

    public Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> getAudioVirtualTracksMap();

    /**
     * This method can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to the
     * resources referenced by the Composition.
     *
     * @param headerPartitionTuples        list of HeaderPartitionTuples corresponding to the IMF essences referenced in the Composition
     * @param conformAllVirtualTracksInCpl a boolean that turns on/off conforming all the VirtualTracks in the Composition
     * @return boolean to indicate of the Composition is conformant or not
     * @throws IOException        - any I/O related error is exposed through an IOException.
     */
    public List<ErrorLogger.ErrorObject> conformVirtualTracksInComposition(List<Composition.HeaderPartitionTuple>
                                                                   headerPartitionTuples,
                                                     boolean conformAllVirtualTracksInCpl) throws IOException;

    /**
     * A method to get Application Composition type.
     *
     * @return Application Composition Type
     */
    ApplicationCompositionType getApplicationCompositionType();

    /**
     * A method to get Composition image essence descriptor model
     *
     * @return Application CompositionImageEssenceDescriptorModel
     */
    @Nullable CompositionImageEssenceDescriptorModel getCompositionImageEssenceDescriptorModel();

    /**
     * A method that confirms if the inputStream corresponds to a Composition document instance.
     *
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     * @return a boolean indicating if the input file is a Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isCompositionPlaylist(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        return AbstractApplicationComposition.isCompositionPlaylist(resourceByteRangeProvider);
    }
}