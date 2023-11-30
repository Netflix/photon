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
 * Object model corresponding to RIFFChunkReferencesSubDescriptor structural metadata set defined in ST 2131
 */
@Immutable
public final class RIFFChunkReferencesSubDescriptor extends SubDescriptor {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + RIFFChunkReferencesSubDescriptor.class.getSimpleName() + " : ";
    private final RIFFChunkReferencesSubDescriptorBO riffChunkReferencesSubDescriptorBO;

    /**
     * Constructor for an RIFFChunkReferencesSubDescriptor object
     * @param riffChunkReferencesSubDescriptorBO the parsed RIFFChunkReferencesSubDescriptor object
     */
    public RIFFChunkReferencesSubDescriptor(RIFFChunkReferencesSubDescriptorBO riffChunkReferencesSubDescriptorBO)
    {
        super();
        this.riffChunkReferencesSubDescriptorBO = riffChunkReferencesSubDescriptorBO;
    }
    /**
     * A method that returns a string representation of an RIFFChunkReferencesSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.riffChunkReferencesSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed RIFFChunkReferencesSubDescriptor structural metadata set defined in st2131
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class RIFFChunkReferencesSubDescriptorBO extends SubDescriptor.SubDescriptorBO
    {
        @MXFProperty(size=0) protected final CompoundDataTypes.MXFCollections.MXFCollection<Integer> riff_chunk_stream_ids_array = null; //UINT32

        /**
         * Instantiates a new parsed RIFFChunkReferencesSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public RIFFChunkReferencesSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.riff_chunk_stream_ids_array == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        RIFFChunkReferencesSubDescriptor.ERROR_DESCRIPTION_PREFIX + "RIFFChunkStreamIDsArray is null");
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
            if (this.riff_chunk_stream_ids_array != null)
            {
                sb.append(String.format("RIFFChunkStreamIDsArray = %s%n", this.riff_chunk_stream_ids_array.toString()));
            }

            return sb.toString();
        }
    }
}
