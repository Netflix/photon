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
 * Object model corresponding to Structural Component structural metadata set defined in st377-1:2011
 */
public abstract class StructuralComponent extends InterchangeObject
{
    /**
     * Object corresponding to a parsed SourcePackage structural metadata set defined in st377-1:2011
     */
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public abstract static class StructuralComponentBO extends InterchangeObjectBO
    {
        /**
         * The Data _ definition.
         */
        @MXFField(size=16) protected final byte[] data_definition = null;
        /**
         * The Duration.
         */
        @MXFField(size=8) protected final Long duration = null;

        /**
         * Instantiates a new Structural component ByteObject.
         *
         * @param header the header
         */
        StructuralComponentBO(MXFKLVPacket.Header header)
        {
            super(header);
        }

        /**
         * Accessor for the duration
         * @return the duration of this structural component
         */
        public Long getDuration(){
            return this.duration;
        }
    }
}
