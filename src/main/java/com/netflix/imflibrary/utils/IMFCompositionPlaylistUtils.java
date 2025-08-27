package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.CoreConstraints;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.*;


public class IMFCompositionPlaylistUtils {

    public static boolean hasIABVirtualTracks(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist) {
        Iterator iterator = imfCompositionPlaylist.getVirtualTrackMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceType().equals("IABSequence")) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasMGASADMVirtualTracks(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist){
        Iterator iterator = imfCompositionPlaylist.getVirtualTrackMap().entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();
            if (virtualTrack.getSequenceType().equals("MGASADMSignalSequence")) {
                return true;
            }
        }

        return false;
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
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(audioVirtualTrack.getSequenceType() != CoreConstraints.MAIN_AUDIO_SEQUENCE){
            throw new IMFException(String.format("Virtual track that was passed in is of type %s, spoken language is " +
                            "currently supported for only %s tracks", audioVirtualTrack.getSequenceType(),
                    CoreConstraints.MAIN_AUDIO_SEQUENCE));
        }
        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>();
        virtualTracks.add(audioVirtualTrack);

        //imfErrorLogger.addAllErrors(checkVirtualTrackAndEssencesHeaderPartitionPayloadRecords(virtualTracks, essencesHeaderPartition));
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
        List<IMFCompositionPlaylist> imfCompositionPlaylists = new ArrayList<>();
        try
        {
            IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new ByteArrayByteRangeProvider(referenceCPLPayloadRecord.getPayload()));
            imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
            if (imfErrorLogger.hasFatalErrors()) {
                return imfErrorLogger.getErrors();
            }

            imfCompositionPlaylists.add(imfCompositionPlaylist);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        for (PayloadRecord cpl : cplPayloadRecords) {
            try
            {
                IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new ByteArrayByteRangeProvider(cpl.getPayload()));
                imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
                if (imfErrorLogger.hasFatalErrors()) {
                    return imfErrorLogger.getErrors();
                }

                imfCompositionPlaylists.add(imfCompositionPlaylist);
            }
            catch(IMFException e)
            {
                imfErrorLogger.addAllErrors(e.getErrors());
            }
        }

        if(imfErrorLogger.hasFatalErrors()) {
            return imfErrorLogger.getErrors();
        }

        Composition.VirtualTrack referenceVideoVirtualTrack = imfCompositionPlaylists.get(0).getVideoVirtualTrack();
        UUID referenceCPLUUID = imfCompositionPlaylists.get(0).getUUID();
        for (int i = 1; i < imfCompositionPlaylists.size(); i++) {
            if (!referenceVideoVirtualTrack.equivalent(imfCompositionPlaylists.get(i).getVideoVirtualTrack())) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since the video virtual tracks do not seem to represent the same timeline.", imfCompositionPlaylists.get(i).getUUID(), referenceCPLUUID));
            }
        }

        /**
         * Perform AudioTrack mergeability checks
         * 1) Identify AudioTracks that are the same language
         * 2) Compare language tracks to see if they represent the same timeline
         */
        Boolean bAudioVirtualTrackMapFail = false;
        List<Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack>> audioVirtualTracksMapList = new ArrayList<>();
        for (IMFCompositionPlaylist imfCompositionPlaylist : imfCompositionPlaylists) {
            try {
                audioVirtualTracksMapList.add(imfCompositionPlaylist.getAudioVirtualTracksMap());
            }
            catch(IMFException e)
            {
                bAudioVirtualTrackMapFail = false;
                imfErrorLogger.addAllErrors(e.getErrors());
            }
        }


        if(!bAudioVirtualTrackMapFail) {
            Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> referenceAudioVirtualTracksMap = audioVirtualTracksMapList.get(0);
            for (int i = 1; i < audioVirtualTracksMapList.size(); i++) {
                if (!compareAudioVirtualTrackMaps(Collections.unmodifiableMap(referenceAudioVirtualTracksMap), Collections.unmodifiableMap(audioVirtualTracksMapList.get(i)), imfErrorLogger)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since 2 same language audio tracks do not seem to represent the same timeline.", imfCompositionPlaylists.get(i).getUUID(), referenceCPLUUID));
                }
            }
        }

        /**
         * Perform MarkerTrack mergeability checks
         */
        Composition.VirtualTrack referenceMarkerVirtualTrack = imfCompositionPlaylists.get(0).getMarkerVirtualTrack();
        if (referenceMarkerVirtualTrack != null) {
            UUID referenceMarkerCPLUUID = imfCompositionPlaylists.get(0).getUUID();
            for (int i = 1; i < imfCompositionPlaylists.size(); i++) {
                if (!referenceMarkerVirtualTrack.equivalent(imfCompositionPlaylists.get(i).getMarkerVirtualTrack())) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("CPL Id %s can't be merged with Reference CPL Id %s, since the marker virtual tracks do not seem to represent the same timeline.", imfCompositionPlaylists.get(i).getUUID(), referenceMarkerCPLUUID));
                }
            }
        }

        return imfErrorLogger.getErrors();
    }



    private static boolean compareAudioVirtualTrackMaps(Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> map1, Map<Set<DOMNodeObjectModel>, ? extends Composition.VirtualTrack> map2, IMFErrorLogger imfErrorLogger){
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



    /**
     * A stateless method to retrieve all the VirtualTracks that are a part of a Composition
     * @param cpl - a payload corresponding to the Composition Playlist
     * @return list of VirtualTracks
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<? extends Composition.VirtualTrack> getVirtualTracks(PayloadRecord cpl) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(new ByteArrayByteRangeProvider(cpl.getPayload()));
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        if(imfErrorLogger.hasFatalErrors()) {
            throw new IMFException("Virtual track failed validation", imfErrorLogger);
        }

        return imfCompositionPlaylist.getVirtualTracks();
    }



    public static Boolean isCompositionComplete(IMFCompositionPlaylist IMFCompositionPlaylist, Set<UUID> trackFileIDsSet, IMFErrorLogger imfErrorLogger) throws IOException {
        boolean bComplete = true;
        for (IMFEssenceComponentVirtualTrack virtualTrack : IMFCompositionPlaylist.getEssenceVirtualTracks()) {
            for (UUID uuid : virtualTrack.getTrackResourceIds()) {
                if (!trackFileIDsSet.contains(uuid)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes
                                    .IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                            String.format("CPL resource %s is not present in the package", uuid.toString()));
                    bComplete &= false;
                }
            }
        }

        return bComplete;
    }


    public static Boolean isVirtualTrackComplete(IMFEssenceComponentVirtualTrack virtualTrack, Set<UUID> trackFileIDsSet) throws IOException {

        for (UUID uuid : virtualTrack.getTrackResourceIds()) {
            if (!trackFileIDsSet.contains(uuid)) {
                return false;
            }
        }

        return true;

    }









}
