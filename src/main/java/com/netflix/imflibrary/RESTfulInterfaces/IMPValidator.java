package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ErrorLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A RESTful interface for validating an IMF Master Package.
 */
public class IMPValidator {


    public static List<ErrorLogger.ErrorObject> validatePKL(PayloadRecord pkl){

        if(pkl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", pkl.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new PackingList(new ByteArrayByteRangeProvider(pkl.getPayload()), imfErrorLogger);
        }
        catch(Exception e){
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validateAssetMap(PayloadRecord assetMap){
        if(assetMap.getPayloadAssetType() != PayloadRecord.PayloadAssetType.AssetMap){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMap.getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new AssetMap(new ByteArrayByteRangeProvider(assetMap.getPayload()), imfErrorLogger);
        }
        catch(Exception e){
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validatePKLAndAssetMap(PayloadRecord pkl, PayloadRecord cpl){
        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        return errors;
    }

    public static List<ErrorLogger.ErrorObject> validateCPL(PayloadRecord cpl) {
        if(cpl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.CompositionPlaylist){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", cpl.getPayloadAssetType(), PayloadRecord.PayloadAssetType.CompositionPlaylist.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new CompositionPlaylist(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger);
        }
        catch(Exception e){
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    /* IMF essence related inspection calls*/
    public static Long getRandomIndexPackSize(PayloadRecord essenceFooter4Bytes){
        if(essenceFooter4Bytes.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssenceFooter4Bytes){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", essenceFooter4Bytes.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssenceFooter4Bytes.toString()));
        }
        return (long)(ByteBuffer.wrap(essenceFooter4Bytes.getPayload()).getInt());
    }

    public static List<Long> getEssencePartitionOffsets(PayloadRecord randomIndexPackPayload, Long randomIndexPackSize) throws IOException {
        RandomIndexPack randomIndexPack = new RandomIndexPack(new ByteArrayDataProvider(randomIndexPackPayload.getPayload()), 0L, randomIndexPackSize);
        return randomIndexPack.getAllPartitionByteOffsets();
    }

    public static boolean isCPLConformed(
            PayloadRecord cplPayloadRecord,
            List<PayloadRecord> essencesHeaderPartition){
        boolean result = true;

        return result;
    }

    public static boolean isCPLMergeable(List<PayloadRecord> cplPayloadRecords){
        boolean result = true;

        return result;
    }
}