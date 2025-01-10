package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_100.OutputProfileList;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.validation.ConstraintsValidator;
import com.netflix.imflibrary.validation.ConstraintsValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

/**
 * A RESTful interface for validating an IMF Master Package.
 */
public class IMPValidator {

    private static final Logger logger = LoggerFactory.getLogger(IMPValidator.class);

    /**
     * A stateless method that determines if the Asset type of the payload is an IMF AssetMap, Packinglist or Composition
     * @param payloadRecord - a payload record corresponding to the asset whose type needs to be confirmed
     *                      Note: for now this method only supports text/xml documents identified in the PKL
     *                      application/mxf asset types cannot be determined.
     * @return asset type of the payload either one of AssetMap, PackingList or Composition
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static PayloadRecord.PayloadAssetType getPayloadType(PayloadRecord payloadRecord) throws IOException {

        ResourceByteRangeProvider resourceByteRangeProvider = new ByteArrayByteRangeProvider(payloadRecord.getPayload());
        if(AssetMap.isFileOfSupportedSchema(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.AssetMap;
        }
        else if(PackingList.isFileOfSupportedSchema(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.PackingList;
        }
        else if(IMFCompositionPlaylist.isCompositionPlaylist(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.CompositionPlaylist;
        }
        else if(OutputProfileList.isOutputProfileList(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.OutputProfileList;
        }
        return PayloadRecord.PayloadAssetType.Unknown;
    }

    /**
     * A stateless method that will validate an IMF PackingList document
     * @param pkl - a payload record for a Packing List document
     * @return list of error messages encountered while validating a Packing List document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validatePKL(PayloadRecord pkl) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if(pkl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", pkl
                    .getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()), imfErrorLogger);
        }
        try{
            PackingList packingList = new PackingList(new ByteArrayByteRangeProvider(pkl.getPayload()));
            imfErrorLogger.addAllErrors(packingList.getErrors());
        }
        catch (IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method that will validate an IMF AssetMap document
     * @param assetMapPayload - a payload record for an AssetMap document
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateAssetMap(PayloadRecord assetMapPayload) throws IOException {
        if(assetMapPayload.getPayloadAssetType() != PayloadRecord.PayloadAssetType.AssetMap){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMapPayload
                    .getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
        }
        try{
            AssetMap assetMap = new AssetMap(new ByteArrayByteRangeProvider(assetMapPayload.getPayload()));
            return assetMap.getErrors();
        }
        catch(IMFException e)
        {
            return e.getErrors();
        }
    }

    /**
     * A stateless method that will validate IMF AssetMap and PackingList documents for all the data
     * that should be cross referenced by both
     * @param assetMapPayload - a payload record for an AssetMap document
     * @param pklPayloads - a list of payload records for Packing List documents referenced by the AssetMap
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validatePKLAndAssetMap(PayloadRecord assetMapPayload, List<PayloadRecord> pklPayloads) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> packingListPayloadRecords = Collections.unmodifiableList(pklPayloads);

        if(assetMapPayload.getPayloadAssetType() != PayloadRecord.PayloadAssetType.AssetMap){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL,
                    String.format("Payload asset type is %s, expected asset type %s", assetMapPayload
                    .getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
            return imfErrorLogger.getErrors();
        }

        ResourceByteRangeProvider assetMapByteRangeProvider = new ByteArrayByteRangeProvider(assetMapPayload.getPayload());
        AssetMap assetMapObjectModel = null;
        try {
            assetMapObjectModel = new AssetMap(assetMapByteRangeProvider);
            imfErrorLogger.addAllErrors(assetMapObjectModel.getErrors());

            if(assetMapObjectModel.getPackingListAssets().size() == 0){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL, String.format("Asset map should reference at least one PackingList, %d " +
                        "references found", assetMapObjectModel.getPackingListAssets().size()));
            }
        }
        catch( IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        List<ResourceByteRangeProvider> packingLists = new ArrayList<>();
        for(PayloadRecord payloadRecord : packingListPayloadRecords){
            if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels
                        .FATAL, String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
            }
            else {
                packingLists.add(new ByteArrayByteRangeProvider(payloadRecord.getPayload()));
            }
        }

        if(packingLists.size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("At least one PackingList is expected, %d were detected", packingLists.size()));
        }

        if(imfErrorLogger.hasFatalErrors())
        {
            return imfErrorLogger.getErrors();
        }

        List<PackingList> packingListObjectModels = new ArrayList<>();
        for (ResourceByteRangeProvider resourceByteRangeProvider : packingLists) {
            try {
                    PackingList packingList = new PackingList(resourceByteRangeProvider);
                    packingListObjectModels.add(packingList);
                    imfErrorLogger.addAllErrors(packingList.getErrors());
            }
            catch (IMFException e)
            {
                imfErrorLogger.addAllErrors(e.getErrors());
                return imfErrorLogger.getErrors();
            }
        }
        List<UUID> assetUUIDsAssetMapList = new ArrayList<>();
        for(AssetMap.Asset asset : assetMapObjectModel.getAssetList()){
            assetUUIDsAssetMapList.add(asset.getUUID());
        }

/*
        //Sort the UUIDs in the AssetMap
        assetUUIDsAssetMapList.sort(new Comparator<UUID>() {
                                    @Override
                                    public int compare(UUID o1, UUID o2) {
                                        return o1.compareTo(o2);
                                    }
                                });
*/

        /* Collect all the assets in all of the PKLs that are a part of this IMP delivery */
        List<UUID> assetUUIDsPackingList = new ArrayList<>();
        for(PackingList packingList : packingListObjectModels) {
            assetUUIDsPackingList.add(packingList.getUUID());//PKL's UUID is also added to this list since that should be present in the AssetMap
            for (PackingList.Asset asset : packingList.getAssets()) {
                assetUUIDsPackingList.add(asset.getUUID());
            }
        }

/*
        //Sort the UUIDs in the PackingList
        assetUUIDsPackingList.sort(new Comparator<UUID>() {
            @Override
            public int compare(UUID o1, UUID o2) {
                return o1.compareTo(o2);
            }
        });
*/

        /* Check to see if all the Assets referenced in the PKL are also referenced by the Asset Map */
        Set<UUID> assetUUIDsAssetMapSet = new HashSet<>(assetUUIDsAssetMapList);
        Set<UUID> assetUUIDsPKLSet = new HashSet<>(assetUUIDsPackingList);

        StringBuilder unreferencedPKLAssetsUUIDs = new StringBuilder();
        for(UUID uuid : assetUUIDsPKLSet){
            if(!assetUUIDsAssetMapSet.contains(uuid)) {
                unreferencedPKLAssetsUUIDs.append(uuid.toString());
                unreferencedPKLAssetsUUIDs.append(", ");
            }
        }

        if(!unreferencedPKLAssetsUUIDs.toString().isEmpty()){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The following UUID/s %s in the Packing list are not referenced by the AssetMap.", unreferencedPKLAssetsUUIDs.toString()));
            return imfErrorLogger.getErrors();
        }

        /* Check if all the assets in the AssetMap that are supposed to be PKLs have the same UUIDs as the PKLs themselves */
        Set<UUID> packingListAssetsUUIDsSet = new HashSet<>();
        for(AssetMap.Asset asset : assetMapObjectModel.getPackingListAssets()){
            packingListAssetsUUIDsSet.add(asset.getUUID());
        }
        StringBuilder unreferencedPKLUUIDs = new StringBuilder();
        for(PackingList packingList : packingListObjectModels) {
            if (!packingListAssetsUUIDsSet.contains(packingList.getUUID())) {
                unreferencedPKLUUIDs.append(packingList.getUUID());
            }
        }
        if(!unreferencedPKLUUIDs.toString().isEmpty()) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The following Packing lists %s are not referenced in the AssetMap", unreferencedPKLUUIDs.toString()));
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method that will validate an IMF Composition document
     * @param cpl - a payload record for a Composition document
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateCPL(PayloadRecord cpl) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if (cpl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.CompositionPlaylist){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", cpl
                    .getPayloadAssetType(), PayloadRecord.PayloadAssetType.CompositionPlaylist.toString()));
        }

        try {
            IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new ByteArrayByteRangeProvider(cpl.getPayload()));
            imfErrorLogger.addAllErrors(validateComposition(imfCompositionPlaylist, null));
        } catch (IOException e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Unable to parse composition playlist: " + e.getMessage());
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }




    public static List<ErrorLogger.ErrorObject> validateComposition(IMFCompositionPlaylist imfCompositionPlaylist, List<PayloadRecord> headerPartitionPayloads) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /*
            run validations based on application identification, cpl and sequence namespaces:
         */

        Set<String> namespaces = imfCompositionPlaylist.getSequenceNamespaceSet();
        namespaces.addAll(imfCompositionPlaylist.getApplicationIdSet());
        namespaces.add(imfCompositionPlaylist.getCplSchema());

        for (String namespace : namespaces) {
            ConstraintsValidator validator = ConstraintsValidatorFactory.getValidator(namespace);
            if (validator != null) {
                List<ErrorLogger.ErrorObject> cplErrors = validator.validateCompositionConstraints(imfCompositionPlaylist, headerPartitionPayloads);
                imfErrorLogger.addAllErrors(cplErrors);
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Namespace not supported: " + namespace);
            }
        }

        return imfErrorLogger.getErrors();
    }




    /* IMF essence related inspection calls*/

    public static List<ErrorLogger.ErrorObject> validateEssencePartitions(List<PayloadRecord> essencePartitionPayloadRecords, String sequenceNamespace) throws IOException {

        IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

        PayloadRecord headerPartitionPayloadRecord = null;
        List<PayloadRecord> indexSegmentPayloadRecords = new ArrayList<>();

        try {
            for (PayloadRecord payloadRecord : essencePartitionPayloadRecords) {

                // ensure payload time is EssencePartition
                if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                    trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("Unable to validate any essence descriptors: payload asset type is %s, expected asset type %s",
                                    payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                    return trackFileErrorLogger.getErrors();
                }

                // get partition pack to determine partition contents
                PartitionPack partitionPack = new PartitionPack(new ByteArrayDataProvider(payloadRecord.getPayload()));

                // ensure partition constraints are met
                trackFileErrorLogger.addAllErrors(IMFConstraints.checkMXFPartitionPackCompliance(partitionPack));
                if (trackFileErrorLogger.hasFatalErrors()) {
                    return trackFileErrorLogger.getErrors();
                }

                // validate header metadata
                if (partitionPack.hasHeaderMetadata()) {
                    // todo: ensure partition is signaled as closed and complete in Partition Pack and use Footer Partition otherwise
                    headerPartitionPayloadRecord = payloadRecord;
                    HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPartitionPayloadRecord.getPayload()),
                            0L,
                            (long)headerPartitionPayloadRecord.getPayload().length,
                            trackFileErrorLogger);

                    trackFileErrorLogger.addAllErrors(IMFConstraints.checkMXFHeaderMetadata(headerPartition));
                }

                // validate index table segments
                if (partitionPack.hasIndexTableSegments()) {

                    indexSegmentPayloadRecords.add(payloadRecord);

                    ByteProvider imfEssenceComponentByteProvider = new ByteArrayDataProvider(payloadRecord.getPayload());

                    long numBytesToRead = payloadRecord.getPayload().length;
                    long numBytesRead = 0;
                    while (numBytesRead < numBytesToRead) {
                        KLVPacket.Header header = new KLVPacket.Header(imfEssenceComponentByteProvider, 0);
                        numBytesRead += header.getKLSize();

                        if (IndexTableSegment.isValidKey(header.getKey())) {
                            new IndexTableSegment(imfEssenceComponentByteProvider, header);
                        } else {
                            imfEssenceComponentByteProvider.skipBytes(header.getVSize());
                        }
                        numBytesRead += header.getVSize();
                    }
                }
            }

        } catch (MXFException e) {
            trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
        }

        if (trackFileErrorLogger.hasFatalErrors())
            return trackFileErrorLogger.getErrors();

        if (sequenceNamespace != null && !sequenceNamespace.isEmpty())  {

            ConstraintsValidator validator = ConstraintsValidatorFactory.getValidator(sequenceNamespace);
            if (validator != null) {
                List<ErrorLogger.ErrorObject> cplErrors = validator.validateEssencePartitionConstraints(headerPartitionPayloadRecord, indexSegmentPayloadRecords);
                trackFileErrorLogger.addAllErrors(cplErrors);
            } else {
                trackFileErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Namespace not supported: " + sequenceNamespace);
            }
        }

        return trackFileErrorLogger.getErrors();
    }


    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <filePath1><filePath2><filePath3> - List of files corresponding to the AssetMap, PackingList and CompositionPlaylist in no particular order%n", IMPValidator.class.getName()));
        return sb.toString();
    }

    public static void main(String args[]) throws IOException, URISyntaxException, SAXException, JAXBException
    {
        if (args.length != 3)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }
        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();
        Path assetMap=null, packingList=null, compositionPlaylist=null;

        for(String arg : args) {
            Path input = Utilities.getPathFromString(arg);
            String filename = Utilities.getFilenameFromPath(input);
            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(input);
            byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
            PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.Unknown, 0L, resourceByteRangeProvider.getResourceSize());
            PayloadRecord.PayloadAssetType payloadAssetType = IMPValidator.getPayloadType(payloadRecord);
            payloadRecord = new PayloadRecord(bytes, payloadAssetType, 0L, resourceByteRangeProvider.getResourceSize());
            switch (payloadAssetType) {
                case PackingList:
                    packingList = input;
                    logger.info(String.format("File %s was identified as a PackingList document.", filename));
                    errors.addAll(validatePKL(payloadRecord));
                    break;
                case AssetMap:
                    assetMap = input;
                    logger.info(String.format("File %s was identified as a AssetMap document.", filename));
                    errors.addAll(validateAssetMap(payloadRecord));
                    break;
                case CompositionPlaylist:
                    compositionPlaylist = input;
                    logger.info(String.format("File %s was identified as a CompositionPlaylist document.", filename));
                    errors.addAll(validateCPL(payloadRecord));
                    break;
                default:
                    throw new IllegalArgumentException(String.format("UnsupportedSequence AssetType for path %s", filename));
            }
        }

        if(assetMap != null
                && packingList != null){
            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetMap);
            byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
            PayloadRecord assetMapPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());

            resourceByteRangeProvider = new FileByteRangeProvider(packingList);
            bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
            PayloadRecord packingListPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.PackingList, 0L, resourceByteRangeProvider.getResourceSize());
            List<PayloadRecord> packingListPayloadRecords = new ArrayList<>();
            packingListPayloadRecords.add(packingListPayloadRecord);

            errors.addAll(IMPValidator.validatePKLAndAssetMap(assetMapPayloadRecord, packingListPayloadRecords));
        }

        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("AssetMap has %d errors and %d warnings",
                    errors.size() - warningCount, warningCount));
            for(ErrorLogger.ErrorObject errorObject : errors){
                if(errorObject.getErrorLevel()!= IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                }
                else if(errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
        }
        else{
            logger.info("No errors were detected in the AssetMap Document");
        }

    }


    /**
     * A stateless method that will validate an IMF OutputProfileList document
     * @param opl - a payload record for a OutputProfileList document
     * @return list of error messages encountered while validating an OutputProfileList document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateOPL(PayloadRecord opl) throws IOException{
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(opl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.OutputProfileList){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", opl
                    .getPayloadAssetType(), PayloadRecord.PayloadAssetType.OutputProfileList.toString()));
        }

        try {
            OutputProfileList.getOutputProfileListType(new ByteArrayByteRangeProvider(opl.getPayload()), imfErrorLogger);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }

}
