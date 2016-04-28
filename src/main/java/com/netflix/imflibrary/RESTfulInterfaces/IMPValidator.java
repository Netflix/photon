package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ErrorLogger;

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
        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        return errors;
    }

    public static List<ErrorLogger.ErrorObject> validatePKLAndAssetMap(PayloadRecord pkl, PayloadRecord cpl){
        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        return errors;
    }

    public static List<ErrorLogger.ErrorObject> validateCPL(PayloadRecord cpl) {
        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        return errors;
    }

    /* IMF essence related inspection calls*/
    public static List<ErrorLogger.ErrorObject> getIndexPartitionOffset(PayloadRecord essenceFooter4Bytes){
        List<ErrorLogger.ErrorObject> errors = new ArrayList<>();

        return errors;
    }

    public static List<Long> getEssencePartitionOffsets(PayloadRecord randomIndexPartition){
        List<Long> offsets = new ArrayList<>();

        return offsets;
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
