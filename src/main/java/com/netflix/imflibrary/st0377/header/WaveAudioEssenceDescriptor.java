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
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to WaveAudioEssenceDescriptor structural metadata set defined in st382:2007
 */
@Immutable
public final class WaveAudioEssenceDescriptor extends GenericSoundEssenceDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + WaveAudioEssenceDescriptor.class.getSimpleName() + " : ";

    public WaveAudioEssenceDescriptor(WaveAudioEssenceDescriptorBO waveAudioEssenceDescriptorBO)
    {
        this.genericSoundEssenceDescriptorBO = waveAudioEssenceDescriptorBO;
    }

    /**
     * Getter for the block align of this WaveAudioEssenceDescriptor
     *
     * @return block align in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when block align is out of range
     */
    public int getBlockAlign() throws MXFException
    {
        long value = ((WaveAudioEssenceDescriptorBO)this.genericSoundEssenceDescriptorBO).block_align;
        if ((value <=0) || (value > Integer.MAX_VALUE))
        {
            throw new MXFException(String.format("Observed block align = %d, which is not supported at this time", value));
        }
        return (int)value;
    }

    /**
     * Getter for channel assignment UL (can be null)
     * @return channel assignment UL (can be null)
     */
    public @Nullable
    MXFUID getChannelAssignmentUL()
    {
        if (((WaveAudioEssenceDescriptorBO)this.genericSoundEssenceDescriptorBO).channel_assignment != null)
        {
            return ((WaveAudioEssenceDescriptorBO)this.genericSoundEssenceDescriptorBO).channel_assignment.getULAsMXFUid();
        }
        return null;
    }

    /**
     * A method that compares this WaveAudioEssenceDescriptor with the object that was passed in and returns true/false depending on whether the objects
     * match field for field.
     * Note: If the object passed in is not an instance of a WaveAudioEssenceDescriptor this method would return
     * false.
     * @param other object that this WaveAudioEssenceDescriptor should be compared with
     * @return boolean indicating the result of comparing this WaveAudioEssenceDescriptor with the object that was passed in
     */
    public boolean equals(Object other)
    {
        if(!(other instanceof WaveAudioEssenceDescriptor))
        {
            return false;
        }

        WaveAudioEssenceDescriptor otherObject = (WaveAudioEssenceDescriptor)other;
        return ((WaveAudioEssenceDescriptorBO)this.genericSoundEssenceDescriptorBO).equals((otherObject.genericSoundEssenceDescriptorBO));
    }

    /**
     * Getter for the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields
     * of this WaveAudioEssenceDescriptor
     * @return the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields of this
     * WaveAudioEssenceDescriptor
     */
    public int hashCode()
    {
        return ((WaveAudioEssenceDescriptorBO)this.genericSoundEssenceDescriptorBO).hashCode();
    }

    /**
     * A method that returns a string representation of a WaveAudioEssenceDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return ((WaveAudioEssenceDescriptorBO)this.genericSoundEssenceDescriptorBO).toString();
    }

    /**
     * Object corresponding to a parsed WaveAudioEssenceDescriptor structural metadata set defined in st382:2007
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class WaveAudioEssenceDescriptorBO extends GenericSoundEssenceDescriptor.GenericSoundEssenceDescriptorBO
    {
        @MXFProperty(size=2) private final Integer block_align = null;
        @MXFProperty(size=4) private final Long average_bytes_per_second = null;
        @MXFProperty(size=16) private final UL channel_assignment = null;

        /**
         * Instantiates a new parsed WaveAudioEssenceDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public WaveAudioEssenceDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header, imfErrorLogger);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            postPopulateCheck();

            if (this.block_align == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        WaveAudioEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "block_align is null");
            }

        }

        /**
         * A method that compares this WaveAudioEssenceDescriptorBO with the object that was passed in and returns true/false depending on whether the objects
         * match field for field.
         * Note: If the object passed in is not an instance of a WaveAudioEssenceDescriptorBO this method would return
         * false.
         * @param other the object that this parsed WaveAudioEssenceDescriptor should be compared with
         * @return result of comparing this parsed WaveAudioEssenceDescriptor with the object that was passed in
         */
        public boolean equals(Object other)
        {
            if(!(other instanceof WaveAudioEssenceDescriptorBO))
            {
                return false;
            }

            boolean genericEqual = super.equals((GenericSoundEssenceDescriptorBO)other);
            if (!genericEqual) return false;

            WaveAudioEssenceDescriptorBO otherObject = (WaveAudioEssenceDescriptorBO)other;
            return !((this.block_align == null) || (!this.block_align.equals(otherObject.block_align)));

        }

        /**
         * Getter for the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields
         * of this WaveAudioEssenceDescriptor
         * @return the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields
         * of this WaveAudioEssenceDescriptor
         */
        public int hashCode()
        {
            return super.hashCode() + this.block_align.hashCode();
        }

        /**
         * A method that returns a string representation of a WaveAudioEssenceDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(String.format("block_align = %d%n", this.block_align));
            sb.append(String.format("average_bytes_per_second = %d%n", this.average_bytes_per_second));
            if (this.channel_assignment != null)
            {
                sb.append(String.format("channel_assignment = %s%n", this.channel_assignment.toString()));
            }
            return sb.toString();
        }
    }
}
