package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.Utilities;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
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

/**
 * A RESTful interface for validating an IMF Master Package.
 */
public class IMPValidator {

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
        else if(Composition.isFileOfSupportedSchema(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.CompositionPlaylist;
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

        if(pkl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", pkl.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new PackingList(new ByteArrayByteRangeProvider(pkl.getPayload()), imfErrorLogger);
        }
        catch (SAXException | JAXBException | IMFException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method that will validate an IMF AssetMap document
     * @param assetMap - a payload record for an AssetMap document
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateAssetMap(PayloadRecord assetMap) throws IOException {
        if(assetMap.getPayloadAssetType() != PayloadRecord.PayloadAssetType.AssetMap){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMap.getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new AssetMap(new ByteArrayByteRangeProvider(assetMap.getPayload()), imfErrorLogger);
        }
        catch (SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
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
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMapPayload.getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
        }

        ResourceByteRangeProvider assetMapByteRangeProvider = new ByteArrayByteRangeProvider(assetMapPayload.getPayload());
        AssetMap assetMapObjectModel;
        try {
            assetMapObjectModel = new AssetMap(assetMapByteRangeProvider, imfErrorLogger);
        }
        catch (SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The AssetMap delivered in this package is invalid. Error %s ocurred while trying to read and parse an AssetMap document", e.getMessage()));
            return imfErrorLogger.getErrors();
        }

        List<ResourceByteRangeProvider> packingLists = new ArrayList<>();
        for(PayloadRecord payloadRecord : packingListPayloadRecords){
            if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMapPayload.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
            }
            packingLists.add(new ByteArrayByteRangeProvider(payloadRecord.getPayload()));
        }

        if(packingLists.size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Atleast one PackingList is expected, %d were detected", packingLists.size()));
            return imfErrorLogger.getErrors();
        }

        if(assetMapObjectModel.getPackingListAssets().size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Asset map should reference atleast one PackingList, %d references found", assetMapObjectModel.getPackingListAssets().size()));
            return imfErrorLogger.getErrors();
        }

        List<PackingList> packingListObjectModels = new ArrayList<>();
        try {
            for (ResourceByteRangeProvider resourceByteRangeProvider : packingLists) {
                packingListObjectModels.add(new PackingList(resourceByteRangeProvider, imfErrorLogger));
            }
        }
        catch (SAXException | JAXBException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Atleast one of the PKLs delivered in this package is invalid. Error %s ocurred while trying to read and parse a PKL document", e.getMessage()));
            return imfErrorLogger.getErrors();
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
        if(cpl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.CompositionPlaylist){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", cpl.getPayloadAssetType(), PayloadRecord.PayloadAssetType.CompositionPlaylist.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new Composition(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger);
        }
        catch(SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method to retrieve all the VirtualTracks that are a part of a Composition
     * @param cpl - a payload corresponding to the Composition Playlist
     * @return list of VirtualTracks
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<? extends Composition.VirtualTrack> getVirtualTracks(PayloadRecord cpl) throws IOException {
        validateCPL(cpl);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Composition composition;
        try{
            composition = new Composition(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger);
            return composition.getVirtualTracks();
        }
        catch(SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            StringBuilder stringBuilder = new StringBuilder();
            for(ErrorLogger.ErrorObject errorObject : imfErrorLogger.getErrors()){
                stringBuilder.append(errorObject.toString());
                stringBuilder.append(String.format("%n"));
            }
            throw new IMFException(stringBuilder.toString());
        }
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
                                                                             Composition.VirtualTrack virtualTrack,
                                                                             List<PayloadRecord> essencesHeaderPartitionPayloads) throws IOException
    {
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>();
        virtualTracks.add(virtualTrack);
        checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks, essencesHeaderPartitionPayloads, new IMFErrorLoggerImpl());
        return conformVirtualTracksInCPL(cplPayloadRecord, essencesHeaderPartitionPayloads, false);
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
        try {
            Composition composition = new Composition(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()), imfErrorLogger);
            List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(composition.getVirtualTracks());
            checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks, essencesHeaderPartitionPayloads, imfErrorLogger);
        }
        catch (SAXException | JAXBException | URISyntaxException | MXFException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return conformVirtualTracksInCPL(cplPayloadRecord, essencesHeaderPartitionPayloads, true);
    }

    private static List<ErrorLogger.ErrorObject> conformVirtualTracksInCPL(PayloadRecord cplPayloadRecord, List<PayloadRecord> essencesHeaderPartitionPayloads, boolean conformAllVirtualTracks) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> essencesHeaderPartition = Collections.unmodifiableList(essencesHeaderPartitionPayloads);
        try {

            List<ErrorLogger.ErrorObject> errors = new ArrayList<>(validateCPL(cplPayloadRecord));
            errors.addAll(validateIMFTrackFileHeaderMetadata(essencesHeaderPartition));
            if(errors.size() > 0){
                return Collections.unmodifiableList(errors);
            }

            Composition composition = new Composition(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()), imfErrorLogger);
            List<HeaderPartitionTuple> headerPartitionTuples = new ArrayList<>();
            for(PayloadRecord payloadRecord : essencesHeaderPartition){
                if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                    throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                }
                headerPartitionTuples.add(new HeaderPartitionTuple(new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long)payloadRecord.getPayload().length,
                        imfErrorLogger),
                        new ByteArrayByteRangeProvider(payloadRecord.getPayload())));
            }
            if(!composition.conformVirtualTrackInComposition(Collections.unmodifiableList(headerPartitionTuples), imfErrorLogger, conformAllVirtualTracks)){
                return imfErrorLogger.getErrors();
            }
        }
        catch (SAXException | JAXBException | URISyntaxException | MXFException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
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
        List<Composition> compositions = new ArrayList<>();
        try {
            compositions.add(new Composition(new ByteArrayByteRangeProvider(referenceCPLPayloadRecord.getPayload()), imfErrorLogger));
        } catch (SAXException | JAXBException | URISyntaxException e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        for(PayloadRecord cpl : cplPayloadRecords) {
            try {
                compositions.add(new Composition(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger));
            } catch (SAXException | JAXBException | URISyntaxException e) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
                return imfErrorLogger.getErrors();
            }
        }

        Composition.VirtualTrack referenceVideoVirtualTrack = compositions.get(0).getVideoVirtualTrack();
        UUID referenceCPLUUID = compositions.get(0).getUUID();
        for(int i = 1; i< compositions.size(); i++){
            if(!referenceVideoVirtualTrack.equivalent(compositions.get(i).getVideoVirtualTrack())){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since the video virtual tracks do not seem to represent the same timeline.", compositions.get(i).getUUID(), referenceCPLUUID));
            }
        }

        if(imfErrorLogger.getErrors().size() > 0){
            return imfErrorLogger.getErrors();
        }

        /**
         * Perform AudioTrack mergeability checks
         * 1) Identify AudioTracks that are the same language
         * 2) Compare language tracks to see if they represent the same timeline
         */
        List<Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack>> audioVirtualTracksMapList = new ArrayList<>();
        for(Composition composition : compositions){
            audioVirtualTracksMapList.add(composition.getAudioVirtualTracksMap());
        }

        Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> referenceAudioVirtualTracksMap = audioVirtualTracksMapList.get(0);
        for(int i=1; i<audioVirtualTracksMapList.size(); i++){
            if(!compareAudioVirtualTrackMaps(Collections.unmodifiableMap(referenceAudioVirtualTracksMap), Collections.unmodifiableMap(audioVirtualTracksMapList.get(i)))){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since 2 same language audio tracks do not seem to represent the same timeline.", compositions.get(i).getUUID(), referenceCPLUUID));
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
        if(essenceFooter4Bytes.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssenceFooter4Bytes){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", essenceFooter4Bytes.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssenceFooter4Bytes.toString()));
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
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            }
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long)payloadRecord.getPayload().length,
                    imfErrorLogger);
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition);
            IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
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
    public static String getAudioTrackSpokenLanguage(Composition.VirtualTrack audioVirtualTrack, List<PayloadRecord> essencesHeaderPartition) throws IOException {
        if(audioVirtualTrack.getSequenceTypeEnum() != Composition.SequenceTypeEnum.MainAudioSequence){
            throw new IMFException(String.format("Virtual track that was passed in is of type %s, spoken language is currently supported for only %s tracks", audioVirtualTrack.getSequenceTypeEnum().toString(), Composition.SequenceTypeEnum.MainAudioSequence.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>();
        virtualTracks.add(audioVirtualTrack);
        checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks, essencesHeaderPartition, imfErrorLogger);

        Set<String> audioLanguageSet = new HashSet<>();
        for (PayloadRecord payloadRecord : essencesHeaderPartition){
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            }
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                0L,
                (long) payloadRecord.getPayload().length,
                imfErrorLogger);
            audioLanguageSet.add(headerPartition.getAudioEssenceSpokenLanguage());
        }

        if(audioLanguageSet.size() > 1){
            throw new IMFException(String.format("It seems that RFC-5646 spoken language is not consistent across resources of this Audio Virtual Track, found references to %s languages in the HeaderPartition", Utilities.serializeObjectCollectionToString(audioLanguageSet)));
        }
        return audioLanguageSet.iterator().next();
    }

    private static boolean compareAudioVirtualTrackMaps(Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> map1, Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> map2){
        boolean result = true;
        Iterator refIterator = map1.entrySet().iterator();
        while(refIterator.hasNext()){
            Map.Entry<Set<DOMNodeObjectModel>, Composition.VirtualTrack> entry = (Map.Entry<Set<DOMNodeObjectModel>, Composition.VirtualTrack>) refIterator.next();
            Composition.VirtualTrack refVirtualTrack = entry.getValue();
            Composition.VirtualTrack otherVirtualTrack = map2.get(entry.getKey());
            if(otherVirtualTrack != null){//If we identified an audio virtual track with the same essence description we can compare, else no point comparing hence the default result = true.
                result &= refVirtualTrack.equivalent(otherVirtualTrack);
            }
        }
        return result;
    }

    private static void checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(List<Composition.VirtualTrack> virtualTracks, List<PayloadRecord> essencesHeaderPartition, IMFErrorLogger imfErrorLogger) throws IOException {
        Set<UUID> trackFileIDsSet = new HashSet<>();

        for (PayloadRecord payloadRecord : essencesHeaderPartition){
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            }
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long) payloadRecord.getPayload().length,
                    imfErrorLogger);
            /**
             * Add the Top Level Package UUID to the set of TrackFileIDs, this is required to validate that the essences header partition that were passed in
             * are in fact from the constituent resources of the VirtualTack
             */
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition);
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
            Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
            trackFileIDsSet.add(packageUUID);
        }

        Set<UUID> virtualTrackResourceIDsSet = new HashSet<>();
        for(Composition.VirtualTrack virtualTrack : virtualTracks){
            virtualTrackResourceIDsSet.addAll(virtualTrack.getTrackResourceIds());
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
            throw new IMFException(String.format("It seems that no EssenceHeaderPartition data was passed in for VirtualTrack Resource Ids %s, please verify that the correct Header Partition payloads for the Virtual Track were passed in", Utilities.serializeObjectCollectionToString(unreferencedResourceIDsSet)));
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
            throw new IMFException(String.format("It seems that EssenceHeaderPartition data was passed in for Resource Ids %s which are not part of this virtual track, please verify that only the Header Partition payloads for the Virtual Track were passed in", Utilities.serializeObjectCollectionToString(unreferencedTrackFileIDsSet)));
        }

    }

    /**
     * An object model for a HeaderPartition and access to the raw bytes corresponding to the HeaderPartition
     */
    public static class HeaderPartitionTuple {
        private final HeaderPartition headerPartition;
        private final ResourceByteRangeProvider resourceByteRangeProvider;

        private HeaderPartitionTuple(HeaderPartition headerPartition, ResourceByteRangeProvider resourceByteRangeProvider){
            this.headerPartition = headerPartition;
            this.resourceByteRangeProvider = resourceByteRangeProvider;
        }

        /**
         * A getter for the resourceByteRangeProvider object corresponding to this HeaderPartition to allow
         * access to the raw bytes
         * @return ResourceByteRangeProvider object corresponding to this HeaderPartition
         */
        public ResourceByteRangeProvider getResourceByteRangeProvider(){
            return this.resourceByteRangeProvider;
        }

        /**
         * A getter for the HeaderPartition object corresponding to a resource referenced from the Composition
         * @return HeaderPartition of a certain resource in the Composition
         */
        public HeaderPartition getHeaderPartition(){
            return this.headerPartition;
        }
    }
}
