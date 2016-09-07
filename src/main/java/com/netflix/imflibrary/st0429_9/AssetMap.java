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

package com.netflix.imflibrary.st0429_9;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetMapType;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetType;
import org.smpte_ra.schemas.st0429_9_2007.AM.ChunkType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
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
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class represents a thin, immutable wrapper around the XML type 'AssetMapType' which is defined in Section 11,
 * st0429-9:2014. An AssetMap object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st0429-9:2014
 */
@Immutable
public final class AssetMap
{
    private final UUID uuid;
    private final List<Asset> assetList = new ArrayList<>();
    private final List<Asset> packingListAssets = new ArrayList<>();
    private final Map<UUID, URI> uuidToPath = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AssetMap.class);
    public static final List<String> supportedAssetMapSchemaURIs = Collections.unmodifiableList(new ArrayList<String>(){{ add("http://www.smpte-ra.org/schemas/429-9/2007/AM");}});

    public static final Map<String, AssetMapSchema> supportedAssetMapSchemas = Collections.unmodifiableMap
            (new HashMap<String, AssetMapSchema>() {{ put("http://www.smpte-ra.org/schemas/429-9/2007/AM", new AssetMapSchema("org/smpte_ra/schemas/st0429_9_2007/AM/assetMap_schema.xsd", "org.smpte_ra.schemas.st0429_9_2007.AM"));}});
    private final IMFErrorLogger imfErrorLogger;
    private static class AssetMapSchema {
        private final String assetMapSchemaPath;
        private final String assetMapContext;

        private AssetMapSchema(String assetMapSchemaPath, String assetMapContext){
            this.assetMapSchemaPath = assetMapSchemaPath;
            this.assetMapContext = assetMapContext;
        }

        private String getAssetMapSchemaPath(){
            return this.assetMapSchemaPath;
        }

        private String getAssetMapContext(){
            return this.assetMapContext;
        }
    }
    /**
     * Constructor for an {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object from an XML file that contains an AssetMap document
     * @param assetMapXmlFile the input XML file
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public AssetMap(File assetMapXmlFile) throws IOException
    {
        this(getFileAsResourceByteRangeProvider(assetMapXmlFile));
    }

    /**
     * Constructor for an {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object from an XML file that contains an AssetMap document
     * @param resourceByteRangeProvider that supports the mark() (mark position should be set to point to the beginning of the file) and reset() methods corresponding to the input XML file
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public AssetMap(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException
    {
        imfErrorLogger = new IMFErrorLoggerImpl();

        JAXBElement assetMapTypeJAXBElement = null;

        String assetMapNamespaceURI = getAssetMapNamespaceURI(resourceByteRangeProvider);
        AssetMapSchema assetMapSchema = supportedAssetMapSchemas.get(assetMapNamespaceURI);
        if(assetMapSchema == null){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, String.format("Please check the AssetMap document, currently we only support " +
                    "the following schema URIs %s", serializeAssetMapSchemasToString()));
            throw new IMFException(String.format("Please check the AssetMap document, currently we only support the " +
                    "following schema URIs %s", serializeAssetMapSchemasToString()), imfErrorLogger);
        }
        
        try {
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream assetMap_schema_is = Thread.currentThread().getContextClassLoader().getResourceAsStream(assetMapSchema.getAssetMapSchemaPath());) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StreamSource schemaSource = new StreamSource(assetMap_schema_is);
                Schema schema = schemaFactory.newSchema(schemaSource);

                ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
                JAXBContext jaxbContext = JAXBContext.newInstance(assetMapSchema.getAssetMapContext());
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(validationEventHandlerImpl);
                unmarshaller.setSchema(schema);

                assetMapTypeJAXBElement = (JAXBElement) unmarshaller.unmarshal(inputStream);

                if (validationEventHandlerImpl.hasErrors()) {
                    List<ValidationEventHandlerImpl.ValidationErrorObject> errors = validationEventHandlerImpl.getErrors();
                    for (ValidationEventHandlerImpl.ValidationErrorObject error : errors) {
                        String errorMessage = "Line Number : " + error.getLineNumber().toString() + " - " + error.getErrorMessage();
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, error.getValidationEventSeverity(), errorMessage);
                    }
                    throw new IMFException("AssetMap parsing failed with validation errors", imfErrorLogger);
                }
            }
        }
        catch(SAXException | JAXBException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    e.getMessage());
        }

        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("AssetMap parsing failed", imfErrorLogger);
        }

        switch(assetMapSchema.getAssetMapContext()) {
            case "org.smpte_ra.schemas.st0429_9_2007.AM":
                UUID uuid = null;
                org.smpte_ra.schemas.st0429_9_2007.AM.AssetMapType assetMapType = (org.smpte_ra.schemas.st0429_9_2007.AM.AssetMapType) assetMapTypeJAXBElement.getValue();

                imfErrorLogger.addAllErrors(checkConformance(assetMapType));

                uuid = UUIDHelper.fromUUIDAsURNStringToUUID(assetMapType.getId());
                this.uuid = uuid;
                for (org.smpte_ra.schemas.st0429_9_2007.AM.AssetType assetType : assetMapType.getAssetList().getAsset()) {
                    boolean isPackingList = (assetType.isPackingList() != null) ? assetType.isPackingList() : false;
                    String path = assetType.getChunkList().getChunk().get(0).getPath();
                    try
                    {
                        Asset asset = new Asset(assetType.getId(), isPackingList, path);
                        this.assetList.add(asset);
                        this.uuidToPath.put(asset.getUUID(), asset.getPath());
                        if ((assetType.isPackingList() != null) && (assetType.isPackingList())) {
                            this.packingListAssets.add(asset);
                        }
                    }
                    catch(URISyntaxException e)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                e.getMessage());
                    }
                }
                break;
            default:
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL,
                        String.format("Please check the AssetMap document, currently we only support " +
                                "the following schema URIs %s", serializeAssetMapSchemasToString()));
                throw new IMFException(String.format("Please check the AssetMap document, currently we only support " +
                        "the following schema URIs %s", serializeAssetMapSchemasToString()), imfErrorLogger);
        }

        if (imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException(String.format("Found %d errors in AssetMap XML file", imfErrorLogger
                    .getNumberOfErrors()), imfErrorLogger);
        }
    }

    /**
     * Getter for the errors in AssetMap
     *
     * @return List of errors in AssetMap.
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }


    private static final String serializeAssetMapSchemasToString(){
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iterator = supportedAssetMapSchemas.values().iterator();
        while(iterator.hasNext()){
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(((AssetMapSchema)iterator.next()).getAssetMapContext());
        }
        return stringBuilder.toString();
    }

    /**
     * A stateless method that verifies if the raw data represented by the ResourceByteRangeProvider corresponds to a valid
     * IMF AssetMap document
     * @param resourceByteRangeProvider - a byte range provider for the document that needs to be verified
     * @return - a boolean indicating if the document represented is an IMF AssetMap or not
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
            for(String supportedSchemaURI : supportedAssetMapSchemaURIs) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, "AssetMap");
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

    private static String getAssetMapNamespaceURI(ResourceByteRangeProvider resourceByteRangeProvider) throws
            IOException{

        String assetMapNamespaceURI = "";
        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            NodeList nodeList = null;
            for(String supportedSchemaURI : supportedAssetMapSchemaURIs) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, "AssetMap");
                if (nodeList != null
                        && nodeList.getLength() == 1)
                {
                    assetMapNamespaceURI = supportedSchemaURI;
                    break;
                }
            }
        }
        catch(ParserConfigurationException | SAXException e)
        {
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL,
                    String.format("Error occurred while trying to determine the AssetMap Namespace " +
                            "URI, invalid AssetMap document Error Message : %s", e.getMessage()));
            throw new IMFException(String.format("Error occurred while trying to determine the AssetMap Namespace " +
                    "URI, invalid AssetMap document Error Message : %s", e.getMessage()), imfErrorLogger);
        }
        if(assetMapNamespaceURI.isEmpty()) {
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL,
                    String.format("Please check the AssetMap document and namespace URI, currently we " +
                            "only support the following schema URIs %s", Utilities.serializeObjectCollectionToString
                            (supportedAssetMapSchemaURIs)));
            throw new IMFException(String.format("Please check the AssetMap document and namespace URI, currently we " +
                    "only support the following schema URIs %s", Utilities.serializeObjectCollectionToString
                    (supportedAssetMapSchemaURIs)), imfErrorLogger);
        }

        return assetMapNamespaceURI;
    }

    private static ResourceByteRangeProvider getFileAsResourceByteRangeProvider(File file)
    {
        return new FileByteRangeProvider(file);
    }

    static List<ErrorLogger.ErrorObject> checkConformance(AssetMapType assetMapType)
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        //per st0429-9:2014 Section 5.4, VolumeCount shall be one
        if (!assetMapType.getVolumeCount().equals(new BigInteger("1")))
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL, String.format("<VolumeCount> element = %d, is not equal to 1",
                    assetMapType.getVolumeCount()));
        }

        if(assetMapType.getAssetList() == null || assetMapType.getAssetList().getAsset().size() <= 0)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, String.format("No assets in AssetMap"));
        }
        else {
            for (AssetType assetType : assetMapType.getAssetList().getAsset()) {
                if(assetType.getChunkList() == null || assetType.getChunkList().getChunk().size() <= 0 )
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL, String.format("No chunks in Asset %s", assetType.getId()));
                }
                else {
                    //per st0429-9:2014 Section 6.4, <ChunkList> shall contain one <Chunk> element
                    if (assetType.getChunkList().getChunk().size() != 1) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                                .ErrorLevels.FATAL, String.format("<ChunkList> element contains %d <Chunk> elements, only 1 " +
                                "is allowed", assetType.getChunkList().getChunk().size()));
                    }

                    ChunkType chunkType = assetType.getChunkList().getChunk().get(0);

                    //per st0429-9:2014 Section 6.4, <VolumeIndex> shall be equal to 1 or absent
                    if ((chunkType.getVolumeIndex() != null) && !chunkType.getVolumeIndex().equals(new BigInteger("1"))) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                                .ErrorLevels.FATAL, String.format("<VolumeIndex> element = %d, only 1 is allowed", chunkType
                                .getVolumeIndex()));
                    }

                    //per st0429-9:2014 Section 6.4, <Offset> shall be equal to 0 or absent
                    if ((chunkType.getOffset() != null) && !chunkType.getOffset().equals(new BigInteger("0"))) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                                .ErrorLevels.FATAL, String.format("<Offset> element = %d, only 0 is allowed", chunkType
                                .getOffset()));
                    }
                }
            }
        }

        return imfErrorLogger.getErrors();
    }

    @Override
    public String toString()
    {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=================== AssetMap : %s%n", this.uuid));
        for (Asset asset : this.assetList)
        {
            sb.append(asset.toString());
        }
        return sb.toString();
    }

    /**
     * Getter for the complete list of assets present in this AssetMap
     * @return the list of assets present in this AssetMap
     */
    public List<Asset> getAssetList()
    {
        return Collections.unmodifiableList(this.assetList);
    }

    /**
     * Getter for the list of packing list (see st0429-8:2007) assets present in this AssetMap
     * @return the list of assets present in this AssetMap that are packing lists
     */
    public List<Asset> getPackingListAssets()
    {
        return Collections.unmodifiableList(this.packingListAssets);
    }

    /**
     *
     * @param uuid the unique identifier for the asset of interest
     * @return the unmodified path URI (as it occurs in the AssetMap XML document) corresponding to the asset of interest
     */
    public @Nullable URI getPath(UUID uuid)
    {
        return this.uuidToPath.get(uuid);
    }

    /**
     * Getter for the AssetMap's UUID
     * @return uuid corresponding to this AssetMap document
     */
    public UUID getUUID(){
        return this.uuid;
    }

    /**
     * This class represents a thin, immutable wrapper around the XML type 'AssetType' which is defined in Section 11,
     * st0429-9:2014. It exposes a minimal set of properties of the wrapped object through appropriate Getter methods
     */
    @Immutable
    public static final class Asset
    {
        private final UUID uuid;
        private final boolean isPackingList;
        private final URI path;

        /**
         * Constructor for the wrapping {@link com.netflix.imflibrary.st0429_9.AssetMap.Asset Asset} object from the wrapped model version of XML type 'AssetType'. Construction
         * fails in case the URI associated with wrapped object is invalid
         * @param uuid - the ID corresponding to an Asset in the AssetMap
         * @param isPackingList - a boolean flag to indicate if this Asset is a PackingList
         * @param path - URI associated with this asset
         * @throws URISyntaxException - exposes any issues with the URI associated with the wrapped object
         */
        public Asset(String uuid, boolean isPackingList, String path) throws URISyntaxException
        {
            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(uuid);
            this.isPackingList = isPackingList;
            String[] pathSegments = path.split("/");
            List<String> invalidPathSegments = new ArrayList<>();
            for(String pathSegment : pathSegments) {
                if (pathSegment.matches("^[a-zA-Z0-9._-]+") == false) {
                    invalidPathSegments.add(pathSegment);
                }
            }
            if(invalidPathSegments.size() > 0){
                throw new URISyntaxException(path,
                        String.format("The Asset path %s does not conform to the specified URI syntax in Annex-A of st429-9:2014 (a-z, A-Z, 0-9, ., _, -) for a path segment, the following path segments do not comply %s", path, Utilities.serializeObjectCollectionToString(invalidPathSegments)));
            }
            this.path = new URI(path);
        }

        /**
         * Getter for the UUID associated with the {@link com.netflix.imflibrary.st0429_9.AssetMap.Asset Asset} object
         * @return the asset UUID
         */
        public UUID getUUID()
        {
            return this.uuid;
        }

        /**
         * Tells if the {@link com.netflix.imflibrary.st0429_9.AssetMap.Asset Asset} object is a packing list
         * @return true if the object corresponds to a packing list, false otherwise
         */
        public boolean isPackingList()
        {
            return this.isPackingList;
        }

        /**
         * Getter for the path associated with the {@link com.netflix.imflibrary.st0429_9.AssetMap.Asset Asset} object. The
         * path is unmodified relative to what was present in the AssetMap XML document
         * @return the asset path as a URI
         */
        public URI getPath()
        {
            return this.path;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("================== Asset : %s%n", this.uuid));
            sb.append(String.format("isPackingList = %s%n", this.isPackingList()));
            sb.append(String.format("path = %s%n", this.path));
            return sb.toString();
        }

    }

    public static List<ErrorLogger.ErrorObject> validateAssetMapSchema(ResourceByteRangeProvider
                                                                               resourceByteRangeProvider) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        String assetMapSchemaURI = getAssetMapNamespaceURI(resourceByteRangeProvider);
        AssetMapSchema assetMapSchema = supportedAssetMapSchemas.get(assetMapSchemaURI);
        if(assetMapSchema == null){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL,
                    String.format("Please check the AssetMap document, currently we only support the " +
                            "following schema URIs %s", serializeAssetMapSchemasToString()));
            return imfErrorLogger.getErrors();
        }

        try {
            try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
                 InputStream assetMap_is = Thread.currentThread().getContextClassLoader().getResourceAsStream(assetMapSchema.getAssetMapSchemaPath());
            ) {
                StreamSource inputSource = new StreamSource(inputStream);


                StreamSource[] streamSources = new StreamSource[1];
                streamSources[0] = new StreamSource(assetMap_is);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(streamSources);

                Validator validator = schema.newValidator();
                validator.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, exception.getMessage()));
                    }

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, exception.getMessage()));
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, exception.getMessage()));
                    }
                });
                validator.validate(inputSource);
            }
        }
        catch(SAXException e)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, e.getMessage());
        }

        return imfErrorLogger.getErrors();
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath>%n", AssetMap.class.getName()));
        return sb.toString();
    }

    public static void main(String args[]) throws IOException, URISyntaxException, SAXException, JAXBException
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
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateAssetMap(payloadRecord);

        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("AssetMap Document has %d errors and %d warnings",
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
            logger.info("No errors were detected in the AssetMap Document");
        }

    }
}
