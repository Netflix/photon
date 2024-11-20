package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.st2067_203.MGASADMTrackFileConstraints;
import com.netflix.imflibrary.st2067_203.IMFMGASADMConstraintsChecker;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.*;

public class IMFMGASADMPluginConstraintsValidator implements ConstraintsValidator {

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

        // ST 2067-203 MGASADMVirtualTrackParameterSet checks
        List<ErrorLogger.ErrorObject> errors = IMFMGASADMConstraintsChecker.checkMGASADMVirtualTrackParameterSet(imfCompositionPlaylist);
        imfErrorLogger.addAllErrors(errors);

        errors = IMFMGASADMConstraintsChecker.checkMGASADMVirtualTrack(editRate, virtualTrackMap, essenceDescriptorListMap, Set.of());
        imfErrorLogger.addAllErrors(errors);



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
         * Find the IDs of all MGA SADM Resources so that we can filter the provided header partition payloads
         */
        Set<UUID> mgaSADMResourceIDs = new HashSet<>();
        for (Composition.VirtualTrack virtualTrack : imfCompositionPlaylist.getVirtualTrackMap().values()) {
            if (virtualTrack.getSequenceTypeEnum().equals(Composition.SequenceTypeEnum.MGASADMSignalSequence)) {
                virtualTrack.getResourceList().forEach(imfBaseResourceType -> {
                    IMFTrackFileResourceType tfResource = (IMFTrackFileResourceType) imfBaseResourceType;
                    mgaSADMResourceIDs.add(UUIDHelper.fromUUIDAsURNStringToUUID(tfResource.getTrackFileId()));
                });
            }
        }

        for (PayloadRecord payloadRecord : headerPartitionPayloads) {
            try {
                HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long) payloadRecord.getPayload().length,
                        imfErrorLogger);
                Preface preface = headerPartition.getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();

                if (mgaSADMResourceIDs.contains(packageUUID)) {
                    MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                    IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                    MGASADMTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
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

        return imfErrorLogger.getErrors();
    }


}
