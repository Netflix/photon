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

import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.annotations.MXFField;

/**
 * Object model corresponding to GenericTrack structural metadata set defined in st377-1:2011
 */
public abstract class GenericTrack extends InterchangeObject
{

    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public abstract static class GenericTrackBO extends InterchangeObjectBO
    {
        /**
         * The Track _ id.
         */
        @MXFField(size=4) protected final Long track_id = null;
        /**
         * The Track _ number.
         */
        @MXFField(size=4) protected final Long track_number = null;
        /**
         * The Sequence.
         */
        @MXFField(size=16, depends=true) protected final StrongRef sequence = null;

        /**
         * Instantiates a new Generic track ByteObject.
         *
         * @param header the header
         */
        GenericTrackBO(MXFKLVPacket.Header header)
        {
            super(header);
        }
    }
}
