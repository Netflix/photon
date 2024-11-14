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
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.CoreConstraints;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2016;
import com.netflix.imflibrary.writerTools.IMPBuilder;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import jakarta.annotation.Nullable;
import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by svenkatrav on 9/2/16.
 */
public class IMPFixer {

    private static final String CONFORMANCE_LOGGER_PREFIX = "Virtual Track Conformance";
    private static final Logger logger = LoggerFactory.getLogger(IMPFixer.class);

    public static UUID getTrackFileId(PayloadRecord headerPartitionPayloadRecord) throws
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

    public static Map<UUID, PayloadRecord> getTrackFileIdToHeaderPartitionPayLoadMap(List<PayloadRecord>
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

    private static Boolean isCompositionComplete(IMFCompositionPlaylist imfCompositionPlaylist, Set<UUID> trackFileIDsSet, IMFErrorLogger imfErrorLogger) throws IOException {
        boolean bComplete = true;
        for (IMFEssenceComponentVirtualTrack virtualTrack : imfCompositionPlaylist.getEssenceVirtualTracks()) {
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
    public static PayloadRecord getHeaderPartitionPayloadRecord(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        if(rangeStart < 0 ) {
            return null;
        }
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = MXFUtils.getRandomIndexPackSize(payloadRecord);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;
        if(rangeStart < 0 ) {
            return null;
        }

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = MXFUtils.getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize);

        if (partitionByteOffsets.size() >= 2) {
            rangeStart = partitionByteOffsets.get(0);
            rangeEnd = partitionByteOffsets.get(1) - 1;
            byte[] headerPartitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            PayloadRecord headerParitionPayload = new PayloadRecord(headerPartitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
            return headerParitionPayload;
        }


        return null;

    }

    public static List<ErrorLogger.ErrorObject> analyzePackageAndWrite(Path rootPath, Path targetFile, String versionCPLSchema, Boolean copyTrackfile, Boolean generateHash) throws
            IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if (!Files.isDirectory(rootPath)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Root path invalid: %s", rootPath));
            return imfErrorLogger.getErrors();
        }

        List<PayloadRecord> headerPartitionPayloadRecords = new ArrayList<>();
        BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(rootPath);

        Path amPath = Paths.get(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI());
        if (!Files.isRegularFile(amPath)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("AssetMap path invalid: %s", mapProfileV2MappedFileSet.getAbsoluteAssetMapURI()));
            return imfErrorLogger.getErrors();
        }

        AssetMap assetMap = new AssetMap(amPath);
        for (AssetMap.Asset packingListAsset : assetMap.getPackingListAssets()) {

            Path pklPath = rootPath.resolve(packingListAsset.getPath().toString());
            if (!Files.isRegularFile(pklPath)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("PackingList path invalid: %s", rootPath.toString() + packingListAsset.getPath().toString()));
                continue;
            }

            PackingList packingList = new PackingList(pklPath);
            Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();

            for (PackingList.Asset asset : packingList.getAssets()) {

                Path assetPath = rootPath.resolve(assetMap.getPath(asset.getUUID()).toString());
                if (!Files.isRegularFile(assetPath)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("Path for asset with ID %s is invalid: %s", asset.getUUID(), rootPath.toString() + assetMap.getPath(asset.getUUID()).toString()));
                    continue;
                }

                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetPath);

                if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
                    PayloadRecord headerPartitionPayloadRecord = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, new IMFErrorLoggerImpl());
                    headerPartitionPayloadRecords.add(headerPartitionPayloadRecord);
                    byte[] bytes = headerPartitionPayloadRecord.getPayload();
                    byte[] hash = asset.getHash();
                    if( generateHash) {
                        hash = IMFUtils.generateSHA1Hash(resourceByteRangeProvider);
                    }
                    imfTrackFileMetadataMap.put(getTrackFileId(headerPartitionPayloadRecord),
                            new IMPBuilder.IMFTrackFileMetadata(bytes,
                                    hash,
                                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                                    Utilities.getFilenameFromPath(assetPath),
                                    resourceByteRangeProvider.getResourceSize())
                    );

                    if (copyTrackfile) {
                        try {
                            Path output = targetFile.resolve(Utilities.getFilenameFromPath(assetPath));
                            Files.copy(assetPath, output, REPLACE_EXISTING);
                        } catch (InvalidPathException e) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                    String.format("Invalid output path for Track File: " + Utilities.getFilenameFromPath(assetPath)));
                        } catch (IOException e) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                    String.format(e.toString()));
                        }
                    }
                }
            }

            Map<UUID, PayloadRecord> trackFileIDToHeaderPartitionPayLoadMap =
                    getTrackFileIdToHeaderPartitionPayLoadMap(headerPartitionPayloadRecords);

            for (PackingList.Asset asset : packingList.getAssets()) {
                Path assetPath = rootPath.resolve(assetMap.getPath(asset.getUUID()).toString());
                if (!Files.isRegularFile(assetPath)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("Path for asset with ID %s is invalid: %s", rootPath.toString() + assetMap.getPath(asset.getUUID()).toString() ,rootPath.toString() + packingListAsset.getPath().toString()));
                    continue;
                }

                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetPath);
                if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE) && IMFCompositionPlaylist.isCompositionPlaylist(resourceByteRangeProvider)) {

                    IMFCompositionPlaylist imfCompositionPlaylist;
                    try {
                        imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);
                    } catch (Exception e) {
                        continue;
                    }

                    Set<UUID> trackFileIDsSet = trackFileIDToHeaderPartitionPayLoadMap.keySet();
                        if(versionCPLSchema.equals(""))
                        {
                            String coreConstraintsSchema = imfCompositionPlaylist.getCoreConstraintsSchema();
                            if (coreConstraintsSchema.equals(CoreConstraints.NAMESPACE_IMF_2013)) {
                                versionCPLSchema = "2013";
                            }
                            else if (coreConstraintsSchema.equals(CoreConstraints.NAMESPACE_IMF_2016)
                                    || coreConstraintsSchema.equals(CoreConstraints.NAMESPACE_IMF_2020)) {
                                versionCPLSchema = "2016";
                            }
                            else {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                        String.format("Input package CoreConstraints Schema %s not supported", imfCompositionPlaylist.getCoreConstraintsSchema()));
                            }
                        }

                        if(versionCPLSchema.equals("2016"))
                        {
                            imfErrorLogger.addAllErrors(IMPBuilder.buildIMP_2016("IMP",
                                    "Netflix",
                                    imfCompositionPlaylist.getEssenceVirtualTracks(),
                                    imfCompositionPlaylist.getEditRate(),
                                    "http://www.smpte-ra.org/schemas/2067-21/2016",
                                    imfTrackFileMetadataMap,
                                    targetFile));

                        }
                        else if(versionCPLSchema.equals("2013")) {
                            imfErrorLogger.addAllErrors(IMPBuilder.buildIMP_2013("IMP",
                                    "Netflix",
                                    imfCompositionPlaylist.getEssenceVirtualTracks(),
                                    imfCompositionPlaylist.getEditRate(),
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

    private static String usage() {
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
            IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException, NoSuchAlgorithmException {

        if (args.length < 2) {
            logger.error(usage());
            System.exit(-1);
        }

        String inputFolderName = args[0];
        Path inputPath = Utilities.getPathFromString(inputFolderName);

        if (!Files.isDirectory(inputPath)) {
            logger.error(String.format("Invalid input package path: %s", inputFolderName));
            System.exit(-1);
        }

        String outputFolderName = args[1];
        Path outputPath = Utilities.getPathFromString(outputFolderName);
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            logger.error(String.format("Directory %s cannot be created", outputFolderName));
            System.exit(-1);
        }

        String versionCPLSchema = "";
        Boolean copyTrackFile = true;
        Boolean generateHash = true;

        for(int argIdx = 2; argIdx < args.length; ++argIdx)
        {
            String curArg = args[argIdx];
            String nextArg = argIdx < args.length - 1 ? args[argIdx + 1] : "";
            if(curArg.equalsIgnoreCase("--cpl-schema") || curArg.equalsIgnoreCase("-cs")) {
                if(nextArg.length() == 0 || nextArg.charAt(0) == '-') {
                    logger.error(usage());
                    System.exit(-1);
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
                logger.error(usage());
                System.exit(-1);
            }
        }

        List<ErrorLogger.ErrorObject> errors = analyzePackageAndWrite(inputPath, outputPath, versionCPLSchema, copyTrackFile, generateHash);
        if (errors.size() > 0) {
            logger.info(String.format("IMPWriter encountered errors:"));
            for (ErrorLogger.ErrorObject errorObject : errors) {
                if (errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                } else if (errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
            System.exit(-1);
        } else {
            logger.info(String.format("Created %s IMP successfully", outputFolderName));
        }
    }

}
