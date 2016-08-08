package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFAuthoringException;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.CompositionModels.CompositionModel_st2067_2_2016;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A stateless class that will create the AssetMap, Packing List and CompositionPlaylist that represent a complete IMF Master Package by utilizing the relevant builders
 */
public class IMPBuilder {

    /**
     * To prevent instantiation
     */
    private IMPBuilder(){

    }

    public static List<ErrorLogger.ErrorObject> buildIMP_2013(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                              @Nonnull Composition.EditRate compositionEditRate,
                                                              @Nonnull Map<UUID, IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                                              @Nonnull File workingDirectory)
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        int numErrors = imfErrorLogger.getNumberOfErrors();
        UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();

        Composition.VirtualTrack mainImageVirtualTrack = null;
        for(Composition.VirtualTrack virtualTrack : virtualTracks){
            if(virtualTrack.getSequenceTypeEnum() == Composition.SequenceTypeEnum.MainImageSequence){
                mainImageVirtualTrack = virtualTrack;
                break;
            }
        }

        if(mainImageVirtualTrack == null){
            throw new IMFAuthoringException(String.format("Exactly 1 MainImageSequence virtual track is required to create an IMP, none present"));
        }

        /**
         * Logic to compute total running time
         */
        long totalRunningTime = 0L;
        long totalNumberOfImageEditUnits = 0L;
        for(Composition.TrackResource trackResource : mainImageVirtualTrack.getTrackResources()){
            totalNumberOfImageEditUnits += trackResource.getSourceDuration().longValue() * trackResource.getRepeatCount().longValue();
        }
        totalRunningTime = totalNumberOfImageEditUnits/(compositionEditRate.getNumerator()/compositionEditRate.getDenominator());

        CompositionPlaylistBuilder_2013 compositionPlaylistBuilder_2013 = new CompositionPlaylistBuilder_2013(cplUUID,
                                                                                                                CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013(annotationText, "en"),
                                                                                                                CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013(issuer, "en"),
                                                                                                                CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013("Photon PackingListBuilder", "en"),
                                                                                                                virtualTracks,
                                                                                                                compositionEditRate,
                                                                                                                totalRunningTime,
                                                                                                                trackFileHeaderPartitionMap,
                                                                                                                workingDirectory,
                                                                                                                imfErrorLogger);

        compositionPlaylistBuilder_2013.build();
        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the CompositionPlaylist. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;
        File cplFile = null;
        byte[] cplHash = null;
        File[] files = workingDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getName().contains("CPL-")) {
                    cplFile = file;
                    cplHash = IMFUtils.generateSHA1HashAndBase64Encode(cplFile);
                }
            }
        }

        if(cplFile == null){
            throw new IMFAuthoringException(String.format("CompositionPlaylist file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.getAbsolutePath()));
        }
        String cplFileName = cplFile.getName();

        /**
         * Build the PackingList
         */
        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        PackingListBuilder packingListBuilder = new PackingListBuilder(pklUUID,
                                                                        IMFUtils.createXMLGregorianCalendar(),
                                                                        workingDirectory,
                                                                        imfErrorLogger);

        org.smpte_ra.schemas.st0429_8_2007.PKL.UserText pklAnnotationText = PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en");
        org.smpte_ra.schemas.st0429_8_2007.PKL.UserText creator = PackingListBuilder.buildPKLUserTextType_2007("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas.st0429_8_2007.PKL.UserText pklIssuer = PackingListBuilder.buildPKLUserTextType_2007(issuer, "en");
        List<PackingListBuilder.PackingListBuilderAsset_2007> packingListBuilderAssets = new ArrayList<>();
        /**
         * Build the CPL asset to be entered into the PackingList
         */
        PackingListBuilder.PackingListBuilderAsset_2007 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2007(cplUUID,
                                                                    PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                                                                    Arrays.copyOf(cplHash, cplHash.length),
                                                                    cplFile.length(),
                                                                    PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                                                                    PackingListBuilder.buildPKLUserTextType_2007(cplFileName, "en"));
        packingListBuilderAssets.add(cplAsset);
        Set<Map.Entry<UUID, IMFTrackFileMetadata>> trackFileMetadataEntriesSet = trackFileHeaderPartitionMap.entrySet();
        for(Map.Entry<UUID, IMFTrackFileMetadata> entry : trackFileMetadataEntriesSet){
            PackingListBuilder.PackingListBuilderAsset_2007 asset =
                    new PackingListBuilder.PackingListBuilderAsset_2007(entry.getKey(),
                                                                        PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                                                                        Arrays.copyOf(entry.getValue().getHash(), entry.getValue().getHash().length),
                                                                        entry.getValue().getLength(),
                                                                        PackingListBuilder.PKLAssetTypeEnum.APP_MXF,
                                                                        PackingListBuilder.buildPKLUserTextType_2007(entry.getValue().getOriginalFileName(), "en"));
            packingListBuilderAssets.add(asset);
        }
        packingListBuilder.buildPackingList_2007(pklAnnotationText, pklIssuer, creator, packingListBuilderAssets);

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;
        File pklFile = null;
        files = workingDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getName().contains("PKL-")) {
                    pklFile = file;
                }
            }
        }
        if(pklFile == null){
            throw new IMFAuthoringException(String.format("PackingList file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.getAbsolutePath()));
        }

        /**
         * Build the AssetMap
         */
        UUID assetMapUUID = IMFUUIDGenerator.getInstance().generateUUID();
        List<AssetMapBuilder.Asset> assetMapAssets = new ArrayList<>();
        for(PackingListBuilder.PackingListBuilderAsset_2007 pklAsset : packingListBuilderAssets){
            AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(pklAsset.getOriginalFileName().getValue(), pklAsset.getSize().longValue());
            List<AssetMapBuilder.Chunk> chunkList = new ArrayList<>();
            chunkList.add(chunk);
            AssetMapBuilder.Asset amAsset = new AssetMapBuilder.Asset(UUIDHelper.fromUUIDAsURNStringToUUID(pklAsset.getUUID()),
                                                                        AssetMapBuilder.buildAssetMapUserTextType_2007(pklAsset.getAnnotationText().getValue(), "en"),
                                                                        false,
                                                                        chunkList);
            assetMapAssets.add(amAsset);
        }
        //Add the PKL as an AssetMap asset
        List<AssetMapBuilder.Chunk> chunkList = new ArrayList<>();
        AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(pklFile.getName(), pklFile.length());
        chunkList.add(chunk);
        AssetMapBuilder.Asset amAsset = new AssetMapBuilder.Asset(pklUUID,
                AssetMapBuilder.buildAssetMapUserTextType_2007(pklAnnotationText.getValue(), "en"),
                true,
                chunkList);
        assetMapAssets.add(amAsset);

        AssetMapBuilder assetMapBuilder = new AssetMapBuilder(assetMapUUID,
                                                                AssetMapBuilder.buildAssetMapUserTextType_2007(annotationText, "en"),
                                                                AssetMapBuilder.buildAssetMapUserTextType_2007("Photon AssetMapBuilder", "en"),
                                                                IMFUtils.createXMLGregorianCalendar(),
                                                                AssetMapBuilder.buildAssetMapUserTextType_2007(issuer, "en"),
                                                                assetMapAssets,
                                                                workingDirectory,
                                                                imfErrorLogger);
        assetMapBuilder.build();

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the AssetMap. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }

        File assetMapFile = null;
        files = workingDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getName().contains("AssetMap-")) {
                    assetMapFile = file;
                }
            }
        }
        if(assetMapFile == null){
            throw new IMFAuthoringException(String.format("AssetMap file does not exist in the working directory %s, IMP is incomplete", workingDirectory.getAbsolutePath()));
        }

        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> buildIMP_2016(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                              @Nonnull Composition.EditRate compositionEditRate,
                                                              @Nonnull Map<UUID, IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                                              @Nonnull File workingDirectory)
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        int numErrors = imfErrorLogger.getNumberOfErrors();
        UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();

        Composition.VirtualTrack mainImageVirtualTrack = null;
        for(Composition.VirtualTrack virtualTrack : virtualTracks){
            if(virtualTrack.getSequenceTypeEnum() == Composition.SequenceTypeEnum.MainImageSequence){
                mainImageVirtualTrack = virtualTrack;
                break;
            }
        }

        if(mainImageVirtualTrack == null){
            throw new IMFAuthoringException(String.format("Exactly 1 MainImageSequence virtual track is required to create an IMP, none present"));
        }

        /**
         * Logic to compute total running time
         */
        long totalRunningTime = 0L;
        long totalNumberOfImageEditUnits = 0L;
        for(Composition.TrackResource trackResource : mainImageVirtualTrack.getTrackResources()){
            totalNumberOfImageEditUnits += trackResource.getSourceDuration().longValue() * trackResource.getRepeatCount().longValue();
        }
        totalRunningTime = totalNumberOfImageEditUnits/(compositionEditRate.getNumerator()/compositionEditRate.getDenominator());

        CompositionPlaylistBuilder_2016 compositionPlaylistBuilder_2016 = new CompositionPlaylistBuilder_2016(cplUUID,
                CompositionPlaylistBuilder_2016.buildCPLUserTextType_2016(annotationText, "en"),
                CompositionPlaylistBuilder_2016.buildCPLUserTextType_2016(issuer, "en"),
                CompositionPlaylistBuilder_2016.buildCPLUserTextType_2016("Photon PackingListBuilder", "en"),
                virtualTracks,
                compositionEditRate,
                totalRunningTime,
                trackFileHeaderPartitionMap,
                workingDirectory,
                imfErrorLogger);

        compositionPlaylistBuilder_2016.build();

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the CompositionPlaylist. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;
        File cplFile = null;
        byte[] cplHash = null;
        File[] files = workingDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getName().contains("CPL-")) {
                    cplFile = file;
                    cplHash = IMFUtils.generateSHA1HashAndBase64Encode(cplFile);
                }
            }
        }
        if(cplFile == null){
            throw new IMFAuthoringException(String.format("CompositionPlaylist file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.getAbsolutePath()));
        }
        String cplFileName = cplFile.getName();

        /**
         * Build the PackingList
         */
        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        PackingListBuilder packingListBuilder = new PackingListBuilder(pklUUID,
                IMFUtils.createXMLGregorianCalendar(),
                workingDirectory,
                imfErrorLogger);

        org.smpte_ra.schemas.st2067_2_2016.PKL.UserText pklAnnotationText = PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en");
        org.smpte_ra.schemas.st2067_2_2016.PKL.UserText creator = PackingListBuilder.buildPKLUserTextType_2016("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas.st2067_2_2016.PKL.UserText pklIssuer = PackingListBuilder.buildPKLUserTextType_2016(issuer, "en");
        List<PackingListBuilder.PackingListBuilderAsset_2016> packingListBuilderAssets = new ArrayList<>();
        /**
         * Build the CPL asset to be entered into the PackingList
         */
        PackingListBuilder.PackingListBuilderAsset_2016 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2016(cplUUID,
                        PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en"),
                        Arrays.copyOf(cplHash, cplHash.length),
                        packingListBuilder.buildDefaultDigestMethodType(),
                        cplFile.length(),
                        PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                        PackingListBuilder.buildPKLUserTextType_2016(cplFileName, "en"));
        packingListBuilderAssets.add(cplAsset);
        Set<Map.Entry<UUID, IMFTrackFileMetadata>> trackFileMetadataEntriesSet = trackFileHeaderPartitionMap.entrySet();
        for(Map.Entry<UUID, IMFTrackFileMetadata> entry : trackFileMetadataEntriesSet){
            PackingListBuilder.PackingListBuilderAsset_2016 asset =
                    new PackingListBuilder.PackingListBuilderAsset_2016(entry.getKey(),
                            PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en"),
                            Arrays.copyOf(entry.getValue().getHash(), entry.getValue().getHash().length),
                            packingListBuilder.buildDefaultDigestMethodType(),
                            entry.getValue().getLength(),
                            PackingListBuilder.PKLAssetTypeEnum.APP_MXF,
                            PackingListBuilder.buildPKLUserTextType_2016(entry.getValue().getOriginalFileName(), "en"));
            packingListBuilderAssets.add(asset);
        }
        packingListBuilder.buildPackingList_2016(pklAnnotationText, pklIssuer, creator, packingListBuilderAssets);

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;
        File pklFile = null;
        files = workingDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getName().contains("PKL-")) {
                    pklFile = file;
                }
            }
        }
        if(pklFile == null){
            throw new IMFAuthoringException(String.format("PackingList file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.getAbsolutePath()));
        }

        /**
         * Build the AssetMap
         */
        UUID assetMapUUID = IMFUUIDGenerator.getInstance().generateUUID();
        List<AssetMapBuilder.Asset> assetMapAssets = new ArrayList<>();
        for(PackingListBuilder.PackingListBuilderAsset_2016 pklAsset : packingListBuilderAssets){
            AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(pklAsset.getOriginalFileName().getValue(), pklAsset.getSize().longValue());
            List<AssetMapBuilder.Chunk> chunkList = new ArrayList<>();
            chunkList.add(chunk);
            AssetMapBuilder.Asset amAsset = new AssetMapBuilder.Asset(UUIDHelper.fromUUIDAsURNStringToUUID(pklAsset.getUUID()),
                    AssetMapBuilder.buildAssetMapUserTextType_2007(pklAsset.getAnnotationText().getValue(), "en"),
                    false,
                    chunkList);
            assetMapAssets.add(amAsset);
        }
        //Add the PKL as an AssetMap asset
        List<AssetMapBuilder.Chunk> chunkList = new ArrayList<>();
        AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(pklFile.getName(), pklFile.length());
        chunkList.add(chunk);
        AssetMapBuilder.Asset amAsset = new AssetMapBuilder.Asset(pklUUID,
                AssetMapBuilder.buildAssetMapUserTextType_2007(pklAnnotationText.getValue(), "en"),
                true,
                chunkList);
        assetMapAssets.add(amAsset);

        AssetMapBuilder assetMapBuilder = new AssetMapBuilder(assetMapUUID,
                AssetMapBuilder.buildAssetMapUserTextType_2007(annotationText, "en"),
                AssetMapBuilder.buildAssetMapUserTextType_2007("Photon AssetMapBuilder", "en"),
                IMFUtils.createXMLGregorianCalendar(),
                AssetMapBuilder.buildAssetMapUserTextType_2007(issuer, "en"),
                assetMapAssets,
                workingDirectory,
                imfErrorLogger);
        assetMapBuilder.build();

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the AssetMap. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }

        File assetMapFile = null;
        files = workingDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getName().contains("AssetMap-")) {
                    assetMapFile = file;
                }
            }
        }
        if(assetMapFile == null){
            throw new IMFAuthoringException(String.format("AssetMap file does not exist in the working directory %s, IMP is incomplete", workingDirectory.getAbsolutePath()));
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A thin class representing the EssenceMetadata required to construct a CPL document
     */
    public static class IMFTrackFileMetadata {
        private final byte[] headerPartition;
        private final byte[] hash;
        private final String hashAlgorithm;
        private final String originalFileName;
        private final long length;

        /**
         * A constructor for the EssenceMetadata required to construct a CPL document
         * @param headerPartition a byte[] containing the EssenceHeaderPartition metadata of the IMFTrack file
         * @param hash a byte[] containing the SHA-1, Base64 encoded hash of the IMFTrack file
         * @param hashAlgorithm a string representing the Hash Algorithm used to generate the Hash of the IMFTrack file
         * @param originalFileName a string representing the FileName of the IMFTrack file
         * @param length a long integer represeting the length of the IMFTrack file
         */
        public IMFTrackFileMetadata(byte[] headerPartition, byte[] hash, String hashAlgorithm, String originalFileName, long length){
            this.headerPartition = Arrays.copyOf(headerPartition, headerPartition.length);
            this.hash = Arrays.copyOf(hash, hash.length);
            this.hashAlgorithm = hashAlgorithm;
            this.originalFileName = originalFileName;
            this.length = length;
        }

        /**
         * Getter for the HeaderPartition metadata for the IMFTrack file corresponding to the IMFTrackFile metadata
         * @return a byte[] containing the HeaderParition metadata
         */
        public byte[] getHeaderPartition(){
            return Arrays.copyOf(this.headerPartition, this.headerPartition.length);
        }

        /**
         * Getter for the Hash of the IMFTrackFile
         * @return a byte[] containing the SHA-1 Base64 encoded hash of the IMFTrackFile
         */
        public byte[] getHash(){
            return Arrays.copyOf(this.hash, this.hash.length);
        }

        /**
         * Getter for the HashAlgorithm used to create the Hash of the IMFTrackFile
         * @return a string representing the Hash of the IMFTrackFile
         */
        public String getHashAlgorithm(){
            return this.hashAlgorithm;
        }

        /**
         * Getter for the original file name of the IMFTrackFile
         * @return a string representing the name of the IMFTrackFile
         */
        public String getOriginalFileName(){
            return this.originalFileName;
        }

        /**
         * Getter for the length of the IMFTrackFile
         * @return a long integer representing the length of the track file in bytes
         */
        public long getLength() {
            return this.length;
        }
    }
}
