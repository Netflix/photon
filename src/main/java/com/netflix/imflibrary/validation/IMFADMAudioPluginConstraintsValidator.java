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
import com.netflix.imflibrary.st2067_204.ADMAudioTrackFileConstraints;
import com.netflix.imflibrary.st2067_204.IMFADMAudioConstraintsChecker;
import com.netflix.imflibrary.utils.*;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.*;

/**
 * Collection of properties and validations specific to ST 2067-204.
 */
public class IMFADMAudioPluginConstraintsValidator implements ConstraintsValidator {

    @Override
    public String getConstraintsSpecification() {
        return "SMPTE ST 2067-204:2026-05 Audio with ADM Metadata Plug-in ";
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Composition.EditRate editRate = imfCompositionPlaylist.getEditRate();
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = imfCompositionPlaylist.getVirtualTrackMap();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = imfCompositionPlaylist.getEssenceDescriptorListMap();

        // ST 2067-204 ADMAudioVirtualTrackParameterSet checks
        imfErrorLogger.addAllErrors(IMFADMAudioConstraintsChecker.checkADMAudioVirtualTrackParameterSet(imfCompositionPlaylist));

        // ST 2067-204 ADMAudioVirtualTrack checks
        imfErrorLogger.addAllErrors(IMFADMAudioConstraintsChecker.checkADMAudioVirtualTrack(editRate, virtualTrackMap, essenceDescriptorListMap, Set.of()));

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

        try {
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPartitionPayload.getPayload()),
                    0L,
                    (long) headerPartitionPayload.getPayload().length,
                    imfErrorLogger);

            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkMXFHeaderMetadata(headerPartitionOP1A, imfErrorLogger);
            ADMAudioTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
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

        return imfErrorLogger.getErrors();
    }
}
