/*
 *
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.MXFUid;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to TimelineTrack structural metadata set defined in st377-1:2011
 */
@Immutable
public final class TimelineTrack extends GenericTrack
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + TimelineTrack.class.getSimpleName() + " : ";
    private final TimelineTrackBO timelineTrackBO;
    private final Sequence sequence;

    /**
     * Instantiates a new TimelineTrack object
     *
     * @param timelineTrackBO the parsed TimelineTrack object
     * @param sequence the sequence referred by this TimelineTrack object
     */
    public TimelineTrack(TimelineTrackBO timelineTrackBO, Sequence sequence)
    {
        this.timelineTrackBO = timelineTrackBO;
        this.sequence = sequence;
    }

    /**
     * Getter for the instance UID of this TimelineTrack
     * @return the instance UID of this TimelineTrack
     */
    public MXFUid getInstanceUID()
    {
        return new MXFUid(this.timelineTrackBO.instance_uid);
    }

    /**
     * Getter for the UID of the sequence descriptive metadata set referred by this Timeline Track
     * @return the UID of the sequence descriptive metadata set referred by this Timeline Track
     */
    public MXFUid getSequenceUID()
    {
        return this.timelineTrackBO.sequence.getInstanceUID();
    }

    /**
     * Getter for the Sequence descriptive metadata set referred by this Timeline Track
     * @return the Sequence descriptive metadata set referred by this Timeline Track
     */
    public Sequence getSequence()
    {
        return this.sequence;
    }

    /**
     * Getter for the numerator of this Timeline Track's edit rate
     * @return the numerator of this Timeline Track's edit rate
     */
    public long getEditRateNumerator()
    {
        return this.timelineTrackBO.edit_rate.getNumerator();
    }

    /**
     * Getter for the denominator of this Timeline Track's edit rate
     * @return the denominator of this Timeline Track's edit rate
     */
    public long getEditRateDenominator()
    {
        return this.timelineTrackBO.edit_rate.getDenominator();
    }

    /**
     * A method that returns a string representation of a Timeline Track object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.timelineTrackBO.toString();
    }

    /**
     * Object corresponding to a parsed Timeline Track structural metadata set defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class TimelineTrackBO extends GenericTrackBO
    {
        @MXFField(size=0) private final CompoundDataTypes.Rational edit_rate = null;
        @MXFField(size=8) private final Long origin = null; //Position type

        /**
         * Instantiates a new parsed TimelineTrack object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public TimelineTrackBO(MXFKLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUid> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        TimelineTrack.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.sequence == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        TimelineTrack.ERROR_DESCRIPTION_PREFIX + "sequence is null");
            }

            if (this.edit_rate == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_FIELD_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NOT_FATAL,
                        TimelineTrack.ERROR_DESCRIPTION_PREFIX + "edit_rate is null");
            }

        }

        /**
         * Getter for the UID of the sequence descriptive metadata set referred by this Timeline Track
         * @return the UID of the sequence descriptive metadata set referred by this Timeline Track
         */
        public MXFUid getSequenceUID()
        {
            return this.sequence.getInstanceUID();
        }

        /**
         * A method that returns a string representation of a Timeline Track object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== TimelineTrack ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("track_id = 0x%08x(%d)%n", this.track_id, this.track_id));
            sb.append(String.format("track_number = 0x%08x(%d)%n", this.track_number, this.track_number));
            sb.append(String.format("sequence = %s%n", this.sequence.toString()));
            sb.append("================== EditRate ======================\n");
            sb.append(this.edit_rate.toString());
            sb.append(String.format("origin = %d%n", this.origin));
            return sb.toString();
        }
    }
}
