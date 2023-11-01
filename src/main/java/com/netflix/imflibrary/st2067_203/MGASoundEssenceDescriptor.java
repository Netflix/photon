package com.netflix.imflibrary.st2067_203;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.GenericSoundEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Object model corresponding to MGASoundEssenceDescriptor structural metadata set defined in st2067-203:2023
 */
@Immutable
public class MGASoundEssenceDescriptor extends GenericSoundEssenceDescriptor {

    public final static UL MGA_AUDIOESSENCE_UNCOMPRESSED_SOUND_ENCODING_LABEL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.04020201.02010000");
    public final static UL IMF_MGASADM_ESSENCE_CLIP_WRAPPED_ELEMENT_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2B34.01020101.0D010301.16000F00");
    public final static UL IMF_MGASADM_ESSENCE_CLIP_WRAPPED_CONTAINER_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.0d010301.02250200");
    public final static UL MGAAudioEssenceUncompressedSoundCoding = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.04020201.02010000");
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + MGASoundEssenceDescriptor.class.getSimpleName() + " : ";

    public MGASoundEssenceDescriptor(MGASoundEssenceDescriptorBO mgaSoundEssenceDescriptorBO)
    {
        this.genericSoundEssenceDescriptorBO = mgaSoundEssenceDescriptorBO;
    }

    public List<Long> getSampleRate() {
        return this.genericSoundEssenceDescriptorBO.getSampleRate();
    }

    /**
     * Object corresponding to a parsed MGASoundEssenceDescriptor structural metadata set defined in st2067_201
     */
    @Immutable
    public static final class MGASoundEssenceDescriptorBO extends GenericSoundEssenceDescriptor.GenericSoundEssenceDescriptorBO {

        @MXFProperty(size=2) protected final Short mga_sound_essence_block_align = null;
        @MXFProperty(size=4) protected final Long mga_sound_average_bytes_per_second = null;
        @MXFProperty(size=1) protected final Short mga_sound_essence_sequence_offset = null;

        /**
         * Constructor for a MGASoundEssenceDescriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public MGASoundEssenceDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException {
            super(header, imfErrorLogger);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            postPopulateCheck();

            if (this.mga_sound_essence_block_align == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                		MGASoundEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_sound_essence_block_align is null");
            }
            if (this.mga_sound_average_bytes_per_second == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                		MGASoundEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "mga_sound_average_bytes_per_second is null");
            }
        }

        public boolean equals(Object other) {
            if (!(other instanceof MGASoundEssenceDescriptorBO)) {
                return false;
            }
            return super.equals(other);
        }

        /**
         * A method that returns a string representation of a MGASoundEssenceDescriptorBO object
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
