package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.app.IMPFixer;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFMarkerResourceType;
import com.netflix.imflibrary.st2067_2.IMFMarkerVirtualTrack;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_2.IMFMarkerType;
import com.netflix.imflibrary.utils.*;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


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
    public AssembledIMPResult assembleIMFFromFiles(SimpleTimeline simpleTimeline, Path outputDirectory, boolean copyTrackFiles) throws IOException, JAXBException, ParserConfigurationException, URISyntaxException, SAXException {
        Map<UUID, IMPBuilder.IMFTrackFileMetadata> imfTrackFileMetadataMap = new HashMap<>();
        IMFErrorLogger imfErrors = new IMFErrorLoggerImpl();
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>();
        Map<UUID, UUID> trackFileIdToResourceMap = new HashMap<>();
        Map<UUID, List<Long>> sampleRateMap = new HashMap<>();
        Map<UUID, BigInteger> sampleCountMap = new HashMap<>();
        Map<UUID, byte[]> hashMap = new HashMap<>();
        long videoIntrinsicDuration = 0;


        for (Track track : simpleTimeline.getEssenceTracks()) {
            // build cpl track here
            List<IMFTrackFileResourceType> trackFileResources = new ArrayList<>();

            for (TrackEntry trackEntry : track.getTrackEntries()) {
                if (trackEntry instanceof EssenceTrackEntry) {
                    EssenceTrackEntry essenceTrackEntry = (EssenceTrackEntry) trackEntry;
                    logger.info("track: {}, file: {}: path: {}", simpleTimeline.getEssenceTracks().indexOf(track), track.getTrackEntries().indexOf(trackEntry), essenceTrackEntry.getPath().toString());
                    ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(essenceTrackEntry.getPath());
                    PayloadRecord headerPartitionPayloadRecord = IMPFixer.getHeaderPartitionPayloadRecord(resourceByteRangeProvider, imfErrors);
                    if (headerPartitionPayloadRecord == null) {
                        throw new IOException("Could not get header partition for file: " + essenceTrackEntry.getPath().toString());
                    }
                    byte[] headerPartitionBytes = headerPartitionPayloadRecord.getPayload();

                    // get sha-1 hash or use cached value
                    byte[] hash = null;
                    if (essenceTrackEntry.getHash() != null) {
                        logger.info("Using hash from user: {}", essenceTrackEntry.getHash());
                        hash = essenceTrackEntry.getHash();
                        hashMap.put(IMPFixer.getTrackFileId(headerPartitionPayloadRecord), essenceTrackEntry.getHash());
                    } else if (hashMap.containsKey(IMPFixer.getTrackFileId(headerPartitionPayloadRecord))) {
                        logger.info("Using cached hash: {}", hashMap.get(IMPFixer.getTrackFileId(headerPartitionPayloadRecord)));
                        hash = hashMap.get(IMPFixer.getTrackFileId(headerPartitionPayloadRecord));
                    } else {
                        logger.info("Generating hash for file: {}", essenceTrackEntry.getPath().toString());
                        hash = IMFUtils.generateSHA1Hash(resourceByteRangeProvider);
                        hashMap.put(IMPFixer.getTrackFileId(headerPartitionPayloadRecord), hash);
                    }

                    String essenceTrackFilename = Utilities.getFilenameFromPath(essenceTrackEntry.getPath());

                    UUID trackFileId = IMPFixer.getTrackFileId(headerPartitionPayloadRecord);
                    logger.info("UUID read from file: {}: {}", essenceTrackFilename, trackFileId.toString());
                    logger.info("Adding file {} to imfTrackFileMetadataMap", essenceTrackFilename);
                    imfTrackFileMetadataMap.put(
                            trackFileId,
                            new IMPBuilder.IMFTrackFileMetadata(headerPartitionBytes,
                                    hash,   // a byte[] containing the SHA-1, Base64 encoded hash of the IMFTrack file
                                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                                    essenceTrackFilename,
                                    resourceByteRangeProvider.getResourceSize())
                    );

                    if (copyTrackFiles) {
                        Path outputTrackFile = outputDirectory.resolve(Utilities.getFilenameFromPath(essenceTrackEntry.getPath()));
                        logger.info("Copying track file from\n{} to\n{}", essenceTrackEntry.getPath().toString(), outputTrackFile.toString());
                        Files.copy(essenceTrackEntry.getPath(), outputTrackFile, REPLACE_EXISTING);
                    }

                    IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(outputDirectory, resourceByteRangeProvider);

                    // get sample rate or use cached value
                    List<Long> sampleRate = null;
                    if (essenceTrackEntry.getSampleRate() != null) {
                        // if user provided sample rate, use it
                        sampleRate = Arrays.asList(essenceTrackEntry.getSampleRate().getNumerator(), essenceTrackEntry.getSampleRate().getDenominator());
                        logger.info("Using sample rate from user: {}/{}", sampleRate.get(0), sampleRate.get(1));
                    } else if (!sampleRateMap.containsKey(trackFileId)) {
                        // sample rate has not already been found, find it
                        sampleRate = imfTrackFileReader.getEssenceEditRateAsList(imfErrors);
                        sampleRateMap.put(trackFileId, sampleRate);
                        logger.info("Found sample rate of: {}/{}", sampleRate.get(0), sampleRate.get(1));
                    } else {
                        sampleRate = sampleRateMap.get(trackFileId);
                        logger.info("Using cached sample rate of: {}/{}", sampleRate.get(0), sampleRate.get(1));
                    }


                    // get sample count or use cached value
                    BigInteger sampleCount = null;
                    if (essenceTrackEntry.getIntrinsicDuration() != null) {
                        // use sample count provided by user
                        sampleCount = essenceTrackEntry.getIntrinsicDuration();
                        logger.info("Intrinsic duration from user: {}", sampleCount);
                    } else if (!sampleCountMap.containsKey(trackFileId)) {
                        // compute sample count
                        sampleCount = imfTrackFileReader.getEssenceDuration(imfErrors);
                        sampleCountMap.put(trackFileId, sampleCount);
                        logger.info("Found essence duration of: {}", sampleCount);
                    } else {
                        // use cached sample count
                        sampleCount = sampleCountMap.get(trackFileId);
                        logger.info("Using cached intrinsic duration of: {}", sampleCount);
                    }

                    // delete temporary file left over from FileByteRangeProvider or ByteArrayByteRangeProvider
                    Path tempFilePath = outputDirectory.resolve("range");
                    logger.info("Deleting temporary file if it exists: {}", tempFilePath);
                    Files.deleteIfExists(tempFilePath);


                    // add to resources
                    logger.info("Adding file to resources: {}..", essenceTrackFilename);

                    if (track.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                        videoIntrinsicDuration += sampleCount.longValue();
                    }

                    trackFileResources.add(
                            new IMFTrackFileResourceType(
                                    UUIDHelper.fromUUID(IMFUUIDGenerator.getInstance().generateUUID()),
                                    UUIDHelper.fromUUID(trackFileId),
                                    sampleRate, // defaults to 1/1
                                    sampleCount,
                                    essenceTrackEntry.getEntryPoint(), // defaults to 0 if null
                                    essenceTrackEntry.getDuration() == null ? sampleCount : essenceTrackEntry.getDuration(), // defaults to intrinsic duration if null
                                    essenceTrackEntry.getRepeatCount(), // defaults to 1 if null
                                    UUIDHelper.fromUUID(getOrGenerateSourceEncoding(trackFileIdToResourceMap, trackFileId)),   // used as the essence descriptor id
                                    hash,
                                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm
                            )
                    );
                }
            }

            // add to virtual tracks
            logger.info("Creating virtual track..");
            Composition.VirtualTrack  virtualTrack = new IMFEssenceComponentVirtualTrack(
                        IMFUUIDGenerator.getInstance().generateUUID(),
                        track.getSequenceTypeEnum(),
                        trackFileResources,
                        simpleTimeline.getEditRate()
                );
            virtualTracks.add(virtualTrack);
        }

        for (Track track: simpleTimeline.getMarkerTracks()){

            List<IMFMarkerResourceType> markerResources = new ArrayList<>();
            List<IMFMarkerType> markerList = new ArrayList<>();

            for (TrackEntry trackEntry : track.getTrackEntries()) {
                if (trackEntry instanceof MarkerTrackEntry) {
                    MarkerTrackEntry markerTrackEntry = (MarkerTrackEntry) trackEntry;
                    IMFMarkerType marker = new IMFMarkerType(markerTrackEntry.getAnnotation(), markerTrackEntry.getLabel(), markerTrackEntry.getOffset());
                    markerList.add(marker);
                }
            }
            List<Long> editRate = new ArrayList<>();
            editRate.add(0, simpleTimeline.getEditRate().getNumerator());
            editRate.add(1, simpleTimeline.getEditRate().getDenominator());

            markerResources.add(new IMFMarkerResourceType(
                    UUIDHelper.fromUUID(IMFUUIDGenerator.getInstance().generateUUID()),
                    editRate,
                    BigInteger.valueOf(videoIntrinsicDuration),
                    BigInteger.ZERO,
                    BigInteger.valueOf(videoIntrinsicDuration), // source duration may not be necessary
                    BigInteger.ONE,
                    markerList));

            logger.info("Creating marker track..");
            Composition.VirtualTrack virtualTrack = new IMFMarkerVirtualTrack(IMFUUIDGenerator.getInstance().generateUUID(),
                    track.getSequenceTypeEnum(),
                    markerResources,
                    simpleTimeline.getEditRate());
            virtualTracks.add(virtualTrack);
        }

        logger.debug("Created list of virtual tracks: {}", virtualTracks);
        logger.debug("Created track file metadata map: {}", imfTrackFileMetadataMap);

        logger.info("Building IMP here: {}...", outputDirectory.toString());
        imfErrors.addAllErrors(IMPBuilder.buildIMP_2016("IMP",
                "Netflix",
                virtualTracks,
                simpleTimeline.getEditRate(),
                "http://www.smpte-ra.org/schemas/2067-21/2016",
                imfTrackFileMetadataMap,
                outputDirectory));

        logger.info("Listing files in output dir..");
        if (Files.isDirectory(outputDirectory)) {
            try (Stream<Path> filesStream = Files.list(outputDirectory)) {
                filesStream.forEach(file -> {
                    logger.info("File in output dir: {}", file.toString());
                });
            } catch (IOException e) {
                logger.error("Unable to list list files in output dir: {}", outputDirectory.toString());
                logger.error(e.toString());
            }
        }

        BasicMapProfileV2MappedFileSet mapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(outputDirectory);
        AssetMap assetMap = mapProfileV2MappedFileSet.getAssetMap();
        Path assetMapOutputFile = Paths.get(mapProfileV2MappedFileSet.getAbsoluteAssetMapURI());
        Path pklOutputFile = null;
        Path cplOutputFile = null;
        List<Path> outputTrackFiles = new ArrayList<>();

        if (assetMap.getPackingListAssets().size() > 1) {
            throw new IllegalArgumentException("More than one packing list found in the output asset map");
        }
        for (AssetMap.Asset packingListAsset: assetMap.getPackingListAssets()) {
            if (packingListAsset.isPackingList()) {
                pklOutputFile = outputDirectory.resolve(packingListAsset.getPath().toString());
                PackingList packingList = new PackingList(pklOutputFile);
                for (PackingList.Asset asset : packingList.getAssets()) {
                    logger.debug("Asset from packing list: {}", asset);
                    if (asset.getType().equals(PackingList.Asset.TEXT_XML_TYPE)
                            && ApplicationComposition.isCompositionPlaylist(new FileByteRangeProvider(outputDirectory.resolve(asset.getOriginalFilename())))) {
                        logger.info("Adding output CPL asset to response: {}", asset);
                        cplOutputFile = outputDirectory.resolve(asset.getOriginalFilename());
                    } else if (asset.getOriginalFilename() != null) {
                        logger.info("Adding output Track file to response: {}", asset);
                        outputTrackFiles.add(outputDirectory.resolve(asset.getOriginalFilename()));
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
        public Path getCpl() {
            return cpl;
        }

        public void setCpl(Path cpl) {
            this.cpl = cpl;
        }

        public Path getPkl() {
            return pkl;
        }

        public void setPkl(Path pkl) {
            this.pkl = pkl;
        }

        public Path getAssetMap() {
            return assetMap;
        }

        public void setAssetMap(Path assetMap) {
            this.assetMap = assetMap;
        }

        public List<ErrorLogger.ErrorObject> getErrors() {
            return errors;
        }

        public void setErrors(List<ErrorLogger.ErrorObject> errors) {
            this.errors = errors;
        }

        private Path cpl;
        private Path pkl;
        private Path assetMap;

        public List<Path> getTrackFiles() {
            return trackFiles;
        }

        public void setTrackFiles(List<Path> trackFiles) {
            this.trackFiles = trackFiles;
        }

        private List<Path> trackFiles;
        private List<ErrorLogger.ErrorObject> errors;

        /**
         * Constructor for an assembled IMP result
         * @param cpl - the CPL path
         * @param pkl - the PKL path
         * @param assetMap - the AssetMap path
         * @param trackFiles - a list of track files used in the IMP
         * @param errors - a list of errors encountered when creating the IMP
         */
        public AssembledIMPResult(Path cpl, Path pkl, Path assetMap, List<Path> trackFiles, List<ErrorLogger.ErrorObject> errors) {
            this.cpl = cpl;
            this.pkl = pkl;
            this.assetMap = assetMap;
            this.trackFiles = trackFiles;
            this.errors = errors;
        }
    }


    public static class SimpleTimeline {
        public SimpleTimeline() {
            this.essenceTracks = new ArrayList<>();
            this.markerTracks = new ArrayList<>();
        }

        /**
         * Constructor for a simple timeline
         * @param essenceTracks - a list of tracks to use in the timeline
         * @param editRate - the edit rate, must match the video frame rate
         */
        public SimpleTimeline(List<Track> essenceTracks, List<Track> markerTracks,Composition.EditRate editRate) {
            this.essenceTracks = essenceTracks;
            this.markerTracks = markerTracks;
            this.editRate = editRate;
        }

        public void setEssenceTracks(List<Track> essenceTracks) {
            this.essenceTracks = essenceTracks;
        }

        public void setEditRate(Composition.EditRate editRate) {
            this.editRate = editRate;
        }

        public List<Track> getEssenceTracks() {
            return essenceTracks;
        }

        public Composition.EditRate getEditRate() {
            return editRate;
        }

        public List<Track> getMarkerTracks() {
            return markerTracks;
        }

        public void setMarkerTracks(List<Track> markerTracks) {
            this.markerTracks = markerTracks;
        }

        private List<Track> essenceTracks;
        private List<Track> markerTracks;
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

        private Composition.SequenceTypeEnum sequenceTypeEnum;
        private List<TrackEntry> trackEntries;
    }

    public static class EssenceTrackEntry extends TrackEntry{
        /**
         * Constructor for a track entry to be used to construct a simple timeline
         * @param path - the MXF file
         * @param sampleRate - the sample rate, optional, introspected if null
         * @param intrinsicDuration - the intrinsic duration, optional, introspected if null
         * @param entryPoint - the entry point, if null, defaults to 0
         * @param duration - the duration, if null, defaults to intrinsic duration
         * @param repeatCount - the repeat count, if null, defaults to 1
         * @param hash - the SHA-1 hash of the file, optional, introspected if null
         */
        public EssenceTrackEntry(@Nonnull Path path, @Nullable Composition.EditRate sampleRate, @Nullable BigInteger intrinsicDuration, @Nullable BigInteger entryPoint, @Nullable BigInteger duration, @Nullable BigInteger repeatCount, @Nullable byte[] hash) {
            this.path = path;
            this.sampleRate = sampleRate;
            this.intrinsicDuration = intrinsicDuration;
            this.entryPoint = entryPoint;
            this.duration = duration;
            this.repeatCount = repeatCount;
            this.hash = hash;
        }

        public EssenceTrackEntry(@Nonnull Path path, @Nullable Composition.EditRate sampleRate, @Nullable BigInteger intrinsicDuration, @Nullable BigInteger entryPoint, @Nullable BigInteger duration, @Nullable BigInteger repeatCount) {
            this(path, sampleRate, intrinsicDuration, entryPoint, duration, repeatCount, null);
        }

        public EssenceTrackEntry() {
            this.path = null;
            this.sampleRate = null;
            this.intrinsicDuration = null;
            this.entryPoint = null;
            this.duration = null;
            this.repeatCount = null;
            this.hash = null;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path file) {
            this.path = file;
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

        private Path path;
        private Composition.EditRate sampleRate;
        private BigInteger intrinsicDuration;
        private BigInteger entryPoint;
        private BigInteger duration;
        private BigInteger repeatCount;

        public byte[] getHash() {
            return hash;
        }

        private void setHash(byte[] hash) {
            this.hash = hash;
        }

        public byte[] hash;
    }

    public static class MarkerTrackEntry extends TrackEntry{
        private String annotation;
        private IMFMarkerType.Label label;
        private BigInteger offset;

        public MarkerTrackEntry(String annotation, IMFMarkerType.Label label, BigInteger offset) {
            this.annotation = annotation;
            this.label = label;
            this.offset = offset;
        }

        public String getAnnotation() {
            return annotation;
        }

        public IMFMarkerType.Label getLabel() {
            return label;
        }

        public BigInteger getOffset() {
            return offset;
        }
    }
    public static abstract class TrackEntry {

    }
}
