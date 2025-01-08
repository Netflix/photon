package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.*;
import com.sandflow.smpte.klv.Triplet;
import jakarta.annotation.Nonnull;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

abstract public class IMFCPLValidator implements ConstraintsValidator {

    private static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-3/2013");
        add("http://www.smpte-ra.org/schemas/2067-3/2016");
    }});


    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {
        return List.of();
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        // check durations etc
        imfErrorLogger.addAllErrors(checkSegments(imfCompositionPlaylist));

        // check descriptor cross-references and compare CPL descriptors to actual header metadata descriptors
        imfErrorLogger.addAllErrors(checkEssenceDescriptors(imfCompositionPlaylist, headerPartitionPayloads));

        // iterate over virtual tracks
        for (Map.Entry<UUID, ? extends Composition.VirtualTrack> virtualTrackEntry : imfCompositionPlaylist.getVirtualTrackMap().entrySet()) {
            Composition.VirtualTrack virtualTrack = virtualTrackEntry.getValue();

            // run basic checks for CPL constraints on all virtual track resource lists
            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            imfErrorLogger.addAllErrors(checkVirtualTrackResourceList(virtualTrack.getTrackID(), virtualTrackResourceList));

            // retrieve sequence namespace associated with the virtual track
            String virtualTrackSequenceNamespace = imfCompositionPlaylist.getSequenceNamespaceForVirtualTrackID(virtualTrack.getTrackID());
            if (!supportedCPLSchemaURIs.contains(virtualTrackSequenceNamespace)) {
                continue;
            }

        }


        // MARKER TRACK VALIDATION, CONTENT KIND VALUES, ETC

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
                if (resources.isEmpty())
                    continue;

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
     * A stateless method that can be used to determine if a Composition is conformant. Conformance checks
     * perform deeper inspection of the Composition and the EssenceDescriptors corresponding to all the
     * Virtual Tracks that are a part of the Composition
     * @param imfCompositionPlaylist an IMFCompositionPlaylist object corresponding to the Composition
     * @param essencesHeaderPartitionPayloads list of payload records containing the raw bytes of the HeaderPartitions of the IMF Track files that are a part of the Virtual Track/s in the Composition
     * @return list of error messages encountered while performing conformance validation of the Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    private static List<ErrorLogger.ErrorObject> checkEssenceDescriptors(IMFCompositionPlaylist imfCompositionPlaylist,
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
        Map<UUID, Composition.HeaderPartitionTuple> headerPartitionTuples = new HashMap<>();

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

                // check for compliance
                imfErrorLogger.addAllErrors(IMFConstraints.checkMXFHeaderMetadata(headerPartition));
                if (imfErrorLogger.hasFatalErrors())
                    return imfErrorLogger.getErrors();

                UUID packageUUID = MXFUtils.getTrackFileId(payloadRecord, imfErrorLogger);
                if (packageUUID == null) {
                    throw new MXFException("Unable to determine the package UUID from the payload record.");
                }
                for (IMFTrackFileResourceType tf : imfCompositionPlaylist.getTrackFileResources()) {
                    if (packageUUID.equals(UUIDHelper.fromUUIDAsURNStringToUUID(tf.getTrackFileId()))) {
                        referencedHeaderPayloads.put(packageUUID, payloadRecord);

                        Composition.HeaderPartitionTuple headerPartitionTuple = new Composition.HeaderPartitionTuple(headerPartition,
                                new ByteArrayByteRangeProvider(payloadRecord.getPayload()));

                        headerPartitionTuples.put(packageUUID, headerPartitionTuple);
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

        // only attempt to match/parse essence descriptors if ANY were provided
        if (essencesHeaderPartitionPayloads.isEmpty()) {
            return imfErrorLogger.getErrors();
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
            resourceEssenceDescriptorMap = getResourcesEssenceDescriptorsMap(imfCompositionPlaylist, headerPartitionTuples, imfErrorLogger);
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
        if(virtualBaseResourceList == null || virtualBaseResourceList.isEmpty()){
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






    private static Map<UUID, List<DOMNodeObjectModel>> getResourcesEssenceDescriptorsMap(IMFCompositionPlaylist imfCompositionPlaylist, Map<UUID, Composition.HeaderPartitionTuple> resourceUUIDHeaderPartitionMap, IMFErrorLogger imfErrorLogger) throws IOException {

        int previousNumberOfErrors = imfErrorLogger.getErrors().size();
        Map<UUID, List<DOMNodeObjectModel>> resourcesEssenceDescriptorMap = new LinkedHashMap<>();

        List<Composition.VirtualTrack> virtualTracks = new ArrayList<>(imfCompositionPlaylist.getVirtualTrackMap().values());

        /*Go through all the Virtual Tracks in the Composition and construct a map of Resource Source Encoding Element and a list of DOM nodes representing every EssenceDescriptor in the HeaderPartition corresponding to that Resource*/
        for (Composition.VirtualTrack virtualTrack : virtualTracks) {
            List<IMFCompositionPlaylist.ResourceIdTuple> resourceIdTuples = imfCompositionPlaylist.getVirtualTrackResourceIDs(virtualTrack);/*Retrieve a list of ResourceIDTuples corresponding to this virtual track*/
            for (IMFCompositionPlaylist.ResourceIdTuple resourceIdTuple : resourceIdTuples)
            {
                try
                {
                    Composition.HeaderPartitionTuple headerPartitionTuple = resourceUUIDHeaderPartitionMap.get(resourceIdTuple.getTrackFileId());
                    if (headerPartitionTuple != null)
                    {
                        /*Create a DOM Node representation of the EssenceDescriptors present in this header partition
                        corresponding to an IMFTrackFile*/
                        List<Node> essenceDescriptorDOMNodes = getEssenceDescriptorDOMNodes(headerPartitionTuple, imfErrorLogger);
                        List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
                        for (Node node : essenceDescriptorDOMNodes) {
                            try {
                                domNodeObjectModels.add(new DOMNodeObjectModel(node));
                            }
                            catch( IMFException e) {
                                imfErrorLogger.addAllErrors(e.getErrors());
                            }
                        }
                        resourcesEssenceDescriptorMap.put(resourceIdTuple.getSourceEncoding(), domNodeObjectModels);
                    }
                }
                catch( IMFException e)
                {
                    imfErrorLogger.addAllErrors(e.getErrors());
                }
            }
        }

        if( imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors()))
        {
            throw new IMFException("Failed to get Essence Descriptor for a resource", imfErrorLogger);
        }

        if (resourcesEssenceDescriptorMap.entrySet().size() == 0) {
            String message = "Composition does not refer to a single IMFEssence represented by the HeaderPartitions " +
                    "that were passed in.";
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, imfErrorLogger);
        }

        return Collections.unmodifiableMap(resourcesEssenceDescriptorMap);
    }


    private static List<Node> getEssenceDescriptorDOMNodes(Composition.HeaderPartitionTuple headerPartitionTuple, IMFErrorLogger imfErrorLogger) throws IOException {
        List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = headerPartitionTuple.getHeaderPartition().getEssenceDescriptors();
        List<Node> essenceDescriptorNodes = new ArrayList<>();
        for (InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
            try {
                KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                List<KLVPacket.Header> subDescriptorHeaders = getSubDescriptorKLVHeader(headerPartitionTuple.getHeaderPartition(), essenceDescriptor);
                /*Create a dom*/
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();

                DocumentFragment documentFragment = getEssenceDescriptorAsDocumentFragment(document, headerPartitionTuple, essenceDescriptorHeader, subDescriptorHeaders, imfErrorLogger);
                Node node = documentFragment.getFirstChild();
                essenceDescriptorNodes.add(node);
            } catch (ParserConfigurationException e) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            }
        }

        return essenceDescriptorNodes;
    }

    private static List<KLVPacket.Header> getSubDescriptorKLVHeader(HeaderPartition headerPartition, InterchangeObject.InterchangeObjectBO essenceDescriptor) {
        List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors(essenceDescriptor);
        for (InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors) {
            if (subDescriptorBO != null) {
                subDescriptorHeaders.add(subDescriptorBO.getHeader());
            }
        }
        return Collections.unmodifiableList(subDescriptorHeaders);
    }

    private static DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, Composition.HeaderPartitionTuple headerPartitionTuple, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header> subDescriptors, IMFErrorLogger imfErrorLogger) throws MXFException, IOException {
        document.setXmlStandalone(true);

        PrimerPack primerPack = headerPartitionTuple.getHeaderPartition().getPrimerPack();
        ResourceByteRangeProvider resourceByteRangeProvider = headerPartitionTuple.getResourceByteRangeProvider();
        RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(primerPack.getHeader(), getByteProvider(resourceByteRangeProvider, primerPack.getHeader()));
        Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, getByteProvider(resourceByteRangeProvider, essenceDescriptor));
        //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for (KLVPacket.Header subDescriptorHeader : subDescriptors) {
            subDescriptorTriplets.add(regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, getByteProvider(resourceByteRangeProvider, subDescriptorHeader)));
        }
        return regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document, imfErrorLogger);
    }


    private static ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        return byteProvider;
    }











}
