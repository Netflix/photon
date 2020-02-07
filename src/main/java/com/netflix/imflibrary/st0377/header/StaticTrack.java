package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

public class StaticTrack extends GenericTrack {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + StaticTrack.class.getSimpleName() + " : ";
    private final StaticTrackBO staticTrackBO;


    /**
     * Instantiates a new StaticTrack object
     *
     * @param staticTrackBO the parsed StaticTrack object
     * @param sequence the sequence referred by this StaticTrack object
     */
    public StaticTrack(StaticTrackBO staticTrackBO, Sequence sequence)
    {
        super(staticTrackBO, sequence);
        this.staticTrackBO = staticTrackBO;
    }

    /**
     * Object corresponding to a parsed StaticTrack structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class StaticTrackBO extends GenericTrackBO
    {

        /**
         * Instantiates a new parsed StaticTrack object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public StaticTrackBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header, imfErrorLogger);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            postPopulateCheck();

        }

        /**
         * A method that returns a string representation of a Timeline Track object
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
