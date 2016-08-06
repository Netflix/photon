package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.UUIDHelper;
<<<<<<< 93ce6a422a2d13d8e8e29d03662d8611741f4a1c
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2013;
import org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType;
=======
>>>>>>> Adding class hierarchy for track resource


import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
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
    public static CompositionPlaylistType getCompositionPlayList (@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<SegmentType> segmentList = new ArrayList<SegmentType>();
        for (org.smpte_ra.schemas.st2067_2_2013.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            List<SequenceType> sequenceList = new ArrayList<SequenceType>();
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
                        BaseResourceType baseResource;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType) resource;

                            baseResource = new TrackFileResource_st2067_2_2013(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding()
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
                        BaseResourceType baseResource;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType) resource;

                            baseResource = new TrackFileResource_st2067_2_2013(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding()
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
            List<BaseResourceType> baseResources = entry.getValue();
            entry.setValue(Collections.unmodifiableList(baseResources));
        }

        return virtualTrackResourceMap;
    }
<<<<<<< 93ce6a422a2d13d8e8e29d03662d8611741f4a1c

    /**
     * A class that models a Virtual Track compliant with the 2013 Composition Playlist Schema
     */
    public final static class VirtualTrack_st2067_2_2013 extends Composition.VirtualTrack {
        private final List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList;
        VirtualTrack_st2067_2_2013(UUID trackID, Composition.SequenceTypeEnum sequenceTypeEnum,
                                   List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList){
            super(trackID, sequenceTypeEnum);
            this.resourceList = Collections.unmodifiableList(resourceList);
            for(org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType resource : this.resourceList){
                this.resourceIds.add(UUIDHelper.fromUUIDAsURNStringToUUID(resource.getTrackFileId()));
                BigInteger entryPoint = resource.getEntryPoint() == null ? BigInteger.valueOf(0L) : resource.getEntryPoint();
                BigInteger sourceDuration = resource.getSourceDuration() == null
                                                        ? BigInteger.valueOf(resource.getIntrinsicDuration().longValue() - entryPoint.longValue())
                                                        : resource.getSourceDuration();
                if(sourceDuration.longValue() <= 0
                        || sourceDuration.longValue() > resource.getIntrinsicDuration().longValue() - entryPoint.longValue() ){
                    throw new IMFException(String.format("Source duration %d should be in the range (0, IntrinsicDuration(%d) - entryPoint(%d)]", sourceDuration, resource.getIntrinsicDuration(), entryPoint));
                }
                        this.resources.add(new Composition.TrackResource(resource.getId(),
                        resource.getTrackFileId(),
                        resource.getSourceEncoding(),
                        resource.getEditRate(),
                        resource.getIntrinsicDuration(),
                        entryPoint,
                        sourceDuration,
                        resource.getRepeatCount() == null ? BigInteger.valueOf(1) : resource.getRepeatCount(),
                        resource.getHash(),
                        CompositionPlaylistBuilder_2013.defaultHashAlgorithm));
            }
        }

        /**
         * Getter for the list of resources associated with this VirtualTrack
         * @return the list of TrackFileResources associated with this VirtualTrack.
         */
        public List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> getResourceList(){
            return Collections.unmodifiableList(this.resourceList);
        }

        /**
         * A method to determine the equivalence of any 2 virtual tracks.
         * @param other - the object to compare against
         * @return boolean indicating if the 2 virtual tracks are equivalent or represent the same timeline
         */
        public boolean equivalent(Composition.VirtualTrack other)
        {
            if(other == null
                || !(other instanceof VirtualTrack_st2067_2_2013)){
                return false;
            }
            VirtualTrack_st2067_2_2013 otherVirtualTrack = VirtualTrack_st2067_2_2013.class.cast(other);
            boolean result = true;
            List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> otherResourceList = otherVirtualTrack.getResourceList();
            if(otherResourceList.size() != this.resourceList.size()){
                return false;
            }
            for(int i=0; i<this.getResourceList().size(); i++){
                TrackFileResourceType thisResource = this.resourceList.get(i);
                TrackFileResourceType otherResource = otherResourceList.get(i);

                //Compare the following fields of the track file resources that have to be equal
                //for the 2 resources to be considered equivalent/representing the same timeline.

                result &= thisResource.getTrackFileId().equals(otherResource.getTrackFileId());
                result &= thisResource.getEditRate().equals(otherResource.getEditRate());
                result &= thisResource.getEntryPoint().equals(otherResource.getEntryPoint());
                result &= thisResource.getIntrinsicDuration().equals(otherResource.getIntrinsicDuration());
                result &= thisResource.getRepeatCount().equals(otherResource.getRepeatCount());
                result &= thisResource.getSourceEncoding().equals(otherResource.getSourceEncoding());
            }
            return  result;
        }
    }
=======
>>>>>>> Adding class hierarchy for track resource
}
