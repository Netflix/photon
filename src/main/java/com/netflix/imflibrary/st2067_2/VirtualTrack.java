package com.netflix.imflibrary.st2067_2;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The class is an immutable implementation of the virtual track concept defined in Section 6.9.3 of st2067-3:2013. A
 * virtual track is characterized by its UUID, the type of sequence and a list of UUIDs of the
 * IMF track files that comprise it.
 */
@Immutable
public abstract class VirtualTrack
{
    protected final UUID trackID;
    protected final Composition.SequenceTypeEnum sequenceTypeEnum;
    protected final List<UUID> resourceIds = new ArrayList<>();
    protected final List<BaseResourceType> resources = new ArrayList<>();

    /**
     * Constructor for a VirtualTrack object
     * @param trackID the UUID associated with this VirtualTrack object
     * @param sequenceTypeEnum the type of the associated sequence
     */
    public VirtualTrack(UUID trackID, Composition.SequenceTypeEnum sequenceTypeEnum)
    {
        this.trackID = trackID;
        this.sequenceTypeEnum = sequenceTypeEnum;
    }

    /**
     * Getter for the sequence type associated with this VirtualTrack object
     * @return the sequence type associated with this VirtualTrack object as an enum
     */
    public Composition.SequenceTypeEnum getSequenceTypeEnum()
    {
        return this.sequenceTypeEnum;
    }

    /**
     * Getter for the UUID associated with this VirtualTrack object
     * @return the UUID associated with the Virtual track
     */
    public UUID getTrackID(){
        return this.trackID;
    }

    /**
     * Getter for the UUIDs of the resources that are a part of this virtual track
     * @return an unmodifiable list of UUIDs of resources that are a part of this virtual track
     */
    public List<UUID> getTrackResourceIds(){
        return Collections.unmodifiableList(this.resourceIds);
    }

    /**
     * Getter for the Resources of the Virtual Track
     * @return an unmodifiable list of resources of the Virtual Track
     */
    public List<BaseResourceType> getResourceList(){
        return Collections.unmodifiableList(this.resources);
    }

    /**
     * A method to determine the equivalence of any 2 virtual tracks.
     * @param other - the object to compare against
     * @return boolean indicating if the 2 virtual tracks are equivalent or represent the same timeline
     */
    public boolean equivalent(VirtualTrack other)
    {
        if(other == null){
            return false;
        }
        boolean result = true;
        List<BaseResourceType> otherResourceList = other.resources;
        if(otherResourceList.size() != resources.size()){
            return false;
        }
        for(int i=0; i< resources.size(); i++){
            BaseResourceType thisResource = this.resources.get(i);
            BaseResourceType otherResource = otherResourceList.get(i);

            result &= thisResource.equivalent(otherResource);
        }
        return  result;
    }
}
