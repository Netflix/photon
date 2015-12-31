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

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st0429_8_2007.PKL.AssetType;
import org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

public final class PackingList
{
    private static final Logger logger = LoggerFactory.getLogger(PackingList.class);

    private static final String pkl_schema_path = "/org/smpte_ra/schemas/st0429_8_2007/PKL/packingList_schema.xsd";
    private static final String xmldig_core_schema_path = "/org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";

    private final PackingListType packingListType;
    private final UUID uuid;
    private final List<Asset> assetList = new ArrayList<>();

    public PackingList(File packingListXML) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {

        PackingList.validatePackingListSchema(packingListXML);

        try(InputStream input = new FileInputStream(packingListXML);
            InputStream xmldig_core_is = PackingList.class.getResourceAsStream(PackingList.xmldig_core_schema_path);
            InputStream pkl_is = PackingList.class.getResourceAsStream(PackingList.pkl_schema_path);
        )
        {
            StreamSource[] streamSources = new StreamSource[2];
            streamSources[0] = new StreamSource(xmldig_core_is);
            streamSources[1] = new StreamSource(pkl_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st0429_8_2007.PKL");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<PackingListType> packingListTypeJAXBElement = (JAXBElement)unmarshaller.unmarshal(input);
            if(validationEventHandlerImpl.hasErrors())
            {
                throw new IMFException(validationEventHandlerImpl.toString());
            }

            this.packingListType  = PackingList.checkConformance(packingListTypeJAXBElement.getValue());

            this.uuid = UUID.fromString(this.packingListType.getId().split("urn:uuid:")[1]);

            for (AssetType assetType : this.packingListType.getAssetList().getAsset())
            {
                Asset asset = new Asset(UUID.fromString(assetType.getId().split("urn:uuid:")[1]), assetType.getHash(),
                        assetType.getSize().longValue(), assetType.getType(), assetType.getOriginalFileName().getValue());
                this.assetList.add(asset);
            }
        }

    }

    private static PackingListType checkConformance(PackingListType packingListType)
    {
        return packingListType;
    }

    public List<Asset> getAssets()
    {
        return Collections.unmodifiableList(this.assetList);
    }

    public UUID getUuid()
    {
        return this.uuid;
    }

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


    public static final class Asset
    {
        private final UUID uuid;
        private final byte[] hash;
        private final long size;
        private final String type;
        private final String original_filename;

        public Asset(UUID uuid, byte[] hash, long size, String type, String original_filename)
        {
            this.uuid = uuid;
            this.hash = Arrays.copyOf(hash, hash.length);
            this.size = size;
            this.type = type;
            this.original_filename = original_filename;
        }

        public UUID getUuid()
        {
            return this.uuid;
        }

        public long getSize()
        {
            return this.size;
        }

        public String getType()
        {
            return this.type;
        }

        public @Nullable String getOriginalFilename()
        {
            return this.original_filename;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("=================== Asset : %s%n", this.uuid));
            sb.append(String.format("hash = %s%n", Arrays.toString(this.hash)));
            sb.append(String.format("size = %d%n", this.size));
            sb.append(String.format("type = %s%n", this.type));
            sb.append(String.format("original_filename = %s%n", this.original_filename));
            return sb.toString();
        }

    }


    public static void validatePackingListSchema(File xmlFile) throws IOException, URISyntaxException, SAXException
    {


        try(InputStream input = new FileInputStream(xmlFile);
            InputStream xmldig_core_is = PackingList.class.getResourceAsStream(PackingList.xmldig_core_schema_path);
            InputStream pkl_is = PackingList.class.getResourceAsStream(PackingList.pkl_schema_path);
        )
        {
            StreamSource inputSource = new StreamSource(input);

            StreamSource[] streamSources = new StreamSource[2];
            streamSources[0] = new StreamSource(xmldig_core_is);
            streamSources[1] = new StreamSource(pkl_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            Validator validator = schema.newValidator();
            validator.validate(inputSource);
        }

    }

    public static void main(String args[]) throws IOException, URISyntaxException, SAXException, ParserConfigurationException, JAXBException
    {
        File inputFile = new File(args[0]);

        PackingList.validatePackingListSchema(inputFile);

        PackingList packingList = new PackingList(inputFile);
        logger.warn(packingList.toString());

    }

}
