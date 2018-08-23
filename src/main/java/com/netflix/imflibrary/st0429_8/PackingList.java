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
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final IMFErrorLogger imfErrorLogger;
    private static final String xmldsig_core_schema_path = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    public static final List<String> supportedPKLNamespaces = Collections.unmodifiableList(new ArrayList<String>(){{ add("http://www.smpte-ra.org/schemas/429-8/2007/PKL");
                                                                                                                        add("http://www.smpte-ra.org/schemas/2067-2/2016/PKL");}});

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
    public static final Map<String, PKLSchema> supportedPKLSchemas = Collections.unmodifiableMap
            (new HashMap<String, PKLSchema>() {{ put("http://www.smpte-ra.org/schemas/429-8/2007/PKL", new PKLSchema("org/smpte_ra/schemas/st0429_8_2007/PKL/packingList_schema.xsd", "org.smpte_ra.schemas.st0429_8_2007.PKL"));
                                            put("http://www.smpte-ra.org/schemas/2067-2/2016/PKL", new PKLSchema("org/smpte_ra/schemas/st2067_2_2016/PKL/packingList_schema.xsd", "org.smpte_ra.schemas.st2067_2_2016.PKL"));}});

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object that corresponds to a PackingList XML document
     * @param packingListXMLFile the input XML file
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public PackingList(FileLocator packingListXMLFile) throws IOException {
        this(packingListXMLFile.getResourceByteRangeProvider());
    }

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object that corresponds to a PackingList XML document
     * @param resourceByteRangeProvider corresponding to the PackingList XML file
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public PackingList(ResourceByteRangeProvider resourceByteRangeProvider)throws IOException {

        JAXBElement<PackingListType> packingListTypeJAXBElement = null;
        imfErrorLogger = new IMFErrorLoggerImpl();

        String packingListNamespaceURI = getPackingListSchemaURI(resourceByteRangeProvider, imfErrorLogger);
        PKLSchema pklSchema = supportedPKLSchemas.get(packingListNamespaceURI);
        if(pklSchema == null){
            String message = String.format("Please check the PKL document, currently we only support the " +
                    "following schema URIs %s", serializePKLSchemasToString());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(PackingList.xmldsig_core_schema_path);
                 InputStream pkl_is = contextClassLoader.getResourceAsStream(pklSchema.getPKLSchemaPath());
            ) {
                StreamSource[] streamSources = new StreamSource[2];
                streamSources[0] = new StreamSource(xmldsig_core_is);
                streamSources[1] = new StreamSource(pkl_is);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(streamSources);

                ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
                JAXBContext jaxbContext = JAXBContext.newInstance(pklSchema.getPKLContext());
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(validationEventHandlerImpl);
                unmarshaller.setSchema(schema);

                packingListTypeJAXBElement = (JAXBElement) unmarshaller.unmarshal(inputStream);

                if (validationEventHandlerImpl.hasErrors()) {
                    List<ValidationEventHandlerImpl.ValidationErrorObject> errors = validationEventHandlerImpl.getErrors();
                    for (ValidationEventHandlerImpl.ValidationErrorObject error : errors) {
                        String errorMessage = "Line Number : " + error.getLineNumber().toString() + " - " + error.getErrorMessage();
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, error.getValidationEventSeverity(), errorMessage);
                    }
                    throw new IMFException(validationEventHandlerImpl.toString(), imfErrorLogger);
                }
            }
        }
        catch(SAXException | JAXBException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    e.getMessage());
        }

        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("PackingList parsing failed", imfErrorLogger);
        }

        this.pklSchema = pklSchema;
        this.packingListTypeJAXBElement = packingListTypeJAXBElement;

        switch(this.pklSchema.getPKLContext())
        {
            case "org.smpte_ra.schemas.st0429_8_2007.PKL":
                //this.packingListType = PackingList.checkConformance(packingListTypeJAXBElement.getValue());
                org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType packingListType_st0429_8_2007_PKL = (org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType) this.packingListTypeJAXBElement.getValue();
                this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(packingListType_st0429_8_2007_PKL.getId());

                for (org.smpte_ra.schemas.st0429_8_2007.PKL.AssetType assetType : packingListType_st0429_8_2007_PKL.getAssetList().getAsset())
                {
                    Asset asset = new Asset(assetType.getId(), Arrays.copyOf(assetType.getHash(), assetType.getHash().length),
					    assetType.getSize().longValue(), assetType.getType(),
					    assetType.getOriginalFileName() != null ? assetType.getOriginalFileName().getValue() : null);
                    this.assetList.add(asset);
                }
                break;
            case "org.smpte_ra.schemas.st2067_2_2016.PKL":
                org.smpte_ra.schemas.st2067_2_2016.PKL.PackingListType packingListType_st2067_2_2016_PKL = (org.smpte_ra.schemas.st2067_2_2016.PKL.PackingListType) this.packingListTypeJAXBElement.getValue();
                this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(packingListType_st2067_2_2016_PKL.getId());

                for (org.smpte_ra.schemas.st2067_2_2016.PKL.AssetType assetType : packingListType_st2067_2_2016_PKL.getAssetList().getAsset())
                {
                    Asset asset = new Asset(assetType.getId(), Arrays.copyOf(assetType.getHash(), assetType.getHash().length),
					    assetType.getSize().longValue(), assetType.getType(),
					    assetType.getOriginalFileName() != null ? assetType.getOriginalFileName().getValue() : null,
					    assetType.getHashAlgorithm().getAlgorithm());
                    this.assetList.add(asset);
                }
                break;
            default:
                String message = String.format("Please check the PKL document, currently we only support the " +
                        "following schema URIs %s", serializePKLSchemasToString());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL, message);
                throw new IMFException(message, imfErrorLogger);
        }

        Set<UUID> assetUUIDs = new HashSet<>();
        for(Asset asset : this.assetList){
            if(assetUUIDs.contains(asset.getUUID())){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("More than one PackingList Asset seems to use AssetUUID %s this is invalid.", asset.getUUID().toString()));
            }
            else{
                assetUUIDs.add(asset.getUUID());
            }
        }
    }

    private static String getPackingListSchemaURI(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {

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
            String message = String.format("Error occurred while trying to determine the PackingList Namespace " +
                            "URI, invalid PKL document Error Message : %s", e.getMessage());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        if(packingListSchemaURI.isEmpty()) {
            String message = String.format("Please check the PKL document and namespace URI, currently we only " +
                    "support the following schema URIs %s", serializePKLSchemasToString());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        return packingListSchemaURI;
    }

    private static final String serializePKLSchemasToString(){
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iterator = supportedPKLSchemas.values().iterator();
        while(iterator.hasNext()){
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(((PKLSchema)iterator.next()).getPKLContext());
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
     * Getter for the errors in PackingList
     *
     * @return List of errors in PackingList.
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }

    /**
     * This class represents a thin, immutable wrapper around the XML type 'AssetType' which is defined in Section 7,
     * st0429-8:2007. It exposes a minimal set of properties of the wrapped object through appropriate Getter methods
     */
    public static final class Asset
    {
        public static final String APPLICATION_MXF_TYPE = "application/mxf";
        public static final String TEXT_XML_TYPE = "text/xml";
        private static final String DEFAULT_HASH_ALGORITHM = "http://www.w3.org/2000/09/xmldig#sha1";

        private final UUID uuid;
        private final byte[] hash;
        private final long size;
        private final String type;
        private final String original_filename;
        private final String hash_algorithm;

        /**
         * Constructor for the wrapping {@link com.netflix.imflibrary.st0429_8.PackingList.Asset Asset} object from the wrapped model version of XML type 'AssetType'
         * @param uuid identifying the PackingList Asset
         * @param hash hash a byte[] containing the Base64 encoded SHA-1 hash of this PackingList Asset
         * @param size of the asset in bytes
         * @param type could be either text/xml or application/mxf as defined in st0429-9:2007
         * @param original_filename a free form human readable text that contains the name of the file
         *                         containing the asset at the time the PackingList was created
         */
        public Asset(String uuid, byte[] hash, long size, String type, String original_filename)
        {
            this(uuid, hash, size, type, original_filename, Asset.DEFAULT_HASH_ALGORITHM);
        }

        public Asset(String uuid, byte[] hash, long size, String type, String original_filename, String hash_algorithm)
        {
            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(uuid);
            this.hash = Arrays.copyOf(hash, hash.length);
            this.size = size;
            this.type = type;
            this.original_filename = original_filename;
            this.hash_algorithm = hash_algorithm;
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

        public byte[] getHash(){
            return Arrays.copyOf(this.hash, this.hash.length);
        }

        /**
         * Getter for the Hash Algorithm used in generating the Hash of this Asset
         * @return a string corresponding to the HashAlgorithm
         */
        public String getHashAlgorithm(){
            return this.hash_algorithm;
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
            sb.append(String.format("hash_algorithm = %s%n", this.hash_algorithm));
            return sb.toString();
        }

    }

    public static void validatePackingListSchema(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException, SAXException {

        String pklNamespaceURI = PackingList.getPackingListSchemaURI(resourceByteRangeProvider, imfErrorLogger);
        PKLSchema pklSchema = supportedPKLSchemas.get(pklNamespaceURI);
        if(pklSchema == null){
            String message = String.format("Please check the PKL document, currently we only support the " +
                    "following schema URIs %s", serializePKLSchemasToString());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
             InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(PackingList.xmldsig_core_schema_path);
             InputStream pkl_is = contextClassLoader.getResourceAsStream(pklSchema.getPKLSchemaPath());
        )
        {
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
        }
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath>%n", PackingList.class.getName()));
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
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validatePKL(payloadRecord);

        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("PackingList Document has %d errors and %d warnings",
                    errors.size() - warningCount, warningCount));
            for(ErrorLogger.ErrorObject errorObject : errors){
                if(errorObject.getErrorLevel()!= IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                }
                else if(errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
        }
        else{
            logger.info("No errors were detected in the PackingList Document");
        }
    }

}
