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

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetMapType;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetType;
import org.smpte_ra.schemas.st0429_9_2007.AM.ChunkType;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public final class AssetMap
{
    private static final String assetMap_schema_path = "/org/smpte_ra/schemas/st0429_9_2007/AM/assetMap_schema.xsd";

    private final UUID uuid;
    private final List<Asset> assetList = new ArrayList<>();
    private final List<Asset> packingListAssets = new ArrayList<>();
    private Map<UUID, URI> uuidToPath = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AssetMap.class);
    private final AssetMapType assetMapType;


    public AssetMap(File assetMapXmlFile) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {

        AssetMap.validateAssetMapSchema(assetMapXmlFile);

        try(InputStream input = new FileInputStream(assetMapXmlFile);
            InputStream assetMap_schema_is = AssetMap.class.getResourceAsStream(AssetMap.assetMap_schema_path);
        )
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
            StreamSource schemaSource = new StreamSource(assetMap_schema_is);
            Schema schema = schemaFactory.newSchema(schemaSource);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st0429_9_2007.AM");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<AssetMapType> assetMapTypeJAXBElement = (JAXBElement)unmarshaller.unmarshal(input);
            if(validationEventHandlerImpl.hasErrors())
            {
                throw new IMFException(validationEventHandlerImpl.toString());
            }

            this.assetMapType  = AssetMap.checkConformance(assetMapTypeJAXBElement.getValue());
        }

        this.uuid = UUID.fromString(this.assetMapType.getId().split("urn:uuid:")[1]);

        for (AssetType assetType : this.assetMapType.getAssetList().getAsset())
        {

            Asset asset = new Asset(UUID.fromString(assetType.getId().split("urn:uuid:")[1]),
                    assetType.isPackingList(), new URI(assetType.getChunkList().getChunk().get(0).getPath()));
            this.assetList.add(asset);
            this.uuidToPath.put(asset.getUuid(), asset.getPath());
            if (assetType.isPackingList())
            {
                this.packingListAssets.add(asset);
            }
        }

    }

    private static AssetMapType checkConformance(AssetMapType assetMapType)
    {
        //per st0429-9:2014 Section 5.4, VolumeCount shall be one
        if (!assetMapType.getVolumeCount().equals(new BigInteger("1")))
        {
            throw new IMFException("");
        }

        for (AssetType assetType : assetMapType.getAssetList().getAsset())
        {
            //per st0429-9:2014 Section 6.4, <ChunkList> shall contain one <Chunk> element
            if (assetType.getChunkList().getChunk().size() != 1)
            {
                throw new IMFException("");
            }

            ChunkType chunkType = assetType.getChunkList().getChunk().get(0);

            //per st0429-9:2014 Section 6.4, <VolumeIndex> shall be equal to 1 or absent
            if ((chunkType.getVolumeIndex() != null) && !chunkType.getVolumeIndex().equals(new BigInteger("1")))
            {
                throw new IMFException("");
            }

            //per st0429-9:2014 Section 6.4, <Offset> shall be equal to 1 or absent
            if ((chunkType.getOffset() != null) && !chunkType.getOffset().equals(new BigInteger("0")))
            {
                throw new IMFException("");
            }
        }

        return assetMapType;
    }

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

    public List<Asset> getAssetList()
    {
        return Collections.unmodifiableList(this.assetList);
    }

    public List<Asset> getPackingListAssets()
    {
        return Collections.unmodifiableList(this.packingListAssets);
    }

    public @Nullable URI getPath(UUID uuid)
    {
        return this.uuidToPath.get(uuid);
    }

    public static final class Asset
    {
        private final UUID uuid;
        private final boolean isPackingList;
        private final URI path;

        public Asset(UUID uuid, boolean isPackingList, URI path)
        {
            this.uuid = uuid;
            this.isPackingList = isPackingList;
            this.path = path;
        }

        public UUID getUuid()
        {
            return this.uuid;
        }

        public boolean isPackingList()
        {
            return this.isPackingList;
        }

        public URI getPath()
        {
            return this.path;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("================== Asset : %s%n", this.uuid));
            sb.append(String.format("isPackingList = %s%n", this.isPackingList()));
            sb.append(String.format("path = %s%n", this.path));
            return sb.toString();
        }

    }

    public static void validateAssetMapSchema(File xmlFile) throws IOException, URISyntaxException, SAXException
    {
        InputStream input = null;
        InputStream assetMap_is = null;

        try
        {
            input = new FileInputStream(xmlFile);
            StreamSource inputSource = new StreamSource(input);

            assetMap_is = AssetMap.class.getResourceAsStream(AssetMap.assetMap_schema_path);
            StreamSource[] streamSources = new StreamSource[1];
            streamSources[0] = new StreamSource(assetMap_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            Validator validator = schema.newValidator();
            validator.validate(inputSource);
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }

            if (assetMap_is != null)
            {
                assetMap_is.close();
            }
        }
    }

    public static void main(String args[]) throws IOException, URISyntaxException, SAXException, ParserConfigurationException, JAXBException
    {
        File inputFile = new File(args[0]);

        AssetMap.validateAssetMapSchema(inputFile);

        AssetMap assetMap = new AssetMap(inputFile);
        logger.warn(assetMap.toString());

    }
}
