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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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


    public static Map<String, List<ErrorLogger.ErrorObject>> analyzePackage(Path rootPath) throws IOException {

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
                if (!Files.isRegularFile(assetMapPath)) {
                    assetMapErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("AssetMap file not found: %s.", mapProfileV2MappedFileSet.getAbsoluteAssetMapURI()));
                    errorMap.put(assetMapPath.toString(), assetMapErrorLogger.getErrors());
                    return errorMap;
                }

                AssetMap assetMap = new AssetMap(assetMapPath);
                assetMapErrorLogger.addAllErrors(assetMap.getErrors());

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
                        Map<UUID, PayloadRecord> trackFileIDToHeaderPartitionPayLoadMap = new HashMap<>();
                        int xmlFileCount = 0;
                        for (PackingList.Asset asset : packingList.getAssets()) {
                            if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
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
                                            String.format("Packing List references Asset with ID = %s, but not found: %s.", asset.getUUID().toString(), assetPath));
                                    continue;
                                }

                                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetPath);
                                IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

                                try {
                                    PayloadRecord headerPartitionPayloadRecord = MXFUtils.getHeaderPartitionPayloadRecord(resourceByteRangeProvider, trackFileErrorLogger);
                                    if (headerPartitionPayloadRecord == null) {
                                        trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                                String.format("Failed to retrieve header partition for %s", assetPath.toString()));
                                        continue;
                                    }

                                    // add header payload into UUID->Payload map
                                    UUID trackFileID = MXFUtils.getTrackFileId(headerPartitionPayloadRecord, trackFileErrorLogger);
                                    if (trackFileID != null) {
                                        trackFileIDToHeaderPartitionPayLoadMap.put(trackFileID, headerPartitionPayloadRecord);
                                        if (!trackFileID.equals(asset.getUUID())) {
                                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the track file asset
                                            trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the MXF file is not same as UUID %s of the MXF file in the AssetMap", trackFileID.toString(), asset.getUUID().toString()));
                                        }
                                    }

                                    List<PayloadRecord> indexTablePartitionPayloadRecords = MXFUtils.getIndexTablePartitionPayloadRecords(resourceByteRangeProvider, trackFileErrorLogger);
                                    if (indexTablePartitionPayloadRecords.isEmpty()) {
                                        trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                                String.format("Failed to retrieve index partition for %s", assetPath.toString()));
                                        continue;
                                    }


                                    List<PayloadRecord> headerPartitionPayloadRecords = new ArrayList<>();
                                    headerPartitionPayloadRecords.add(headerPartitionPayloadRecord);

                                    // run essence partition validation
                                    trackFileErrorLogger.addAllErrors(IMPValidator.validateEssencePartitions(headerPartitionPayloadRecords, indexTablePartitionPayloadRecords));

                                } catch( MXFException e) {
                                    trackFileErrorLogger.addAllErrors(e.getErrors());
                                }
                                catch( IMFException e) {
                                    trackFileErrorLogger.addAllErrors(e.getErrors());
                                }
                                finally {
                                    Path filename = assetPath.getFileName();
                                    if (filename != null) {
                                        errorMap.put(filename.toString(), trackFileErrorLogger.getErrors());
                                    }
                                }
                            } else if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                                xmlFileCount++;
                            }
                        }

                        // issue a warning if the PKL does not contain any assets of type text/xml to help troubleshoot non-compliant mime-types for CPL/OPL
                        if( xmlFileCount == 0) {
                            packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("Packing List does not contain any assets of type \"%s\" (i.e. PKL does not contain any CPL/OPL files).", PackingList.Asset.TEXT_XML_TYPE));
                        }

                        List<IMFCompositionPlaylist> imfCompositionPlaylistList = analyzeApplicationCompositions( rootPath, assetMap, packingList, packingListErrorLogger, errorMap, trackFileIDToHeaderPartitionPayLoadMap);
                        analyzeOutputProfileLists( rootPath, assetMap, packingList, imfCompositionPlaylistList, packingListErrorLogger, errorMap);

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
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                    continue;
                }

                Path assetPath = rootPath.resolve(assetMap.getPath(asset.getUUID()).toString());
                if(!Files.isRegularFile(assetPath)) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Cannot find asset with path %s ID = %s", assetPath.toString(), asset.getUUID().toString()));
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



    private static List<IMFCompositionPlaylist> analyzeApplicationCompositions(Path rootFile,
                                                                              AssetMap assetMap,
                                                                              PackingList packingList,
                                                                              IMFErrorLogger packingListErrorLogger,
                                                                              Map<String, List<ErrorLogger.ErrorObject>> errorMap,
                                                                              Map<UUID, PayloadRecord> trackFileIDToHeaderPartitionPayLoadMap) throws IOException {
        List<IMFCompositionPlaylist> IMFCompositionPlaylistList = new ArrayList<>();
        
        for (PackingList.Asset asset : packingList.getAssets()) {
            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                URI path = assetMap.getPath(asset.getUUID());
                if (path == null) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                    continue;
                }

                Path assetPath = rootFile.resolve(path.toString());
                if (!Files.isRegularFile(assetPath)) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Cannot find asset with path %s ID = %s", assetPath.toString(), asset.getUUID().toString()));
                    continue;
                }

                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetPath);
                if (IMFCompositionPlaylist.isCompositionPlaylist(resourceByteRangeProvider)) {
                    IMFErrorLogger compositionErrorLogger = new IMFErrorLoggerImpl();
                    IMFErrorLogger compositionConformanceErrorLogger = new IMFErrorLoggerImpl();

                    try {

                        // instantiate IMFCompositionPlaylist
                        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(resourceByteRangeProvider);
                        compositionErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
                        if (compositionErrorLogger.hasFatalErrors()) {
                            continue;
                        }

                        // ensure Composition ID matches the one stated in the AssetMap
                        if (!imfCompositionPlaylist.getUUID().equals(asset.getUUID())) {
                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the CPL asset
                            compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the CPL is not same as UUID %s of the CPL in the AssetMap", imfCompositionPlaylist.getUUID().toString(), asset.getUUID().toString()));
                        }

                        // validate IMFCompositionPlaylist
                        compositionErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist));
                        if (compositionErrorLogger.hasFatalErrors()) {
                            continue;
                        }

                        // add IMFCompositionPlaylist to return List since no FATAL errors were encountered
                        IMFCompositionPlaylistList.add(imfCompositionPlaylist);
                        Set<UUID> trackFileIDsSet = trackFileIDToHeaderPartitionPayLoadMap.keySet();

                        try {
                            List<PayloadRecord> cplHeaderPartitionPayloads = imfCompositionPlaylist.getEssenceVirtualTracks()
                                    .stream()
                                    .map(IMFEssenceComponentVirtualTrack::getTrackResourceIds)
                                    .flatMap(Set::stream)
                                    .map(e -> trackFileIDToHeaderPartitionPayLoadMap.get(e))
                                    .collect(Collectors.toList());
                            compositionConformanceErrorLogger.addAllErrors(IMPValidator.conformVirtualTracksInComposition(imfCompositionPlaylist, cplHeaderPartitionPayloads));
                        } catch (IMFException e) {
                            compositionConformanceErrorLogger.addAllErrors(e.getErrors());
                        } finally {
                            errorMap.put(Utilities.getFilenameFromPath(assetPath) + " " + CONFORMANCE_LOGGER_PREFIX, compositionConformanceErrorLogger.getErrors());
                        }
                    } catch (IMFException e) {
                        compositionErrorLogger.addAllErrors(e.getErrors());
                    } finally {
                        errorMap.put(Utilities.getFilenameFromPath(assetPath), compositionErrorLogger.getErrors());
                    }
                }
            }
        }

        return IMFCompositionPlaylistList;
    }


    public static List<ErrorLogger.ErrorObject> analyzeFile(Path input) throws IOException {
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

                List<PayloadRecord> headerPartitionPayloadRecords = new ArrayList<>();
                headerPartitionPayloadRecords.add(headerPartitionPayload);

                // validate essence partitions
                errorLogger.addAllErrors(IMPValidator.validateEssencePartitions(headerPartitionPayloadRecords, indexSegmentPayloadRecords));

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


    private static void logErrors(String file, List<ErrorLogger.ErrorObject> errors)
    {
        if(errors.size()>0)

        {
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("%s has %d errors and %d warnings", file,
                    errors.size() - warningCount, warningCount));
            for (ErrorLogger.ErrorObject errorObject : errors) {
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
        if (args.length != 1)
        {
            logger.error(usage());
            System.exit(-1);
        }

        String inputFileName = args[0];
        Path input = Utilities.getPathFromString(inputFileName);

        if (Files.isDirectory(input)) {
            logger.info("==========================================================================" );
            logger.info(String.format("Analyzing IMF package %s", inputFileName));
            logger.info("==========================================================================");

            Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(input);
            for(Map.Entry<String, List<ErrorLogger.ErrorObject>> entry: errorMap.entrySet()) {
                if(!entry.getKey().contains(CONFORMANCE_LOGGER_PREFIX)) {
                    logErrors(entry.getKey(), entry.getValue());
                }
            }

            logger.info("\n\n\n");
            logger.info("==========================================================================" );
            logger.info("Virtual Track Conformance" );
            logger.info("==========================================================================");


            for(Map.Entry<String, List<ErrorLogger.ErrorObject>> entry: errorMap.entrySet()) {
                if(entry.getKey().contains(CONFORMANCE_LOGGER_PREFIX)) {
                    logErrors(entry.getKey(), entry.getValue());
                }
            }
        }
        else
        {
            Path filename = input.getFileName();
            if (filename != null) {
                logger.info("==========================================================================\n" );
                logger.info(String.format("Analyzing path %s", filename.toString()));
                logger.info("==========================================================================\n");
                List<ErrorLogger.ErrorObject>errors = analyzeFile(input);
                logErrors(filename.toString(), errors);
            }
        }
    }
}
