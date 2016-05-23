package com.netflix.imflibrary.RESTfulInterfaces;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFOperationalPattern1A;
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
import java.util.Collections;
import java.util.List;

/**
 * A RESTful interface for validating an IMF Master Package.
 */
public class IMPValidator {

    /**
     * A stateless method that determines if the Asset type of the payload is an IMF AssetMap, Packinglist or CompositionPlaylist
     * @param payloadRecord - a payload record corresponding to the asset whose type needs to be confirmed
     * @return asset type of the payload either one of AssetMap, PackingList or CompositionPlaylist
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static PayloadRecord.PayloadAssetType getPayloadType(PayloadRecord payloadRecord) throws IOException {

        ResourceByteRangeProvider resourceByteRangeProvider = new ByteArrayByteRangeProvider(payloadRecord.getPayload());
        if(AssetMap.isFileOfSupportedSchema(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.AssetMap;
        }
        else if(PackingList.isFileOfSupportedSchema(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.PackingList;
        }
        else if(CompositionPlaylist.isFileOfSupportedSchema(resourceByteRangeProvider)){
            return PayloadRecord.PayloadAssetType.CompositionPlaylist;
        }
        return PayloadRecord.PayloadAssetType.Unknown;
    }

    /**
     * A stateless method that will validate an IMF PackingList document
     * @param pkl - a payload record for a Packing List document
     * @return list of error messages encountered while validating a Packing List document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
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

    /**
     * A stateless method that will validate an IMF AssetMap document
     * @param assetMap - a payload record for an AssetMap document
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
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

    /**
     * A stateless method that will validate IMF AssetMap and PackingList documents for all the data
     * that should be cross referenced by both
     * @param assetMap - a payload record for an AssetMap document
     * @param pkls - a list of payload records for Packing List documents referenced by the AssetMap
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
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

    /**
     * A stateless method that will validate an IMF CompositionPlaylist document
     * @param cpl - a payload record for a CompositionPlaylist document
     * @return list of error messages encountered while validating an AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
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

    /**
     * A stateless method that will return the size of the RandomIndexPack present within a MXF file. In a typical IMF workflow
     * this would be the first method that would need to be invoked to perform IMF essence component level validation
     * @param essenceFooter4Bytes - the last 4 bytes of the MXF file used to infer the size of the RandomIndexPack
     * @return a long integer value representing the size of the RandomIndexPack
     */
    public static Long getRandomIndexPackSize(PayloadRecord essenceFooter4Bytes){
        if(essenceFooter4Bytes.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssenceFooter4Bytes){
            throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", essenceFooter4Bytes.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssenceFooter4Bytes.toString()));
        }
        return (long)(ByteBuffer.wrap(essenceFooter4Bytes.getPayload()).getInt());
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
        RandomIndexPack randomIndexPack = new RandomIndexPack(new ByteArrayDataProvider(randomIndexPackPayload.getPayload()), 0L, randomIndexPackSize);
        return randomIndexPack.getAllPartitionByteOffsets();
    }

    /**
     * A stateless method that validates an IMFEssenceComponent's header partition and verifies MXF OP1A and IMF compliance. This could be utilized
     * to perform preliminary validation of IMF essences
     * @param essencesHeaderPartition - a list of IMF Essence Component header partition payloads
     * @return a list of errors encountered while performing compliance checks on the IMF Essence Component Header partition
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> validateIMFEssenceComponentHeaderMetadata(List<PayloadRecord> essencesHeaderPartition) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        for(PayloadRecord payloadRecord : essencesHeaderPartition){
            if(payloadRecord.getPayloadAssetType() != PayloadRecord.PayloadAssetType.EssencePartition){
                throw new IMFException(String.format("Payload asset type is %s, expected asset type %s", payloadRecord.getPayloadAssetType(), PayloadRecord.PayloadAssetType.EssencePartition.toString()));
            }
            HeaderPartition headerPartition = new HeaderPartition(new ByteArrayDataProvider(payloadRecord.getPayload()),
                    0L,
                    (long)payloadRecord.getPayload().length,
                    imfErrorLogger);
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition);
            IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A stateless method that can be used to determine if a CompositionPlaylist is conformant. Conformance checks
     * perform deeper inspection of the CompositionPlaylist and the EssenceDescriptors corresponding to the
     * resources referenced by the CompositionPlaylist
     * @param cplPayloadRecord a payload record corresponding to the CompositionPlaylist payload
     * @param essencesHeaderPartition list of payload records containing the raw bytes of the HeaderPartitions of the IMF essences referenced in the CompositionPlaylist
     * @return list of error messages encountered while performing conformance validation of the CompositionPlaylist document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static List<ErrorLogger.ErrorObject> isCPLConformed(
            PayloadRecord cplPayloadRecord,
            List<PayloadRecord> essencesHeaderPartition) throws IOException {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try {

            List<ErrorLogger.ErrorObject> errors = new ArrayList<>(validateCPL(cplPayloadRecord));
            errors.addAll(validateIMFEssenceComponentHeaderMetadata(essencesHeaderPartition));
            if(errors.size() > 0){
                return Collections.unmodifiableList(errors);
            }

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

    /**
     * A stateless method that determines if 2 or more CompositionPlaylist documents corresponding to the same title can be inferred to
     * represent the same presentation timeline. This method is present to work around current limitations in the IMF eco system
     * wherein CPL's might not be built incrementally to include all the IMF essences that are a part of the same timeline
     * @param cplPayloadRecords - a list of payload records corresponding to each of the CompositionPlaylist documents
     *                          that need to be verified for mergeability
     * @return a boolean indicating if the CPLs can be merged or not
     */
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
