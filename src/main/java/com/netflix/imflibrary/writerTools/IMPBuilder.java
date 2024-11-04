package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFAuthoringException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RegXMLLibHelper;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import com.sandflow.smpte.klv.Triplet;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.netflix.imflibrary.st2067_2.AbstractApplicationComposition.getResourceIdTuples;

/**
 * A stateless class that will create the AssetMap, Packing List and CompositionPlaylist that represent a complete IMF Master Package by utilizing the relevant builders
 */
public class IMPBuilder {

    /**
     * To prevent instantiation
     */
    private IMPBuilder(){

    }

    /**
     * A method to generate the AssetMap, PackingList and CompositionPlaylist documents conforming to the
     * st0429-9:2007, st2067-2:2013 and st2067-2/3:2013 schemas respectively
     * @param annotationText a human readable text that will be used to annotate the corresponding elements of the XML documents
     * @param issuer a human readable text indicating the issuer of the XML documents
     * @param virtualTracks a list of all VirtualTracks that are a part of the Composition
     * @param compositionEditRate the EditRate of the composition
     * @param applicationId ApplicationId for the composition
     * @param trackFileHeaderPartitionMap a Map of IMFTrackFileId to the HeaderPartition metadata of the track file
     * @param workingDirectory a folder location for the generated documents
     * @return a list of errors that occurred while building an IMP
     * @throws IOException - any I/O related error will be exposed through an IOException
     * @throws ParserConfigurationException if a DocumentBuilder
     *   cannot be created which satisfies the configuration requested by the underlying builder implementation
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public static List<ErrorLogger.ErrorObject> buildIMP_2013(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                              @Nonnull Composition.EditRate compositionEditRate,
                                                              @Nonnull String applicationId,
                                                              @Nonnull Map<UUID, IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                                              @Nonnull Path workingDirectory) throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException {
        if(trackFileHeaderPartitionMap.entrySet().stream().filter(e -> e.getValue().getHeaderPartition() == null).count() > 0) {
            throw new IMFAuthoringException(String.format("trackFileHeaderPartitionMap has IMFTrackFileMetadata with null header partition"));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Map<UUID, List<Node>> imfEssenceDescriptorMap = buildEDLForVirtualTracks(trackFileHeaderPartitionMap, virtualTracks, imfErrorLogger);

        Map<UUID, IMFTrackFileInfo> uuidimfTrackFileInfoMap = new HashMap<>();

        for(Map.Entry<UUID, IMFTrackFileMetadata> entry: trackFileHeaderPartitionMap.entrySet()) {
            IMFTrackFileInfo imfTrackFileInfo = new IMFTrackFileInfo(entry.getValue().getHash(), entry.getValue().getHashAlgorithm(), entry.getValue().getOriginalFileName(), entry.getValue().getLength(), entry.getValue().isExcludeFromPackage());
            uuidimfTrackFileInfoMap.put(entry.getKey(), imfTrackFileInfo);
        }
        imfErrorLogger.addAllErrors(buildIMP_2013(annotationText, issuer, virtualTracks, compositionEditRate, applicationId, uuidimfTrackFileInfoMap, workingDirectory, imfEssenceDescriptorMap));
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> buildIMPWithoutCreatingNewCPL_2013(@Nonnull String annotationText,
                                                                                   @Nonnull String issuer,
                                                                                   @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                                                   @Nonnull Composition.EditRate compositionEditRate,
                                                                                   @Nonnull String applicationId,
                                                                                   @Nonnull Map<UUID, IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                                                                   @Nonnull Path workingDirectory, @Nonnull Path cplFile) throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException {
        if (trackFileHeaderPartitionMap.entrySet().stream().filter(e -> e.getValue().getHeaderPartition() == null).count() > 0) {
            throw new IMFAuthoringException(String.format("trackFileHeaderPartitionMap has IMFTrackFileMetadata with null header partition"));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Map<UUID, IMFTrackFileInfo> uuidimfTrackFileInfoMap = new HashMap<>();

        for (Map.Entry<UUID, IMFTrackFileMetadata> entry : trackFileHeaderPartitionMap.entrySet()) {
            IMFTrackFileInfo imfTrackFileInfo = new IMFTrackFileInfo(entry.getValue().getHash(), entry.getValue().getHashAlgorithm(), entry.getValue().getOriginalFileName(), entry.getValue().getLength(), entry.getValue().isExcludeFromPackage());
            uuidimfTrackFileInfoMap.put(entry.getKey(), imfTrackFileInfo);
        }
        imfErrorLogger.addAllErrors(buildIMPWithoutCreatingNewCPL_2013(annotationText, issuer, virtualTracks, applicationId, uuidimfTrackFileInfoMap, workingDirectory, cplFile));
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> buildIMPWithoutCreatingNewCPL_2013(@Nonnull String annotationText,
                                                                                   @Nonnull String issuer,
                                                                                   @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                                                   @Nonnull String applicationId,
                                                                                   @Nonnull Map<UUID, IMFTrackFileInfo> trackFileInfoMap,
                                                                                   @Nonnull Path workingDirectory,
                                                                                   @Nonnull Path cplFile) throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        int numErrors = imfErrorLogger.getNumberOfErrors();
        Set<String> applicationIds = Collections.singleton(applicationId);
        String coreConstraintsSchema = CoreConstraints.fromApplicationId(applicationIds);
        if (coreConstraintsSchema == null)
            coreConstraintsSchema = CoreConstraints.NAMESPACE_IMF_2013;

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

        /* No need to build a new CPL because we already have it in cplFile */

        if(!Files.isRegularFile(cplFile)){
            throw new IMFAuthoringException(String.format("CompositionPlaylist file does not exist, cannot generate the rest of the documents"));
        }
        byte[] cplHash = IMFUtils.generateSHA1Hash(cplFile);

        /**
         * Build the PackingList
         */
        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        PackingListBuilder packingListBuilder = new PackingListBuilder(pklUUID,
                IMFUtils.createXMLGregorianCalendar(),
                workingDirectory,
                imfErrorLogger);

        org.smpte_ra.schemas._429_8._2007.pkl.UserText pklAnnotationText = PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en");
        org.smpte_ra.schemas._429_8._2007.pkl.UserText creator = PackingListBuilder.buildPKLUserTextType_2007("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas._429_8._2007.pkl.UserText pklIssuer = PackingListBuilder.buildPKLUserTextType_2007(issuer, "en");
        List<PackingListBuilder.PackingListBuilderAsset_2007> packingListBuilderAssets = new ArrayList<>();
        /**
         * Build the CPL asset to be entered into the PackingList
         */
        UUID cplUUID = IMFUtils.extractUUIDFromCPLFile(cplFile, imfErrorLogger);

        PackingListBuilder.PackingListBuilderAsset_2007 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2007(cplUUID,
                        PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                        Arrays.copyOf(cplHash, cplHash.length),
                        Files.size(cplFile),
                        PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                        PackingListBuilder.buildPKLUserTextType_2007(Utilities.getFilenameFromPath(cplFile), "en"));
        packingListBuilderAssets.add(cplAsset);
        Set<Map.Entry<UUID, IMFTrackFileInfo>> trackFileMetadataEntriesSet = trackFileInfoMap.entrySet().stream().filter( e -> !(e.getValue().isExcludeFromPackage())).collect(Collectors.toSet());
        for(Map.Entry<UUID, IMFTrackFileInfo> entry : trackFileMetadataEntriesSet){
            PackingListBuilder.PackingListBuilderAsset_2007 asset =
                    new PackingListBuilder.PackingListBuilderAsset_2007(entry.getKey(),
                            PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                            Arrays.copyOf(entry.getValue().getHash(), entry.getValue().getHash().length),
                            entry.getValue().getLength(),
                            PackingListBuilder.PKLAssetTypeEnum.APP_MXF,
                            PackingListBuilder.buildPKLUserTextType_2007(entry.getValue().getOriginalFileName(), "en"));
            packingListBuilderAssets.add(asset);
        }
        imfErrorLogger.addAllErrors(packingListBuilder.buildPackingList_2007(pklAnnotationText, pklIssuer, creator,
                packingListBuilderAssets));

        imfErrorLogger.addAllErrors(packingListBuilder.getErrors());

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;

        Path pklFile = workingDirectory.resolve(packingListBuilder.getPKLFileName());
        if (!Files.isRegularFile(pklFile)){
            throw new IMFAuthoringException(String.format("PackingList path does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
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
        AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(Utilities.getFilenameFromPath(pklFile), Files.size(pklFile));
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
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the AssetMap. Please see following error messages %s",
                    Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }

        Path assetMapPath = workingDirectory.resolve(assetMapBuilder.getAssetMapFileName());
        if (!Files.isRegularFile(assetMapPath)){
            throw new IMFAuthoringException(String.format("AssetMap path does not exist in the working directory %s", workingDirectory.toString()));
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A method to generate the AssetMap, PackingList and CompositionPlaylist documents conforming to the
     * st0429-9:2007, st0429-8:2007 and st2067-2/3:2013 schemas respectively
     * @param annotationText a human readable text that will be used to annotate the corresponding elements of the XML documents
     * @param issuer a human readable text indicating the issuer of the XML documents
     * @param virtualTracks a list of all VirtualTracks that are a part of the Composition
     * @param compositionEditRate the EditRate of the composition
     * @param applicationId ApplicationId for the composition
     * @param trackFileInfoMap a Map of IMFTrackFileId to IMFTrackFileInfo
     * @param workingDirectory a folder location for the generated documents
     * @param essenceDescriptorDomNodeMap Map containing mapping between Track file ID to essence descriptor DOM node list
     * @return a list of errors that occurred while building an IMP
     * @throws IOException - any I/O related error will be exposed through an IOException
     * @throws ParserConfigurationException if a DocumentBuilder
     *   cannot be created which satisfies the configuration requested by the underlying builder implementation
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public static List<ErrorLogger.ErrorObject> buildIMP_2013(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                              @Nonnull Composition.EditRate compositionEditRate,
                                                              @Nonnull String applicationId,
                                                              @Nonnull Map<UUID, IMFTrackFileInfo> trackFileInfoMap,
                                                              @Nonnull Path workingDirectory,
                                                              @Nonnull Map<UUID, List<Node>> essenceDescriptorDomNodeMap)
            throws IOException, ParserConfigurationException, URISyntaxException
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        int numErrors = imfErrorLogger.getNumberOfErrors();

        UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();
        Set<String> applicationIds = Collections.singleton(applicationId);
        String coreConstraintsSchema = CoreConstraints.fromApplicationId(applicationIds);
        if (coreConstraintsSchema == null)
            coreConstraintsSchema = CoreConstraints.NAMESPACE_IMF_2013;

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
        for(IMFTrackFileResourceType trackResource : (List<IMFTrackFileResourceType>)mainImageVirtualTrack.getResourceList()){
            totalNumberOfImageEditUnits += trackResource.getSourceDuration().longValue() * trackResource.getRepeatCount().longValue();
        }
        totalRunningTime = totalNumberOfImageEditUnits/(compositionEditRate.getNumerator()/compositionEditRate.getDenominator());

        List<IMFEssenceDescriptorBaseType> imfEssenceDescriptorBaseTypeList = new ArrayList<>();
        Map<UUID, UUID> trackFileIdToEssenceDescriptorIdMap = new HashMap<>();
        for(AbstractApplicationComposition.ResourceIdTuple resourceIdTuple : getResourceIdTuples(virtualTracks)) {
            trackFileIdToEssenceDescriptorIdMap.put(resourceIdTuple.getTrackFileId(), resourceIdTuple.getSourceEncoding());
        }
        for(Map.Entry<UUID, List<Node>> entry: essenceDescriptorDomNodeMap.entrySet()) {
            if(trackFileIdToEssenceDescriptorIdMap.containsKey(entry.getKey())) {
                imfEssenceDescriptorBaseTypeList.add(new IMFEssenceDescriptorBaseType(UUIDHelper.fromUUID(trackFileIdToEssenceDescriptorIdMap.get(entry.getKey())), new ArrayList<>( entry.getValue())));
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Resource = %s is not referenced in the virtual track.", UUIDHelper.fromUUID(entry.getKey())));
            }
        }

        CompositionPlaylistBuilder_2013 compositionPlaylistBuilder_2013 = new CompositionPlaylistBuilder_2013(cplUUID,
                CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013(annotationText, "en"),
                CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013(issuer, "en"),
                CompositionPlaylistBuilder_2013.buildCPLUserTextType_2013("Photon PackingListBuilder", "en"),
                virtualTracks,
                compositionEditRate,
                applicationIds,
                totalRunningTime,
                trackFileInfoMap,
                workingDirectory,
                imfEssenceDescriptorBaseTypeList,
                coreConstraintsSchema,
                trackFileIdToEssenceDescriptorIdMap);

        imfErrorLogger.addAllErrors(compositionPlaylistBuilder_2013.build());


        if(imfErrorLogger.hasFatalErrors()) {
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the CompositionPlaylist. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;

        Path cplFile = workingDirectory.resolve(compositionPlaylistBuilder_2013.getCPLFileName());
        if (!Files.isRegularFile(cplFile)) {
            throw new IMFAuthoringException(String.format("CompositionPlaylist path does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
        }
        byte[] cplHash = IMFUtils.generateSHA1Hash(cplFile);


        /**
         * Build the PackingList
         */
        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        PackingListBuilder packingListBuilder = new PackingListBuilder(pklUUID,
                IMFUtils.createXMLGregorianCalendar(),
                workingDirectory,
                imfErrorLogger);

        org.smpte_ra.schemas._429_8._2007.pkl.UserText pklAnnotationText = PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en");
        org.smpte_ra.schemas._429_8._2007.pkl.UserText creator = PackingListBuilder.buildPKLUserTextType_2007("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas._429_8._2007.pkl.UserText pklIssuer = PackingListBuilder.buildPKLUserTextType_2007(issuer, "en");
        List<PackingListBuilder.PackingListBuilderAsset_2007> packingListBuilderAssets = new ArrayList<>();
        /**
         * Build the CPL asset to be entered into the PackingList
         */
        PackingListBuilder.PackingListBuilderAsset_2007 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2007(cplUUID,
                        PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                        Arrays.copyOf(cplHash, cplHash.length),
                        Files.size(cplFile),
                        PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                        PackingListBuilder.buildPKLUserTextType_2007(compositionPlaylistBuilder_2013.getCPLFileName(), "en"));
        packingListBuilderAssets.add(cplAsset);
        Set<Map.Entry<UUID, IMFTrackFileInfo>> trackFileMetadataEntriesSet = trackFileInfoMap.entrySet().stream().filter( e -> !(e.getValue().isExcludeFromPackage())).collect(Collectors.toSet());
        for(Map.Entry<UUID, IMFTrackFileInfo> entry : trackFileMetadataEntriesSet){
            PackingListBuilder.PackingListBuilderAsset_2007 asset =
                    new PackingListBuilder.PackingListBuilderAsset_2007(entry.getKey(),
                            PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                            Arrays.copyOf(entry.getValue().getHash(), entry.getValue().getHash().length),
                            entry.getValue().getLength(),
                            PackingListBuilder.PKLAssetTypeEnum.APP_MXF,
                            PackingListBuilder.buildPKLUserTextType_2007(entry.getValue().getOriginalFileName(), "en"));
            packingListBuilderAssets.add(asset);
        }
        imfErrorLogger.addAllErrors(packingListBuilder.buildPackingList_2007(pklAnnotationText, pklIssuer, creator,
                packingListBuilderAssets));

        imfErrorLogger.addAllErrors(packingListBuilder.getErrors());

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;

        Path pklPath = workingDirectory.resolve(packingListBuilder.getPKLFileName());
        if (!Files.isRegularFile(pklPath)){
            throw new IMFAuthoringException(String.format("PackingList path does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
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
        AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(Utilities.getFilenameFromPath(pklPath), Files.size(pklPath));
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
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the AssetMap. Please see following error messages %s",
                    Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }

        Path assetMapPath = workingDirectory.resolve(assetMapBuilder.getAssetMapFileName());
        if(!Files.isRegularFile(assetMapPath)){
            throw new IMFAuthoringException(String.format("AssetMap path does not exist in the working directory %s", workingDirectory.toString()));
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A method to generate the AssetMap, PackingList and CompositionPlaylist documents conforming to the
     * st0429-9:2007, st2067-2:2016 and st2067-2/3:2016 schemas respectively
     * @param annotationText a human readable text that will be used to annotate the corresponding elements of the XML documents
     * @param issuer a human readable text indicating the issuer of the XML documents
     * @param virtualTracks a list of all VirtualTracks that are a part of the Composition
     * @param compositionEditRate the EditRate of the composition
     * @param applicationId ApplicationId for the composition
     * @param trackFileHeaderPartitionMap a Map of IMFTrackFileId to the HeaderPartition metadata of the track file
     * @param workingDirectory a folder location for the generated documents
     * @return a list of errors that occurred while building an IMP
     * @throws IOException - any I/O related error will be exposed through an IOException
     * @throws ParserConfigurationException if a DocumentBuilder
     *   cannot be created which satisfies the configuration requested by the underlying builder implementation
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public static List<ErrorLogger.ErrorObject> buildIMP_2016(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                              @Nonnull Composition.EditRate compositionEditRate,
                                                              @Nonnull String applicationId,
                                                              @Nonnull Map<UUID, IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                                              @Nonnull Path workingDirectory) throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException {
        if(trackFileHeaderPartitionMap.entrySet().stream().filter(e -> e.getValue().getHeaderPartition() == null).count() > 0) {
            throw new IMFAuthoringException(String.format("trackFileHeaderPartitionMap has IMFTrackFileMetadata with null header partition"));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Map<UUID, List<Node>> imfEssenceDescriptorMap = buildEDLForVirtualTracks(trackFileHeaderPartitionMap, virtualTracks, imfErrorLogger);

        Map<UUID, IMFTrackFileInfo> uuidimfTrackFileInfoMap = new HashMap<>();
        for(Map.Entry<UUID, IMFTrackFileMetadata> entry: trackFileHeaderPartitionMap.entrySet()) {
            IMFTrackFileInfo imfTrackFileInfo = new IMFTrackFileInfo(entry.getValue().getHash(), entry.getValue().getHashAlgorithm(), entry.getValue().getOriginalFileName(), entry.getValue().getLength(), entry.getValue().isExcludeFromPackage());
            uuidimfTrackFileInfoMap.put(entry.getKey(), imfTrackFileInfo);
        }

        imfErrorLogger.addAllErrors(buildIMP_2016(annotationText, issuer, virtualTracks, compositionEditRate, applicationId, uuidimfTrackFileInfoMap, workingDirectory, imfEssenceDescriptorMap));
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> buildIMPWithoutCreatingNewCPL_2016(@Nonnull String annotationText,
                                                                                   @Nonnull String issuer,
                                                                                   @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                                                   @Nonnull Composition.EditRate compositionEditRate,
                                                                                   @Nonnull String applicationId,
                                                                                   @Nonnull Map<UUID, IMFTrackFileMetadata> trackFileHeaderPartitionMap,
                                                                                   @Nonnull Path workingDirectory, @Nonnull Path cplFile) throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException {
        if(trackFileHeaderPartitionMap.entrySet().stream().filter(e -> e.getValue().getHeaderPartition() == null).count() > 0) {
            throw new IMFAuthoringException(String.format("trackFileHeaderPartitionMap has IMFTrackFileMetadata with null header partition"));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Map<UUID, IMFTrackFileInfo> trackFileInfoMap = new HashMap<>();
        for(Map.Entry<UUID, IMFTrackFileMetadata> entry: trackFileHeaderPartitionMap.entrySet()) {
            IMFTrackFileInfo imfTrackFileInfo = new IMFTrackFileInfo(entry.getValue().getHash(), entry.getValue().getHashAlgorithm(), entry.getValue().getOriginalFileName(), entry.getValue().getLength(), entry.getValue().isExcludeFromPackage());
            trackFileInfoMap.put(entry.getKey(), imfTrackFileInfo);
        }

        imfErrorLogger.addAllErrors(buildIMPWithoutCreatingNewCPL_2016(annotationText, issuer, virtualTracks, applicationId, trackFileInfoMap, workingDirectory, cplFile));
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> buildIMPWithoutCreatingNewCPL_2016(@Nonnull String annotationText,
                                                                                   @Nonnull String issuer,
                                                                                   @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                                                   @Nonnull String applicationId,
                                                                                   @Nonnull Map<UUID, IMFTrackFileInfo> trackFileInfoMap,
                                                                                   @Nonnull Path workingDirectory,
                                                                                   @Nonnull Path cplFile) throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        int numErrors = imfErrorLogger.getNumberOfErrors();
        Set<String> applicationIds = Collections.singleton(applicationId);
        String coreConstraintsSchema = CoreConstraints.fromApplicationId(applicationIds);
        if (coreConstraintsSchema == null)
            coreConstraintsSchema = CoreConstraints.NAMESPACE_IMF_2016;

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

        if(!Files.exists(cplFile)){
            throw new IMFAuthoringException(String.format("CompositionPlaylist file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
        }
        byte[] cplHash = IMFUtils.generateSHA1Hash(cplFile);

        /**
         * Build the PackingList
         */
        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        PackingListBuilder packingListBuilder = new PackingListBuilder(pklUUID,
                IMFUtils.createXMLGregorianCalendar(),
                workingDirectory,
                imfErrorLogger);

        org.smpte_ra.schemas._2067_2._2016.pkl.UserText pklAnnotationText = PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en");
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText creator = PackingListBuilder.buildPKLUserTextType_2016("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText pklIssuer = PackingListBuilder.buildPKLUserTextType_2016(issuer, "en");
        List<PackingListBuilder.PackingListBuilderAsset_2016> packingListBuilderAssets = new ArrayList<>();
        /**
         * Build the CPL asset to be entered into the PackingList
         */

        UUID cplUUID = IMFUtils.extractUUIDFromCPLFile(cplFile, imfErrorLogger);
        PackingListBuilder.PackingListBuilderAsset_2016 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2016(cplUUID,
                        PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en"),
                        Arrays.copyOf(cplHash, cplHash.length),
                        packingListBuilder.buildDefaultDigestMethodType(),
                        Files.size(cplFile),
                        PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                        PackingListBuilder.buildPKLUserTextType_2016(Utilities.getFilenameFromPath(cplFile), "en"));
        packingListBuilderAssets.add(cplAsset);
        Set<Map.Entry<UUID, IMFTrackFileInfo>> trackFileInfoEntriesSet = trackFileInfoMap.entrySet().stream().filter( e -> !(e.getValue().isExcludeFromPackage())).collect(Collectors.toSet());
        for(Map.Entry<UUID, IMFTrackFileInfo> entry : trackFileInfoEntriesSet){
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
        Path pklPath = workingDirectory.resolve(packingListBuilder.getPKLFileName());
        if(!Files.isRegularFile(pklPath)){
            throw new IMFAuthoringException(String.format("PackingList path does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
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
        AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(Utilities.getFilenameFromPath(pklPath), Files.size(pklPath));
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

        Path assetMapFile = workingDirectory.resolve(assetMapBuilder.getAssetMapFileName());
        if (!Files.isRegularFile(assetMapFile)) {
            throw new IMFAuthoringException(String.format("Error establishing path to AssetMap path in the working directory %s", workingDirectory.toString()));
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A method to generate the AssetMap, PackingList and CompositionPlaylist documents conforming to the
     * st0429-9:2007, st2067-2:2016 and st2067-2/3:2016 schemas respectively
     * @param annotationText a human readable text that will be used to annotate the corresponding elements of the XML documents
     * @param issuer a human readable text indicating the issuer of the XML documents
     * @param virtualTracks a list of all VirtualTracks that are a part of the Composition
     * @param compositionEditRate the EditRate of the composition
     * @param applicationId ApplicationId for the composition
     * @param trackFileInfoMap a Map of IMFTrackFileId to IMFTrackFileInfo
     * @param workingDirectory a folder location for the generated documents
     * @param essenceDescriptorDomNodeMap Map containing mapping between Track file ID to essence descriptor DOM node list
     * @return a list of errors that occurred while building an IMP
     * @throws IOException - any I/O related error will be exposed through an IOException
     * @throws ParserConfigurationException if a DocumentBuilder
     *   cannot be created which satisfies the configuration requested by the underlying builder implementation
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public static List<ErrorLogger.ErrorObject> buildIMP_2016(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<? extends Composition.VirtualTrack> virtualTracks,
                                                              @Nonnull Composition.EditRate compositionEditRate,
                                                              @Nonnull String applicationId,
                                                              @Nonnull Map<UUID, IMFTrackFileInfo> trackFileInfoMap,
                                                              @Nonnull Path workingDirectory,
                                                              @Nonnull Map<UUID, List<Node>> essenceDescriptorDomNodeMap)
            throws IOException, ParserConfigurationException, SAXException, JAXBException, URISyntaxException
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        int numErrors = imfErrorLogger.getNumberOfErrors();
        UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();
        Set<String> applicationIds = Collections.singleton(applicationId);
        String coreConstraintsSchema = CoreConstraints.fromApplicationId(applicationIds);
        if (coreConstraintsSchema == null)
            coreConstraintsSchema = CoreConstraints.NAMESPACE_IMF_2016;

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
        for(IMFTrackFileResourceType trackResource : (List<IMFTrackFileResourceType>)mainImageVirtualTrack.getResourceList()){
            totalNumberOfImageEditUnits += trackResource.getSourceDuration().longValue() * trackResource.getRepeatCount().longValue();
        }
        totalRunningTime = totalNumberOfImageEditUnits/(compositionEditRate.getNumerator()/compositionEditRate.getDenominator());

        List<IMFEssenceDescriptorBaseType> imfEssenceDescriptorBaseTypeList = new ArrayList<>();
        Map<UUID, UUID> trackFileIdToEssenceDescriptorIdMap = new HashMap<>();
        for(AbstractApplicationComposition.ResourceIdTuple resourceIdTuple : getResourceIdTuples(virtualTracks)) {
            trackFileIdToEssenceDescriptorIdMap.put(resourceIdTuple.getTrackFileId(), resourceIdTuple.getSourceEncoding());
        }
        for(Map.Entry<UUID, List<Node>> entry: essenceDescriptorDomNodeMap.entrySet()) {
            if(trackFileIdToEssenceDescriptorIdMap.containsKey(entry.getKey())) {
                imfEssenceDescriptorBaseTypeList.add(new IMFEssenceDescriptorBaseType(UUIDHelper.fromUUID(trackFileIdToEssenceDescriptorIdMap.get(entry.getKey())), new ArrayList<>( entry.getValue())));
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Resource = %s is not referenced in the virtual track.", UUIDHelper.fromUUID(entry.getKey())));
            }
        }

        CompositionPlaylistBuilder_2016 compositionPlaylistBuilder_2016 = new CompositionPlaylistBuilder_2016(cplUUID,
                CompositionPlaylistBuilder_2016.buildCPLUserTextType_2016(annotationText, "en"),
                CompositionPlaylistBuilder_2016.buildCPLUserTextType_2016(issuer, "en"),
                CompositionPlaylistBuilder_2016.buildCPLUserTextType_2016("Photon PackingListBuilder", "en"),
                virtualTracks,
                compositionEditRate,
                applicationIds,
                totalRunningTime,
                trackFileInfoMap,
                workingDirectory,
                imfEssenceDescriptorBaseTypeList,
                coreConstraintsSchema,
                trackFileIdToEssenceDescriptorIdMap);

        imfErrorLogger.addAllErrors(compositionPlaylistBuilder_2016.build());

        if(compositionPlaylistBuilder_2016.getErrors().stream().filter( e -> e.getErrorLevel().equals(IMFErrorLogger
                .IMFErrors.ErrorLevels.FATAL)).count() > 0) {
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the CompositionPlaylist. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;

        Path cplFile = workingDirectory.resolve(compositionPlaylistBuilder_2016.getCPLFileName());
        if (!Files.isRegularFile(cplFile)) {
            throw new IMFAuthoringException(String.format("CompositionPlaylist file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
        }
        byte[] cplHash = IMFUtils.generateSHA1Hash(cplFile);

        /**
         * Build the PackingList
         */
        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        PackingListBuilder packingListBuilder = new PackingListBuilder(pklUUID,
                IMFUtils.createXMLGregorianCalendar(),
                workingDirectory,
                imfErrorLogger);

        org.smpte_ra.schemas._2067_2._2016.pkl.UserText pklAnnotationText = PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en");
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText creator = PackingListBuilder.buildPKLUserTextType_2016("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText pklIssuer = PackingListBuilder.buildPKLUserTextType_2016(issuer, "en");
        List<PackingListBuilder.PackingListBuilderAsset_2016> packingListBuilderAssets = new ArrayList<>();
        /**
         * Build the CPL asset to be entered into the PackingList
         */
        PackingListBuilder.PackingListBuilderAsset_2016 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2016(cplUUID,
                        PackingListBuilder.buildPKLUserTextType_2016(annotationText, "en"),
                        Arrays.copyOf(cplHash, cplHash.length),
                        packingListBuilder.buildDefaultDigestMethodType(),
                        Files.size(cplFile),
                        PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                        PackingListBuilder.buildPKLUserTextType_2016(compositionPlaylistBuilder_2016.getCPLFileName(), "en"));
        packingListBuilderAssets.add(cplAsset);
        Set<Map.Entry<UUID, IMFTrackFileInfo>> trackFileInfoEntriesSet = trackFileInfoMap.entrySet().stream().filter( e -> !(e.getValue().isExcludeFromPackage())).collect(Collectors.toSet());
        for(Map.Entry<UUID, IMFTrackFileInfo> entry : trackFileInfoEntriesSet){
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

        Path pklPath = workingDirectory.resolve(packingListBuilder.getPKLFileName());
        if(!Files.isRegularFile(pklPath)){
            throw new IMFAuthoringException(String.format("PackingList file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.toString()));
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
        AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(Utilities.getFilenameFromPath(pklPath), Files.size(pklPath));
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

        Path assetMapPath = workingDirectory.resolve(assetMapBuilder.getAssetMapFileName());
        if(!Files.isRegularFile(assetMapPath)){
            throw new IMFAuthoringException(String.format("AssetMap path does not exist in the working directory %s", workingDirectory.toString()));
        }

        return imfErrorLogger.getErrors();
    }

    public static Map<UUID, List<Node>> buildEDLForVirtualTracks (Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap, List<? extends Composition.VirtualTrack> virtualTrackList, IMFErrorLogger imfErrorLogger) throws IOException, ParserConfigurationException{
        Map<UUID, List<Node>> imfEssenceDescriptorMap = new HashMap<>();

        for(Composition.VirtualTrack virtualTrack : virtualTrackList) {
            if (!(virtualTrack instanceof IMFEssenceComponentVirtualTrack)) {
                continue; // Skip non-essence tracks
            }

            Set<UUID> trackResourceIds = IMFEssenceComponentVirtualTrack.class.cast(virtualTrack).getTrackResourceIds();
            /**
             * Create the RegXML representation of the EssenceDescriptor metadata for every Resource of every VirtualTrack
             * of the Composition
             */
            for (UUID uuid : trackResourceIds) {
                IMPBuilder.IMFTrackFileMetadata imfTrackFileMetadata = imfTrackFileMetadataMap.get(uuid);
                if (imfTrackFileMetadata == null) {
                    throw new IMFAuthoringException(String.format("TrackFileHeaderMetadata for Track Resource Id %s within VirtualTrack Id %s is absent", uuid.toString(), virtualTrack.getTrackID()));
                }
                ByteProvider byteProvider = new ByteArrayDataProvider(imfTrackFileMetadata.getHeaderPartition());
                ResourceByteRangeProvider resourceByteRangeProvider = new ByteArrayByteRangeProvider(imfTrackFileMetadata.getHeaderPartition());
                //Create the HeaderPartition
                HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, (long) imfTrackFileMetadata.getHeaderPartition().length, imfErrorLogger);

                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = headerPartition.getEssenceDescriptors();
                for (InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                    KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                    List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
                    List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors(essenceDescriptor);
                    for (InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors) {
                        if (subDescriptorBO != null) {
                            subDescriptorHeaders.add(subDescriptorBO.getHeader());
                        }
                    }
                    /*Create a dom*/
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document document = docBuilder.newDocument();

                    RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(headerPartition.getPrimerPack().getHeader(), getByteProvider(resourceByteRangeProvider, headerPartition.getPrimerPack().getHeader()));

                    DocumentFragment documentFragment = getEssenceDescriptorAsDocumentFragment(regXMLLibHelper, document, essenceDescriptorHeader, subDescriptorHeaders, resourceByteRangeProvider, imfErrorLogger);
                    Node node = documentFragment.getFirstChild();
                    imfEssenceDescriptorMap.put(uuid, Arrays.asList(node));
                }
            }
        }
        return imfEssenceDescriptorMap;
    }

    private static DocumentFragment getEssenceDescriptorAsDocumentFragment(RegXMLLibHelper regXMLLibHelper,
                                                                           Document document,
                                                                           KLVPacket.Header essenceDescriptor,
                                                                           List<KLVPacket.Header>subDescriptors,
                                                                           ResourceByteRangeProvider resourceByteRangeProvider,
                                                                           IMFErrorLogger imfErrorLogger) throws MXFException, IOException {
        document.setXmlStandalone(true);

        Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, getByteProvider(resourceByteRangeProvider, essenceDescriptor));
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for(KLVPacket.Header subDescriptorHeader : subDescriptors){
            subDescriptorTriplets.add(regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, getByteProvider(resourceByteRangeProvider, subDescriptorHeader)));
        }
        return regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document, imfErrorLogger);
    }

    private static ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        return new ByteArrayDataProvider(bytes);
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
        private final boolean excludeFromPackage;

        /**
         * A constructor for the EssenceMetadata required to construct a CPL document
         * @param headerPartition a byte[] containing the EssenceHeaderPartition metadata of the IMFTrack file
         * @param hash a byte[] containing the SHA-1, Base64 encoded hash of the IMFTrack file
         * @param hashAlgorithm a string representing the Hash Algorithm used to generate the Hash of the IMFTrack file
         * @param originalFileName a string representing the FileName of the IMFTrack file
         * @param length a long integer represeting the length of the IMFTrack file
         * @param excludeFromPackage a boolean indicating if this track file needs to be excluded from package.
         *                           If excluded this file will not be referenced in asset map and packing list but may be referenced in CPL.
         */
        public IMFTrackFileMetadata(byte[] headerPartition, byte[] hash, String hashAlgorithm, String originalFileName, long length, boolean excludeFromPackage){
            this.headerPartition = Arrays.copyOf(headerPartition, headerPartition.length);
            this.hash = Arrays.copyOf(hash, hash.length);
            this.hashAlgorithm = hashAlgorithm;
            this.originalFileName = originalFileName;
            this.length = length;
            this.excludeFromPackage = excludeFromPackage;
        }

        public IMFTrackFileMetadata(byte[] headerPartition, byte[] hash, String hashAlgorithm, String originalFileName, long length){
            this( headerPartition, hash, hashAlgorithm, originalFileName, length, false);
        }

        /**
         * Getter for the HeaderPartition metadata for the IMFTrack file corresponding to the IMFTrackFile metadata
         * @return a byte[] containing the HeaderParition metadata
         */
        public byte[] getHeaderPartition(){
            return Arrays.copyOf(headerPartition, headerPartition.length);
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

        /**
         * Getter for the excludedFromPackage boolean flag
         * @return a boolean indicating of this file is excluded from the package
         */
        public boolean isExcludeFromPackage() {
            return excludeFromPackage;
        }
    }

    /**
     * A thin class representing the track file info required to construct a CPL document
     */
    public static class IMFTrackFileInfo {
        private final byte[] hash;
        private final String hashAlgorithm;
        private final String originalFileName;
        private final long length;
        private final boolean excludeFromPackage;

        /**
         * A constructor for the track file info required to construct a CPL document
         * @param hash a byte[] containing the SHA-1, Base64 encoded hash of the IMFTrack file
         * @param hashAlgorithm a string representing the Hash Algorithm used to generate the Hash of the IMFTrack file
         * @param originalFileName a string representing the FileName of the IMFTrack file
         * @param length a long integer represeting the length of the IMFTrack file
         * @param excludeFromPackage a boolean indicating if this track file needs to be excluded from package.
         *                           If excluded this file will not be referenced in asset map and packing list but may be referenced in CPL.
         */
        public IMFTrackFileInfo(byte[] hash, String hashAlgorithm, String originalFileName, long length, boolean excludeFromPackage){
            this.hash = Arrays.copyOf(hash, hash.length);
            this.hashAlgorithm = hashAlgorithm;
            this.originalFileName = originalFileName;
            this.length = length;
            this.excludeFromPackage = excludeFromPackage;
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

        /**
         * Getter for the excludedFromPackage boolean flag
         * @return a boolean indicating of this file is excluded from the package
         */
        public boolean isExcludeFromPackage() {
            return excludeFromPackage;
        }
    }
}
