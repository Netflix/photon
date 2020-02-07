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
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
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

    /**
     * Instantiates a new TimelineTrack object
     *
     * @param timelineTrackBO the parsed TimelineTrack object
     * @param sequence the sequence referred by this TimelineTrack object
     */
    public TimelineTrack(TimelineTrackBO timelineTrackBO, Sequence sequence)
    {
        super(timelineTrackBO, sequence);
        this.timelineTrackBO = timelineTrackBO;
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
     * Getter for the Origin property of this TimelineTrack
     * @return a long integer representing the origin property of this timeline track
     */
    public long getOrigin(){
        return this.timelineTrackBO.origin;
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
        @MXFProperty(size=0) private final CompoundDataTypes.Rational edit_rate = null;
        @MXFProperty(size=8) private final Long origin = null; //Position type

        /**
         * Instantiates a new parsed TimelineTrack object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public TimelineTrackBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header, imfErrorLogger);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            postPopulateCheck();

            if (this.edit_rate == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        TimelineTrack.ERROR_DESCRIPTION_PREFIX + "edit_rate is null");
            }

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
            sb.append("================== EditRate ======================\n");
            sb.append(this.edit_rate.toString());
            sb.append(String.format("origin = %d%n", this.origin));
            return sb.toString();
        }
    }
}
