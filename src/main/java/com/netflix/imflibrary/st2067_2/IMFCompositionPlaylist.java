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

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.utils.*;

import com.sandflow.smpte.klv.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas._2067_3._2016.CompositionPlaylistType.ExtensionProperties;
import org.w3c.dom.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This interface represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
public class IMFCompositionPlaylist {

    private static final Logger logger = LoggerFactory.getLogger(IMFCompositionPlaylist.class);

    private final Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap;
    private final Map<UUID, List<Node>> essenceDescriptorDomNodeMap;

    private final IMFErrorLogger imfErrorLogger;
    private final RegXMLLibDictionary regXMLLibDictionary;

    private final UUID id;
    private final Composition.EditRate editRate;
    private final String annotation;
    private final String issuer;
    private final String creator;
    private final String contentOriginator;
    private final String contentTitle;
    private final String coreConstraintsSchema;
    private final List<IMFSegmentType> segmentList;
    private final List<IMFEssenceDescriptorBaseType> essenceDescriptorList;
    private final Set<String> applicationIdSet;
    private final ExtensionProperties extensionProperties;


    static class Builder {
        private IMFErrorLogger imfErrorLogger;
        private String coreConstraintsSchema;
        private UUID id;
        private Composition.EditRate editRate;
        private String annotation;
        private String issuer;
        private String creator;
        private String contentOriginator;
        private String contentTitle;
        private List<IMFSegmentType> segmentList;
        private List<IMFEssenceDescriptorBaseType> essenceDescriptorList;
        private Set<String> applicationIdSet;
        private ExtensionProperties extensionProperties;

        Builder imfErrorLogger(IMFErrorLogger imfErrorLogger) {
            this.imfErrorLogger = imfErrorLogger;
            return this;
        }

        Builder coreConstraintsSchema(String coreConstraintsSchema) {
            this.coreConstraintsSchema = coreConstraintsSchema;
            return this;
        }

        Builder id(UUID id) {
            this.id = id;
            return this;
        }

        Builder editRate(Composition.EditRate editRate) {
            this.editRate = editRate;
            return this;
        }

        Builder annotation(String annotation) {
            this.annotation = annotation;
            return this;
        }

        Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        Builder creator(String creator) {
            this.creator = creator;
            return this;
        }

        Builder contentOriginator(String contentOriginator) {
            this.contentOriginator = contentOriginator;
            return this;
        }

        Builder contentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        Builder segmentList(List<IMFSegmentType> segmentList) {
            this.segmentList = segmentList;
            return this;
        }

        Builder essenceDescriptorList(List<IMFEssenceDescriptorBaseType> essenceDescriptorList) {
            this.essenceDescriptorList = essenceDescriptorList;
            return this;
        }

        Builder applicationIdSet(Set<String> applicationIdSet) {
            this.applicationIdSet = applicationIdSet;
            return this;
        }

        Builder extensionProperties(ExtensionProperties extensionProperties) {
            this.extensionProperties = extensionProperties;
            return this;
        }

        IMFCompositionPlaylist build() {

            if (imfErrorLogger == null)
                imfErrorLogger = new IMFErrorLoggerImpl();

            return new IMFCompositionPlaylist(this);
        }
    }



    /**
     * Constructor for a {@link IMFCompositionPlaylist Composition} object from a XML file
     *
     * @param compositionPlaylistXMLFile the input XML file that is conformed to schema and constraints specified in st2067-3:2013 and st2067-2:2013
     * @throws IOException        any I/O related error is exposed through an IOException
     */
    public IMFCompositionPlaylist(Path compositionPlaylistXMLFile) throws IOException {
        this(new FileByteRangeProvider(compositionPlaylistXMLFile));
    }

    /**
     * Constructor for a {@link IMFCompositionPlaylist Composition} object from a XML file
     *
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     *                                  if any {@link IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     * @throws IOException        any I/O related error is exposed through an IOException
     */
    public IMFCompositionPlaylist(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        this(getIMFCompositionPlaylistBuilder(resourceByteRangeProvider));
    }

    /**
     * Constructor for a {@link IMFCompositionPlaylist Composition} object from a XML file
     *
     * @param builder corresponding to the Composition XML file.
     *                                  if any {@link IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     */
    private IMFCompositionPlaylist(Builder builder) {
        this.coreConstraintsSchema = builder.coreConstraintsSchema;
        this.id = builder.id;
        this.editRate = builder.editRate;
        this.annotation = builder.annotation;
        this.issuer = builder.issuer;
        this.creator = builder.creator;
        this.contentOriginator = builder.contentOriginator;
        this.contentTitle = builder.contentTitle;
        this.segmentList = builder.segmentList;
        this.essenceDescriptorList = builder.essenceDescriptorList;
        this.applicationIdSet = builder.applicationIdSet;
        this.extensionProperties = builder.extensionProperties;
        imfErrorLogger = builder.imfErrorLogger;

        this.regXMLLibDictionary = new RegXMLLibDictionary();
        this.virtualTrackMap = this.getVirtualTracksMap(imfErrorLogger);
        this.essenceDescriptorDomNodeMap = Collections.unmodifiableMap(createEssenceDescriptorDomNodeMap());

        // Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap= this.getEssenceDescriptorListMap(ignoreSet);
    }





    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     *
     * @param imfErrorLogger          - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding Composition.VirtualTrack
     */
    private Map<UUID, Composition.VirtualTrack> getVirtualTracksMap(@Nonnull IMFErrorLogger imfErrorLogger) {
        Map<UUID, Composition.VirtualTrack> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<IMFBaseResourceType>> virtualTrackResourceMap = getVirtualTrackResourceMap(imfErrorLogger);

        //process first segment to create virtual track map
        IMFSegmentType segment = getSegmentList().get(0);
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
                                getEditRate());
                    } else if (virtualTrackResourceList.get(0) instanceof IMFMarkerResourceType) {
                        virtualTrack = new IMFMarkerVirtualTrack(uuid,
                                sequence.getType(),
                                (List<IMFMarkerResourceType>) virtualTrackResourceList,
                                getEditRate());
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

        // todo: move call to somewhere else
        //IMFCoreConstraintsChecker.checkSegments(this, imfErrorLogger);

        return virtualTrackMap;
    }

    /**
     * A stateless method that completely reads and parses the resources of all the Composition.VirtualTracks that are a part of the Composition
     *
     * @param imfErrorLogger          - an object for logging errors
     * @return map of VirtualTrack identifier to the list of all the Track's resources, for every Composition.VirtualTrack of the Composition
     */
    private Map<UUID, List<IMFBaseResourceType>> getVirtualTrackResourceMap(@Nonnull IMFErrorLogger imfErrorLogger) {
        Map<UUID, List<IMFBaseResourceType>> virtualTrackResourceMap = new LinkedHashMap<>();
        for (IMFSegmentType segment : getSegmentList()) {
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
     * Getter for the SegmentList of the Composition Playlist
     * @return a string representing the SegmentList of the Composition Playlist
     */
    public List<IMFSegmentType> getSegmentList(){
        return this.segmentList;
    }



    /**
     * A method that returns a string representation of a Composition object
     *
     * @return string representing the object
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("======= Composition : %s =======%n", this.getId()));
        sb.append(this.getEditRate().toString());
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
        try (SeekableByteChannel byteChannel = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);
             InputStream inputStream = Channels.newInputStream(byteChannel);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for (String cplNamespaceURI : supportedCPLSchemaURIs) {
                nodeList = document.getElementsByTagNameNS(cplNamespaceURI, "CompositionPlaylist");
                if (nodeList != null
                        && nodeList.getLength() == 1) {
                    return true;
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            return false;
        }

        return false;
    }


    /**
     * Getter for the Composition Playlist ID
     * @return a string representing the urn:uuid of the Composition Playlist
     */
    public UUID getId(){
        return this.id;
    }


    /**
     * Getter for the composition edit rate as specified in the Composition XML file
     *
     * @return the edit rate associated with the Composition
     */
    public Composition.EditRate getEditRate() {
        return this.editRate;
    }

    /**
     * Getter method for Annotation child element of CompositionPlaylist
     *
     * @return value of Annotation child element or null if it is not exist
     */
    public
    @Nullable
    String getAnnotation() {
        return this.annotation;
    }

    /**
     * Getter method for Issuer child element of CompositionPlaylist
     *
     * @return value of Issuer child element or null if it is not exist
     */
    public
    @Nullable
    String getIssuer() {
        return this.issuer;
    }

    /**
     * Getter method for Creator child element of CompositionPlaylist
     *
     * @return value of Creator child element or null if it is not exist
     */
    public
    @Nullable
    String getCreator() {
        return this.creator;
    }

    /**
     * Getter method for ContentOriginator child element of CompositionPlaylist
     *
     * @return value of ContentOriginator child element or null if it is not exist
     */
    public
    @Nullable
    String getContentOriginator() {
        return this.contentOriginator;
    }

    /**
     * Getter method for ContentTitle child element of CompositionPlaylist
     *
     * @return value of ContentTitle child element or null if it is not exist
     */
    public
    @Nullable
    String getContentTitle() {
        return this.contentTitle;
    }

    /**
     * Getter for the virtual track map associated with this Composition
     *
     * @return {@link Map Map}&lt;{@link UUID UUID},{@link Composition.VirtualTrack VirtualTrack}&gt;. The UUID key corresponds to VirtualTrackID
     */
    public Map<UUID, ? extends Composition.VirtualTrack> getVirtualTrackMap() {
        return Collections.unmodifiableMap(this.virtualTrackMap);
    }

    /**
     * Getter for the UUID corresponding to this Composition document
     *
     * @return the uuid of this Composition object
     */
    public UUID getUUID() {
        return this.id;
    }


    /**
     * Getter for the set of Application Identifications corresponding to this Composition document
     *
     * @return the set of Application Identifications
     */
    public Set<String> getApplicationIdSet() {
        return this.applicationIdSet;
    }

    /**
     * Getter for the set of Sequence Namespaces present in this Composition document
     *
     * @return the set of Sequence Namespaces
     */
    public Set<String> getSequenceNamespaceSet() {

        Set<String> sequenceNamespaceSet = new HashSet<>();
        List<IMFSegmentType> segments = getSegmentList();

        if (segments != null) {
            segments.forEach(segment -> {
                segment.getSequenceList().forEach(sequence -> {
                    sequenceNamespaceSet.add(sequence.namespace);
                });
            });
        }

        return sequenceNamespaceSet;
    }


    /**
     * Getter for the ExtensionProperties corresponding to this Composition document
     *
     * @return value of ExtensionProperties of this Composition object
     */
    public org.smpte_ra.schemas._2067_3._2016.CompositionPlaylistType.ExtensionProperties getExtensionProperties() {
        return this.extensionProperties;
    }

    /**
     * Getter for the EssenceDescriptorlist of the Composition Playlist
     * @return a string representing the EssenceDescriptorlist of the Composition Playlist
     */
    public List<IMFEssenceDescriptorBaseType> getEssenceDescriptorList(){
        return this.essenceDescriptorList;
    }

    /**
     * Getter for the Core Constraints schema URI.
     * @return URI for the Core Constraints schema
     */
    @Nonnull public String getCoreConstraintsSchema() {
        return this.coreConstraintsSchema;
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

    public static List<ErrorLogger.ErrorObject> validateCompositionPlaylistSchema(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {

        Builder builder = getIMFCompositionPlaylistBuilder(resourceByteRangeProvider);
        return builder.imfErrorLogger.getErrors();
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
    public static List<IMFCompositionPlaylist.ResourceIdTuple> getVirtualTrackResourceIDs(@Nonnull Composition.VirtualTrack virtualTrack) {

        List<IMFCompositionPlaylist.ResourceIdTuple> virtualTrackResourceIDs = new ArrayList<>();

        List<? extends IMFBaseResourceType> resourceList = virtualTrack.getResourceList();
        if (resourceList != null
                && resourceList.size() > 0 &&
                virtualTrack.getResourceList().get(0) instanceof IMFTrackFileResourceType) {

            for (IMFBaseResourceType baseResource : resourceList) {
                IMFTrackFileResourceType trackFileResource = IMFTrackFileResourceType.class.cast(baseResource);

                virtualTrackResourceIDs.add(new IMFCompositionPlaylist.ResourceIdTuple(UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResource.getTrackFileId())
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
        if (getEssenceDescriptorList() != null) {
            List<IMFEssenceDescriptorBaseType> essenceDescriptors = getEssenceDescriptorList();
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
    public Map<UUID, DOMNodeObjectModel> getEssenceDescriptorListMap() {
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


    public Set<UUID> getEssenceDescriptorIdsSet() {
        HashSet<UUID> essenceDescriptorIdsSet = new LinkedHashSet<>();
        if (getEssenceDescriptorList() != null) {
            List<IMFEssenceDescriptorBaseType> essenceDescriptorList = getEssenceDescriptorList();
            for (IMFEssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptorList) {
                UUID sourceEncodingElement = essenceDescriptorBaseType.getId();
                /*Construct a set of SourceEncodingElements/IDs corresponding to every EssenceDescriptorBaseType in the EssenceDescriptorList*/
                essenceDescriptorIdsSet.add(sourceEncodingElement);
            }
        }
        return essenceDescriptorIdsSet;
    }


    public Set<UUID> getResourceEssenceDescriptorIdsSet() {
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(this.getVirtualTrackMap().values());
        LinkedHashSet<UUID> resourceSourceEncodingElementsSet = new LinkedHashSet<>();
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            List<IMFCompositionPlaylist.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);
            for (IMFCompositionPlaylist.ResourceIdTuple resourceIdTuple : resourceIdTuples) {
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        return resourceSourceEncodingElementsSet;
    }

    public static List<IMFCompositionPlaylist.ResourceIdTuple> getResourceIdTuples(List<? extends Composition.VirtualTrack> virtualTracks) {
        List<IMFCompositionPlaylist.ResourceIdTuple>  resourceIdTupleList = new ArrayList<>();
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            resourceIdTupleList.addAll(getVirtualTrackResourceIDs(virtualTrack));
        }
        return resourceIdTupleList;
    }

    public Map<UUID, List<DOMNodeObjectModel>> getResourcesEssenceDescriptorsMap(List<Composition.HeaderPartitionTuple> headerPartitionTuples) throws IOException {
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
            List<IMFCompositionPlaylist.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);/*Retrieve a list of ResourceIDTuples corresponding to this virtual track*/
            for (IMFCompositionPlaylist.ResourceIdTuple resourceIdTuple : resourceIdTuples)
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


    @Nonnull
    public List<CompositionImageEssenceDescriptorModel> getCompositionImageEssenceDescriptorModels() {

        List<CompositionImageEssenceDescriptorModel> imageEssenceDescriptorModels = new ArrayList<>();

        this.getVideoVirtualTrack().getTrackResourceIds().forEach(id -> {
            DOMNodeObjectModel imageEssencedescriptorDOMNode = this.getEssenceDescriptor(id);

            if (imageEssencedescriptorDOMNode != null) {
                UUID imageEssenceDescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst().get();
                CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = new CompositionImageEssenceDescriptorModel(imageEssenceDescriptorID, imageEssencedescriptorDOMNode, regXMLLibDictionary);
                imageEssenceDescriptorModels.add(imageEssenceDescriptorModel);
            }
        });

        return imageEssenceDescriptorModels;
    }

    private  Map<UUID, List<Node>> createEssenceDescriptorDomNodeMap() {
        final Map<UUID, List<Node>> essenceDescriptorDomNodeMap = new HashMap<>();
        if (getEssenceDescriptorList() != null) {
            Map<UUID, UUID> essenceDescriptorIdToTrackFileIdMap = new HashMap<>();
            for(IMFCompositionPlaylist.ResourceIdTuple resourceIdTuple : getResourceIdTuples(this.getVirtualTracks())) {
                essenceDescriptorIdToTrackFileIdMap.put(resourceIdTuple.getSourceEncoding(), resourceIdTuple.getTrackFileId());
            }
            for(IMFEssenceDescriptorBaseType imfEssenceDescriptorBaseType : getEssenceDescriptorList()) {
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




    private static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-3/2013");
        add("http://www.smpte-ra.org/schemas/2067-3/2016");
    }});



    @Nonnull
    private static final String getCompositionNamespaceURI(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {

        String result = "";

        try (SeekableByteChannel byteChannel = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);
             InputStream inputStream = Channels.newInputStream(byteChannel);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, exception.getMessage()));
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, exception.getMessage()));
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, exception.getMessage()));
                }
            });
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for (String cplNamespaceURI : supportedCPLSchemaURIs) {
                nodeList = document.getElementsByTagNameNS(cplNamespaceURI, "CompositionPlaylist");
                if (nodeList != null && nodeList.getLength() == 1) {
                    result = cplNamespaceURI;
                    break;
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            String message = String.format("Error occurred while trying to determine the Composition Playlist " +
                    "Namespace URI, XML document appears to be invalid. Error Message : %s", e.getMessage());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        if (result.isEmpty()) {
            String message = String.format("Please check the CPL document and namespace URI, currently we only " +
                    "support the following schema URIs %s", Utilities.serializeObjectCollectionToString
                    (supportedCPLSchemaURIs));
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        return result;
    }



    private static IMFCompositionPlaylist.Builder getIMFCompositionPlaylistBuilder(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        // Determine which version of the CPL namespace is being used
        String cplNamespace = getCompositionNamespaceURI(resourceByteRangeProvider, imfErrorLogger);

        if (cplNamespace.equals("http://www.smpte-ra.org/schemas/2067-3/2013"))
        {
            org.smpte_ra.schemas._2067_3._2013.CompositionPlaylistType jaxbCpl
                    = CompositionModel_st2067_2_2013.unmarshallCpl(resourceByteRangeProvider, imfErrorLogger);

            return CompositionModel_st2067_2_2013.getApplicationCompositionBuilder(jaxbCpl, imfErrorLogger);
        }
        else if (cplNamespace.equals("http://www.smpte-ra.org/schemas/2067-3/2016"))
        {
            org.smpte_ra.schemas._2067_3._2016.CompositionPlaylistType jaxbCpl
                    = CompositionModel_st2067_2_2016.unmarshallCpl(resourceByteRangeProvider, imfErrorLogger);

            return CompositionModel_st2067_2_2016.getApplicationCompositionBuilder(jaxbCpl, imfErrorLogger);
        }
        else
        {
            String message = String.format("Please check the CPL document and namespace URI, currently we " +
                    "only support the following schema URIs %s", supportedCPLSchemaURIs);
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                    .IMFErrors.ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
    }




}