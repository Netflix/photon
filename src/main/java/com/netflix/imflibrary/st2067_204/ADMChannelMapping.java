package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ADMChannelMapping structural metadata set defined in ST 2131
 */
@Immutable
public final class ADMChannelMapping extends InterchangeObject {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ADMChannelMapping.class.getSimpleName() + " : ";
    private final ADMChannelMappingBO admChannelMappingBO;

    /**
     * Constructor for an ADMChannelMapping object
     * @param admChannelMappingBO the parsed ADMChannelMapping object
     */
    public ADMChannelMapping(ADMChannelMappingBO admChannelMappingBO)
    {
        super();
        this.admChannelMappingBO = admChannelMappingBO;
    }
    /**
     * A method that returns a string representation of an ADMChannelMapping object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.admChannelMappingBO.toString();
    }

    /**
     * Object corresponding to a parsed ADMChannelMapping structural metadata set defined in st2131
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class ADMChannelMappingBO extends InterchangeObject.InterchangeObjectBO
    {
        @MXFProperty(size=4) protected final Long local_channel_id = null; //UINT32
        @MXFProperty(size=0, charset="UTF-16") protected final String adm_audio_track_uid = null; //UTF16String
        @MXFProperty(size=0, charset="UTF-16") protected final String adm_audio_track_channel_format_uid = null; //UTF16String
        @MXFProperty(size=0, charset="UTF-16") protected final String adm_audio_pack_format_uid = null; //UTF16String

        /**
         * Instantiates a new parsed ADMChannelMapping object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ADMChannelMappingBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.local_channel_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADMChannelMapping.ERROR_DESCRIPTION_PREFIX + "LocalChannelID is null");
            }
            if (this.adm_audio_track_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADMChannelMapping.ERROR_DESCRIPTION_PREFIX + "ADMAudioTrackUID is null");
            }
            if (this.adm_audio_track_channel_format_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADMChannelMapping.ERROR_DESCRIPTION_PREFIX + "NumADMAudioTrackUIDs is null");
            }
        }

        /**
         * A method that returns a string representation of the object.
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (this.local_channel_id != null)
            {
                sb.append(String.format("LocalChannelID = %d%n", this.local_channel_id));
            }
            if (this.adm_audio_track_uid != null)
            {
                sb.append(String.format("NumADMAudioTrackUIDs = %s%n", this.adm_audio_track_uid));
            }
            if (this.adm_audio_track_channel_format_uid != null)
            {
                sb.append(String.format("ADMChannelMappingsArray = %s%n", this.adm_audio_track_channel_format_uid));
            }
            if (this.adm_audio_pack_format_uid != null)
            {
                sb.append(String.format("ADMAudioPackFormatID = %s%n", this.adm_audio_pack_format_uid));
            }

            return sb.toString();
        }
    }
}
