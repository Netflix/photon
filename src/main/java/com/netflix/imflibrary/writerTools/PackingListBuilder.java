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

package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFAuthoringException;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A class that implements the logic to build a SMPTE st0429-8:2007 or st2067-2:2016 schema compliant PackingList document.
 */
public class PackingListBuilder {

    private final UUID uuid;
    private final XMLGregorianCalendar issueDate;
    private final String iconId;
    private final String groupId;
    private final File workingDirectory;
    private final IMFErrorLogger imfErrorLogger;
    public final static String defaultHashAlgorithm = "http://www.w3.org/2000/09/xmldsig#sha1";
    private final String pklFileName;

    /**
     * A constructor for the PackingListBuilder object
     * @param uuid that uniquely identifies the PackingList document
     * @param issueDate date at which the PackingList was issued
     * @param workingDirectory a folder location where the generated PackingListDocument will be written to
     * @param imfErrorLogger a logger object to record errors that occur during the creation of the PackingList document
     */
    public PackingListBuilder(@Nonnull UUID uuid,
                              @Nonnull XMLGregorianCalendar issueDate,
                              @Nonnull File workingDirectory,
                              @Nonnull IMFErrorLogger imfErrorLogger){
        this.uuid = uuid;
        this.issueDate = issueDate;
        this.iconId = null;
        this.groupId = null;
        this.workingDirectory = workingDirectory;
        this.imfErrorLogger = imfErrorLogger;
        this.pklFileName = "PKL-" + this.uuid.toString() + ".xml";
    }

    /**
     * A constructor for the PackingListBuilder object
     * @param uuid that uniquely identifies the PackingList document
     * @param issueDate date at which the PackingList was issued
     * @param iconId a urn:uuid: that identifies an external image resource containing a picture illustrating the PackingList
     * @param groupId a urn:uuid: that is used to create associations between packages
     * @param workingDirectory a folder location where the generated PackingListDocument will be written to
     * @param imfErrorLogger a logger object to record errors that occur during the creation of the PackingList document
     */
    public PackingListBuilder(@Nonnull UUID uuid,
                              @Nonnull XMLGregorianCalendar issueDate,
                              @Nonnull String iconId,
                              @Nonnull String groupId,
                              @Nonnull File workingDirectory,
                              @Nonnull IMFErrorLogger imfErrorLogger){
        this.uuid = uuid;
        this.issueDate = issueDate;
        this.iconId = iconId;
        this.groupId = groupId;
        this.workingDirectory = workingDirectory;
        this.imfErrorLogger = imfErrorLogger;
        this.pklFileName = "PKL-" + this.uuid.toString() + ".xml";
    }

    /**
     * A method to construct a UserTextType compliant with the 2007 schema for IMF PackingList documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType
     */
    public static org.smpte_ra.schemas._429_8._2007.pkl.UserText buildPKLUserTextType_2007(String value, String language){
        org.smpte_ra.schemas._429_8._2007.pkl.UserText userText = new org.smpte_ra.schemas._429_8._2007.pkl.UserText();
        userText.setValue(value);
        userText.setLanguage(language);
        return userText;
    }

    /**
     * A method to construct a UserTextType compliant with the 2016 schema for IMF PackingList documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType
     */
    public static org.smpte_ra.schemas._2067_2._2016.pkl.UserText buildPKLUserTextType_2016(String value, String language){
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText userText = new org.smpte_ra.schemas._2067_2._2016.pkl.UserText();
        userText.setValue(value);
        userText.setLanguage(language);
        return userText;
    }

    /**
     * A method to construct a Default Digest Method Type with a default HashAlgorithm
     * @return a DigestMethodType object conforming to the 2016 schema with the default HashAlgorithm
     */
    public org.w3._2000._09.xmldsig_.DigestMethodType buildDefaultDigestMethodType(){
        org.w3._2000._09.xmldsig_.DigestMethodType digestMethodType = new org.w3._2000._09.xmldsig_.DigestMethodType();
        digestMethodType.setAlgorithm(PackingListBuilder.defaultHashAlgorithm);
        return digestMethodType;
    }

    /**
     * A method to construct a Digest Method Type with the HashAlgorithm string that was passed in
     * @param algorithm a String representing the algorithm used for generating the Hash
     * @return a DigestMethodType object conforming to the 2016 schema with the default HashAlgorithm
     */
    public org.w3._2000._09.xmldsig_.DigestMethodType buildDigestMethodType(String algorithm){
        org.w3._2000._09.xmldsig_.DigestMethodType digestMethodType = new org.w3._2000._09.xmldsig_.DigestMethodType();
        digestMethodType.setAlgorithm(algorithm);
        return digestMethodType;
    }

    /**
     * A method to build a PackingList document compliant with the st0429-8:2007 schema
     * @param annotationText a free form human readable text
     * @param issuer a free form human readable text describing the issuer of the PackingList document
     * @param creator a free form human readable text describing the tool used to create the AssetMap document
     * @param assets a list of PackingListBuilder assets roughly modeling the PackingList Asset compliant
     *               with the st0429-8:2007 schema
     * @return a list of errors that occurred while generating the PackingList document compliant with the st0429-8:2007 schema
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public List<ErrorLogger.ErrorObject> buildPackingList_2007(@Nonnull org.smpte_ra.schemas._429_8._2007.pkl.UserText annotationText,
                                                               @Nonnull org.smpte_ra.schemas._429_8._2007.pkl.UserText issuer,
                                                               @Nonnull org.smpte_ra.schemas._429_8._2007.pkl.UserText creator,
                                                               @Nonnull List<PackingListBuilderAsset_2007> assets)
            throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        org.smpte_ra.schemas._429_8._2007.pkl.PackingListType packingListType = IMFPKLObjectFieldsFactory.constructPackingListType_2007();
        packingListType.setId(UUIDHelper.fromUUID(this.uuid));
        packingListType.setAnnotationText(annotationText);
        packingListType.setIconId(this.iconId);
        packingListType.setIssueDate(this.issueDate);
        packingListType.setIssuer(issuer);
        packingListType.setCreator(creator);
        packingListType.setGroupId(this.groupId);
        org.smpte_ra.schemas._429_8._2007.pkl.PackingListType.AssetList assetList = new org.smpte_ra.schemas._429_8._2007.pkl.PackingListType.AssetList();
        List<org.smpte_ra.schemas._429_8._2007.pkl.AssetType> packingListAssets = assetList.getAsset();
        for(PackingListBuilderAsset_2007 asset : assets){
            org.smpte_ra.schemas._429_8._2007.pkl.AssetType packingListAssetType = new org.smpte_ra.schemas._429_8._2007.pkl.AssetType();
            packingListAssetType.setId(asset.getUUID());
            packingListAssetType.setAnnotationText(asset.getAnnotationText());
            packingListAssetType.setHash(asset.getHash());
            packingListAssetType.setSize(asset.getSize());
            packingListAssetType.setType(asset.getAssetType().toString());
            packingListAssetType.setOriginalFileName(asset.getOriginalFileName());
            packingListAssets.add(packingListAssetType);
        }
        packingListType.setAssetList(assetList);
        //The following attributes are optional setting them to null so that the JAXB Marshaller will not marshall them
        packingListType.setSigner(null);
        packingListType.setSignature(null);


        File outputFile = new File(this.workingDirectory + File.separator + this.pklFileName);
        boolean formatted = true;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try(
                InputStream packingListSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st0429_8_2007/PKL/packingList_schema.xsd");
                InputStream dsigSchemaAsAStream = contextClassLoader.getResourceAsStream("org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd");
                OutputStream outputStream = new FileOutputStream(outputFile);
        )
        {
            try
            {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StreamSource[] schemaSources = new StreamSource[2];
                //The order in which these schema sources are initialized is important because some elements in the
                //PackingList schema depend on types defined in the DSig schema.
                schemaSources[0] = new StreamSource(dsigSchemaAsAStream);
                schemaSources[1] = new StreamSource(packingListSchemaAsAStream);
                Schema schema = schemaFactory.newSchema(schemaSources);

                JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas._429_8._2007.pkl");
                Marshaller marshaller = jaxbContext.createMarshaller();
                ValidationEventHandlerImpl validationEventHandler = new ValidationEventHandlerImpl(true);
                marshaller.setEventHandler(validationEventHandler);
                marshaller.setSchema(schema);
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

                /*marshaller.marshal(cplType, output);
                workaround for 'Error: unable to marshal type "AssetMapType" as an element because it is missing an @XmlRootElement annotation'
                as found at https://weblogs.java.net/blog/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always
                 */
                marshaller.marshal(new JAXBElement<>(new QName("http://www.smpte-ra.org/schemas/429-8/2007/PKL", "PackingList"), org.smpte_ra.schemas._429_8._2007.pkl.PackingListType.class, packingListType), outputStream);
                outputStream.close();

                if (validationEventHandler.hasErrors()) {
                    //TODO : Perhaps a candidate for a Lambda
                    for (ValidationEventHandlerImpl.ValidationErrorObject validationErrorObject : validationEventHandler.getErrors()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, validationErrorObject.getValidationEventSeverity(), validationErrorObject.getErrorMessage());
                    }
                }
            }
            catch( SAXException | JAXBException e)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                                .ErrorLevels.FATAL,
                        e.getMessage());
            }
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A class that roughly models a Packing List Asset conformant to the st0429-8:2007 schema
     */
    public static class PackingListBuilderAsset_2007 {
        private final String uuid;
        private final org.smpte_ra.schemas._429_8._2007.pkl.UserText annotationText;
        private final byte[] hash;
        private final BigInteger size;
        private final PKLAssetTypeEnum assetType;
        private final org.smpte_ra.schemas._429_8._2007.pkl.UserText originalFileName;

        /**
         * A constructor for a PackingListAsset that roughly models a Packing List Asset conformant to the st0429-8:2007 schema
         * @param uuid that uniquely identifies this asset in the Packing List
         * @param annotationText a free form human readable text
         * @param hash a byte[] containing the Base64 encoded SHA-1 hash of this PackingList Asset
         * @param size of the asset in bytes
         * @param assetType could be either text/xml or application/mxf as defined in st0429-8:2007
         * @param originalFileName a free form human readable text that contains the name of the file
         *                         containing the asset at the time the PackingList was created
         */
        public PackingListBuilderAsset_2007(@Nonnull UUID uuid,
                                            @Nonnull org.smpte_ra.schemas._429_8._2007.pkl.UserText annotationText,
                                            @Nonnull byte[] hash,
                                            @Nonnull Long size,
                                            @Nonnull PKLAssetTypeEnum assetType,
                                            org.smpte_ra.schemas._429_8._2007.pkl.UserText originalFileName){
            this.uuid = UUIDHelper.fromUUID(uuid);
            this.annotationText = annotationText;
            this.hash = Arrays.copyOf(hash, hash.length);
            this.size = BigInteger.valueOf(size);
            this.assetType = assetType;
            this.originalFileName = originalFileName;
        }

        /**
         * Getter for the UUID identifying this Packing List Asset
         * @return a String representing the "urn:uuid:" of this PackingList Asset
         */
        public String getUUID(){
            return this.uuid;
        }

        /**
         * Getter for the AnnotationText of this Packing List Asset
         * @return a UserText representing the Annotation Text
         */
        public org.smpte_ra.schemas._429_8._2007.pkl.UserText getAnnotationText(){
            return this.annotationText;
        }

        /**
         * Getter for the Hash of this Packing List Asset
         * @return a byte[] representing the Base64 encoded SHA-1 Hash of the Asset
         */
        public byte[] getHash(){
            return Arrays.copyOf(this.hash, this.hash.length);
        }

        /**
         * Getter for the size of this Packing List Asset
         * @return a BigInteger representing the size of this PackingList Asset
         */
        public BigInteger getSize(){
            return this.size;
        }

        /**
         * Getter for the type of this PackingList Asset
         * @return an enumerated value representing the type of this PackingList Asset
         */
        public PKLAssetTypeEnum getAssetType(){
            return this.assetType;
        }

        /**
         * Getter for the OriginalFileName of this PackingList Asset
         * @return a UserText representing the OriginalFileName of the PackingList Asset
         *          at the time that the PackingList was created
         */
        public org.smpte_ra.schemas._429_8._2007.pkl.UserText getOriginalFileName(){
            return this.originalFileName;
        }
    }

    /**
     * Getter for the errors in PackingListBuilder
     *
     * @return List of errors in PackingListBuilder.
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }

    /**
     * Getter for the PKL file name for the PackingList
     *
     * @return PKL file name for the PackingList.
     */
    public String getPKLFileName() {
        return this.pklFileName;
    }

    public static enum PKLAssetTypeEnum {
        TEXT_XML("text/xml"),
        APP_MXF("application/mxf"),
        UNDEFINED("Undefined");

        private final String assetType;

        /**
         * To prevent instantiation
         * @param pklAssetType a string representing the PKL AssetType
         */
        private PKLAssetTypeEnum(String pklAssetType){
            this.assetType = pklAssetType;
        }

        /**
         * Getter for the AssetType of the PKLAssetTypeEnum
         * @return a string indicating the asset type represented by this Enumeration
         */
        public String getAssetType(){
            return this.assetType;
        }

        public static PKLAssetTypeEnum getAssetTypeEnum(String assetType){
            switch (assetType){
                case "text/xml":
                    return PKLAssetTypeEnum.TEXT_XML;
                case "application/mxf":
                    return PKLAssetTypeEnum.APP_MXF;
                default:
                    return PKLAssetTypeEnum.UNDEFINED;
            }
        }

        /**
         * The overridden toString() method
         * @return a string representing this PKLAssetType enumeration
         */
        public String toString(){
            return this.assetType;
        }
    }

    /**
     * A method to build a PackingList document compliant with the st0429-8:2007 schema
     * @param annotationText a free form human readable text
     * @param issuer a free form human readable text describing the issuer of the PackingList document
     * @param creator a free form human readable text describing the tool used to create the AssetMap document
     * @param assets a list of PackingListBuilder assets roughly modeling the PackingList Asset compliant
     *               with the st0429-8:2007 schema
     * @return a list of errors that occurred while generating the PackingList document compliant with the st0429-8:2007 schema
     * @throws IOException - any I/O related error will be exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     */
    public List<ErrorLogger.ErrorObject> buildPackingList_2016(@Nonnull org.smpte_ra.schemas._2067_2._2016.pkl.UserText annotationText,
                                                         @Nonnull org.smpte_ra.schemas._2067_2._2016.pkl.UserText issuer,
                                                         @Nonnull org.smpte_ra.schemas._2067_2._2016.pkl.UserText creator,
                                                         @Nonnull List<PackingListBuilderAsset_2016> assets) throws IOException, SAXException, JAXBException {

        int numErrors = imfErrorLogger.getNumberOfErrors();
        org.smpte_ra.schemas._2067_2._2016.pkl.PackingListType packingListType = IMFPKLObjectFieldsFactory.constructPackingListType_2016();
        packingListType.setId(UUIDHelper.fromUUID(this.uuid));
        packingListType.setIconId(this.iconId);
        packingListType.setAnnotationText(annotationText);
        packingListType.setIssueDate(this.issueDate);
        packingListType.setIssuer(issuer);
        packingListType.setCreator(creator);
        packingListType.setGroupId(this.groupId);
        org.smpte_ra.schemas._2067_2._2016.pkl.PackingListType.AssetList assetList = new org.smpte_ra.schemas._2067_2._2016.pkl.PackingListType.AssetList();
        List<org.smpte_ra.schemas._2067_2._2016.pkl.AssetType> packingListAssets = assetList.getAsset();
        for(PackingListBuilderAsset_2016 asset : assets){
            org.smpte_ra.schemas._2067_2._2016.pkl.AssetType packingListAssetType = new org.smpte_ra.schemas._2067_2._2016.pkl.AssetType();
            packingListAssetType.setId(asset.getUUID());
            packingListAssetType.setAnnotationText(asset.getAnnotationText());
            packingListAssetType.setHash(asset.getHash());
            packingListAssetType.setSize(asset.getSize());
            packingListAssetType.setType(asset.getAssetType().toString());
            packingListAssetType.setOriginalFileName(asset.getOriginalFileName());
            packingListAssetType.setHashAlgorithm(asset.getHashAlgorithm());
            packingListAssets.add(packingListAssetType);
        }
        packingListType.setAssetList(assetList);

        //The following attributes are optional setting them to null so that the JAXB Marshaller will not marshall them
        packingListType.setSigner(null);
        packingListType.setSignature(null);

        File outputFile = new File(this.workingDirectory + File.separator + this.pklFileName);
        boolean formatted = true;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try(
                InputStream packingListSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_2_2016/PKL/packingList_schema.xsd");
                InputStream dsigSchemaAsAStream = contextClassLoader.getResourceAsStream("org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd");
                OutputStream outputStream = new FileOutputStream(outputFile);
        )
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
            StreamSource[] schemaSources = new StreamSource[2];
            //The order in which these schema sources are initialized is important because some elements in the
            //PackingList schema depend on types defined in the DSig schema.
            schemaSources[0] = new StreamSource(dsigSchemaAsAStream);
            schemaSources[1] = new StreamSource(packingListSchemaAsAStream);
            Schema schema = schemaFactory.newSchema(schemaSources);

            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas._2067_2._2016.pkl");
            Marshaller marshaller = jaxbContext.createMarshaller();
            ValidationEventHandlerImpl validationEventHandler = new ValidationEventHandlerImpl(true);
            marshaller.setEventHandler(validationEventHandler);
            marshaller.setSchema(schema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

            /*marshaller.marshal(cplType, output);
            workaround for 'Error: unable to marshal type "AssetMapType" as an element because it is missing an @XmlRootElement annotation'
            as found at https://weblogs.java.net/blog/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always
             */
            marshaller.marshal(new JAXBElement<>(new QName("http://www.smpte-ra.org/schemas/2067-2/2016/PKL", "PackingList"), org.smpte_ra.schemas._2067_2._2016.pkl.PackingListType.class, packingListType), outputStream);
            outputStream.close();

            if(validationEventHandler.hasErrors())
            {
                //TODO : Perhaps a candidate for a Lambda
                for(ValidationEventHandlerImpl.ValidationErrorObject validationErrorObject : validationEventHandler.getErrors()) {
                    this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, validationErrorObject.getValidationEventSeverity(), validationErrorObject.getErrorMessage());
                }
            }
        }

        if(this.imfErrorLogger.getNumberOfErrors() > numErrors){
            List<ErrorLogger.ErrorObject> fatalErrors = imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, this.imfErrorLogger.getNumberOfErrors());
            if(fatalErrors.size() > 0){
                throw new IMFAuthoringException(String.format("Following FATAL errors were detected while building the PackingList document %s", fatalErrors.toString()));
            }
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A class that roughly models a Packing List Asset conformant to the st0429-8:2007 schema
     */
    public static class PackingListBuilderAsset_2016 {
        private final String uuid;
        private final org.smpte_ra.schemas._2067_2._2016.pkl.UserText annotationText;
        private final byte[] hash;
        private final org.w3._2000._09.xmldsig_.DigestMethodType hashAlgorithm;
        private final BigInteger size;
        private final PKLAssetTypeEnum assetType;
        private final org.smpte_ra.schemas._2067_2._2016.pkl.UserText originalFileName;

        /**
         * A constructor for a PackingListAsset that roughly models a Packing List Asset conformant to the st0429-8:2007 schema
         * @param uuid that uniquely identifies this asset in the Packing List
         * @param annotationText a free form human readable text
         * @param hash a byte[] containing the Base64 encoded SHA-1 hash of this PackingList Asset
         * @param hashAlgorithm a String representing the algorithm used to generate the Hash
         * @param size of the asset in bytes
         * @param assetType could be either text/xml or application/mxf as defined in st0429-8:2007
         * @param originalFileName a free form human readable text that contains the name of the file
         *                         containing the asset at the time the PackingList was created
         */
        public PackingListBuilderAsset_2016(@Nonnull UUID uuid,
                                            @Nonnull org.smpte_ra.schemas._2067_2._2016.pkl.UserText annotationText,
                                            @Nonnull byte[] hash,
                                            @Nonnull org.w3._2000._09.xmldsig_.DigestMethodType hashAlgorithm,
                                            @Nonnull Long size,
                                            @Nonnull PKLAssetTypeEnum assetType,
                                            org.smpte_ra.schemas._2067_2._2016.pkl.UserText originalFileName){
            this.uuid = UUIDHelper.fromUUID(uuid);
            this.annotationText = annotationText;
            this.hash = Arrays.copyOf(hash, hash.length);
            this.hashAlgorithm = hashAlgorithm;
            this.size = BigInteger.valueOf(size);
            this.assetType = assetType;
            this.originalFileName = originalFileName;
        }

        /**
         * Getter for the UUID identifying this Packing List Asset
         * @return a String representing the "urn:uuid:" of this PackingList Asset
         */
        public String getUUID(){
            return this.uuid;
        }

        /**
         * Getter for the AnnotationText of this Packing List Asset
         * @return a UserText representing the Annotation Text
         */
        public org.smpte_ra.schemas._2067_2._2016.pkl.UserText getAnnotationText(){
            return this.annotationText;
        }

        /**
         * Getter for the Hash of this Packing List Asset
         * @return a byte[] representing the Base64 encoded SHA-1 Hash of the Asset
         */
        public byte[] getHash(){
            return Arrays.copyOf(this.hash, this.hash.length);
        }

        /**
         * Getter for the HashAlgorithm used to generate the Hash of this PackingList Asset
         * @return a org.w3._2000._09.xmldsig_.DigestMethodType representing the Hashing algorithm
         */
        public org.w3._2000._09.xmldsig_.DigestMethodType getHashAlgorithm(){
            return this.hashAlgorithm;
        }

        /**
         * Getter for the size of this Packing List Asset
         * @return a BigInteger representing the size of this PackingList Asset
         */
        public BigInteger getSize(){
            return this.size;
        }

        /**
         * Getter for the type of this PackingList Asset
         * @return an enumerated value representing the type of this PackingList Asset
         */
        public PKLAssetTypeEnum getAssetType(){
            return this.assetType;
        }

        /**
         * Getter for the OriginalFileName of this PackingList Asset
         * @return a UserText representing the OriginalFileName of the PackingList Asset
         *          at the time that the PackingList was created
         */
        public org.smpte_ra.schemas._2067_2._2016.pkl.UserText getOriginalFileName(){
            return this.originalFileName;
        }
    }
}
