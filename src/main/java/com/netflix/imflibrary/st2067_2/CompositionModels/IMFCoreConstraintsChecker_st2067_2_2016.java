package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.BaseResourceType;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.VirtualTrack;
import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBElement;
import java.util.*;

/**
 * A class that performs 2016 CoreConstraints st2067-2:2016 related checks on the elements of a Composition Playlist such as VirtualTracks, Segments, Sequences and Resources.
 */
public final class IMFCoreConstraintsChecker_st2067_2_2016
{


    //To prevent instantiation
    private IMFCoreConstraintsChecker_st2067_2_2016(){

    }

    public static boolean checkVirtualTracks(org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, Map<UUID, ? extends VirtualTrack> virtualTrackMap, IMFErrorLogger imfErrorLogger){

        boolean foundMainImageEssence = false;
        boolean result = true;
        Iterator iterator = virtualTrackMap.entrySet().iterator();
        while(iterator.hasNext()) {
            VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends VirtualTrack>) iterator.next()).getValue();
            List<BaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            result &= checkVirtualTrackResourceList(virtualTrack.getTrackID(), virtualTrackResourceList, imfErrorLogger);
            if(!result){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("VirtualTrack with id %s is invalid, please see errors reported earlier.", virtualTrack.getTrackID().toString()));
            }

            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                foundMainImageEssence = true;
                Composition.EditRate compositionEditRate = new Composition.EditRate(compositionPlaylistType.getEditRate());
                for (BaseResourceType baseResource : virtualTrackResourceList) {
                    Composition.EditRate trackResourceEditRate = baseResource.getEditRate();
                    if (trackResourceEditRate != null
                            && !trackResourceEditRate.equals(compositionEditRate)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                String.format("This Composition is invalid since the CompositionEditRate %s is not the same as atleast one of the MainImageSequence's Resource EditRate %s. Please refer to st2067-2:2016 Section 6.4",
                                        compositionEditRate.toString(), trackResourceEditRate.toString()));
                        result &= false;
                    }
                }
            }
        }

        //TODO : Add a check to ensure that all the VirtualTracks have the same duration.

        if(!foundMainImageEssence){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("CPL Id %s does not reference a single image essence", UUIDHelper.fromUUIDAsURNStringToUUID(compositionPlaylistType.getId()).toString()));
            result &= false;
        }
        return result;
    }

    public static void checkSegments(org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, Map<UUID, VirtualTrack> virtualTrackMap, @Nullable IMFErrorLogger imfErrorLogger)
    {
        for (org.smpte_ra.schemas.st2067_2_2016.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            Set<UUID> trackIDs = new HashSet<>();
            org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;
            sequence = segment.getSequenceList().getMarkerSequence();
            if (sequence != null)
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                trackIDs.add(uuid);
                if (virtualTrackMap.get(uuid) == null)
                {
                    String message = String.format(
                            "A segment in Composition XML file does not contain virtual track UUID %s", uuid.toString());
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
                sequence = (org.smpte_ra.schemas.st2067_2_2016.SequenceType)(jaxbElement).getValue();
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

    public static boolean checkVirtualTrackResourceList(UUID trackID, List<BaseResourceType> virtualTrackResourceList, @Nonnull IMFErrorLogger imfErrorLogger){
        boolean result = true;
        if(virtualTrackResourceList == null
                || virtualTrackResourceList.size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s does not have any associated resources this is invalid", trackID.toString()));
            return false;
        }
        Set<Composition.EditRate> editRates = new HashSet<>();
        Composition.EditRate trackResourceEditRate = null;
        for(BaseResourceType baseResource : virtualTrackResourceList){
            long compositionPlaylistResourceIntrinsicDuration = baseResource.getIntrinsicDuration().longValue();
            long compositionPlaylistResourceEntryPoint = (baseResource.getEntryPoint() == null) ? 0L : baseResource.getEntryPoint().longValue();
            //Check to see if the Resource's source duration value is in the valid range as specified in st2067-3:2016 section 6.11.6
            if(baseResource.getSourceDuration() != null){
                if(baseResource.getSourceDuration().longValue() < 0
                        || baseResource.getSourceDuration().longValue() > (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s has a resource with ID %s, that has an invalid source duration value %d, should be in the range [0,%d]",
                            trackID.toString(), baseResource.getId(),
                            baseResource.getSourceDuration().longValue(),
                            (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)));
                    result &= false;
                }
            }
            trackResourceEditRate = baseResource.getEditRate();
            if(trackResourceEditRate != null){
                editRates.add(trackResourceEditRate);
            }
        }

        if(editRates.size() > 1){
            StringBuilder editRatesString = new StringBuilder();
            Iterator iterator = editRates.iterator();
            while(iterator.hasNext()){
                editRatesString.append(iterator.next().toString());
                editRatesString.append(String.format("%n"));
            }
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s has resources with inconsistent editRates %s", trackID.toString(), editRatesString.toString()));
        }
        return result;
    }
}
