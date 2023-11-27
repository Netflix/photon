package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.SubDescriptor;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ADMAudioMetadataSubDescriptor structural metadata set defined in ST 2131
 */
@Immutable
public final class ADMAudioMetadataSubDescriptor extends SubDescriptor {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ADMAudioMetadataSubDescriptor.class.getSimpleName() + " : ";
    private final ADMAudioMetadataSubDescriptorBO admAudioMetadataSubDescriptorBO;

    /**
     * Constructor for an ADMAudioMetadataSubDescriptor object
     * @param admAudioMetadataSubDescriptorBO the parsed ADMAudioMetadataSubDescriptor object
     */
    public ADMAudioMetadataSubDescriptor(ADMAudioMetadataSubDescriptorBO admAudioMetadataSubDescriptorBO)
    {
        super();
        this.admAudioMetadataSubDescriptorBO = admAudioMetadataSubDescriptorBO;
    }
    /**
     * A method that returns a string representation of an ADMAudioMetadataSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.admAudioMetadataSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed ADMAudioMetadataSubDescriptor structural metadata set defined in st2131
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class ADMAudioMetadataSubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {
        @MXFProperty(size=4) protected final Long riff_chunk_stream_id_link1 = null; //UINT32
        @MXFProperty(size=0) private final CompoundDataTypes.MXFCollections.MXFCollection<UL> adm_profile_level_batch = null;

        /**
         * Instantiates a new parsed ADMAudioMetadataSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ADMAudioMetadataSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.riff_chunk_stream_id_link1 == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADMAudioMetadataSubDescriptor.ERROR_DESCRIPTION_PREFIX + "RIFFChunkStreamID_link1 is null");
            }
        }

        /**
         * Accessor for the riff_chunk_stream_id_link1 of this ADMAudioMetadataSubDescriptor
         * @return a byte array representing the riff_chunk_stream_id_link1 for the ADMAudioMetadataSubDescriptor
         */
        public Long getRIFFChunkStreamID_link1(){
            return this.riff_chunk_stream_id_link1;
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
            if (this.riff_chunk_stream_id_link1 != null)
            {
                sb.append(String.format("riff_chunk_stream_id_link1 = %d%n", this.riff_chunk_stream_id_link1));
            }

            return sb.toString();
        }
    }
}
