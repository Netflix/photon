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
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.RepeatableInputStream;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetMapType;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetType;
import org.smpte_ra.schemas.st0429_9_2007.AM.ChunkType;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private static final String assetMap_schema_path = "/org/smpte_ra/schemas/st0429_9_2007/AM/assetMap_schema.xsd";

    private final UUID uuid;
    private final List<Asset> assetList = new ArrayList<>();
    private final List<Asset> packingListAssets = new ArrayList<>();
    private final Map<UUID, URI> uuidToPath = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AssetMap.class);
    private final AssetMapType assetMapType;
    public static final List<String> supportedAssetMapSchemaURIs = Collections.unmodifiableList(new ArrayList<String>(){{ add("http://www.smpte-ra.org/schemas/429-9/2007/AM");}});

    /**
     * Constructor for an {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object from an XML file that contains an AssetMap document
     * @param assetMapXmlFile the input XML file
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public AssetMap(File assetMapXmlFile, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException, SAXException, JAXBException, URISyntaxException
    {
        this(getFileAsResourceByteRangeProvider(assetMapXmlFile), imfErrorLogger);
    }

    /**
     * Constructor for an {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object from an XML file that contains an AssetMap document
     * @param resourceByteRangeProvider that supports the mark() (mark position should be set to point to the beginning of the file) and reset() methods corresponding to the input XML file
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public AssetMap(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException, SAXException, JAXBException, URISyntaxException
    {

        int numErrors = (imfErrorLogger != null) ? imfErrorLogger.getNumberOfErrors() : 0;
        validateAssetMapSchema(resourceByteRangeProvider, imfErrorLogger);

        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);
            InputStream assetMap_schema_is = AssetMap.class.getResourceAsStream(AssetMap.assetMap_schema_path);)
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
            StreamSource schemaSource = new StreamSource(assetMap_schema_is);
            Schema schema = schemaFactory.newSchema(schemaSource);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st0429_9_2007.AM");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<AssetMapType> assetMapTypeJAXBElement = (JAXBElement)unmarshaller.unmarshal(inputStream);
            if(validationEventHandlerImpl.hasErrors())
            {
                List<ValidationEventHandlerImpl.ValidationErrorObject> errors = validationEventHandlerImpl.getErrors();
                for(ValidationEventHandlerImpl.ValidationErrorObject error : errors){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, error.getValidationEventSeverity(), error.getErrorMessage());
                }
                throw new IMFException(validationEventHandlerImpl.toString());
            }

            this.assetMapType  = AssetMap.checkConformance(assetMapTypeJAXBElement.getValue(), imfErrorLogger);
        }

        UUID uuid = null;
        try
        {
            uuid = UUIDHelper.fromUUIDAsURNStringToUUID(this.assetMapType.getId());
        }
        catch(IMFException e)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    e.getMessage());

            throw e;
        }
        this.uuid = uuid;

        for (AssetType assetType : this.assetMapType.getAssetList().getAsset())
        {
            Asset asset = new Asset(assetType);
            this.assetList.add(asset);
            this.uuidToPath.put(asset.getUUID(), asset.getPath());
            if ((assetType.isPackingList() != null) && (assetType.isPackingList()))
            {
                this.packingListAssets.add(asset);
            }
        }

        if (imfErrorLogger.getNumberOfErrors() > numErrors)
        {
            throw new IMFException(String.format("Found %d errors in AssetMap XML file", imfErrorLogger.getNumberOfErrors() - numErrors));
        }

    }

    private static ResourceByteRangeProvider getFileAsResourceByteRangeProvider(File file)
    {
        return new FileByteRangeProvider(file);
    }

    static AssetMapType checkConformance(AssetMapType assetMapType, @Nullable IMFErrorLogger imfErrorLogger)
    {
        //per st0429-9:2014 Section 5.4, VolumeCount shall be one
        if (!assetMapType.getVolumeCount().equals(new BigInteger("1")))
        {
            String message = String.format("<VolumeCount> element = %d, is not equal to 1", assetMapType.getVolumeCount());
            if (imfErrorLogger != null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
            }
            else
            {
                throw new IMFException(message);
            }
        }

        for (AssetType assetType : assetMapType.getAssetList().getAsset())
        {
            //per st0429-9:2014 Section 6.4, <ChunkList> shall contain one <Chunk> element
            if (assetType.getChunkList().getChunk().size() != 1)
            {
                String message = String.format("<ChunkList> element contains %d <Chunk> elements, only 1 is allowed", assetType.getChunkList().getChunk().size());
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }

            }

            ChunkType chunkType = assetType.getChunkList().getChunk().get(0);

            //per st0429-9:2014 Section 6.4, <VolumeIndex> shall be equal to 1 or absent
            if ((chunkType.getVolumeIndex() != null) && !chunkType.getVolumeIndex().equals(new BigInteger("1")))
            {
                String message = String.format("<VolumeIndex> element = %d, only 1 is allowed", chunkType.getVolumeIndex());
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }
            }

            //per st0429-9:2014 Section 6.4, <Offset> shall be equal to 0 or absent
            if ((chunkType.getOffset() != null) && !chunkType.getOffset().equals(new BigInteger("0")))
            {
                String message = String.format("<Offset> element = %d, only 0 is allowed", chunkType.getOffset());
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }
            }
        }

        return assetMapType;
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
         * @param assetType the wrapped object
         * @throws URISyntaxException - exposes any issues with the URI associated with the wrapped object
         */
        public Asset(AssetType assetType) throws URISyntaxException
        {
            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(assetType.getId());
            this.isPackingList = (assetType.isPackingList() != null) ? assetType.isPackingList() : false;
            String path = assetType.getChunkList().getChunk().get(0).getPath();
            if(path.matches("^[a-zA-Z0-9._-]+") == true) {
                this.path = new URI(assetType.getChunkList().getChunk().get(0).getPath());
            }
            else{
                throw new URISyntaxException(path, String.format("The Asset path %s does not conform to the specified URI syntax in Annex-A of st429-9:2014 (a-z, A-Z, 0-9, ., _, -)", path));
            }
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

    private void validateAssetMapSchema(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException, SAXException {

        InputStream assetMap_is = null;
        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
        {
            StreamSource inputSource = new StreamSource(inputStream);

            assetMap_is = AssetMap.class.getResourceAsStream(AssetMap.assetMap_schema_path);
            StreamSource[] streamSources = new StreamSource[1];
            streamSources[0] = new StreamSource(assetMap_is);

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
        finally
        {
            if (assetMap_is != null)
            {
                assetMap_is.close();
            }
        }
    }

    public static void main(String args[]) throws IOException, URISyntaxException, SAXException, JAXBException
    {
        File inputFile = new File(args[0]);

        AssetMap assetMap = new AssetMap(inputFile, new IMFErrorLoggerImpl());
        logger.warn(assetMap.toString());

    }
}
