package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that performs CoreConstraints st2067-2 related checks on the elements of a Composition Playlist such as VirtualTracks, Segments, Sequences and Resources.
 */
public final class IMFCoreConstraintsChecker {

    private static final Set<String> homogeneitySelectionSet = new HashSet<String>(){{
        add("CDCIDescriptor");
        add("RGBADescriptor");
        add("SubDescriptors");
        add("JPEG2000SubDescriptor");
        add("WAVEPCMDescriptor");
        add("StoredWidth");
        add("StoredHeight");
        add("FrameLayout");
        add("SampleRate");
        add("PixelLayout");
        add("ColorPrimaries");
        add("TransferCharacteristic");
        add("PictureCompression");
        add("ComponentMaxRef");
        add("ComponentMinRef");
        add("BlackRefLevel");
        add("WhiteRefLevel");
        add("ColorRange");
        add("ColorSiting");
        add("ComponentDepth");
        add("HorizontalSubsampling");
        add("VerticalSubsampling");
        add("Xsiz");
        add("Ysiz");
        add("Csiz");
        add("J2CLayout");
        add("RGBAComponent");
        add("Code");
        add("ComponentSize");
        add("PictureComponentSizing");
        add("J2KComponentSizing");
        add("Ssiz");
        add("XRSiz");
        add("YRSiz");
        add("AudioSampleRate");
        add("QuantizationBits");
    }};

    //To prevent instantiation
    private IMFCoreConstraintsChecker(){

    }

    public static List<ErrorLogger.ErrorObject> checkVirtualTracks(IMFCompositionPlaylist imfCompositionPlaylist) {
        return checkVirtualTracks(imfCompositionPlaylist, new RegXMLLibDictionary());
    }

    public static List<ErrorLogger.ErrorObject> checkVirtualTracks(IMFCompositionPlaylist imfCompositionPlaylist,
                                                                   RegXMLLibDictionary regXMLLibDictionary) {
        return checkVirtualTracks(imfCompositionPlaylist, regXMLLibDictionary, new HashSet<>());
    }

    /**
     * Checks that there is only one video track and at least one audio track and that
     * for each virtual track in the given virtual track map that:
     * - the track is made of supported sequences
     * - the CPL edit rate matches one of the MainImageSequence edit rate
     * - the resources are valid see checkVirtualTrackResourceList
     * - each resource has a corresponding essence descriptor
     * - the CPL and descriptor rates match
     * - the descriptors are homogeneous
     *
     * @param imfCompositionPlaylist CPL object
     * @param regXMLLibDictionary helper for producing XML representation of descriptors
     * @param homogeneitySelectionSet set of strings for which homogenity has to be checked
     * @return a list of errors
     */
     public static List<ErrorLogger.ErrorObject> checkVirtualTracks(IMFCompositionPlaylist imfCompositionPlaylist,
                                                                    RegXMLLibDictionary regXMLLibDictionary,
                                                                    Set<String> homogeneitySelectionSet){

        boolean foundMainImageEssence = false;
        int numberOfMainImageEssences = 0;
        boolean foundMainAudioEssence = false;
        IMFErrorLogger imfErrorLogger =new IMFErrorLoggerImpl();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = imfCompositionPlaylist.getEssenceDescriptorListMap();
        Iterator iterator = imfCompositionPlaylist.getVirtualTrackMap().entrySet().iterator();
        while(iterator.hasNext()) {
            Composition.VirtualTrack virtualTrack = ((Map.Entry<UUID, ? extends Composition.VirtualTrack>) iterator.next()).getValue();

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            imfErrorLogger.addAllErrors(checkVirtualTrackResourceList(virtualTrack.getTrackID(), virtualTrackResourceList));

            boolean isSupportedEssenceSequence = false;

            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)
                    || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence)
                    || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.SubtitlesSequence)
                    || (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.ForcedNarrativeSequence)
                    && imfCompositionPlaylist.getCoreConstraintsSchema().equals(CoreConstraints.NAMESPACE_IMF_2020))
                    || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.IABSequence)
                    || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MGASADMSignalSequence))
                isSupportedEssenceSequence = true;


            if (!(isSupportedEssenceSequence || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MarkerSequence))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("CPL has a Sequence of type %s which is not fully supported sequence type in Photon, NS: %s",
                                virtualTrack.getSequenceTypeEnum().toString(), imfCompositionPlaylist.getCoreConstraintsSchema()));
                continue;
            }

            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                foundMainImageEssence = true;
                numberOfMainImageEssences++;
                Composition.EditRate compositionEditRate = imfCompositionPlaylist.getEditRate();
                for (IMFBaseResourceType baseResourceType : virtualTrackResourceList) {
                    Composition.EditRate trackResourceEditRate = baseResourceType.getEditRate();
                    //Section 6.4 st2067-2:2016
                    if (trackResourceEditRate != null
                            && !trackResourceEditRate.equals(compositionEditRate)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("This Composition is invalid since the CompositionEditRate %s is not the same as atleast one of the MainImageSequence's Resource EditRate %s. Please refer to st2067-2:2013 Section 6.4", compositionEditRate.toString(), trackResourceEditRate.toString()));
                    }
                }
            }
            else if(virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence)){
                foundMainAudioEssence = true;
            }

            if(isSupportedEssenceSequence
                    && imfCompositionPlaylist.getEssenceDescriptorList() != null
                    && imfCompositionPlaylist.getEssenceDescriptorList().size() > 0)
            {
                List<DOMNodeObjectModel> virtualTrackEssenceDescriptors = new ArrayList<>();
                String refSourceEncodingElement = "";
                String essenceDescriptorField = "";
                String otherEssenceDescriptorField = "";
                Composition.EditRate essenceEditRate = null;
                for(IMFBaseResourceType imfBaseResourceType : virtualTrackResourceList){

                    IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(imfBaseResourceType);
                    DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
                    //Section 6.8 st2067-2:2016
                    if(domNodeObjectModel == null){
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by a " +
                                        "VirtualTrack Resource does not have a corresponding EssenceDescriptor in the EssenceDescriptorList in the CPL",
                                imfTrackFileResourceType.getSourceEncoding()));
                    }
                    else {

                        if (!refSourceEncodingElement.equals(imfTrackFileResourceType.getSourceEncoding())) {
                            refSourceEncodingElement = imfTrackFileResourceType.getSourceEncoding();
                            //Section 6.3.1 and 6.3.2 st2067-2:2016 Edit Rate check
                            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence) ||
                                    virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.SubtitlesSequence) ||
                                    (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.ForcedNarrativeSequence)
                                            && imfCompositionPlaylist.getCoreConstraintsSchema().equals(CoreConstraints.NAMESPACE_IMF_2020))) {
                                essenceDescriptorField = "SampleRate";
                            } else if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence) ||
                                       virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.IABSequence)     ||
                                       virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MGASADMSignalSequence)){
                                essenceDescriptorField = "SampleRate";
                                otherEssenceDescriptorField = "AudioSampleRate";
                            }

                            String sampleRate = domNodeObjectModel.getFieldAsString(essenceDescriptorField);
                            if(sampleRate == null && !otherEssenceDescriptorField.isEmpty()) {
                                sampleRate = domNodeObjectModel.getFieldAsString(otherEssenceDescriptorField);
                            }

                            if (sampleRate != null) {
                                Long numerator = 0L;
                                Long denominator = 0L;
                                String[] sampleRateElements = (sampleRate.contains(" ")) ? sampleRate.split(" ") : sampleRate.contains("/") ? sampleRate.split("/") : new String[2];
                                if (sampleRateElements.length == 2) {
                                    numerator = Long.valueOf(sampleRateElements[0]);
                                    denominator = Long.valueOf(sampleRateElements[1]);
                                } else if (sampleRateElements.length == 1) {
                                    numerator = Long.valueOf(sampleRateElements[0]);
                                    denominator = 1L;
                                }
                                List<Long> editRate = new ArrayList<>();
                                Integer sampleRateToEditRateScale = 1;

                                if(virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)) {
                                    CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = new CompositionImageEssenceDescriptorModel(UUIDHelper.fromUUIDAsURNStringToUUID
                                            (imfTrackFileResourceType.getSourceEncoding()),
                                            domNodeObjectModel,
                                            regXMLLibDictionary);
                                    sampleRateToEditRateScale = imageEssenceDescriptorModel.getFrameLayoutType().equals(GenericPictureEssenceDescriptor.FrameLayoutType.SeparateFields) ? 2 : 1;
                                }
                                editRate.add(numerator / sampleRateToEditRateScale);
                                editRate.add(denominator);

                                essenceEditRate = new Composition.EditRate(editRate);
                            }
                        }
                        if (essenceEditRate == null) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s has a Resource represented by ID %s that seems to refer to a EssenceDescriptor in the CPL's EssenceDescriptorList represented by the ID %s " +
                                                    "which does not have a value set for the field %s, however the Resource Edit Rate is %s"
                                            , imfCompositionPlaylist.getId().toString(), virtualTrack.getTrackID().toString(), imfBaseResourceType.getId(), imfTrackFileResourceType.getSourceEncoding(), essenceDescriptorField, imfBaseResourceType.getEditRate().toString()));
                        } else if (!essenceEditRate.equals(imfBaseResourceType.getEditRate())) {
                            //Section 6.3.1 and 6.3.2 st2067-2:2016
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s has a Resource represented by ID %s that refers to a EssenceDescriptor in the CPL's EssenceDescriptorList represented by the ID %s " +
                                                    "whose indicated %s value is %s, however the Resource Edit Rate is %s"
                                            , imfCompositionPlaylist.getId().toString(), virtualTrack.getTrackID().toString(), imfBaseResourceType.getId(), imfTrackFileResourceType.getSourceEncoding(), essenceDescriptorField, essenceEditRate.toString(), imfBaseResourceType.getEditRate().toString()));
                        }
                        virtualTrackEssenceDescriptors.add(domNodeObjectModel);
                    }
                }

                if( !virtualTrackEssenceDescriptors.isEmpty()
                        && (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainImageSequence)
                            || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MainAudioSequence)
                            || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.IABSequence)
                    || virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MGASADMSignalSequence))){
                    boolean isVirtualTrackHomogeneous = true;
                    Set<String> homogeneitySelectionSetAll = new HashSet<>(homogeneitySelectionSet);
                    homogeneitySelectionSetAll.addAll(IMFCoreConstraintsChecker.homogeneitySelectionSet);
                    if (isCDCIEssenceDescriptor(virtualTrackEssenceDescriptors.get(0))) {
                        homogeneitySelectionSetAll.add("CodingEquations");
                    }
                    DOMNodeObjectModel refDOMNodeObjectModel = virtualTrackEssenceDescriptors.get(0).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(0), homogeneitySelectionSetAll);
                    for (int i = 1; i < virtualTrackEssenceDescriptors.size(); i++) {
                        DOMNodeObjectModel other = virtualTrackEssenceDescriptors.get(i).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(i), homogeneitySelectionSetAll);
                        isVirtualTrackHomogeneous &= refDOMNodeObjectModel.equals(other);
                    }
                    List<DOMNodeObjectModel> modelsIgnoreSet = new ArrayList<>();
                    if (!isVirtualTrackHomogeneous) {
                        for(int i = 1; i< virtualTrackEssenceDescriptors.size(); i++){
                            DOMNodeObjectModel other = virtualTrackEssenceDescriptors.get(i).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(i), homogeneitySelectionSetAll);
                            modelsIgnoreSet.add(other);
                            imfErrorLogger.addAllErrors(DOMNodeObjectModel.getNamespaceURIMismatchErrors(refDOMNodeObjectModel, other));
                        }
                        //Section 6.2 st2067-2:2016
                        imfErrorLogger.addAllErrors(refDOMNodeObjectModel.getErrors());
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s is not homogeneous based on a comparison of the EssenceDescriptors referenced by its resources in the Essence Descriptor List, " +
                                        "the EssenceDescriptors corresponding to this VirtualTrack in the EssenceDescriptorList are as follows %n%n%s", imfCompositionPlaylist.getId().toString(), virtualTrack.getTrackID().toString(), Utilities.serializeObjectCollectionToString(modelsIgnoreSet)));
                    }
                }
            }
        }

        //TODO : Add a check to ensure that all the VirtualTracks have the same duration.
        //Section 6.3.1 st2067-2:2016 and Section 6.9.3 st2067-3:2016
        if(!foundMainImageEssence){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition represented by Id %s does not contain a single image essence in its first segment, exactly one is required", imfCompositionPlaylist.getId().toString()));
        }
        else{
            if(numberOfMainImageEssences > 1){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition represented by Id %s seems to contain %d image essences in its first segment, exactly one is required", imfCompositionPlaylist.getId().toString(), numberOfMainImageEssences));
            }
        }

        //Section 6.3.2 st2067-2:2016 and Section 6.9.3 st2067-3:2016
        //Section 6.3.2 st2067-2:2020 allows CPLs without Audio Virtual Tracks
        if(!foundMainAudioEssence
                && (imfCompositionPlaylist.getCoreConstraintsSchema().equals(CoreConstraints.NAMESPACE_IMF_2013)
                || imfCompositionPlaylist.getCoreConstraintsSchema().equals(CoreConstraints.NAMESPACE_IMF_2016)))
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition represented by Id %s does not contain a single audio essence in its first segment, one or more is required", imfCompositionPlaylist.getId().toString()));
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * Checks that for each segment that:
     * - all tracks in the segment are in the reference track map
     * - the reference track map and the segment have the same number of tracks
     * - it has an integer duration when expressed in the CPL edit rate
     * - all its sequences have the same duration
     *
     * @param imfCompositionPlaylist the playlist from which segments are to be checked
     **/
    public static List<ErrorLogger.ErrorObject> checkSegments(IMFCompositionPlaylist imfCompositionPlaylist)
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        for (IMFSegmentType segment : imfCompositionPlaylist.getSegmentList())
        {
            Set<UUID> trackIDs = new HashSet<>();

            /* TODO: Add check for Marker sequence */
            Set<Long> sequencesDurationSet = new HashSet<>();
            for (IMFSequenceType sequence : segment.getSequenceList())
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                trackIDs.add(uuid);
                if (imfCompositionPlaylist.getVirtualTrackMap().get(uuid) == null)
                {
                    //Section 6.9.3 st2067-3:2016
                    String message = String.format(
                            "Segment represented by the ID %s in the Composition represented by ID %s contains virtual track represented by ID %s, which does not appear in all the segments of the Composition, this is invalid",
                            segment.getId(), imfCompositionPlaylist.getId().toString(), uuid);
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
                }
                List<? extends IMFBaseResourceType> resources = sequence.getResourceList();
                Long sequenceDurationInCompositionEditUnits = 0L;
                Long sequenceDuration = 0L;
                //Based on Section 6.2 and 6.3 in st2067-2:2016 All resources of either an Image Sequence or an Audio Sequence have to be of the same EditRate, hence we can sum the source durations of all the resources
                //of a virtual track to get its duration in resource edit units.
                for(IMFBaseResourceType imfBaseResourceType : resources){
                    sequenceDuration += imfBaseResourceType.getDuration();
                }
                //Section 7.3 st2067-3:2016
                long compositionEditRateNumerator = imfCompositionPlaylist.getEditRate().getNumerator();
                long compositionEditRateDenominator = imfCompositionPlaylist.getEditRate().getDenominator();
                long resourceEditRateNumerator = resources.get(0).getEditRate().getNumerator();
                long resourceEditRateDenominator = resources.get(0).getEditRate().getDenominator();

                long sequenceDurationInCompositionEditRateReminder = (sequenceDuration * compositionEditRateNumerator * resourceEditRateDenominator) % (compositionEditRateDenominator * resourceEditRateNumerator);
                Double sequenceDurationDoubleValue = ((double)sequenceDuration * compositionEditRateNumerator * resourceEditRateDenominator) / (compositionEditRateDenominator * resourceEditRateNumerator);
                //Section 7.3 st2067-3:2016
                if(sequenceDurationInCompositionEditRateReminder != 0){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("Segment represented by the Id %s in the Composition represented by ID %s has a sequence represented by ID %s, whose duration represented in Composition Edit Units is (%f) is not an integer"
                                    , segment.getId(), imfCompositionPlaylist.getId().toString(), sequence.getId(), sequenceDurationDoubleValue));
                }
                sequenceDurationInCompositionEditUnits = Math.round(sequenceDurationDoubleValue);
                sequencesDurationSet.add(sequenceDurationInCompositionEditUnits);

            }
            //Section 7.2 st2067-3:2016
            if(sequencesDurationSet.size() > 1){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Segment represented by the Id %s seems to have sequences that are not of the same duration, following sequence durations were computed based on the information in the Sequence List for this Segment, %s represented in Composition Edit Units", segment.getId(), Utilities.serializeObjectCollectionToString(sequencesDurationSet)));
            }
            //Section 6.9.3 st2067-3:2016
            if (trackIDs.size() != imfCompositionPlaylist.getVirtualTrackMap().size())
            {
                String message = String.format(
                        "Number of distinct virtual trackIDs in a segment = %s, different from first segment %d", trackIDs.size(), imfCompositionPlaylist.getVirtualTrackMap().size());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, message);
            }

        }

        return imfErrorLogger.getErrors();
    }

    /**
     * Checks within a list of Resources that
     * - each resource has a valid duration (positive and less than the intrinsic duration), including for marker resources
     * - all resources use the same edit rate
     *
     * @param trackID the track ID of the track to which the resources belong, used for logging
     * @param virtualBaseResourceList the list of resources, including marker resources
     * @return a list of errors
     */
     public static List<ErrorLogger.ErrorObject> checkVirtualTrackResourceList(UUID trackID, List<? extends IMFBaseResourceType>
            virtualBaseResourceList){
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        //Section 6.9.3 st2067-3:2016
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
        //Section 6.2, 6.3.1 and 6.3.2 st2067-2:2016
        if(editRates.size() > 1){
            StringBuilder editRatesString = new StringBuilder();
            Iterator iterator = editRates.iterator();
            while(iterator.hasNext()){
                editRatesString.append(iterator.next().toString());
                editRatesString.append(String.format("%n"));
            }
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("VirtualTrack with ID %s has resources with inconsistent editRates %s", trackID.toString(), editRatesString.toString()));
        }
        return imfErrorLogger.getErrors();
    }

    private static boolean isCDCIEssenceDescriptor(DOMNodeObjectModel domNodeObjectModel) {
        return domNodeObjectModel.getLocalName().equals("CDCIDescriptor");
    }









    /**
     * A stateless method that can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to all the
     * Virtual Tracks that are a part of the Composition
     * @param imfCompositionPlaylist an IMFCompositionPlaylist object corresponding to the Composition
     * @param essencesHeaderPartitionPayloads list of payload records containing the raw bytes of the HeaderPartitions of the IMF Track files that are a part of the Virtual Track/s in the Composition
     * @return list of error messages encountered while performing conformance validation of the Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> checkEssenceDescriptors(IMFCompositionPlaylist imfCompositionPlaylist,
                                                                        List<PayloadRecord> essencesHeaderPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if (essencesHeaderPartitionPayloads == null)
            essencesHeaderPartitionPayloads = new ArrayList<>();

        /*
         * Verify that the CPL is valid before attempting to parse it.
         */
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        if (imfErrorLogger.hasFatalErrors()) {
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "Unable to validate essence descriptors: IMF Composition Playlist has FATAL errors"));
            return imfErrorLogger.getErrors();
        }

        /**
         * Check that each entry in the EssenceDescriptorList is referenced from at least one Resource.
         */
        imfCompositionPlaylist.getEssenceDescriptorIdsSet().forEach(descriptorId -> {
            if (!imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet().contains(descriptorId)) {
                //Section 6.1.10.1 st2067-3:2013
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptorID %s in the CPL " +
                        "EssenceDescriptorList is not referenced by any resource in any of the Virtual Tracks in the CPL.", descriptorId.toString()));
            }
        });


        /**
         * Check that every Resource SourceEncodingID is present in the EssenceDescriptorList.
         */
        imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet().forEach(resourceEssenceDescriptorId -> {
            if (!imfCompositionPlaylist.getEssenceDescriptorIdsSet().contains(resourceEssenceDescriptorId)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with SourceEncodingID " +
                        "%s is missing in EssenceDescriptorList.", resourceEssenceDescriptorId.toString()));
            }
        });

        /*
         * Collect the UUIDs from the header payloads and filter out any that are _not_ referenced from the input composition
         */
        Map<UUID, PayloadRecord> referencedHeaderPayloads = new HashMap<>();
        List<Composition.HeaderPartitionTuple> headerPartitionTuples = new ArrayList<>();

        for (PayloadRecord payloadRecord : essencesHeaderPartitionPayloads) {
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Unable to validate any essence descriptors: payload asset type is %s, expected asset type %s",
                                payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                return imfErrorLogger.getErrors();
            }

            try {
                HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long)payloadRecord.getPayload().length,
                        imfErrorLogger);

                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);

                // check for compliance
                imfErrorLogger.addAllErrors(IMFConstraints.checkMXFHeaderMetadata(headerPartitionOP1A));
                if (imfErrorLogger.hasFatalErrors())
                    return imfErrorLogger.getErrors();

                UUID packageUUID = MXFUtils.getTrackFileId(payloadRecord, imfErrorLogger);
                if (packageUUID == null) {
                    throw new MXFException("Unable to determine the package UUID from the payload record.");
                }
                for (IMFTrackFileResourceType tf : imfCompositionPlaylist.getTrackFileResources()) {
                    if (packageUUID.equals(UUIDHelper.fromUUIDAsURNStringToUUID(tf.getTrackFileId()))) {
                        referencedHeaderPayloads.put(packageUUID, payloadRecord);

                        headerPartitionTuples.add(new Composition.HeaderPartitionTuple(headerPartition,
                                new ByteArrayByteRangeProvider(payloadRecord.getPayload())));
                        break;
                    }
                }
            } catch (MXFException e) {
                imfErrorLogger.addAllErrors(e.getErrors());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        e.getMessage());
                return imfErrorLogger.getErrors();
            } catch (IOException e) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        "Exception occured while attempting to validate header metadata.");
                return imfErrorLogger.getErrors();
            }
        }

        if (referencedHeaderPayloads.isEmpty()) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                    "Unable to validate any essence descriptors: no matching essence partition payloads provided");
            return imfErrorLogger.getErrors();
        }

        /*
         * Raise a warning for any missing header payloads (supplemental IMP use case, i.e. not all MXF Track Files are part of the IMP)
         */
        for (IMFTrackFileResourceType tf : imfCompositionPlaylist.getTrackFileResources()) {
            if (referencedHeaderPayloads.get(UUIDHelper.fromUUIDAsURNStringToUUID(tf.getTrackFileId())) == null ) {
                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("Unable to validate essence descriptors: no matching essence partition payload provided for ID %s", tf.getTrackFileId())));
            }
        }

        Map<UUID, DOMNodeObjectModel> essenceDescriptorMap = imfCompositionPlaylist.getEssenceDescriptorListMap();
        Map<UUID, List<DOMNodeObjectModel>> resourceEssenceDescriptorMap = null;

        try {
            resourceEssenceDescriptorMap = imfCompositionPlaylist.getResourcesEssenceDescriptorsMap(headerPartitionTuples);
        } catch (IOException e) {
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "Failed to retrieve resource essence descriptor map from Composition Playlist"));
        }
        if (essenceDescriptorMap == null || resourceEssenceDescriptorMap == null) {
            return imfErrorLogger.getErrors();
        }

        /**
         * An exhaustive compare of the eDLMap and essenceDescriptorsMap is required to ensure that the essence descriptors
         * in the EssenceDescriptorList and the EssenceDescriptors in the physical essence files corresponding to the
         * same source encoding element as indicated in the TrackFileResource and EDL are a good match.
         *
         * The Maps have the DOMObjectModel for every EssenceDescriptor in the EssenceDescriptorList in the CPL and
         * the essence descriptor in each of the essences referenced from every track file resource within each virtual track.
         */

        Set<String> ignoreSet = new HashSet<String>();

        // PHDRMetadataTrackSubDescriptor is not present in SMPTE registries and cannot be serialized
        // todo:
        ignoreSet.add("PHDRMetadataTrackSubDescriptor");

        /**
         * The following check ensures that we have atleast one EssenceDescriptor in a TrackFile that equals the corresponding EssenceDescriptor element in the CPL's EDL
         */
        Iterator<Map.Entry<UUID, List<DOMNodeObjectModel>>> iterator = resourceEssenceDescriptorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<DOMNodeObjectModel>> entry = (Map.Entry<UUID, List<DOMNodeObjectModel>>) iterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = entry.getValue();
            DOMNodeObjectModel referenceDOMNodeObjectModel = essenceDescriptorMap.get(entry.getKey());
            if (referenceDOMNodeObjectModel == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Source Encoding " +
                        "Element %s in a track does not have a corresponding entry in the CPL's Essence Descriptor List.", entry.getKey().toString()));
            }
            else {
                referenceDOMNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(essenceDescriptorMap.get(entry.getKey()), ignoreSet);
                boolean intermediateResult = false;

                List<DOMNodeObjectModel> domNodeObjectModelsIgnoreSet = new ArrayList<>();
                for (DOMNodeObjectModel domNodeObjectModel : domNodeObjectModels) {
                    domNodeObjectModel = DOMNodeObjectModel.createDOMNodeObjectModelIgnoreSet(domNodeObjectModel, ignoreSet);
                    domNodeObjectModelsIgnoreSet.add(domNodeObjectModel);
                    intermediateResult |= referenceDOMNodeObjectModel.equals(domNodeObjectModel);
                }
                if (!intermediateResult) {
                    DOMNodeObjectModel matchingDOMNodeObjectModel = DOMNodeObjectModel.getMatchingDOMNodeObjectModel(referenceDOMNodeObjectModel, domNodeObjectModelsIgnoreSet);
                    imfErrorLogger.addAllErrors(DOMNodeObjectModel.getNamespaceURIMismatchErrors(referenceDOMNodeObjectModel, matchingDOMNodeObjectModel));

                    String domNodeName = referenceDOMNodeObjectModel.getLocalName();
                    List<DOMNodeObjectModel> domNodeObjectModelList = domNodeObjectModelsIgnoreSet.stream().filter( e -> e.getLocalName().equals(domNodeName)).collect(Collectors.toList());
                    if(domNodeObjectModelList.size() != 0)
                    {
                        DOMNodeObjectModel diffCPLEssenceDescriptor = referenceDOMNodeObjectModel.removeNodes(domNodeObjectModelList.get(0));
                        DOMNodeObjectModel diffTrackFileEssenceDescriptor = domNodeObjectModelList.get(0).removeNodes(referenceDOMNodeObjectModel);
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Id %s in the CPL's " +
                                        "EssenceDescriptorList doesn't match any EssenceDescriptors within the IMFTrackFile resource that references it, " +
                                        "%n%n EssenceDescriptor in CPL EssenceDescriptorList with mismatching fields is as follows %n%s, %n%nEssenceDescriptor found in the " +
                                        "TrackFile resource with mismatching fields is as follows %n%s%n%n",
                                entry.getKey().toString(), diffCPLEssenceDescriptor.toString(), diffTrackFileEssenceDescriptor.toString()));
                    }
                    else {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with Id %s in the CPL's " +
                                        "EssenceDescriptorList doesn't match any EssenceDescriptors within the IMFTrackFile resource that references it, " +
                                        "%n%n EssenceDescriptor in CPL EssenceDescriptorList is as follows %n%s, %n%nEssenceDescriptors found in the TrackFile resource %n%s%n%n",
                                entry.getKey().toString(), referenceDOMNodeObjectModel.toString(), Utilities.serializeObjectCollectionToString(domNodeObjectModelsIgnoreSet)));
                    }
                }
            }
        }

        return imfErrorLogger.getErrors();
    }

}
