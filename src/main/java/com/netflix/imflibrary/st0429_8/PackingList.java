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

package com.netflix.imflibrary.st0429_8;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st0429_8_2007.PKL.AssetType;
import org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType;
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
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * This class represents a thin, immutable wrapper around the XML type 'PackingListType' which is defined in Section 7,
 * st0429-8:2007. A PackingList object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st0429-8:2007
 */
@Immutable
public final class PackingList
{
    private static final Logger logger = LoggerFactory.getLogger(PackingList.class);

    private static final String xmldsig_core_schema_path = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    public static final List<String> supportedPKLNamespaces = Collections.unmodifiableList(new ArrayList<String>(){{ add("http://www.smpte-ra.org/schemas/429-8/2007/PKL");}});

    private final UUID uuid;
    private final JAXBElement packingListTypeJAXBElement;
    private final PKLSchema pklSchema;
    private final List<Asset> assetList = new ArrayList<>();

    private static class PKLSchema {
        private final String pklSchemaPath;
        private final String pklContext;

        private PKLSchema(String pklSchemaPath, String pklContext){
            this.pklSchemaPath = pklSchemaPath;
            this.pklContext = pklContext;
        }

        private String getPKLSchemaPath(){
            return this.pklSchemaPath;
        }

        private String getPKLContext(){
            return this.pklContext;
        }
    }
    public static final List<PKLSchema> supportedPKLSchemas = Collections.unmodifiableList
            (new ArrayList<PKLSchema>() {{ add( new PKLSchema("org/smpte_ra/schemas/st0429_8_2007/PKL/packingList_schema.xsd", "org.smpte_ra.schemas.st0429_8_2007.PKL"));
                add( new PKLSchema("org/smpte_ra/schemas/st0429_8_2016/PKL/packingList_schema.xsd", "org.smpte_ra.schemas.st0429_8_2016.PKL"));}});

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object that corresponds to a PackingList XML document
     * @param packingListXMLFile the input XML file
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     */
    public PackingList(File packingListXMLFile, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException, SAXException, JAXBException {
        this(new FileByteRangeProvider(packingListXMLFile), imfErrorLogger);
    }

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object that corresponds to a PackingList XML document
     * @param resourceByteRangeProvider corresponding to the PackingList XML file
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     */
    public PackingList(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger)throws IOException, SAXException, JAXBException {

        validatePackingListSchema(resourceByteRangeProvider, imfErrorLogger);

        JAXBElement<PackingListType> packingListTypeJAXBElement = null;
        PKLSchema pklSchema = null;

        for(int i=0; i<supportedPKLSchemas.size(); i++) {
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = ClassLoader.getSystemResourceAsStream(PackingList.xmldsig_core_schema_path);
                 InputStream pkl_is = ClassLoader.getSystemResourceAsStream(supportedPKLSchemas.get(i).getPKLSchemaPath());
            ) {
                StreamSource[] streamSources = new StreamSource[2];
                streamSources[0] = new StreamSource(xmldsig_core_is);
                streamSources[1] = new StreamSource(pkl_is);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(streamSources);

                ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
                JAXBContext jaxbContext = JAXBContext.newInstance(supportedPKLSchemas.get(i).getPKLContext());
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(validationEventHandlerImpl);
                unmarshaller.setSchema(schema);

                packingListTypeJAXBElement = (JAXBElement) unmarshaller.unmarshal(inputStream);
                pklSchema = supportedPKLSchemas.get(i);

                if (validationEventHandlerImpl.hasErrors()) {
                    List<ValidationEventHandlerImpl.ValidationErrorObject> errors = validationEventHandlerImpl.getErrors();
                    for (ValidationEventHandlerImpl.ValidationErrorObject error : errors) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, error.getValidationEventSeverity(), error.getErrorMessage());
                    }
                    throw new IMFException(validationEventHandlerImpl.toString());
                }
                break;
            }
            catch (SAXException | JAXBException e){
                if(i == supportedPKLSchemas.size()-1){
                    throw e;
                }
            }
        }
        this.pklSchema = pklSchema;
        this.packingListTypeJAXBElement = packingListTypeJAXBElement;

        switch(this.pklSchema.getPKLContext()) {
            case "org.smpte_ra.schemas.st0429_8_2007.PKL":
                //this.packingListType = PackingList.checkConformance(packingListTypeJAXBElement.getValue());
                org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType packingListType = (org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType) this.packingListTypeJAXBElement.getValue();
                this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(packingListType.getId());

                for (org.smpte_ra.schemas.st0429_8_2007.PKL.AssetType assetType : packingListType.getAssetList().getAsset()) {
                    Asset asset = new Asset(assetType.getId(), Arrays.copyOf(assetType.getHash(), assetType.getHash().length), assetType.getSize().longValue(), assetType.getType(), assetType.getOriginalFileName().getValue());
                    this.assetList.add(asset);
                }
                break;
            case "org.smpte_ra.schemas.st0429_8_2016.PKL":
                throw new IMFException(String.format("Please check the PKL document and namespace URI, currently we only support the 2007 PKL schema URI"));
            default:
                throw new IMFException(String.format("Please check the PKL document, currently we only support the following schema URIs %s", serializePKLSchemasToString(supportedPKLSchemas)));
        }

    }

    private String getPackingListSchemaURI(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {

        String packingListSchemaURI = "";
        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
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
            NodeList nodeList = null;
            for(String supportedSchemaURI : supportedPKLNamespaces) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, "PackingList");
                if (nodeList != null
                        && nodeList.getLength() == 1)
                {
                    packingListSchemaURI = supportedSchemaURI;
                    break;
                }
            }
        }
        catch(ParserConfigurationException | SAXException e)
        {
            throw new IMFException(String.format("Error occurred while trying to determine the PackingList Namespace URI, invalid PKL document Error Message : %s", e.getMessage()));
        }
        if(packingListSchemaURI.isEmpty()) {
            throw new IMFException(String.format("Please check the PKL document and namespace URI, currently we only support the following schema URIs %s", serializePKLSchemasToString(supportedPKLSchemas)));
        }
        return packingListSchemaURI;
    }

    private final String serializePKLSchemasToString(List<PKLSchema> pklSchemas){
        StringBuilder stringBuilder = new StringBuilder();
        for(PKLSchema pklSchema : pklSchemas){
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(pklSchema.getPKLContext());
        }
        return stringBuilder.toString();
    }

    /**
     * A stateless method that verifies if the raw data represented by the ResourceByteRangeProvider corresponds to a valid
     * IMF Packing List document
     * @param resourceByteRangeProvider - a byte range provider for the document that needs to be verified
     * @return - a boolean indicating if the document represented is an IMF PackingList or not
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
            for(String supportedSchemaURI : supportedPKLNamespaces) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, "PackingList");
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

    private static PackingListType checkConformance(PackingListType packingListType)
    {
        return packingListType;
    }

    /**
     * Getter for the complete list of assets present in this PackingList
     * @return the list of assets present in this PackingList
     */
    public List<Asset> getAssets()
    {
        return Collections.unmodifiableList(this.assetList);
    }

    /**
     * Getter for the UUID corresponding to this PackingList object
     * @return the uuid of this PackingList object
     */
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * A method that returns a string representation of a PackingList object
     *
     * @return string representing the object
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=================== PackingList : %s%n", this.uuid));
        for (Asset asset : this.assetList)
        {
            sb.append(asset.toString());
        }
        return sb.toString();
    }

    /**
     * This class represents a thin, immutable wrapper around the XML type 'AssetType' which is defined in Section 7,
     * st0429-8:2007. It exposes a minimal set of properties of the wrapped object through appropriate Getter methods
     */
    public static final class Asset
    {
        private final UUID uuid;
        private final byte[] hash;
        private final long size;
        private final String type;
        private final String original_filename;

        /**
         * Constructor for the wrapping {@link com.netflix.imflibrary.st0429_8.PackingList.Asset Asset} object from the wrapped model version of XML type 'AssetType'
         * @param uuid
         * @param hash
         * @param size
         * @param type
         * @param original_filename
         */
        public Asset(String uuid, byte[] hash, long size, String type, String original_filename)
        {
            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(uuid);
            this.hash = Arrays.copyOf(hash, hash.length);
            this.size = size;
            this.type = type;
            this.original_filename = original_filename;
        }

        /**
         * Getter for the UUID associated with this object
         * @return the asset UUID
         */
        public UUID getUUID()
        {
            return this.uuid;
        }

        /**
         * Getter for the size of the underlying file associated with this object
         * @return the file size
         */
        public long getSize()
        {
            return this.size;
        }

        /**
         * Getter for the MIME type of the underlying file associated with this object
         * @return the MIME type as a string
         */
        public String getType()
        {
            return this.type;
        }

        /**
         * Getter for the filename of the underlying file associated with this object
         * @return the filename or null if no file name was present
         */
        public @Nullable String getOriginalFilename()
        {
            return this.original_filename;
        }

        /**
         * A method that returns a string representation of a PackingList Asset object
         *
         * @return string representing the object
         */
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("=================== Asset : %s%n", this.getUUID()));
            sb.append(String.format("hash = %s%n", Arrays.toString(this.hash)));
            sb.append(String.format("size = %d%n", this.getSize()));
            sb.append(String.format("type = %s%n", this.getType()));
            sb.append(String.format("original_filename = %s%n", this.getOriginalFilename()));
            return sb.toString();
        }

    }

    private void validatePackingListSchema(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException, SAXException {

        for(int i=0; i<supportedPKLSchemas.size(); i++) {
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = ClassLoader.getSystemResourceAsStream(PackingList.xmldsig_core_schema_path);
                 InputStream pkl_is = ClassLoader.getSystemResourceAsStream(supportedPKLSchemas.get(i).getPKLSchemaPath());
            ) {
                StreamSource inputSource = new StreamSource(inputStream);

                StreamSource[] streamSources = new StreamSource[2];
                streamSources[0] = new StreamSource(xmldsig_core_is);
                streamSources[1] = new StreamSource(pkl_is);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(streamSources);

                Validator validator = schema.newValidator();
                validator.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, exception.getMessage()));
                    }

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, exception.getMessage()));
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, exception.getMessage()));
                    }
                });
                validator.validate(inputSource);
                break;
            }
            catch (SAXException e){
                if(i == supportedPKLSchemas.size()-1){
                    throw e;
                }
            }
        }
    }

    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException, JAXBException
    {
        File inputFile = new File(args[0]);

        PackingList packingList = new PackingList(inputFile, new IMFErrorLoggerImpl());
        logger.warn(packingList.toString());

    }

}
