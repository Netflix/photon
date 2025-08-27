package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.st2067_100.OutputProfileList;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateAssetMap;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateCPL;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateOPL;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validatePKL;

/**
 * Created by svenkatrav on 9/2/16.
 */
public class IMPAnalyzer {

    private static final String CONFORMANCE_LOGGER_PREFIX = "Virtual Track Conformance";
    private static final Logger logger = LoggerFactory.getLogger(IMPAnalyzer.class);



    private static final class TrackFilePartitionsRecord {
        String filename;
        PayloadRecord headerPartition;
        List<PayloadRecord> indexPartitions;

        TrackFilePartitionsRecord(String filename, PayloadRecord headerPartition, List<PayloadRecord> indexPartitions) {
            this.filename = filename;
            this.headerPartition = headerPartition;
            this.indexPartitions = indexPartitions;
        }
    }



    public static Map<String, List<ErrorLogger.ErrorObject>> analyzeDelivery(Path rootPath) throws IOException {

        Map<String, List<ErrorLogger.ErrorObject>> errorMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if (!Files.isDirectory(rootPath)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Root folder does not exist: %s.", rootPath));
            errorMap.put(rootPath.toString(), imfErrorLogger.getErrors());
            return errorMap;
        }

        try {
            BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(rootPath);
            imfErrorLogger.addAllErrors(mapProfileV2MappedFileSet.getErrors());
            IMFErrorLogger assetMapErrorLogger = new IMFErrorLoggerImpl();

            try {
                Path assetMapPath = Paths.get(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI());
                String amFilename = Utilities.getFilenameFromPath(assetMapPath);

                if (!Files.isRegularFile(assetMapPath)) {
                    assetMapErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("AssetMap file not found: %s.", mapProfileV2MappedFileSet.getAbsoluteAssetMapURI()));
                    errorMap.put(amFilename, assetMapErrorLogger.getErrors());
                    return errorMap;
                }

                AssetMap assetMap = new AssetMap(assetMapPath);
                assetMapErrorLogger.addAllErrors(assetMap.getErrors());

                if (assetMap.getPackingListAssets().isEmpty()) {
                    assetMapErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                            "AssetMap does not reference any PackingLists.");
                    errorMap.put(amFilename, assetMapErrorLogger.getErrors());
                    return errorMap;
                }

                for (AssetMap.Asset packingListAsset : assetMap.getPackingListAssets()) {
                    IMFErrorLogger packingListErrorLogger = new IMFErrorLoggerImpl();
                    try {

                        Path pkl = rootPath.resolve(packingListAsset.getPath().toString());
                        if (!Files.isRegularFile(assetMapPath)) {
                            assetMapErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                    String.format("Packing List file referenced from AssetMap, but not found: %s.", pkl));
                            continue;
                        }

                        PackingList packingList = new PackingList(pkl);
                        if (!packingList.getUUID().equals(packingListAsset.getUUID())) {
                            assetMapErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("AssetMap references PKL with ID %s, but PKL contains ID %s", packingListAsset.getUUID().toString(), packingList.getUUID().toString()));
                        }
                        packingListErrorLogger.addAllErrors(packingList.getErrors());

                        boolean xmlFileReferenced = false;
                        Map<String, IMFCompositionPlaylist> imfCompositionPlaylistMap = new HashMap<>();
                        Map<UUID, TrackFilePartitionsRecord> trackFileMap = new HashMap<>();

                        for (PackingList.Asset asset : packingList.getAssets()) {

                            // used for below check to issue warning if no assets of type XML are present at all; counting
                            // files before actually resolving path to avoid misleading WARNING due to missing uuid/file
                            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                                xmlFileReferenced = true;
                            }

                            URI path = assetMap.getPath(asset.getUUID());
                            if( path == null) {
                                packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                                continue;
                            }

                            Path assetPath = rootPath.resolve(assetMap.getPath(asset.getUUID()).toString());
                            if (!Files.isRegularFile(assetPath)) {
                                packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("Cannot find asset with id: urn:uuid:%s (path according to asset map: %s)", asset.getUUID().toString(), assetPath));
                                continue;
                            }

                            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetPath);
                            IMFErrorLogger assetErrorLogger = new IMFErrorLoggerImpl();
                            String filename = Utilities.getFilenameFromPath(assetPath);

                            if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
                                //
                                // MXF Track File
                                //
                                try {
                                    PayloadRecord headerPartitionPayloadRecord = MXFUtils.getHeaderPartitionPayloadRecord(resourceByteRangeProvider, assetErrorLogger);
                                    if (headerPartitionPayloadRecord == null) {
                                        assetErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                                String.format("Failed to retrieve header partition for %s", assetPath.toString()));
                                        continue;
                                    }

                                    // add header payload into UUID->Payload map
                                    UUID trackFileID = MXFUtils.getTrackFileId(headerPartitionPayloadRecord, assetErrorLogger);
                                    if (trackFileID == null) {
                                        throw new MXFException("Unable to retrieve Track File UUID from header metadata", assetErrorLogger);
                                    } else {
                                        if (!trackFileID.equals(asset.getUUID())) {
                                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the track file asset
                                            assetErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the MXF file is not same as UUID %s of the MXF file in the AssetMap", trackFileID.toString(), asset.getUUID().toString()));
                                        }
                                    }

                                    List<PayloadRecord> indexTablePartitionPayloadRecords = MXFUtils.getIndexTablePartitionPayloadRecords(resourceByteRangeProvider, assetErrorLogger);
                                    if (indexTablePartitionPayloadRecords.isEmpty()) {
                                        assetErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                                String.format("Failed to retrieve index partition for %s", assetPath.toString()));
                                        continue;
                                    }

                                    // add entry to track file map for further validation
                                    trackFileMap.put(trackFileID, new TrackFilePartitionsRecord(filename, headerPartitionPayloadRecord, indexTablePartitionPayloadRecords));
                                } catch( MXFException e) {
                                    assetErrorLogger.addAllErrors(e.getErrors());
                                }
                                catch( IMFException e) {
                                    assetErrorLogger.addAllErrors(e.getErrors());
                                }
                                finally {
                                    errorMap.put(filename, assetErrorLogger.getErrors());
                                }
                            } else if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {

                                // XML Assets

                                if (IMFCompositionPlaylist.isCompositionPlaylist(resourceByteRangeProvider)) {

                                    // Composition Playlist
                                    try {
                                        // instantiate IMFCompositionPlaylist
                                        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);
                                        assetErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
                                        if (assetErrorLogger.hasFatalErrors()) {
                                            continue;
                                        }

                                        // add IMFCompositionPlaylist to return List since no FATAL errors were encountered
                                        imfCompositionPlaylistMap.put(filename, imfCompositionPlaylist);

                                        // ensure Composition ID matches the one stated in the AssetMap
                                        if (!imfCompositionPlaylist.getUUID().equals(asset.getUUID())) {
                                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the CPL asset
                                            assetErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the CPL is not same as UUID %s of the CPL in the AssetMap", imfCompositionPlaylist.getUUID().toString(), asset.getUUID().toString()));
                                        }
                                    } catch (IMFException e) {
                                        assetErrorLogger.addAllErrors(e.getErrors());
                                    } finally {
                                        errorMap.put(filename, assetErrorLogger.getErrors());
                                    }
                                }
                            }
                        } // end of foreach-asset-in-pkl

                        // issue a warning if the PKL does not contain any assets of type text/xml to help troubleshoot non-compliant mime-types for CPL/OPL
                        if (!xmlFileReferenced) {
                            packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("Packing List does not contain any assets of type \"%s\" (i.e. PKL does not contain any CPL/OPL files).", PackingList.Asset.TEXT_XML_TYPE));
                        }

                        // identify sequence namespace for each individual track file entry and validate essence partitions
                        for (UUID key : trackFileMap.keySet()) {
                            TrackFilePartitionsRecord trackFileEntry = trackFileMap.get(key);
                            String sequenceNamespace = null;

                            for (IMFCompositionPlaylist imfCompositionPlaylist : imfCompositionPlaylistMap.values()) {
                                sequenceNamespace = imfCompositionPlaylist.getSequenceNamespaceForTrackFileID(key);
                                if (sequenceNamespace != null) {
                                    break;
                                }
                            }

                            List<ErrorLogger.ErrorObject> aggregateErrors = new ArrayList<>();

                            List<PayloadRecord> essencePartitions = new ArrayList<>();
                            essencePartitions.add(trackFileEntry.headerPartition);
                            essencePartitions.addAll(trackFileEntry.indexPartitions);

                            // avoid overwriting
                            aggregateErrors.addAll(IMPValidator.validateEssencePartitions(essencePartitions, sequenceNamespace));
                            if (errorMap.get(trackFileEntry.filename) != null)
                                aggregateErrors.addAll(errorMap.get(trackFileEntry.filename));
                            errorMap.put(trackFileEntry.filename, aggregateErrors);
                        }

                        // validate virtual track compliance for each IMFCompositionPlaylist with the header partition payloads collected from MXF Track Files
                        for (String filename : imfCompositionPlaylistMap.keySet()) {
                            IMFErrorLogger compositionErrorLogger = new IMFErrorLoggerImpl();

                            IMFCompositionPlaylist imfCompositionPlaylist = imfCompositionPlaylistMap.get(filename);

                            try {
                                // extract just the header partition payloads into a list
                                List<PayloadRecord> payloadRecords = new ArrayList<>();
                                trackFileMap.values().forEach(trackFilePartitionsRecord -> {
                                        payloadRecords.add(trackFilePartitionsRecord.headerPartition);
                                });

                                // validate IMFCompositionPlaylist
                                compositionErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, payloadRecords));
                            } catch (IMFException e) {
                                compositionErrorLogger.addAllErrors(e.getErrors());
                            } finally {
                                List<ErrorLogger.ErrorObject> aggregateErrors = new ArrayList<>();
                                aggregateErrors.addAll(compositionErrorLogger.getErrors());
                                if (errorMap.get(filename) != null)
                                    aggregateErrors.addAll(errorMap.get(filename));
                                errorMap.put(filename, aggregateErrors);
                            }
                        }

                        // lastly, validate OPLs
                        analyzeOutputProfileLists( rootPath, assetMap, packingList, imfCompositionPlaylistMap.values().stream().collect(Collectors.toUnmodifiableList()), packingListErrorLogger, errorMap);

                    } catch (IMFException e) {
                        packingListErrorLogger.addAllErrors(e.getErrors());
                    }
                    finally {
                        errorMap.put(packingListAsset.getPath().toString(), packingListErrorLogger.getErrors());
                    }
                }
            } catch (IMFException e) {
                assetMapErrorLogger.addAllErrors(e.getErrors());
            }
            finally {
                errorMap.put(BasicMapProfileV2MappedFileSet.ASSETMAP_FILE_NAME, assetMapErrorLogger.getErrors());
            }
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
            errorMap.put(rootPath.toString(), imfErrorLogger.getErrors());
        }

        return errorMap;
    }

    private static List<OutputProfileList> analyzeOutputProfileLists(Path rootPath,
                                                                    AssetMap assetMap,
                                                                    PackingList packingList,
                                                                    List<IMFCompositionPlaylist> IMFCompositionPlaylistList,
                                                                    IMFErrorLogger packingListErrorLogger,
                                                                    Map<String, List<ErrorLogger.ErrorObject>> errorMap) throws IOException {

        List<OutputProfileList> outputProfileListTypeList = new ArrayList<>();

        for (PackingList.Asset asset : packingList.getAssets()) {
            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                URI path = assetMap.getPath(asset.getUUID());
                if (path == null) {

                    // todo: pkl/am errors are already reported in analyzePackage(), needs cleanup

                    //packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                    //      IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                    continue;
                }

                Path assetPath = rootPath.resolve(assetMap.getPath(asset.getUUID()).toString());
                if(!Files.isRegularFile(assetPath)) {

                    // todo: pkl/am errors are already reported in analyzePackage(), needs cleanup

                    //packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                    //        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Cannot find asset with path %s ID = %s", assetPath.toString(), asset.getUUID().toString()));
                    continue;
                }

                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetPath);
                if (OutputProfileList.isOutputProfileList(resourceByteRangeProvider)) {
                    IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
                    try {
                        OutputProfileList outputProfileListType = OutputProfileList.getOutputProfileListType(resourceByteRangeProvider, imfErrorLogger);
                        if (outputProfileListType == null) {
                            continue;
                        }

                        if (!outputProfileListType.getId().equals(asset.getUUID())) {
                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the OPL asset
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the OPL is not same as UUID %s of the OPL in the AssetMap", outputProfileListType.getId().toString(), asset.getUUID().toString()));
                        }
                        outputProfileListTypeList.add(outputProfileListType);

                        Optional<IMFCompositionPlaylist> optional = IMFCompositionPlaylistList.stream().filter(e -> e.getUUID().equals(outputProfileListType.getCompositionPlaylistId())).findAny();
                        if (!optional.isPresent()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("Failed to get application composition with ID = %s for OutputProfileList with ID %s",
                                            outputProfileListType.getCompositionPlaylistId().toString(), outputProfileListType.getId().toString()));
                            continue;
                        }
                        imfErrorLogger.addAllErrors(outputProfileListType.applyOutputProfileOnComposition(optional.get()));

                    } catch (IMFException e) {
                        imfErrorLogger.addAllErrors(e.getErrors());
                    } finally {
                        errorMap.put(Utilities.getFilenameFromPath(assetPath), imfErrorLogger.getErrors());
                    }
                }
            }
        }

        return outputProfileListTypeList;
   }



    public static List<ErrorLogger.ErrorObject> analyzeFile(Path input) throws IOException {
        return analyzeFile(input, null);
    }


    public static List<ErrorLogger.ErrorObject> analyzeFile(Path input, String namespace) throws IOException {
        IMFErrorLogger errorLogger = new IMFErrorLoggerImpl();

        if (!Files.isRegularFile(input)) {
            errorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("File %s does not exist", input));
            return errorLogger.getErrors();
        }

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(input);

        String fileName = Utilities.getFilenameFromPath(input);
        if(fileName.lastIndexOf('.') > 0) {
            String extension = fileName.substring(fileName.lastIndexOf('.')+1);
            if(extension.equalsIgnoreCase("mxf")) {

                // input file is an MXF file

                // retrieve header partition payload
                // todo: evaluate partitions to ensure reading the complete/final header metadata
                PayloadRecord headerPartitionPayload = MXFUtils.getHeaderPartitionPayloadRecord(resourceByteRangeProvider, errorLogger);
                if (headerPartitionPayload == null) {
                    errorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("Failed to retrieve header partition"));
                    return errorLogger.getErrors();
                }

                // retrieve index table partitions
                List<PayloadRecord> indexSegmentPayloadRecords = MXFUtils.getIndexTablePartitionPayloadRecords(resourceByteRangeProvider, errorLogger);
                if (indexSegmentPayloadRecords.isEmpty()) {
                    errorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("Failed to retrieve index partitions"));
                    return errorLogger.getErrors();
                }

                List<PayloadRecord> essencePartitions = new ArrayList<>();
                essencePartitions.add(headerPartitionPayload);
                essencePartitions.addAll(indexSegmentPayloadRecords);


                // todo: could guestimate namespace based on essence type, if none is provided

                // validate essence partitions
                errorLogger.addAllErrors(IMPValidator.validateEssencePartitions(essencePartitions, namespace));

                return errorLogger.getErrors();
            }
        }

        // input file is not an MXF file

        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.Unknown, 0L, resourceByteRangeProvider.getResourceSize());
        PayloadRecord.PayloadAssetType payloadAssetType = IMPValidator.getPayloadType(payloadRecord);
        payloadRecord = new PayloadRecord(bytes, payloadAssetType, 0L, resourceByteRangeProvider.getResourceSize());

        switch (payloadAssetType) {
            case PackingList:
                errorLogger.addAllErrors(validatePKL(payloadRecord));
                break;
            case AssetMap:
                errorLogger.addAllErrors(validateAssetMap(payloadRecord));
                break;
            case CompositionPlaylist:
                errorLogger.addAllErrors(validateCPL(payloadRecord));
                break;
            case OutputProfileList:
                errorLogger.addAllErrors(validateOPL(payloadRecord));
                break;
            default:
                errorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Unknown AssetType"));
        }

        return errorLogger.getErrors();
    }


    private static String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <package_directory>%n", IMPAnalyzer.class.getName()));
        sb.append(String.format("%s <cpl_file>%n", IMPAnalyzer.class.getName()));
        sb.append(String.format("%s <asset_map_file>%n", IMPAnalyzer.class.getName()));
        sb.append(String.format("%s <pkl_file>%n", IMPAnalyzer.class.getName()));
        sb.append(String.format("%s <mxf_file>%n", IMPAnalyzer.class.getName()));
        return sb.toString();
    }


    private static void logErrors(String file, List<ErrorLogger.ErrorObject> errorsList)
    {
        if(errorsList.size()>0)
        {
            // deduplicate errors first
            Set<ErrorLogger.ErrorObject> errorsDeduped = new HashSet<>(errorsList);

            long warningCount = errorsDeduped.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("%s has %d errors and %d warnings", file,
                    errorsDeduped.size() - warningCount, warningCount));
            for (ErrorLogger.ErrorObject errorObject : errorsDeduped) {
                if (errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error("\t\t" + errorObject.toString());
                } else if (errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn("\t\t" + errorObject.toString());
                }
            }
        }

        else

        {
            logger.info(String.format("%s has no errors or warnings", file));
        }

    }

    public static void main(String args[]) throws IOException
    {
        if (args.length < 1 || args.length > 2)
        {
            logger.error(usage());
            System.exit(-1);
        }

        String inputFileName = args[0];
        Path input = Utilities.getPathFromString(inputFileName);

        if (Files.isDirectory(input)) {
            logger.info("==========================================================================" );
            logger.info(String.format("Analyzing IMF delivery: %s", inputFileName));
            logger.info("==========================================================================");

            Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzeDelivery(input);
            for(Map.Entry<String, List<ErrorLogger.ErrorObject>> entry: errorMap.entrySet()) {
                logErrors(entry.getKey(), entry.getValue());
            }
        }
        else
        {
            Path filename = input.getFileName();
            String namespace = null;
            if (args.length == 2)
                namespace = args[1];
            if (filename != null) {
                logger.info("==========================================================================\n" );
                logger.info(String.format("Analyzing file: %s", filename.toString()));
                logger.info("==========================================================================\n");
                List<ErrorLogger.ErrorObject>errors = analyzeFile(input, namespace);
                logErrors(filename.toString(), errors);
            }
        }
    }
}
