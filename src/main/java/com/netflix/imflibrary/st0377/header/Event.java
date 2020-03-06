package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.annotations.MXFProperty;
import javax.annotation.concurrent.Immutable;

public class Event extends StructuralComponent {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + Event.class.getSimpleName() + " : ";
    private final EventBO eventBO;

    /**
     * Instantiates a new Event object
     *
     * @param eventBO the parsed Event object
     */
    public Event(EventBO eventBO)
    {
        this.eventBO = eventBO;
    }

    /**
     * Object corresponding to a parsed Event structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static class EventBO extends StructuralComponentBO
    {
        @MXFProperty(size=8) protected final Long event_start_position = null;
        @MXFProperty(size=0, charset = "UTF-16") protected final String event_comment = null; //UTF-16 String

        /**
         * Instantiates a new parsed Event object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         */
        public EventBO(KLVPacket.Header header) {
            super(header);
        }


        /**
         * A method that returns a string representation of a EventBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== Event ======================\n");
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
            return sb.toString();

        }
    }
}
