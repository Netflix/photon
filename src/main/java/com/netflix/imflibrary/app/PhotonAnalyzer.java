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
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.*;

/**
 * Created by svenkatrav on 9/2/16.
 */
public class PhotonAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(PhotonAnalyzer.class);

    private static Set<UUID> getTrackFileIds(List<PayloadRecord> headerPartitionPayloadRecords) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Set<UUID> trackFileIDsSet = new HashSet<>();

        for (PayloadRecord payloadRecord : headerPartitionPayloadRecords) {
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(),
                                PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                continue;
            }
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long) payloadRecord.getPayload().length,
                    imfErrorLogger);

            try {
                /**
                 * Add the Top Level Package UUID to the set of TrackFileIDs, this is required to validate that the essences header partition that were passed in
                 * are in fact from the constituent resources of the VirtualTack
                 */
                File workingDirectory = Files.createTempDirectory(null).toFile();
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                trackFileIDsSet.add(packageUUID);
                trackFileIDsSet.add(packageUUID);
            } catch (IMFException e) {
                imfErrorLogger.addAllErrors(e.getErrors());
            } catch (MXFException e) {
                imfErrorLogger.addAllErrors(e.getErrors());
            }
        }

        if (imfErrorLogger.hasFatalErrors()) {
            return new HashSet<>();
        }

        return Collections.unmodifiableSet(trackFileIDsSet);

    }


    private static Boolean isVirtualTrackComplete(IMFEssenceComponentVirtualTrack virtualTrack, Set<UUID> trackFileIDsSet) throws IOException {

        for (UUID uuid : virtualTrack.getTrackResourceIds()) {
            if (!trackFileIDsSet.contains(uuid)) {
                return false;
            }
        }

        return true;

    }

    private static Boolean isCompositionComplete(Composition composition, Set<UUID> trackFileIDsSet) throws IOException {

        for (IMFEssenceComponentVirtualTrack virtualTrack : composition.getEssenceVirtualTracks()) {
            for (UUID uuid : virtualTrack.getTrackResourceIds()) {
                if (!trackFileIDsSet.contains(uuid)) {
                    return false;
                }
            }
        }

        return true;

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
                            File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());
                            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);

                            if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
                                IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

                                PayloadRecord headerPartitionPayload = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, trackFileErrorLogger);
                                if(headerPartitionPayload == null) {
                                    trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_HEADER_PARTITION_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                            String.format("Failed to get header partition for %s", assetFile.getPath()));
                                }
                                else {
                                    List<PayloadRecord> payloadRecords = new ArrayList<>();
                                    payloadRecords.add(headerPartitionPayload);
                                    trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(payloadRecords));
                                }
                                errorMap.put(assetFile.getName(), trackFileErrorLogger.getErrors());
                                if (!trackFileErrorLogger.hasFatalErrors()) {
                                    headerPartitionPayloadRecords.add(headerPartitionPayload);
                                }
                            }
                        }

                        Set<UUID> trackFileIDsSet = getTrackFileIds(headerPartitionPayloadRecords);

                        for (PackingList.Asset asset : packingList.getAssets()) {
                            File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());
                            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);
                            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE) &&
                                    Composition.isCompositionPlaylist(resourceByteRangeProvider)) {
                                IMFErrorLogger compositionErrorLogger = new IMFErrorLoggerImpl();

                                PayloadRecord cplPayloadRecord = new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1),
                                        PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

                                try {
                                    Composition composition = new Composition(resourceByteRangeProvider);
                                    compositionErrorLogger.addAllErrors(composition.getErrors());

                                    if (!isCompositionComplete(composition, trackFileIDsSet)) {
                                        compositionErrorLogger.addAllErrors(IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, headerPartitionPayloadRecords, false));
                                        for (IMFEssenceComponentVirtualTrack virtualTrack : composition.getEssenceVirtualTracks()) {
                                            if (isVirtualTrackComplete(virtualTrack, trackFileIDsSet)) {
                                                compositionErrorLogger.addAllErrors(IMPValidator.isVirtualTrackInCPLConformed(cplPayloadRecord, virtualTrack, headerPartitionPayloadRecords));
                                            }
                                        }
                                    } else {
                                        compositionErrorLogger.addAllErrors(IMPValidator.areAllVirtualTracksInCPLConformed(cplPayloadRecord, headerPartitionPayloadRecords));
                                    }
                                } catch (IMFException e) {
                                    compositionErrorLogger.addAllErrors(e.getErrors());
                                } finally {
                                    errorMap.put(assetFile.getName(), compositionErrorLogger.getErrors());
                                }
                            }
                        }
                    } catch (IMFException e) {
                        packingListErrorLogger.addAllErrors(e.getErrors());
                        errorMap.put(packingListAsset.getPath().toString(), packingListErrorLogger.getErrors());

                    }
                }
            } catch (IMFException e) {
                assetMapErrorLogger.addAllErrors(e.getErrors());
            }
            errorMap.put(BasicMapProfileV2MappedFileSet.ASSETMAP_FILE_NAME, assetMapErrorLogger.getErrors());
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
                trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_HEADER_PARTITION_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Failed to get header partition"));
            }
            else {
                List<PayloadRecord> payloadRecords = new ArrayList<>();
                payloadRecords.add(headerPartitionPayload);
                trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(payloadRecords));
            }

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
        sb.append(String.format("%s <inputFilePath>%n", PhotonAnalyzer.class.getName()));
        return sb.toString();
    }


    private static void logErrros(String file, List<ErrorLogger.ErrorObject> errors)
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
            throw new IllegalArgumentException("Invalid parameters");
        }

        String inputFileName = args[0];
        File inputFile = new File(inputFileName);
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }

        if(inputFile.isDirectory()) {
            logger.info(String.format("Analyzing IMF package %s", inputFile.getName()));
            Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
            for(Map.Entry<String, List<ErrorLogger.ErrorObject>> entry: errorMap.entrySet()) {
                logErrros(entry.getKey(), entry.getValue());
            }
        }
        else
        {
            logger.info(String.format("Analyzing file %s", inputFile.getName()));
            List<ErrorLogger.ErrorObject>errors = analyzeFile(inputFile);
            logErrros(inputFile.getName(), errors);
        }
    }
}
