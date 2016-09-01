package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class that performs 2013 CoreConstraints st2067-2:2013 related checks on the elements of a Composition Playlist such as VirtualTracks, Segments, Sequences and Resources.
 */
final class IMFCoreConstraintsChecker {


    //To prevent instantiation
    private IMFCoreConstraintsChecker(){

    }

    public static List checkVirtualTracks(IMFCompositionPlaylistType compositionPlaylistType, Map<UUID, ? extends
            Composition.VirtualTrack> virtualTrackMap){

        boolean foundMainImageEssence = false;
        int numberOfMainImageEssences = 0;
        boolean foundMainAudioEssence = false;
        int numberOfMarkerSequences = 0;
        IMFErrorLogger imfErrorLogger =new IMFErrorLoggerImpl();
        Iterator iterator = virtualTrackMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            imfErrorLogger.addAllErrors(checkVirtualTrackResourceList(virtualTrack.getTrackID(), virtualTrackResourceList));

            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                foundMainImageEssence = true;
                numberOfMainImageEssences++;
                Composition.EditRate compositionEditRate = compositionPlaylistType.getEditRate();
                for (IMFBaseResourceType baseResourceType : virtualTrackResourceList) {
                    Composition.EditRate trackResourceEditRate = baseResourceType.getEditRate();
                    if (trackResourceEditRate != null
                            && !trackResourceEditRate.equals(compositionEditRate)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("This Composition is invalid since the CompositionEditRate %s is not the same as atleast one of the MainImageSequence's Resource EditRate %s. Please refer to st2067-2:2013 Section 6.4", compositionEditRate.toString(), trackResourceEditRate.toString()));
                    }
                }
            }
            else if(virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence)){
                foundMainAudioEssence = true;
            }

            if((virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)
                    || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence))
                    && compositionPlaylistType.getEssenceDescriptorList() != null
                    && compositionPlaylistType.getEssenceDescriptorList().size() > 0)
            {
                //Construct a DOMNodeObjectModel object for every EssenceDescriptor that is a part of the EssenceDescriptorList
                Map<UUID, List<DOMNodeObjectModel>> domNodeObjectModelMap = new HashMap<>();
                List<IMFEssenceDescriptorBaseType> essenceDescriptors = compositionPlaylistType.getEssenceDescriptorList();
                for(IMFEssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptors){
                    List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
                    for (Object object : essenceDescriptorBaseType.getAny()) {
                        domNodeObjectModels.add(new DOMNodeObjectModel((Node) object));
                    }
                    domNodeObjectModelMap.put(essenceDescriptorBaseType.getId(), domNodeObjectModels);
                }

                List<DOMNodeObjectModel> virtualTrackEssenceDescriptors = new ArrayList<>();
                for(IMFBaseResourceType imfBaseResourceType : virtualTrackResourceList){
                    IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(imfBaseResourceType);
                    List<DOMNodeObjectModel> domNodeObjectModels = domNodeObjectModelMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
                    virtualTrackEssenceDescriptors.addAll(domNodeObjectModels);
                }

                if(!(virtualTrackEssenceDescriptors.size() > 0)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s doesn't seem to refer to a single EssenceDescriptor in the CPL's EssenceDescriptorList", compositionPlaylistType.getId().toString(), virtualTrack.getTrackID().toString()));
                }
                boolean isVirtualTrackHomogeneous = true;
                DOMNodeObjectModel refDOMNodeObjectModel = virtualTrackEssenceDescriptors.get(0);
                for(int i=1; i<virtualTrackEssenceDescriptors.size(); i++){
                    isVirtualTrackHomogeneous &= refDOMNodeObjectModel.equivalent(virtualTrackEssenceDescriptors.get(i));
                    refDOMNodeObjectModel = virtualTrackEssenceDescriptors.get(i);
                }
                if(!isVirtualTrackHomogeneous) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                            String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s is not homogeneous based on a comparison of the EssenceDescriptors referenced by its resources in the Essence Descriptor List", compositionPlaylistType.getId().toString(), virtualTrack.getTrackID().toString()));
                }
            }
        }

        //TODO : Add a check to ensure that all the VirtualTracks have the same duration.
        //Section 6.3.1 st2067-2:2016 and Section 6.9.3 st2067-3:2016
        if(!foundMainImageEssence){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition represented by Id %s does not contain a single image essence in its first segment, exactly one is required", compositionPlaylistType.getId().toString()));
        }
        else{
            if(numberOfMainImageEssences > 1){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition represented by Id %s seems to contain %d image essences in its first segment, exactly one is required", compositionPlaylistType.getId().toString(), numberOfMainImageEssences));
            }
        }

        //Section 6.3.2 st2067-2:2016 and Section 6.9.3 st2067-3:2016
        if(!foundMainAudioEssence){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition represented by Id %s does not contain a single audio essence in its first segment, one or more is required", compositionPlaylistType.getId().toString()));
        }

        return imfErrorLogger.getErrors();
    }

    public static void checkSegments(IMFCompositionPlaylistType compositionPlaylistType, Map<UUID, Composition.VirtualTrack> virtualTrackMap, @Nullable IMFErrorLogger imfErrorLogger)
    {
        for (IMFSegmentType segment : compositionPlaylistType.getSegmentList())
        {
            Set<UUID> trackIDs = new HashSet<>();

            /* TODO: Add check for Marker sequence */

            for (IMFSequenceType sequence : segment.getSequenceList())
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                trackIDs.add(uuid);
                if (virtualTrackMap.get(uuid) == null)
                {
                    //Section 6.9.3 st2067-3:2016
                    String message = String.format(
                            "Segment %s in Composition XML file contains virtual track UUID %s, which does not appear in all the segments of the Composition, this is invalid",
                            segment.getId(), uuid);
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
            }

            if (trackIDs.size() != virtualTrackMap.size())
            {
                String message = String.format(
                        "Number of distinct virtual trackIDs in a segment = %s, different from first segment %d", trackIDs.size(), virtualTrackMap.size());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
            }

        }
    }

    public static List checkVirtualTrackResourceList(UUID trackID, List<? extends IMFBaseResourceType>
            virtualBaseResourceList){
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(virtualBaseResourceList == null
                || virtualBaseResourceList.size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s does not have any associated resources this is invalid", trackID.toString()));
            return imfErrorLogger.getErrors();
        }
        Set<Composition.EditRate> editRates = new HashSet<>();
        Composition.EditRate baseResourceEditRate = null;
        for(IMFBaseResourceType baseResource : virtualBaseResourceList){
            long compositionPlaylistResourceIntrinsicDuration = baseResource.getIntrinsicDuration().longValue();
            long compositionPlaylistResourceEntryPoint = (baseResource.getEntryPoint() == null) ? 0L : baseResource.getEntryPoint().longValue();
            //Check to see if the Resource's source duration value is in the valid range as specified in st2067-3:2013 section 6.11.6
            if(baseResource.getSourceDuration() != null){
                if(baseResource.getSourceDuration().longValue() < 0
                        || baseResource.getSourceDuration().longValue() > (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s has a resource with ID %s, that has an invalid source duration value %d, should be in the range [0,%d]",
                                    trackID.toString(),
                                    baseResource.getId(),
                                    baseResource.getSourceDuration().longValue(),
                                    (compositionPlaylistResourceIntrinsicDuration - compositionPlaylistResourceEntryPoint)));
                }
            }

            //Check to see if the Marker Resource's intrinsic duration value is in the valid range as specified in st2067-3:2013 section 6.13
            if (baseResource instanceof IMFMarkerResourceType) {
                IMFMarkerResourceType markerResource = IMFMarkerResourceType.class.cast(baseResource);
                List<IMFMarkerType> markerList = markerResource.getMarkerList();
                for (IMFMarkerType marker : markerList) {
                    if (marker.getOffset().longValue() >= markerResource.getIntrinsicDuration().longValue()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                                .IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s  has a  " +
                                        "resource with ID %s, that has a marker %s, that has an invalid offset " +
                                        "value %d, should be in the range [0,%d] ",
                                trackID.toString(),
                                markerResource.getId(), marker.getLabel().getValue(), marker
                                        .getOffset().longValue(), markerResource.getIntrinsicDuration().longValue()-1));
                    }
                }
            }

            baseResourceEditRate = baseResource.getEditRate();
            if(baseResourceEditRate != null){
                editRates.add(baseResourceEditRate);
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
        return imfErrorLogger.getErrors();
    }
}
