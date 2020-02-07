package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

public class GenericStreamTextBasedSet extends TextBasedObject {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + GenericStreamTextBasedSet.class.getSimpleName() + " : ";
    private final GenericStreamTextBasedSetBO genericStreamTextBasedSetBO;

    public GenericStreamTextBasedSet(GenericStreamTextBasedSetBO genericStreamTextBasedSetBO) {
        super(genericStreamTextBasedSetBO);
        this.genericStreamTextBasedSetBO = genericStreamTextBasedSetBO;
    }

    public Long getGenericStreamId() {
        return this.genericStreamTextBasedSetBO.generic_stream_id;
    }

    /**
     * Object corresponding to a parsed GenericStreamTextBasedSet structural metadata set defined in RP 2057:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class GenericStreamTextBasedSetBO extends TextBasedObjectBO {

        @MXFProperty(size=4) protected final Long generic_stream_id = null;
        private final IMFErrorLogger imfErrorLogger;

        /**
         * Instantiates a new parsed GenericStreamTextBasedSetBO object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         */
        public GenericStreamTextBasedSetBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException {
            super(header, imfErrorLogger);
            this.imfErrorLogger = imfErrorLogger;

            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            postPopulateCheck();

        }

        protected void postPopulateCheck() {
            super.postPopulateCheck();

            if (generic_stream_id == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ERROR_DESCRIPTION_PREFIX + "generic_stream_id is null");
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("================== TextBasedObject ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("mime_type = %s%n", this.mime_type));
            sb.append(String.format("language_code = %s%n", this.language_code));
            if (this.description != null) {
                sb.append(String.format("description = %s%n", this.description));
            }
            sb.append(String.format("generic_stream_id = %d%n", this.generic_stream_id));
            return sb.toString();
        }
    }

}
