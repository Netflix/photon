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

    final static UL IMF_IAB_TRACK_FILE_LEVEL0_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2B34.0401010D.01010201.02000000");
    public final static UL IMMERSIVE_AUDIO_CODING_LABEL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2B34.04010105.0E090604.00000000");
    public final static UL IMF_IAB_ESSENCE_CLIP_WRAPPED_ELEMENT_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2B34.01020101.0D010301.16000D00");
    public final static UL IMF_IAB_ESSENCE_CLIP_WRAPPED_CONTAINER_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2B34.0401010D.0D010301.021D0101");

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
