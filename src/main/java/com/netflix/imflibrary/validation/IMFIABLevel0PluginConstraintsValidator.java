package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_201.IABTrackFileConstraints;
import com.netflix.imflibrary.st2067_201.IMFIABConstraintsChecker;
import com.netflix.imflibrary.utils.*;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.*;

public class IMFIABLevel0PluginConstraintsValidator implements ConstraintsValidator {

    @Override
    public String getConstraintsSpecification() {
        return "IMF IAB Level 0 Plugin SMPTE ST2067-201";
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Composition.EditRate editRate = imfCompositionPlaylist.getEditRate();
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = imfCompositionPlaylist.getVirtualTrackMap();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = imfCompositionPlaylist.getEssenceDescriptorListMap();

        imfErrorLogger.addAllErrors(IMFIABConstraintsChecker.checkIABVirtualTrack(editRate, virtualTrackMap, essenceDescriptorListMap, Set.of()));


        return imfErrorLogger.getErrors();
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartitionPayload, @Nonnull List<PayloadRecord> indexPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /*
         * Verify that the input Payload is for an EssencePartitions. We'll further validate after filtering out any that are not referenced.
         */
        if (headerPartitionPayload.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Unable to validate any essence descriptors: payload asset type is %s, expected asset type %s",
                            headerPartitionPayload.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            return imfErrorLogger.getErrors();
        }


        /*
         * Find the IDs of all IAB Resources so that we can filter the provided header partition payloads

        Set<UUID> iabResourceIDs = new HashSet<>();
        for (Composition.VirtualTrack virtualTrack : imfCompositionPlaylist.getVirtualTrackMap().values()) {
            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.IABSequence)) {
                virtualTrack.getResourceList().forEach(imfBaseResourceType -> {
                    IMFTrackFileResourceType tfResource = (IMFTrackFileResourceType) imfBaseResourceType;
                    iabResourceIDs.add(UUIDHelper.fromUUIDAsURNStringToUUID(tfResource.getTrackFileId()));
                });
            }
        }
            */


        try {
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPartitionPayload.getPayload()),
                    0L,
                    (long) headerPartitionPayload.getPayload().length,
                    imfErrorLogger);

            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkMXFHeaderMetadata(headerPartitionOP1A, imfErrorLogger);
            IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
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


        try {
            imfErrorLogger.addAllErrors(validateIndexEditRate(headerPartitionPayload, indexPartitionPayloads));
        } catch (IOException e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "Unable to validate index edit rate: unable to parse index partition payload");
            return imfErrorLogger.getErrors();
        }

        return imfErrorLogger.getErrors();
    }


    /**
     * A stateless method, used for IMP containing IAB and/or MGA S-ADM tracks, that will validate that the index edit rate in the index segment matches the one in the descriptor (according to Section 5.7 of SMPTE ST 2067-201:2019)
     * @param headerPartitionPayloadRecord - an IMF Essence Component partition payload for header partitions
     * @param indexSegmentPayloadRecords - a list of IMF Essence Component partition payloads for index partitions
     * @return list of error messages encountered while validating
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    private static List<ErrorLogger.ErrorObject> validateIndexEditRate(PayloadRecord headerPartitionPayloadRecord, List<PayloadRecord> indexSegmentPayloadRecords) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

            if(headerPartitionPayloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s",
                                headerPartitionPayloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                return imfErrorLogger.getErrors();
            }

            HeaderPartition headerPartition = null;
            try {
                headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPartitionPayloadRecord.getPayload()),
                        0L, (long) headerPartitionPayloadRecord.getPayload().length, imfErrorLogger);

                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkMXFHeaderMetadata(headerPartitionOP1A, imfErrorLogger);

                for (PayloadRecord indexPayloadRecord : indexSegmentPayloadRecords) {
                    if (indexPayloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                                String.format("Payload asset type is %s, expected asset type %s",
                                        indexPayloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                        continue;
                    }
                    PartitionPack partitionPack = new PartitionPack(new ByteArrayDataProvider(indexPayloadRecord.getPayload()));
                    if (partitionPack.hasIndexTableSegments()) {//logic to provide as an input stream the portion of the archive that contains a Partition
                        ByteProvider imfEssenceComponentByteProvider = new ByteArrayDataProvider(indexPayloadRecord.getPayload());

                        long numBytesToRead = indexPayloadRecord.getPayload().length;
                        long numBytesRead = 0;
                        while (numBytesRead < numBytesToRead) {
                            KLVPacket.Header header = new KLVPacket.Header(imfEssenceComponentByteProvider, 0);
                            numBytesRead += header.getKLSize();

                            if (IndexTableSegment.isValidKey(header.getKey())) {
                                IndexTableSegment indexTableSegment = new IndexTableSegment(imfEssenceComponentByteProvider, header);
                                IABTrackFileConstraints.checkIndexEditRate(headerPartitionIMF, indexTableSegment, imfErrorLogger);
                            } else {
                                imfEssenceComponentByteProvider.skipBytes(header.getVSize());
                            }
                            numBytesRead += header.getVSize();
                        }

                    }
                }
            } catch (IMFException | MXFException e){
                if(headerPartition != null) {
                    Preface preface = headerPartition.getPreface();
                    GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                    SourcePackage filePackage = (SourcePackage) genericPackage;
                    UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("IMFTrackFile with ID %s has fatal errors", packageUUID.toString())));
                }
                if(e instanceof IMFException){
                    IMFException imfException = (IMFException)e;
                    imfErrorLogger.addAllErrors(imfException.getErrors());
                }
                else if(e instanceof MXFException){
                    MXFException mxfException = (MXFException)e;
                    imfErrorLogger.addAllErrors(mxfException.getErrors());
                }
            }

        return imfErrorLogger.getErrors();
    }

}
