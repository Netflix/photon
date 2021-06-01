package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateAssetMap;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateCPL;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validateOPL;
import static com.netflix.imflibrary.RESTfulInterfaces.IMPValidator.validatePKL;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderOrFooterPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.st2067_100.OutputProfileList;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Locator;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by svenkatrav on 9/2/16.
 */
public class IMPAnalyzer {

    private static final String CONFORMANCE_LOGGER_PREFIX = "Virtual Track Conformance";
    private static final Logger logger = LoggerFactory.getLogger(IMPAnalyzer.class);

    private static UUID getTrackFileId(PayloadRecord payloadRecord, IMFErrorLogger imfErrorLogger) throws
            IOException {

        if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(),
                            PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            return null;
        }

        HeaderOrFooterPartition headerOrFooterPartition = new HeaderOrFooterPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                0L,
                (long) payloadRecord.getPayload().length,
                imfErrorLogger, false);

        Preface preface = headerOrFooterPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage) genericPackage;
        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
        return packageUUID;
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
        boolean bComplete = true;
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

    private static List<Long> getPartitionByteOffsets(ResourceByteRangeProvider resourceByteRangeProvider, boolean addRIPStart) throws IOException {
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
        List<Long> partitionByteOffsets = new ArrayList<>();
        partitionByteOffsets.addAll(IMPValidator.getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize));
        if (addRIPStart) {
            partitionByteOffsets.add(rangeStart);
        }
        return partitionByteOffsets;
    }

    @Nullable
    private static PayloadRecord getFooterPartitionPayloadRecord(ResourceByteRangeProvider resourceByteRangeProvider, List<Long> partitionByteOffsets, IMFErrorLogger trackFileErrorLogger) throws IOException {
        if (partitionByteOffsets.size() >= 2) {
            long rangeStart = partitionByteOffsets.get(partitionByteOffsets.size()-2);
            long rangeEnd = partitionByteOffsets.get(partitionByteOffsets.size()-1) - 1;

            byte[] headerPartitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            final PartitionPack pp = new PartitionPack(new ByteArrayDataProvider(headerPartitionBytes), 0L, false, null);
            if (pp.isFooterPartition() && pp.isValidFooterPartition() && pp.getHeaderByteCount() > 0) {
                if (pp.getPartitionStatus() != PartitionPack.PartitionStatus.ClosedComplete && pp.getPartitionStatus() != PartitionPack.PartitionStatus.OpenComplete) {
                    trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Neither the Header or last Footer Partition in the MXF file is complete, meta data could be incomplete."));
                }
                return new PayloadRecord(headerPartitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeStart + pp.getHeaderByteCount());
            }
        }
        return null;
    }

    @Nullable
    private static PayloadRecord getHeaderPartitionPayloadRecord(ResourceByteRangeProvider resourceByteRangeProvider, List<Long> partitionByteOffsets) throws IOException {
        if (partitionByteOffsets.size() >= 2) {
            long rangeStart = partitionByteOffsets.get(0);
            long rangeEnd = partitionByteOffsets.get(1) - 1;

            byte[] headerPartitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            return new PayloadRecord(headerPartitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
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
        List<Long> partitionByteOffsets = getPartitionByteOffsets(resourceByteRangeProvider, false);
        partitionByteOffsets.add(resourceByteRangeProvider.getResourceSize());

        for(int i =0; i < partitionByteOffsets.size() -1; i++) {
            long rangeStart = partitionByteOffsets.get(i);
            long rangeEnd = partitionByteOffsets.get(i+1) - 1;
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

    public static Map<String, List<ErrorLogger.ErrorObject>> analyzePackage(Locator rootFile) throws IOException {
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = new HashMap<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try {
            BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(rootFile);
            imfErrorLogger.addAllErrors(mapProfileV2MappedFileSet.getErrors());
            IMFErrorLogger assetMapErrorLogger = new IMFErrorLoggerImpl();

            try {

                AssetMap assetMap = new AssetMap(Locator.of(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI(), rootFile.getConfiguration()));
                assetMapErrorLogger.addAllErrors(assetMap.getErrors());


                for (AssetMap.Asset packingListAsset : assetMap.getPackingListAssets()) {
                    IMFErrorLogger packingListErrorLogger = new IMFErrorLoggerImpl();
                    try {
                        PackingList packingList = new PackingList(rootFile.getChild(packingListAsset.getPath().toString()));
                        packingListErrorLogger.addAllErrors(packingList.getErrors());
                        Map<UUID, PayloadRecord> trackFileIDToPartitionPayLoadMap = new HashMap<>();
                        for (PackingList.Asset asset : packingList.getAssets()) {
                            if (asset.getType().equals(PackingList.Asset.APPLICATION_MXF_TYPE)) {
                                URI path = assetMap.getPath(asset.getUUID());
                                if( path == null) {
                                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                                    continue;
                                }
                                Locator assetFile = rootFile.getChild(assetMap.getPath(asset.getUUID()).toString());
                                if(!assetFile.exists()) {
                                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Cannot find asset with path %s ID = %s", assetFile.getAbsolutePath(), asset.getUUID().toString
                                                    ()));
                                    continue;
                                }

                                ResourceByteRangeProvider resourceByteRangeProvider = assetFile.getResourceByteRangeProvider();

                                IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

                                try {
                                    final List<Long> partitionByteOffsets = getPartitionByteOffsets(resourceByteRangeProvider, true);
                                    final PayloadRecord headerPartitionPayloadRecord = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, partitionByteOffsets);
                                    if (headerPartitionPayloadRecord == null) {
                                        trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                                String.format("Failed to get header partition for %s", assetFile.getPath()));
                                    } else {
                                        // MXF files allows open header and footers. Check whether the Header is closed. If is not, obtain the last
                                        // footer in the MXF file.
                                        final PartitionPack headerPP = new PartitionPack(new ByteArrayDataProvider(headerPartitionPayloadRecord.getPayload()), 0L, false, null);
                                        final PayloadRecord footerPayloadRecord;
                                        // Check whether the header is complete.
                                        if (headerPP.getPartitionStatus() == PartitionPack.PartitionStatus.OpenIncomplete || headerPP.getPartitionStatus() == PartitionPack.PartitionStatus.ClosedIncomplete) {
                                            footerPayloadRecord = getFooterPartitionPayloadRecord(resourceByteRangeProvider, partitionByteOffsets, trackFileErrorLogger);
                                            if (footerPayloadRecord == null) {
                                                trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The Header Partition in the MXF file is not complete"));
                                            }
                                        } else {
                                            footerPayloadRecord = null;
                                        }
                                        List<PayloadRecord> payloadRecords = new ArrayList<>();
                                        payloadRecords.add(headerPartitionPayloadRecord);
                                        trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(payloadRecords));
                                        UUID trackFileID = getTrackFileId(headerPartitionPayloadRecord, trackFileErrorLogger);
                                        if(trackFileID != null) {
                                            if  (footerPayloadRecord != null) {
                                                trackFileIDToPartitionPayLoadMap.put(trackFileID, footerPayloadRecord);
                                            } else {
                                                trackFileIDToPartitionPayLoadMap.put(trackFileID, headerPartitionPayloadRecord);
                                            }
                                            if (!trackFileID.equals(asset.getUUID())) {
                                                // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the track file asset
                                                trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the MXF file is not same as UUID %s of the MXF file in the AssetMap", trackFileID.toString(), asset.getUUID().toString()));
                                            }
                                        }
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

                        List<ApplicationComposition> applicationCompositionList = analyzeApplicationCompositions( rootFile, assetMap, packingList, packingListErrorLogger, errorMap, trackFileIDToPartitionPayLoadMap);

                        analyzeOutputProfileLists( rootFile, assetMap, packingList, applicationCompositionList, packingListErrorLogger, errorMap);

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
        List<PayloadRecord> headerPartitionPayloadRecords = new ArrayList<>();

        final List<Long> partitionByteOffsets = getPartitionByteOffsets(resourceByteRangeProvider, true);
        final PayloadRecord headerPartitionPayload = getHeaderPartitionPayloadRecord(resourceByteRangeProvider, partitionByteOffsets);
        if(headerPartitionPayload == null) {
            trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Failed to get header partition"));
        }
        else {
            final PartitionPack headerPP = new PartitionPack(new ByteArrayDataProvider(headerPartitionPayload.getPayload()), 0L, false, null);
            final PayloadRecord footerPayloadRecord;
            // Check whether the header is complete.
            if (headerPP.getPartitionStatus() == PartitionPack.PartitionStatus.OpenIncomplete || headerPP.getPartitionStatus() == PartitionPack.PartitionStatus.ClosedIncomplete) {
                footerPayloadRecord = getFooterPartitionPayloadRecord(resourceByteRangeProvider, partitionByteOffsets, trackFileErrorLogger);
                if (footerPayloadRecord == null) {
                    trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The Header Partition in the MXF file is not complete"));
                }
            } else {
                footerPayloadRecord = null;
            }
            headerPartitionPayloadRecords.add(headerPartitionPayload);
            if (footerPayloadRecord != null) {
                headerPartitionPayloadRecords.add(footerPayloadRecord);
            }

            trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(headerPartitionPayloadRecords));
        }

        // Validate index table segments
        List<PayloadRecord>  indexSegmentPayloadRecords = getIndexTablePartitionPayloadRecords(resourceByteRangeProvider, trackFileErrorLogger);
        trackFileErrorLogger.addAllErrors(IMPValidator.validateIndexTableSegments(indexSegmentPayloadRecords));

        trackFileErrorLogger.addAllErrors(IMPValidator.validateIndexEditRate(headerPartitionPayloadRecords, indexSegmentPayloadRecords));

        return trackFileErrorLogger.getErrors();
    }

    public static List<OutputProfileList> analyzeOutputProfileLists(Locator rootFile,
                                                                    AssetMap assetMap,
                                                                    PackingList packingList,
                                                                    List<ApplicationComposition> applicationCompositionList,
                                                                    IMFErrorLogger packingListErrorLogger,
                                                                    Map<String, List<ErrorLogger.ErrorObject>> errorMap) throws IOException {

        List<OutputProfileList> outputProfileListTypeList = new ArrayList<>();

        for (PackingList.Asset asset : packingList.getAssets()) {
            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                URI path = assetMap.getPath(asset.getUUID());
                if( path == null) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                    continue;
                }
                Locator assetFile = rootFile.getChild(assetMap.getPath(asset.getUUID()).toString());

                if(!assetFile.exists()) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Cannot find asset with path %s ID = %s", assetFile.getAbsolutePath(), asset.getUUID().toString()));
                    continue;
                }

                ResourceByteRangeProvider resourceByteRangeProvider = assetFile.getResourceByteRangeProvider();
                if (OutputProfileList.isOutputProfileList(resourceByteRangeProvider)) {
                    IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
                    try {
                        OutputProfileList outputProfileListType = OutputProfileList.getOutputProfileListType(resourceByteRangeProvider, imfErrorLogger);
                        if(outputProfileListType == null) {
                            continue;
                        }

                        if(!outputProfileListType.getId().equals(asset.getUUID())) {
                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the OPL asset
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the OPL is not same as UUID %s of the OPL in the AssetMap", outputProfileListType.getId().toString(), asset.getUUID().toString()));
                        }                        outputProfileListTypeList.add(outputProfileListType);

                        Optional<ApplicationComposition> optional = applicationCompositionList.stream().filter(e -> e.getUUID().equals(outputProfileListType.getCompositionPlaylistId())).findAny();
                        if(!optional.isPresent()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("Failed to get application composition with ID = %s for OutputProfileList with ID %s",
                                            outputProfileListType.getCompositionPlaylistId().toString(), outputProfileListType.getId().toString()));
                            continue;
                        }
                        imfErrorLogger.addAllErrors(outputProfileListType.applyOutputProfileOnComposition(optional.get()));

                    } catch (IMFException e) {
                        imfErrorLogger.addAllErrors(e.getErrors());
                    } finally {
                        errorMap.put(assetFile.getName(), imfErrorLogger.getErrors());
                    }
                }
            }
        }

        return outputProfileListTypeList;
   }


    public static List<ApplicationComposition> analyzeApplicationCompositions(Locator rootFile,
                                                                              AssetMap assetMap,
                                                                              PackingList packingList,
                                                                              IMFErrorLogger packingListErrorLogger,
                                                                              Map<String, List<ErrorLogger.ErrorObject>> errorMap,
                                                                              Map<UUID, PayloadRecord> trackFileIDToPartitionPayLoadMap) throws IOException {
        List<ApplicationComposition> applicationCompositionList = new ArrayList<>();

        for (PackingList.Asset asset : packingList.getAssets()) {
            if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)) {
                URI path = assetMap.getPath(asset.getUUID());
                if( path == null) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Failed to get path for Asset with ID = %s", asset.getUUID().toString()));
                    continue;
                }
                Locator assetFile = rootFile.getChild(assetMap.getPath(asset.getUUID()).toString());

                if(!assetFile.exists()) {
                    packingListErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Cannot find asset with path %s ID = %s", assetFile.getAbsolutePath(), asset.getUUID().toString()));
                    continue;
                }

                ResourceByteRangeProvider resourceByteRangeProvider = assetFile.getResourceByteRangeProvider();
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
                        if(!applicationComposition.getUUID().equals(asset.getUUID())) {
                            // ST 2067-2:2016   7.3.1: The value of the Id element shall be extracted from the asset as specified in Table 19 for the CPL asset
                            compositionErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("UUID %s in the CPL is not same as UUID %s of the CPL in the AssetMap", applicationComposition.getUUID().toString(), asset.getUUID().toString()));
                        }
                        applicationCompositionList.add(applicationComposition);
                        Set<UUID> trackFileIDsSet = trackFileIDToPartitionPayLoadMap
                                .keySet();

                        try {
                            if (!isCompositionComplete(applicationComposition, trackFileIDsSet, compositionConformanceErrorLogger)) {
                                for (IMFEssenceComponentVirtualTrack virtualTrack : applicationComposition.getEssenceVirtualTracks()) {
                                    Set<UUID> trackFileIds = virtualTrack.getTrackResourceIds();
                                    List<PayloadRecord> trackHeaderPartitionPayloads = new ArrayList<>();
                                    for (UUID trackFileId : trackFileIds) {
                                        if (trackFileIDToPartitionPayLoadMap.containsKey(trackFileId))
                                            trackHeaderPartitionPayloads.add
                                                    (trackFileIDToPartitionPayLoadMap.get(trackFileId));
                                    }

                                    if (isVirtualTrackComplete(virtualTrack, trackFileIDsSet)) {
                                        compositionConformanceErrorLogger.addAllErrors(IMPValidator.isVirtualTrackInCPLConformed(cplPayloadRecord, virtualTrack, trackHeaderPartitionPayloads));
                                    } else if (trackHeaderPartitionPayloads.size() != 0) {
                                        compositionConformanceErrorLogger.addAllErrors(IMPValidator.conformVirtualTracksInCPL(cplPayloadRecord, trackHeaderPartitionPayloads, false));
                                    }
                                }
                            } else {
                                List<PayloadRecord> cplPartitionPayloads = applicationComposition.getEssenceVirtualTracks()
                                        .stream()
                                        .map(IMFEssenceComponentVirtualTrack::getTrackResourceIds)
                                        .flatMap(Set::stream)
                                        .map( e -> trackFileIDToPartitionPayLoadMap.get(e))
                                        .collect(Collectors.toList());
                                compositionConformanceErrorLogger.addAllErrors(IMPValidator.areAllVirtualTracksInCPLConformed(cplPayloadRecord, cplPartitionPayloads));
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

        return applicationCompositionList;
    }


    public static List<ErrorLogger.ErrorObject> analyzeFile(Locator inputFile) throws IOException {
        IMFErrorLogger errorLogger = new IMFErrorLoggerImpl();

        ResourceByteRangeProvider resourceByteRangeProvider = inputFile.getResourceByteRangeProvider();

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
            case OutputProfileList:
                errorLogger.addAllErrors(validateOPL(payloadRecord));
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
        sb.append(String.format("%nFor S3, use s3:// URIs, directories should be postfixed with a slash (/)%n"));
        sb.append(String.format("%nOptional S3 parameters%n"));
        sb.append(String.format("--aws.accesskey=<aws_access_key_id>%n"));
        sb.append(String.format("--aws.secretkey=<aws_secret_access_key>%n"));
        sb.append(String.format("--aws.token=<aws_session_token>%n"));
        sb.append(String.format("--aws.profile=<aws profile>%n"));
        sb.append(String.format("--aws.rolearn=<aws role arn>%n"));
        sb.append(String.format("--aws.externalid=<external id>%n"));
        sb.append(String.format("--aws.anonymous%n"));
        sb.append(String.format("--aws.endpoint=<endpoint>%n"));
        sb.append(String.format("%nFor AWS, the role (or account) needs the following rights.%n"));
        sb.append(String.format("s3:ListBucket, s3:GetBucketLocation on the bucket, and%n"));
        sb.append(String.format("s3:GetObject for the keys in that bucket.%n"));
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
        final Locator inputFile = Locator.first(args, t -> {
            if (t != 1) {
                logger.error(usage());
                System.exit(-1);
            }
        });
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
