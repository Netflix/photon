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
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Map<UUID, List<Long>> sampleRateMap = new HashMap<>();
        Map<UUID, BigInteger> sampleCountMap = new HashMap<>();
        Map<UUID, byte[]> hashMap = new HashMap<>();
        long videoTotalSourceDuration = 0;


        for (Track track : simpleTimeline.getEssenceTracks()) {
            // build cpl track here
            List<IMFTrackFileResourceType> trackFileResources = new ArrayList<>();

            for (TrackEntry trackEntry : track.getTrackEntries()) {
                if (trackEntry instanceof EssenceTrackEntry) {
                    EssenceTrackEntry essenceTrackEntry = (EssenceTrackEntry) trackEntry;
                    logger.info("track: {}, file: {}: path: {}", simpleTimeline.getEssenceTracks().indexOf(track), track.getTrackEntries().indexOf(trackEntry), essenceTrackEntry.getFile().getAbsolutePath());
                    ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(essenceTrackEntry.getFile());
                    PayloadRecord headerPartitionPayloadRecord = IMPFixer.getHeaderPartitionPayloadRecord(resourceByteRangeProvider, imfErrors);
                    if (headerPartitionPayloadRecord == null) {
                        throw new IOException("Could not get header partition for file: " + essenceTrackEntry.getFile().getAbsolutePath());
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
                        logger.info("Generating hash for file: {}", essenceTrackEntry.getFile().getAbsolutePath());
                        hash = IMFUtils.generateSHA1Hash(resourceByteRangeProvider);
                        hashMap.put(IMPFixer.getTrackFileId(headerPartitionPayloadRecord), hash);
                    }

                    UUID trackFileId = IMPFixer.getTrackFileId(headerPartitionPayloadRecord);
                    logger.info("UUID read from file: {}: {}", essenceTrackEntry.getFile().getName(), trackFileId.toString());
                    logger.info("Adding file {} to imfTrackFileMetadataMap", essenceTrackEntry.getFile().getName());
                    imfTrackFileMetadataMap.put(
                            trackFileId,
                            new IMPBuilder.IMFTrackFileMetadata(headerPartitionBytes,
                                    hash,   // a byte[] containing the SHA-1, Base64 encoded hash of the IMFTrack file
                                    CompositionPlaylistBuilder_2016.defaultHashAlgorithm,
                                    essenceTrackEntry.getFile().getName(),
                                    resourceByteRangeProvider.getResourceSize())
                    );

                    if (copyTrackFiles) {
                        File outputTrackFile = new File(outputDirectory.getAbsolutePath() + File.separator + essenceTrackEntry.getFile().getName());
                        logger.info("Copying track file from\n{} to\n{}", essenceTrackEntry.getFile().getAbsolutePath(), outputTrackFile.getAbsolutePath());
                        Files.copy(essenceTrackEntry.getFile().toPath(), outputTrackFile.toPath(), REPLACE_EXISTING);
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
                    Path tempFilePath = Paths.get(outputDirectory.getAbsolutePath(), "range");
                    logger.info("Deleting temporary file if it exists: {}", tempFilePath);
                    Files.deleteIfExists(tempFilePath);


                    // add to resources
                    logger.info("Adding file to resources: {}..", essenceTrackEntry.getFile().getName());

                    if (track.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                        videoTotalSourceDuration += (essenceTrackEntry.getDuration() == null ? sampleCount : essenceTrackEntry.getDuration()).longValue();
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
                    BigInteger.valueOf(videoTotalSourceDuration),
                    BigInteger.ZERO,
                    BigInteger.valueOf(videoTotalSourceDuration), // source duration may not be necessary
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
         * @param file - the MXF file
         * @param sampleRate - the sample rate, optional, introspected if null
         * @param intrinsicDuration - the intrinsic duration, optional, introspected if null
         * @param entryPoint - the entry point, if null, defaults to 0
         * @param duration - the duration, if null, defaults to intrinsic duration
         * @param repeatCount - the repeat count, if null, defaults to 1
         * @param hash - the SHA-1 hash of the file, optional, introspected if null
         */
        public EssenceTrackEntry(@Nonnull File file, @Nullable Composition.EditRate sampleRate, @Nullable BigInteger intrinsicDuration, @Nullable BigInteger entryPoint, @Nullable BigInteger duration, @Nullable BigInteger repeatCount, @Nullable byte[] hash) {
            this.file = file;
            this.sampleRate = sampleRate;
            this.intrinsicDuration = intrinsicDuration;
            this.entryPoint = entryPoint;
            this.duration = duration;
            this.repeatCount = repeatCount;
            this.hash = hash;
        }

        public EssenceTrackEntry(@Nonnull File file, @Nullable Composition.EditRate sampleRate, @Nullable BigInteger intrinsicDuration, @Nullable BigInteger entryPoint, @Nullable BigInteger duration, @Nullable BigInteger repeatCount) {
            this(file, sampleRate, intrinsicDuration, entryPoint, duration, repeatCount, null);
        }

        public EssenceTrackEntry() {
            this.file = null;
            this.sampleRate = null;
            this.intrinsicDuration = null;
            this.entryPoint = null;
            this.duration = null;
            this.repeatCount = null;
            this.hash = null;
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

        private File file;
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
