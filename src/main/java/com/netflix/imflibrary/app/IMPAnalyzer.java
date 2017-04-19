package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateAssetMap;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateCPL;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validatePKL;

/**
 * Created by svenkatrav on 9/2/16.
 */
public class IMPAnalyzer {

    private static final String CONFORMANCE_LOGGER_PREFIX = "Virtual Track Conformance";
    private static final Logger logger = LoggerFactory.getLogger(IMPAnalyzer.class);

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

            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                0L,
                (long) payloadRecord.getPayload().length,
                imfErrorLogger);

            Preface preface = headerPartition.getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage) genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
            trackFileIDMap.put(packageUUID, payloadRecord);
        }

        return Collections.unmodifiableMap(trackFileIDMap);
    }


    private static Boolean isVirtualTrackComplete(IMFEssenceComponentVirtualTrack virtualTrack, Set<UUID> trackFileIDsSet) throws IOException {

        for (UUID uuid : virtualTrack.getTrackResourceIds()) {
            if (!trackFileIDsSet.contains(uuid)) {
                return false;
            }
        }

        return true;

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

    private static PartitionPack getPartitionPack(ResourceByteRangeProvider resourceByteRangeProvider, long resourceOffset) throws IOException
    {
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        KLVPacket.Header header;
        {//logic to provide as an input stream the portion of the archive that contains PartitionPack KLVPacker Header
            long rangeStart = resourceOffset;
            long rangeEnd = resourceOffset +
                    (KLVPacket.KEY_FIELD_SIZE + KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE) -1;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            byte[] bytesWithPartitionPackKLVPacketHeader = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            ByteProvider imfEssenceComponentByteProvider = new ByteArrayDataProvider(bytesWithPartitionPackKLVPacketHeader);
            header = new KLVPacket.Header(imfEssenceComponentByteProvider, rangeStart);
        }

        PartitionPack partitionPack;
        long rangeStart = resourceOffset;
        long rangeEnd = resourceOffset + header.getKLSize() + header.getVSize() - 1;
        rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

        byte[] partitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        partitionPack = new PartitionPack(new ByteArrayDataProvider(partitionBytes));

        return partitionPack;
    }

    private static List<PayloadRecord> getIndexTablePartitionPayloadRecords(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        List<PayloadRecord> payloadRecords = new ArrayList<>();
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        if(rangeStart < 0 ) {
            return payloadRecords;
        }
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = IMPValidator.getRandomIndexPackSize(payloadRecord);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;
        if(rangeStart < 0 ) {
            return payloadRecords;
        }

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = new ArrayList<>();
        partitionByteOffsets.addAll(IMPValidator.getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize));
        partitionByteOffsets.add(resourceByteRangeProvider.getResourceSize());

        for(int i =0; i < partitionByteOffsets.size() -1; i++) {
            rangeStart = partitionByteOffsets.get(i);
            rangeEnd = partitionByteOffsets.get(i+1) - 1;
            PartitionPack partitionPack = getPartitionPack(resourceByteRangeProvider, rangeStart);
            //2067-5 section 5.1.1
            if (partitionPack.hasEssenceContainer() && partitionPack.hasIndexTableSegments()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Partition %d has both index table and essence", i));
            }
            else if (partitionPack.hasIndexTableSegments()) {
                byte[] partitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
                PayloadRecord partitionPayloadRecord = new PayloadRecord(partitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
                payloadRecords.add(partitionPayloadRecord);
            }
        }
        return payloadRecords;

    }

    private static List<ErrorLogger.ErrorObject> conformVirtualTrack(ResourceByteRangeProvider cplByteRangeProvider, Composition.VirtualTrack virtualTrack, List<PayloadRecord>
            headerPartitionPayloadRecords) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        byte[] bytes = cplByteRangeProvider.getByteRangeAsBytes(0, cplByteRangeProvider.getResourceSize() - 1);
        PayloadRecord cplPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, cplByteRangeProvider.getResourceSize());

        imfErrorLogger.addAllErrors(IMPValidator.isVirtualTrackInCPLConformed(cplPayloadRecord, virtualTrack, headerPartitionPayloadRecords));

        return imfErrorLogger.getErrors();
    }

    public static Map<String, List<ErrorLogger.ErrorObject>> analyzePackage(File rootFile) throws IOException {
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> headerPartitionPayloadRecords = new ArrayList<>();
        try {
            BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(rootFile);
            imfErrorLogger.addAllErrors(mapProfileV2MappedFileSet.getErrors());
            IMFErrorLogger assetMapErrorLogger = new IMFErrorLoggerImpl();

            try {

                AssetMap assetMap = new AssetMap(new File(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI()));
                assetMapErrorLogger.addAllErrors(assetMap.getErrors());


                for (AssetMap.Asset packingListAsset : assetMap.getPackingListAssets()) {
                    IMFErrorLogger packingListErrorLogger = new IMFErrorLoggerImpl();
                    try {
                        PackingList packingList = new PackingList(new File(rootFile, packingListAsset.getPath().toString()));
                        packingListErrorLogger.addAllErrors(packingList.getErrors());

                        for (PackingList.Asset asset : packingList.getAssets()) {
                            if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
                                URI path = assetMap.getPath(asset.getUUID());
                                if( path == null) {
                                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                                    continue;
                                }
                                File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());
                                if(!assetFile.exists()) {
                                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Cannot find asset with path %s ID = %s", assetFile.getAbsolutePath(), asset.getUUID().toString
                                                    ()));
                                    continue;
                                }

                                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);

                                IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

                                try {
                                    PayloadRecord headerPartitionPayloadRecord = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, trackFileErrorLogger);
                                    if (headerPartitionPayloadRecord == null) {
                                        trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                                String.format("Failed to get header partition for %s", assetFile.getPath()));
                                    } else {
                                        List<PayloadRecord> payloadRecords = new ArrayList<>();
                                        payloadRecords.add(headerPartitionPayloadRecord);
                                        trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(payloadRecords));
                                        headerPartitionPayloadRecords.add(headerPartitionPayloadRecord);
                                    }
                                    List<PayloadRecord>  payloadRecords = getIndexTablePartitionPayloadRecords(resourceByteRangeProvider, trackFileErrorLogger);
                                    trackFileErrorLogger.addAllErrors(IMPValidator.validateIndexTableSegments(payloadRecords));
                                } catch( MXFException e) {
                                    trackFileErrorLogger.addAllErrors(e.getErrors());
                                }
                                catch( IMFException e) {
                                    trackFileErrorLogger.addAllErrors(e.getErrors());
                                }
                                finally {
                                    errorMap.put(assetFile.getName(), trackFileErrorLogger.getErrors());
                                }
                            }
                        }

                        Map<UUID, PayloadRecord> trackFileIDToHeaderPartitionPayLoadMap =
                                getTrackFileIdToHeaderPartitionPayLoadMap(headerPartitionPayloadRecords);

                        for (PackingList.Asset asset : packingList.getAssets()) {
                            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                                URI path = assetMap.getPath(asset.getUUID());
                                if( path == null) {
                                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                                    continue;
                                }
                                File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());

                                if(!assetFile.exists()) {
                                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Cannot find asset with path %s ID = %s", assetFile.getAbsolutePath(), asset.getUUID().toString()));
                                    continue;
                                }

                                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);
                                if (ApplicationComposition.isCompositionPlaylist(resourceByteRangeProvider)) {
                                    IMFErrorLogger compositionErrorLogger = new IMFErrorLoggerImpl();
                                    IMFErrorLogger compositionConformanceErrorLogger = new IMFErrorLoggerImpl();
                                    PayloadRecord cplPayloadRecord = new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1),
                                            PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

                                    try {
                                        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, compositionErrorLogger);
                                        if(applicationComposition == null) {
                                            continue;
                                        }
                                        Set<UUID> trackFileIDsSet = trackFileIDToHeaderPartitionPayLoadMap
                                                .keySet();

                                        try {
                                            if (!isCompositionComplete(applicationComposition, trackFileIDsSet, compositionConformanceErrorLogger)) {
                                                for (IMFEssenceComponentVirtualTrack virtualTrack : applicationComposition.getEssenceVirtualTracks()) {
                                                    Set<UUID> trackFileIds = virtualTrack.getTrackResourceIds();
                                                    List<PayloadRecord> trackHeaderPartitionPayloads = new ArrayList<>();
                                                    for (UUID trackFileId : trackFileIds) {
                                                        if (trackFileIDToHeaderPartitionPayLoadMap.containsKey(trackFileId))
                                                            trackHeaderPartitionPayloads.add
                                                                    (trackFileIDToHeaderPartitionPayLoadMap.get(trackFileId));
                                                    }

                                                    if (isVirtualTrackComplete(virtualTrack, trackFileIDsSet)) {
                                                        compositionConformanceErrorLogger.addAllErrors(IMPValidator.isVirtualTrackInCPLConformed(cplPayloadRecord, virtualTrack, trackHeaderPartitionPayloads));
                                                    } else if (trackHeaderPartitionPayloads.size() != 0) {
                                                        compositionConformanceErrorLogger.addAllErrors(IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, trackHeaderPartitionPayloads, false));
                                                    }
                                                }
                                            } else {
                                                compositionConformanceErrorLogger.addAllErrors(IMPValidator.areAllVirtualTracksInCPLConformed(cplPayloadRecord, headerPartitionPayloadRecords));
                                            }
                                        } catch (IMFException e) {
                                            compositionConformanceErrorLogger.addAllErrors(e.getErrors());
                                        } finally {
                                            errorMap.put(assetFile.getName() + " " + CONFORMANCE_LOGGER_PREFIX, compositionConformanceErrorLogger.getErrors());
                                        }
                                    } catch (IMFException e) {
                                        compositionErrorLogger.addAllErrors(e.getErrors());
                                    } finally {
                                        errorMap.put(assetFile.getName(), compositionErrorLogger.getErrors());
                                    }
                                }
                            }
                        }
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
            errorMap.put(rootFile.getName(), imfErrorLogger.getErrors());
        }


        return errorMap;
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

        // Validate index table segments
        List<PayloadRecord>  payloadRecords = getIndexTablePartitionPayloadRecords(resourceByteRangeProvider, trackFileErrorLogger);
        trackFileErrorLogger.addAllErrors(IMPValidator.validateIndexTableSegments(payloadRecords));

        return trackFileErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> analyzeFile(File inputFile) throws IOException {
        IMFErrorLogger errorLogger = new IMFErrorLoggerImpl();

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);

        if(inputFile.getName().lastIndexOf('.') > 0) {
            String extension = inputFile.getName().substring(inputFile.getName().lastIndexOf('.')+1);
            if(extension.equalsIgnoreCase("mxf")) {
                errorLogger.addAllErrors(validateEssencePartition(resourceByteRangeProvider));
                return errorLogger.getErrors();
            }
        }

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
            default:
                errorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Unknown  AssetType"));
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
        File inputFile = new File(inputFileName);
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }

        if(inputFile.isDirectory()) {
            logger.info("==========================================================================" );
            logger.info(String.format("Analyzing IMF package %s", inputFile.getName()));
            logger.info("==========================================================================");

            Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
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
            logger.info("==========================================================================\n" );
            logger.info(String.format("Analyzing file %s", inputFile.getName()));
            logger.info("==========================================================================\n");
            List<ErrorLogger.ErrorObject>errors = analyzeFile(inputFile);
            logErrors(inputFile.getName(), errors);
        }
    }
}
