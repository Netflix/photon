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

import com.sandflow.smpte.klv.Triplet;
import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import org.smpte_ra.schemas.ObjectFactory;
import org.smpte_ra.schemas.TrackFileResourceType;
import org.smpte_ra.schemas.BaseResourceType;
import org.smpte_ra.schemas.CompositionTimecodeType;
import org.smpte_ra.schemas.ContentKindType;
import org.smpte_ra.schemas.ContentMaturityRatingType;
import org.smpte_ra.schemas.ContentVersionType;
import org.smpte_ra.schemas.EssenceDescriptorBaseType;
import org.smpte_ra.schemas.LocaleType;
import org.smpte_ra.schemas.SegmentType;
import org.smpte_ra.schemas.SequenceType;
import org.smpte_ra.schemas.UserTextType;
import org.smpte_ra.schemas.CompositionPlaylistType;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.writerTools.IMFCPLFactory;
import com.netflix.imflibrary.writerTools.RegXMLLibHelper;
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
import java.util.List;

/**
 * A class that builds an IMF CPL representation of an IMF Essence
 */
@Immutable
final class IMFEssenceCPLBuilder {

    private static final Logger logger = LoggerFactory.getLogger(IMFEssenceComponentReader.class);
    private final IMFEssenceComponentReader imfEssenceComponentReader;
    private final RegXMLLibHelper regXMLLibHelper;
    private final File workingDirectory;
    private final CompositionPlaylistType cplRoot;
    private final File mxfFile;
    private final String fileName;


    /**
     * A constructor for the IMFEssenceCPLBuilder class. This class creates an IMF CPL representation of an IMF Essence
     * @param workingDirectory - A location on a file system used for processing the essence.
     *                         This would also be the location where the CPL representation of the IMFEssence would be written into.
     * @param essenceFile - File representing an IMF Essence
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public IMFEssenceCPLBuilder(File workingDirectory, File essenceFile) throws IOException {
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(essenceFile);
        this.imfEssenceComponentReader = new IMFEssenceComponentReader(workingDirectory, resourceByteRangeProvider);
        MXFKLVPacket.Header primerPackHeader = this.imfEssenceComponentReader.getPrimerPackHeader();
        this.regXMLLibHelper = new RegXMLLibHelper(primerPackHeader, this.imfEssenceComponentReader.getByteProvider(primerPackHeader));
        this.workingDirectory = workingDirectory;
        /*Peek into the CompositionPlayListType and recursively construct its constituent fields*/
        this.cplRoot = IMFCPLFactory.constructCompositionPlaylistType();
        this.mxfFile = essenceFile;
        this.fileName = this.mxfFile.getName();
    }

    /**
     * A template method to get an IMF CPL representation of the IMF Essence that the IMFEssenceCPLBuilder was
     * initialized with.
     *
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getCompositionPlaylist() throws IOException {
        this.cplRoot.setId(IMFUUIDGenerator.getInstance().getUUID());
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
        String essenceType = this.imfEssenceComponentReader.getEssenceType();
        this.cplRoot.getContentKind().setValue(essenceType);
        this.cplRoot.getContentKind().setScope("General");
        /*Extension Properties*/
        buildExtensionProperties();
        /*Total Running Time*/
        this.cplRoot.setTotalRunningTime(null);
        /*Build ContentVersionList*/
        buildContentVersionList();
        /*EssenceDescriptorList*/
        buildEssenceDescriptorList();
        /*CompositionTimeCode*/
        buildCompositionTimeCode();
        /*Edit Rate*/
        List<Long> list = this.imfEssenceComponentReader.getEssenceEditRateAsList();
        this.cplRoot.getEditRate().addAll(list);
        /*Locale List*/
        this.buildLocaleList();
        /*Segment List*/
        this.buildSegmentList();
        /*Signature elements*/
        this.buildSignatureElements();

        /*This will serialize the CPL and generate its XML representation*/
        return this.serializeCPL();
    }

    private File serializeCPL() throws IOException {
        File outputFile = new File(this.workingDirectory + "/" + "CPL.xml");
        IMFUtils.writeCPLToFile(this.cplRoot, outputFile);
        return outputFile;
    }

    private UserTextType buildUserTextType(String value, String language){
        UserTextType userTextType = new UserTextType();
        userTextType.setValue(value);
        userTextType.setLanguage(language);
        return userTextType;
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
        String uuidString = IMFUUIDGenerator.getInstance().getUUID();
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

    private void buildEssenceDescriptorList() throws IOException{

        try {
            List<EssenceDescriptorBaseType> essenceDescriptorList = this.cplRoot.getEssenceDescriptorList().getEssenceDescriptor();
            List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = this.imfEssenceComponentReader.getEssenceDescriptors();
            for(InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                MXFKLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                List<MXFKLVPacket.Header> subDescriptorHeaders = this.imfEssenceComponentReader.getSubDescriptorKLVHeader(essenceDescriptor);
                /*Create a dom*/
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();

                EssenceDescriptorBaseType essenceDescriptorBaseType = new EssenceDescriptorBaseType();
                essenceDescriptorBaseType.setId(IMFUUIDGenerator.getInstance().getUUID());

                DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(document, essenceDescriptorHeader, subDescriptorHeaders);
                Node node = documentFragment.getFirstChild();

                essenceDescriptorBaseType.getAny().add(node);
                essenceDescriptorList.add(essenceDescriptorBaseType);
            }
        }
        catch(ParserConfigurationException e){
            throw new IMFException(e);
        }

    }

    private void buildCompositionTimeCode(){
        this.cplRoot.setCompositionTimecode(null);
    }

    private void buildSampleCompositionTimeCode() throws IOException {
        /* Following serves as SampleCode to getCompositionPlaylist a CompositionTimecode object*/
        CompositionTimecodeType compositionTimecodeType = this.cplRoot.getCompositionTimecode();
        compositionTimecodeType.setTimecodeDropFrame(false);/*TimecodeDropFrame set to false by default*/
        compositionTimecodeType.setTimecodeRate(this.imfEssenceComponentReader.getEssenceEditRate());
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
        localeType.setAnnotation(this.buildUserTextType("Netflix-CustomLocale", "en"));
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

    private void buildSegmentList() throws IOException {
        /*Segment Id*/
        List<SegmentType>segments = this.cplRoot.getSegmentList().getSegment();
        SegmentType segmentType = new SegmentType();
        segmentType.setId(IMFUUIDGenerator.getInstance().getUUID());
        /*Segment Annotation*/
        String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
        segmentType.setAnnotation(buildUserTextType(name, "en"));
        /*Sequence List*/
        SegmentType.SequenceList sequenceList = new SegmentType.SequenceList();
        SequenceType sequenceType = this.buildSequenceType();
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<SequenceType> element = null;
        if(this.imfEssenceComponentReader.getEssenceType().equals("MainImageSequence")){
            element = objectFactory.createMainImageSequence(sequenceType);
        }
        else if(this.imfEssenceComponentReader.getEssenceType().equals("MainAudioSequence")){
            element = objectFactory.createMainAudioSequence(sequenceType);
        }
        else{
            throw new IMFException(String.format("Currently only Audio/Image sequence types are supported"));
        }
        sequenceList.getAny().add(element);
        segmentType.setSequenceList(sequenceList);
        /*Add the segment that was built to the list of segments*/
        segments.add(segmentType);
    }

    private SequenceType buildSequenceType() throws IOException {
        SequenceType sequenceType = new SequenceType();
        /*Id*/
        sequenceType.setId(IMFUUIDGenerator.getInstance().getUUID());
        /*Track Id*/
        String trackId = IMFUUIDGenerator.getInstance().getUUID();
        sequenceType.setTrackId(trackId);
        /*ResourceList*/
        sequenceType.setResourceList(buildTrackResourceList());
        return sequenceType;
    }

    private SequenceType.ResourceList buildTrackResourceList() throws IOException {
        SequenceType.ResourceList resourceList = new SequenceType.ResourceList();
        List<BaseResourceType> baseResourceTypes = resourceList.getResource();
        TrackFileResourceType trackFileResourceType = new TrackFileResourceType();
        trackFileResourceType.setId(IMFUUIDGenerator.getInstance().getUUID());
        /*Resource Annotation*/
        String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
        trackFileResourceType.setAnnotation(buildUserTextType(name, "en"));
        /*Edit Rate*/
        trackFileResourceType.getEditRate().addAll(this.imfEssenceComponentReader.getEssenceEditRateAsList());
        /*Intrinsic Duration*/
        trackFileResourceType.setIntrinsicDuration(this.imfEssenceComponentReader.getEssenceDuration());
        /*Entry Point*/
        trackFileResourceType.setEntryPoint(BigInteger.valueOf(0L));
        /*Source Duration*/
        trackFileResourceType.setSourceDuration(trackFileResourceType.getIntrinsicDuration());/*Source Duration is set to the same value as the intrinsic duration*/
        /*Repeat Count*/
        trackFileResourceType.setRepeatCount(BigInteger.valueOf(1));
        /*Source Encoding*/
        trackFileResourceType.setSourceEncoding(IMFUUIDGenerator.getInstance().getUUID());
        /*Track File Id*/
        trackFileResourceType.setTrackFileId(IMFUUIDGenerator.getInstance().getUUID());
        /*Key Id*/
        trackFileResourceType.setKeyId(IMFUUIDGenerator.getInstance().getUUID());
        /*Hash*/
        trackFileResourceType.setHash(IMFUtils.generateHashAndBase64Encode(this.mxfFile));

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
    File getEssenceDescriptorAsXMLFile(Document document, MXFKLVPacket.Header essenceDescriptor) throws MXFException, IOException, TransformerException {

        File outputFile = new File(this.workingDirectory + "/" + "EssenceDescriptor.xml");

        document.setXmlStandalone(true);

        Triplet triplet = this.regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, this.imfEssenceComponentReader.getByteProvider(essenceDescriptor));
        DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(triplet, document);
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

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, MXFKLVPacket.Header essenceDescriptor, List<MXFKLVPacket.Header>subDescriptors) throws MXFException, IOException {
        document.setXmlStandalone(true);

        Triplet essenceDescriptorTriplet = this.regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, this.imfEssenceComponentReader.getByteProvider(essenceDescriptor));
        //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for(MXFKLVPacket.Header subDescriptorHeader : subDescriptors){
            subDescriptorTriplets.add(this.regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, this.imfEssenceComponentReader.getByteProvider(subDescriptorHeader)));
        }
        return this.regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document);
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath> <workingDirectory>%n", IMFEssenceCPLBuilder.class.getName()));
        return sb.toString();
    }

    public static void main(String[] args){

        if (args.length != 2)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);
        File workingDirectory = new File(args[1]);

        logger.info(String.format("File Name is %s", inputFile.getName()));
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFEssenceComponentReader imfEssenceComponentReader = new IMFEssenceComponentReader(workingDirectory, resourceByteRangeProvider);
        StringBuilder sb = new StringBuilder();

        try
        {
            IMFEssenceCPLBuilder IMFEssenceCPLBuilder = new IMFEssenceCPLBuilder(workingDirectory, inputFile);
            sb.append(imfEssenceComponentReader.getRandomIndexPack());
            logger.info(String.format("%s", sb.toString()));

            IMFEssenceCPLBuilder.getCompositionPlaylist();
        }
        catch(IOException e)
        {
            throw new IMFException(e);
        }

        logger.info(imfEssenceComponentReader.toString());
    }

}
