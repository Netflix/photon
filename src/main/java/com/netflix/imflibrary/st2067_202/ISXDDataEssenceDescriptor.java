package com.netflix.imflibrary.st2067_202;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.st0377.header.GenericDataEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ISXDEssenceDescriptor structural metadata set defined in st2067-202
 */
@Immutable
public class ISXDDataEssenceDescriptor extends GenericDataEssenceDescriptor {

    public final static UL UTF8_TEXT_DATA_ESSENCE_CODING_LABEL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2b34.04010105.0E090606.00000000 ");
    public final static UL IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2b34.04010105.0E090607.01010103");

      /**
     * Getter for the Essence Container UL of this FileDescriptor
     * @return a UL representing the Essence Container
     */
    public static UL getEssenceContainerUL(){
        return IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL;
    }

    public ISXDDataEssenceDescriptor(ISXDEssenceDescriptorBO isxdEssenceDescriptorBO)
    {
        this.genericDataEssenceDescriptorBO = isxdEssenceDescriptorBO;
    }

    /**
     * Object corresponding to a parsed ISXDEssenceDescriptor structural metadata set defined in st2067_202
     */
    @Immutable
    public static final class ISXDEssenceDescriptorBO extends GenericDataEssenceDescriptor.GenericDataEssenceDescriptorBO {

        /**
         * Constructor for a ISXDEssenceDescriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ISXDEssenceDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);
        }

        public boolean equals(Object other) {
            if (!(other instanceof ISXDEssenceDescriptorBO)) {
                return false;
            }
            return super.equals(other);
        }

        /**
         * A method that returns a string representation of a ISXDEssenceDescriptorBO object
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
