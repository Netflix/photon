package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.smpte_ra.schemas.st2067_2_2013.BaseResourceType;
import org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType;
import org.smpte_ra.schemas.st2067_2_2013.SegmentType;
import org.smpte_ra.schemas.st2067_2_2013.SequenceType;
import org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class that performs 2013 CoreConstraints related checks on the elements of a Composition Playlist. It plays a significant role in
 * reading and parsing aspects of a Composition such as VirtualTracks, Segments, Sequences and Resources.
 */
public class IMFCoreConstraintsChecker_st2067_2_2013 {

    public static Map<UUID, CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013> checkVirtualTracks(CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>>virtualTrackResourceMap =  populateVirtualTrackResourceList(compositionPlaylistType, imfErrorLogger);

        boolean foundMainImageEssence = false;

        //process first segment to create virtual track map
        org.smpte_ra.schemas.st2067_2_2013.SegmentType segment = compositionPlaylistType.getSegmentList().getSegment().get(0);
        org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence;
        sequence = segment.getSequenceList().getMarkerSequence();
        if (sequence != null)
        {
            UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
            if (virtualTrackMap.get(uuid) == null)
            {
                List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> virtualTrackResourceList = null;
                if(virtualTrackResourceMap.get(uuid) == null){
                    virtualTrackResourceList = new ArrayList<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>();
                }
                else{
                    virtualTrackResourceList = virtualTrackResourceMap.get(uuid);
                }
                CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013 virtualTrack = new CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013(uuid, Composition.SequenceTypeEnum.MarkerSequence, virtualTrackResourceList);
                virtualTrackMap.put(uuid, virtualTrack);
            }
            else
            {
                String message = String.format(
                        "First segment in Composition XML file has multiple occurrences of virtual track UUID %s", uuid);
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }
            }
        }

        for (Object object : segment.getSequenceList().getAny())
        {
            if(!(object instanceof JAXBElement)){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                continue;
            }
            JAXBElement jaxbElement = (JAXBElement)(object);
            String name = jaxbElement.getName().getLocalPart();
            sequence = (org.smpte_ra.schemas.st2067_2_2013.SequenceType)(jaxbElement).getValue();
            if (sequence != null)
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                if (virtualTrackMap.get(uuid) == null)
                {
                    List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> virtualTrackResourceList = null;
                    if(virtualTrackResourceMap.get(uuid) == null){
                        virtualTrackResourceList = new ArrayList<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>();
                    }
                    else{
                        virtualTrackResourceList = virtualTrackResourceMap.get(uuid);
                    }
                    checkTrackResourceList(virtualTrackResourceList, imfErrorLogger);
                    CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013 virtualTrack = new CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013(uuid, Composition.SequenceTypeEnum.getSequenceTypeEnum(name), virtualTrackResourceList);
                    virtualTrackMap.put(uuid, virtualTrack);
                    if(Composition.SequenceTypeEnum.getSequenceTypeEnum(name) == Composition.SequenceTypeEnum.MainImageSequence){
                        foundMainImageEssence = true;
                        Composition.EditRate compositionEditRate = new Composition.EditRate(compositionPlaylistType.getEditRate());
                        for(org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResourceType : virtualTrackResourceList){
                            Composition.EditRate trackResourceEditRate = new Composition.EditRate(trackFileResourceType.getEditRate());
                            if(!trackResourceEditRate.equals(compositionEditRate)){
                                throw new IMFException(String.format("This Composition is invalid since the CompositionEditRate %s is not the same as atleast one of the MainImageSequence's Resource EditRate %s. Please refer to st2067-2:2013 Section 6.4", compositionEditRate.toString(), trackResourceEditRate.toString()));
                            }
                        }
                    }
                }
                else
                {
                    String message = String.format(
                            "First segment in Composition XML file has multiple occurrences of virtual track UUID %s", uuid);
                    if (imfErrorLogger != null)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                    }
                    else
                    {
                        throw new IMFException(message);
                    }
                }
            }

        }

        checkSegments(compositionPlaylistType, virtualTrackMap, imfErrorLogger);

        //TODO : Add a check to ensure that all the VirtualTracks have the same duration.

        if(!foundMainImageEssence){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("CPL Id %s does not reference a single image essence", UUIDHelper.fromUUIDAsURNStringToUUID(compositionPlaylistType.getId()).toString()));
        }

        return virtualTrackMap;
    }

    static void checkSegments(org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, Map<UUID, CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013> virtualTrackMap, @Nullable IMFErrorLogger imfErrorLogger)
    {
        for (org.smpte_ra.schemas.st2067_2_2013.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            Set<UUID> trackIDs = new HashSet<>();
            org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence;
            sequence = segment.getSequenceList().getMarkerSequence();
            if (sequence != null)
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                trackIDs.add(uuid);
                if (virtualTrackMap.get(uuid) == null)
                {
                    String message = String.format(
                            "A segment in Composition XML file does not contain virtual track UUID %s", uuid);
                    if (imfErrorLogger != null)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                    }
                    else
                    {
                        throw new IMFException(message);
                    }
                }
            }

            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                sequence = (org.smpte_ra.schemas.st2067_2_2013.SequenceType)(jaxbElement).getValue();
                if (sequence != null)
                {
                    UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                    trackIDs.add(uuid);
                    if (virtualTrackMap.get(uuid) == null)
                    {
                        String message = String.format(
                                "A segment in Composition XML file does not contain virtual track UUID %s", uuid);
                        if (imfErrorLogger != null)
                        {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                        }
                        else
                        {
                            throw new IMFException(message);
                        }
                    }
                }
            }

            if (trackIDs.size() != virtualTrackMap.size())
            {
                String message = String.format(
                        "Number of distinct virtual trackIDs in a segment = %s, different from first segment %d", trackIDs.size(), virtualTrackMap.size());
                if (imfErrorLogger != null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                else
                {
                    throw new IMFException(message);
                }
            }

        }
    }

    public static Map<UUID, List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>> populateVirtualTrackResourceList(@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>> virtualTrackResourceMap = new LinkedHashMap<>();
        for (org.smpte_ra.schemas.st2067_2_2013.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {

            org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence;
            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                sequence = (org.smpte_ra.schemas.st2067_2_2013.SequenceType)(jaxbElement).getValue();
                if (sequence != null)
                {
                    UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                    /**
                     * A LinkedList seems appropriate since we want to preserve the order of the Resources referenced
                     * by a virtual track to recreate the presentation. Since the LinkedList implementation is not
                     * synchronized wrapping it around a synchronized list collection, although in this case it
                     * is perhaps not required since this method is only invoked from the context of the constructor.
                     */
                    List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> trackFileResources = Collections.synchronizedList(new LinkedList<>());
                    for (org.smpte_ra.schemas.st2067_2_2013.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResource = (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType)resource;
                        trackFileResources.add(trackFileResource);
                    }
                    checkTrackResourceList(trackFileResources, null);
                    if (virtualTrackResourceMap.get(uuid) == null)
                    {
                        virtualTrackResourceMap.put(uuid, trackFileResources);
                    }
                    else
                    {
                        virtualTrackResourceMap.get(uuid).addAll(trackFileResources);
                    }
                }
            }
        }

        //make virtualTrackResourceMap immutable
        for(Map.Entry<UUID, List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>> entry : virtualTrackResourceMap.entrySet())
        {
            List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> trackFileResources = entry.getValue();
            entry.setValue(Collections.unmodifiableList(trackFileResources));
        }

        return virtualTrackResourceMap;
    }

    static boolean checkTrackResourceList(List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> virtualTrackResourceList, @Nullable IMFErrorLogger imfErrorLogger){
        boolean result = true;
        for(org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResource : virtualTrackResourceList){
            long compositionPlaylistResourceIntrinsicDuration = trackFileResource.getIntrinsicDuration().longValue();
            //WARNING : We might be losing some precision here since EntryPoint is an XML non-negative integer with no upper-bound
            long compositionPlaylistResourceEntryPoint = (trackFileResource.getEntryPoint() == null) ? 0L : trackFileResource.getEntryPoint().longValue();
            //Check to see if the Resource's source duration value is in the valid range as specified in st2067-3:2013 section 6.11.6
            if(trackFileResource.getSourceDuration() != null){
                if(trackFileResource.getSourceDuration().longValue() < 0
                        || trackFileResource.getSourceDuration().longValue() > (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)){
                    throw new IMFException(String.format("Invalid resource source duration value %d, should be in the range [0,%d]", trackFileResource.getSourceDuration().longValue(), (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)));
                }
            }
        }
        return result;
    }
}
