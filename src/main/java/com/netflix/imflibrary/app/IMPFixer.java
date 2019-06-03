package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2016;
import com.netflix.imflibrary.writerTools.IMPBuilder;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by svenkatrav on 9/2/16.
 */
public class IMPFixer {

    private static final String CONFORMANCE_LOGGER_PREFIX = "Virtual Track Conformance";
    private static final Logger logger = LoggerFactory.getLogger(IMPFixer.class);

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
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
            Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage) genericPackage;
            packageUUID = filePackage.getPackageMaterialNumberasUUID();
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        } catch (MXFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        return packageUUID;
    }

    private static Map<UUID, PayloadRecord> getTrackFileIdToHeaderPartitionPayLoadMap(List<PayloadRecord>
                                                                                              headerPartitionPayloadRecords) throws
            IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Map<UUID, PayloadRecord> trackFileIDMap = new HashMap<>();

        for (PayloadRecord payloadRecord : headerPartitionPayloadRecords) {
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(),
                                PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                continue;
            }
            try {
                HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long) payloadRecord.getPayload().length,
                        imfErrorLogger);

                /**
                 * Add the Top Level Package UUID to the set of TrackFileIDs, this is required to validate that the essences header partition that were passed in
                 * are in fact from the constituent resources of the VirtualTack
                 */
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                trackFileIDMap.put(packageUUID, payloadRecord);
            } catch (IMFException e) {
                imfErrorLogger.addAllErrors(e.getErrors());
            } catch (MXFException e) {
                imfErrorLogger.addAllErrors(e.getErrors());
            }
        }

        return Collections.unmodifiableMap(trackFileIDMap);
    }

    private static Boolean isCompositionComplete(ApplicationComposition applicationComposition, Set<UUID> trackFileIDsSet, IMFErrorLogger imfErrorLogger) throws IOException {
        Boolean bComplete = true;
        for (IMFEssenceComponentVirtualTrack virtualTrack : applicationComposition.getEssenceVirtualTracks()) {
            for (UUID uuid : virtualTrack.getTrackResourceIds()) {
                if (!trackFileIDsSet.contains(uuid)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes
                                    .IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                            String.format("CPL resource %s is not present in the package", uuid.toString()));
                    bComplete &= false;
                }
            }
        }

        return bComplete;

    }

    @Nullable
    private static PayloadRecord getHeaderPartitionPayloadRecord(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
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

    public static List<ErrorLogger.ErrorObject> analyzePackageAndWrite(File rootFile, File targetFile, String versionCPLSchema, Boolean copyTrackfile, Boolean generateHash) throws
            IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> headerPartitionPayloadRecords = new ArrayList<>();
        BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(rootFile);
        AssetMap assetMap = new AssetMap(new File(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI()));
        for (AssetMap.Asset packingListAsset : assetMap.getPackingListAssets()) {
            PackingList packingList = new PackingList(new File(rootFile, packingListAsset.getPath().toString()));
            Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();

            for (PackingList.Asset asset : packingList.getAssets()) {
                File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());
                if (!assetFile.exists()) {
                    throw new FileNotFoundException("Asset listed in PKL not found: " + assetFile.toPath().toString());
                }
                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);

                if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
                    PayloadRecord headerPartitionPayloadRecord = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, new IMFErrorLoggerImpl());
                    headerPartitionPayloadRecords.add(headerPartitionPayloadRecord);
                    if (headerPartitionPayloadRecord == null) {
                        throw new IMFException("Unable to get header partition payload for file: " + assetFile.toPath().toString());
                    }
                    byte[] bytes = headerPartitionPayloadRecord.getPayload();
                    byte[] hash = asset.getHash();
                    if( generateHash) {
                        hash = IMFUtils.generateSHA1Hash(resourceByteRangeProvider);
                    }
                    imfTrackFileMetadataMap.put(getTrackFileId(headerPartitionPayloadRecord),
                            new IMPBuilder.IMFTrackFileMetadata(bytes,
                                    hash,
                                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                                    assetFile.getName(),
                                    resourceByteRangeProvider.getResourceSize())
                    );
                    if(copyTrackfile) {
                        File outputFile = new File(targetFile.toString() + File.separator + assetFile.getName());
                        Files.copy(assetFile.toPath(), outputFile.toPath(), REPLACE_EXISTING);
                    }
                }
            }

            Map<UUID, PayloadRecord> trackFileIDToHeaderPartitionPayLoadMap =
                    getTrackFileIdToHeaderPartitionPayLoadMap(headerPartitionPayloadRecords);


            for (PackingList.Asset asset : packingList.getAssets()) {
                File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());
                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);
                if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE) && ApplicationComposition.isCompositionPlaylist(resourceByteRangeProvider)) {
                    ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, new IMFErrorLoggerImpl());
                    if(applicationComposition == null){
                        continue;
                    }
                    Set<UUID> trackFileIDsSet = trackFileIDToHeaderPartitionPayLoadMap.keySet();
                        if(versionCPLSchema.equals(""))
                        {
                            if (applicationComposition.getCoreConstraintsVersion().contains("st2067_2_2013")) {
                                versionCPLSchema = "2013";
                            }
                            else if (applicationComposition.getCoreConstraintsVersion().contains("st2067_2_2016")) {
                                versionCPLSchema = "2016";
                            }
                            else {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                        String.format("Input package CoreConstraintsVersion %s not supported", applicationComposition.getCoreConstraintsVersion().toString()));
                            }
                        }

                        if(versionCPLSchema.equals("2016"))
                        {
                            imfErrorLogger.addAllErrors(IMPBuilder.buildIMP_2016("IMP",
                                    "Netflix",
                                    applicationComposition.getEssenceVirtualTracks(),
                                    applicationComposition.getEditRate(),
                                    "http://www.smpte-ra.org/schemas/2067-21/2016",
                                    imfTrackFileMetadataMap,
                                    targetFile));

                        }
                        else if(versionCPLSchema.equals("2013")) {
                            imfErrorLogger.addAllErrors(IMPBuilder.buildIMP_2013("IMP",
                                    "Netflix",
                                    applicationComposition.getEssenceVirtualTracks(),
                                    applicationComposition.getEditRate(),
                                    "http://www.smpte-ra.org/schemas/2067-21/2016",
                                    imfTrackFileMetadataMap,
                                    targetFile));
                        }
                        else {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                    String.format("Invalid CPL schema %s for output", versionCPLSchema.equals("2013")));
                        }
                    }
                }
            }
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validateEssencePartition(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {

        IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

        PayloadRecord headerPartitionPayload = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, trackFileErrorLogger);
        if(headerPartitionPayload == null) {
            trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Failed to get header partition"));
        }
        else {
            List<PayloadRecord> payloadRecords = new ArrayList<>();
            payloadRecords.add(headerPartitionPayload);
            trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(payloadRecords));
        }

        return trackFileErrorLogger.getErrors();
    }

    static String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s input_package_directory output_package_directory [options]%n", IMPFixer.class.getName()));
        sb.append(String.format("options:            %n"));
        sb.append(String.format("-cs, --cpl-schema VERSION      CPL schema version for output IMP, supported values are 2013 or 2016%n"));
        sb.append(String.format("-nc, --no-copy                 don't copy track files     %n"));
        sb.append(String.format("-nh, --no-hash                 No update for trackfile hash in PKL %n"));
        return sb.toString();
    }


    public static void main(String args[]) throws
            IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException,
            NoSuchAlgorithmException, IllegalArgumentException {

        if (args.length < 2) {
            String message = usage();
            throw new IllegalArgumentException(message);
        }

        String inputFileName = args[0];
        File inputFile = new File(inputFileName);
        if (!inputFile.exists()) {
            String message = String.format("File %s does not exist", inputFile.getAbsolutePath());
            throw new FileNotFoundException(message);
        }

        String outputFileName = args[1];
        File outputFile = new File(outputFileName);
        if (!outputFile.exists() && !outputFile.mkdir()) {
            String message = String.format("Directory %s cannot be created", outputFile.getAbsolutePath());
            throw new FileNotFoundException(message);
        }

        String versionCPLSchema = "";
        Boolean copyTrackFile = true;
        Boolean generateHash = true;

        for(int argIdx = 2; argIdx < args.length; ++argIdx)
        {
            String curArg = args[argIdx];
            String nextArg = argIdx < args.length - 1 ? args[argIdx + 1] : "";
            if(curArg.equalsIgnoreCase("--cpl-schema") || curArg.equalsIgnoreCase("-cs")) {
                if(nextArg.length() == 0 || !Arrays.asList("2013", "2016").contains(nextArg)) {
                    throw new IllegalArgumentException(usage());
                }
                versionCPLSchema = nextArg;
                argIdx++;
            }
            else if(curArg.equalsIgnoreCase("--no-copy") || curArg.equalsIgnoreCase("-nc")) {
                copyTrackFile = false;
            }
            else if(curArg.equalsIgnoreCase("--no-hash") || curArg.equalsIgnoreCase("-nh")) {
                generateHash = false;
            }
            else {
                throw new IllegalArgumentException(usage());
            }
        }

        if (!inputFile.isDirectory()) {
            String message = String.format("Invalid input package path: %s", inputFileName);
            throw new IllegalArgumentException(message);
        }

        List<ErrorLogger.ErrorObject> errorsAndWarnings = analyzePackageAndWrite(inputFile, outputFile, versionCPLSchema, copyTrackFile, generateHash);
        if (errorsAndWarnings.size() > 0) {
            long errorCount = errorsAndWarnings.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL)).count();
            long warningCount = errorsAndWarnings.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.WARNING)).count();
            long nonFatalCount = errorsAndWarnings.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL)).count();

            if (warningCount > 0) {
                logger.warn("IMPWriter encountered warnings:");
                errorsAndWarnings.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.WARNING)).forEach(er -> logger.warn(er.toString()));
            }

            if (errorCount > 0 || nonFatalCount > 0) {
                String message = errorsAndWarnings.stream().filter(e -> (e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL) || e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL)))
                        .map(Object::toString).collect(Collectors.joining(System.lineSeparator()));
                throw new IMFException(message);
            }
        }
        logger.info(String.format("Created %s IMP successfully", outputFile.getName()));
    }

}
