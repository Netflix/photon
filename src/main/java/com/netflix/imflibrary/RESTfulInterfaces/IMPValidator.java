package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.imp_validation.IMFMasterPackage;
import com.netflix.imflibrary.imp_validation.cpl.CompositionPlaylistConformanceValidator;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A RESTful interface for validating an IMF Master Package.
 */
public class IMPValidator {


    public static List<ErrorLogger.ErrorObject> validatePKL(PayloadRecord pkl) throws IOException {

        if(pkl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", pkl.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new PackingList(new ByteArrayByteRangeProvider(pkl.getPayload()), imfErrorLogger);
        }
        catch (SAXException | JAXBException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_PKL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validateAssetMap(PayloadRecord assetMap) throws IOException {
        if(assetMap.getPayloadAssetType() != PayloadRecord.PayloadAssetType.AssetMap){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMap.getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new AssetMap(new ByteArrayByteRangeProvider(assetMap.getPayload()), imfErrorLogger);
        }
        catch (SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validatePKLAndAssetMap(PayloadRecord assetMap, List<PayloadRecord> pkls) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<ResourceByteRangeProvider> resourceByteRangeProviders = new ArrayList<>();
        if(assetMap.getPayloadAssetType() != PayloadRecord.PayloadAssetType.AssetMap){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMap.getPayloadAssetType(), PayloadRecord.PayloadAssetType.AssetMap.toString()));
        }
        resourceByteRangeProviders.add(new ByteArrayByteRangeProvider(assetMap.getPayload()));
        for(PayloadRecord payloadRecord : pkls){
            if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.PackingList){
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", assetMap.getPayloadAssetType(), PayloadRecord.PayloadAssetType.PackingList.toString()));
            }
            resourceByteRangeProviders.add(new ByteArrayByteRangeProvider(payloadRecord.getPayload()));
        }
        new IMFMasterPackage(resourceByteRangeProviders, imfErrorLogger);
        return imfErrorLogger.getErrors();
    }

    public static List<ErrorLogger.ErrorObject> validateCPL(PayloadRecord cpl) throws IOException{
        if(cpl.getPayloadAssetType() != PayloadRecord.PayloadAssetType.CompositionPlaylist){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", cpl.getPayloadAssetType(), PayloadRecord.PayloadAssetType.CompositionPlaylist.toString()));
        }
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try{
            new CompositionPlaylist(new ByteArrayByteRangeProvider(cpl.getPayload()), imfErrorLogger);
        }
        catch(SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
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

    public static List<ErrorLogger.ErrorObject> isCPLConformed(
            PayloadRecord cplPayloadRecord,
            List<PayloadRecord> essencesHeaderPartition) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try {

            CompositionPlaylist compositionPlaylist = new CompositionPlaylist(new ByteArrayByteRangeProvider(cplPayloadRecord.getPayload()), imfErrorLogger);
            List<HeaderPartitionTuple> headerPartitionTuples = new ArrayList<>();
            for(PayloadRecord payloadRecord : essencesHeaderPartition){
                if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                    throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
                }
                headerPartitionTuples.add(new HeaderPartitionTuple(new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                                                            0L,
                                                            (long)payloadRecord.getPayload().length,
                                                            imfErrorLogger),
                                                            new ByteArrayByteRangeProvider(payloadRecord.getPayload())));
            }
            if(!new CompositionPlaylistConformanceValidator().isCompositionPlaylistConformed(compositionPlaylist, headerPartitionTuples)){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, "CompositionPlaylist is not conformed since its EssenceDescriptorList does not match the EssenceDescriptors of its Resources");
            }
        }
        catch (SAXException | JAXBException | URISyntaxException | MXFException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, e.getMessage());
            return imfErrorLogger.getErrors();
        }
        return imfErrorLogger.getErrors();
    }

    public static boolean isCPLMergeable(List<PayloadRecord> cplPayloadRecords){
        boolean result = true;

        return result;
    }

    /**
     * An object model for a HeaderPartition and access to the raw bytes corresponding to the HeaderPartition
     */
    public static class HeaderPartitionTuple {
        private final HeaderPartition headerPartition;
        private final ResourceByteRangeProvider resourceByteRangeProvider;

        private HeaderPartitionTuple(HeaderPartition headerPartition, ResourceByteRangeProvider resourceByteRangeProvider){
            this.headerPartition = headerPartition;
            this.resourceByteRangeProvider = resourceByteRangeProvider;
        }

        /**
         * A getter for the resourceByteRangeProvider object corresponding to this HeaderPartition to allow
         * access to the raw bytes
         * @return ResourceByteRangeProvider object corresponding to this HeaderPartition
         */
        public ResourceByteRangeProvider getResourceByteRangeProvider(){
            return this.resourceByteRangeProvider;
        }

        /**
         * A getter for the HeaderPartition object corresponding to a resource referenced from the CompositionPlaylist
         * @return HeaderPartition of a certain resource in the CompositionPlaylist
         */
        public HeaderPartition getHeaderPartition(){
            return this.headerPartition;
        }
    }
}
