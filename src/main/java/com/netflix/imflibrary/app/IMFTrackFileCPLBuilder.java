/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2013;
import com.sandflow.smpte.klv.Triplet;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import org.smpte_ra.schemas.st2067_2_2013.ObjectFactory;
import org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType;
import org.smpte_ra.schemas.st2067_2_2013.BaseResourceType;
import org.smpte_ra.schemas.st2067_2_2013.CompositionTimecodeType;
import org.smpte_ra.schemas.st2067_2_2013.ContentKindType;
import org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType;
import org.smpte_ra.schemas.st2067_2_2013.ContentVersionType;
import org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType;
import org.smpte_ra.schemas.st2067_2_2013.LocaleType;
import org.smpte_ra.schemas.st2067_2_2013.SegmentType;
import org.smpte_ra.schemas.st2067_2_2013.SequenceType;
import org.smpte_ra.schemas.st2067_2_2013.UserTextType;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.writerTools.IMFCPLObjectFieldsFactory;
import com.netflix.imflibrary.utils.RegXMLLibHelper;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;


import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that builds an IMF Composition representation of an IMF Essence
 */
@Immutable
final class IMFTrackFileCPLBuilder {

    private static final Logger logger = LoggerFactory.getLogger(IMFTrackFileReader.class);
    private final IMFTrackFileReader imfTrackFileReader;
    private final RegXMLLibHelper regXMLLibHelper;
    private final File workingDirectory;
    private final org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType cplRoot;
    private final File mxfFile;
    private final String fileName;


    /**
     * A constructor for the IMFTrackFileCPLBuilder class. This class creates an IMF CPL representation of an IMF Essence
     * @param workingDirectory - A location on a file system used for processing the essence.
     *                         This would also be the location where the CPL representation of the IMFEssence would be written into.
     * @param essenceFile - File representing an IMF Essence
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public IMFTrackFileCPLBuilder(File workingDirectory, File essenceFile) throws IOException {
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(essenceFile);
        this.imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        KLVPacket.Header primerPackHeader = this.imfTrackFileReader.getPrimerPackHeader(imfErrorLogger);
        this.regXMLLibHelper = new RegXMLLibHelper(primerPackHeader, this.imfTrackFileReader.getByteProvider(primerPackHeader));
        this.workingDirectory = workingDirectory;
        /*Peek into the CompositionPlayListType and recursively construct its constituent fields*/
        this.cplRoot = IMFCPLObjectFieldsFactory.constructCompositionPlaylistType_2013();
        this.mxfFile = essenceFile;
        this.fileName = this.mxfFile.getName();
    }

    /**
     * A template method to get an IMF CPL representation of the IMF Essence that the IMFTrackFileCPLBuilder was
     * initialized with.
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getCompositionPlaylist(IMFErrorLogger imfErrorLogger) throws IOException {

        this.cplRoot.setId(IMFUUIDGenerator.getInstance().getUrnUUID());
        /*CPL Annotation*/
        this.cplRoot.getAnnotation().setValue(this.fileName);
        this.cplRoot.getAnnotation().setLanguage("en");/*Setting language to English which is also the default*/
        /*IssueDate*/
        this.cplRoot.setIssueDate(IMFUtils.createXMLGregorianCalendar());
        /*Issuer*/
        this.cplRoot.setIssuer(null);
        /*Creator*/
        this.cplRoot.setCreator(null);
        /*Content Originator*/
        this.cplRoot.setContentOriginator(null);
        /*Content Title*/
        String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
        this.cplRoot.getContentTitle().setValue(name);
        this.cplRoot.getContentTitle().setLanguage("en");
        /*Content Kind*/
        String essenceType = this.imfTrackFileReader.getEssenceType(imfErrorLogger).toString();
        this.cplRoot.getContentKind().setValue(essenceType);
        this.cplRoot.getContentKind().setScope("General");
        /*Extension Properties*/
        buildExtensionProperties();
        /*Total Running Time*/
        this.cplRoot.setTotalRunningTime(null);
        /*Build ContentVersionList*/
        buildContentVersionList();
        /*EssenceDescriptorList*/
        List<String> essenceDescriptorIdsList = Collections.synchronizedList(new LinkedList<String>());
        buildEssenceDescriptorList(essenceDescriptorIdsList, imfErrorLogger);
        /*CompositionTimeCode*/
        buildCompositionTimeCode();
        /*Edit Rate*/
        List<Long> list = this.imfTrackFileReader.getEssenceEditRateAsList(imfErrorLogger);
        this.cplRoot.getEditRate().addAll(list);
        /*Locale List*/
        this.buildLocaleList();
        /*Segment List*/
        this.buildSegmentList(essenceDescriptorIdsList, imfErrorLogger);
        /*Signature elements*/
        this.buildSignatureElements();

        /*This will serialize the CPL and generate its XML representation*/
        return this.serializeCPL();
    }

    private File serializeCPL() throws IOException {
        File outputFile = new File(this.workingDirectory + "/" + this.mxfFile.getName() + ".xml");
        IMFUtils.writeCPLToFile(this.cplRoot, outputFile);
        return outputFile;
    }

    private ContentKindType buildContentKindType(String value, String scope){
        ContentKindType contentKindType = this.cplRoot.getContentKind();
        contentKindType.setValue(value);
        contentKindType.setScope(scope);
        return contentKindType;
    }

    private void buildContentVersionList(){
        /*Content Version List*/
        List<ContentVersionType> contentVersionTypeList = this.cplRoot.getContentVersionList().getContentVersion();
        ContentVersionType contentVersionType = new ContentVersionType();
        /*Content Version Type*/
        String uuidString = IMFUUIDGenerator.getInstance().getUrnUUID();
        contentVersionType.setId(uuidString);
        UserTextType userTextType = new UserTextType();
        userTextType.setValue(uuidString);
        userTextType.setLanguage("en");
        contentVersionType.setLabelText(userTextType);
        /*Add the just created ContentVersionType to the list*/
        contentVersionTypeList.add(contentVersionType);
    }

    private void buildExtensionProperties(){
        this.cplRoot.setExtensionProperties(null);
    }

    private void buildEssenceDescriptorList(List<String> uuidList, IMFErrorLogger imfErrorLogger) throws IOException{

        try {
            List<EssenceDescriptorBaseType> essenceDescriptorList = this.cplRoot.getEssenceDescriptorList().getEssenceDescriptor();
            List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = this.imfTrackFileReader.getEssenceDescriptors(imfErrorLogger);
            for(InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                List<KLVPacket.Header> subDescriptorHeaders = this.imfTrackFileReader.getSubDescriptorKLVHeader(essenceDescriptor, imfErrorLogger);
                /*Create a dom*/
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();

                EssenceDescriptorBaseType essenceDescriptorBaseType = new EssenceDescriptorBaseType();
                String uuid = IMFUUIDGenerator.getInstance().getUrnUUID();
                essenceDescriptorBaseType.setId(uuid);
                uuidList.add(uuid);

                DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(document, essenceDescriptorHeader, subDescriptorHeaders, imfErrorLogger);
                Node node = documentFragment.getFirstChild();

                essenceDescriptorBaseType.getAny().add(node);
                essenceDescriptorList.add(essenceDescriptorBaseType);
            }
        }
        catch(ParserConfigurationException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, e.getMessage());
            throw new IMFException(e);
        }
    }

    private void buildCompositionTimeCode(){
        this.cplRoot.setCompositionTimecode(null);
    }

    private void buildSampleCompositionTimeCode(IMFErrorLogger imfErrorLogger) throws IOException {
        /* Following serves as SampleCode to getCompositionPlaylist a CompositionTimecode object*/
        CompositionTimecodeType compositionTimecodeType = this.cplRoot.getCompositionTimecode();
        compositionTimecodeType.setTimecodeDropFrame(false);/*TimecodeDropFrame set to false by default*/
        compositionTimecodeType.setTimecodeRate(this.imfTrackFileReader.getEssenceEditRate(imfErrorLogger));
        compositionTimecodeType.setTimecodeStartAddress(IMFUtils.generateTimecodeStartAddress());
    }

    private void buildLocaleList(){
        this.cplRoot.setLocaleList(null);
    }

    private void buildSampleLocaleList(){
        /*Following serves as SampleCode to getCompositionPlaylist a SampleLocaleList*/
        List<LocaleType> list = this.cplRoot.getLocaleList().getLocale();
        LocaleType localeType = new LocaleType();
        /*Locale Annotation*/
        localeType.setAnnotation(CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013("Netflix-CustomLocale", "en"));
        /*Locale Language List*/
        LocaleType.LanguageList languageList = new LocaleType.LanguageList();
        languageList.getLanguage().add("en");
        localeType.setLanguageList(languageList);
        /*Locale Region List*/
        LocaleType.RegionList regionList = new LocaleType.RegionList();
        regionList.getRegion().add("US");
        localeType.setRegionList(regionList);
        /*Locale Maturity Rating*/
        LocaleType.ContentMaturityRatingList contentMaturityRatingList = new LocaleType.ContentMaturityRatingList();
        List<ContentMaturityRatingType> contentMaturityRatingTypes = contentMaturityRatingList.getContentMaturityRating();
        ContentMaturityRatingType contentMaturityRatingType = new ContentMaturityRatingType();
        contentMaturityRatingType.setAgency("None");
        ContentMaturityRatingType.Audience audience = new ContentMaturityRatingType.Audience();
        audience.setValue("None");
        audience.setScope("General");
        contentMaturityRatingType.setAudience(audience);
        contentMaturityRatingType.setRating("None");
        contentMaturityRatingTypes.add(contentMaturityRatingType);
        localeType.setContentMaturityRatingList(contentMaturityRatingList);
        list.add(localeType);
    }

    private void buildSegmentList(List<String> uuidList, IMFErrorLogger imfErrorLogger) throws IOException {
        /*Segment Id*/
        List<SegmentType>segments = this.cplRoot.getSegmentList().getSegment();
        SegmentType segmentType = new SegmentType();
        segmentType.setId(IMFUUIDGenerator.getInstance().getUrnUUID());
        /*Segment Annotation*/
        String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
        String language = this.imfTrackFileReader.getAudioEssenceLanguage(imfErrorLogger) != null ? this.imfTrackFileReader.getAudioEssenceLanguage(imfErrorLogger) : "unknown";
        segmentType.setAnnotation(CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013(name, language));
        /*Sequence List*/
        SegmentType.SequenceList sequenceList = new SegmentType.SequenceList();
        int index = 0;
        SequenceType sequenceType = this.buildSequenceType(uuidList, index, imfErrorLogger);
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<SequenceType> element = null;
        if(this.imfTrackFileReader.getEssenceType(imfErrorLogger).equals(HeaderPartition.EssenceTypeEnum.MainImageEssence)){
            element = objectFactory.createMainImageSequence(sequenceType);
        }
        else if(this.imfTrackFileReader.getEssenceType(imfErrorLogger).equals(HeaderPartition.EssenceTypeEnum.MainAudioEssence)){
            element = objectFactory.createMainAudioSequence(sequenceType);
        }
        else{
            String message = String.format("Currently only Audio/Image sequence types are supported");
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, imfErrorLogger);
        }
        sequenceList.getAny().add(element);
        segmentType.setSequenceList(sequenceList);
        /*Add the segment that was built to the list of segments*/
        segments.add(segmentType);
    }

    private SequenceType buildSequenceType(List<String> uuidList, int index, IMFErrorLogger imfErrorLogger) throws IOException {
        SequenceType sequenceType = new SequenceType();
        /*Id*/
        sequenceType.setId(IMFUUIDGenerator.getInstance().getUrnUUID());
        /*Track Id*/
        String trackId = IMFUUIDGenerator.getInstance().getUrnUUID();
        sequenceType.setTrackId(trackId);
        /*ResourceList*/
        sequenceType.setResourceList(buildTrackResourceList(uuidList, index, imfErrorLogger));
        return sequenceType;
    }

    private SequenceType.ResourceList buildTrackResourceList(List<String> uuidList, int index, IMFErrorLogger imfErrorLogger) throws IOException {
        SequenceType.ResourceList resourceList = new SequenceType.ResourceList();
        List<BaseResourceType> baseResourceTypes = resourceList.getResource();
        TrackFileResourceType trackFileResourceType = new TrackFileResourceType();
        trackFileResourceType.setId(IMFUUIDGenerator.getInstance().getUrnUUID());
        /*Resource Annotation*/
        String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
        String language = this.imfTrackFileReader.getAudioEssenceLanguage(imfErrorLogger) != null ? this.imfTrackFileReader.getAudioEssenceLanguage(imfErrorLogger) : "unknown";
        trackFileResourceType.setAnnotation(CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013(name, language));
        /*Edit Rate*/
        trackFileResourceType.getEditRate().addAll(this.imfTrackFileReader.getEssenceEditRateAsList(imfErrorLogger));
        /*Intrinsic Duration*/
        trackFileResourceType.setIntrinsicDuration(this.imfTrackFileReader.getEssenceDuration(imfErrorLogger));
        /*Entry Point*/
        trackFileResourceType.setEntryPoint(BigInteger.valueOf(0L));
        /*Source Duration*/
        trackFileResourceType.setSourceDuration(trackFileResourceType.getIntrinsicDuration());/*Source Duration is set to the same value as the intrinsic duration*/
        /*Repeat Count*/
        trackFileResourceType.setRepeatCount(BigInteger.valueOf(1));
        /*Source Encoding*/
        trackFileResourceType.setSourceEncoding(uuidList.get(index));/*For the moment we assume that an EssenceDescriptor reference changes only at the Sequence Level*/
        /*Track File Id*/
        trackFileResourceType.setTrackFileId("urn" + ":" + "uuid" + ":" + this.imfTrackFileReader.getTrackFileId().toString());
        /*Key Id*/
        trackFileResourceType.setKeyId(IMFUUIDGenerator.getInstance().getUrnUUID());
        /*Hash*/
        trackFileResourceType.setHash(IMFUtils.generateSHA1HashAndBase64Encode(this.mxfFile));

        /*Add the constructed TrackFileResourceType to the ResourceTypes list*/
        BaseResourceType baseResourceType = (BaseResourceType)trackFileResourceType;
        baseResourceTypes.add(baseResourceType);
        return resourceList;
    }

    private void buildSignatureElements(){
       /**
        * For now set the signature related elements to null - TO DO populate accordingly
        */
        this.cplRoot.setSigner(null);
        this.cplRoot.setSignature(null);
    }


    /**
     * A method that returns a XML file representing a MXF EssenceDescriptor present in the IMF Essence.
     *
     * @return An XML DOM Document fragment representing the EssenceDescriptors contained in the MXF file
     * @throws IOException - any I/O related error will be exposed through an IOException,
     * @throws MXFException - any MXF standard related non-compliance will be exposed through a MXF exception
     * @throws TransformerException - any XML transformation critical error will be exposed through a TransformerException
     */
    File getEssenceDescriptorAsXMLFile(Document document, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header>subDescriptors, IMFErrorLogger imfErrorLogger) throws MXFException, IOException,
            TransformerException {

        File outputFile = new File(this.workingDirectory + "/" + "EssenceDescriptor.xml");

        document.setXmlStandalone(true);


        DocumentFragment documentFragment = getEssenceDescriptorAsDocumentFragment(document, essenceDescriptor, subDescriptors, imfErrorLogger);
        document.appendChild(documentFragment);

        /* write DOM to file */
        Transformer tr = TransformerFactory.newInstance().newTransformer();

        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        /*tr.transform(
                new DOMSource(document),
                new StreamResult(System.out)
        );*/

        tr.transform(
                new DOMSource(document),
                new StreamResult(outputFile)
        );


        return outputFile;
    }

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header>subDescriptors, IMFErrorLogger imfErrorLogger) throws
            MXFException, IOException {
        document.setXmlStandalone(true);

        Triplet essenceDescriptorTriplet = this.regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, this.imfTrackFileReader.getByteProvider(essenceDescriptor));
        //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for(KLVPacket.Header subDescriptorHeader : subDescriptors){
            subDescriptorTriplets.add(this.regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, this.imfTrackFileReader.getByteProvider(subDescriptorHeader)));
        }
        return this.regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document, imfErrorLogger);
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath> <workingDirectory>%n", IMFTrackFileCPLBuilder.class.getName()));
        return sb.toString();
    }

    public static void main(String[] args){

        if (args.length != 2)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }
        File workingDirectory = new File(args[1]);

        logger.info(String.format("File Name is %s", inputFile.getName()));
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        StringBuilder sb = new StringBuilder();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try
        {
            IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
            sb.append(imfTrackFileReader.getRandomIndexPack(imfErrorLogger));
            logger.info(String.format("%s", sb.toString()));

            imfTrackFileCPLBuilder.getCompositionPlaylist(imfErrorLogger);
        }
        catch(IOException e)
        {
            throw new IMFException(e);
        }
        List<ErrorLogger.ErrorObject> errors = imfErrorLogger.getErrors();
        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("IMFTrackFile has %d errors and %d warnings",
                    errors.size() - warningCount, warningCount));
            for(ErrorLogger.ErrorObject errorObject : errors){
                if(errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                }
                else if(errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
        }
        else{
            logger.info(imfTrackFileReader.toString());
            logger.info("No errors were detected in the IMFTrackFile");
        }
    }

}
