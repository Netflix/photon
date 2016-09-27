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
    private final WaveAudioEssenceDescriptorBO waveAudioEssenceDescriptorBO;

    public WaveAudioEssenceDescriptor(WaveAudioEssenceDescriptorBO waveAudioEssenceDescriptorBO)
    {
        this.waveAudioEssenceDescriptorBO = waveAudioEssenceDescriptorBO;
    }

    /**
     * Getter for the audio sampling rate numerator of this WaveAudioEssenceDescriptor
     *
     * @return audio sampling rate numerator in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when audio sampling rate numerator is out of range
     */
    public int getAudioSamplingRateNumerator() throws MXFException
    {
        long value = this.waveAudioEssenceDescriptorBO.audio_sampling_rate.getNumerator();
        if ((value <=0) || (value > Integer.MAX_VALUE))
        {
            throw new MXFException(String.format("Observed audio sampling rate numerator = %d, which is not supported at this time", value));
        }
        return (int)value;
    }

    /**
     * Getter for the audio sampling rate denominator of this WaveAudioEssenceDescriptor
     *
     * @return audio sampling rate denominator in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when audio sampling rate denominator is out of range
     */
    public int getAudioSamplingRateDenominator() throws MXFException
    {
        long value = this.waveAudioEssenceDescriptorBO.audio_sampling_rate.getDenominator();
        if ((value <=0) || (value > Integer.MAX_VALUE))
        {
            throw new MXFException(String.format("Observed audio sampling rate denominator = %d, which is not supported at this time", value));
        }
        return (int)value;
    }

    /**
     * Getter for the channel count of this WaveAudioEssenceDescriptor
     *
     * @return channel count in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when channel count is out of range
     */
    public int getChannelCount() throws MXFException
    {
        long value = this.waveAudioEssenceDescriptorBO.channelcount;
        if ((value <=0) || (value > Integer.MAX_VALUE))
        {
            throw new MXFException(String.format("Observed channel count = %d, which is not supported at this time", value));
        }
        return (int)value;
    }

    /**
     * Getter for the quantization bits of this WaveAudioEssenceDescriptor
     *
     * @return quantization bits in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when quantization bits is out of range
     */
    public int getQuantizationBits() throws MXFException
    {
        long value = this.waveAudioEssenceDescriptorBO.quantization_bits;
        if ((value <=0) || (value > Integer.MAX_VALUE))
        {
            throw new MXFException(String.format("Observed quantization bits = %d, which is not supported at this time", value));
        }
        return (int)value;
    }

    /**
     * Getter for the block align of this WaveAudioEssenceDescriptor
     *
     * @return block align in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when block align is out of range
     */
    public int getBlockAlign() throws MXFException
    {
        long value = this.waveAudioEssenceDescriptorBO.block_align;
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
        if (this.waveAudioEssenceDescriptorBO.channel_assignment != null)
        {
            return this.waveAudioEssenceDescriptorBO.channel_assignment.getULAsMXFUid();
        }
        return null;
    }

    /**
     * Getter for the Essence Container UL of this FileDescriptor
     * @return a UL representing the Essence Container
     */
    public UL getEssenceContainerUL(){
        return this.waveAudioEssenceDescriptorBO.getEssenceContainerUL();
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
        return this.waveAudioEssenceDescriptorBO.equals(otherObject.waveAudioEssenceDescriptorBO);
    }

    /**
     * Getter for the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields
     * of this WaveAudioEssenceDescriptor
     * @return the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields of this
     * WaveAudioEssenceDescriptor
     */
    public int hashCode()
    {
        return this.waveAudioEssenceDescriptorBO.hashCode();
    }

    /**
     * A method that returns a string representation of a WaveAudioEssenceDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.waveAudioEssenceDescriptorBO.toString();
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
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        WaveAudioEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.audio_sampling_rate == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        WaveAudioEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "audio_sampling_rate is null");
            }

            if (this.channelcount == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        WaveAudioEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "channelcount is null");
            }

            if (this.quantization_bits == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        WaveAudioEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "quantization_bits is null");
            }

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

            WaveAudioEssenceDescriptorBO otherObject = (WaveAudioEssenceDescriptorBO)other;

            if ((this.audio_sampling_rate == null) || (!this.audio_sampling_rate.equals(otherObject.audio_sampling_rate)))
            {
                return false;
            }

            if ((this.channelcount== null) || (!this.channelcount.equals(otherObject.channelcount)))
            {
                return false;
            }

            if ((this.quantization_bits == null) || (!this.quantization_bits.equals(otherObject.quantization_bits)))
            {
                return false;
            }

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
            return this.audio_sampling_rate.hashCode() + this.channelcount.hashCode() + this.quantization_bits.hashCode()
                    + this.block_align.hashCode();
        }

        /**
         * A method that returns a string representation of a WaveAudioEssenceDescriptorBO object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== WaveAudioEssenceDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append("================== SampleRate ======================\n");
            sb.append(this.sample_rate.toString());
            sb.append(String.format("essence_container = %s%n", this.essence_container.toString()));
            sb.append("================== AudioSamplingRate ======================\n");
            sb.append(this.audio_sampling_rate.toString());
            sb.append(String.format("channelcount = %d%n", this.channelcount));
            sb.append(String.format("quantization_bits = %d%n", this.quantization_bits));
            if (this.sound_essence_coding != null)
            {
                sb.append(String.format("sound_essence_coding = %s%n", this.sound_essence_coding.toString()));
            }
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
