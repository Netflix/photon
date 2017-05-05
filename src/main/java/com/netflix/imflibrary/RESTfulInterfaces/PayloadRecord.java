package com.netflix.imflibrary.RESTfulInterfaces;


import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * An object model for submitting Payloads to Photon
 */
public class PayloadRecord {
    private final byte[] payload;
    private final PayloadAssetType payloadAssetType;
    private final Long originalFileOffset;
    private final Long originalSize;

    /**
     * A constructor for the Payloads to be passed in to Photon for validation/metadata extraction.
     * @param payload - a byte[] consisting of the raw bytes of the payload
     * @param payloadAssetType - a payload asset type to indicate what data is being passed in
     * @param originalFileOffset - offset of the data in the original asset/file
     * @param originalSize - size of the payload as read from the original asset/file
     */
    public PayloadRecord(byte[] payload, PayloadAssetType payloadAssetType, @Nullable Long originalFileOffset, @Nullable Long originalSize){
        this.payload = Arrays.copyOf(payload, payload.length);
        this.payloadAssetType = payloadAssetType;
        this.originalFileOffset = originalFileOffset;
        this.originalSize = originalSize;
    }

    /**
     * A getter for the Payload raw bytes
     * @return a byte[] containing the raw bytes that need to be analyzed
     */
    public byte[] getPayload(){
        return Arrays.copyOf(payload, payload.length);
    }

    /**
     * A getter for the Payload Asset type
     * @return the PayloadAssetType of the payload
     */
    public PayloadAssetType getPayloadAssetType(){
        return this.payloadAssetType;
    }

    /**
     * Enumerates the different AssetTypes that can be passed to this library for analysis
     */
    public enum PayloadAssetType {

        CompositionPlaylist("Composition"),
        PackingList ("PackingList"),
        AssetMap("AssetMap"),
        TextPartition("TextPartition"),
        EssenceFooter4Bytes("EssenceFooter4Bytes"),
        EssencePartition("EssencePartition"),
        OutputProfileList("OutputProfileList"),
        Unknown("Unknown");

        private final String assetType;

        PayloadAssetType(String assetType){
            this.assetType = assetType;
        }

        /**
         * A toString() method
         * @return string representation of this enumeration constant
         */
        public String toString(){
            return this.assetType;
        }
    }
}
