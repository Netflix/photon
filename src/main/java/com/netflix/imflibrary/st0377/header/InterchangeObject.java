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
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Object model corresponding to InterchangeObject structural metadata defined in st377-1:2011
 */
public abstract class InterchangeObject
{
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public abstract static class InterchangeObjectBO
    {
        /**
         * The Header.
         */
        protected final KLVPacket.Header header;
        /**
         * The Instance _ uid.
         */
        @MXFProperty(size=16) protected final byte[] instance_uid = null;


        /**
         * Instantiates a new Interchange object ByteObject.
         *
         * @param header the header
         */
        protected InterchangeObjectBO(KLVPacket.Header header)
        {
            this.header = header;
        }

        /**
         * Gets header.
         *
         * @return the header
         */
        public KLVPacket.Header getHeader()
        {
            return this.header;
        }

        /**
         * Gets instance uID.
         *
         * @return the instance uID
         */
        public @Nullable
        MXFUID getInstanceUID()
        {
            if (this.instance_uid != null)
            {
                return new MXFUID(this.instance_uid);
            }

            return null;
        }

        /**
         * A logical representation of a Strong Reference
         */
        public static final class StrongRef{
            private final byte[] instance_uid;

            /**
             * Constructor for a StrongRef object
             * @param instance_uid that this Strong reference object represents
             */
            public StrongRef(byte[] instance_uid){
                this.instance_uid = Arrays.copyOf(instance_uid, instance_uid.length);
            }

            /**
             * Accessor for the underlying instance_uid
             * @return MXFUId type corresponding to the instance_uid that this Strong reference object represents
             */
            public MXFUID getInstanceUID(){
                return new MXFUID(this.instance_uid);
            }

            /**
             * toString() method
             * @return string representation of the StrongRef object
             */
            public String toString(){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(String.format("0x"));
                for(byte b : this.instance_uid) {
                    stringBuilder.append(String.format("%02x", b));
                }
                return stringBuilder.toString();
            }
        }
    }
}
