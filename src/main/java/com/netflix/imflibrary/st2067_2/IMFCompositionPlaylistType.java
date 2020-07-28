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

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.w3c.dom.Document;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A class that models an IMF Composition Playlist structure.
 */
@Immutable
final class IMFCompositionPlaylistType {
    private final UUID id;
    private final Composition.EditRate editRate;
    private final String annotation;
    private final String issuer;
    private final String creator;
    private final String contentOriginator;
    private final String contentTitle;
    private final String contentKind;
    private final Composition.ContentVersionList contentVersionList;
    private final List<IMFSegmentType> segmentList;
    private final List<IMFEssenceDescriptorBaseType> essenceDescriptorList;
    private final IMFErrorLogger imfErrorLogger;
    private final String coreConstraintsVersion;
    private final Set<String> applicationIdSet;

    /**
     * @deprecated
     * This constructor is the legacy constructor, it uses a single String for the application id
     * but a CPL may declare that it conforms to multiple application ids.
     * The constructor using a Set should be preferred.
     */
    @Deprecated
    public IMFCompositionPlaylistType(String id,
                                      List<Long> editRate,
                                      String annotation,
                                      String issuer,
                                      String creator,
                                      String contentOriginator,
                                      String contentTitle,
                                      String contentKind,
                                      Map<String, String> contentVersionList,
                                      List<IMFSegmentType> segmentList,
                                      List<IMFEssenceDescriptorBaseType> essenceDescriptorList,
                                      String coreConstraintsVersion,
                                      String applicationId)
    {
        this(id, editRate, annotation, issuer, creator, contentOriginator, contentTitle, contentKind, contentVersionList, segmentList, essenceDescriptorList, coreConstraintsVersion, (applicationId == null ? new HashSet<>() : new HashSet<String>(Arrays.asList(applicationId))));
    }

    public IMFCompositionPlaylistType(String id,
                                   List<Long> editRate,
                                   String annotation,
                                   String issuer,
                                   String creator,
                                   String contentOriginator,
                                   String contentTitle,
                                   String contentKind,
                                   Map<String, String> contentVersionList,
                                   List<IMFSegmentType> segmentList,
                                   List<IMFEssenceDescriptorBaseType> essenceDescriptorList,
                                   String coreConstraintsVersion,
                                   @Nonnull Set<String> applicationIds)
    {
        this.id                = UUIDHelper.fromUUIDAsURNStringToUUID(id);
        Composition.EditRate rate = null;
        Composition.ContentVersionList versions = null;
        imfErrorLogger = new IMFErrorLoggerImpl();
        try
        {
            rate = new Composition.EditRate(editRate);
            versions = new Composition.ContentVersionList(contentVersionList);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        this.editRate          = rate;
        this.annotation        = annotation;
        this.issuer            = issuer;
        this.creator           = creator;
        this.contentOriginator = contentOriginator;
        this.contentTitle      = contentTitle;
        this.contentKind       = contentKind;
        this.contentVersionList= versions;
        this.segmentList       = segmentList;
        this.essenceDescriptorList  = essenceDescriptorList;
        this.coreConstraintsVersion = coreConstraintsVersion;
        this.applicationIdSet = applicationIds;

        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("Failed to create IMFBaseResourceType", imfErrorLogger);
        }
    }

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

    private static final String dcmlTypes_schema_path = "org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd";
    private static final String xmldsig_core_schema_path = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    private static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-3/2013");
        add("http://www.smpte-ra.org/schemas/2067-3/2016");
    }});

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

    private static final String getCPLNamespaceVersion(String namespaceURI) {
        String[] uriComponents = namespaceURI.split("/");
        String namespaceVersion = uriComponents[uriComponents.length - 1];
        return namespaceVersion;
    }

    private static final String serializeIMFCoreConstaintsSchemasToString(List<CoreConstraintsSchemas> coreConstraintsSchemas) {
        StringBuilder stringBuilder = new StringBuilder();
        for (CoreConstraintsSchemas coreConstraintsSchema : coreConstraintsSchemas) {
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(coreConstraintsSchema.getCoreConstraintsContext());
        }
        return stringBuilder.toString();
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

    public static IMFCompositionPlaylistType getCompositionPlayListType(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
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

        CoreConstraintsSchemas coreConstraintsSchema = supportedIMFCoreConstraintsSchemas.get(0);
        JAXBElement jaxbElement = null;

        for (int i = 0; i < supportedIMFCoreConstraintsSchemas.size(); i++) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(xmldsig_core_schema_path);
                 InputStream dcmlTypes_is = contextClassLoader.getResourceAsStream(dcmlTypes_schema_path);
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
                                    "Line Number : " + e.getLineNumber().toString() + " - " + e.getErrorMessage())
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

        String coreConstraintsVersion = coreConstraintsSchema.getCoreConstraintsContext();
        IMFCompositionPlaylistType compositionPlaylistType = null;

        switch (coreConstraintsVersion) {
            case "org.smpte_ra.schemas.st2067_2_2013": {
                org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistTypeJaxb =
                        (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType) jaxbElement.getValue();

                compositionPlaylistType = CompositionModel_st2067_2_2013.getCompositionPlaylist(compositionPlaylistTypeJaxb,
                        imfErrorLogger);
            }
            break;
            case "org.smpte_ra.schemas.st2067_2_2016": {
                org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistTypeJaxb = (org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType) jaxbElement.getValue();

                compositionPlaylistType = CompositionModel_st2067_2_2016.getCompositionPlaylist(compositionPlaylistTypeJaxb, imfErrorLogger);
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

        return compositionPlaylistType;
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
     * Getter for the Composition Playlist contentKind
     * @return a string representing contentKind of the Composition Playlist
     */
    public String getContentKind(){
        return this.contentKind;
    }

    /**
     * Getter for the Composition Playlist contentVersionList
     * @return an object representing contentVersionList of the Composition Playlist
     */
    public Composition.ContentVersionList getContentVersionList(){
        return this.contentVersionList;
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

    /**
     * Getter for the CoreConstraintsURI corresponding to this CompositionPlaylist
     *
     * @return the uri for the CoreConstraints schema for this CompositionPlaylist
     */
    public String getCoreConstraintsVersion() {
        return this.coreConstraintsVersion;
    }

    /**
     * Getter for the ApplicationIdentification corresponding to this CompositionPlaylist
     *
     * @return a string representing ApplicationIdentification for this CompositionPlaylist
     *
     * @deprecated
     * A CPL may declare multiple Application identifiers, the getter that returns a Set should be used instead.
     */
    @Deprecated
    public String getApplicationIdentification() {
        if (this.applicationIdSet.size() > 0) {
            return this.applicationIdSet.iterator().next();
        } else {
            return "";
        }
    }

    /**
     * Getter for the ApplicationIdentification Set corresponding to this CompositionPlaylist
     *
     * @return a set of all the strings representing ApplicationIdentification for this CompositionPlaylist
     */
    public Set<String> getApplicationIdentificationSet() {
        return this.applicationIdSet;
    }
}
