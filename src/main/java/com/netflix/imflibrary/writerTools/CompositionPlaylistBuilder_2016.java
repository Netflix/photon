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
package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFAuthoringException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import com.sandflow.smpte.klv.Triplet;
import org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType;
import org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType;
import org.smpte_ra.schemas.st2067_2_2016.CompositionTimecodeType;
import org.smpte_ra.schemas.st2067_2_2016.ContentVersionType;
import org.smpte_ra.schemas.st2067_2_2016.SegmentType;
import org.smpte_ra.schemas.st2067_2_2016.SequenceType;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class that implements the logic to build a SMPTE st2067-2:2016 schema compliant CompositionPlaylist document.
 */
public class CompositionPlaylistBuilder_2016 {

    private final UUID uuid;
    private final XMLGregorianCalendar issueDate;
    private final org.smpte_ra.schemas.st2067_2_2016.UserTextType annotationText;
    private final org.smpte_ra.schemas.st2067_2_2016.UserTextType issuer;
    private final org.smpte_ra.schemas.st2067_2_2016.UserTextType creator;
    private final List<? extends Composition.VirtualTrack> virtualTracks;
    private final List<Long> compositionEditRate;
    private final Long totalRunningTime;
    private final Map<UUID, IMPBuilder.IMFTrackFileMetadata> trackFileHeaderPartitionMap;
    private final File workingDirectory;
    private final IMFErrorLogger imfErrorLogger;
    private final Map<Node, String> essenceDescriptorIDMap = new HashMap<>();
    private final List<org.smpte_ra.schemas.st2067_2_2016.SegmentType> segments = new ArrayList<>();
    private final List<List<org.smpte_ra.schemas.st2067_2_2016.SequenceType>> sequenceList = new ArrayList<>();

    public final static String defaultHashAlgorithm = "http://www.w3.org/2000/09/xmldsig#sha1";
    private final static String defaultContentKindScope = "http://www.smpte-ra.org/schemas/2067-3/XXXX#content-kind";


    /**
     * A constructor for CompositionPlaylistBuilder class to build a CompositionPlaylist document compliant with st2067-2:2013 schema
     * @param uuid identifying the CompositionPlaylist document
     * @param annotationText a free form human readable text
     * @param issuer a free form human readable text describing the issuer of the CompositionPlaylist document
     * @param creator a free form human readable text describing the tool used to create the CompositionPlaylist document
     * @param virtualTracks a list of VirtualTracks of the Composition
     * @param compositionEditRate the edit rate of the Composition
     * @param totalRunningTime a long value representing in seconds the total running time of this composition
     * @param trackFileHeaderPartitionMap a map of the IMFTrackFile's UUID to the EssenceHeaderPartition metadata
     * @param workingDirectory a folder location where the constructed CPL document can be written to
     */
    public CompositionPlaylistBuilder_2016(@Nonnull UUID uuid,
                                           @Nonnull org.smpte_ra.schemas.st2067_2_2016.UserTextType annotationText,
                                           @Nonnull org.smpte_ra.schemas.st2067_2_2016.UserTextType issuer,
                                           @Nonnull org.smpte_ra.schemas.st2067_2_2016.UserTextType creator,
                                           @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                           @Nonnull Composition.EditRate compositionEditRate,
                                           long totalRunningTime,
                                           @Nonnull Map<UUID, IMPBuilder.IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                           @Nonnull File workingDirectory){
        this.uuid = IMFUUIDGenerator.getInstance().generateUUID();
        this.annotationText = annotationText;
        this.issuer = issuer;
        this.creator = creator;
        this.issueDate = IMFUtils.createXMLGregorianCalendar();
        this.virtualTracks = Collections.unmodifiableList(virtualTracks);
        List<Long> editRate = new ArrayList<Long>() {{add(compositionEditRate.getNumerator());
                                                    add(compositionEditRate.getDenominator());}};
        this.compositionEditRate = Collections.unmodifiableList(editRate);
        this.totalRunningTime = totalRunningTime;
        this.trackFileHeaderPartitionMap = Collections.unmodifiableMap(trackFileHeaderPartitionMap);
        this.workingDirectory = workingDirectory;
        this.imfErrorLogger = new IMFErrorLoggerImpl();
    }

    /**
     * A method to build a CompositionPlaylist document conforming to the st2067-2/3:2016 schema
     * @return a list of errors resulting during the creation of the CPL document
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws ParserConfigurationException if a DocumentBuilder
     *   cannot be created which satisfies the configuration requested
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     */
    public List<ErrorLogger.ErrorObject> build() throws IOException, ParserConfigurationException, SAXException, JAXBException {
        org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType cplRoot = IMFCPLObjectFieldsFactory.constructCompositionPlaylistType_2016();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();

        cplRoot.setId(UUIDHelper.fromUUID(cplUUID));
        cplRoot.setAnnotation(this.annotationText);
        cplRoot.setIssueDate(IMFUtils.createXMLGregorianCalendar());
        cplRoot.setIssuer(this.issuer);
        cplRoot.setCreator(this.creator);
        cplRoot.setContentOriginator(null);
        cplRoot.setContentTitle(buildCPLUserTextType_2016("Not Included", "en"));
        cplRoot.setContentKind(null);
        org.smpte_ra.schemas.st2067_2_2016.ContentVersionType contentVersionType = buildContentVersionType(IMFUUIDGenerator.getInstance().getUrnUUID(), buildCPLUserTextType_2016("Photon CompositionPlaylistBuilder", "en"));
        List<org.smpte_ra.schemas.st2067_2_2016.ContentVersionType> contentVersionTypeList = new ArrayList<>();
        contentVersionTypeList.add(contentVersionType);
        cplRoot.setContentVersionList(buildContentVersionList(contentVersionTypeList));
        cplRoot.setLocaleList(null);
        cplRoot.setExtensionProperties(null);
        cplRoot.getEditRate().addAll(this.compositionEditRate);
        /*long compositionEditRate = (this.compositionEditRate.get(0)/ this.compositionEditRate.get(1));
        cplRoot.setCompositionTimecode(buildCompositionTimeCode(BigInteger.valueOf(compositionEditRate)));
        */
        cplRoot.setCompositionTimecode(null);
        cplRoot.setTotalRunningTime(LocalTime.MIN.plusSeconds(totalRunningTime).toString());

        /**
         * Process each VirtualTrack that is a part of this Composition
         */
        List<org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType> essenceDescriptorList = new ArrayList<>();
        List<CompositionPlaylistBuilder_2016.SequenceTypeTuple> sequenceTypeTuples = new ArrayList<>();
        for(Composition.VirtualTrack virtualTrack : virtualTracks) {
            /**
             * Build the EssenceDescriptorList
             */
            essenceDescriptorList.addAll(buildEDLForVirtualTrack(virtualTrack));
            /**
             * Build TrackResourceList
             */
            List<org.smpte_ra.schemas.st2067_2_2016.BaseResourceType> trackResourceList = buildTrackResourceList(virtualTrack);
            /**
             * Build the Sequence
             */
            UUID sequenceId = IMFUUIDGenerator.getInstance().generateUUID();
            UUID trackId = IMFUUIDGenerator.getInstance().generateUUID();
            SequenceTypeTuple sequenceTypeTuple = buildSequenceTypeTuple(sequenceId, trackId, buildResourceList(trackResourceList), virtualTrack.getSequenceTypeEnum());
            sequenceTypeTuples.add(sequenceTypeTuple);
        }
        org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.EssenceDescriptorList essenceDescriptorListType = buildEssenceDescriptorList(essenceDescriptorList);
        cplRoot.setEssenceDescriptorList(essenceDescriptorListType);
        UUID segmentId = IMFUUIDGenerator.getInstance().generateUUID();
        org.smpte_ra.schemas.st2067_2_2016.SegmentType segmentType = buildSegment(segmentId, buildCPLUserTextType_2016("Segment-1", "en"));
        populateSequenceListForSegment(sequenceTypeTuples, segmentType);
        cplRoot.setSegmentList(buildSegmentList(new ArrayList<SegmentType>(){{add(segmentType);}}));
        cplRoot.setSigner(null);
        cplRoot.setSignature(null);
        File outputFile = new File(this.workingDirectory + File.separator + "CPL-" + cplUUID.toString() + ".xml");
        serializeCPLToXML(cplRoot, outputFile);
        return imfErrorLogger.getErrors();
    }

    private List<EssenceDescriptorBaseType> buildEDLForVirtualTrack (Composition.VirtualTrack virtualTrack) throws IOException, ParserConfigurationException{

        Map<UUID, UUID> trackResourceSourceEncodingMap = new HashMap<>();//Map of TrackFileId -> SourceEncodingElement of each resource of this VirtualTrack
        for(IMFTrackFileResourceType trackResource : (List<IMFTrackFileResourceType>)virtualTrack.getResourceList()){
            UUID sourceEncoding = trackResourceSourceEncodingMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(trackResource.getTrackFileId()));
            if(sourceEncoding != null
                    && !sourceEncoding.equals(UUIDHelper.fromUUIDAsURNStringToUUID(trackResource.getSourceEncoding()))){
                throw new IMFAuthoringException(String.format("Error occurred while trying to build the EssenceDescriptorList for the CPL document. It seems like VirtualTrackId %s, has 2 resources with the same TrackFileId %s but different SourceEncodingElement %s and %s values. This is ambiguous and invalid.", virtualTrack.getTrackID(), trackResource.getTrackFileId(), trackResource.getSourceEncoding(), sourceEncoding));
            }
            else{
                trackResourceSourceEncodingMap.put(UUIDHelper.fromUUIDAsURNStringToUUID(trackResource.getTrackFileId()), UUIDHelper.fromUUIDAsURNStringToUUID(trackResource.getSourceEncoding()));
            }
        }
        List<org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType> essenceDescriptorList = new ArrayList<>();
        Set<UUID> trackResourceIds = IMFEssenceComponentVirtualTrack.class.cast(virtualTrack).getTrackResourceIds();
        /**
         * Create the RegXML representation of the EssenceDescriptor metadata for every Resource of every VirtualTrack
         * of the Composition
         */
        for(UUID uuid : trackResourceIds){
            IMPBuilder.IMFTrackFileMetadata imfTrackFileMetadata = this.trackFileHeaderPartitionMap.get(uuid);
            if(imfTrackFileMetadata == null){
                throw new IMFAuthoringException(String.format("TrackFileHeaderMetadata for Track Resource Id %s within VirtualTrack Id %s is absent", uuid.toString(), virtualTrack.getTrackID()));
            }
            ByteProvider byteProvider = new ByteArrayDataProvider(imfTrackFileMetadata.getHeaderPartition());
            ResourceByteRangeProvider resourceByteRangeProvider = new ByteArrayByteRangeProvider(imfTrackFileMetadata.getHeaderPartition());
            //Create the HeaderPartition
            HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, (long)imfTrackFileMetadata.getHeaderPartition().length, imfErrorLogger);

            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
            List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = headerPartition.getEssenceDescriptors();
            for(InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
                List<InterchangeObject.InterchangeObjectBO>subDescriptors = headerPartition.getSubDescriptors(essenceDescriptor);
                for(InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors){
                    if(subDescriptorBO != null) {
                        subDescriptorHeaders.add(subDescriptorBO.getHeader());
                    }
                }
                    /*Create a dom*/
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();

                RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(headerPartition.getPrimerPack().getHeader(), getByteProvider(resourceByteRangeProvider, headerPartition.getPrimerPack().getHeader()));

                DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(regXMLLibHelper, document, essenceDescriptorHeader, subDescriptorHeaders,resourceByteRangeProvider);
                Node node = documentFragment.getFirstChild();
                UUID essenceDescriptorId = trackResourceSourceEncodingMap.get(uuid);
                org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType essenceDescriptorBaseType = buildEssenceDescriptorBaseType(essenceDescriptorId, node);
                essenceDescriptorList.add(essenceDescriptorBaseType);
            }
        }
        return essenceDescriptorList;
    }

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(RegXMLLibHelper regXMLLibHelper,
                                                                    Document document,
                                                                    KLVPacket.Header essenceDescriptor,
                                                                    List<KLVPacket.Header>subDescriptors,
                                                                    ResourceByteRangeProvider resourceByteRangeProvider) throws MXFException, IOException {
        document.setXmlStandalone(true);

        Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, this.getByteProvider(resourceByteRangeProvider, essenceDescriptor));
        //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for(KLVPacket.Header subDescriptorHeader : subDescriptors){
            subDescriptorTriplets.add(regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, this.getByteProvider(resourceByteRangeProvider, subDescriptorHeader)));
        }
        return regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document);
    }

    private ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        return new ByteArrayDataProvider(bytes);
    }

    private List<org.smpte_ra.schemas.st2067_2_2016.BaseResourceType> buildTrackResourceList(Composition.VirtualTrack virtualTrack){
        List<IMFTrackFileResourceType> trackResources = (List<IMFTrackFileResourceType>)virtualTrack.getResourceList();
        List<org.smpte_ra.schemas.st2067_2_2016.BaseResourceType> trackResourceList = new ArrayList<>();
        for(IMFTrackFileResourceType trackResource : trackResources){
            trackResourceList.add(buildTrackFileResource(trackResource));
        }
        return Collections.unmodifiableList(trackResourceList);
    }

        private void serializeCPLToXML(org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType cplRoot, File outputFile) throws IOException, JAXBException, SAXException{

        int numErrors = imfErrorLogger.getNumberOfErrors();
        boolean formatted = true;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try(
                InputStream dsigSchemaAsAStream = contextClassLoader.getResourceAsStream("org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd");
                InputStream dcmlSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd");
                InputStream cplSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_3_2016/imf-cpl-20160411.xsd");
                InputStream coreConstraintsSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_2_2016/imf-core-constraints-20160411.xsd");
                OutputStream outputStream = new FileOutputStream(outputFile)
        )
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
            StreamSource[] schemaSources = new StreamSource[4];
            schemaSources[0] = new StreamSource(dsigSchemaAsAStream);
            schemaSources[1] = new StreamSource(dcmlSchemaAsAStream);
            schemaSources[2] = new StreamSource(cplSchemaAsAStream);
            schemaSources[3] = new StreamSource(coreConstraintsSchemaAsAStream);
            Schema schema = schemaFactory.newSchema(schemaSources);

            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st2067_2_2016");
            Marshaller marshaller = jaxbContext.createMarshaller();
            ValidationEventHandlerImpl validationEventHandler = new ValidationEventHandlerImpl(true);
            marshaller.setEventHandler(validationEventHandler);
            marshaller.setSchema(schema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

            /*marshaller.marshal(cplType, output);
            workaround for 'Error: unable to marshal type "CompositionPlaylistType" as an element because it is missing an @XmlRootElement annotation'
            as found at https://weblogs.java.net/blog/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always
             */
            marshaller.marshal(new JAXBElement<>(new QName("http://www.smpte-ra.org/schemas/2067-3/2016", "CompositionPlaylist"), CompositionPlaylistType.class, cplRoot), outputStream);


            if(this.imfErrorLogger.getNumberOfErrors() > numErrors){
                List<ErrorLogger.ErrorObject> fatalErrors = imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, this.imfErrorLogger.getNumberOfErrors());
                if(fatalErrors.size() > 0){
                    throw new IMFAuthoringException(String.format("Following FATAL errors were detected while building the PackingList document %s", fatalErrors.toString()));
                }
            }
        }
    }

    /**
     * A method to construct a UserTextType compliant with the 2016 schema for IMF CompositionPlaylist documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType conforming to the 2016 schema
     */
    public static org.smpte_ra.schemas.st2067_2_2016.UserTextType buildCPLUserTextType_2016(String value, String language){
        org.smpte_ra.schemas.st2067_2_2016.UserTextType userTextType = new org.smpte_ra.schemas.st2067_2_2016.UserTextType();
        userTextType.setValue(value);
        userTextType.setLanguage(language);
        return userTextType;
    }

    /**
     * A method to construct a ContentKindType object conforming to the 2016 schema
     * @param value the string correspding to the Content Kind
     * @param scope a string corresponding to the scope attribute of a Content Kind
     * @return a ContentKind object conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.ContentKindType buildContentKindType(@Nonnull String value, String scope)  {

        org.smpte_ra.schemas.st2067_2_2016.ContentKindType contentKindType = new org.smpte_ra.schemas.st2067_2_2016.ContentKindType();
        if(!scope.matches("^[a-zA-Z0-9._-]+") == true) {
            this.imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The ContentKind scope %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)", scope)));
            contentKindType.setScope(scope);
        }
        else{
            contentKindType.setScope(scope);
        }
        contentKindType.setValue(value);
        return contentKindType;
    }

    /**
     * A method to construct a ContentVersionType object conforming to the 2016 schema
     * @param id urn uuid corresponding to the content version type
     * @param value a UserTextType representing the value attribute of the ContentVersion
     * @return a content version object conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.ContentVersionType buildContentVersionType(String id, org.smpte_ra.schemas.st2067_2_2016.UserTextType value) {
        ContentVersionType contentVersionType = new ContentVersionType();
        contentVersionType.setId(id);
        contentVersionType.setLabelText(value);
        return contentVersionType;
    }

    /**
     * A method to construct a ContentVersionList object conforming to the 2016 schema
     * @param contentVersions a list of ContentVersion objects conforming to the 2016 schema
     * @return a content version list object conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.ContentVersionList buildContentVersionList(List<org.smpte_ra.schemas.st2067_2_2016.ContentVersionType> contentVersions){
        org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.ContentVersionList contentVersionList = new CompositionPlaylistType.ContentVersionList();
        contentVersionList.getContentVersion().addAll(contentVersions);
        return contentVersionList;
    }

    /**
     * A method to construct an EssenceDescriptorBaseType object conforming to the 2016 schema
     * @param id a UUID identifying the EssenceDescriptor. Note : This value should be the same as the SourceEncoding Element of
     *           the resource in a VirtualTrack of this composition whose EssenceDescriptor it represents in the EssenceDescriptorList. This cannot be enforced
     *           hence the responsibility is with the caller to ensure this else the generated CPL fail validation checks.
     * @param node a regxml representation of an EssenceDescriptor
     * @return a EssenceDescriptorBaseType object conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType buildEssenceDescriptorBaseType(UUID id, Node node){
        org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType essenceDescriptorBaseType = new org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType();
        essenceDescriptorBaseType.setId(UUIDHelper.fromUUID(id));
        this.essenceDescriptorIDMap.put(node, UUIDHelper.fromUUID(id));
        essenceDescriptorBaseType.getAny().add(node);
        return essenceDescriptorBaseType;
    }

    /**
     * A method to construct an EssenceDescriptorList conforming to the 2016 schema
     * @param essenceDescriptorBaseTypes a list of EssenceDescritorBaseType objects conforming to the 2016 schema
     * @return EssenceDescriptorList type object
     */
    public org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.EssenceDescriptorList buildEssenceDescriptorList(List<org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType> essenceDescriptorBaseTypes){
        org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.EssenceDescriptorList essenceDescriptorList = new org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.EssenceDescriptorList();
        essenceDescriptorList.getEssenceDescriptor().addAll(essenceDescriptorBaseTypes);
        return essenceDescriptorList;
    }

    /**
     * A method to construct a CompositionTimecodeType conforming to the 2016 schema
     * @param compositionEditRate the EditRate corresponding to the Composition's EditRate
     * @return a CompositionTimecodeType conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.CompositionTimecodeType buildCompositionTimeCode(BigInteger compositionEditRate){
        org.smpte_ra.schemas.st2067_2_2016.CompositionTimecodeType compositionTimecodeType = new CompositionTimecodeType();
        compositionTimecodeType.setTimecodeDropFrame(false);/*TimecodeDropFrame set to false by default*/
        compositionTimecodeType.setTimecodeRate(compositionEditRate);
        compositionTimecodeType.setTimecodeStartAddress(IMFUtils.generateTimecodeStartAddress());
        return compositionTimecodeType;
    }


    /**
     * A method to construct a LocaleType conforming to the 2016 schema
     * @param annotationText for the localeType
     * @param languages a list of string representing Language Tags as specified in RFC-5646
     * @param regions a list of strings representing regions
     * @param contentMaturityRatings a list of ContentMaturityRating objects conforming to the 2016 schema
     * @return a LocaleType object conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.LocaleType buildLocaleType(org.smpte_ra.schemas.st2067_2_2016.UserTextType annotationText,
                                                                         List<String> languages,
                                                                         List<String> regions,
                                                                         List<org.smpte_ra.schemas.st2067_2_2016.ContentMaturityRatingType> contentMaturityRatings){
        org.smpte_ra.schemas.st2067_2_2016.LocaleType localeType = new org.smpte_ra.schemas.st2067_2_2016.LocaleType();
        localeType.setAnnotation(annotationText);
        org.smpte_ra.schemas.st2067_2_2016.LocaleType.LanguageList languageList = new org.smpte_ra.schemas.st2067_2_2016.LocaleType.LanguageList();
        languageList.getLanguage().addAll(languages);
        localeType.setLanguageList(languageList);
        org.smpte_ra.schemas.st2067_2_2016.LocaleType.RegionList regionList = new org.smpte_ra.schemas.st2067_2_2016.LocaleType.RegionList();
        regionList.getRegion().addAll(regions);
        localeType.setRegionList(regionList);
        org.smpte_ra.schemas.st2067_2_2016.LocaleType.ContentMaturityRatingList contentMaturityRatingList = new org.smpte_ra.schemas.st2067_2_2016.LocaleType.ContentMaturityRatingList();
        contentMaturityRatingList.getContentMaturityRating().addAll(contentMaturityRatings);
        return localeType;
    }

    /**
     * A method to construct a ContentMaturityRatingType conforming to the 2016 schema
     * @param agency a string representing the agency that issued the rating for this Composition
     * @param rating a human-readable representation of the rating of this Composition
     * @param audience a human-readable representation of the intended target audience of this Composition
     * @return a ContentMaturityRating object conforming to the 2016 schema
     * @throws URISyntaxException any syntax errors with the agency attribute is exposed through a URISyntaxException
     */
    public org.smpte_ra.schemas.st2067_2_2016.ContentMaturityRatingType buildContentMaturityRatingType(String agency, String rating, org.smpte_ra.schemas.st2067_2_2016.ContentMaturityRatingType.Audience audience) throws URISyntaxException {
        org.smpte_ra.schemas.st2067_2_2016.ContentMaturityRatingType contentMaturityRatingType = new org.smpte_ra.schemas.st2067_2_2016.ContentMaturityRatingType();
        if(!agency.matches("^[a-zA-Z0-9._-]+") == true) {
            //this.imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The ContentKind scope %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)", id)));
            throw new URISyntaxException("Invalid URI", "The ContentMaturityRating agency %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)");
        }
        contentMaturityRatingType.setAgency(agency);
        contentMaturityRatingType.setRating(rating);
        contentMaturityRatingType.setAudience(audience);
        return contentMaturityRatingType;
    }

    /**
     * A method to construct a SegmentType conforming to the 2016 schema
     * @param id a uuid identifying the segment
     * @param annotationText a human readable annotation describing the Segment conforming to the 2016 schema
     * @return a SegmentType conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.SegmentType buildSegment(UUID id,
                                                                       org.smpte_ra.schemas.st2067_2_2016.UserTextType annotationText){
        org.smpte_ra.schemas.st2067_2_2016.SegmentType segment = new org.smpte_ra.schemas.st2067_2_2016.SegmentType();
        segment.setId(UUIDHelper.fromUUID(uuid));
        segment.setAnnotation(annotationText);
        org.smpte_ra.schemas.st2067_2_2016.SegmentType.SequenceList sequenceList = new org.smpte_ra.schemas.st2067_2_2016.SegmentType.SequenceList();
        segment.setSequenceList(sequenceList);
        this.segments.add(segment);
        return segment;
    }

    /**
     * A method to construct a SegmentList conforming to the 2016 schema
     * @param segments a list of Segments conforming to the 2016 schema
     * @return a SegmentList conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.SegmentList buildSegmentList(List<org.smpte_ra.schemas.st2067_2_2016.SegmentType> segments){
        org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.SegmentList segmentList = new org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType.SegmentList();
        segmentList.getSegment().addAll(segments);
        return segmentList;
    }

    /**
     * A method to construct a SequenceTypeTuple object that maintains a reference to the SequenceType object conforming to the 2016 schema
     * and the type of the sequence
     * @param id a uuid identifying the sequence
     * @param trackId a uuid identifying the virtual track to which this SequenceBelongs. Note : This Id should remain constant across Segments for
     *                a Sequence that belongs to a VirtualTrack, please see the definition of a TrackId in st2067-3:2016
     * @param resourceList a list of resources corresponding to this Sequence
     * @param sequenceType an enumeration identifying the contents of this Sequence (Currently we only support serializing
     *                     MainImageSequence and MainAudioSequence to a CPL)
     * @return a SequenceTypeTuple that maintains a reference to a Sequence and its type
     */
    public SequenceTypeTuple buildSequenceTypeTuple(UUID id,
                                                    UUID trackId,
                                                    org.smpte_ra.schemas.st2067_2_2016.SequenceType.ResourceList resourceList,
                                                    Composition.SequenceTypeEnum sequenceType){
        org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence = new org.smpte_ra.schemas.st2067_2_2016.SequenceType();
        sequence.setId(UUIDHelper.fromUUID(id));
        sequence.setTrackId(UUIDHelper.fromUUID(trackId));
        sequence.setResourceList(resourceList);
        return new SequenceTypeTuple(sequence, sequenceType);
    }

    /**
     * A method to construct a ResourceList for a Sequence conforming to the 2016 schema
     * @param trackResourceList a list of BaseResourceTypes
     * @return a resource list conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.SequenceType.ResourceList buildResourceList(List<org.smpte_ra.schemas.st2067_2_2016.BaseResourceType> trackResourceList){
        org.smpte_ra.schemas.st2067_2_2016.SequenceType.ResourceList resourceList = new org.smpte_ra.schemas.st2067_2_2016.SequenceType.ResourceList();
        resourceList.getResource().addAll(trackResourceList);
        return resourceList;
    }

    /**
     * A method to populate the SequenceList of a VirtualTrack segment
     * @param sequenceTypeTuples a SequenceTypeTuple that maintains a reference to a Sequence and its type. This type is
     *                           deliberately an opaque object whose definition is not known outside this builder. This
     *                           is done in order to allow robust construction of a SequenceList serially.
     * @param segment a VirtualTrack Segment conforming to the 2016 schema
     */
    public void populateSequenceListForSegment(List<SequenceTypeTuple> sequenceTypeTuples,
                                               org.smpte_ra.schemas.st2067_2_2016.SegmentType segment) {

        org.smpte_ra.schemas.st2067_2_2016.ObjectFactory objectFactory = new org.smpte_ra.schemas.st2067_2_2016.ObjectFactory();
        JAXBElement<SequenceType> element = null;
        List<Object> any = segment.getSequenceList().getAny();

        for(SequenceTypeTuple sequenceTypeTuple : sequenceTypeTuples){
            switch(sequenceTypeTuple.getSequenceType()){
                case MainImageSequence:
                    element = objectFactory.createMainImageSequence(sequenceTypeTuple.getSequence());
                    break;
                case MainAudioSequence:
                    element = objectFactory.createMainAudioSequence(sequenceTypeTuple.getSequence());
                    break;
                default:
                    throw new IMFAuthoringException(String.format("Currently we only support %s and %s sequence types in building a Composition Playlist document, the type of sequence being requested is %s", Composition.SequenceTypeEnum.MainAudioSequence.toString(), Composition.SequenceTypeEnum.MainImageSequence, sequenceTypeTuple.getSequenceType().toString()));
            }
            any.add(element);
        }
    }

    /**
     * A method to construct a TrackFileResourceType conforming to the 2016 schema
     * @param trackResource an object that roughly models a TrackFileResourceType
     * @return a BaseResourceType conforming to the 2016 schema
     */
    public org.smpte_ra.schemas.st2067_2_2016.BaseResourceType buildTrackFileResource(IMFTrackFileResourceType trackResource){
        org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType trackFileResource = new org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType();
        trackFileResource.setId(trackResource.getId());
        trackFileResource.setAnnotation(null);
        trackFileResource.setTrackFileId(trackResource.getTrackFileId());
        trackFileResource.getEditRate().add(trackResource.getEditRate().getNumerator());
        trackFileResource.getEditRate().add(trackResource.getEditRate().getDenominator());
        trackFileResource.setIntrinsicDuration(trackResource.getIntrinsicDuration());
        trackFileResource.setEntryPoint(trackResource.getEntryPoint());
        trackFileResource.setSourceDuration(trackResource.getSourceDuration());
        trackFileResource.setRepeatCount(trackResource.getRepeatCount());
        trackFileResource.setSourceEncoding(trackResource.getSourceEncoding());
        trackFileResource.setHash(trackResource.getHash());
        trackFileResource.setHashAlgorithm(buildDefaultDigestMethodType());

        return trackFileResource;
    }

    /**
     * A method to construct a Default Digest Method Type with a default HashAlgorithm
     * @return a DigestMethodType object conforming to the 2016 schema with the default HashAlgorithm
     */
    public org.smpte_ra.schemas.st2067_2_2016.DigestMethodType buildDefaultDigestMethodType(){
        org.smpte_ra.schemas.st2067_2_2016.DigestMethodType digestMethodType = new org.smpte_ra.schemas.st2067_2_2016.DigestMethodType();
        digestMethodType.setAlgorithm(CompositionPlaylistBuilder_2016.defaultHashAlgorithm);
        return digestMethodType;
    }

    /**
     * A method to construct a Digest Method Type with the HashAlgorithm string that was passed in
     * @param algorithm a String representing the alogrithm used for generating the Hash
     * @return a DigestMethodType object conforming to the 2016 schema with the default HashAlgorithm
     */
    public org.smpte_ra.schemas.st2067_2_2016.DigestMethodType buildDigestMethodType(String algorithm){
        org.smpte_ra.schemas.st2067_2_2016.DigestMethodType digestMethodType = new org.smpte_ra.schemas.st2067_2_2016.DigestMethodType();
        digestMethodType.setAlgorithm(algorithm);
        return digestMethodType;
    }

    /**
     * Getter for the errors in CompositionPlaylistBuilder_2016
     *
     * @return List of errors in CompositionPlaylistBuilder_2016.
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }

    /**
     * A thin class that maintains a reference to a VirtualTrack Sequence object and the type of the Sequence.
     * Its state is opaque to classes outside this builder
     */
    public static class SequenceTypeTuple{
        private final org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;
        private final Composition.SequenceTypeEnum sequenceType;

        private SequenceTypeTuple(org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence, Composition.SequenceTypeEnum sequenceType){
            this.sequence = sequence;
            this.sequenceType = sequenceType;
        }

        private org.smpte_ra.schemas.st2067_2_2016.SequenceType getSequence(){
            return this.sequence;
        }

        private Composition.SequenceTypeEnum getSequenceType(){
            return this.sequenceType;
        }
    }

}
