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
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st2067_2.CompositionModels.*;
import com.netflix.imflibrary.st2067_2.CompositionModels.st2067_2_2013.CompositionModel_st2067_2_2013;
import com.netflix.imflibrary.st2067_2.CompositionModels.st2067_2_2016.CompositionModel_st2067_2_2016;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.RegXMLLibHelper;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import com.sandflow.smpte.klv.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
@Immutable
public final class Composition {
    private static final Logger logger = LoggerFactory.getLogger(Composition.class);

    private static final String dcmlTypes_schema_path = "org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd";
    private static final String xmldsig_core_schema_path = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    private static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-3/2013");
        add("http://www.smpte-ra.org/schemas/2067-3/2016");
    }});

    private static class CoreConstraintsSchemas {
        private final String coreConstraintsSchemaPath;
        private final String coreConstraintsContext;

        private CoreConstraintsSchemas(String coreConstraintsSchemaPath, String coreConstraintsContext) {
            this.coreConstraintsSchemaPath = coreConstraintsSchemaPath;
            this.coreConstraintsContext = coreConstraintsContext;
        }

        private String getCoreConstraintsSchemaPath() {
            return this.coreConstraintsSchemaPath;
        }

        private String getCoreConstraintsContext() {
            return this.coreConstraintsContext;
        }
    }

    private static final List<CoreConstraintsSchemas> supportedIMFCoreConstraintsSchemas = Collections.unmodifiableList
            (new ArrayList<CoreConstraintsSchemas>() {{
                add(new CoreConstraintsSchemas("org/smpte_ra/schemas/st2067_2_2013/imf-core-constraints-20130620-pal.xsd", "org.smpte_ra.schemas.st2067_2_2013"));
                add(new CoreConstraintsSchemas("org/smpte_ra/schemas/st2067_2_2016/imf-core-constraints-20160411.xsd", "org.smpte_ra.schemas.st2067_2_2016"));
            }});

    private final String coreConstraintsVersion;
    private final Map<UUID, ? extends VirtualTrack> virtualTrackMap;
    private final IMFCompositionPlaylistType compositionPlaylistType;
    private final IMFErrorLogger imfErrorLogger;

    /**
     * Constructor for a {@link Composition Composition} object from a XML file
     *
     * @param compositionPlaylistXMLFile the input XML file that is conformed to schema and constraints specified in st2067-3:2013 and st2067-2:2013
     * @param imfErrorLogger             A non-nullable error logger for recording all errors. Construction is failed by throwing a {@link RuntimeException RuntimeException}
     *                                   if any {@link com.netflix.imflibrary.IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     * @throws IOException        any I/O related error is exposed through an IOException
     */
    public Composition(File compositionPlaylistXMLFile, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        this(new FileByteRangeProvider(compositionPlaylistXMLFile));
    }

    /**
     * Constructor for a {@link Composition Composition} object from a XML file
     *
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     *                                  if any {@link com.netflix.imflibrary.IMFErrorLogger.IMFErrors.ErrorLevels#FATAL fatal} errors are encountered
     * @throws IOException        any I/O related error is exposed through an IOException
     */
    public Composition(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        imfErrorLogger = new IMFErrorLoggerImpl();
        String imf_cpl_schema_path = "";
        try {
            String cplNameSpaceURI = getCompositionNamespaceURI(resourceByteRangeProvider, imfErrorLogger);

            String namespaceVersion = getCPLNamespaceVersion(cplNameSpaceURI);
            imf_cpl_schema_path = getIMFCPLSchemaPath(namespaceVersion, imfErrorLogger);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
            throw new IMFException("Composition creation failed", imfErrorLogger);
        }

        CoreConstraintsSchemas coreConstraintsSchema = this.supportedIMFCoreConstraintsSchemas.get(0);
        JAXBElement jaxbElement = null;

        for (int i = 0; i < supportedIMFCoreConstraintsSchemas.size(); i++) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(Composition.xmldsig_core_schema_path);
                 InputStream dcmlTypes_is = contextClassLoader.getResourceAsStream(Composition.dcmlTypes_schema_path);
                 InputStream imf_cpl_is = contextClassLoader.getResourceAsStream(imf_cpl_schema_path);
                 InputStream imf_core_constraints_is = contextClassLoader.getResourceAsStream(supportedIMFCoreConstraintsSchemas.get(i).coreConstraintsSchemaPath);) {
                StreamSource[] streamSources = new StreamSource[4];
                streamSources[0] = new StreamSource(xmldsig_core_is);
                streamSources[1] = new StreamSource(dcmlTypes_is);
                streamSources[2] = new StreamSource(imf_cpl_is);
                streamSources[3] = new StreamSource(imf_core_constraints_is);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(streamSources);

                ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
                JAXBContext jaxbContext = JAXBContext.newInstance(supportedIMFCoreConstraintsSchemas.get(i).coreConstraintsContext);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(validationEventHandlerImpl);
                unmarshaller.setSchema(schema);

                jaxbElement = (JAXBElement) unmarshaller.unmarshal(inputStream);
                coreConstraintsSchema = supportedIMFCoreConstraintsSchemas.get(i);

                if (validationEventHandlerImpl.hasErrors()) {
                    validationEventHandlerImpl.getErrors().stream()
                            .map(e -> new ErrorLogger.ErrorObject(
                                    IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                    e.getValidationEventSeverity(),
                                    e.getLineNumber().toString() + " - " + e.getErrorMessage())
                            )
                            .forEach(imfErrorLogger::addError);

                      throw new IMFException(validationEventHandlerImpl.toString(), imfErrorLogger);
                }
                break; //No errors so we can break out without trying other Core constraints schema namespaces.
            } catch (SAXException | JAXBException e) {
                if (i == supportedIMFCoreConstraintsSchemas.size() - 1) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                            .IMFErrors.ErrorLevels.FATAL,
                            e.getMessage());
                    throw new IMFException(e.getMessage(), imfErrorLogger);
                }
            }
        }

        this.coreConstraintsVersion = coreConstraintsSchema.getCoreConstraintsContext();

        switch (coreConstraintsVersion) {
            case "org.smpte_ra.schemas.st2067_2_2013": {
                org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistTypeJaxb =
                        (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType) jaxbElement.getValue();

                this.compositionPlaylistType = CompositionModel_st2067_2_2013.getCompositionPlaylist(compositionPlaylistTypeJaxb,
                        imfErrorLogger);
            }
            break;
            case "org.smpte_ra.schemas.st2067_2_2016": {
                org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistTypeJaxb = (org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType) jaxbElement.getValue();

                this.compositionPlaylistType = CompositionModel_st2067_2_2016.getCompositionPlaylist(compositionPlaylistTypeJaxb, imfErrorLogger);
            }
            break;
            default:
                String message = String.format("Please check the CPL document, currently we only support the " +
                        "following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString
                        (supportedIMFCoreConstraintsSchemas));
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                                .IMFErrors.ErrorLevels.FATAL, message);
                throw new IMFException(message, imfErrorLogger);

        }

        this.virtualTrackMap = getVirtualTracksMap(compositionPlaylistType, imfErrorLogger);


        imfErrorLogger.addAllErrors(IMFCoreConstraintsChecker.checkVirtualTracks(compositionPlaylistType, this
                .virtualTrackMap));

        if ((compositionPlaylistType.getEssenceDescriptorList() == null) ||
                (compositionPlaylistType.getEssenceDescriptorList().size() < 1)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ESSENCE_DESCRIPTOR_LIST_MISSING,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, "EssenceDescriptorList is either absent or empty");
        }

        if (imfErrorLogger.hasFatal()) {
            throw new IMFException(String.format("Found fatal errors in CompositionPlaylist XML file"), imfErrorLogger);
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
                                (List<IMFTrackFileResourceType>) virtualTrackResourceList);
                    } else if (virtualTrackResourceList.get(0) instanceof IMFMarkerResourceType) {
                        virtualTrack = new IMFMarkerVirtualTrack(uuid,
                                sequence.getType(),
                                (List<IMFMarkerResourceType>) virtualTrackResourceList);
                    }
                }
                virtualTrackMap.put(uuid, virtualTrack);
            } else {
                String message = String.format(
                        "First segment in Composition XML file has multiple occurrences of virtual track UUID %s", uuid);
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
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

    private static final String getIMFCPLSchemaPath(String namespaceVersion, @Nonnull IMFErrorLogger imfErrorLogger) {
        String imf_cpl_schema_path;
        switch (namespaceVersion) {
            case "2013":
                imf_cpl_schema_path = "org/smpte_ra/schemas/st2067_3_2013/imf-cpl.xsd";
                break;
            case "2016":
                imf_cpl_schema_path = "org/smpte_ra/schemas/st2067_3_2016/imf-cpl-20160411.xsd";
                break;
            default:
                String message = String.format("Please check the CPL document and namespace URI, currently we " +
                        "only support the following schema URIs %s", Utilities.serializeObjectCollectionToString
                        (supportedCPLSchemaURIs));
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL,
                        message);
                throw new IMFException(message, imfErrorLogger);
        }
        return imf_cpl_schema_path;
    }

    @Nullable
    private static final String getCompositionNamespaceURI(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {

        String result = "";

        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);) {
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
            for (String cplNamespaceURI : Composition.supportedCPLSchemaURIs) {
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

    private static final String getCPLNamespaceVersion(String namespaceURI) {
        String[] uriComponents = namespaceURI.split("/");
        String namespaceVersion = uriComponents[uriComponents.length - 1];
        return namespaceVersion;
    }

    private final String serializeIMFCoreConstaintsSchemasToString(List<CoreConstraintsSchemas> coreConstraintsSchemas) {
        StringBuilder stringBuilder = new StringBuilder();
        for (CoreConstraintsSchemas coreConstraintsSchema : coreConstraintsSchemas) {
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(coreConstraintsSchema.getCoreConstraintsContext());
        }
        return stringBuilder.toString();
    }

    /**
     * A stateless method that verifies if the raw data represented by the ResourceByteRangeProvider corresponds to a valid
     * IMF Composition Playlist document
     *
     * @param resourceByteRangeProvider - a byte range provider for the document that needs to be verified
     * @return - a boolean indicating if the document represented is an IMF Composition or not
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isFileOfSupportedSchema(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {

        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            NodeList nodeList = null;
            for (String supportedSchemaURI : supportedCPLSchemaURIs) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, "CompositionPlaylist");
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
        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for (String cplNamespaceURI : Composition.supportedCPLSchemaURIs) {
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
     * Getter for the composition edit rate as specified in the Composition XML file
     *
     * @return the edit rate associated with the Composition
     */
    public EditRate getEditRate() {
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
     * @return {@link java.util.Map Map}&lt;{@link java.util.UUID UUID},{@link Composition.VirtualTrack VirtualTrack}&gt;. The UUID key corresponds to VirtualTrackID
     */
    public Map<UUID, ? extends VirtualTrack> getVirtualTrackMap() {
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
            if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MainImageSequence)) {
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
            if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MainAudioSequence)) {
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
            if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MarkerSequence)) {
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
        String imf_cpl_schema_path = "";
        try {
            String cplNameSpaceURI = getCompositionNamespaceURI(resourceByteRangeProvider, imfErrorLogger);
            String namespaceVersion = getCPLNamespaceVersion(cplNameSpaceURI);
            imf_cpl_schema_path = getIMFCPLSchemaPath(namespaceVersion, imfErrorLogger);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
            return imfErrorLogger.getErrors();
        }

        for (int i = 0; i < supportedIMFCoreConstraintsSchemas.size(); i++) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(Composition.xmldsig_core_schema_path);
                 InputStream dcmlTypes_is = contextClassLoader.getResourceAsStream(Composition.dcmlTypes_schema_path);
                 InputStream imf_cpl_is = contextClassLoader.getResourceAsStream(imf_cpl_schema_path);
                 InputStream imf_core_constraints_is = contextClassLoader.getResourceAsStream(supportedIMFCoreConstraintsSchemas.get(i).coreConstraintsSchemaPath);) {

                StreamSource inputSource = new StreamSource(inputStream);

                StreamSource[] streamSources = new StreamSource[4];
                streamSources[0] = new StreamSource(xmldsig_core_is);
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
                break;//No errors so we can break out without trying other Core constraints schema namespaces.
            } catch (SAXException e) {
                if (i == supportedIMFCoreConstraintsSchemas.size() - 1) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                            .IMFErrors.ErrorLevels.FATAL, e.getMessage());
                    return imfErrorLogger.getErrors();
                }
            }
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * This class is an immutable implementation of a rational number described as a ratio of two longs and used to hold
     * non-integer frame rate values
     */
    @Immutable
    public static final class EditRate {
        private final Long numerator;
        private final Long denominator;
        private final IMFErrorLogger imfErrorLogger;

        /**
         * Constructor for the rational frame rate number.
         *
         * @param numbers the input list of numbers. The first number in the list is treated as the numerator and the
         *                second as
         *                the denominator. Construction succeeds only if the list has exactly two numbers
         */
        public EditRate(List<Long> numbers) {
            Long denominator = 1L;
            Long numerator = 1L;
            imfErrorLogger = new IMFErrorLoggerImpl();
            if (numbers.size() != 2) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL, String.format(
                        "Input list is expected to contain 2 numbers representing numerator and denominator " +
                                "respectively, found %d numbers in list %s",
                        numbers.size(), Arrays.toString(numbers.toArray())));

            } else if (numbers.get(0) == 0
                    || numbers.get(1) == 0) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL, String.format(
                        "Input list is expected to contain 2 non-zero numbers representing numerator and denominator " +
                                "of the EditRate respectively, found Numerator %d, Denominator %d",
                        numbers.get(0), numbers.get(1)));
            }
            else {
                numerator = numbers.get(0);
                denominator = numbers.get(1);
            }

            if(imfErrorLogger.hasFatal())
            {
                throw new IMFException("Failed to create IMFBaseResourceType", imfErrorLogger);
            }

            this.numerator = numerator;
            this.denominator = denominator;
        }

        /**
         * Getter for the frame rate numerator
         *
         * @return a long value corresponding to the frame rate numerator
         */
        public Long getNumerator() {
            return this.numerator;
        }

        /**
         * Getter for the frame rate denominator
         *
         * @return a long value corresponding to the frame rate denominator
         */
        public Long getDenominator() {
            return this.denominator;
        }

        /**
         * A method that returns a string representation of a Composition object
         *
         * @return string representing the object
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=================== EditRate =====================\n");
            sb.append(String.format("numerator = %d, denominator = %d%n", this.numerator, this.denominator));
            return sb.toString();
        }

        /**
         * Overridden equals method.
         *
         * @param object the EditRate to be compared with.
         * @return boolean false if the object is null or is not an instance of the EditRate class.
         */
        @Override
        public boolean equals(Object object) {
            if (object == null
                    || !(object instanceof EditRate)) {
                return false;
            }
            EditRate other = (EditRate) object;
            return ((this.getNumerator().equals(other.getNumerator())) && (this.getDenominator().equals(other.getDenominator())));
        }

        /**
         * A Java compliant implementation of the hashCode() method
         *
         * @return integer containing the hash code corresponding to this object
         */
        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + this.numerator.hashCode(); /*Numerator can be used since it is non-null*/
            hash = hash * 31
                    + this.denominator.hashCode();/*Another field that is indicated to be non-null*/
            return hash;
        }
    }

    /**
     * This class enumerates various types of {@link org.smpte_ra.schemas.st2067_2_2013.SequenceType Sequence} that are valid in
     * Composition document that is compliant with st2067-2:2013. Such types are mostly defined in Section 6.3 of st2067-2:2013
     */
    public static enum SequenceTypeEnum {
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

        private SequenceTypeEnum(String name) {
            this.name = name;
        }

        /**
         * A getter for the SequenceTypeEnum given a string that represents the name of a SequenceTypeEnum
         *
         * @param name the string that should represent the SequenceTypeEnum
         * @return the SequenceTypeEnum value corresponding to the name that was passed
         */
        public static SequenceTypeEnum getSequenceTypeEnum(String name) {
            switch (name) {
                case "MarkerSequence":
                    return MarkerSequence;
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

        /**
         * An override of the toString() method
         *
         * @return a string representing the SequenceTypeEnum
         */
        @Override
        public String toString() {
            return this.name;
        }

    }

    /**
     * The class is an immutable implementation of the virtual track concept defined in Section 6.9.3 of st2067-3:2013. A
     * virtual track is characterized by its UUID, the type of sequence and a list of UUIDs of the
     * IMF track files that comprise it.
     */
    @Immutable
    public abstract static class VirtualTrack {
        protected final UUID trackID;
        protected final SequenceTypeEnum sequenceTypeEnum;
        protected final List<? extends IMFBaseResourceType> resources;

        /**
         * Constructor for a VirtualTrack object
         *
         * @param trackID          the UUID associated with this VirtualTrack object
         * @param sequenceTypeEnum the type of the associated sequence
         * @param resources        the resource list of the Virtual Track
         */
        public VirtualTrack(UUID trackID, SequenceTypeEnum sequenceTypeEnum, List<? extends IMFBaseResourceType> resources) {
            this.trackID = trackID;
            this.sequenceTypeEnum = sequenceTypeEnum;
            this.resources = resources;
        }

        /**
         * Getter for the sequence type associated with this VirtualTrack object
         *
         * @return the sequence type associated with this VirtualTrack object as an enum
         */
        public SequenceTypeEnum getSequenceTypeEnum() {
            return this.sequenceTypeEnum;
        }

        /**
         * Getter for the UUID associated with this VirtualTrack object
         *
         * @return the UUID associated with the Virtual track
         */
        public UUID getTrackID() {
            return this.trackID;
        }

        /**
         * Getter for the Resources of the Virtual Track
         *
         * @return an unmodifiable list of resources of the Virtual Track
         */
        public List<? extends IMFBaseResourceType> getResourceList() {
            return Collections.unmodifiableList(this.resources);
        }

        /**
         * A method to determine the equivalence of any 2 virtual tracks.
         *
         * @param other - the object to compare against
         * @return boolean indicating if the 2 virtual tracks are equivalent or represent the same timeline
         */
        public boolean equivalent(Composition.VirtualTrack other) {
            if (other == null) {
                return false;
            }
            boolean result = true;
            List<? extends IMFBaseResourceType> otherResourceList = other.resources;
            if (otherResourceList.size() != resources.size()) {
                return false;
            }
            for (int i = 0; i < resources.size(); i++) {
                IMFBaseResourceType thisResource = this.resources.get(i);
                IMFBaseResourceType otherResource = otherResourceList.get(i);

                result &= thisResource.equivalent(otherResource);
            }
            return result;
        }
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
     * A utility method to retrieve the UUIDs of the Track files referenced by a Virtual track within a Composition.
     *
     * @param virtualTrack - object model of an IMF virtual track {@link Composition.VirtualTrack}
     * @return A list of TrackFileResourceType objects corresponding to the virtual track in the Composition.
     */
    @Nonnull
    public List<ResourceIdTuple> getVirtualTrackResourceIDs(@Nonnull Composition.VirtualTrack virtualTrack) {

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
     * A utility method that will analyze the EssenceDescriptorList in a Composition and construct a HashMap mapping
     * a UUID to a EssenceDescriptor.
     *
     * @return a HashMap mapping the UUID to its corresponding EssenceDescriptor in the Composition
     */
    public Map<UUID, DOMNodeObjectModel> getEssenceDescriptorListMap() {
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
        if(imfErrorLogger.hasFatal())
        {
            throw new IMFException("Creating essenceDescriptorMap failed", imfErrorLogger);
        }
        return Collections.unmodifiableMap(essenceDescriptorMap);
    }

    public Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> getAudioVirtualTracksMap() {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<? extends Composition.VirtualTrack> audioVirtualTracks = this.getAudioVirtualTracks();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = this.getEssenceDescriptorListMap();
        Map<Set<DOMNodeObjectModel>, VirtualTrack> audioVirtualTrackMap = new HashMap<>();
        for (VirtualTrack audioVirtualTrack : audioVirtualTracks) {
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
        if(imfErrorLogger.hasFatal())
        {
            throw new IMFException("Creating Audio Virtual track map failed", imfErrorLogger);
        }
        return Collections.unmodifiableMap(audioVirtualTrackMap);
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
     * This method can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to the
     * resources referenced by the Composition.
     *
     * @param headerPartitionTuples        list of HeaderPartitionTuples corresponding to the IMF essences referenced in the Composition
     * @param conformAllVirtualTracksInCpl a boolean that turns on/off conforming all the VirtualTracks in the Composition
     * @return boolean to indicate of the Composition is conformant or not
     * @throws IOException        - any I/O related error is exposed through an IOException.
     */
    public List<ErrorLogger.ErrorObject> conformVirtualTracksInComposition(List<IMPValidator.HeaderPartitionTuple>
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
        Iterator resourceEssenceDescriptorIDs = resourceEssenceDescriptorIDsSet.iterator();
        Set<UUID> cplEssenceDescriptorIDsSet = getEssenceDescriptorIdsSet();
        Iterator cplEssenceDescriptorIDs = cplEssenceDescriptorIDsSet.iterator();
        while (resourceEssenceDescriptorIDs.hasNext()) {
            UUID resourceEssenceDescriptorUUID = (UUID) resourceEssenceDescriptorIDs.next();
            if (!cplEssenceDescriptorIDsSet.contains(resourceEssenceDescriptorUUID)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor ID %s referenced by a VirtualTrack Resource does not have a corresponding EssenceDescriptor in the EssenceDescriptorList in the CPL", resourceEssenceDescriptorUUID.toString()));
            }
        }

        /**
         * The following checks that at least one of the Virtual Tracks references an EssenceDescriptor in the EDL. This
         * check should be performed only when we need to conform all the Virtual Tracks in the CPL.
         */
        if (conformAllVirtualTracksInCpl) {
            while (cplEssenceDescriptorIDs.hasNext()) {
                UUID cplEssenceDescriptorUUID = (UUID) cplEssenceDescriptorIDs.next();
                if (!resourceEssenceDescriptorIDsSet.contains(cplEssenceDescriptorUUID)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptorID %s in the CPL EssenceDescriptorList is not referenced by any resource in any of the Virtual tracks in the CPL, this violates the constraint in st2067-3:2013 section 6.1.10.1", cplEssenceDescriptorUUID.toString()));
                }
            }
        }

        if (imfErrorLogger.hasFatal()) {
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

        if( essenceDescriptorMap == null || resourceEssenceDescriptorMap == null || imfErrorLogger.hasFatal())
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
            List<Composition.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);
            for (Composition.ResourceIdTuple resourceIdTuple : resourceIdTuples) {
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        return resourceSourceEncodingElementsSet;
    }

    private Map<UUID, List<DOMNodeObjectModel>> getResourcesEssenceDescriptorsMap(List<IMPValidator
            .HeaderPartitionTuple> headerPartitionTuples) throws IOException {
        int previousNumberOfErrors = imfErrorLogger.getErrors().size();
        Map<UUID, List<DOMNodeObjectModel>> resourcesEssenceDescriptorMap = new LinkedHashMap<>();

        /*Create a Map of FilePackage UUID which should be equal to the TrackFileId of the resource in the Composition if the asset is referenced and the HeaderPartitionTuple, Map<UUID, HeaderPartitionTuple>*/
        Map<UUID, IMPValidator.HeaderPartitionTuple> resourceUUIDHeaderPartitionMap = new HashMap<>();
        for (IMPValidator.HeaderPartitionTuple headerPartitionTuple : headerPartitionTuples) {
            //validate header partition
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartitionTuple.getHeaderPartition());
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
            Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage) genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
            resourceUUIDHeaderPartitionMap.put(packageUUID, headerPartitionTuple);
        }
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(this.getVirtualTrackMap().values());

        /*Go through all the Virtual Tracks in the Composition and construct a map of Resource Source Encoding Element and a list of DOM nodes representing every EssenceDescriptor in the HeaderPartition corresponding to that Resource*/
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            List<Composition.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);/*Retrieve a list of ResourceIDTuples corresponding to this virtual track*/
            for (Composition.ResourceIdTuple resourceIdTuple : resourceIdTuples)
            {
                try
                {
                    IMPValidator.HeaderPartitionTuple headerPartitionTuple = resourceUUIDHeaderPartitionMap.get(resourceIdTuple.getTrackFileId());
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

        if( imfErrorLogger.hasFatal(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors()))
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

    private List<Node> getEssenceDescriptorDOMNodes(IMPValidator.HeaderPartitionTuple headerPartitionTuple) throws IOException {
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
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_HEADER_PARTITION_ERROR,
                            IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL, e.getMessage());
                }
            }
            if(imfErrorLogger.hasFatal()) {
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

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, IMPValidator.HeaderPartitionTuple headerPartitionTuple, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header> subDescriptors) throws MXFException, IOException {
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
        return regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document);
    }

    private ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        return byteProvider;
    }

    private List<IMFErrorLogger.ErrorObject> conformEssenceDescriptors(Map<UUID, List<DOMNodeObjectModel>>
                                                                    essenceDescriptorsMap, Map<UUID,
            DOMNodeObjectModel> eDLMap) {
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
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Source Encoding Element %s in a track does not have a corresponding entry in the CPL's EDL", sourceEncodingElement.toString()));
            }
        }
        /**
         * The following check ensures that we have atleast one EssenceDescriptor in a TrackFile that equals the corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> iterator = essenceDescriptorsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DOMNodeObjectModel>> entry = (Map.Entry<UUID, List<DOMNodeObjectModel>>) iterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = entry.getValue();
            DOMNodeObjectModel referenceDOMNodeObjectModel = eDLMap.get(entry.getKey());
            if (referenceDOMNodeObjectModel == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Source Encoding Element %s in a track does not have a corresponding entry in the CPL's EDL", entry.getKey().toString()));
            }
            else {

                boolean intermediateResult = false;

                for (DOMNodeObjectModel domNodeObjectModel : domNodeObjectModels) {
                    intermediateResult |= referenceDOMNodeObjectModel.equals(domNodeObjectModel);
                }
                if (!intermediateResult) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Id %s in the CPL's EDL doesn't match any EssenceDescriptors within the IMFTrackFile that references it", entry.getKey().toString()));
                }
            }
        }

        return imfErrorLogger.getErrors();
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath>%n", Composition.class.getName()));
        return sb.toString();
    }

    public static void main(String args[]) throws IOException, SAXException, JAXBException
    {
        if (args.length != 1)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateCPL(payloadRecord);

        if(errors.size() > 0){
            for(ErrorLogger.ErrorObject errorObject : errors){
                if(errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                }
                else if(errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
        }
        else{
            logger.info("No errors were detected in the CompositionPlaylist Document");
        }
    }
}