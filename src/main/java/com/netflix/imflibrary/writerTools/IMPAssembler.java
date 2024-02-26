package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMPFixer;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class IMPAssembler {

    private static final Logger logger = LoggerFactory.getLogger(IMPAssembler.class);



    /**
     * Generate the CPL, PKL, and AssetMap XML files given a simple timeline of track entries
     * Code adapted from IMPFixer
     * @param simpleTimeline - a timeline tracks of track entries
     * @param outputDirectory - the destination directory for the generated files
     * @param copyTrackFiles - whether to copy the track files to the output directory
     */
    public AssembledIMPResult assembleIMFFromFiles(SimpleTimeline simpleTimeline, File outputDirectory, boolean copyTrackFiles) throws IOException, JAXBException, ParserConfigurationException, URISyntaxException, SAXException {
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        IMFErrorLogger imfErrors = new IMFErrorLoggerImpl();
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>();
        Map<UUID, UUID> trackFileIdToResourceMap = new HashMap<>();


        for (Track track : simpleTimeline.getTracks()) {
            // build cpl track here
            List<IMFTrackFileResourceType> resources = new ArrayList<>();

            for (TrackEntry trackEntry : track.getTrackEntries()) {
                logger.info("track: {}, file: {}: path: {}", simpleTimeline.getTracks().indexOf(track), track.getTrackEntries().indexOf(trackEntry), trackEntry.getFile().getAbsolutePath());
                ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(trackEntry.getFile());
                PayloadRecord headerPartitionPayloadRecord = IMPFixer.getHeaderPartitionPayloadRecord(resourceByteRangeProvider, imfErrors);
                if (headerPartitionPayloadRecord == null) {
                    throw new IOException("Could not get header partition for file: " + trackEntry.getFile().getAbsolutePath());
                }
                byte[] headerPartitionBytes = headerPartitionPayloadRecord.getPayload();
                byte[] hash = IMFUtils.generateSHA1Hash(resourceByteRangeProvider);

                UUID trackFileId = IMPFixer.getTrackFileId(headerPartitionPayloadRecord);
                logger.info("UUID read from file: {}: {}", trackEntry.getFile().getName(), trackFileId.toString());
                logger.info("Adding file {} to imfTrackFileMetadataMap", trackEntry.getFile().getName());
                imfTrackFileMetadataMap.put(
                        trackFileId,
                        new IMPBuilder.IMFTrackFileMetadata(headerPartitionBytes,
                                hash,
                                CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                                trackEntry.getFile().getName(),
                                resourceByteRangeProvider.getResourceSize())
                );

                if (copyTrackFiles) {
                    File outputTrackFile = new File(outputDirectory.getAbsolutePath() + File.separator + trackEntry.getFile().getName());
                    logger.info("Copying track file from\n{} to\n{}", trackEntry.getFile().getAbsolutePath(), outputTrackFile.getAbsolutePath());
                    Files.copy(trackEntry.getFile().toPath(), outputTrackFile.toPath(), REPLACE_EXISTING);
                }

                // add to resources
                logger.info("Adding file to resources: {}..", trackEntry.getFile().getName());
                resources.add(
                        new IMFTrackFileResourceType(
                            UUIDHelper.fromUUID(IMFUUIDGenerator.getInstance().generateUUID()),
                            UUIDHelper.fromUUID(trackFileId),
                            Arrays.asList(trackEntry.getSampleRate().getNumerator(), trackEntry.getSampleRate().getDenominator()),    // defaults to 1/1
                            trackEntry.getIntrinsicDuration(),
                            trackEntry.getEntryPoint(), // defaults to 0 if null
                            trackEntry.getDuration(), // defaults to intrinsic duration if null
                            trackEntry.getRepeatCount(), // defaults to 1 if null
                            UUIDHelper.fromUUID(getOrGenerateSourceEncoding(trackFileIdToResourceMap, trackFileId)),   // used as the essence descriptor id
                            hash,
                            CompositionPlaylistBuilder_2016.defaultHashAlgorithm
                        )
                );
            }
            // add to virtual tracks
            logger.info("Creating virtual track..");
            Composition.VirtualTrack virtualTrack = new IMFEssenceComponentVirtualTrack(
                    IMFUUIDGenerator.getInstance().generateUUID(),
                    track.getSequenceTypeEnum(),
                    resources,
                    simpleTimeline.getEditRate()
            );
            virtualTracks.add(virtualTrack);
        }

        logger.debug("Created list of virtual tracks: {}", virtualTracks);
        logger.debug("Created track file metadata map: {}", imfTrackFileMetadataMap);

        logger.info("Building IMP here: {}...", outputDirectory.getAbsolutePath());
        imfErrors.addAllErrors(IMPBuilder.buildIMP_2016("IMP",
                "Netflix",
                virtualTracks,
                simpleTimeline.getEditRate(),
                "http://www.smpte-ra.org/schemas/2067-21/2016",
                imfTrackFileMetadataMap,
                outputDirectory));

        logger.info("Listing files in output dir..");
        if (outputDirectory.isDirectory()) {
            File[] files = outputDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file != null) {
                        logger.info("File in output dir: {}", file.getAbsolutePath());
                    }
                }
            }
        }

        BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(outputDirectory);
        AssetMap assetMap = mapProfileV2MappedFileSet.getAssetMap();
        File assetMapOutputFile = new File(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI());
        File pklOutputFile = null;
        File cplOutputFile = null;
        List<File> outputTrackFiles = new ArrayList<>();

        if (assetMap.getPackingListAssets().size() > 1) {
            throw new IllegalArgumentException("More than one packing list found in the output asset map");
        }
        for (AssetMap.Asset packingListAsset: assetMap.getPackingListAssets()) {
            if (packingListAsset.isPackingList()) {
                pklOutputFile = new File(outputDirectory, packingListAsset.getPath().toString());
                PackingList packingList = new PackingList(pklOutputFile);
                for (PackingList.Asset asset : packingList.getAssets()) {
                    logger.debug("Asset from packing list: {}", asset);
                    if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)
                            && ApplicationComposition.isCompositionPlaylist(new FileByteRangeProvider((new File(outputDirectory, asset.getOriginalFilename()))))) {
                        logger.info("Adding output CPL asset to response: {}", asset);
                        cplOutputFile = new File(outputDirectory, asset.getOriginalFilename());
                    } else if (asset.getOriginalFilename() != null) {
                        logger.info("Adding output track file to response: {}", asset);
                        outputTrackFiles.add(new File(outputDirectory, asset.getOriginalFilename()));
                    }
                }
            }
        }

        return new AssembledIMPResult(
                cplOutputFile,
                pklOutputFile,
                assetMapOutputFile,
                outputTrackFiles,
                imfErrors.getErrors()
        );
    }


    /**
     * Gets or generates the SourceEncoding UUID for a given trackFileId
     * @param trackFileIdToResourceMap - a map of trackFileId to SourceEncoding UUID
     * @param trackFileId - the trackFileId to look up
     * @return the SourceEncoding UUID
     */
    private static UUID getOrGenerateSourceEncoding(Map<UUID, UUID> trackFileIdToResourceMap, UUID trackFileId) {
        if (trackFileIdToResourceMap.containsKey(trackFileId)) {
            return trackFileIdToResourceMap.get(trackFileId);
        } else {
            UUID sourceEncoding = IMFUUIDGenerator.getInstance().generateUUID();
            trackFileIdToResourceMap.put(trackFileId, sourceEncoding);
            return sourceEncoding;
        }
    }


    /**
     * Contains the paths to the generated IMP files and a list of errors encountered when creating the IMP
     */
    public static class AssembledIMPResult {
        public File getCpl() {
            return cpl;
        }

        public void setCpl(File cpl) {
            this.cpl = cpl;
        }

        public File getPkl() {
            return pkl;
        }

        public void setPkl(File pkl) {
            this.pkl = pkl;
        }

        public File getAssetMap() {
            return assetMap;
        }

        public void setAssetMap(File assetMap) {
            this.assetMap = assetMap;
        }

        public List<ErrorLogger.ErrorObject> getErrors() {
            return errors;
        }

        public void setErrors(List<ErrorLogger.ErrorObject> errors) {
            this.errors = errors;
        }

        private File cpl;
        private File pkl;
        private File assetMap;

        public List<File> getTrackFiles() {
            return trackFiles;
        }

        public void setTrackFiles(List<File> trackFiles) {
            this.trackFiles = trackFiles;
        }

        private List<File> trackFiles;
        private List<ErrorLogger.ErrorObject> errors;

        /**
         * Constructor for an assembled IMP result
         * @param cpl - the CPL file
         * @param pkl - the PKL file
         * @param assetMap - the AssetMap file
         * @param trackFiles - a list of track files used in the IMP
         * @param errors - a list of errors encountered when creating the IMP
         */
        public AssembledIMPResult(File cpl, File pkl, File assetMap, List<File> trackFiles, List<ErrorLogger.ErrorObject> errors) {
            this.cpl = cpl;
            this.pkl = pkl;
            this.assetMap = assetMap;
            this.trackFiles = trackFiles;
            this.errors = errors;
        }
    }


    public static class SimpleTimeline {
        public SimpleTimeline() {
            this.tracks = new ArrayList<>();
        }

        /**
         * Constructor for a simple timeline
         * @param tracks - a list of tracks to use in the timeline
         * @param editRate - the edit rate, must match the video frame rate
         */
        public SimpleTimeline(List<Track> tracks, Composition.EditRate editRate) {
            this.tracks = tracks;
            this.editRate = editRate;
        }

        public void setTracks(List<Track> tracks) {
            this.tracks = tracks;
        }

        public void setEditRate(Composition.EditRate editRate) {
            this.editRate = editRate;
        }

        public List<Track> getTracks() {
            return tracks;
        }

        public Composition.EditRate getEditRate() {
            return editRate;
        }

        private List<Track> tracks;
        public Composition.EditRate editRate;
    }

    public static class Track {
        public Track() {
            this.trackEntries = new ArrayList<>();
        }

        /**
         * Constructor for a track to be used to construct a simple timeline
         * @param trackEntries - a list of entries to use in the track, can contain edits
         * @param sequenceTypeEnum - describes whether the track is a video, audio, etc..
         */
        public Track(List<TrackEntry> trackEntries, Composition.SequenceTypeEnum sequenceTypeEnum) {
            this.trackEntries = trackEntries;
            this.sequenceTypeEnum = sequenceTypeEnum;
        }

        public Composition.SequenceTypeEnum getSequenceTypeEnum() {
            return sequenceTypeEnum;
        }

        public void setSequenceTypeEnum(Composition.SequenceTypeEnum sequenceTypeEnum) {
            this.sequenceTypeEnum = sequenceTypeEnum;
        }

        public List<TrackEntry> getTrackEntries() {
            return trackEntries;
        }

        public void setTrackEntries(List<TrackEntry> trackEntries) {
            this.trackEntries = trackEntries;
        }

        public Composition.SequenceTypeEnum sequenceTypeEnum;
        public List<TrackEntry> trackEntries;
    }

    public static class TrackEntry {
        /**
         * Constructor for a track entry to be used to construct a simple timeline
         * @param file - the MXF file
         * @param sampleRate - the sample rate
         * @param intrinsicDuration - the intrinsic duration
         * @param entryPoint - the entry point (if null, defaults to 0)
         * @param duration - the duration (if null, defaults to intrinsic duration)
         * @param repeatCount - the repeat count (if null, defaults to 1)
         */
        public TrackEntry(@Nonnull File file, @Nonnull Composition.EditRate sampleRate, @Nonnull BigInteger intrinsicDuration, @Nullable BigInteger entryPoint, @Nullable BigInteger duration, @Nullable BigInteger repeatCount) {
            this.file = file;
            this.sampleRate = sampleRate;
            this.intrinsicDuration = intrinsicDuration;
            this.entryPoint = entryPoint;
            this.duration = duration;
            this.repeatCount = repeatCount;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public Composition.EditRate getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(Composition.EditRate sampleRate) {
            this.sampleRate = sampleRate;
        }

        public BigInteger getIntrinsicDuration() {
            return intrinsicDuration;
        }

        public void setIntrinsicDuration(BigInteger intrinsicDuration) {
            this.intrinsicDuration = intrinsicDuration;
        }

        public BigInteger getEntryPoint() {
            return entryPoint;
        }

        public void setEntryPoint(BigInteger entryPoint) {
            this.entryPoint = entryPoint;
        }

        public BigInteger getDuration() {
            return duration;
        }

        public void setDuration(BigInteger duration) {
            this.duration = duration;
        }

        public BigInteger getRepeatCount() {
            return repeatCount;
        }

        public void setRepeatCount(BigInteger repeatCount) {
            this.repeatCount = repeatCount;
        }

        public File file;
        public Composition.EditRate sampleRate;
        public BigInteger intrinsicDuration;
        public BigInteger entryPoint;
        public BigInteger duration;
        public BigInteger repeatCount;
    }
}
