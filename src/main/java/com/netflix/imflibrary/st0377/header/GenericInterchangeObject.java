/*
 *
 * Copyright 2026 Netflix, Inc.
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

import java.util.Arrays;

import javax.annotation.concurrent.Immutable;

/**
 * Placeholder interchange object for structural metadata sets that are not yet
 * mapped to a concrete Java class (e.g. new sub-descriptor types from updated
 * SMPTE registers). Allows strong references (e.g. SubDescriptors) to resolve
 * so that regxml fragment building does not report STRONG_REFERENCE_NOT_FOUND.
 */
public final class GenericInterchangeObject extends InterchangeObject {

    /**
     * Byte-object for an unknown structural metadata set. Holds only the KLV header
     * and instance UID so the set can be registered in uidToBOs and referenced.
     */
    @Immutable
    public static final class GenericInterchangeObjectBO extends InterchangeObjectBO {

        private final byte[] instanceUid;

        /**
         * @param header the MXF KLV header
         * @param instanceUid the 16-byte instance_uid from the set
         */
        public GenericInterchangeObjectBO(KLVPacket.Header header, byte[] instanceUid) {
            super(header);
            this.instanceUid = Arrays.copyOf(instanceUid, instanceUid.length);
        }

        @Override
        public MXFUID getInstanceUID() {
            return new MXFUID(this.instanceUid);
        }
    }

    private final GenericInterchangeObjectBO genericInterchangeObjectBO;

    /**
     * @param genericInterchangeObjectBO the byte-object for this generic set
     */
    public GenericInterchangeObject(GenericInterchangeObjectBO genericInterchangeObjectBO) {
        // placeholder for unknown structural metadata sets; no parent constructor
        this.genericInterchangeObjectBO = genericInterchangeObjectBO;
    }
}
