package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.annotations.MXFProperty;

import javax.annotation.concurrent.Immutable;

public class TextBasedObject extends InterchangeObject {
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + TextBasedObject.class.getSimpleName() + " : ";
    private final TextBasedObjectBO textBasedObjectBO;

    public TextBasedObject(TextBasedObjectBO textBasedObjectBO) {
        this.textBasedObjectBO = textBasedObjectBO;
    }

    public String getDescription() {
        return this.textBasedObjectBO.description;
    }

    public String getMimeType() {
        return this.textBasedObjectBO.mime_type;
    }

    public String getLanguageCode() {
        return this.textBasedObjectBO.language_code;
    }
    /**
     * Object corresponding to a parsed TextBasedObject structural metadata set defined in RP 2057:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static class TextBasedObjectBO extends InterchangeObjectBO {

        @MXFProperty(size=16) protected final byte[] metadata_payload_scheme_id = null;
        @MXFProperty(size=0, charset = "UTF-16") protected final String mime_type = null;
        @MXFProperty(size=0, charset = "UTF-16") protected final String language_code = null;
        @MXFProperty(size=0, charset = "UTF-16") protected final String description = null;
        private final IMFErrorLogger imfErrorLogger;

        /**
         * Instantiates a new parsed TextBasedObjectBO object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param imfErrorLogger logger for recording any parsing errors
         */
        TextBasedObjectBO(KLVPacket.Header header, IMFErrorLogger imfErrorLogger)
        {
            super(header);
            this.imfErrorLogger = imfErrorLogger;
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
            return sb.toString();
        }

        protected void postPopulateCheck() {
            if (this.metadata_payload_scheme_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ERROR_DESCRIPTION_PREFIX + "metadata_payload_scheme_id is null");
            }

            if (this.mime_type == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ERROR_DESCRIPTION_PREFIX + "mime_type is null");
            }

            if (this.language_code == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ERROR_DESCRIPTION_PREFIX + "language_code is null");
            }
        }
    }

}
