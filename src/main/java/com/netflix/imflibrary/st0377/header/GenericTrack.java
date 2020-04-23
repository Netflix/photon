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
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;

/**
 * Object model corresponding to GenericTrack structural metadata set defined in st377-1:2011
 */
public abstract class GenericTrack extends InterchangeObject
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + GenericTrack.class.getSimpleName() + " : ";
    private final Sequence sequence;
    private final GenericTrackBO genericTrackBO;

    public GenericTrack(GenericTrackBO genericTrackBO, Sequence sequence) {
        this.sequence = sequence;
        this.genericTrackBO = genericTrackBO;
    }

    /**
     * Getter for the instance UID of this TimelineTrack
     * @return the instance UID of this TimelineTrack
     */
    public MXFUID getInstanceUID()
    {
        return new MXFUID(this.genericTrackBO.instance_uid);
    }

    /**
     * Getter for the UID of the sequence descriptive metadata set referred by this Timeline Track
     * @return the UID of the sequence descriptive metadata set referred by this Timeline Track
     */
    public MXFUID getSequenceUID()
    {
        return this.genericTrackBO.sequence.getInstanceUID();
    }

    /**
     * Getter for the Sequence descriptive metadata set referred by this Timeline Track
     * @return the Sequence descriptive metadata set referred by this Timeline Track
     */
    public Sequence getSequence()
    {
        return this.sequence;
    }

    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public abstract static class GenericTrackBO extends InterchangeObjectBO
    {
        /**
         * The Track _ id.
         */
        @MXFProperty(size=4) protected final Long track_id = null;
        /**
         * The Track _ number.
         */
        @MXFProperty(size=4) protected final Long track_number = null;
        /**
         * The Sequence.
         */
        @MXFProperty(size=16, depends=true) protected final StrongRef sequence = null;

        private final IMFErrorLogger imfErrorLogger;

        /**
         * Instantiates a new Generic track ByteObject.
         *
         * @param header the header
         */
        GenericTrackBO(KLVPacket.Header header, IMFErrorLogger imfErrorLogger)
        {

            super(header);
            this.imfErrorLogger = imfErrorLogger;
        }

        /**
         * Getter for the UID of the sequence descriptive metadata set referred by this Track
         * @return the UID of the sequence descriptive metadata set referred by this Track
         */
        public MXFUID getSequenceUID()
        {
            return this.sequence.getInstanceUID();
        }

        public void postPopulateCheck() {
            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.sequence == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ERROR_DESCRIPTION_PREFIX + "sequence is null");
            }
        }

        /**
         * A method that returns a string representation of a Generic Track object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== GenericTrack ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("track_id = 0x%08x(%d)%n", this.track_id, this.track_id));
            sb.append(String.format("track_number = 0x%08x(%d)%n", this.track_number, this.track_number));
            sb.append(String.format("sequence = %s%n", this.sequence.toString()));
            return sb.toString();
        }

    }
}
