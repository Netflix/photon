package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models aspects of a Composition such as a VirtualTrack, TrackResources etc. compliant with the 2013 CompositionPlaylist specification st2067-3:2013.
 */
public final class CompositionModel_st2067_2_2013 {

    //To prevent instantiation
    private CompositionModel_st2067_2_2013(){

    }

    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding VirtualTrack
     */
    public static Map<UUID, VirtualTrack> getVirtualTracksMap (@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, VirtualTrack> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<BaseResourceType>>virtualTrackResourceMap =  CompositionModel_st2067_2_2013.getVirtualTrackResourceMap(compositionPlaylistType, imfErrorLogger);

        //process first segment to create virtual track map
        org.smpte_ra.schemas.st2067_2_2013.SegmentType segment = compositionPlaylistType.getSegmentList().getSegment().get(0);
        org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence;

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
                    List<BaseResourceType> virtualTrackResourceList = null;
                    if(virtualTrackResourceMap.get(uuid) == null){
                        virtualTrackResourceList = new ArrayList<BaseResourceType>();
                    }
                    else{
                        virtualTrackResourceList = virtualTrackResourceMap.get(uuid);
                    }
                    VirtualTrack virtualTrack = null;
                    if(virtualTrackResourceList.size() != 0)
                    {
                        if( virtualTrackResourceList.get(0) instanceof TrackFileResourceType)
                        {
                            virtualTrack = new FileVirtualTrack(uuid,
                                    Composition.SequenceTypeEnum.getSequenceTypeEnum(name),
                                    virtualTrackResourceList);
                        }
                    }
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

        }

        IMFCoreConstraintsChecker_st2067_2_2013.checkSegments(compositionPlaylistType, virtualTrackMap, imfErrorLogger);

        return virtualTrackMap;
    }

    /**
     * A stateless method that completely reads and parses the resources of all the VirtualTracks that are a part of the Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return map of VirtualTrack identifier to the list of all the Track's resources, for every VirtualTrack of the Composition
     */
    public static Map<UUID, List<BaseResourceType>> getVirtualTrackResourceMap(@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, List<BaseResourceType>> virtualTrackResourceMap = new LinkedHashMap<>();
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
                    List<BaseResourceType> baseResources = Collections.synchronizedList(new LinkedList<>());
                    for (org.smpte_ra.schemas.st2067_2_2013.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        BaseResourceType baseResource = new TrackFileResource_st2067_2_2013((org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType)resource);
                        baseResources.add(baseResource);
                    }
                    IMFCoreConstraintsChecker_st2067_2_2013.checkVirtualTrackResourceList(uuid, baseResources, imfErrorLogger);
                    if (virtualTrackResourceMap.get(uuid) == null)
                    {
                        virtualTrackResourceMap.put(uuid, baseResources);
                    }
                    else
                    {
                        virtualTrackResourceMap.get(uuid).addAll(baseResources);
                    }
                }
            }
        }

        //make virtualTrackResourceMap immutable
        for(Map.Entry<UUID, List<BaseResourceType>> entry : virtualTrackResourceMap.entrySet())
        {
            List<BaseResourceType> baseResources = entry.getValue();
            entry.setValue(Collections.unmodifiableList(baseResources));
        }

        return virtualTrackResourceMap;
    }
}
