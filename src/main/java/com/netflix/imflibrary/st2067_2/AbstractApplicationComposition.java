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

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_201.IMFIABConstraintsChecker;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;
import com.netflix.imflibrary.utils.RegXMLLibHelper;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.sandflow.smpte.klv.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
@Immutable
public abstract class AbstractApplicationComposition implements ApplicationComposition {
    private static final Logger logger = LoggerFactory.getLogger(AbstractApplicationComposition.class);
    private final Set<String> essenceDescriptorKeyIgnoreSet;



    private final String coreConstraintsVersion;
    private final Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap;
    private final IMFCompositionPlaylistType compositionPlaylistType;
    private final Map<UUID, List<Node>> essenceDescriptorDomNodeMap;

    protected final IMFErrorLogger imfErrorLogger;
    protected final RegXMLLibDictionary regXMLLibDictionary;

    /**
     * Constructor for a {@link AbstractApplicationComposition Composition} object from a XML file
     *
     * @param compositionPlaylistXMLFile the input XML file that is conformed to schema and constraints specified in st2067-3:2013 and st2067-2:2013
     * @throws IOException        any I/O related error is exposed through an IOException
     */
    public AbstractApplicationComposition(File compositionPlaylistXMLFile) throws IOException {
        this(new FileByteRangeProvider(compositionPlaylistXMLFile));
    }

    /**
     * Constructor for a {@link AbstractApplicationComposition Composition} object from a XML file
     *
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     *                                  if any {@link IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     * @throws IOException        any I/O related error is exposed through an IOException
     */
    public AbstractApplicationComposition(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        this(IMFCompositionPlaylistType.getCompositionPlayListType( resourceByteRangeProvider, new IMFErrorLoggerImpl()), new HashSet<String>());
    }

    /**
     * Constructor for a {@link AbstractApplicationComposition Composition} object from a XML file
     *
     * @param imfCompositionPlaylistType corresponding to the Composition XML file.
     *                                  if any {@link IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     * @param ignoreSet Set of essence descriptor fields to ignore
     */
    public AbstractApplicationComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, @Nonnull Set<String> ignoreSet) {
        this(imfCompositionPlaylistType, ignoreSet, new HashSet<>());
    }


    /**
     * Constructor for a {@link AbstractApplicationComposition Composition} object from a XML file
     *
     * @param imfCompositionPlaylistType corresponding to the Composition XML file.
     *                                  if any {@link IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     * @param ignoreSet Set of essence descriptor fields to ignore
     * @param homogeneitySelectionSet Set of essence descriptor fields to select in track homogeneity check
     */
    public AbstractApplicationComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, @Nonnull Set<String> ignoreSet, @Nonnull Set<String> homogeneitySelectionSet) {
        imfErrorLogger = new IMFErrorLoggerImpl();

        this.compositionPlaylistType = imfCompositionPlaylistType;

        this.regXMLLibDictionary = new RegXMLLibDictionary();

        this.coreConstraintsVersion = this.compositionPlaylistType.getCoreConstraintsVersion();

        this.essenceDescriptorKeyIgnoreSet = Collections.unmodifiableSet(ignoreSet);

        this.virtualTrackMap = this.getVirtualTracksMap(compositionPlaylistType, imfErrorLogger);
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap= this.getEssenceDescriptorListMap(ignoreSet);

        imfErrorLogger.addAllErrors(IMFCoreConstraintsChecker.checkVirtualTracks(compositionPlaylistType, this
                .virtualTrackMap, essenceDescriptorListMap, this.regXMLLibDictionary, homogeneitySelectionSet));

        if (IMFCoreConstraintsChecker.hasIABVirtualTracks(compositionPlaylistType, virtualTrackMap)) {
            List<ErrorLogger.ErrorObject> errors = IMFIABConstraintsChecker.checkIABVirtualTrack(compositionPlaylistType.getEditRate(), virtualTrackMap, essenceDescriptorListMap, this.regXMLLibDictionary, homogeneitySelectionSet);
            imfErrorLogger.addAllErrors(errors);
        }

        if ((compositionPlaylistType.getEssenceDescriptorList() == null) ||
                (compositionPlaylistType.getEssenceDescriptorList().size() < 1)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ESSENCE_DESCRIPTOR_LIST_MISSING,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, "EssenceDescriptorList is either absent or empty.");
        }

        this.essenceDescriptorDomNodeMap = Collections.unmodifiableMap(createEssenceDescriptorDomNodeMap());

        if (imfErrorLogger.hasFatalErrors()) {
            throw new IMFException(String.format("Found fatal errors in CompositionPlaylist XML file."), imfErrorLogger);
        }
    }

    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     *
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger          - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding Composition.VirtualTrack
     */
    private static Map<UUID, Composition.VirtualTrack> getVirtualTracksMap(@Nonnull IMFCompositionPlaylistType
                                                                             compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger) {
        Map<UUID, Composition.VirtualTrack> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<IMFBaseResourceType>> virtualTrackResourceMap = getVirtualTrackResourceMap(compositionPlaylistType, imfErrorLogger);

        //process first segment to create virtual track map
        IMFSegmentType segment = compositionPlaylistType.getSegmentList().get(0);
        for (IMFSequenceType sequence : segment.getSequenceList()) {
            UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
            if (virtualTrackMap.get(uuid) == null) {
                List<? extends IMFBaseResourceType> virtualTrackResourceList = null;
                if (virtualTrackResourceMap.get(uuid) == null) {
                    virtualTrackResourceList = new ArrayList<IMFBaseResourceType>();
                } else {
                    virtualTrackResourceList = virtualTrackResourceMap.get(uuid);
                }
                Composition.VirtualTrack virtualTrack = null;
                if (virtualTrackResourceList.size() != 0) {
                    if (virtualTrackResourceList.get(0) instanceof IMFTrackFileResourceType) {
                        virtualTrack = new IMFEssenceComponentVirtualTrack(uuid,
                                sequence.getType(),
                                (List<IMFTrackFileResourceType>) virtualTrackResourceList,
                                compositionPlaylistType.getEditRate());
                    } else if (virtualTrackResourceList.get(0) instanceof IMFMarkerResourceType) {
                        virtualTrack = new IMFMarkerVirtualTrack(uuid,
                                sequence.getType(),
                                (List<IMFMarkerResourceType>) virtualTrackResourceList,
                                compositionPlaylistType.getEditRate());
                    }
                }
                virtualTrackMap.put(uuid, virtualTrack);
            } else {
                //Section 6.9.3 st2067-3:2016
                String message = String.format(
                        "First segment in Composition XML file has multiple occurrences of virtual track UUID %s this is invalid.", uuid);
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, message);
            }
        }

        IMFCoreConstraintsChecker.checkSegments(compositionPlaylistType, virtualTrackMap, imfErrorLogger);

        return virtualTrackMap;
    }

    /**
     * A stateless method that completely reads and parses the resources of all the Composition.VirtualTracks that are a part of the Composition
     *
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger          - an object for logging errors
     * @return map of VirtualTrack identifier to the list of all the Track's resources, for every Composition.VirtualTrack of the Composition
     */
    private static Map<UUID, List<IMFBaseResourceType>> getVirtualTrackResourceMap(@Nonnull IMFCompositionPlaylistType
                                                                                     compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger) {
        Map<UUID, List<IMFBaseResourceType>> virtualTrackResourceMap = new LinkedHashMap<>();
        for (IMFSegmentType segment : compositionPlaylistType.getSegmentList()) {
            for (IMFSequenceType sequence : segment.getSequenceList()) {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                if (virtualTrackResourceMap.get(uuid) == null) {
                    virtualTrackResourceMap.put(uuid, new ArrayList<IMFBaseResourceType>());
                }

                for (IMFBaseResourceType baseResource : sequence.getResourceList()) {
                   /* Ignore track file resource with zero or negative duration */
                    if (baseResource.getSourceDuration().longValue() <= 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("Resource with zero source duration ignored: VirtualTrackID %s ResourceID %s",
                                        uuid.toString(),
                                        baseResource.getId()));
                    } else {
                        virtualTrackResourceMap.get(uuid).add(baseResource);
                    }
                }
            }
        }

        //make virtualTrackResourceMap immutable
        for (Map.Entry<UUID, List<IMFBaseResourceType>> entry : virtualTrackResourceMap.entrySet()) {
            List<? extends IMFBaseResourceType> baseResources = entry.getValue();
            entry.setValue(Collections.unmodifiableList(baseResources));
        }

        return virtualTrackResourceMap;
    }

    /**
     * A method that returns a string representation of a Composition object
     *
     * @return string representing the object
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("======= Composition : %s =======%n", this.compositionPlaylistType.getId()));
        sb.append(this.compositionPlaylistType.getEditRate().toString());
        return sb.toString();
    }

    /**
     * A method that confirms if the inputStream corresponds to a Composition document instance.
     *
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     * @return a boolean indicating if the input file is a Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isCompositionPlaylist(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        return IMFCompositionPlaylistType.isCompositionPlaylist(resourceByteRangeProvider);
    }

    /**
     * Getter for the composition edit rate as specified in the Composition XML file
     *
     * @return the edit rate associated with the Composition
     */
    public Composition.EditRate getEditRate() {
        return this.compositionPlaylistType.getEditRate();
    }

    /**
     * Getter method for Annotation child element of CompositionPlaylist
     *
     * @return value of Annotation child element or null if it is not exist
     */
    public
    @Nullable
    String getAnnotation() {
        return this.compositionPlaylistType.getAnnotation();
    }

    /**
     * Getter method for Issuer child element of CompositionPlaylist
     *
     * @return value of Issuer child element or null if it is not exist
     */
    public
    @Nullable
    String getIssuer() {
        return this.compositionPlaylistType.getIssuer();
    }

    /**
     * Getter method for Creator child element of CompositionPlaylist
     *
     * @return value of Creator child element or null if it is not exist
     */
    public
    @Nullable
    String getCreator() {
        return this.compositionPlaylistType.getCreator();
    }

    /**
     * Getter method for ContentOriginator child element of CompositionPlaylist
     *
     * @return value of ContentOriginator child element or null if it is not exist
     */
    public
    @Nullable
    String getContentOriginator() {
        return this.compositionPlaylistType.getContentOriginator();
    }

    /**
     * Getter method for ContentTitle child element of CompositionPlaylist
     *
     * @return value of ContentTitle child element or null if it is not exist
     */
    public
    @Nullable
    String getContentTitle() {
        return this.compositionPlaylistType.getContentTitle();
    }

    /**
     * Getter for the virtual track map associated with this Composition
     *
     * @return {@link Map Map}&lt;{@link UUID UUID},{@link Composition.VirtualTrack VirtualTrack}&gt;. The UUID key corresponds to VirtualTrackID
     */
    Map<UUID, ? extends Composition.VirtualTrack> getVirtualTrackMap() {
        return Collections.unmodifiableMap(this.virtualTrackMap);
    }

    /**
     * Getter for the UUID corresponding to this Composition document
     *
     * @return the uuid of this Composition object
     */
    public UUID getUUID() {
        return this.compositionPlaylistType.getId();
    }

    /**
     * Getter for the CompositionPlaylistType object model of the Composition defined by the st2067-3 schema.
     *
     * @return the composition playlist type object model.
     */
    private IMFCompositionPlaylistType getCompositionPlaylistType() {
        return this.compositionPlaylistType;
    }

    /**
     * Getter for the CoreConstraintsURI corresponding to this CompositionPlaylist
     *
     * @return the uri for the CoreConstraints schema for this CompositionPlaylist
     */
    public String getCoreConstraintsVersion() {
        return this.coreConstraintsVersion;
    }

    /**
     * Getter for the Track file resources in this Composition
     *
     * @return a list of track file resources that are a part of this composition or an empty list if there are none
     * track
     */
    public List<IMFTrackFileResourceType> getTrackFileResources() {
        List<IMFTrackFileResourceType> trackFileResources = new ArrayList<>();
        Iterator iterator = this.getVirtualTrackMap().entrySet().iterator();
        while (iterator != null
                && iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getResourceList().size() != 0 && virtualTrack.getResourceList().get(0) instanceof IMFTrackFileResourceType) {
                trackFileResources.addAll(IMFEssenceComponentVirtualTrack.class.cast(virtualTrack).getTrackFileResourceList());
            }
        }
        return Collections.unmodifiableList(trackFileResources);
    }

    /**
     * Getter for the essence VirtualTracks in this Composition
     *
     * @return a list of essence virtual tracks that are a part of this composition or an empty list if there are none
     * track
     */
    @Nullable
    public List<IMFEssenceComponentVirtualTrack> getEssenceVirtualTracks() {
        List<IMFEssenceComponentVirtualTrack> essenceVirtualTracks = new ArrayList<>();
        Iterator iterator = this.getVirtualTrackMap().entrySet().iterator();
        while (iterator != null
                && iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getResourceList().size() != 0 && virtualTrack.getResourceList().get(0) instanceof IMFTrackFileResourceType) {
                essenceVirtualTracks.add(IMFEssenceComponentVirtualTrack.class.cast(virtualTrack));
            }
        }
        return Collections.unmodifiableList(essenceVirtualTracks);
    }

    /**
     * Getter for the video VirtualTrack in this Composition
     *
     * @return the video virtual track that is a part of this composition or null if there is not video virtual track
     */
    @Nullable
    public IMFEssenceComponentVirtualTrack getVideoVirtualTrack() {
        Iterator iterator = this.virtualTrackMap.entrySet().iterator();
        while (iterator != null
                && iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>)
                    iterator.next()).getValue();
            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                return IMFEssenceComponentVirtualTrack.class.cast(virtualTrack);
            }
        }
        return null;
    }

    /**
     * Getter for the audio VirtualTracks in this Composition
     *
     * @return a list of audio virtual tracks that are a part of this composition or an empty list if there are none
     */
    public List<IMFEssenceComponentVirtualTrack> getAudioVirtualTracks() {
        List<IMFEssenceComponentVirtualTrack> audioVirtualTracks = new ArrayList<>();
        Iterator iterator = this.getVirtualTrackMap().entrySet().iterator();
        while (iterator != null
                && iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence)) {
                audioVirtualTracks.add(IMFEssenceComponentVirtualTrack.class.cast(virtualTrack));
            }
        }
        return Collections.unmodifiableList(audioVirtualTracks);
    }

    /**
     * Getter for the marker VirtualTrack in this Composition
     *
     * @return the marker virtual track that is a part of this composition or null if there is no marker virtual track
     */
    @Nullable
    public IMFMarkerVirtualTrack getMarkerVirtualTrack() {
        Iterator iterator = this.virtualTrackMap.entrySet().iterator();
        while (iterator != null
                && iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MarkerSequence)) {
                return IMFMarkerVirtualTrack.class.cast(virtualTrack);
            }
        }
        return null;
    }

    /**
     * Getter for the errors in Composition
     *
     * @return List of errors in Composition.
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validateCompositionPlaylistSchema(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException, SAXException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, imfErrorLogger);
        return imfErrorLogger.getErrors();
    }

    /**
     * A utility method to retrieve the VirtualTracks within a Composition.
     *
     * @return A list of VirtualTracks in the Composition.
     */
    @Nonnull
    public List<? extends Composition.VirtualTrack> getVirtualTracks() {
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = this.getVirtualTrackMap();
        return new ArrayList<>(virtualTrackMap.values());
    }

    /**
     * A utility method to retrieve the EssenceDescriptors within a Composition.
     *
     * @return A list of EssenceDescriptors in the Composition.
     */
    @Nonnull
    public List<DOMNodeObjectModel> getEssenceDescriptors() {
        Map<UUID, DOMNodeObjectModel> essenceDescriptorMap = this.getEssenceDescriptorListMap();
        return new ArrayList<>(essenceDescriptorMap.values());
    }

    /**
     * A utility method to retrieve the EssenceDescriptors within a Composition based on the name.
     *
     * @param  descriptorName EssenceDescriptor name
     * @return A list of DOMNodeObjectModels representing EssenceDescriptors with given name.
     */
    public List<DOMNodeObjectModel> getEssenceDescriptors(String descriptorName) {
        return this.getEssenceDescriptors()
                .stream().filter(e -> e.getLocalName().equals(descriptorName))
                .collect(Collectors.toList());
    }

    /**
     * A utility method to retrieve the EssenceDescriptor within a Composition for a Resource with given track file ID.
     *
     * @param trackFileId the track file id of the resource
     * @return  the DOMNodeObjectModel representing the EssenceDescriptor
     */
    public DOMNodeObjectModel getEssenceDescriptor(UUID trackFileId) {
        IMFTrackFileResourceType imfTrackFileResourceType = this.getTrackFileResources()
                .stream()
                .filter(e -> UUIDHelper.fromUUIDAsURNStringToUUID(e.getTrackFileId()).equals(trackFileId))
                .findFirst()
                .get();

        return this.getEssenceDescriptorListMap().get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
    }

    /**
     * A utility method to retrieve the UUIDs of the Track files referenced by a Virtual track within a Composition.
     *
     * @param virtualTrack - object model of an IMF virtual track {@link Composition.VirtualTrack}
     * @return A list of TrackFileResourceType objects corresponding to the virtual track in the Composition.
     */
    @Nonnull
    public static List<ResourceIdTuple> getVirtualTrackResourceIDs(@Nonnull Composition.VirtualTrack virtualTrack) {

        List<ResourceIdTuple> virtualTrackResourceIDs = new ArrayList<>();

        List<? extends IMFBaseResourceType> resourceList = virtualTrack.getResourceList();
        if (resourceList != null
                && resourceList.size() > 0 &&
                virtualTrack.getResourceList().get(0) instanceof IMFTrackFileResourceType) {

            for (IMFBaseResourceType baseResource : resourceList) {
                IMFTrackFileResourceType trackFileResource = IMFTrackFileResourceType.class.cast(baseResource);

                virtualTrackResourceIDs.add(new ResourceIdTuple(UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResource.getTrackFileId())
                        , UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResource.getSourceEncoding())));
            }
        }

        return Collections.unmodifiableList(virtualTrackResourceIDs);
    }


    /**
     * This class is a representation of a Resource SourceEncoding element and trackFileId tuple.
     */
    public static final class ResourceIdTuple {
        private final UUID trackFileId;
        private final UUID sourceEncoding;

        private ResourceIdTuple(UUID trackFileId, UUID sourceEncoding) {
            this.trackFileId = trackFileId;
            this.sourceEncoding = sourceEncoding;
        }

        /**
         * A getter for the trackFileId referenced by the resource corresponding to this ResourceIdTuple
         *
         * @return the trackFileId associated with this ResourceIdTuple
         */
        public UUID getTrackFileId() {
            return this.trackFileId;
        }

        /**
         * A getter for the source encoding element referenced by the resource corresponding to this ResourceIdTuple
         *
         * @return the source encoding element associated with this ResourceIdTuple
         */
        public UUID getSourceEncoding() {
            return this.sourceEncoding;
        }
    }

    /**
     * A utility method that will analyze the EssenceDescriptorList in a Composition and construct a HashMap mapping
     * a UUID to a EssenceDescriptor.
     *
     * @param ignoreSet - Set with names of properties to ignore
     * @return a HashMap mapping the UUID to its corresponding EssenceDescriptor in the Composition
     */
    Map<UUID, DOMNodeObjectModel> getEssenceDescriptorListMap(Set<String> ignoreSet) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorMap = new HashMap<>();
        if (compositionPlaylistType.getEssenceDescriptorList() != null) {
            List<IMFEssenceDescriptorBaseType> essenceDescriptors = compositionPlaylistType.getEssenceDescriptorList();
            for (IMFEssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptors) {
                try {
                    UUID uuid = essenceDescriptorBaseType.getId();
                    DOMNodeObjectModel domNodeObjectModel = null;
                    for (Object object : essenceDescriptorBaseType.getAny()) {
                        domNodeObjectModel = new DOMNodeObjectModel((Node) object);
                        if(domNodeObjectModel != null && ignoreSet.size() != 0) {
                            domNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(domNodeObjectModel, ignoreSet);
                        }
                    }
                    if (domNodeObjectModel != null) {
                        essenceDescriptorMap.put(uuid, domNodeObjectModel);
                    }
                }
                catch(IMFException e)
                {
                    imfErrorLogger.addAllErrors(e.getErrors());
                }
            }
        }
        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("Creating essenceDescriptorMap failed", imfErrorLogger);
        }
        return Collections.unmodifiableMap(essenceDescriptorMap);
    }

    /**
     * A utility method that will analyze the EssenceDescriptorList in a Composition and construct a HashMap mapping
     * a UUID to a EssenceDescriptor.
     *
     * @return a HashMap mapping the UUID to its corresponding EssenceDescriptor in the Composition
     */
    Map<UUID, DOMNodeObjectModel> getEssenceDescriptorListMap() {
        return getEssenceDescriptorListMap(new HashSet<>());
    }

    public Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> getAudioVirtualTracksMap() {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<? extends Composition.VirtualTrack> audioVirtualTracks = this.getAudioVirtualTracks();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = this.getEssenceDescriptorListMap();
        Map<Set<DOMNodeObjectModel>, Composition.VirtualTrack> audioVirtualTrackMap = new HashMap<>();
        for (Composition.VirtualTrack audioVirtualTrack : audioVirtualTracks) {
            Set<DOMNodeObjectModel> set = new HashSet<>();
            List<? extends IMFBaseResourceType> resources = audioVirtualTrack.getResourceList();
            for (IMFBaseResourceType resource : resources) {
                IMFTrackFileResourceType trackFileResource = IMFTrackFileResourceType.class.cast(resource);
                try {
                    set.add(essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResource.getSourceEncoding())));//Fetch and add the EssenceDescriptor referenced by the resource via the SourceEncoding element to the ED set.
                }
                catch(IMFException e)
                {
                    imfErrorLogger.addAllErrors(e.getErrors());
                }
            }
            audioVirtualTrackMap.put(set, audioVirtualTrack);
        }
        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("Creating Audio Virtual track map failed", imfErrorLogger);
        }
        return Collections.unmodifiableMap(audioVirtualTrackMap);
    }

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
                                                     boolean conformAllVirtualTracksInCpl) throws IOException {
        /*
         * The algorithm for conformance checking a Composition (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list (EDL) is referenced through its id element if conformAllVirtualTracks is enabled
         * by at least one TrackFileResource within the Virtual tracks in the Composition (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the Composition are present in
         * the physical essence files referenced by the resources of a virtual track and are equal.
         */
        /*The following check simultaneously verifies 1) and 2) from above.*/
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Set<UUID> resourceEssenceDescriptorIDsSet = getResourceEssenceDescriptorIdsSet();
        Set<UUID> cplEssenceDescriptorIDsSet = getEssenceDescriptorIdsSet();
        Iterator cplEssenceDescriptorIDs = cplEssenceDescriptorIDsSet.iterator();


        /**
         * The following checks that at least one of the Virtual Tracks references an EssenceDescriptor in the EDL. This
         * check should be performed only when we need to conform all the Virtual Tracks in the CPL.
         */
        if (conformAllVirtualTracksInCpl) {
            while (cplEssenceDescriptorIDs.hasNext()) {
                UUID cplEssenceDescriptorUUID = (UUID) cplEssenceDescriptorIDs.next();
                if (!resourceEssenceDescriptorIDsSet.contains(cplEssenceDescriptorUUID)) {
                    //Section 6.1.10.1 st2067-3:2013
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptorID %s in the CPL " +
                            "EssenceDescriptorList is not referenced by any resource in any of the Virtual tracks in the CPL, this is invalid.", cplEssenceDescriptorUUID.toString()));
                }
            }
        }

        if (imfErrorLogger.hasFatalErrors()) {
            return imfErrorLogger.getErrors();
        }

        Map essenceDescriptorMap = null;
        Map resourceEssenceDescriptorMap = null;
        /*The following check verifies 3) from above.*/
        try {
            essenceDescriptorMap = this.getEssenceDescriptorListMap();
        }
        catch(IMFException e)
        {
            this.imfErrorLogger.addAllErrors(e.getErrors());
        }

        try {
            resourceEssenceDescriptorMap = this.getResourcesEssenceDescriptorsMap(headerPartitionTuples);
        }
        catch(IMFException e)
        {
            this.imfErrorLogger.addAllErrors(e.getErrors());
        }

        if( essenceDescriptorMap == null || resourceEssenceDescriptorMap == null || imfErrorLogger.hasFatalErrors())
        {
            return imfErrorLogger.getErrors();
        }

        imfErrorLogger.addAllErrors(conformEssenceDescriptors(resourceEssenceDescriptorMap, essenceDescriptorMap));
        return imfErrorLogger.getErrors();
    }

    private Set<UUID> getEssenceDescriptorIdsSet() {
        HashSet<UUID> essenceDescriptorIdsSet = new LinkedHashSet<>();
        if (compositionPlaylistType.getEssenceDescriptorList() != null) {
            List<IMFEssenceDescriptorBaseType> essenceDescriptorList = compositionPlaylistType.getEssenceDescriptorList();
            for (IMFEssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptorList) {
                UUID sourceEncodingElement = essenceDescriptorBaseType.getId();
                /*Construct a set of SourceEncodingElements/IDs corresponding to every EssenceDescriptorBaseType in the EssenceDescriptorList*/
                essenceDescriptorIdsSet.add(sourceEncodingElement);
            }
        }
        return essenceDescriptorIdsSet;
    }


    private Set<UUID> getResourceEssenceDescriptorIdsSet() {
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(this.getVirtualTrackMap().values());
        LinkedHashSet<UUID> resourceSourceEncodingElementsSet = new LinkedHashSet<>();
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            List<AbstractApplicationComposition.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);
            for (AbstractApplicationComposition.ResourceIdTuple resourceIdTuple : resourceIdTuples) {
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        return resourceSourceEncodingElementsSet;
    }

    public static List<AbstractApplicationComposition.ResourceIdTuple> getResourceIdTuples(List<? extends Composition.VirtualTrack> virtualTracks) {
        List<AbstractApplicationComposition.ResourceIdTuple>  resourceIdTupleList = new ArrayList<>();
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            resourceIdTupleList.addAll(getVirtualTrackResourceIDs(virtualTrack));
        }
        return resourceIdTupleList;
    }

    private Map<UUID, List<DOMNodeObjectModel>> getResourcesEssenceDescriptorsMap(List<Composition
            .HeaderPartitionTuple> headerPartitionTuples) throws IOException {
        int previousNumberOfErrors = imfErrorLogger.getErrors().size();
        Map<UUID, List<DOMNodeObjectModel>> resourcesEssenceDescriptorMap = new LinkedHashMap<>();

        /*Create a Map of FilePackage UUID which should be equal to the TrackFileId of the resource in the Composition if the asset is referenced and the HeaderPartitionTuple, Map<UUID, HeaderPartitionTuple>*/
        Map<UUID, Composition.HeaderPartitionTuple> resourceUUIDHeaderPartitionMap = new HashMap<>();
        for (Composition.HeaderPartitionTuple headerPartitionTuple : headerPartitionTuples) {
            //validate header partition
            try {
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartitionTuple.getHeaderPartition(), imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                resourceUUIDHeaderPartitionMap.put(packageUUID, headerPartitionTuple);
            }
            catch (IMFException | MXFException e){
                Preface preface = headerPartitionTuple.getHeaderPartition().getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("IMFTrackFile with ID %s has fatal errors", packageUUID.toString())));
                if(e instanceof IMFException){
                    IMFException imfException = (IMFException)e;
                    imfErrorLogger.addAllErrors(imfException.getErrors());
                }
                else if(e instanceof MXFException){
                    MXFException mxfException = (MXFException)e;
                    imfErrorLogger.addAllErrors(mxfException.getErrors());
                }
            }
        }
        if(imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors())){
            throw new IMFException(String.format("Fatal errors were detected in the IMFTrackFiles"), imfErrorLogger);
        }
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(this.getVirtualTrackMap().values());

        /*Go through all the Virtual Tracks in the Composition and construct a map of Resource Source Encoding Element and a list of DOM nodes representing every EssenceDescriptor in the HeaderPartition corresponding to that Resource*/
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            List<AbstractApplicationComposition.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);/*Retrieve a list of ResourceIDTuples corresponding to this virtual track*/
            for (AbstractApplicationComposition.ResourceIdTuple resourceIdTuple : resourceIdTuples)
            {
                try
                {
                    Composition.HeaderPartitionTuple headerPartitionTuple = resourceUUIDHeaderPartitionMap.get(resourceIdTuple.getTrackFileId());
                    if (headerPartitionTuple != null)
                    {
                        /*Create a DOM Node representation of the EssenceDescriptors present in this header partition
                        corresponding to an IMFTrackFile*/
                        List<Node> essenceDescriptorDOMNodes = getEssenceDescriptorDOMNodes(headerPartitionTuple);
                        List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
                        for (Node node : essenceDescriptorDOMNodes) {
                            try {
                                domNodeObjectModels.add(new DOMNodeObjectModel(node));
                            }
                            catch( IMFException e) {
                                imfErrorLogger.addAllErrors(e.getErrors());
                            }

                        }
                        resourcesEssenceDescriptorMap.put(resourceIdTuple.getSourceEncoding(), domNodeObjectModels);
                    }
                }
                catch( IMFException e)
                {
                    imfErrorLogger.addAllErrors(e.getErrors());
                }
            }
        }

        if( imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors()))
        {
            throw new IMFException("Failed to get Essence Descriptor for a resource", this.imfErrorLogger);
        }

        if (resourcesEssenceDescriptorMap.entrySet().size() == 0) {
            String message = "Composition does not refer to a single IMFEssence represented by the HeaderPartitions " +
                    "that were passed in.";
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, this.imfErrorLogger);
        }

        return Collections.unmodifiableMap(resourcesEssenceDescriptorMap);
    }

    private List<Node> getEssenceDescriptorDOMNodes(Composition.HeaderPartitionTuple headerPartitionTuple) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = headerPartitionTuple.getHeaderPartition().getEssenceDescriptors();
            List<Node> essenceDescriptorNodes = new ArrayList<>();
            for (InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                try {
                    KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                    List<KLVPacket.Header> subDescriptorHeaders = this.getSubDescriptorKLVHeader(headerPartitionTuple.getHeaderPartition(), essenceDescriptor);
                    /*Create a dom*/
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document document = docBuilder.newDocument();

                    DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(document, headerPartitionTuple, essenceDescriptorHeader, subDescriptorHeaders);
                    Node node = documentFragment.getFirstChild();
                    essenceDescriptorNodes.add(node);
                } catch (ParserConfigurationException e) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR,
                            IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL, e.getMessage());
                }
            }
            if(imfErrorLogger.hasFatalErrors()) {
                throw new IMFException("Failed to get Essence Descriptor for a resource", imfErrorLogger);
            }
            return essenceDescriptorNodes;

    }

    private List<KLVPacket.Header> getSubDescriptorKLVHeader(HeaderPartition headerPartition, InterchangeObject.InterchangeObjectBO essenceDescriptor) {
        List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors(essenceDescriptor);
        for (InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors) {
            if (subDescriptorBO != null) {
                subDescriptorHeaders.add(subDescriptorBO.getHeader());
            }
        }
        return Collections.unmodifiableList(subDescriptorHeaders);
    }

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, Composition.HeaderPartitionTuple headerPartitionTuple, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header> subDescriptors) throws MXFException, IOException {
        document.setXmlStandalone(true);

        PrimerPack primerPack = headerPartitionTuple.getHeaderPartition().getPrimerPack();
        ResourceByteRangeProvider resourceByteRangeProvider = headerPartitionTuple.getResourceByteRangeProvider();
        RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(primerPack.getHeader(), getByteProvider(resourceByteRangeProvider, primerPack.getHeader()));
        Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, getByteProvider(resourceByteRangeProvider, essenceDescriptor));
        //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for (KLVPacket.Header subDescriptorHeader : subDescriptors) {
            subDescriptorTriplets.add(regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, this.getByteProvider(resourceByteRangeProvider, subDescriptorHeader)));
        }
        return regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document, this.imfErrorLogger);
    }

    private ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        return byteProvider;
    }

    private List<IMFErrorLogger.ErrorObject> conformEssenceDescriptors(Map<UUID, List<DOMNodeObjectModel>> essenceDescriptorsMap, Map<UUID, DOMNodeObjectModel> eDLMap) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /**
         * An exhaustive compare of the eDLMap and essenceDescriptorsMap is required to ensure that the essence descriptors
         * in the EssenceDescriptorList and the EssenceDescriptors in the physical essence files corresponding to the
         * same source encoding element as indicated in the TrackFileResource and EDL are a good match.
         */

        /**
         * The Maps passed in have the DOMObjectModel for every EssenceDescriptor in the EssenceDescriptorList in the CPL and
         * the essence descriptor in each of the essences referenced from every track file resource within each virtual track.
         */

        /**
         * The following check ensures that we do not have a Track Resource that does not have a corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> essenceDescriptorsMapIterator = essenceDescriptorsMap.entrySet().iterator();
        while (essenceDescriptorsMapIterator.hasNext()) {
            UUID sourceEncodingElement = essenceDescriptorsMapIterator.next().getKey();
            if (!eDLMap.keySet().contains(sourceEncodingElement)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Source Encoding " +
                        "Element %s in a track does not have a corresponding entry in the CPL's EDL.", sourceEncodingElement.toString()));
            }
        }
        Set<String> ignoreSet = new HashSet<String>();
        //ignoreSet.add("InstanceUID");
        //ignoreSet.add("InstanceID");
        //ignoreSet.add("EssenceLength");
        //ignoreSet.add("AlternativeCenterCuts");
        //ignoreSet.add("GroupOfSoundfieldGroupsLinkID");

        // PHDRMetadataTrackSubDescriptor is not present in SMPTE registries and cannot be serialized
        ignoreSet.add("PHDRMetadataTrackSubDescriptor");

        /**
         * The following check ensures that we have atleast one EssenceDescriptor in a TrackFile that equals the corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> iterator = essenceDescriptorsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DOMNodeObjectModel>> entry = (Map.Entry<UUID, List<DOMNodeObjectModel>>) iterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = entry.getValue();
            DOMNodeObjectModel referenceDOMNodeObjectModel = eDLMap.get(entry.getKey());
            if (referenceDOMNodeObjectModel == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Source Encoding " +
                        "Element %s in a track does not have a corresponding entry in the CPL's Essence Descriptor List.", entry.getKey().toString()));
            }
            else {
                referenceDOMNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(eDLMap.get(entry.getKey()), ignoreSet);
                boolean intermediateResult = false;

                List<DOMNodeObjectModel> domNodeObjectModelsIgnoreSet = new ArrayList<>();
                for (DOMNodeObjectModel domNodeObjectModel : domNodeObjectModels) {
                    domNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(domNodeObjectModel, ignoreSet);
                    domNodeObjectModelsIgnoreSet.add(domNodeObjectModel);
                    intermediateResult |= referenceDOMNodeObjectModel.equals(domNodeObjectModel);
                }
                if (!intermediateResult) {
                    DOMNodeObjectModel matchingDOMNodeObjectModel = DOMNodeObjectModel.getMatchingDOMNodeObjectModel(referenceDOMNodeObjectModel, domNodeObjectModelsIgnoreSet);
                    imfErrorLogger.addAllErrors(DOMNodeObjectModel.getNamespaceURIMismatchErrors(referenceDOMNodeObjectModel, matchingDOMNodeObjectModel));

                    String domNodeName = referenceDOMNodeObjectModel.getLocalName();
                    List<DOMNodeObjectModel> domNodeObjectModelList = domNodeObjectModelsIgnoreSet.stream().filter( e -> e.getLocalName().equals(domNodeName)).collect(Collectors.toList());
                    if(domNodeObjectModelList.size() != 0)
                    {
                        DOMNodeObjectModel diffCPLEssenceDescriptor = referenceDOMNodeObjectModel.removeNodes(domNodeObjectModelList.get(0));
                        DOMNodeObjectModel diffTrackFileEssenceDescriptor = domNodeObjectModelList.get(0).removeNodes(referenceDOMNodeObjectModel);
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Id %s in the CPL's " +
                                "EssenceDescriptorList doesn't match any EssenceDescriptors within the IMFTrackFile resource that references it, " +
                                "%n%n EssenceDescriptor in CPL EssenceDescriptorList with mismatching fields is as follows %n%s, %n%nEssenceDescriptor found in the " +
                                "TrackFile resource with mismatching fields is as follows %n%s%n%n",
                                entry.getKey().toString(), diffCPLEssenceDescriptor.toString(), diffTrackFileEssenceDescriptor.toString()));
                    }
                    else {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Id %s in the CPL's " +
                                "EssenceDescriptorList doesn't match any EssenceDescriptors within the IMFTrackFile resource that references it, " +
                                "%n%n EssenceDescriptor in CPL EssenceDescriptorList is as follows %n%s, %n%nEssenceDescriptors found in the TrackFile resource %n%s%n%n",
                                entry.getKey().toString(), referenceDOMNodeObjectModel.toString(), Utilities.serializeObjectCollectionToString(domNodeObjectModelsIgnoreSet)));
                    }
                }
            }
        }

        return imfErrorLogger.getErrors();
    }

    public @Nullable CompositionImageEssenceDescriptorModel getCompositionImageEssenceDescriptorModel() {
        CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = null;
        DOMNodeObjectModel imageEssencedescriptorDOMNode = this.getEssenceDescriptor(
                this.getVideoVirtualTrack().getTrackResourceIds().iterator().next());

        if (imageEssencedescriptorDOMNode != null) {
            UUID imageEssenceDescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst()
                    .get();
            imageEssenceDescriptorModel =
                    new CompositionImageEssenceDescriptorModel(imageEssenceDescriptorID, imageEssencedescriptorDOMNode,
                            regXMLLibDictionary);
        }

        return imageEssenceDescriptorModel;
    }

    private  Map<UUID, List<Node>> createEssenceDescriptorDomNodeMap() {
        final Map<UUID, List<Node>> essenceDescriptorDomNodeMap = new HashMap<>();
        if (compositionPlaylistType.getEssenceDescriptorList() != null) {
            Map<UUID, UUID> essenceDescriptorIdToTrackFileIdMap = new HashMap<>();
            for(ResourceIdTuple resourceIdTuple : getResourceIdTuples(this.getVirtualTracks())) {
                essenceDescriptorIdToTrackFileIdMap.put(resourceIdTuple.getSourceEncoding(), resourceIdTuple.getTrackFileId());
            }
            for(IMFEssenceDescriptorBaseType imfEssenceDescriptorBaseType : compositionPlaylistType.getEssenceDescriptorList()) {
                if(essenceDescriptorIdToTrackFileIdMap.containsKey(imfEssenceDescriptorBaseType.getId())) {
                    essenceDescriptorDomNodeMap.put(essenceDescriptorIdToTrackFileIdMap.get(imfEssenceDescriptorBaseType.getId()), imfEssenceDescriptorBaseType.getAny().stream().map(e -> (Node) e).collect(Collectors.toList()));
                }
            }
        }
        return essenceDescriptorDomNodeMap;
    }


    public Map<UUID, List<Node>> getEssenceDescriptorDomNodeMap() {
        return this.essenceDescriptorDomNodeMap;
    }


}