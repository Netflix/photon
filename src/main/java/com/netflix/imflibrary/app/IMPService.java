package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
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
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.*;

import static spark.Spark.*;

public class IMPService {
    private static final String CONFORMANCE_LOGGER_PREFIX = "Virtual Track Conformance";
    private static final Logger logger = LoggerFactory.getLogger(IMPService.class);

    public static void main(String[] args) {
        post("/analysis", (request, response) -> {
            response.type("application/json");
            try {
                String path = request.queryParams("path");
                return process_analysis(path);
            } catch(IOException e){
                return "unable to open path";
            } catch(Exception e){
                return "bad query or error in process";
            }
        });
    }

    static String process_analysis(String path) throws IOException {
        logger.info("==========================================================================" );
        logger.info(String.format("Analyzing IMF package %s", path));
        logger.info("==========================================================================");

        JSONObject report = new JSONObject();

        File inputFile = new File(path);
        report.put("input_file", inputFile.getName());

        JSONArray files_report = new JSONArray();
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);

        for(Map.Entry<String, List<ErrorLogger.ErrorObject>> entry: errorMap.entrySet()) {
            if(!entry.getKey().contains(CONFORMANCE_LOGGER_PREFIX)) {
                JSONObject errors = format_json_errors(entry.getKey(), entry.getValue());
                files_report.add(errors);
            }
        }

        report.put("files", files_report);

        return report.toJSONString();
    }

    private static JSONObject format_json_errors(String file, List<ErrorLogger.ErrorObject> errors)
    {
        JSONObject report = new JSONObject();
        report.put("filename", file);

        JSONArray json_errors = new JSONArray();
        JSONArray json_warnings = new JSONArray();
        for (ErrorLogger.ErrorObject errorObject : errors) {
            JSONObject error = new JSONObject();
            error.put("code", errorObject.getErrorCode().toString());
            error.put("description", errorObject.getErrorDescription());

            if (errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                json_errors.add(error);
            } else if (errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                json_warnings.add(error);
            }
        }

        report.put("errors", json_errors);
        report.put("warnings", json_warnings);
        return report;
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

    static Map<String, List<ErrorLogger.ErrorObject>> analyzePackage(File rootFile) throws IOException {
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
                                }                                File assetFile = new File(rootFile, assetMap.getPath(asset.getUUID()).toString());
                                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetFile);
                                if (ApplicationComposition.isCompositionPlaylist(resourceByteRangeProvider)) {
                                    IMFErrorLogger compositionErrorLogger = new IMFErrorLoggerImpl();
                                    IMFErrorLogger compositionConformanceErrorLogger = new IMFErrorLoggerImpl();
                                    PayloadRecord cplPayloadRecord = new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1),
                                            PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());

                                    try {
                                        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(resourceByteRangeProvider, compositionErrorLogger);
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
}
