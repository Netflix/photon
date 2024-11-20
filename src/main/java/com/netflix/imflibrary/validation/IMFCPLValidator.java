package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


abstract public class IMFCPLValidator implements ConstraintsValidator {

    protected static List<ErrorLogger.ErrorObject> validateCommonConstraints(IMFCompositionPlaylist imfCompositionPlaylist){

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

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

        return imfErrorLogger.getErrors();
    }

    protected static List<ErrorLogger.ErrorObject> validateEssenceDescriptors(IMFCompositionPlaylist imfCompositionPlaylist, List<PayloadRecord> headerPartitionPayloads) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

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

        /*
         * Verify that the input Payloads are EssencePartitions. We'll further validate them after filtering out any that are not referenced.
         */
        for (PayloadRecord payloadRecord : headerPartitionPayloads) {
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Unable to validate any essence descriptors: payload asset type is %s, expected asset type %s",
                                payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                return imfErrorLogger.getErrors();
            }
        }

        /*
         * Collect the UUIDs from the header payloads and filter out any that are _not_ referenced from the input composition
         */
        Map<UUID, PayloadRecord> referencedHeaderPayloads = new HashMap<>();
        List<Composition.HeaderPartitionTuple> headerPartitionTuples = new ArrayList<>();

        for (PayloadRecord payloadRecord : headerPartitionPayloads) {
            try{
                HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long) payloadRecord.getPayload().length,
                        imfErrorLogger);
                Preface preface = headerPartition.getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

                for (IMFTrackFileResourceType tf : imfCompositionPlaylist.getTrackFileResources()) {
                    if (packageUUID.equals(UUIDHelper.fromUUIDAsURNStringToUUID(tf.getTrackFileId()))) {
                        referencedHeaderPayloads.put(packageUUID, payloadRecord);

                        headerPartitionTuples.add(new Composition.HeaderPartitionTuple(new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                                0L,
                                (long) payloadRecord.getPayload().length,
                                imfErrorLogger),
                                new ByteArrayByteRangeProvider(payloadRecord.getPayload())));

                        break;
                    }
                }

            } catch (MXFException e) {
                imfErrorLogger.addAllErrors(e.getErrors());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        "Unable to validate any essence descriptors: unable to parse essence partition payload");
                return imfErrorLogger.getErrors();
            } catch (IOException e) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        "Unable to validate any essence descriptors: unable to parse essence partition payload");
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
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "Unable to validate essence descriptors: failed to retrieve essence descriptor map from CPL"));
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
