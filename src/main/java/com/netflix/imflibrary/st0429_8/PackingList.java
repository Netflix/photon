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
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st0429_8_2007.PKL.AssetType;
import org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
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

/**
 * This class represents a thin, immutable wrapper around the XML type 'PackingListType' which is defined in Section 7,
 * st0429-8:2007. A PackingList object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st0429-8:2007
 */
@Immutable
public final class PackingList
{
    private static final Logger logger = LoggerFactory.getLogger(PackingList.class);

    private static final String pkl_schema_path = "org/smpte_ra/schemas/st0429_8_2007/PKL/packingList_schema.xsd";
    private static final String xmldsig_core_schema_path = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    public static final List<String> supportedPKLSchemaURIs = Collections.unmodifiableList(new ArrayList<String>(){{ add("http://www.smpte-ra.org/schemas/429-8/2007/PKL");}});

    private final PackingListType packingListType;
    private final UUID uuid;
    private final List<Asset> assetList = new ArrayList<>();

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object that corresponds to a PackingList XML document
     * @param packingListXMLFile the input XML file
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     */
    public PackingList(File packingListXMLFile) throws IOException, SAXException, JAXBException {
        PackingList.validatePackingListSchema(packingListXMLFile);

        try(InputStream input = new FileInputStream(packingListXMLFile);
            InputStream xmldsig_core_is = ClassLoader.getSystemResourceAsStream(PackingList.xmldsig_core_schema_path);
            InputStream pkl_is = ClassLoader.getSystemResourceAsStream(PackingList.pkl_schema_path);
        )
        {
            StreamSource[] streamSources = new StreamSource[2];
            streamSources[0] = new StreamSource(xmldsig_core_is);
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

            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(this.packingListType.getId());

            for (AssetType assetType : this.packingListType.getAssetList().getAsset())
            {
                Asset asset = new Asset(assetType);
                this.assetList.add(asset);
            }
        }
    }

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object that corresponds to a PackingList XML document
     * @param inputStream corresponding to the the input XML file
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     */
    public PackingList(InputStream inputStream)throws IOException, SAXException, JAXBException {
        PackingList.validatePackingListSchema(inputStream);

        try(InputStream xmldsig_core_is = ClassLoader.getSystemResourceAsStream(PackingList.xmldsig_core_schema_path);
            InputStream pkl_is = ClassLoader.getSystemResourceAsStream(PackingList.pkl_schema_path);
        )
        {
            StreamSource[] streamSources = new StreamSource[2];
            streamSources[0] = new StreamSource(xmldsig_core_is);
            streamSources[1] = new StreamSource(pkl_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st0429_8_2007.PKL");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<PackingListType> packingListTypeJAXBElement = (JAXBElement)unmarshaller.unmarshal(inputStream);
            if(validationEventHandlerImpl.hasErrors())
            {
                throw new IMFException(validationEventHandlerImpl.toString());
            }

            this.packingListType  = PackingList.checkConformance(packingListTypeJAXBElement.getValue());

            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(this.packingListType.getId());

            for (AssetType assetType : this.packingListType.getAssetList().getAsset())
            {
                Asset asset = new Asset(assetType);
                this.assetList.add(asset);
            }
        }

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
         * @param assetType the wrapped object
         */
        public Asset(AssetType assetType)
        {
            this.uuid = UUIDHelper.fromUUIDAsURNStringToUUID(assetType.getId());
            this.hash = Arrays.copyOf(assetType.getHash(), assetType.getHash().length);
            this.size = assetType.getSize().longValue();
            this.type = assetType.getType();
            this.original_filename = assetType.getOriginalFileName().getValue();
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


    private static void validatePackingListSchema(File xmlFile) throws IOException, SAXException {
        InputStream inputStream = new FileInputStream(xmlFile);
        validatePackingListSchema(inputStream);
        inputStream.close();
    }

    private static void validatePackingListSchema(InputStream inputStream) throws IOException, SAXException {
        try(InputStream xmldsig_core_is = ClassLoader.getSystemResourceAsStream(PackingList.xmldsig_core_schema_path);
            InputStream pkl_is = ClassLoader.getSystemResourceAsStream(PackingList.pkl_schema_path);
        )
        {
            StreamSource inputSource = new StreamSource(inputStream);

            StreamSource[] streamSources = new StreamSource[2];
            streamSources[0] = new StreamSource(xmldsig_core_is);
            streamSources[1] = new StreamSource(pkl_is);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            Validator validator = schema.newValidator();
            validator.validate(inputSource);
        }
    }

    public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException, JAXBException
    {
        File inputFile = new File(args[0]);

        PackingList.validatePackingListSchema(inputFile);

        PackingList packingList = new PackingList(inputFile);
        logger.warn(packingList.toString());

    }

}
