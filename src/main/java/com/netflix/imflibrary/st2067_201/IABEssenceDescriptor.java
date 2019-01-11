package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Object model corresponding to IABEssenceDescriptor structural metadata set defined in st2067-201:201x
 */
@Immutable
public class IABEssenceDescriptor extends GenericSoundEssenceDescriptor {

    private final static byte[] IMF_IAB_TRACK_FILE_LEVEL0_KEY = { 0x06, 0x0E, 0x2B, 0x34, 0x04, 0x01, 0x01, 0x0D, 0x01, 0x01, 0x02, 0x01, 0x02, 0x00, 0x00, 0x00};
    public final static UL IMF_IAB_TRACK_FILE_LEVEL0_UL = new UL(IMF_IAB_TRACK_FILE_LEVEL0_KEY);

    private final static byte[] IMMERSIVE_AUDIO_CODING_KEY = { 0x06, 0x0E, 0x2B, 0x34, 0x04, 0x01, 0x01, 0x05, 0x0E, 0x09, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00};
    public final static UL IMMERSIVE_AUDIO_CODING_LABEL = new UL(IMMERSIVE_AUDIO_CODING_KEY);

    private final static byte[] IMF_IAB_ESSENCE_CLIP_WRAPPED_ELEMENT_KEY = { 0x06, 0x0E, 0x2B, 0x34, 0x01, 0x02, 0x01, 0x01, 0x0D, 0x01, 0x03, 0x01, 0x16, 0x00, 0x0D, 0x00};
    public final static UL IMF_IAB_ESSENCE_CLIP_WRAPPED_ELEMENT_UL = new UL(IMF_IAB_ESSENCE_CLIP_WRAPPED_ELEMENT_KEY);

    private final static byte[] IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_KEY = { 0x06, 0x0E, 0x2B, 0x34, 0x04, 0x01, 0x01, 0x0D, 0x0D, 0x01, 0x03, 0x01, 0x02, 0x1D, 0x01, 0x01};
    public final static UL IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL = new UL(IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_KEY);

    public IABEssenceDescriptor(IABEssenceDescriptorBO iabEssenceDescriptorBO)
    {
        this.genericSoundEssenceDescriptorBO = iabEssenceDescriptorBO;
    }

    public List<Long> getSampleRate() {
        return this.genericSoundEssenceDescriptorBO.getSampleRate();
    }

    /**
     * Object corresponding to a parsed IABEssenceDescriptor structural metadata set defined in st2067_201
     */
    @Immutable
    public static final class IABEssenceDescriptorBO extends GenericSoundEssenceDescriptor.GenericSoundEssenceDescriptorBO {

        /**
         * Constructor for a File descriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        public IABEssenceDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException {
            super(header, imfErrorLogger);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            postPopulateCheck();
        }

        public boolean equals(Object other) {
            if (!(other instanceof IABEssenceDescriptorBO)) {
                return false;
            }
            return super.equals(other);
        }

        /**
         * A method that returns a string representation of a IABEssenceDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            return sb.toString();
        }
    }
}
