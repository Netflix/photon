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
import com.netflix.imflibrary.st0377.RandomIndexPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_100.OutputProfileList;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.st2067_2.Composition.VirtualTrack;
import com.netflix.imflibrary.st2067_201.IABTrackFileConstraints;
import com.netflix.imflibrary.st2067_203.MGASADMTrackFileConstraints;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.validation.ConstraintsValidator;
import com.netflix.imflibrary.validation.ConstraintsValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import jakarta.annotation.Nullable;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

        // this should go elsewhere?
        if ((imfCompositionPlaylist.getEssenceDescriptors() == null) ||
                (imfCompositionPlaylist.getEssenceDescriptors().size() < 1)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ESSENCE_DESCRIPTOR_LIST_MISSING,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, "EssenceDescriptorList is either absent or empty.");
        }

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
     * A stateless method that can be used to determine if a Virtual Track in a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to the Virtual Track
     * @param cplPayloadRecord a payload record corresponding to the Composition payload
     * @param virtualTrack that needs to be conformed in the Composition
     * @param essencesHeaderPartitionPayloads list of payload records containing the raw bytes of the HeaderPartitions of the IMF Track files that are a part of
     *                                        the Virtual Track to be conformed
     * @return list of error messages encountered while performing conformance validation of the Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> isVirtualTrackInCPLConformed(PayloadRecord cplPayloadRecord,
                                                                             VirtualTrack virtualTrack,
                                                                             List<PayloadRecord> essencesHeaderPartitionPayloads) throws IOException
    {
        List<VirtualTrack> virtualTracks = new ArrayList<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        virtualTracks.add(virtualTrack);
        imfErrorLogger.addAllErrors(checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks,
                essencesHeaderPartitionPayloads));
        if (imfErrorLogger.hasFatalErrors()){
            return imfErrorLogger.getErrors();
        }
        imfErrorLogger.addAllErrors(conformVirtualTracksInCPL(cplPayloadRecord, essencesHeaderPartitionPayloads,
                false));

        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method that can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to all the
     * Virtual Tracks that are a part of the Composition
     * @param cplPayloadRecord a payload record corresponding to the Composition payload
     * @param essencesHeaderPartitionPayloads list of payload records containing the raw bytes of the HeaderPartitions of the IMF Track files that are a part of the Virtual Track/s in the Composition
     * @return list of error messages encountered while performing conformance validation of the Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> areAllVirtualTracksInCPLConformed(
            PayloadRecord cplPayloadRecord,
            List<PayloadRecord> essencesHeaderPartitionPayloads) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()));
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());

        if (imfErrorLogger.hasFatalErrors()){
            return imfErrorLogger.getErrors();
        }

        List<VirtualTrack> virtualTracks = new ArrayList<>(imfCompositionPlaylist.getVirtualTracks());
        imfErrorLogger.addAllErrors(checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks,
                essencesHeaderPartitionPayloads));
        if(imfErrorLogger.hasFatalErrors()){
            return imfErrorLogger.getErrors();
        }
        imfErrorLogger.addAllErrors(conformVirtualTracksInCPL(cplPayloadRecord, essencesHeaderPartitionPayloads,
                true));

        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> conformVirtualTracksInCPL(PayloadRecord cplPayloadRecord,
        List<PayloadRecord> essencesHeaderPartitionPayloads,boolean conformAllVirtualTracks) throws IOException
    {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> essencesHeaderPartition = Collections.unmodifiableList(essencesHeaderPartitionPayloads);

        try {
            imfErrorLogger.addAllErrors(validateCPL(cplPayloadRecord));
            if (imfErrorLogger.hasFatalErrors())
                return Collections.unmodifiableList(imfErrorLogger.getErrors());

            IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()));
            imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());

            if (imfErrorLogger.hasFatalErrors()) {
                return imfErrorLogger.getErrors();
            }

            imfErrorLogger.addAllErrors(validateIMFTrackFileHeaderMetadata(essencesHeaderPartition));

            List<Composition.HeaderPartitionTuple> headerPartitionTuples = new ArrayList<>();
            for (PayloadRecord payloadRecord : essencesHeaderPartition) {
                if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger
                            .IMFErrors.ErrorLevels.FATAL, String.format
                            ("Payload asset type is %s, expected asset type %s",
                                    payloadRecord
                                            .getPayloadAssetType(),
                                    PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                    continue;
                }
                headerPartitionTuples.add(new Composition.HeaderPartitionTuple(new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long) payloadRecord.getPayload().length,
                        imfErrorLogger),
                        new ByteArrayByteRangeProvider(payloadRecord.getPayload())));
            }

            if (imfErrorLogger.hasFatalErrors()) {
                return imfErrorLogger.getErrors();
            }

            imfErrorLogger.addAllErrors(conformVirtualTracksInComposition(imfCompositionPlaylist,
                                                                Collections.unmodifiableList(headerPartitionTuples),
                                                                conformAllVirtualTracks));

            imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * This method can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to the
     * resources referenced by the Composition.
     *
     * @param headerPartitionTuples        list of HeaderPartitionTuples corresponding to the IMF essences referenced in the Composition
     * @param conformAllVirtualTracksInCpl a boolean that turns on/off conforming all the VirtualTracks in the Composition
     * @return boolean to indicate of the Composition is conformant or not
     * @throws IOException        - any I/O related error is exposed through an IOException.
     */
    public static List<ErrorLogger.ErrorObject> conformVirtualTracksInComposition(IMFCompositionPlaylist imfCompositionPlaylist,
                                                                                  List<Composition.HeaderPartitionTuple> headerPartitionTuples,
                                                                                  boolean conformAllVirtualTracksInCpl) throws IOException {
        /*
         * The algorithm for conformance checking a Composition (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list (EDL) is referenced through its id element if conformAllVirtualTracks is enabled
         * by at least one TrackFileResource within the Virtual tracks in the Composition (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the Composition are present in
         * the physical essence files referenced by the resources of a virtual track and are equal.
         */
        /*The following check simultaneously verifies 1) and 2) from above.*/
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Set<UUID> resourceEssenceDescriptorIDsSet = imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet();
        Set<UUID> cplEssenceDescriptorIDsSet = imfCompositionPlaylist.getEssenceDescriptorIdsSet();
        Iterator cplEssenceDescriptorIDs = cplEssenceDescriptorIDsSet.iterator();


        /**
         * The following checks that at least one of the Virtual Tracks references an EssenceDescriptor in the EDL. This
         * check should be performed only when we need to conform all the Virtual Tracks in the CPL.
         */
        if (conformAllVirtualTracksInCpl) {
            while (cplEssenceDescriptorIDs.hasNext()) {
                UUID cplEssenceDescriptorUUID = (UUID) cplEssenceDescriptorIDs.next();
                if (!resourceEssenceDescriptorIDsSet.contains(cplEssenceDescriptorUUID)) {
                    //Section 6.1.10.1 st2067-3:2013
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptorID %s in the CPL " +
                            "EssenceDescriptorList is not referenced by any resource in any of the Virtual tracks in the CPL, this is invalid.", cplEssenceDescriptorUUID.toString()));
                }
            }
        }

        if (imfErrorLogger.hasFatalErrors()) {
            return imfErrorLogger.getErrors();
        }

        Map essenceDescriptorMap = null;
        Map resourceEssenceDescriptorMap = null;
        /*The following check verifies 3) from above.*/
        try {
            essenceDescriptorMap = imfCompositionPlaylist.getEssenceDescriptorListMap();
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        try {
            resourceEssenceDescriptorMap = imfCompositionPlaylist.getResourcesEssenceDescriptorsMap(headerPartitionTuples);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        if( essenceDescriptorMap == null || resourceEssenceDescriptorMap == null || imfErrorLogger.hasFatalErrors())
        {
            return imfErrorLogger.getErrors();
        }

        imfErrorLogger.addAllErrors(conformEssenceDescriptors(resourceEssenceDescriptorMap, essenceDescriptorMap));
        return imfErrorLogger.getErrors();
    }

    private static List<IMFErrorLogger.ErrorObject> conformEssenceDescriptors(Map<UUID, List<DOMNodeObjectModel>> essenceDescriptorsMap, Map<UUID, DOMNodeObjectModel> eDLMap) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /**
         * An exhaustive compare of the eDLMap and essenceDescriptorsMap is required to ensure that the essence descriptors
         * in the EssenceDescriptorList and the EssenceDescriptors in the physical essence files corresponding to the
         * same source encoding element as indicated in the TrackFileResource and EDL are a good match.
         */

        /**
         * The Maps passed in have the DOMObjectModel for every EssenceDescriptor in the EssenceDescriptorList in the CPL and
         * the essence descriptor in each of the essences referenced from every track file resource within each virtual track.
         */

        /**
         * The following check ensures that we do not have a Track Resource that does not have a corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> essenceDescriptorsMapIterator = essenceDescriptorsMap.entrySet().iterator();
        while (essenceDescriptorsMapIterator.hasNext()) {
            UUID sourceEncodingElement = essenceDescriptorsMapIterator.next().getKey();
            if (!eDLMap.keySet().contains(sourceEncodingElement)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Source Encoding " +
                        "Element %s in a track does not have a corresponding entry in the CPL's EDL.", sourceEncodingElement.toString()));
            }
        }
        Set<String> ignoreSet = new HashSet<String>();
        //ignoreSet.add("InstanceUID");
        //ignoreSet.add("InstanceID");
        //ignoreSet.add("EssenceLength");
        //ignoreSet.add("AlternativeCenterCuts");
        //ignoreSet.add("GroupOfSoundfieldGroupsLinkID");

        // PHDRMetadataTrackSubDescriptor is not present in SMPTE registries and cannot be serialized
        // todo:
        ignoreSet.add("PHDRMetadataTrackSubDescriptor");

        /**
         * The following check ensures that we have atleast one EssenceDescriptor in a TrackFile that equals the corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> iterator = essenceDescriptorsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DOMNodeObjectModel>> entry = (Map.Entry<UUID, List<DOMNodeObjectModel>>) iterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = entry.getValue();
            DOMNodeObjectModel referenceDOMNodeObjectModel = eDLMap.get(entry.getKey());
            if (referenceDOMNodeObjectModel == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Source Encoding " +
                        "Element %s in a track does not have a corresponding entry in the CPL's Essence Descriptor List.", entry.getKey().toString()));
            }
            else {
                referenceDOMNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(eDLMap.get(entry.getKey()), ignoreSet);
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
        for(PayloadRecord payloadRecord : essencesHeaderPartition){
            if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format
                        ("Payload asset type is %s, expected asset type %s",
                        payloadRecord
                        .getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                continue;
            }
            HeaderPartition headerPartition = null;
            try {
                headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long)payloadRecord.getPayload().length,
                    imfErrorLogger);
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.IABEssence) {
                    IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.MGASADMEssence) {
                    MGASADMTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
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


    public static List<ErrorLogger.ErrorObject> checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(List<VirtualTrack> virtualTracks,
                                                                               List<PayloadRecord> essencesHeaderPartition) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Set<UUID> trackFileIDsSet = new HashSet<>();

        for (PayloadRecord payloadRecord : essencesHeaderPartition){
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s",
                        payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString
                                ()), imfErrorLogger);
            }
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long) payloadRecord.getPayload().length,
                    imfErrorLogger);
            Preface preface = headerPartition.getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage) genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
            trackFileIDsSet.add(packageUUID);

            try {
                /**
                 * Add the Top Level Package UUID to the set of TrackFileIDs, this is required to validate that the essences header partition that were passed in
                 * are in fact from the constituent resources of the VirtualTack
                 */
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.IABEssence)) {
                    IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
                if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.MGASADMEssence)) {
                    MGASADMTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
            }
            catch (IMFException | MXFException e){
                if(headerPartition != null) {

                }
                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("IMFTrackFile with ID %s has fatal errors", packageUUID.toString())));
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

        Set<UUID> virtualTrackResourceIDsSet = new HashSet<>();
        for(Composition.VirtualTrack virtualTrack : virtualTracks){
            if(virtualTrack instanceof IMFEssenceComponentVirtualTrack)
            {
                virtualTrackResourceIDsSet.addAll(IMFEssenceComponentVirtualTrack.class.cast(virtualTrack).getTrackResourceIds());
            }
        }
        /**
         * Following check ensures that the Header Partitions corresponding to all the Resources of the VirtualTracks were passed in.
         */
        Set<UUID> unreferencedResourceIDsSet = new HashSet<>();
        for(UUID uuid : virtualTrackResourceIDsSet){
            if(!trackFileIDsSet.contains(uuid)){
                unreferencedResourceIDsSet.add(uuid);
            }
        }
        if(unreferencedResourceIDsSet.size() > 0){
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("It seems that no EssenceHeaderPartition data was passed in for " +
                    "VirtualTrack Resource Ids %s, please verify that the correct Header Partition payloads for the " +
                    "Virtual Track were passed in", Utilities.serializeObjectCollectionToString
                    (unreferencedResourceIDsSet))));
        }

        /**
         * Following check ensures that the Header Partitions corresponding to only the Resource that are a part of the VirtualTracks were passed in.
         */
        Set<UUID> unreferencedTrackFileIDsSet = new HashSet<>();
        for(UUID uuid : trackFileIDsSet){
            if(!virtualTrackResourceIDsSet.contains(uuid)){
                unreferencedTrackFileIDsSet.add(uuid);
            }
        }
        if(unreferencedTrackFileIDsSet.size() > 0){
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("It seems that EssenceHeaderPartition data was passed in for " +
                    "Resource Ids %s which are not part of this virtual track, please verify that only the Header " +
                    "Partition payloads for the Virtual Track were passed in", Utilities
                    .serializeObjectCollectionToString(unreferencedTrackFileIDsSet))));
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

    /**
     * A stateless method, used for IMP containing IAB and/or MGA S-ADM tracks, that will validate that the index edit rate in the index segment matches the one in the descriptor (according to Section 5.7 of SMPTE ST 2067-201:2019)
     * @param headerPartitionPayloadRecords - a list of IMF Essence Component partition payloads for header partitions
     * @param indexSegmentPayloadRecords - a list of IMF Essence Component partition payloads for index partitions
     * @return list of error messages encountered while validating
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateIndexEditRate(List<PayloadRecord> headerPartitionPayloadRecords, List<PayloadRecord> indexSegmentPayloadRecords) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> essencesHeaderPartition = Collections.unmodifiableList(headerPartitionPayloadRecords);
        for(PayloadRecord headerPayloadRecord : essencesHeaderPartition){
            if(headerPayloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s",
                                headerPayloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                continue;
            }

            HeaderPartition headerPartition = null;
            try {
                headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPayloadRecord.getPayload()),
                        0L, (long) headerPayloadRecord.getPayload().length, imfErrorLogger);

                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);

                for (PayloadRecord indexPayloadRecord : indexSegmentPayloadRecords) {
                    if (indexPayloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                String.format("Payload asset type is %s, expected asset type %s",
                                        indexPayloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                        continue;
                    }
                    PartitionPack partitionPack = new PartitionPack(new ByteArrayDataProvider(indexPayloadRecord.getPayload()));
                    if (partitionPack.hasIndexTableSegments()) {//logic to provide as an input stream the portion of the archive that contains a Partition
                        ByteProvider imfEssenceComponentByteProvider = new ByteArrayDataProvider(indexPayloadRecord.getPayload());

                        long numBytesToRead = indexPayloadRecord.getPayload().length;
                        long numBytesRead = 0;
                        while (numBytesRead < numBytesToRead) {
                            KLVPacket.Header header = new KLVPacket.Header(imfEssenceComponentByteProvider, 0);
                            numBytesRead += header.getKLSize();

                            if (IndexTableSegment.isValidKey(header.getKey())) {
                                IndexTableSegment indexTableSegment = new IndexTableSegment(imfEssenceComponentByteProvider, header);
                                if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.IABEssence)) {
                                    IABTrackFileConstraints.checkIndexEditRate(headerPartitionIMF, indexTableSegment, imfErrorLogger);
                                } else if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.MGASADMEssence)) {
                                    MGASADMTrackFileConstraints.checkIndexEditRate(headerPartitionIMF, indexTableSegment, imfErrorLogger);
                                }
                            } else {
                                imfEssenceComponentByteProvider.skipBytes(header.getVSize());
                            }
                            numBytesRead += header.getVSize();
                        }

                    }
                }
            } catch (IMFException | MXFException e){
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
}
