package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.*;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.*;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.writerTools.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpte_ra.schemas.st429_7_2006.*;
import org.smpte_ra.schemas.st429_7_2006.CompositionPlaylistType;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.xml.sax.SAXException;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by schakrovorthy on 1/14/17.
 */
public class DCPCompositionPlaylistBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DCPCompositionPlaylistBuilder.class);

    private final String annotationText;
    private final UUID cplUUID;
    private final List<File> trackFiles;
    private final File workingDirectory;
    private final String cplFileName;
    private final IMFErrorLogger imfErrorLogger;

    public DCPCompositionPlaylistBuilder(String annotationText,
                                         String issuer,
                                         UUID cplUUID,
                                         List<File> trackFiles,
                                         File workingDirectory) {
        this.annotationText = annotationText;
        this.cplUUID = cplUUID;
        this.trackFiles = trackFiles;
        this.workingDirectory = workingDirectory;
        this.cplFileName = "CPL-" + this.cplUUID.toString() + ".xml";
        this.imfErrorLogger = new IMFErrorLoggerImpl();
    }

    public File build() throws IOException {
        return getCompositionPlaylist();

    }

    /**
     * A template method to get an IMF CPL representation of the IMF Essence that the IMFTrackFileCPLBuilder was
     * initialized with.
     *
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getCompositionPlaylist() throws IOException {
        CompositionPlaylistType cplRoot = new CompositionPlaylistType();

        cplRoot.setId(IMFUUIDGenerator.getInstance().getUrnUUID());
            /*CPL Annotation*/
        cplRoot.setAnnotationText(textToUserText(this.annotationText));
            /*IssueDate*/
        cplRoot.setIssueDate(IMFUtils.createXMLGregorianCalendar());
            /*Issuer*/
        cplRoot.setIssuer(null);
            /*Creator*/
        cplRoot.setCreator(null);
            /*Content Originator*/
        cplRoot.setContentVersion(null);
            /*Content Title*/
        cplRoot.setContentTitleText(this.textToUserText("Not included"));
            /*Content Kind*/
        cplRoot.setContentKind(null);
            /* ID */
        cplRoot.setId(this.cplUUID.toString());
        cplRoot.setIconId(null);

        cplRoot.setRatingList(null);

            /*Signature elements*/
        cplRoot.setSigner(null);
        cplRoot.setSignature(null);

        CompositionPlaylistType.ReelList reelList = new CompositionPlaylistType.ReelList();
        ReelType reelType = new ReelType();

        List<GenericAssetType> assetTypeList = analyzeTrackFiles(trackFiles);
        List<GenericAssetType> pictureTrackFileAssetTypeList = assetTypeList.stream().filter(e -> e instanceof PictureTrackFileAssetType).collect(Collectors.toList());
        List<GenericAssetType> soundTrackFileAssetTypeList = assetTypeList.stream().filter(e -> e instanceof SoundTrackFileAssetType).collect(Collectors.toList());

        ReelType.AssetList assetList = new ReelType.AssetList();
        if(pictureTrackFileAssetTypeList.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Multiple/No video track file exists");
            assetList.setMainPicture(null);
        }
        else {
            assetList.setMainPicture((PictureTrackFileAssetType) pictureTrackFileAssetTypeList.get(0));
        }

        if(soundTrackFileAssetTypeList.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Multiple/No audio track file exists");
            assetList.setMainSound(null);
        }
        else {
            assetList.setMainSound((SoundTrackFileAssetType) soundTrackFileAssetTypeList.get(0));
        }
        assetList.setMainSubtitle(null);
        assetList.setMainMarkers(null);

        reelType.setAssetList(assetList);
        reelList.getReel().add(reelType);
        cplRoot.setReelList(reelList);
            /*This will serialize the CPL and generate its XML representation*/
        File outputFile = new File(this.workingDirectory + File.separator + this.cplFileName);
        serializeCPLToXML(cplRoot, outputFile);
        return outputFile;
    }

    public UserText textToUserText(String text) {
        UserText userText = new UserText();
        userText.setValue(text);
        userText.setLanguage("en");
        return userText;
    }

    private void serializeCPLToXML(CompositionPlaylistType cplRoot, File outputFile) throws IOException {

        int numErrors = imfErrorLogger.getNumberOfErrors();
        boolean formatted = true;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            try (
                    InputStream dsigSchemaAsAStream = contextClassLoader.getResourceAsStream("org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd");
                    InputStream cplSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st429_7_2006/SMPTE-429-7-2006-CPL.xsd");
                    OutputStream outputStream = new FileOutputStream(outputFile)
            ) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StreamSource[] schemaSources = new StreamSource[2];
                schemaSources[0] = new StreamSource(dsigSchemaAsAStream);
                schemaSources[1] = new StreamSource(cplSchemaAsAStream);
                Schema schema = schemaFactory.newSchema(schemaSources);

                JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st429_7_2006");
                Marshaller marshaller = jaxbContext.createMarshaller();
                ValidationEventHandlerImpl validationEventHandler = new ValidationEventHandlerImpl(true);
                marshaller.setEventHandler(validationEventHandler);
                marshaller.setSchema(schema);
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

            /*marshaller.marshal(cplType, output);
            workaround for 'Error: unable to marshal type "CompositionPlaylistType" as an element because it is missing an @XmlRootElement annotation'
            as found at https://weblogs.java.net/blog/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always
             */
                marshaller.marshal(new JAXBElement<>(new QName("http://www.smpte-ra.org/schemas/429-7/2006/CPL", "CompositionPlaylist"), org.smpte_ra.schemas.st429_7_2006.CompositionPlaylistType.class, cplRoot), outputStream);


                if (this.imfErrorLogger.getNumberOfErrors() > numErrors) {
                    List<ErrorLogger.ErrorObject> fatalErrors = imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, this.imfErrorLogger.getNumberOfErrors());
                    if (fatalErrors.size() > 0) {
                        throw new IMFAuthoringException(String.format("Following FATAL errors were detected while building the PackingList document %s", fatalErrors.toString()));
                    }
                }
            }
        }
        catch(SAXException | JAXBException e) {
            throw new IMFAuthoringException(String.format("FATAL errors were detected while building the PackingList document"));
        }

    }

        public List<GenericAssetType> analyzeTrackFiles(List<File> trackFiles) throws IOException {
            List<GenericAssetType> assetTypeList = new ArrayList<>();
            for (File trackFile : trackFiles) {
                if (trackFile.getName().endsWith(".mxf")) {
                    ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(trackFile);

                    PayloadRecord headerPartitionPayloadRecord = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, new IMFErrorLoggerImpl());
                    byte[] headerPartitionBytes = headerPartitionPayloadRecord.getPayload();
                    ByteProvider byteProvider = new ByteArrayDataProvider(headerPartitionBytes);
                    HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, (long) headerPartitionBytes.length, imfErrorLogger);
                    if (headerPartition.getEssenceTypes().size() != 1) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Multiple/No essence found in a track file");
                        continue;
                    }
                    HeaderPartition.EssenceTypeEnum essenceTypeEnum = headerPartition.getEssenceTypes().get(0);
                    TrackFileAssetType trackFileAssetType = null;
                    if (essenceTypeEnum.equals(HeaderPartition.EssenceTypeEnum.MainImageEssence)) {
                        PictureTrackFileAssetType pictureTrackFileAssetType = new PictureTrackFileAssetType();
                        pictureTrackFileAssetType.setId(getTrackFileId(headerPartitionPayloadRecord).toString());
                        pictureTrackFileAssetType.setAnnotationText(textToUserText(trackFile.getName()));
                        pictureTrackFileAssetType.setDuration(headerPartition.getEssenceDuration().longValue());
                        pictureTrackFileAssetType.setEntryPoint(0L);
                        pictureTrackFileAssetType.setIntrinsicDuration(headerPartition.getEssenceDuration().longValue());
                        pictureTrackFileAssetType.setHash(null);
                        pictureTrackFileAssetType.setKeyId(null);
                        List<Long> videoFPS = getVideoFPS(headerPartition);
                        if (videoFPS.size() != 2) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Video FPS missing in image track file");
                        } else {
                            pictureTrackFileAssetType.getFrameRate().addAll(videoFPS);
                            pictureTrackFileAssetType.getEditRate().addAll(videoFPS);
                        }
                        List<Long> aspectRatio = getAspectRatio(headerPartition);
                        if (aspectRatio.size() != 2) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Aspect Ratio not found in image track file");
                        } else {
                            pictureTrackFileAssetType.getScreenAspectRatio().addAll(aspectRatio);
                        }
                        trackFileAssetType = pictureTrackFileAssetType;
                    } else if (essenceTypeEnum.equals(HeaderPartition.EssenceTypeEnum.MainAudioEssence)) {
                        SoundTrackFileAssetType soundTrackFileAssetType = new SoundTrackFileAssetType();
                        soundTrackFileAssetType.setId(getTrackFileId(headerPartitionPayloadRecord).toString());
                        soundTrackFileAssetType.setAnnotationText(textToUserText(trackFile.getName()));
                        soundTrackFileAssetType.setDuration(headerPartition.getEssenceDuration().longValue());
                        soundTrackFileAssetType.setEntryPoint(0L);
                        soundTrackFileAssetType.setIntrinsicDuration(headerPartition.getEssenceDuration().longValue());
                        soundTrackFileAssetType.setHash(null);
                        soundTrackFileAssetType.setKeyId(null);
                        soundTrackFileAssetType.setLanguage(headerPartition.getAudioEssenceSpokenLanguage());
                        List<Long> sampleRate = getAudioSampleRate(headerPartition);
                        if (sampleRate.size() != 2) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Audio sample rate missing in audio track file");
                        } else {
                            soundTrackFileAssetType.getEditRate().addAll(sampleRate);
                        }
                        trackFileAssetType = soundTrackFileAssetType;
                    } else {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Unsupported essence found in a track file");
                    }
                    if (trackFileAssetType != null) {

                        assetTypeList.add(trackFileAssetType);
                    }
                }

            }
            return assetTypeList;
        }

    public List<Long> getAudioSampleRate(HeaderPartition headerPartition) throws IOException {

        List<Long> sampleRate = new ArrayList<>();
        WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = (WaveAudioEssenceDescriptor) headerPartition.getWaveAudioEssenceDescriptors().get(0);
        sampleRate.add(Long.valueOf(waveAudioEssenceDescriptor.getAudioSamplingRateNumerator()));
        sampleRate.add(Long.valueOf(waveAudioEssenceDescriptor.getAudioSamplingRateDenominator()));
        return sampleRate;
    }

    public List<Long> getVideoFPS(HeaderPartition headerPartition) throws IOException {

        List<Long> sampleRate = new ArrayList<>();
        List<InterchangeObject.InterchangeObjectBO>essenceDescriptors = headerPartition.getEssenceDescriptors();
        for(InterchangeObject.InterchangeObjectBO interchangeObjectBO : essenceDescriptors){
            if (interchangeObjectBO instanceof GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO) {
                GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO genericPictureEssenceDescriptorBO = GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO.class.cast(interchangeObjectBO);
                if(genericPictureEssenceDescriptorBO.getAspect_ratio() != null) {
                    sampleRate = genericPictureEssenceDescriptorBO.getSampleRate();
                }
            }
        }
        return sampleRate;
    }

    public List<Long> getAspectRatio(HeaderPartition headerPartition) throws IOException {

        List<Long> aspectRatio = new ArrayList<>();
        List<InterchangeObject.InterchangeObjectBO>essenceDescriptors = headerPartition.getEssenceDescriptors();
        for(InterchangeObject.InterchangeObjectBO interchangeObjectBO : essenceDescriptors){
            if (interchangeObjectBO instanceof GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO) {
                GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO genericPictureEssenceDescriptorBO = GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO.class.cast(interchangeObjectBO);
                if(genericPictureEssenceDescriptorBO.getAspect_ratio() != null) {
                    aspectRatio.add(genericPictureEssenceDescriptorBO.getAspect_ratio().getNumerator());
                    aspectRatio.add(genericPictureEssenceDescriptorBO.getAspect_ratio().getDenominator());
                }
            }
        }
        return aspectRatio;
    }

    private static UUID getTrackFileId(PayloadRecord headerPartitionPayloadRecord) throws
            IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        UUID packageUUID = null;
        if (headerPartitionPayloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Payload asset type is %s, expected asset type %s", headerPartitionPayloadRecord.getPayloadAssetType(),
                            PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            return packageUUID;
        }
        try {
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPartitionPayloadRecord.getPayload()),
                    0L,
                    (long) headerPartitionPayloadRecord.getPayload().length,
                    imfErrorLogger);

            /**
             * Add the Top Level Package UUID to the set of TrackFileIDs, this is required to validate that the essences header partition that were passed in
             * are in fact from the constituent resources of the VirtualTack
             */
            Preface preface = headerPartition.getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            packageUUID = filePackage.getPackageMaterialNumberasUUID();
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        } catch (MXFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        return packageUUID;
    }

    @Nullable
    public static PayloadRecord getHeaderPartitionPayloadRecord(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        if(rangeStart < 0 ) {
            return null;
        }
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = IMPValidator.getRandomIndexPackSize(payloadRecord);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;
        if(rangeStart < 0 ) {
            return null;
        }

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = IMPValidator.getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize);

        if (partitionByteOffsets.size() >= 2) {
            rangeStart = partitionByteOffsets.get(0);
            rangeEnd = partitionByteOffsets.get(1) - 1;
            byte[] headerPartitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            PayloadRecord headerParitionPayload = new PayloadRecord(headerPartitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
            return headerParitionPayload;
        }


        return null;

    }

    public IMFErrorLogger getImfErrorLogger() {
        return imfErrorLogger;
    }

    private static String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <VideoInputFilePath> <AudioInputFilePath> <workingDirectory>%n", DCPCompositionPlaylistBuilder.class.getName()));
        return sb.toString();
    }

    public static void main(String[] args){

        if (args.length != 3)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File videoFile = new File(args[0]);
        if(!videoFile.exists()){
            logger.error(String.format("File %s does not exist", videoFile.getAbsolutePath()));
            System.exit(-1);
        }
        File audioFile = new File(args[1]);
        if(!audioFile.exists()){
            logger.error(String.format("File %s does not exist", audioFile.getAbsolutePath()));
            System.exit(-1);
        }

        File workingDirectory = new File(args[2]);

        logger.info(String.format("Video File Name is %s", videoFile.getName()));
        logger.info(String.format("Audio File Name is %s", audioFile.getName()));

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try
        {
            List<File> fileList = new ArrayList<>();
            fileList.add(videoFile);
            fileList.add(audioFile);
            UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();
            DCPCompositionPlaylistBuilder dcpCompositionPlaylistBuilder = new DCPCompositionPlaylistBuilder("DCPComposition", "Netflix", cplUUID, fileList, workingDirectory);

            File outputFile = dcpCompositionPlaylistBuilder.getCompositionPlaylist();
            logger.info(String.format("CPL File Name is %s", outputFile.getName()));

            imfErrorLogger.addAllErrors(dcpCompositionPlaylistBuilder.getImfErrorLogger().getErrors());
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
            logger.info("No errors were detected in the IMFTrackFile");
        }
    }
}
