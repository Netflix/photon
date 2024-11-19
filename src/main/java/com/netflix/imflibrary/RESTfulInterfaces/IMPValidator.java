package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_100.OutputProfileList;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.st2067_2.Composition.VirtualTrack;
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
import java.util.stream.Collectors;

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
        }

        ResourceByteRangeProvider assetMapByteRangeProvider = new ByteArrayByteRangeProvider(assetMapPayload.getPayload());
        AssetMap assetMapObjectModel = null;
        try {
            assetMapObjectModel = new AssetMap(assetMapByteRangeProvider);
            imfErrorLogger.addAllErrors(assetMapObjectModel.getErrors());

            if(assetMapObjectModel.getPackingListAssets().size() == 0){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.FATAL, String.format("Asset map should reference atleast one PackingList, %d " +
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
                        .FATAL, String.format("Payload asset type is %s, expected asset type %s", assetMapPayload.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
            }
            else {
                packingLists.add(new ByteArrayByteRangeProvider(payloadRecord.getPayload()));
            }
        }

        if(packingLists.size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Atleast one PackingList is expected, %d were detected", packingLists.size()));
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
            imfErrorLogger.addAllErrors(validateComposition(imfCompositionPlaylist));
        } catch (IOException e) {
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "Unable to parse composition playlist: " + e.getMessage()));
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }




    public static List<ErrorLogger.ErrorObject> validateComposition(IMFCompositionPlaylist imfCompositionPlaylist) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /*
            run core validation on all cpls
        */
        imfErrorLogger.addAllErrors(IMFCoreConstraintsChecker.checkVirtualTracks(imfCompositionPlaylist));
        imfErrorLogger.addAllErrors(IMFCoreConstraintsChecker.checkSegments(imfCompositionPlaylist));


        /*
            run application/plugin-level validations, if implemented:
         */

        Set<String> appIds = imfCompositionPlaylist.getApplicationIdSet();
        appIds.forEach(namespace -> {
            ConstraintsValidator validator = ConstraintsValidatorFactory.getValidator(namespace);
            if (validator != null) {
                List<ErrorLogger.ErrorObject> cplErrors = validator.validateCompositionConstraints(imfCompositionPlaylist);
                imfErrorLogger.addAllErrors(cplErrors);
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Application Identification not supported: " + namespace);
            }
        });


        Set<String> sequenceNamespace = imfCompositionPlaylist.getSequenceNamespaceSet();
        sequenceNamespace.forEach(namespace -> {

            // ignore sequences covered by CoreConstraints
            if (CoreConstraints.SUPPORTED_NAMESPACES.contains(namespace)) return;

            ConstraintsValidator validator = ConstraintsValidatorFactory.getValidator(namespace);
            if (validator != null) {
                List<ErrorLogger.ErrorObject> cplErrors = validator.validateCompositionConstraints(imfCompositionPlaylist);
                imfErrorLogger.addAllErrors(cplErrors);
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Sequence namespace not supported: " + namespace);
            }
        });

        return imfErrorLogger.getErrors();
    }



    /**
     * A stateless method that can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to all the
     * Virt ual Tracks that are apart of the Composition
     * @param imfCompositionPlaylist an IMFCompositionPlaylist object corresponding to the Composition
     * @param essencesHeaderPartitionPayloads list of payload records containing the raw bytes of the HeaderPartitions of the IMF Track files that are a part of the Virtual Track/s in the Composition
     * @return list of error messages encountered while performing conformance validation of the Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateVirtualTrackConformance(IMFCompositionPlaylist imfCompositionPlaylist,
                                                                                List<PayloadRecord> essencesHeaderPartitionPayloads) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /*
         * Verify that the EssenceDescriptors are valid before attempting to parse them.
         */
        imfErrorLogger.addAllErrors(validateIMFTrackFileHeaderMetadata(essencesHeaderPartitionPayloads));
        if (imfErrorLogger.hasFatalErrors())
            return imfErrorLogger.getErrors();

        /*
         * Collect the UUIDs from the header payloads and filter out any that are actually not referenced from the composition
         */
        Map<UUID, PayloadRecord> referencedHeaderPayloads = new HashMap<>();
        List<Composition.HeaderPartitionTuple> headerPartitionTuples = new ArrayList<>();

        for (PayloadRecord payloadRecord : essencesHeaderPartitionPayloads) {
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long) payloadRecord.getPayload().length,
                    imfErrorLogger);
            Preface preface = headerPartition.getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage) genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

            if (imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet().contains(packageUUID)) {
                referencedHeaderPayloads.put(packageUUID, payloadRecord);

                headerPartitionTuples.add(new Composition.HeaderPartitionTuple(new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long) payloadRecord.getPayload().length,
                        imfErrorLogger),
                        new ByteArrayByteRangeProvider(payloadRecord.getPayload())));
            }
        }

        /*
         * Raise a warning for any missing header payloads - this is the supplemental IMP use case
         */
        for (UUID resourceEssenceDescriptorId : imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet()) {
            // no header payload provided for ResourceEssenceDescriptorId - supplemental package use case
            if (referencedHeaderPayloads.get(resourceEssenceDescriptorId) == null) {
                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("Unable to locate MXF Track File with ID %s, omitting checks for header metadata compliance and virtual track conformance.", resourceEssenceDescriptorId)));
            }
        }

        /**
         * Check the header metadata for compliance.
         */
        imfErrorLogger.addAllErrors(validateIMFTrackFileHeaderMetadata(Collections.unmodifiableList(new ArrayList<>(referencedHeaderPayloads.values()))));
        if (imfErrorLogger.hasFatalErrors()) {
            return imfErrorLogger.getErrors();
        }

        Map<UUID, DOMNodeObjectModel> essenceDescriptorMap = imfCompositionPlaylist.getEssenceDescriptorListMap();
        Map<UUID, List<DOMNodeObjectModel>> resourceEssenceDescriptorMap = imfCompositionPlaylist.getResourcesEssenceDescriptorsMap(headerPartitionTuples);
        if( essenceDescriptorMap == null || resourceEssenceDescriptorMap == null || imfErrorLogger.hasFatalErrors()) {
            return imfErrorLogger.getErrors();
        }

        /**
         * An exhaustive compare of the eDLMap and essenceDescriptorsMap is required to ensure that the essence descriptors
         * in the EssenceDescriptorList and the EssenceDescriptors in the physical essence files corresponding to the
         * same source encoding element as indicated in the TrackFileResource and EDL are a good match.
         *
         * The Maps have the DOMObjectModel for every EssenceDescriptor in the EssenceDescriptorList in the CPL and
         * the essence descriptor in each of the essences referenced from every track file resource within each virtual track.
         */

        Set<String> ignoreSet = new HashSet<String>();

        // PHDRMetadataTrackSubDescriptor is not present in SMPTE registries and cannot be serialized
        // todo:
        ignoreSet.add("PHDRMetadataTrackSubDescriptor");

        /**
         * The following check ensures that we have atleast one EssenceDescriptor in a TrackFile that equals the corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> iterator = resourceEssenceDescriptorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DOMNodeObjectModel>> entry = (Map.Entry<UUID, List<DOMNodeObjectModel>>) iterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = entry.getValue();
            DOMNodeObjectModel referenceDOMNodeObjectModel = essenceDescriptorMap.get(entry.getKey());
            if (referenceDOMNodeObjectModel == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Source Encoding " +
                        "Element %s in a track does not have a corresponding entry in the CPL's Essence Descriptor List.", entry.getKey().toString()));
            }
            else {
                referenceDOMNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(essenceDescriptorMap.get(entry.getKey()), ignoreSet);
                boolean intermediateResult = false;

                List<DOMNodeObjectModel> domNodeObjectModelsIgnoreSet = new ArrayList<>();
                for (DOMNodeObjectModel domNodeObjectModel : domNodeObjectModels) {
                    domNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(domNodeObjectModel, ignoreSet);
                    domNodeObjectModelsIgnoreSet.add(domNodeObjectModel);
                    intermediateResult |= referenceDOMNodeObjectModel.equals(domNodeObjectModel);
                }
                if (!intermediateResult) {
                    DOMNodeObjectModel matchingDOMNodeObjectModel = DOMNodeObjectModel.getMatchingDOMNodeObjectModel(referenceDOMNodeObjectModel, domNodeObjectModelsIgnoreSet);
                    imfErrorLogger.addAllErrors(DOMNodeObjectModel.getNamespaceURIMismatchErrors(referenceDOMNodeObjectModel, matchingDOMNodeObjectModel));

                    String domNodeName = referenceDOMNodeObjectModel.getLocalName();
                    List<DOMNodeObjectModel> domNodeObjectModelList = domNodeObjectModelsIgnoreSet.stream().filter( e -> e.getLocalName().equals(domNodeName)).collect(Collectors.toList());
                    if(domNodeObjectModelList.size() != 0)
                    {
                        DOMNodeObjectModel diffCPLEssenceDescriptor = referenceDOMNodeObjectModel.removeNodes(domNodeObjectModelList.get(0));
                        DOMNodeObjectModel diffTrackFileEssenceDescriptor = domNodeObjectModelList.get(0).removeNodes(referenceDOMNodeObjectModel);
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Id %s in the CPL's " +
                                        "EssenceDescriptorList doesn't match any EssenceDescriptors within the IMFTrackFile resource that references it, " +
                                        "%n%n EssenceDescriptor in CPL EssenceDescriptorList with mismatching fields is as follows %n%s, %n%nEssenceDescriptor found in the " +
                                        "TrackFile resource with mismatching fields is as follows %n%s%n%n",
                                entry.getKey().toString(), diffCPLEssenceDescriptor.toString(), diffTrackFileEssenceDescriptor.toString()));
                    }
                    else {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Id %s in the CPL's " +
                                        "EssenceDescriptorList doesn't match any EssenceDescriptors within the IMFTrackFile resource that references it, " +
                                        "%n%n EssenceDescriptor in CPL EssenceDescriptorList is as follows %n%s, %n%nEssenceDescriptors found in the TrackFile resource %n%s%n%n",
                                entry.getKey().toString(), referenceDOMNodeObjectModel.toString(), Utilities.serializeObjectCollectionToString(domNodeObjectModelsIgnoreSet)));
                    }
                }
            }
        }

        return imfErrorLogger.getErrors();
    }


    /* IMF essence related inspection calls*/


    public static List<ErrorLogger.ErrorObject> validateEssencePartitions(List<PayloadRecord> headerPartitionPayloadRecords, List<PayloadRecord> indexSegmentPayloadRecords) throws IOException {

        IMFErrorLogger trackFileErrorLogger = new IMFErrorLoggerImpl();

        // validate header metadata based on header partition payloads
        trackFileErrorLogger.addAllErrors(IMPValidator.validateIMFTrackFileHeaderMetadata(headerPartitionPayloadRecords));

        if (trackFileErrorLogger.hasFatalErrors())
            return trackFileErrorLogger.getErrors();

        // Validate index table segments
        trackFileErrorLogger.addAllErrors(IMPValidator.validateIndexTableSegments(indexSegmentPayloadRecords));
        if (trackFileErrorLogger.hasFatalErrors())
            return trackFileErrorLogger.getErrors();

        return trackFileErrorLogger.getErrors();
    }

    /**
     * A stateless method that validates an IMFEssenceComponent's header partition and verifies MXF OP1A and IMF compliance. This could be utilized
     * to perform preliminary validation of IMF essences
     * @param essencesHeaderPartitionPayloads - a list of IMF Essence Component header partition payloads
     * @return a list of errors encountered while performing compliance checks on the IMF Essence Component Header partition
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateIMFTrackFileHeaderMetadata(List<PayloadRecord> essencesHeaderPartitionPayloads) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> essencesHeaderPartition = Collections.unmodifiableList(essencesHeaderPartitionPayloads);

        for (PayloadRecord payloadRecord : essencesHeaderPartition) {
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s",
                                payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                return imfErrorLogger.getErrors();
            }

            HeaderPartition headerPartition = null;
            try {
                headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long)payloadRecord.getPayload().length,
                        imfErrorLogger);
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);

                // check for compliance
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
            }
            catch (IMFException | MXFException e){
                if(headerPartition != null) {
                    Preface preface = headerPartition.getPreface();
                    GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                    SourcePackage filePackage = (SourcePackage) genericPackage;
                    UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("IMFTrackFile with ID %s has fatal errors", packageUUID.toString())));
                }
                if(e instanceof IMFException){
                    IMFException imfException = (IMFException)e;
                    imfErrorLogger.addAllErrors(imfException.getErrors());
                }
                else if(e instanceof MXFException){
                    MXFException mxfException = (MXFException)e;
                    imfErrorLogger.addAllErrors(mxfException.getErrors());
                }
            }
        }

        return imfErrorLogger.getErrors();
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
     * A stateless method that validates IndexTable segments within partitions
     * @param essencesPartitionPayloads - a list of IMF Essence Component partition payloads
     * @return a list of errors encountered while performing compliance checks on IndexTable segments within partition payloads
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateIndexTableSegments(List<PayloadRecord> essencesPartitionPayloads) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        for(PayloadRecord payloadRecord : essencesPartitionPayloads){
            if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format
                                ("Payload asset type is %s, expected asset type %s",
                                        payloadRecord
                                                .getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                continue;
            }
            try {
                PartitionPack partitionPack = new PartitionPack(new ByteArrayDataProvider(payloadRecord.getPayload()));
                if (partitionPack.hasIndexTableSegments())
                {//logic to provide as an input stream the portion of the archive that contains a Partition
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
            } catch (MXFException e) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, e.getMessage());
            }

        }
        return imfErrorLogger.getErrors();
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
