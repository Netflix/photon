package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MXFUtils {

    @Nullable
    public static PayloadRecord getHeaderPartitionPayloadRecord(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        if(rangeStart < 0 ) {
            return null;
        }
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = getRandomIndexPackSize(payloadRecord);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;
        if(rangeStart < 0 ) {
            return null;
        }

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize);

        if (partitionByteOffsets.size() >= 2) {
            rangeStart = partitionByteOffsets.get(0);
            rangeEnd = partitionByteOffsets.get(1) - 1;
            byte[] headerPartitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            PayloadRecord headerParitionPayload = new PayloadRecord(headerPartitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
            return headerParitionPayload;
        }

        return null;

    }

    private static PartitionPack getPartitionPack(ResourceByteRangeProvider resourceByteRangeProvider, long resourceOffset) throws IOException
    {
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        KLVPacket.Header header;
        {//logic to provide as an input stream the portion of the archive that contains PartitionPack KLVPacker Header
            long rangeStart = resourceOffset;
            long rangeEnd = resourceOffset +
                    (KLVPacket.KEY_FIELD_SIZE + KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE) -1;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            byte[] bytesWithPartitionPackKLVPacketHeader = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
            ByteProvider imfEssenceComponentByteProvider = new ByteArrayDataProvider(bytesWithPartitionPackKLVPacketHeader);
            header = new KLVPacket.Header(imfEssenceComponentByteProvider, rangeStart);
        }

        PartitionPack partitionPack;
        long rangeStart = resourceOffset;
        long rangeEnd = resourceOffset + header.getKLSize() + header.getVSize() - 1;
        rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

        byte[] partitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        partitionPack = new PartitionPack(new ByteArrayDataProvider(partitionBytes));

        return partitionPack;
    }

    public static List<PayloadRecord> getIndexTablePartitionPayloadRecords(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        List<PayloadRecord> payloadRecords = new ArrayList<>();
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - 4;
        if(rangeStart < 0 ) {
            return payloadRecords;
        }
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.EssenceFooter4Bytes, rangeStart, rangeEnd);
        Long randomIndexPackSize = getRandomIndexPackSize(payloadRecord);

        rangeStart = archiveFileSize - randomIndexPackSize;
        rangeEnd = archiveFileSize - 1;
        if(rangeStart < 0 ) {
            return payloadRecords;
        }

        byte[] randomIndexPackBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
        PayloadRecord randomIndexPackPayload = new PayloadRecord(randomIndexPackBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
        List<Long> partitionByteOffsets = new ArrayList<>();
        partitionByteOffsets.addAll(getEssencePartitionOffsets(randomIndexPackPayload, randomIndexPackSize));
        partitionByteOffsets.add(resourceByteRangeProvider.getResourceSize());

        for(int i =0; i < partitionByteOffsets.size() -1; i++) {
            rangeStart = partitionByteOffsets.get(i);
            rangeEnd = partitionByteOffsets.get(i+1) - 1;
            PartitionPack partitionPack = getPartitionPack(resourceByteRangeProvider, rangeStart);
            //2067-5 section 5.1.1
            if (partitionPack.hasEssenceContainer() && partitionPack.hasIndexTableSegments()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("Partition %d has both index table and essence", i));
            }
            else if (partitionPack.hasIndexTableSegments()) {
                byte[] partitionBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
                PayloadRecord partitionPayloadRecord = new PayloadRecord(partitionBytes, PayloadRecord.PayloadAssetType.EssencePartition, rangeStart, rangeEnd);
                payloadRecords.add(partitionPayloadRecord);
            }
        }
        return payloadRecords;

    }

    /**
     * A stateless method that will read and parse the RandomIndexPack within a MXF file and return a list of byte offsets
     * corresponding to the partitions of the MXF file. In a typical IMF workflow this would be the second method after
     * {@link #getRandomIndexPackSize(PayloadRecord)} that would need to be invoked to perform IMF essence component
     * level validation
     * @param randomIndexPackPayload - a payload containing the raw bytes corresponding to the RandomIndexPack of the MXF file
     * @param randomIndexPackSize - size of the RandomIndexPack of the MXF file
     * @return list of long integer values representing the byte offsets of the partitions in the MXF file
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<Long> getEssencePartitionOffsets(PayloadRecord randomIndexPackPayload, Long randomIndexPackSize) throws IOException {
        if(randomIndexPackPayload.getPayload().length != randomIndexPackSize){
            throw new IllegalArgumentException(String.format("RandomIndexPackSize passed in is = %d, RandomIndexPack payload size = %d, they should be equal", randomIndexPackSize, randomIndexPackPayload.getPayload().length));
        }
        RandomIndexPack randomIndexPack = new RandomIndexPack(new ByteArrayDataProvider(randomIndexPackPayload.getPayload()), 0L, randomIndexPackSize);
        return randomIndexPack.getAllPartitionByteOffsets();
    }

    /**
     * A stateless method that will return the size of the RandomIndexPack present within a MXF file. In a typical IMF workflow
     * this would be the first method that would need to be invoked to perform IMF essence component level validation
     * @param essenceFooter4Bytes - the last 4 bytes of the MXF file used to infer the size of the RandomIndexPack
     * @return a long integer value representing the size of the RandomIndexPack
     */
    public static Long getRandomIndexPackSize(PayloadRecord essenceFooter4Bytes){
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(essenceFooter4Bytes.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssenceFooter4Bytes){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s",
                    essenceFooter4Bytes.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssenceFooter4Bytes
                            .toString()), imfErrorLogger);
        }
        return (long)(ByteBuffer.wrap(essenceFooter4Bytes.getPayload()).getInt());
    }


    public static UUID getTrackFileId(PayloadRecord payloadRecord, IMFErrorLogger imfErrorLogger) throws
            IOException {

        if (payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMP_VALIDATOR_PAYLOAD_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(),
                            PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            return null;
        }

        HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                0L,
                (long) payloadRecord.getPayload().length,
                imfErrorLogger);

        Preface preface = headerPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage) genericPackage;
        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
        return packageUUID;
    }



}
