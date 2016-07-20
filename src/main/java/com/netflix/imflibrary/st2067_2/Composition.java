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
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.CompositionModels.CompositionModel_st2067_2_2013;
import com.netflix.imflibrary.st2067_2.CompositionModels.IMFCoreConstraintsChecker_st2067_2_2013;
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

/**
 * This class represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
@Immutable
public final class Composition
{
    private static final Logger logger = LoggerFactory.getLogger(Composition.class);

    private static final String dcmlTypes_schema_path = "org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd";
    private static final String xmldsig_core_schema_path = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    public static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>(){{ add("http://www.smpte-ra.org/schemas/2067-3/2013");}});

    private static class CoreConstraintsSchemas{
        private final String coreConstraintsSchemaPath;
        private final String coreConstraintsContext;

        private CoreConstraintsSchemas(String coreConstraintsSchemaPath, String coreConstraintsContext){
            this.coreConstraintsSchemaPath = coreConstraintsSchemaPath;
            this.coreConstraintsContext = coreConstraintsContext;
        }

        private String getCoreConstraintsSchemaPath(){
            return this.coreConstraintsSchemaPath;
        }

        private String getCoreConstraintsContext(){
            return this.coreConstraintsContext;
        }
    }
    public static final List<CoreConstraintsSchemas> supportedIMFCoreConstraintsSchemas = Collections.unmodifiableList
            (new ArrayList<CoreConstraintsSchemas>() {{ add( new CoreConstraintsSchemas("org/smpte_ra/schemas/st2067_2_2013/imf-core-constraints-20130620-pal.xsd", "org.smpte_ra.schemas.st2067_2_2013"));
                                                        add( new CoreConstraintsSchemas("org/smpte_ra/schemas/st2067_2_2016/imf-core-constraints.xsd", "org.smpte_ra.schemas.st2067_2_2016"));}});

    private final JAXBElement compositionPlaylistTypeJAXBElement;
    private final String coreConstraintsVersion;
    private final UUID uuid;
    private final EditRate editRate;
    private final Map<UUID, ? extends VirtualTrack> virtualTrackMap;

    /**
     * Constructor for a {@link Composition Composition} object from a XML file
     * @param compositionPlaylistXMLFile the input XML file that is conformed to schema and constraints specified in st2067-3:2013 and st2067-2:2013
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public Composition(File compositionPlaylistXMLFile, @Nonnull IMFErrorLogger imfErrorLogger)  throws IOException, SAXException, JAXBException, URISyntaxException {
        this(new FileByteRangeProvider(compositionPlaylistXMLFile), imfErrorLogger);
    }

    /**
     * Constructor for a {@link Composition Composition} object from a XML file
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public Composition(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger)  throws IOException, SAXException, JAXBException, URISyntaxException {

        int numErrors = imfErrorLogger.getNumberOfErrors();

        String cplNameSpaceURI = getCompositionNamespaceURI(resourceByteRangeProvider, imfErrorLogger);

        String namespaceVersion = getCPLNamespaceVersion(cplNameSpaceURI);
        String imf_cpl_schema_path = getIMFCPLSchemaPath(namespaceVersion);
        CoreConstraintsSchemas coreConstraintsSchema = this.supportedIMFCoreConstraintsSchemas.get(0);
        JAXBElement jaxbElement = null;

        for(int i=0; i<supportedIMFCoreConstraintsSchemas.size(); i++)
        {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(Composition.xmldsig_core_schema_path);
                 InputStream dcmlTypes_is = contextClassLoader.getResourceAsStream(Composition.dcmlTypes_schema_path);
                 InputStream imf_cpl_is = contextClassLoader.getResourceAsStream(imf_cpl_schema_path);
                 InputStream imf_core_constraints_is = contextClassLoader.getResourceAsStream(supportedIMFCoreConstraintsSchemas.get(i).coreConstraintsSchemaPath);)
            {
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

                if (validationEventHandlerImpl.hasErrors())
                {
                    throw new IMFException(validationEventHandlerImpl.toString());
                }
                //CompositionPlaylistType compositionPlaylistType = compositionPlaylistTypeJAXBElement.getValue();
                //this.compositionPlaylistType = compositionPlaylistType;
                break; //No errors so we can break out without trying other Core constraints schema namespaces.
            }
            catch (SAXException | JAXBException e)
            {
                if(i == supportedIMFCoreConstraintsSchemas.size()-1)
                {
                    throw e;
                }
            }
        }

        this.compositionPlaylistTypeJAXBElement = jaxbElement;
        this.coreConstraintsVersion = coreConstraintsSchema.getCoreConstraintsContext();

        switch(coreConstraintsVersion){
            case "org.smpte_ra.schemas.st2067_2_2013":
                org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType = (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType) this.compositionPlaylistTypeJAXBElement.getValue();
                this.virtualTrackMap = CompositionModel_st2067_2_2013.getVirtualTracksMap(compositionPlaylistType, imfErrorLogger);
                if(!IMFCoreConstraintsChecker_st2067_2_2013.checkVirtualTracks(compositionPlaylistType, this.virtualTrackMap, imfErrorLogger)){
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i=numErrors; i<imfErrorLogger.getErrors().size() ; i++){
                        stringBuilder.append(String.format("%n"));
                        stringBuilder.append(imfErrorLogger.getErrors().get(i));
                    }
                    throw new IMFException(String.format("Found following errors while validating the virtual tracks in the Composition %n %s", stringBuilder.toString()));
                }
                this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(compositionPlaylistType.getId());
                this.editRate = new EditRate(compositionPlaylistType.getEditRate());
                break;
            case "org.smpte_ra.schemas.st2067_2_2016":
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
            default:
                throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));

        }


        if ((imfErrorLogger != null) && (imfErrorLogger.getNumberOfErrors() > numErrors))
        {
            throw new IMFException(String.format("Found %d errors in CompositionPlaylist XML file", imfErrorLogger.getNumberOfErrors() - numErrors));
        }
    }

    private static final String getIMFCPLSchemaPath(String namespaceVersion){
        String imf_cpl_schema_path;
        switch(namespaceVersion){
            case "2013":
                imf_cpl_schema_path = "org/smpte_ra/schemas/st2067_3_2013/imf-cpl.xsd";
                break;
            case "2016":
                imf_cpl_schema_path = "org/smpte_ra/schemas/st2067_3_2016/imf-cpl.xsd";
                break;
            default:
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the following schema URIs %s", Utilities.serializeObjectCollectionToString(supportedCPLSchemaURIs)));
        }
        return imf_cpl_schema_path;
    }

    @Nullable
    private static final String getCompositionNamespaceURI(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull  IMFErrorLogger imfErrorLogger) throws IOException {

        String result = "";

        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new ErrorHandler()
            {
                @Override
                public void warning(SAXParseException exception) throws SAXException
                {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, exception.getMessage()));
                }

                @Override
                public void error(SAXParseException exception) throws SAXException
                {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, exception.getMessage()));
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException
                {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, exception.getMessage()));
                }
            });
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for(String cplNamespaceURI : Composition.supportedCPLSchemaURIs) {
                nodeList = document.getElementsByTagNameNS(cplNamespaceURI, "CompositionPlaylist");
                if (nodeList != null && nodeList.getLength() == 1)
                {
                    result = cplNamespaceURI;
                    break;
                }
            }
        }
        catch(ParserConfigurationException | SAXException e)
        {
            throw new IMFException(String.format("Error occurred while trying to determine the Composition Playlist Namespace URI, XML document appears to be invalid. Error Message : %s", e.getMessage()));
        }
        if(result.isEmpty()) {
            throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the following schema URIs %s", Utilities.serializeObjectCollectionToString(supportedCPLSchemaURIs)));
        }
        return result;
    }

    private static final String getCPLNamespaceVersion(String namespaceURI){
        String[] uriComponents = namespaceURI.split("/");
        String namespaceVersion = uriComponents[uriComponents.length - 1];
        return namespaceVersion;
    }

    private final String serializeIMFCoreConstaintsSchemasToString(List<CoreConstraintsSchemas> coreConstraintsSchemas){
        StringBuilder stringBuilder = new StringBuilder();
        for(CoreConstraintsSchemas coreConstraintsSchema : coreConstraintsSchemas){
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(coreConstraintsSchema.getCoreConstraintsContext());
        }
        return stringBuilder.toString();
    }

    /**
     * A stateless method that verifies if the raw data represented by the ResourceByteRangeProvider corresponds to a valid
     * IMF Composition Playlist document
     * @param resourceByteRangeProvider - a byte range provider for the document that needs to be verified
     * @return - a boolean indicating if the document represented is an IMF Composition or not
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
     * A method that returns a string representation of a Composition object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("======= Composition : %s =======%n", this.uuid));
        sb.append(this.editRate.toString());
        return sb.toString();
    }

    /**
     * A method that confirms if the inputStream corresponds to a Composition document instance.
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     * @return a boolean indicating if the input file is a Composition document
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
            for(String cplNamespaceURI : Composition.supportedCPLSchemaURIs) {
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
     * Getter for the composition edit rate as specified in the Composition XML file
     * @return the edit rate associated with the Composition
     */
    public EditRate getEditRate()
    {
        return this.editRate;
    }

    /**
     * Getter for the virtual track map associated with this Composition
     * @return {@link java.util.Map Map}&lt;{@link java.util.UUID UUID},{@link Composition.VirtualTrack VirtualTrack}&gt;. The UUID key corresponds to VirtualTrackID
     */
    public Map<UUID, ? extends VirtualTrack> getVirtualTrackMap()
    {
        return Collections.unmodifiableMap(this.virtualTrackMap);
    }

    /**
     * Getter for the UUID corresponding to this Composition document
     * @return the uuid of this Composition object
     */
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * Getter for the CompositionPlaylistType object model of the Composition defined by the st2067-3 schema.
     * @return the composition playlist type object model.
     */
    private JAXBElement getCompositionPlaylistTypeJAXBElement(){
        return this.compositionPlaylistTypeJAXBElement;
    }

    /**
     * Getter for the CoreConstraintsURI corresponding to this CompositionPlaylist
     * @return the uri for the CoreConstraints schema for this CompositionPlaylist
     */
    public String getCoreConstraintsVersion(){
        return this.coreConstraintsVersion;
    }

    /**
     * Getter for the video VirtualTrack in this Composition
     * @return the video virtual track that is a part of this composition or null if there is not video virtual track
     */
    @Nullable
    public VirtualTrack getVideoVirtualTrack(){
        switch(coreConstraintsVersion) {
            case "org.smpte_ra.schemas.st2067_2_2013":
                Iterator iterator = this.virtualTrackMap.entrySet().iterator();
                while (iterator != null
                        && iterator.hasNext()) {
                    Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
                    if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MainImageSequence)) {
                        return virtualTrack;
                    }
                }
                break;
            case "org.smpte_ra.schemas.st2067_2_2016":
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
            default:
                throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));
        }
        return null;
    }

    /**
     * Getter for the audio VirtualTracks in this Composition
     * @return a list of audio virtual tracks that are a part of this composition or an empty list if there are none
     */
    public List<? extends VirtualTrack> getAudioVirtualTracks(){
        List<VirtualTrack> audioVirtualTracks = new ArrayList<>();
        Iterator iterator = this.getVirtualTrackMap().entrySet().iterator();
        while(iterator != null
                && iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceTypeEnum().equals(SequenceTypeEnum.MainAudioSequence)) {
                audioVirtualTracks.add(virtualTrack);
            }
        }
        return Collections.unmodifiableList(audioVirtualTracks);
    }

    public static void validateCompositionPlaylistSchema(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException, SAXException {

        String cplNameSpaceURI = getCompositionNamespaceURI(resourceByteRangeProvider, imfErrorLogger);
        String namespaceVersion = getCPLNamespaceVersion(cplNameSpaceURI);
        String imf_cpl_schema_path = getIMFCPLSchemaPath(namespaceVersion);

        for (int i=0; i<supportedIMFCoreConstraintsSchemas.size(); i++)
        {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(Composition.xmldsig_core_schema_path);
                 InputStream dcmlTypes_is = contextClassLoader.getResourceAsStream(Composition.dcmlTypes_schema_path);
                 InputStream imf_cpl_is = contextClassLoader.getResourceAsStream(imf_cpl_schema_path);
                 InputStream imf_core_constraints_is = contextClassLoader.getResourceAsStream(supportedIMFCoreConstraintsSchemas.get(i).coreConstraintsSchemaPath);)
            {

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
            }
            catch (SAXException e)
            {
                if(i == supportedIMFCoreConstraintsSchemas.size()-1)
                {
                    throw e;
                }
            }
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
            else if(numbers.get(0) == 0
                    || numbers.get(1) == 0){
                throw new IMFException(String.format(
                        "Input list is expected to contain 2 non-zero numbers representing numerator and denominator of the EditRate respectively, found Numerator %d, Denominator %d",
                        numbers.get(0), numbers.get(1)));
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
         * A method that returns a string representation of a Composition object
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
     * Composition document that is compliant with st2067-2:2013. Such types are mostly defined in Section 6.3 of st2067-2:2013
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

        /**
         * A getter for the SequenceTypeEnum given a string that represents the name of a SequenceTypeEnum
         * @param name the string that should represent the SequenceTypeEnum
         * @return the SequenceTypeEnum value corresponding to the name that was passed
         */
        public static SequenceTypeEnum getSequenceTypeEnum(String name)
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

        /**
         * An override of the toString() method
         * @return a string representing the SequenceTypeEnum
         */
        @Override
        public String toString(){
            return this.name;
        }

    }

    /**
     * The class is an immutable implementation of the virtual track concept defined in Section 6.9.3 of st2067-3:2013. A
     * virtual track is characterized by its UUID and the type of sequence it holds
     */
    @Immutable
    public abstract static class VirtualTrack
    {
        protected final UUID trackID;
        protected final SequenceTypeEnum sequenceTypeEnum;
        protected final List<UUID> resourceIds = new ArrayList<>();

        /**
         * Constructor for a VirtualTrack object
         * @param trackID the UUID associated with this VirtualTrack object
         * @param sequenceTypeEnum the type of the associated sequence
         */
        public VirtualTrack(UUID trackID, SequenceTypeEnum sequenceTypeEnum)
        {
            this.trackID = trackID;
            this.sequenceTypeEnum = sequenceTypeEnum;
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
         * Getter for the UUIDs of the resources that are a part of this virtual track
         * @return an unmodifiable list of UUIDs of resources that are a part of this virtual track
         */
        public List<UUID> getTrackResourceIds(){
            return Collections.unmodifiableList(this.resourceIds);
        }

        /**
         * A method to determine the equivalence of any 2 virtual tracks.
         * @param other - the object to compare against
         * @return boolean indicating if the 2 virtual tracks are equivalent or represent the same timeline
         */
        public abstract boolean equivalent(VirtualTrack other);
    }

    /**
     * A utility method to retrieve the VirtualTracks within a Composition.
     * @return A list of VirtualTracks in the Composition.
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    @Nonnull
    public List<? extends Composition.VirtualTrack> getVirtualTracks() throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = this.getVirtualTrackMap();
        return new ArrayList<>(virtualTrackMap.values());
    }

    /**
     * A utility method to retrieve the UUIDs of the Track files referenced by a Virtual track within a Composition.
     * @param virtualTrack - object model of an IMF virtual track {@link Composition.VirtualTrack}
     * @return A list of TrackFileResourceType objects corresponding to the virtual track in the Composition.
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    @Nonnull
    public List<ResourceIdTuple> getVirtualTrackResourceIDs(@Nonnull Composition.VirtualTrack virtualTrack) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {

        List<ResourceIdTuple> virtualTrackResourceIDs = new ArrayList<>();
        switch(coreConstraintsVersion){
            case "org.smpte_ra.schemas.st2067_2_2013":
                CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013 virtualTrack_st2067_2_2013 = CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013.class.cast(virtualTrack);
                List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList = virtualTrack_st2067_2_2013.getResourceList();
                if(resourceList != null
                        && resourceList.size() > 0) {
                    for (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResourceType : resourceList) {
                        virtualTrackResourceIDs.add(new ResourceIdTuple(UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResourceType.getTrackFileId())
                                , UUIDHelper.fromUUIDAsURNStringToUUID(trackFileResourceType.getSourceEncoding())));
                    }
                }
                break;
            case "org.smpte_ra.schemas.st2067_2_2016":
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
            default:
                throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));
        }

        return Collections.unmodifiableList(virtualTrackResourceIDs);
    }

    /**
     * A utility method that will analyze the EssenceDescriptorList in a Composition and construct a HashMap mapping
     * a UUID to a EssenceDescriptor.
     * @return a HashMap mapping the UUID to its corresponding EssenceDescriptor in the Composition
     */
    public Map<UUID, DOMNodeObjectModel> getEssenceDescriptorListMap(){
        Map<UUID, DOMNodeObjectModel> essenceDescriptorMap = new HashMap<>();
        switch(this.coreConstraintsVersion) {
            case "org.smpte_ra.schemas.st2067_2_2013":
                org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType = (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType) this.compositionPlaylistTypeJAXBElement.getValue();
                List<org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType> essenceDescriptors = compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor();
                for (org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptors) {
                    UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(essenceDescriptorBaseType.getId());
                    DOMNodeObjectModel domNodeObjectModel = null;
                    for (Object object : essenceDescriptorBaseType.getAny()) {
                        domNodeObjectModel = new DOMNodeObjectModel((Node) object);
                    }
                    if (domNodeObjectModel != null) {
                        essenceDescriptorMap.put(uuid, domNodeObjectModel);
                    }
                }
                break;
            case "org.smpte_ra.schemas.st2067_2_2016":
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
            default:
                throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));
        }
        return Collections.unmodifiableMap(essenceDescriptorMap);
    }

    public Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> getAudioVirtualTracksMap() {

        List<? extends Composition.VirtualTrack> audioVirtualTracks = this.getAudioVirtualTracks();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = this.getEssenceDescriptorListMap();
        switch(this.coreConstraintsVersion) {
            case "org.smpte_ra.schemas.st2067_2_2013":
                Map<Set<DOMNodeObjectModel>, CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013> audioVirtualTrackMap = new HashMap<>();
                for (Composition.VirtualTrack audioVirtualTrack : audioVirtualTracks) {
                    Set<DOMNodeObjectModel> set = new HashSet<>();
                    CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013 audioVirtualTrack_st2067_2_2013 = CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013.class.cast(audioVirtualTrack);
                    List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resources = audioVirtualTrack_st2067_2_2013.getResourceList();
                    for (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType resource : resources) {
                        set.add(essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(resource.getSourceEncoding())));//Fetch and add the EssenceDescriptor referenced by the resource via the SourceEncoding element to the ED set.
                    }
                    audioVirtualTrackMap.put(set, audioVirtualTrack_st2067_2_2013);
                }
                return Collections.unmodifiableMap(audioVirtualTrackMap);
            case "org.smpte_ra.schemas.st2067_2_2016":
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
            default:
                throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));
        }
    }

    /**
     * This class is a representation of a Resource SourceEncoding element and trackFileId tuple.
     */
    public static final class ResourceIdTuple{
        private final UUID trackFileId;
        private final UUID sourceEncoding;

        private ResourceIdTuple(UUID trackFileId, UUID sourceEncoding){
            this.trackFileId = trackFileId;
            this.sourceEncoding = sourceEncoding;
        }

        /**
         * A getter for the trackFileId referenced by the resource corresponding to this ResourceIdTuple
         * @return the trackFileId associated with this ResourceIdTuple
         */
        public UUID getTrackFileId(){
            return this.trackFileId;
        }

        /**
         * A getter for the source encoding element referenced by the resource corresponding to this ResourceIdTuple
         * @return the source encoding element associated with this ResourceIdTuple
         */
        public UUID getSourceEncoding(){
            return this.sourceEncoding;
        }
    }

    /**
     * This method can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to the
     * resources referenced by the Composition.
     * @param headerPartitionTuples list of HeaderPartitionTuples corresponding to the IMF essences referenced in the Composition
     * @param imfErrorLogger an error logging object
     * @return boolean to indicate of the Composition is conformant or not
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public boolean conformVirtualTrackInComposition(List<IMPValidator.HeaderPartitionTuple> headerPartitionTuples,
                                                    IMFErrorLogger imfErrorLogger,
                                                    boolean conformAllVirtualTracks)
            throws IOException, IMFException, SAXException, JAXBException, URISyntaxException{
        boolean result = true;
        /*
         * The algorithm for conformance checking a Composition (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list (EDL) is referenced through its id element if conformAllVirtualTracks is enabled
         * by at least one TrackFileResource within the Virtual tracks in the Composition (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the Composition are present in
         * the physical essence files referenced by the resources of a virtual track and are equal.
         */
        /*The following check simultaneously verifies 1) and 2) from above.*/
        Set<UUID> resourceEssenceDescriptorIDsSet = getResourceEssenceDescriptorIdsSet();
        Iterator resourceEssenceDescriptorIDs = resourceEssenceDescriptorIDsSet.iterator();
        Set<UUID> cplEssenceDescriptorIDsSet = getEssenceDescriptorIdsSet();
        Iterator cplEssenceDescriptorIDs = cplEssenceDescriptorIDsSet.iterator();
        while(resourceEssenceDescriptorIDs.hasNext()){
            UUID resourceEssenceDescriptorUUID = (UUID) resourceEssenceDescriptorIDs.next();
            if(!cplEssenceDescriptorIDsSet.contains(resourceEssenceDescriptorUUID)) {
                result &= false;
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor ID %s referenced by a VirtualTrack Resource does not have a corresponding EssenceDescriptor in the EssenceDescriptorList in the CPL", resourceEssenceDescriptorUUID.toString()));
            }
        }

        /**
         * The following checks that at least one of the Virtual Tracks references an EssenceDescriptor in the EDL. This
         * check should be performed only when we need to conform all the Virtual Tracks in the CPL.
         */
        if(conformAllVirtualTracks) {
            while (cplEssenceDescriptorIDs.hasNext()) {
                UUID cplEssenceDescriptorUUID = (UUID) cplEssenceDescriptorIDs.next();
                if (!resourceEssenceDescriptorIDsSet.contains(cplEssenceDescriptorUUID)) {
                    result &= false;
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptorID %s in the CPL EssenceDescriptorList is not referenced by any resource in any of the Virtual tracks in the CPL, this violates the constraint in st2067-3:2013 section 6.1.10.1", cplEssenceDescriptorUUID.toString()));
                }
            }
        }

        if(!result){
            return result;
        }

        /*The following check verifies 3) from above.*/
        result &= compareEssenceDescriptors(getResourcesEssenceDescriptorMap(headerPartitionTuples), this.getEssenceDescriptorListMap(), imfErrorLogger);
        return result;
    }

    private Set<UUID> getEssenceDescriptorIdsSet () {
        HashSet<UUID> essenceDescriptorIdsSet = new LinkedHashSet<>();
        switch(this.coreConstraintsVersion) {
            case "org.smpte_ra.schemas.st2067_2_2013":
                org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType = (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType)this.getCompositionPlaylistTypeJAXBElement().getValue();
                List<org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType> essenceDescriptorList = compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor();
                for (org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptorList) {
                    UUID sourceEncodingElement = UUIDHelper.fromUUIDAsURNStringToUUID(essenceDescriptorBaseType.getId());
                    /*Construct a set of SourceEncodingElements/IDs corresponding to every EssenceDescriptorBaseType in the EssenceDescriptorList*/
                    essenceDescriptorIdsSet.add(sourceEncodingElement);
                }
                break;
            case "org.smpte_ra.schemas.st2067_2_2016":
                throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
            default:
                throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));
        }
        return essenceDescriptorIdsSet;
    }



    private Set<UUID> getResourceEssenceDescriptorIdsSet () throws IOException, SAXException, JAXBException, URISyntaxException{
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(this.getVirtualTrackMap().values());
        LinkedHashSet<UUID> resourceSourceEncodingElementsSet = new LinkedHashSet<>();
        for(Composition.VirtualTrack virtualTrack : virtualTracks){
            List<Composition.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);
            for(Composition.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        return resourceSourceEncodingElementsSet;
    }

    private Map<UUID, List<DOMNodeObjectModel>> getResourcesEssenceDescriptorMap(List<IMPValidator.HeaderPartitionTuple> headerPartitionTuples) throws IOException, SAXException, JAXBException, URISyntaxException{
        Map<UUID, List<DOMNodeObjectModel>> resourcesEssenceDescriptorMap = new LinkedHashMap<>();

        /*Create a Map of FilePackage UUID which should be equal to the TrackFileId of the resource in the Composition if the asset is referenced and the HeaderPartitionTuple, Map<UUID, HeaderPartitionTuple>*/
        Map<UUID, IMPValidator.HeaderPartitionTuple> resourceUUIDHeaderPartitionMap = new HashMap<>();
        for(IMPValidator.HeaderPartitionTuple headerPartitionTuple : headerPartitionTuples) {
            //validate header partition
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartitionTuple.getHeaderPartition());
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
            Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
            resourceUUIDHeaderPartitionMap.put(packageUUID, headerPartitionTuple);
        }
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(this.getVirtualTrackMap().values());

        /*Go through all the Virtual Tracks in the Composition and construct a map of Resource Source Encoding Element and a list of DOM nodes representing every EssenceDescriptor in the HeaderPartition corresponding to that Resource*/
        for(Composition.VirtualTrack virtualTrack : virtualTracks){
            List<Composition.ResourceIdTuple> resourceIdTuples = this.getVirtualTrackResourceIDs(virtualTrack);/*Retrieve a list of ResourceIDTuples corresponding to this virtual track*/
            for(Composition.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                IMPValidator.HeaderPartitionTuple headerPartitionTuple = resourceUUIDHeaderPartitionMap.get(resourceIdTuple.getTrackFileId());
                if(headerPartitionTuple != null){
                    /*Create a DOM Node representation of the EssenceDescriptors present in this header partition corresponding to an IMFTrackFile*/
                    List<Node> essenceDescriptorDOMNodes = getEssenceDescriptorDOMNodes(headerPartitionTuple);
                    List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
                    for(Node node : essenceDescriptorDOMNodes){
                        domNodeObjectModels.add(new DOMNodeObjectModel(node));
                    }
                    resourcesEssenceDescriptorMap.put(resourceIdTuple.getSourceEncoding(), domNodeObjectModels);
                }
            }
        }
        if(resourcesEssenceDescriptorMap.entrySet().size() == 0){
            throw new MXFException(String.format("Composition does not refer to a single IMFEssence represented by the HeaderPartitions that were passed in."));
        }
        return Collections.unmodifiableMap(resourcesEssenceDescriptorMap);
    }

    private List<Node> getEssenceDescriptorDOMNodes(IMPValidator.HeaderPartitionTuple headerPartitionTuple) throws IOException {
        try {
            List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = headerPartitionTuple.getHeaderPartition().getEssenceDescriptors();
            List<Node> essenceDescriptorNodes = new ArrayList<>();
            for (InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                List<KLVPacket.Header> subDescriptorHeaders = this.getSubDescriptorKLVHeader(headerPartitionTuple.getHeaderPartition(), essenceDescriptor);
                /*Create a dom*/
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();

                DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(document, headerPartitionTuple, essenceDescriptorHeader, subDescriptorHeaders);
                Node node = documentFragment.getFirstChild();
                essenceDescriptorNodes.add(node);
            }
            return essenceDescriptorNodes;
        }
        catch(ParserConfigurationException e){
            throw new IMFException(e);
        }
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

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, IMPValidator.HeaderPartitionTuple headerPartitionTuple, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header>subDescriptors) throws MXFException, IOException {
        document.setXmlStandalone(true);

        PrimerPack primerPack = headerPartitionTuple.getHeaderPartition().getPrimerPack();
        ResourceByteRangeProvider resourceByteRangeProvider = headerPartitionTuple.getResourceByteRangeProvider();


        RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(primerPack.getHeader(), getByteProvider(resourceByteRangeProvider, primerPack.getHeader()));

        Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, getByteProvider(resourceByteRangeProvider, essenceDescriptor));
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
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        return byteProvider;
    }

    private boolean compareEssenceDescriptors(Map<UUID, List<DOMNodeObjectModel>> essenceDescriptorsMap, Map<UUID, DOMNodeObjectModel> eDLMap, IMFErrorLogger imfErrorLogger){

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
        while(essenceDescriptorsMapIterator.hasNext()){
            UUID sourceEncodingElement = essenceDescriptorsMapIterator.next().getKey();
            if(!eDLMap.keySet().contains(sourceEncodingElement)){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Source Encoding Element %s in a track does not have a corresponding entry in the CPL's EDL", sourceEncodingElement.toString()));
                return false;
            }
        }
        /**
         * The following check ensures that we have atleast one EssenceDescriptor in a TrackFile that equals the corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, DOMNodeObjectModel>> eDLMapIterator = eDLMap.entrySet().iterator();
        while(eDLMapIterator.hasNext()){
            Map.Entry<UUID, DOMNodeObjectModel> entry = (Map.Entry<UUID, DOMNodeObjectModel>) eDLMapIterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = essenceDescriptorsMap.get(entry.getKey());
            if(domNodeObjectModels == null){
                //This implies we did not find a single VirtualTrack that referenced this particular EssenceDescriptor in the EDL
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Id %s in the CPL's EDL is not referenced by a single resource within any of the VirtualTracks in the CPL, this violates the constraint in st2067-3:2013 section 6.1.10.1", entry.getKey().toString()));
                return false;
            }
            DOMNodeObjectModel referenceDOMNodeObjectModel = entry.getValue();
            boolean intermediateResult = false;
            for(DOMNodeObjectModel domNodeObjectModel : domNodeObjectModels){
                intermediateResult |= referenceDOMNodeObjectModel.equals(domNodeObjectModel);
            }
            if(!intermediateResult){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Id %s in the CPL's EDL doesn't match any EssenceDescriptors within the IMFTrackFile that references it", entry.getKey().toString()));
                return false;
            }
        }

        return true;
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFile>%n", Composition.class.getName()));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 1)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);

        logger.info(String.format("File Name is %s", inputFile.getName()));

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try
        {
            Composition composition = new Composition(inputFile, imfErrorLogger);
            logger.info(composition.toString());

            List<? extends Composition.VirtualTrack> virtualTracks = composition.getVirtualTracks();
            List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();

            switch(composition.getCoreConstraintsVersion())
            {
                case "org.smpte_ra.schemas.st2067_2_2013":
                    org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType = (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType) composition.getCompositionPlaylistTypeJAXBElement().getValue();
                    if (compositionPlaylistType.getEssenceDescriptorList() != null)
                    {
                        for (org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType essenceDescriptorBaseType : compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor())
                        {
                            for (Object object : essenceDescriptorBaseType.getAny())
                            {
                                Node node = (Node) object;
                                domNodeObjectModels.add(new DOMNodeObjectModel(node));
                            }
                        }
                    }
                    else
                    {
                        logger.error("No essence descriptor list was found in CPL");
                    }
                    for(Composition.VirtualTrack virtualTrack : virtualTracks)
                    {
                        CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013 virtualTrack_st2067_2_2013 = (CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013) virtualTrack;
                        List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList = virtualTrack_st2067_2_2013.getResourceList();
                        if (resourceList.size() == 0)
                        {
                            throw new Exception(String.format("CPL file has a VirtualTrack with no resources which is invalid"));
                        }
                    }
                    break;

                case "org.smpte_ra.schemas.st2067_2_2016":
                    throw new IMFException(String.format("Please check the CPL document and namespace URI, currently we only support the 2013 CoreConstraints schema URI"));
                default:
                    throw new IMFException(String.format("Please check the CPL document, currently we only support the following CoreConstraints schema URIs %s", composition.serializeIMFCoreConstaintsSchemasToString(supportedIMFCoreConstraintsSchemas)));

            }

            for(int i=0; i<domNodeObjectModels.size(); i++)
            {
                logger.info(String.format("ObjectModel of EssenceDescriptor-%d in the EssenceDescriptorList in the CPL: %n%s", i, domNodeObjectModels.get(i).toString()));
            }
        }
        finally
        {
            for (ErrorLogger.ErrorObject errorObject : imfErrorLogger.getErrors())
            {
                logger.error(errorObject.toString());
            }
        }
    }

}
