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
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

/**
 * Object model corresponding to GenericSoundEssenceDescriptor structural metadata set defined in st377-1:2011
 */
public abstract class GenericSoundEssenceDescriptor extends FileDescriptor{

    public enum ElectroSpatialFormulation {
        TWO_CHANNEL_MODE_DEFAULT(0),
        TWO_CHANNEL_MODE(1),
        SINGLE_CHANNEL_MODE(2),
        PRIMARY_SECONDARY_MODE(3),
        STEREOPHONIC_MODE(4),
        SINGLE_CHANNEL_DOUBLE_FREQUENCY_MODE(7),
        STEREO_LEFT_CHANNEL_DOUBLE_FREQUENCY_MODE(8),
        STEREO_RIGHT_CHANNEL_DOUBLE_FREQUENCY_MODE(9),
        MULTI_CHANNEL_MODE(15);

        private final Short mode;

        ElectroSpatialFormulation(int mode) {
            this.mode = (short)mode;
        }

        public Short value() {
            return mode;
        }
    }

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + GenericSoundEssenceDescriptor.class.getSimpleName() + " : ";
    protected GenericSoundEssenceDescriptorBO genericSoundEssenceDescriptorBO;

    /**
     * Getter for the audio sampling rate numerator of this WaveAudioEssenceDescriptor
     *
     * @return audio sampling rate numerator in the inclusive range [1, Integer.MAX_VALUE]
     * @throws MXFException when audio sampling rate numerator is out of range
     */
    public int getAudioSamplingRateNumerator() throws MXFException
    {
        long value = this.genericSoundEssenceDescriptorBO.audio_sampling_rate.getNumerator();
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
        long value = this.genericSoundEssenceDescriptorBO.audio_sampling_rate.getDenominator();
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
        long value = this.genericSoundEssenceDescriptorBO.channelcount;
        if ((value <0) || (value > Integer.MAX_VALUE))
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
        long value = this.genericSoundEssenceDescriptorBO.quantization_bits;
        if ((value <=0) || (value > Integer.MAX_VALUE))
        {
            throw new MXFException(String.format("Observed quantization bits = %d, which is not supported at this time", value));
        }
        return (int)value;
    }

    /**
     * Getter for Codec UL (can be null)
     * @return Codec UL (can be null)
     */
    public UL getCodec() {
        return this.genericSoundEssenceDescriptorBO.getCodecUL();
    }

    /**
     * Getter for Sound Essence Coding UL (can be null)
     * @return Sound Essence Coding UL (can be null)
     */
    public UL getSoundEssenceCoding() {
        return this.genericSoundEssenceDescriptorBO.getSoundEssenceCodingUL();
    }

    /**
     * Getter for Electro-Spatial Formulation (can be null)
     * @return Electro-Spatial Formulation (can be null)
     */
    public ElectroSpatialFormulation getElectroSpatialFormulation() {
        return this.genericSoundEssenceDescriptorBO.getElectroSpatialFormulation();
    }

    /**
     * Getter for Reference Audio Alignment Level (can be null)
     * @return Reference Audio Alignment Level (can be null)
     */
    public Short getReferenceAudioAlignmentLevel() {
        return this.genericSoundEssenceDescriptorBO.getReferenceAudioAlignmentLevel();
    }

    /**
     * Getter for the Reference Image Edit Rate (can be null)
     * @return Rational for Reference Image Edit Rate (can be null)
     */
    public CompoundDataTypes.Rational getReferenceImageEditRate() {
        return this.genericSoundEssenceDescriptorBO.getReferenceImageEditRate();
    }

    public static abstract class GenericSoundEssenceDescriptorBO extends FileDescriptorBO{

        @MXFProperty(size=0) protected final CompoundDataTypes.Rational audio_sampling_rate = null;
        @MXFProperty(size=4) protected final Long channelcount = null;
        @MXFProperty(size=4) protected final Long quantization_bits = null;
        @MXFProperty(size=16) protected final UL sound_essence_coding = null;
        @MXFProperty(size=8) protected final CompoundDataTypes.Rational reference_image_edit_rate = null;
        @MXFProperty(size=1) protected final Short reference_audio_alignment_level = null;
        @MXFProperty(size=1) protected  final ElectroSpatialFormulation electro_spatial_formulation = null;

        private final IMFErrorLogger imfErrorLogger;
        /**
         * Constructor for a File descriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        public GenericSoundEssenceDescriptorBO(final KLVPacket.Header header, IMFErrorLogger imfErrorLogger) {
            super(header);
            this.imfErrorLogger = imfErrorLogger;
        }

        public void postPopulateCheck() {
            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GenericSoundEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.audio_sampling_rate == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GenericSoundEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "audio_sampling_rate is null");
            }

            if (this.channelcount == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GenericSoundEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "channelcount is null");
            }

            if (this.quantization_bits == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GenericSoundEssenceDescriptor.ERROR_DESCRIPTION_PREFIX + "quantization_bits is null");
            }
        }

        /**
         * Accessor for the Essence Container UL of this FileDescriptor
         * @return a UL representing the Essence Container
         */
        public UL getSoundEssenceCodingUL(){
            return this.sound_essence_coding;
        }


        /**
         * Accessor for the Essence Container UL of this FileDescriptor
         * @return a UL representing the Essence Container
         */
        public ElectroSpatialFormulation getElectroSpatialFormulation(){
            return this.electro_spatial_formulation;
        }


        /**
         * Accessor for the Reference Audio Alignment Level of this Generic Sound Essence Descriptor
         * @return a Short representing the Reference Audio Alignment Level of this Generic Sound Essence Descriptor
         */
        public Short getReferenceAudioAlignmentLevel(){
            return this.reference_audio_alignment_level;
        }


        /**
         * Accessor for the Reference Image Edit Rate for this Generic Sound Essence Descriptor
         * @return a Rational representing the Reference Image Edit Rate for this Generic Sound Essence Descriptor
         */
        public CompoundDataTypes.Rational getReferenceImageEditRate(){
            return this.reference_image_edit_rate;
        }

        /**
         * A method that compares this GenericSoundEssenceDescriptorBO with the object that was passed in and returns true/false depending on whether the objects
         * match field for field.
         * Note: If the object passed in is not an instance of a GenericSoundEssenceDescriptorBO this method would return
         * false.
         * @param other the object that this parsed GenericSoundEssenceDescriptorBO should be compared with
         * @return result of comparing this parsed GenericSoundEssenceDescriptorBO with the object that was passed in
         */
        public boolean equals(Object other) {
            if(!(other instanceof GenericSoundEssenceDescriptorBO))
            {
                return false;
            }
            GenericSoundEssenceDescriptorBO otherObject = (GenericSoundEssenceDescriptorBO)other;

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

            return true;
        }

        /**
         * Getter for the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits
         * of this GenericSoundEssenceDescriptor
         * @return the sum of hash codes of AudioSamplingRate, Channel Count, Quantization Bits and Block Align fields
         * of this WaveAudioEssenceDescriptor
         */
        public int hashCode()
        {
            return this.audio_sampling_rate.hashCode() + this.channelcount.hashCode() + this.quantization_bits.hashCode();
        }

        /**
         * A method that returns a string representation of a GenericSoundEssenceDescriptorBO object
         * for use by derived classes
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("================== %s ======================%n", GenericSoundEssenceDescriptor.class.getSimpleName()));
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
            if (this.electro_spatial_formulation != null)
            {
                sb.append(String.format("electro_spatial_formulation = %s%n", this.electro_spatial_formulation.toString()));
            }
            return sb.toString();
        }
    }
}
