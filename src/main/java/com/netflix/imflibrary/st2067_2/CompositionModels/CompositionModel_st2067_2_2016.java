package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.util.*;

/**
 * A class that models aspects of a Composition such as a VirtualTrack, TrackResources etc. compliant with the 2016 CompositionPlaylist specification st2067-3:2016.
 */
public final class CompositionModel_st2067_2_2016
{

    //To prevent instantiation
    private CompositionModel_st2067_2_2016(){

    }

    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding VirtualTrack
     */
    public static CompositionPlaylistType getCompositionPlayList (@Nonnull org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<SegmentType> segmentList = new ArrayList<SegmentType>();
        for (org.smpte_ra.schemas.st2067_2_2016.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            List<SequenceType> sequenceList = new ArrayList<SequenceType>();
            org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;
            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                sequence = (org.smpte_ra.schemas.st2067_2_2016.SequenceType)(jaxbElement).getValue();
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
                    for (org.smpte_ra.schemas.st2067_2_2016.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        BaseResourceType baseResource;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType) resource;

                            baseResource = new TrackFileResource_st2067_2_2016(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding(),
                                    trackFileResource.getHashAlgorithm()
                            );
                        }
                        else
                        {
                            baseResource = new BaseResourceType(resource.getId(),
                                    resource.getEditRate(),
                                    resource.getIntrinsicDuration(),
                                    resource.getEntryPoint(),
                                    resource.getSourceDuration(),
                                    resource.getRepeatCount());
                        }
                        baseResources.add(baseResource);
                    }
                    sequenceList.add(new SequenceType(sequence.getId(), sequence.getTrackId(), baseResources));
                }
            }
            sequenceList = Collections.unmodifiableList(sequenceList);
            segmentList.add(new SegmentType(segment.getId(), sequenceList));
        }
        segmentList = Collections.unmodifiableList(segmentList);

        CompositionPlaylistType cpl =  new CompositionPlaylistType( compositionPlaylistType.getId(),
                compositionPlaylistType.getEditRate(),
                (compositionPlaylistType.getAnnotation() == null ? null : compositionPlaylistType.getAnnotation().getValue()),
                (compositionPlaylistType.getIssuer() == null ? null : compositionPlaylistType.getIssuer().getValue()),
                (compositionPlaylistType.getCreator() == null ? null : compositionPlaylistType.getCreator().getValue()),
                (compositionPlaylistType.getContentOriginator() == null ? null : compositionPlaylistType.getContentOriginator().getValue()),
                (compositionPlaylistType.getContentTitle() == null ? null : compositionPlaylistType.getContentTitle().getValue()),
                segmentList);

        return cpl;
    }
    
    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding VirtualTrack
     */
    public static Map<UUID, VirtualTrack> getVirtualTracksMap (@Nonnull org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, VirtualTrack> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<BaseResourceType>>virtualTrackResourceMap =  CompositionModel_st2067_2_2016.getVirtualTrackResourceMap(compositionPlaylistType, imfErrorLogger);

        //process first segment to create virtual track map
        org.smpte_ra.schemas.st2067_2_2016.SegmentType segment = compositionPlaylistType.getSegmentList().getSegment().get(0);
        org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;
        sequence = segment.getSequenceList().getMarkerSequence();

        for (Object object : segment.getSequenceList().getAny())
        {
            if(!(object instanceof JAXBElement)){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                continue;
            }
            JAXBElement jaxbElement = (JAXBElement)(object);
            String name = jaxbElement.getName().getLocalPart();
            sequence = (org.smpte_ra.schemas.st2067_2_2016.SequenceType)(jaxbElement).getValue();
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
                            virtualTrack = new EssenceComponentVirtualTrack(uuid,
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

        IMFCoreConstraintsChecker_st2067_2_2016.checkSegments(compositionPlaylistType, virtualTrackMap, imfErrorLogger);

        return virtualTrackMap;
    }

    /**
     * A stateless method that completely reads and parses the resources of all the VirtualTracks that are a part of the Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return map of VirtualTrack identifier to the list of all the Track's resources, for every VirtualTrack of the Composition
     */
    private static Map<UUID, List<BaseResourceType>> getVirtualTrackResourceMap(@Nonnull org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, List<BaseResourceType>> virtualTrackResourceMap = new LinkedHashMap<>();
        for (org.smpte_ra.schemas.st2067_2_2016.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {

            org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;
            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                sequence = (org.smpte_ra.schemas.st2067_2_2016.SequenceType)(jaxbElement).getValue();
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
                    for (org.smpte_ra.schemas.st2067_2_2016.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        BaseResourceType baseResource;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType) resource;

                            baseResource = new TrackFileResource_st2067_2_2016(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding(),
                                    trackFileResource.getHashAlgorithm()
                            );
                        }
                        else
                        {
                            baseResource = new BaseResourceType(resource.getId(),
                                    resource.getEditRate(),
                                    resource.getIntrinsicDuration(),
                                    resource.getEntryPoint(),
                                    resource.getSourceDuration(),
                                    resource.getRepeatCount());
                        }
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
            List<BaseResourceType> trackFileResources = entry.getValue();
            entry.setValue(Collections.unmodifiableList(trackFileResources));
        }

        return virtualTrackResourceMap;
    }

}
