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

package com.netflix.imflibrary.st0377;

import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.KLVPacket;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Arrays;

/**
 * Object model corresponding to a Primer Pack defined in st377-1:2011
 */
@Immutable
public final class PrimerPack
{
    private static final byte[] KEY      = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x00, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x05, 0x01, 0x00};
    private static final byte[] KEY_MASK = {   1,    1,     1,    1,    1,    1,   1,    0,    1,    1,    1,     1,    1,   1,    1,    1};

    private final KLVPacket.Header header;
    private final LocalTagEntryBatch localTagEntryBatch;

    /**
     * Instantiates a new Primer pack.
     *
     * @param byteProvider the mxf byte provider
     * @throws IOException the iO exception
     */
    PrimerPack(ByteProvider byteProvider, long byteOffset) throws IOException
    {
        this.header = new KLVPacket.Header(byteProvider, byteOffset);
        if(!PrimerPack.isValidKey(Arrays.copyOf(this.header.getKey(), this.header.getKey().length)))
        {
            throw new MXFException("Found invalid PrimerPack key");
        }
        this.localTagEntryBatch = new LocalTagEntryBatch(byteProvider);
    }

    /**
     * Instantiates a new Primer pack.
     *
     * @param byteProvider the mxf byte provider
     * @param header the mxf klv packet header
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    PrimerPack(ByteProvider byteProvider, KLVPacket.Header header) throws IOException
    {
        this.header = header;
        this.localTagEntryBatch = new LocalTagEntryBatch(byteProvider);
    }

    /**
     * Getter for the PrimerPack's MXFKLV Header
     *
     * @return the MXFKLV Header
     */
    public KLVPacket.Header getHeader(){
        return this.header;
    }

    /**
     * Checks if the key that was passed in corresponds to a Primer Pack
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isValidKey(byte[] key)
    {
        for (int i=0; i< KLVPacket.KEY_FIELD_SIZE; i++)
        {
            if((PrimerPack.KEY_MASK[i] != 0) && (PrimerPack.KEY[i] != key[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for the batch of local tag to UL mappings
     *
     * @return the local tag entry batch
     */
    public LocalTagEntryBatch getLocalTagEntryBatch()
    {
        return this.localTagEntryBatch;
    }

    /**
     * A method that returns a string representation of a Primer Pack object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("================== PrimerPack ======================\n");
        sb.append(this.header.toString());
        sb.append(this.localTagEntryBatch.toString());

        return sb.toString();
    }

}
