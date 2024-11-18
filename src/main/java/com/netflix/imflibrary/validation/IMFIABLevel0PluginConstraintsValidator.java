package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMFTrackFileReader;
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
import com.netflix.imflibrary.st2067_203.MGASADMTrackFileConstraints;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;

import java.io.IOException;
import java.util.*;

public class IMFIABLevel0PluginConstraintsValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateTrackFileConstraints(IMFTrackFileReader imfTrackFileReader) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = imfTrackFileReader.getHeaderPartitionIMF(imfErrorLogger);
            IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
        } catch (IOException e) {
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(
                    IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Exception while retrieving Track File information for validation: %s", e)));

        }

        return imfErrorLogger.getErrors();
    }


    @Override
    public String getConstraintsSpecification() {
        return "IMF IAB Level 0 Plugin SMPTE ST2067-201";
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(IMFCompositionPlaylist IMFCompositionPlaylist) {

        Composition.EditRate editRate = IMFCompositionPlaylist.getEditRate();
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = IMFCompositionPlaylist.getVirtualTrackMap();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = IMFCompositionPlaylist.getEssenceDescriptorListMap();

        return IMFIABConstraintsChecker.checkIABVirtualTrack(editRate, virtualTrackMap, essenceDescriptorListMap, Set.of());
    }


    /**
     * A stateless method, used for IMP containing IAB and/or MGA S-ADM tracks, that will validate that the index edit rate in the index segment matches the one in the descriptor (according to Section 5.7 of SMPTE ST 2067-201:2019)
     * @param headerPartitionPayloadRecords - a list of IMF Essence Component partition payloads for header partitions
     * @param indexSegmentPayloadRecords - a list of IMF Essence Component partition payloads for index partitions
     * @return list of error messages encountered while validating
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    private static List<ErrorLogger.ErrorObject> validateIndexEditRate(List<PayloadRecord> headerPartitionPayloadRecords, List<PayloadRecord> indexSegmentPayloadRecords) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> essencesHeaderPartition = Collections.unmodifiableList(headerPartitionPayloadRecords);
        for(PayloadRecord headerPayloadRecord : essencesHeaderPartition){
            if(headerPayloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s",
                                headerPayloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                continue;
            }

            HeaderPartition headerPartition = null;
            try {
                headerPartition = new HeaderPartition(new ByteArrayDataProvider(headerPayloadRecord.getPayload()),
                        0L, (long) headerPayloadRecord.getPayload().length, imfErrorLogger);

                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);

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
                                if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.IABEssence)) {
                                    IABTrackFileConstraints.checkIndexEditRate(headerPartitionIMF, indexTableSegment, imfErrorLogger);
                                } else if (headerPartitionIMF.hasMatchingEssence(HeaderPartition.EssenceTypeEnum.MGASADMEssence)) {
                                    MGASADMTrackFileConstraints.checkIndexEditRate(headerPartitionIMF, indexTableSegment, imfErrorLogger);
                                }
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
        }
        return imfErrorLogger.getErrors();
    }


    /**
     * A stateless method that validates an IMFEssenceComponent's header partition and verifies MXF OP1A and IMF compliance. This could be utilized
     * to perform preliminary validation of IMF essences
     * @param essencesHeaderPartitionPayloads - a list of IMF Essence Component header partition payloads
     * @return a list of errors encountered while performing compliance checks on the IMF Essence Component Header partition
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateIMFTrackFileHeaderMetadata(List<PayloadRecord> essencesHeaderPartitionPayloads) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<PayloadRecord> essencesHeaderPartition = Collections.unmodifiableList(essencesHeaderPartitionPayloads);

        for (PayloadRecord payloadRecord : essencesHeaderPartition) {
            if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format("Payload asset type is %s, expected asset type %s",
                                payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                return imfErrorLogger.getErrors();
            }

            HeaderPartition headerPartition = null;
            try {
                headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                        0L,
                        (long)payloadRecord.getPayload().length,
                        imfErrorLogger);
                MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
                IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.IABEssence) {
                    IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
                if (headerPartitionIMF.getEssenceType() == HeaderPartition.EssenceTypeEnum.MGASADMEssence) {
                    MGASADMTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
                }
            }
            catch (IMFException | MXFException e){
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
        }

        return imfErrorLogger.getErrors();
    }

}
