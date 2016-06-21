package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType;

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
    public static Map<UUID, CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013> getVirtualTracksMap (@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        Map<UUID, CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013> virtualTrackMap = new LinkedHashMap<>();

        Map<UUID, List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>>virtualTrackResourceMap =  CompositionModel_st2067_2_2013.getVirtualTrackResourceMap(compositionPlaylistType, imfErrorLogger);

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
                    CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013 virtualTrack = new CompositionModel_st2067_2_2013.VirtualTrack_st2067_2_2013(uuid, Composition.SequenceTypeEnum.getSequenceTypeEnum(name), virtualTrackResourceList);
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
    public static Map<UUID, List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType>> getVirtualTrackResourceMap(@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
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
                    IMFCoreConstraintsChecker_st2067_2_2013.checkVirtualTrackResourceList(uuid, trackFileResources, imfErrorLogger);
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

    public final static class VirtualTrack_st2067_2_2013 extends Composition.VirtualTrack {
        private final List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList;
        VirtualTrack_st2067_2_2013(UUID trackID, Composition.SequenceTypeEnum sequenceTypeEnum,
                                   List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList){
            super(trackID, sequenceTypeEnum);
            this.resourceList = Collections.unmodifiableList(resourceList);
            for(org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType resource : this.resourceList){
                this.resourceIds.add(UUIDHelper.fromUUIDAsURNStringToUUID(resource.getTrackFileId()));
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
}
