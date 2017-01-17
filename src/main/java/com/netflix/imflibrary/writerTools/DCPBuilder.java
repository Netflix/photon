package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.DCPErrorLogger;
import com.netflix.imflibrary.DCPErrorLoggerImpl;
import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.DCPAuthoringException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileDataProvider;
import com.netflix.imflibrary.utils.IMFTrackFilePartitionsExtractor;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by schakrovorthy on 1/14/17.
 */
public class DCPBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DCPBuilder.class);

    public static List<ErrorLogger.ErrorObject> buildDCPPackage(@Nonnull String annotationText,
                                                              @Nonnull String issuer,
                                                              @Nonnull List<File> trackFiles,
                                                              @Nonnull File workingDirectory) throws IOException, URISyntaxException {
        DCPErrorLogger dcpErrorLogger = new DCPErrorLoggerImpl();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        int numErrors = imfErrorLogger.getNumberOfErrors();
        UUID cplUUID = IMFUUIDGenerator.getInstance().generateUUID();

        File cplFile = new DCPCompositionPlaylistBuilder(annotationText, issuer, cplUUID, trackFiles, workingDirectory).build();

        UUID pklUUID = IMFUUIDGenerator.getInstance().generateUUID();
        /**
         * Build the PackingList
         */
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
        byte[] cplHash = IMFUtils.generateSHA1HashAndBase64Encode(cplFile);
        PackingListBuilder.PackingListBuilderAsset_2007 cplAsset =
                new PackingListBuilder.PackingListBuilderAsset_2007(cplUUID,
                        PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                        Arrays.copyOf(cplHash, cplHash.length),
                        cplFile.length(),
                        PackingListBuilder.PKLAssetTypeEnum.TEXT_XML,
                        PackingListBuilder.buildPKLUserTextType_2007(cplFile.getName(), "en"));
        packingListBuilderAssets.add(cplAsset);

        for(File trackFile : trackFiles){
            byte[] hash = IMFUtils.generateSHA1HashAndBase64Encode(trackFile);
            File headerPartitionFile = IMFTrackFilePartitionsExtractor.extractHeaderPartition(trackFile, workingDirectory);
            HeaderPartition headerPartition = new HeaderPartition(new FileDataProvider(headerPartitionFile),
                    0L,
                    headerPartitionFile.length(),
                    imfErrorLogger);
            Preface preface = headerPartition.getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            UUID trackFileId = filePackage.getPackageMaterialNumberasUUID();

            PackingListBuilder.PackingListBuilderAsset_2007 asset =
                    new PackingListBuilder.PackingListBuilderAsset_2007(trackFileId, //Get the UUID of the track
                            PackingListBuilder.buildPKLUserTextType_2007(annotationText, "en"),
                            hash,
                            trackFile.length(),
                            PackingListBuilder.PKLAssetTypeEnum.APP_MXF,
                            PackingListBuilder.buildPKLUserTextType_2007(trackFile.getName(), "en"));
            packingListBuilderAssets.add(asset);
        }

        imfErrorLogger.addAllErrors(packingListBuilder.buildPackingList_2007(pklAnnotationText, pklIssuer, creator,
                packingListBuilderAssets));

        imfErrorLogger.addAllErrors(packingListBuilder.getErrors());

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new DCPAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }
        numErrors = (imfErrorLogger.getNumberOfErrors() > 0) ? imfErrorLogger.getNumberOfErrors()-1 : 0;

        File pklFile = new File(workingDirectory + File.separator + packingListBuilder.getPKLFileName());
        if(!pklFile.exists()){
            throw new DCPAuthoringException(String.format("PackingList file does not exist in the working directory %s, cannot generate the rest of the documents", workingDirectory.getAbsolutePath
                    ()));
        }

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
            throw new DCPAuthoringException(String.format("Fatal errors occurred while generating the AssetMap. Please see following error messages %s",
                    Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, numErrors, imfErrorLogger.getNumberOfErrors()))));
        }

        File assetMapFile = new File(workingDirectory + File.separator + assetMapBuilder.getAssetMapFileName());
        if(!assetMapFile.exists()){
            throw new DCPAuthoringException(String.format("AssetMap file does not exist in the working directory %s", workingDirectory.getAbsolutePath
                    ()));
        }
        return dcpErrorLogger.translateIMFErrors(imfErrorLogger.getErrors());
    }

    private static void usage(){
        logger.info(String.format("\\t DCPBuilder usage \\n"));
        logger.info(String.format("\\t -a <annotationText> \\n"));
        logger.info(String.format("\\t -i <issuer> \\n"));
        logger.info(String.format("\\t -af <AudioTrackFile> \\n"));
        logger.info(String.format("\\t -vf <VideoTrackFile> \\n"));
        logger.info(String.format("\\t -w <WorkingFolder> \\n"));
        System.exit(-1);
    }
    public static void main(String[] args) throws IOException, URISyntaxException{
        if(args.length < 6){
            DCPBuilder.usage();
        }
        List<File> trackFiles = new ArrayList<>();
        File audioFile = new File(args[3]);

        if(!audioFile.exists()){
            throw new IOException(String.format("%s file does not exist", audioFile.toString()));
        }

        File videoFile = new File(args[4]);
        if(!videoFile.exists()){
            throw new IOException(String.format("%s file does not exist", videoFile.toString()));
        }

        File workingFolder = new File(args[5]);
        if(!workingFolder.exists()){
            throw new IOException(String.format("Working Folder %s does not exist", workingFolder.toString()));
        }

        trackFiles.add(audioFile);
        trackFiles.add(videoFile);
        DCPBuilder.buildDCPPackage(args[1],
                args[2],
                trackFiles,
                workingFolder);
    }
}
