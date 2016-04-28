package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ErrorLogger;

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
            PackingList packingList = new PackingList(new ByteArrayByteRangeProvider(pkl.getPayload()), imfErrorLogger);
        }
        catch(Exception e){
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    public static boolean validateAssetMap(PayloadRecord assetMap){

    }

    public static boolean validatePKLAndAssetMap(PayloadRecord pkl, PayloadRecord cpl){

    }

    public static boolean validateCPL(PayloadRecord cpl) {

    }

    /* IMF essence related inspection calls*/
    public static Long getIndexPartitionOffset(PayloadRecord essenceFooter4Bytes){

    }

    public static List<Long> getEssencePartitionOffsets(PayloadRecord randomIndexPartition){

    }

    public static boolean isCPLConformed(
            PayloadRecord cplPayloadRecord,
            List<PayloadRecord> essencesHeaderPartition){

    }

    public static boolean isCPLMergeable(List<PayloadRecord> cplPayloadRecords){

    }
}
