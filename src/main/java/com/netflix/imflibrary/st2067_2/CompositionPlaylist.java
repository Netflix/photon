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

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.imp_validation.DOMNodeObjectModel;
import com.netflix.imflibrary.imp_validation.cpl.CompositionPlaylistHelper;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.RepeatableInputStream;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st2067_2_2013.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.annotation.concurrent.Immutable;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

/**
 * This class represents a thin, immutable wrapper around the XML type 'CompositionPlaylistType' which is defined in Section 6.1,
 * st2067-3:2013. A CompositionPlaylist object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3:2013 and st2067-2:2013
 */
@Immutable
public final class CompositionPlaylist
{
    private static final Logger logger = LoggerFactory.getLogger(CompositionPlaylist.class);

    private static final String imf_core_constraints_schema_path = "/org/smpte_ra/schemas/st2067_2_2013/imf-core-constraints-20130620-pal.xsd";
    private static final String imf_cpl_schema_path = "/org/smpte_ra/schemas/st2067_3_2013/imf-cpl.xsd";
    private static final String dcmlTypes_schema_path = "/org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd";
    private static final String xmldsig_core_schema_path = "/org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    public static final List<String> supportedCPLSchemaURIs = Collections.unmodifiableList(new ArrayList<String>(){{ add("http://www.smpte-ra.org/schemas/2067-3/2013");}});

    private final CompositionPlaylistType compositionPlaylistType;
    private final UUID uuid;
    private final EditRate editRate;
    private final Map<UUID, VirtualTrack> virtualTrackMap;
    private final Map<UUID, List<TrackFileResourceType>> virtualTrackResourceMap;

    /**
     * Constructor for a {@link com.netflix.imflibrary.st2067_2.CompositionPlaylist CompositionPlaylist} object from a XML file
     * @param compositionPlaylistXMLFile the input XML file that is conformed to schema and constraints specified in st2067-3:2013 and st2067-2:2013
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public CompositionPlaylist(File compositionPlaylistXMLFile, @Nonnull IMFErrorLogger imfErrorLogger)  throws IOException, SAXException, JAXBException, URISyntaxException {
        int numErrors = imfErrorLogger.getNumberOfErrors();

        CompositionPlaylist.validateCompositionPlaylistSchema(compositionPlaylistXMLFile, imfErrorLogger);

        try(InputStream input = new FileInputStream(compositionPlaylistXMLFile);
            InputStream xmldsig_core_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.xmldsig_core_schema_path);
            InputStream dcmlTypes_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.dcmlTypes_schema_path);
            InputStream imf_cpl_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.imf_cpl_schema_path);
            InputStream imf_core_constraints_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.imf_core_constraints_schema_path);)
        {
            StreamSource[] streamSources = new StreamSource[4];
            streamSources[0] = new StreamSource(xmldsig_core_is);
            streamSources[1] = new StreamSource(dcmlTypes_is);
            streamSources[2] = new StreamSource(imf_cpl_is);
            streamSources[3] = new StreamSource(imf_core_constraints_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st2067_2_2013");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<CompositionPlaylistType> compositionPlaylistTypeJAXBElement = (JAXBElement) unmarshaller.unmarshal(input);
            if (validationEventHandlerImpl.hasErrors())
            {
                throw new IMFException(validationEventHandlerImpl.toString());
            }

            CompositionPlaylistType compositionPlaylistType = compositionPlaylistTypeJAXBElement.getValue();
            this.compositionPlaylistType = compositionPlaylistType;
            this.virtualTrackMap = checkVirtualTracks(this.compositionPlaylistType, imfErrorLogger);
        }

        this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(this.compositionPlaylistType.getId());

        this.editRate = new EditRate(this.compositionPlaylistType.getEditRate());

        this.virtualTrackResourceMap = populateVirtualTrackResourceList(this.compositionPlaylistType, imfErrorLogger);

        if ((imfErrorLogger != null) && (imfErrorLogger.getNumberOfErrors() > numErrors))
        {
            throw new IMFException(String.format("Found %d errors in CompositionPlaylist XML file", imfErrorLogger.getNumberOfErrors() - numErrors));
        }
    }

    /**
     * Constructor for a {@link com.netflix.imflibrary.st2067_2.CompositionPlaylist CompositionPlaylist} object from a XML file
     * @param resourceByteRangeProvider corresponding to the CompositionPlaylist XML file.
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public CompositionPlaylist(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger)  throws IOException, SAXException, JAXBException, URISyntaxException {

        int numErrors = imfErrorLogger.getNumberOfErrors();

        CompositionPlaylist.validateCompositionPlaylistSchema(resourceByteRangeProvider, imfErrorLogger);

        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() -1);
            InputStream xmldsig_core_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.xmldsig_core_schema_path);
            InputStream dcmlTypes_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.dcmlTypes_schema_path);
            InputStream imf_cpl_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.imf_cpl_schema_path);
            InputStream imf_core_constraints_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.imf_core_constraints_schema_path);)
        {
            StreamSource[] streamSources = new StreamSource[4];
            streamSources[0] = new StreamSource(xmldsig_core_is);
            streamSources[1] = new StreamSource(dcmlTypes_is);
            streamSources[2] = new StreamSource(imf_cpl_is);
            streamSources[3] = new StreamSource(imf_core_constraints_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st2067_2_2013");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<CompositionPlaylistType> compositionPlaylistTypeJAXBElement = (JAXBElement) unmarshaller.unmarshal(inputStream);
            if (validationEventHandlerImpl.hasErrors())
            {
                throw new IMFException(validationEventHandlerImpl.toString());
            }

            CompositionPlaylistType compositionPlaylistType = compositionPlaylistTypeJAXBElement.getValue();
            this.compositionPlaylistType = compositionPlaylistType;
            this.virtualTrackMap = checkVirtualTracks(this.compositionPlaylistType, imfErrorLogger);
        }

        this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(this.compositionPlaylistType.getId());

        this.editRate = new EditRate(this.compositionPlaylistType.getEditRate());

        this.virtualTrackResourceMap = populateVirtualTrackResourceList(this.compositionPlaylistType, imfErrorLogger);

        if ((imfErrorLogger != null) && (imfErrorLogger.getNumberOfErrors() > numErrors))
        {
            throw new IMFException(String.format("Found %d errors in CompositionPlaylist XML file", imfErrorLogger.getNumberOfErrors() - numErrors));
        }
    }

    /**
     * A stateless method that verifies if the raw data represented by the ResourceByteRangeProvider corresponds to a valid
     * IMF Composition Playlist document
     * @param resourceByteRangeProvider - a byte range provider for the document that needs to be verified
     * @return - a boolean indicating if the document represented is an IMF CompositionPlaylist or not
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isFileOfSupportedSchema(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException{

        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            NodeList nodeList = null;
            for(String supportedSchemaURI : supportedCPLSchemaURIs) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, "CompositionPlaylist");
                if (nodeList != null
                        && nodeList.getLength() == 1)
                {
                    return true;
                }
            }
        }
        catch(ParserConfigurationException | SAXException e)
        {
            return false;
        }

        return false;
    }

    /**
     * A method that returns a string representation of a CompositionPlaylist object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("======= CompositionPlaylist : %s =======%n", this.uuid));
        sb.append(this.editRate.toString());
        return sb.toString();
    }

    /**
     * A method that confirms if a file is a CompositionPlaylist document instance.
     * @param xmlFile the input file that is to be verified
     * @return a boolean indicating if the input file is a CompositionPlaylist document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isCompositionPlaylist(File xmlFile) throws IOException {
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(xmlFile);
        return isCompositionPlaylist(resourceByteRangeProvider);
    }

    /**
     * A method that confirms if the inputStream corresponds to a CompositionPlaylist document instance.
     * @param resourceByteRangeProvider corresponding to the CompositionPlaylist XML file.
     * @return a boolean indicating if the input file is a CompositionPlaylist document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isCompositionPlaylist(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException{
        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for(String cplNamespaceURI : CompositionPlaylist.supportedCPLSchemaURIs) {
                nodeList = document.getElementsByTagNameNS(cplNamespaceURI, "CompositionPlaylist");
                if (nodeList != null
                        && nodeList.getLength() == 1)
                {
                    return true;
                }
            }
        }
        catch(ParserConfigurationException | SAXException e)
        {
            return false;
        }

        return false;
    }

    /**
     * Getter for the composition edit rate as specified in the CompositionPlaylist XML file
     * @return the edit rate associated with the Composition
     */
    public EditRate getEditRate()
    {
        return this.editRate;
    }

    /**
     * Getter for the lists of type {@link org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType TrackFileResourceType} indexed by the VirtualTrackID.
     * The VirtualTrack concept is defined in Section 6.9.3 of st2067-3:2013.
     * @return Map&lt;UUID,List &lt;{@link org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType TrackFileResourceType}&gt;&gt;. The UUID key corresponds to VirtualTrackID
     */
    public Map<UUID, List<TrackFileResourceType>> getVirtualTrackResourceMap()
    {
        return Collections.unmodifiableMap(this.virtualTrackResourceMap);
    }

    /**
     * Getter for the virtual track map associated with this CompositionPlaylist
     * @return {@link java.util.Map Map}&lt;{@link java.util.UUID UUID},{@link com.netflix.imflibrary.st2067_2.CompositionPlaylist.VirtualTrack VirtualTrack}&gt;. The UUID key corresponds to VirtualTrackID
     */
    public Map<UUID, VirtualTrack> getVirtualTrackMap()
    {
        return Collections.unmodifiableMap(this.virtualTrackMap);
    }

    /**
     * Getter for the UUID corresponding to this CompositionPlaylist document
     * @return the uuid of this CompositionPlaylist object
     */
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * Getter for the CompositionPlaylistType object model of the CompositionPlaylist defined by the st2067-3 schema.
     * @return the composition playlist type object model.
     */
    public CompositionPlaylistType getCompositionPlaylistType(){
        return this.compositionPlaylistType;
    }

    /**
     * Getter for the video VirtualTrack in this Composition
     * @return the video virtual track that is a part of this composition or null if there is not video virtual track
     */
    @Nullable
    public VirtualTrack getVideoVirtualTrack(){
        Set<Map.Entry<UUID, CompositionPlaylist.VirtualTrack>> virtualTrackMapEntrySet =  this.getVirtualTrackMap().entrySet();
        Iterator iterator = virtualTrackMapEntrySet.iterator();
        while(iterator != null
                && iterator.hasNext()) {
            CompositionPlaylist.VirtualTrack virtualTrack = ((Map.Entry<UUID, CompositionPlaylist.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MainImageSequence)) {
                return virtualTrack;
            }
        }
        return null;
    }

    /**
     * Getter for the audio VirtualTracks in this Composition
     * @return a list of audio virtual tracks that are a part of this composition or an empty list if there are none
     */
    public List<VirtualTrack> getAudioVirtualTracks(){
        List<VirtualTrack> audioVirtualTracks = new ArrayList<>();
        Set<Map.Entry<UUID, CompositionPlaylist.VirtualTrack>> virtualTrackMapEntrySet =  this.getVirtualTrackMap().entrySet();
        Iterator iterator = virtualTrackMapEntrySet.iterator();
        while(iterator != null
                && iterator.hasNext()) {
            CompositionPlaylist.VirtualTrack virtualTrack = ((Map.Entry<UUID, CompositionPlaylist.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MainAudioSequence)) {
                audioVirtualTracks.add(virtualTrack);
            }
        }
        return Collections.unmodifiableList(audioVirtualTracks);
    }

    Map<UUID, VirtualTrack> checkVirtualTracks(CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, VirtualTrack> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<TrackFileResourceType>>virtualTrackResourceMap =  this.populateVirtualTrackResourceList(compositionPlaylistType, imfErrorLogger);

        boolean foundMainImageEssence = false;

        //process first segment to create virtual track map
        SegmentType segment = compositionPlaylistType.getSegmentList().getSegment().get(0);
        SequenceType sequence;
        sequence = segment.getSequenceList().getMarkerSequence();
        if (sequence != null)
        {
            UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
            if (virtualTrackMap.get(uuid) == null)
            {
                List<TrackFileResourceType> virtualTrackResourceList = null;
                if(virtualTrackResourceMap.get(uuid) == null){
                    virtualTrackResourceList = new ArrayList<TrackFileResourceType>();
                }
                else{
                    virtualTrackResourceList = virtualTrackResourceMap.get(uuid);
                }
                VirtualTrack virtualTrack = new VirtualTrack(uuid, virtualTrackResourceList, SequenceTypeEnum.MarkerSequence);
                virtualTrackMap.put(uuid, virtualTrack);
            }
            else
            {
                String message = String.format(
                        "First segment in CompositionPlaylist XML file has multiple occurrences of virtual track UUID %s", uuid);
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }
            }
        }

        for (Object object : segment.getSequenceList().getAny())
        {
            if(!(object instanceof JAXBElement)){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                continue;
            }
            JAXBElement jaxbElement = (JAXBElement)(object);
            String name = jaxbElement.getName().getLocalPart();
            sequence = (SequenceType)(jaxbElement).getValue();
            if (sequence != null)
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                if (virtualTrackMap.get(uuid) == null)
                {
                    List<TrackFileResourceType> virtualTrackResourceList = null;
                    if(virtualTrackResourceMap.get(uuid) == null){
                        virtualTrackResourceList = new ArrayList<TrackFileResourceType>();
                    }
                    else{
                        virtualTrackResourceList = virtualTrackResourceMap.get(uuid);
                    }
                    checkTrackResourceList(virtualTrackResourceList, imfErrorLogger);
                    VirtualTrack virtualTrack = new VirtualTrack(uuid, virtualTrackResourceList, SequenceTypeEnum.getSequenceTypeEnum(name));
                    virtualTrackMap.put(uuid, virtualTrack);
                    if(SequenceTypeEnum.getSequenceTypeEnum(name) == SequenceTypeEnum.MainImageSequence){
                        foundMainImageEssence = true;
                        EditRate compositionEditRate = new EditRate(this.compositionPlaylistType.getEditRate());
                        for(TrackFileResourceType trackFileResourceType : virtualTrackResourceList){
                            EditRate trackResourceEditRate = new EditRate(trackFileResourceType.getEditRate());
                            if(!trackResourceEditRate.equals(compositionEditRate)){
                                throw new IMFException(String.format("This CompositionPlaylist is invalid since the CompositionEditRate %s is not the same as atleast one of the MainImageSequence's Resource EditRate %s. Please refer to st2067-2:2013 Section 6.4", this.editRate.toString(), trackResourceEditRate.toString()));
                            }
                        }
                    }
                }
                else
                {
                    String message = String.format(
                            "First segment in CompositionPlaylist XML file has multiple occurrences of virtual track UUID %s", uuid);
                    if (imfErrorLogger != null)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                    }
                    else
                    {
                        throw new IMFException(message);
                    }
                }
            }

        }

        checkSegments(compositionPlaylistType, virtualTrackMap, imfErrorLogger);

        //TODO : Add a check to ensure that all the VirtualTracks have the same duration.

        if(!foundMainImageEssence){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("CPL Id %s does not reference a single image essence", this.getUUID().toString()));
        }

        return virtualTrackMap;
    }

    void checkSegments(CompositionPlaylistType compositionPlaylistType, Map<UUID, VirtualTrack> virtualTrackMap, @Nullable IMFErrorLogger imfErrorLogger)
    {
        for (SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            Set<UUID> trackIDs = new HashSet<>();
            SequenceType sequence;
            sequence = segment.getSequenceList().getMarkerSequence();
            if (sequence != null)
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                trackIDs.add(uuid);
                if (virtualTrackMap.get(uuid) == null)
                {
                    String message = String.format(
                            "A segment in CompositionPlaylist XML file does not contain virtual track UUID %s", uuid);
                    if (imfErrorLogger != null)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                    }
                    else
                    {
                        throw new IMFException(message);
                    }
                }
            }

            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                sequence = (SequenceType)(jaxbElement).getValue();
                if (sequence != null)
                {
                    UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                    trackIDs.add(uuid);
                    if (virtualTrackMap.get(uuid) == null)
                    {
                        String message = String.format(
                                "A segment in CompositionPlaylist XML file does not contain virtual track UUID %s", uuid);
                        if (imfErrorLogger != null)
                        {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                        }
                        else
                        {
                            throw new IMFException(message);
                        }
                    }
                }
            }

            if (trackIDs.size() != virtualTrackMap.size())
            {
                String message = String.format(
                        "Number of distinct virtual trackIDs in a segment = %s, different from first segment %d", trackIDs.size(), virtualTrackMap.size());
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }
            }

        }
    }

    private Map<UUID, List<TrackFileResourceType>> populateVirtualTrackResourceList(@Nonnull CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, List<TrackFileResourceType>> virtualTrackResourceMap = new LinkedHashMap<>();
        for (SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {

            SequenceType sequence;
            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                sequence = (SequenceType)(jaxbElement).getValue();
                if (sequence != null)
                {
                    UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                    /**
                     * A LinkedList seems appropriate since we want to preserve the order of the Resources referenced
                     * by a virtual track to recreate the presentation. Since the LinkedList implementation is not
                     * synchronized wrapping it around a synchronized list collection, although in this case it
                     * is perhaps not required since this method is only invoked from the context of the constructor.
                     */
                    List<TrackFileResourceType> trackFileResources = Collections.synchronizedList(new LinkedList<>());
                    for (BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        TrackFileResourceType trackFileResource = (TrackFileResourceType)resource;
                        trackFileResources.add(trackFileResource);
                    }
                    checkTrackResourceList(trackFileResources, null);
                    if (virtualTrackResourceMap.get(uuid) == null)
                    {
                        virtualTrackResourceMap.put(uuid, trackFileResources);
                    }
                    else
                    {
                        virtualTrackResourceMap.get(uuid).addAll(trackFileResources);
                    }
                }
            }
        }

        //make virtualTrackResourceMap immutable
        for(Map.Entry<UUID, List<TrackFileResourceType>> entry : virtualTrackResourceMap.entrySet())
        {
            List<TrackFileResourceType> trackFileResources = entry.getValue();
            entry.setValue(Collections.unmodifiableList(trackFileResources));
        }

        return virtualTrackResourceMap;
    }

    boolean checkTrackResourceList(List<TrackFileResourceType> virtualTrackResourceList, @Nullable IMFErrorLogger imfErrorLogger){
        boolean result = true;
        for(TrackFileResourceType trackFileResource : virtualTrackResourceList){
            long compositionPlaylistResourceIntrinsicDuration = trackFileResource.getIntrinsicDuration().longValue();
            //WARNING : We might be losing some precision here since EntryPoint is an XML non-negative integer with no upper-bound
            long compositionPlaylistResourceEntryPoint = (trackFileResource.getEntryPoint() == null) ? 0L : trackFileResource.getEntryPoint().longValue();
            //Check to see if the Resource's source duration value is in the valid range as specified in st2067-3:2013 section 6.11.6
            if(trackFileResource.getSourceDuration() != null){
                if(trackFileResource.getSourceDuration().longValue() < 0
                        || trackFileResource.getSourceDuration().longValue() > (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)){
                    throw new IMFException(String.format("Invalid resource source duration value %d, should be in the range [0,%d]", trackFileResource.getSourceDuration().longValue(), (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)));
                }
            }
        }
        return result;
    }

    private static void validateCompositionPlaylistSchema(File xmlFile, IMFErrorLogger imfErrorLogger) throws IOException, URISyntaxException, SAXException {
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(xmlFile);
        validateCompositionPlaylistSchema(resourceByteRangeProvider, imfErrorLogger);
    }

    private static void validateCompositionPlaylistSchema(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException, URISyntaxException, SAXException {

        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);
            InputStream xmldig_core_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.xmldsig_core_schema_path);
            InputStream dcmlTypes_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.dcmlTypes_schema_path);
            InputStream imf_cpl_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.imf_cpl_schema_path);
            InputStream imf_core_constraints_is = CompositionPlaylist.class.getResourceAsStream(CompositionPlaylist.imf_core_constraints_schema_path);)
        {
            StreamSource inputSource = new StreamSource(inputStream);

            StreamSource[] streamSources = new StreamSource[4];
            streamSources[0] = new StreamSource(xmldig_core_is);
            streamSources[1] = new StreamSource(dcmlTypes_is);
            streamSources[2] = new StreamSource(imf_cpl_is);
            streamSources[3] = new StreamSource(imf_core_constraints_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
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
            validator.validate(inputSource);
        }
    }

    /**
     * This class is an immutable implementation of a rational number described as a ratio of two longs and used to hold
     * non-integer frame rate values
     */
    @Immutable
    public static final class EditRate
    {
        private final Long numerator;
        private final Long denominator;

        /**
         * Constructor for the rational frame rate number.
         * @param numbers the input list of numbers. The first number in the list is treated as the numerator and the second as
         *                the denominator. Construction succeeds only if the list has exactly two numbers
         */
        public EditRate(List<Long> numbers)
        {
            if (numbers.size() != 2)
            {
                throw new IMFException(String.format(
                        "Input list is expected to contain 2 numbers representing numerator and denominator respectively, found %d numbers in list %s",
                        numbers.size(), Arrays.toString(numbers.toArray())));
            }
            this.numerator = numbers.get(0);
            this.denominator = numbers.get(1);
        }

        /**
         * Getter for the frame rate numerator
         * @return a long value corresponding to the frame rate numerator
         */
        public Long getNumerator()
        {
            return this.numerator;
        }

        /**
         * Getter for the frame rate denominator
         * @return a long value corresponding to the frame rate denominator
         */
        public Long getDenominator()
        {
            return this.denominator;
        }

        /**
         * A method that returns a string representation of a CompositionPlaylist object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("=================== EditRate =====================\n");
            sb.append(String.format("numerator = %d, denominator = %d%n", this.numerator, this.denominator));
            return sb.toString();
        }

        /**
         * Overridden equals method.
         * @param object the EditRate to be compared with.
         * @return boolean false if the object is null or is not an instance of the EditRate class.
         */
        @Override
        public boolean equals(Object object){
            if(object == null
                    || !(object instanceof EditRate)){
                return false;
            }
            EditRate other = (EditRate) object;
            return ((this.getNumerator().equals(other.getNumerator())) && (this.getDenominator().equals(other.getDenominator())));
        }

        /**
         * A Java compliant implementation of the hashCode() method
         * @return integer containing the hash code corresponding to this object
         */
        @Override
        public int hashCode(){
            int hash = 1;
            hash = hash * 31 + this.numerator.hashCode(); /*Numerator can be used since it is non-null*/
            hash = hash * 31
                    + this.denominator.hashCode();/*Another field that is indicated to be non-null*/
            return hash;
        }
    }

    /**
     * This class enumerates various types of {@link org.smpte_ra.schemas.st2067_2_2013.SequenceType Sequence} that are valid in
     * CompositionPlaylist document that is compliant with st2067-2:2013. Such types are mostly defined in Section 6.3 of st2067-2:2013
     */
    public static enum SequenceTypeEnum
    {
        MarkerSequence("MarkerSequence"),
        MainImageSequence("MainImageSequence"),
        MainAudioSequence("MainAudioSequence"),
        SubtitlesSequence("SubtitlesSequence"),
        HearingImpairedCaptionsSequence("HearingImpairedCaptionsSequence"),
        VisuallyImpairedTextSequence("VisuallyImpairedTextSequence"),
        CommentarySequence("CommentarySequence"),
        KaraokeSequence("KaraokeSequence"),
        AncillaryDataSequence("AncillaryDataSequence"),
        Unknown("Unknown");

        private final String name;

        private SequenceTypeEnum(String name)
        {
            this.name = name;
        }

        private static SequenceTypeEnum getSequenceTypeEnum(String name)
        {
            switch (name)
            {
                case "MainImageSequence":
                    return MainImageSequence;
                case "MainAudioSequence":
                    return MainAudioSequence;
                case "SubtitlesSequence":
                    return SubtitlesSequence;
                case "HearingImpairedCaptionsSequence":
                    return HearingImpairedCaptionsSequence;
                case "VisuallyImpairedTextSequence":
                    return VisuallyImpairedTextSequence;
                case "CommentarySequence":
                    return CommentarySequence;
                case "KaraokeSequence":
                    return KaraokeSequence;
                case "AncillaryDataSequence":
                    return AncillaryDataSequence;
                default:
                    return Unknown;
            }
        }

    }

    /**
     * The class is an immutable implementation of the virtual track concept defined in Section 6.9.3 of st2067-3:2013. A
     * virtual track is characterized by its UUID and the type of sequence it holds
     */
    @Immutable
    public static final class VirtualTrack
    {
        private final UUID trackID;
        private final SequenceTypeEnum sequenceTypeEnum;
        private final List<TrackFileResourceType> resourceList;

        /**
         * Constructor for a VirtualTrack object
         * @param trackID the UUID associated with this VirtualTrack object
         * @param resourceList the list of resources associated with this VirtualTrack object
         * @param sequenceTypeEnum the type of the associated sequence
         */
        public VirtualTrack(UUID trackID, List<TrackFileResourceType> resourceList, SequenceTypeEnum sequenceTypeEnum)
        {
            this.trackID = trackID;
            this.sequenceTypeEnum = sequenceTypeEnum;
            this.resourceList = resourceList;
        }

        /**
         * Getter for the sequence type associated with this VirtualTrack object
         * @return the sequence type associated with this VirtualTrack object as an enum
         */
        public SequenceTypeEnum getSequenceTypeEnum()
        {
            return this.sequenceTypeEnum;
        }

        /**
         * Getter for the UUID associated with this VirtualTrack object
         * @return the UUID associated with the Virtual track
         */
        public UUID getTrackID(){
            return this.trackID;
        }

        /**
         * Getter for the list of resources associated with this VirtualTrack
         * @return the list of TrackFileResources associated with this VirtualTrack.
         */
        public List<TrackFileResourceType> getResourceList(){
            return Collections.unmodifiableList(this.resourceList);
        }

        /**
         * A method to determine the equivalence of any 2 virtual tracks.
         * @return boolean indicating if the 2 virtual tracks are equivalent or represent the same timeline
         */
        public boolean equivalent(VirtualTrack other){
            boolean result = true;
            List<TrackFileResourceType> otherResourceList = other.getResourceList();
            if(otherResourceList.size() != this.resourceList.size()){
                return false;
            }
            for(int i=0; i<this.getResourceList().size(); i++){
                TrackFileResourceType thisResource = this.resourceList.get(i);
                TrackFileResourceType otherResource = otherResourceList.get(i);

                /**
                 * Compare the following fields of the track file resources that have to be equal
                 * for the 2 resources to be considered equivalent/representing the same timeline.
                 */
                result &= thisResource.getTrackFileId().equals(otherResource.getTrackFileId());
                result &= thisResource.getEditRate().equals(otherResource.getEditRate());
                result &= thisResource.getEntryPoint().equals(otherResource.getEntryPoint());
                result &= thisResource.getIntrinsicDuration().equals(otherResource.getIntrinsicDuration());
                result &= thisResource.getRepeatCount().equals(otherResource.getRepeatCount());
                result &= thisResource.getSourceEncoding().equals(otherResource.getSourceEncoding());
            }
            return  result;
        }
    }

    public static void main(String args[]) throws Exception
    {
        File inputFile = new File(args[0]);

        CompositionPlaylist compositionPlaylist = new CompositionPlaylist(inputFile, new IMFErrorLoggerImpl());
        logger.warn(compositionPlaylist.toString());
    }

}
