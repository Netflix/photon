/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Object model corresponding to FileDescriptor structural metadata set defined in st377-1:2011
 */
public abstract class FileDescriptor extends GenericDescriptor {

    public static abstract class FileDescriptorBO extends GenericDescriptorBO {

        @MXFField(size = 0)
        protected final CompoundDataTypes.Rational sample_rate = null;
        @MXFField(size = 16)
        protected final UL essence_container = null;

        /**
         * Constructor for a File descriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        FileDescriptorBO(final KLVPacket.Header header) {
            super(header);
        }

        /**
         * Accessor for the SampleRate field
         * @return returns a list of long integers representing the numerator and denominator of the sample rate in that order
         */
        public List<Long> getSampleRate(){
            List<Long> list = new ArrayList<>();
            Long numerator = this.sample_rate.getNumerator();
            list.add(numerator);
            Long denominator = this.sample_rate.getDenominator();
            list.add(denominator);
            return list;
        }
    }
}
