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
 * Object model corresponding to RIFFChunkDefinitionSubDescriptor structural metadata set defined in ST 2131
 */
@Immutable
public final class RIFFChunkDefinitionSubDescriptor extends SubDescriptor {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + RIFFChunkDefinitionSubDescriptor.class.getSimpleName() + " : ";
    private final RIFFChunkDefinitionSubDescriptorBO riffChunkDefinitionSubDescriptorBO;

    /**
     * Constructor for an RIFFChunkDefinitionSubDescriptor object
     * @param riffChunkDefinitionSubDescriptorBO the parsed RIFFChunkDefinitionSubDescriptor object
     */
    public RIFFChunkDefinitionSubDescriptor(RIFFChunkDefinitionSubDescriptorBO riffChunkDefinitionSubDescriptorBO)
    {
        super();
        this.riffChunkDefinitionSubDescriptorBO = riffChunkDefinitionSubDescriptorBO;
    }
    /**
     * A method that returns a string representation of an RIFFChunkDefinitionSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.riffChunkDefinitionSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed RIFFChunkDefinitionSubDescriptor structural metadata set defined in st2131
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class RIFFChunkDefinitionSubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {
        @MXFProperty(size=4) protected final Long riff_chunk_stream_id = null; //UINT32
        @MXFProperty(size=4) protected final byte[] riff_chunk_id = null; //ISO7
        @MXFProperty(size=16) protected final byte[] riff_chunk_uuid = null; //UUID
        @MXFProperty(size=20) protected final byte[] riff_chunk_sha1 = null; //DataValue, unstructured sequence of bytes

        /**
         * Instantiates a new parsed RIFFChunkDefinitionSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public RIFFChunkDefinitionSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.riff_chunk_stream_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        RIFFChunkDefinitionSubDescriptor.ERROR_DESCRIPTION_PREFIX + "RIFFChunkStreamID is null");
            }
            if (this.riff_chunk_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        RIFFChunkDefinitionSubDescriptor.ERROR_DESCRIPTION_PREFIX + "RIFFChunkID is null");
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
            if (this.riff_chunk_stream_id != null)
            {
                sb.append(String.format("RIFFChunkStreamID = %d%n", this.riff_chunk_stream_id));
            }

            return sb.toString();
        }
    }
}
