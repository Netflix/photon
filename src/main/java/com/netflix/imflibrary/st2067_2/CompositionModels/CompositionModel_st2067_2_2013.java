package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.st2067_2.Composition;
import org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A class that represents certain aspects of a Composition such as a VirtualTrack compliant with the 2013 CoreConstraints Schema.
 */
public class CompositionModel_st2067_2_2013 {


    public static class VirtualTrack_st2067_2_2013 extends Composition.VirtualTrack {
        private final List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList;
        VirtualTrack_st2067_2_2013(UUID trackID, Composition.SequenceTypeEnum sequenceTypeEnum,
                                   List<org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType> resourceList){
            super(trackID, sequenceTypeEnum);
            this.resourceList = Collections.unmodifiableList(resourceList);
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
            List<TrackFileResourceType> otherResourceList = otherVirtualTrack.getResourceList();
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
