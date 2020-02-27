package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

public class DescriptiveMarkerSegment extends Event {
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + DescriptiveMarkerSegment.class.getSimpleName() + " : ";
    private final DescriptiveMarkerSegmentBO descriptiveMarkerSegmentBO;
    private final DMFramework dmFramework;

    public DescriptiveMarkerSegment(DescriptiveMarkerSegmentBO descriptiveMarkerSegmentBO, DMFramework dmFramework) {
        super(descriptiveMarkerSegmentBO);
        this.descriptiveMarkerSegmentBO = descriptiveMarkerSegmentBO;
        this.dmFramework = dmFramework;
    }

    public DMFramework getDmFramework() {
        return dmFramework;
    }

    /**
     * Object corresponding to a parsed DescriptiveMarkerSegment structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class DescriptiveMarkerSegmentBO extends EventBO
    {
        @MXFProperty(size=16, depends=true) private final StrongRef dm_framework = null;

        /**
         * Instantiates a new parsed DescriptiveMarkerSegment object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public DescriptiveMarkerSegmentBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        DescriptiveMarkerSegment.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
            if (this.dm_framework == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        DescriptiveMarkerSegment.ERROR_DESCRIPTION_PREFIX + "dmframework is null");
            }
        }

        /**
         * A method that returns a string representation of a DescriptiveMarkerSegmentBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== DescriptiveMarkerSegment ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("duration = %d%n", this.duration));
            if (this.event_start_position != null) {
                sb.append(String.format("event_start_position = %d%n", this.event_start_position));
            }
            if (this.event_comment != null)
            {
                sb.append(String.format("event_comment = %s%n", this.event_comment));
            }
            sb.append(this.dm_framework.toString());
            return sb.toString();
        }
    }
}
