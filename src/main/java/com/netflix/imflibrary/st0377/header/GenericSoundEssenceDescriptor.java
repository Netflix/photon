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

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

/**
 * Object model corresponding to GenericSoundEssenceDescriptor structural metadata set defined in st377-1:2011
 */
public abstract class GenericSoundEssenceDescriptor extends FileDescriptor{

    public static abstract class GenericSoundEssenceDescriptorBO extends FileDescriptorBO{

        @MXFField(size=0) protected final CompoundDataTypes.Rational audio_sampling_rate = null;
        @MXFField(size=4) protected final Long channelcount = null;
        @MXFField(size=4) protected final Long quantization_bits = null;
        @MXFField(size=16) protected final UL sound_essence_coding = null;
        /**
         * Constructor for a File descriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        GenericSoundEssenceDescriptorBO(final KLVPacket.Header header) {
            super(header);
        }
    }
}
