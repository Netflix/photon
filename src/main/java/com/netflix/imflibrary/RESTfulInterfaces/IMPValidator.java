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
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0377.header.WaveAudioEssenceDescriptor;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_100.OutputProfileList;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.Composition.VirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.st2067_201.IABTrackFileConstraints;
import com.netflix.imflibrary.st2067_203.MGASADMTrackFileConstraints;
import com.netflix.imflibrary.st2067_204.ADMAudioTrackFileConstraints;
import com.netflix.imflibrary.st2067_204.ADMSoundfieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st2067_204.ADM_CHNASubDescriptor;
import com.netflix.imflibrary.st2067_204.ADMAudioMetadataSubDescriptor;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
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
        else if(ApplicationComposition.isCompositionPlaylist(resourceByteRangeProvider)){
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
    public static List<ErrorLogger.ErrorObject> validateCPL(PayloadRecord cpl) throws IOException{
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(cpl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.CompositionPlaylist){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", cpl
                    .getPayloadAssetType(), PayloadRecord.PayloadAssetType.CompositionPlaylist.toString()));
        }

        try {
            ApplicationCompositionFactory.getApplicationComposition(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method to retrieve all the VirtualTracks that are a part of a Composition
     * @param cpl - a payload corresponding to the Composition Playlist
     * @return list of VirtualTracks
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<? extends VirtualTrack> getVirtualTracks(PayloadRecord cpl) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<ErrorLogger.ErrorObject> errorList = validateCPL(cpl);

        imfErrorLogger.addAllErrors(errorList);

        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("Virtual track failed validation", imfErrorLogger);
        }

        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger);
        if(applicationComposition == null) {
            return new ArrayList<>();
        }
        return applicationComposition.getVirtualTracks();
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
        if(imfErrorLogger.hasFatalErrors()){
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
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()), imfErrorLogger);
        if(applicationComposition == null) {
            return imfErrorLogger.getErrors();
        }

        List<VirtualTrack> virtualTracks = new ArrayList<>(applicationComposition.getVirtualTracks());
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

            ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()), imfErrorLogger);
            if(applicationComposition == null) {
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

            imfErrorLogger.addAllErrors(applicationComposition.conformVirtualTracksInComposition(Collections.unmodifiableList
                    (headerPartitionTuples), conformAllVirtualTracks));

            imfErrorLogger.addAllErrors(applicationComposition.getErrors());
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method that determines if 2 or more Composition documents corresponding to the same title can be inferred to
     * represent the same presentation timeline. This method is present to work around current limitations in the IMF eco system
     * wherein CPL's might not be built incrementally to include all the IMF essences that are a part of the same timeline
     * @param referenceCPLPayloadRecord - a payload record corresponding to a Reference Composition document, perhaps the first
     *                                  composition playlist document that was delivered for a particular composition.
     * @param cplPayloads - a list of payload records corresponding to each of the Composition documents
     *                          that need to be verified for mergeability
     * @return a boolean indicating if the CPLs can be merged or not
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> isCPLMergeable(PayloadRecord referenceCPLPayloadRecord, List<PayloadRecord> cplPayloads) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> cplPayloadRecords = Collections.unmodifiableList(cplPayloads);
        List<ApplicationComposition> applicationCompositions = new ArrayList<>();
        try
        {
            ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new ByteArrayByteRangeProvider(referenceCPLPayloadRecord.getPayload()),
                imfErrorLogger);
            if(applicationComposition == null) {
                return imfErrorLogger.getErrors();
            }

            applicationCompositions.add(applicationComposition);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }


        for (PayloadRecord cpl : cplPayloadRecords) {
            try
            {
                ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(new ByteArrayByteRangeProvider(cpl.getPayload()),
                    imfErrorLogger);
                if(applicationComposition != null) {
                    applicationCompositions.add(applicationComposition);
                }

            }
            catch(IMFException e)
            {
                imfErrorLogger.addAllErrors(e.getErrors());
            }
        }

        if(imfErrorLogger.hasFatalErrors()) {
            return imfErrorLogger.getErrors();
        }

        VirtualTrack referenceVideoVirtualTrack = applicationCompositions.get(0).getVideoVirtualTrack();
        UUID referenceCPLUUID = applicationCompositions.get(0).getUUID();
        for (int i = 1; i < applicationCompositions.size(); i++) {
            if (!referenceVideoVirtualTrack.equivalent(applicationCompositions.get(i).getVideoVirtualTrack())) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since the video virtual tracks do not seem to represent the same timeline.", applicationCompositions.get(i).getUUID(), referenceCPLUUID));
            }
        }

        /**
         * Perform AudioTrack mergeability checks
         * 1) Identify AudioTracks that are the same language
         * 2) Compare language tracks to see if they represent the same timeline
         */
        Boolean bAudioVirtualTrackMapFail = false;
        List<Map<Set<DOMNodeObjectModel>, ? extends VirtualTrack>> audioVirtualTracksMapList = new ArrayList<>();
        for (ApplicationComposition applicationComposition : applicationCompositions) {
            try {
                audioVirtualTracksMapList.add(applicationComposition.getAudioVirtualTracksMap());
            }
            catch(IMFException e)
            {
                bAudioVirtualTrackMapFail = false;
                imfErrorLogger.addAllErrors(e.getErrors());
            }
        }


        if(!bAudioVirtualTrackMapFail) {
            Map<Set<DOMNodeObjectModel>, ? extends VirtualTrack> referenceAudioVirtualTracksMap = audioVirtualTracksMapList.get(0);
            for (int i = 1; i < audioVirtualTracksMapList.size(); i++) {
                if (!compareAudioVirtualTrackMaps(Collections.unmodifiableMap(referenceAudioVirtualTracksMap), Collections.unmodifiableMap(audioVirtualTracksMapList.get(i)), imfErrorLogger)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since 2 same language audio tracks do not seem to represent the same timeline.", applicationCompositions.get(i).getUUID(), referenceCPLUUID));
                }
            }
        }

        /**
         * Perform MarkerTrack mergeability checks
         */
        Composition.VirtualTrack referenceMarkerVirtualTrack = applicationCompositions.get(0).getMarkerVirtualTrack();
        if (referenceMarkerVirtualTrack != null) {
            UUID referenceMarkerCPLUUID = applicationCompositions.get(0).getUUID();
            for (int i = 1; i < applicationCompositions.size(); i++) {
                if (!referenceMarkerVirtualTrack.equivalent(applicationCompositions.get(i).getMarkerVirtualTrack())) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since the marker virtual tracks do not seem to represent the same timeline.", applicationCompositions.get(i).getUUID(), referenceMarkerCPLUUID));
                }
            }
        }

        return imfErrorLogger.getErrors();
    }

    /* IMF essence related inspection calls*/

    /**
     * A stateless method that will return the size of the RandomIndexPack present within a MXF file. In a typical IMF workflow
     * this would be the first method that would need to be invoked to perform IMF essence component level validation
     * @param essenceFooter4Bytes - the last 4 bytes of the MXF file used to infer the size of the RandomIndexPack
     * @return a long integer value representing the size of the RandomIndexPack
     */
    public static Long getRandomIndexPackSize(PayloadRecord essenceFooter4Bytes){
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(essenceFooter4Bytes.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssenceFooter4Bytes){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s",
                    essenceFooter4Bytes.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssenceFooter4Bytes
                            .toString()), imfErrorLogger);
        }
        return (long)(ByteBuffer.wrap(essenceFooter4Bytes.getPayload()).getInt());
    }

    /**
     * A stateless method that will read and parse the RandomIndexPack within a MXF file and return a list of byte offsets
     * corresponding to the partitions of the MXF file. In a typical IMF workflow this would be the second method after
     * {@link #getRandomIndexPackSize(PayloadRecord)} that would need to be invoked to perform IMF essence component
     * level validation
     * @param randomIndexPackPayload - a payload containing the raw bytes corresponding to the RandomIndexPack of the MXF file
     * @param randomIndexPackSize - size of the RandomIndexPack of the MXF file
     * @return list of long integer values representing the byte offsets of the partitions in the MXF file
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<Long> getEssencePartitionOffsets(PayloadRecord randomIndexPackPayload, Long randomIndexPackSize) throws IOException {
        if(randomIndexPackPayload.getPayload().length != randomIndexPackSize){
            throw new IllegalArgumentException(String.format("RandomIndexPackSize passed in is = %d, RandomIndexPack payload size = %d, they should be equal", randomIndexPackSize, randomIndexPackPayload.getPayload().length));
        }
        RandomIndexPack randomIndexPack = new RandomIndexPack(new ByteArrayDataProvider(randomIndexPackPayload.getPayload()), 0L, randomIndexPackSize);
        return randomIndexPack.getAllPartitionByteOffsets();
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
                Preface preface = headerPartition.getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.IABEssence) {
                    IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.MGASADMEssence) {
                    MGASADMTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.MainAudioEssence) {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof WaveAudioEssenceDescriptor) { // Potential support for st2067-204, check if an ADMAudioMetadataSubDescriptor or ADM_CHNASubDescriptor is present
                        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
                        if (subDescriptors.size() != 0) {
                            List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
                            List<InterchangeObject.InterchangeObjectBO> adm_CHNASubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADM_CHNASubDescriptor.class)).collect(Collectors.toList());
                                if (!admAudioMetadataSubDescriptors.isEmpty() || !adm_CHNASubDescriptors.isEmpty()) {
                                    ADMAudioTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                                }
                        }
                    }
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

    /**
     * A stateless method that returns the RFC-5646 Spoken Language Tag present in the Header Partition of an Audio Essence
     * @param essencesHeaderPartition - a list of payloads corresponding to the Header Partitions of TrackFiles that are a part of an Audio VirtualTrack
     * @param audioVirtualTrack - the audio virtual track whose spoken language needs to be ascertained
     * @return string corresponding to the RFC-5646 language tag present in the header partition of the Audio Essence
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    @Nullable
    public static String getAudioTrackSpokenLanguage(VirtualTrack audioVirtualTrack, List<PayloadRecord> essencesHeaderPartition) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(audioVirtualTrack.getSequenceTypeEnum() != Composition.SequenceTypeEnum.MainAudioSequence){
            throw new IMFException(String.format("Virtual track that was passed in is of type %s, spoken language is " +
                    "currently supported for only %s tracks", audioVirtualTrack.getSequenceTypeEnum().toString(),
                    Composition.SequenceTypeEnum.MainAudioSequence.toString()));
        }
        List<VirtualTrack> virtualTracks = new ArrayList<>();
        virtualTracks.add(audioVirtualTrack);
        imfErrorLogger.addAllErrors(checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks,
                essencesHeaderPartition));
        if(imfErrorLogger.hasFatalErrors()){
            throw new IMFException(String.format("Fatal Errors were detected when trying to verify the Virtual Track and Essence Header Partition payloads %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors())));
        }
        Set<String> audioLanguageSet = new HashSet<>();
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
            audioLanguageSet.add(headerPartition.getAudioEssenceSpokenLanguage());
        }

        if(audioLanguageSet.size() > 1){
            throw new IMFException(String.format("It seems that RFC-5646 spoken language is not consistent across " +
                    "resources of this Audio Virtual Track, found references to %s languages in the HeaderPartition",
                    Utilities.serializeObjectCollectionToString(audioLanguageSet)), imfErrorLogger);
        }
        return audioLanguageSet.iterator().next();
    }

    private static boolean compareAudioVirtualTrackMaps(Map<Set<DOMNodeObjectModel>, ? extends VirtualTrack> map1, Map<Set<DOMNodeObjectModel>, ? extends VirtualTrack> map2, IMFErrorLogger imfErrorLogger){
        boolean result = true;
        Iterator refIterator = map1.entrySet().iterator();
        while(refIterator.hasNext()){
            Map.Entry<Set<DOMNodeObjectModel>, VirtualTrack> entry = (Map.Entry<Set<DOMNodeObjectModel>, VirtualTrack>) refIterator.next();
            VirtualTrack refVirtualTrack = entry.getValue();
            VirtualTrack otherVirtualTrack = map2.get(entry.getKey());
            if(otherVirtualTrack != null){//If we identified an audio virtual track with the same essence description we can compare, else no point comparing hence the default result = true.
                result &= refVirtualTrack.equivalent(otherVirtualTrack);
            }
        }
        return result;
    }

    private static List<ErrorLogger.ErrorObject> checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(List<VirtualTrack>
                                                                                               virtualTracks,
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
                if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.MainAudioEssence)) {
                    GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                    if (genericDescriptor instanceof WaveAudioEssenceDescriptor) { // Potential support for st2067-204, check if an ADMAudioMetadataSubDescriptor or ADM_CHNASubDescriptor is present
                        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
                        if (subDescriptors.size() != 0) {
                            List<InterchangeObject.InterchangeObjectBO> admAudioMetadataSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADMAudioMetadataSubDescriptor.class)).collect(Collectors.toList());
                            List<InterchangeObject.InterchangeObjectBO> adm_CHNASubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ADM_CHNASubDescriptor.class)).collect(Collectors.toList());
                                if (!admAudioMetadataSubDescriptors.isEmpty() || !adm_CHNASubDescriptors.isEmpty()) {
                                    ADMAudioTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                                }
                        }
                    }
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
        File assetMapFile=null, packingListFile=null, compositionPlaylistFile=null;

        for(String arg : args) {
            File inputFile = new File(arg);
            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
            byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
            PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.Unknown, 0L, resourceByteRangeProvider.getResourceSize());
            PayloadRecord.PayloadAssetType payloadAssetType = IMPValidator.getPayloadType(payloadRecord);
            payloadRecord = new PayloadRecord(bytes, payloadAssetType, 0L, resourceByteRangeProvider.getResourceSize());
            switch (payloadAssetType) {
                case PackingList:
                    packingListFile = inputFile;
                    logger.info(String.format("File %s was identified as a PackingList document.", packingListFile.getName()));
                    errors.addAll(validatePKL(payloadRecord));
                    break;
                case AssetMap:
                    assetMapFile = inputFile;
                    logger.info(String.format("File %s was identified as a AssetMap document.", assetMapFile.getName()));
                    errors.addAll(validateAssetMap(payloadRecord));
                    break;
                case CompositionPlaylist:
                    compositionPlaylistFile = inputFile;
                    logger.info(String.format("File %s was identified as a CompositionPlaylist document.", compositionPlaylistFile.getName()));
                    errors.addAll(validateCPL(payloadRecord));
                    break;
                default:
                    throw new IllegalArgumentException(String.format("UnsupportedSequence AssetType for file %s", inputFile.getName()));
            }
        }

        if(assetMapFile != null
                && packingListFile != null){
            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(assetMapFile);
            byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize() - 1);
            PayloadRecord assetMapPayloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.AssetMap, 0L, resourceByteRangeProvider.getResourceSize());

            resourceByteRangeProvider = new FileByteRangeProvider(packingListFile);
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
