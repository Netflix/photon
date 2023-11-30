package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.SubDescriptor;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0377.header.InterchangeObject.InterchangeObjectBO.StrongRef;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ADM_CHNASubDescriptor structural metadata set defined in ST 2131
 */
@Immutable
public final class ADM_CHNASubDescriptor extends SubDescriptor {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ADM_CHNASubDescriptor.class.getSimpleName() + " : ";
    private final ADM_CHNASubDescriptorBO adm_CHNASubDescriptorBO;

    /**
     * Constructor for an ADM_CHNASubDescriptor object
     * @param adm_CHNASubDescriptorBO the parsed ADM_CHNASubDescriptor object
     */
    public ADM_CHNASubDescriptor(ADM_CHNASubDescriptorBO adm_CHNASubDescriptorBO)
    {
        super();
        this.adm_CHNASubDescriptorBO = adm_CHNASubDescriptorBO;
    }
    /**
     * A method that returns a string representation of an ADM_CHNASubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.adm_CHNASubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed ADM_CHNASubDescriptor structural metadata set defined in st2131
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class ADM_CHNASubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {
        @MXFProperty(size=2) protected final Short num_local_channels = null; //UINT16
        @MXFProperty(size=2) protected final Short num_adm_audio_track_uids = null; //UINT16
        @MXFProperty(size=0, depends = true) protected final CompoundDataTypes.MXFCollections.MXFCollection<StrongRef> adm_channel_mappings_array = null; //

        /**
         * Instantiates a new parsed ADM_CHNASubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ADM_CHNASubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.num_local_channels == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADM_CHNASubDescriptor.ERROR_DESCRIPTION_PREFIX + "NumLocalChannels is null");
            }
            if (this.num_adm_audio_track_uids == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADM_CHNASubDescriptor.ERROR_DESCRIPTION_PREFIX + "NumADMAudioTrackUIDs is null");
            }
            if (this.adm_channel_mappings_array == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADM_CHNASubDescriptor.ERROR_DESCRIPTION_PREFIX + "ADMChannelMappingsArray is null");
            }
        }

        /**
         * Accessor for the mga_link_id of this MGAAudioMetadataSubDescriptor
         * @return a byte array representing the mga_link_id for the MGAAudioMetadataSubDescriptor
         */
        public CompoundDataTypes.MXFCollections.MXFCollection<StrongRef> getADMChannelMappingsArray(){
            return this.adm_channel_mappings_array;
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
            if (this.num_local_channels != null)
            {
                sb.append(String.format("NumLocalChannels = %s%n", this.num_local_channels.toString()));
            }
            if (this.num_adm_audio_track_uids != null)
            {
                sb.append(String.format("NumADMAudioTrackUIDs = %s%n", this.num_adm_audio_track_uids.toString()));
            }
            if (this.adm_channel_mappings_array != null)
            {
                sb.append(String.format("ADMChannelMappingsArray = %s%n", this.adm_channel_mappings_array.toString()));
            }

            return sb.toString();
        }
    }
}
