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
import com.netflix.imflibrary.MXFFieldPopulator;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Object model corresponding to a batch of LocalTags
 */
@Immutable
public final class LocalTagEntryBatch
{
    /**
     * The constant LOCAL_TAG_ENTRY_SIZE.
     */
    public static final int LOCAL_TAG_ENTRY_SIZE = 18;
    private final CompoundDataTypes.MXFCollections.Header header;
    private final Map<Integer, MXFUID> localTagToUID;

    /**
     * Instantiates a new Local tag entry batch.
     *
     * @param byteProvider the mxf byte provider
     * @throws IOException the iO exception
     */
    LocalTagEntryBatch(ByteProvider byteProvider) throws IOException
    {
        this.header = new CompoundDataTypes.MXFCollections.Header(byteProvider);
        if (this.header.getSizeOfElement() != LocalTagEntryBatch.LOCAL_TAG_ENTRY_SIZE)
        {
            throw new MXFException(String.format("Element size = %d in LocalTagEntryBatch header is different from expected size = %d",
                    this.header.getSizeOfElement(), LocalTagEntryBatch.LOCAL_TAG_ENTRY_SIZE));
        }

        this.localTagToUID = new HashMap<>();

        for (long i=0; i<this.header.getNumberOfElements(); i++)
        {
            int localTag = MXFFieldPopulator.getUnsignedShortAsInt(byteProvider.getBytes(2), KLVPacket.BYTE_ORDER);
            //smpte st 377-1:2011, section 9.2
            if (localTag == 0)
            {
                throw new MXFException(String.format("localTag = 0x%04x(%d) is not permitted", localTag, localTag));
            }
            //smpte st 377-1:2011, section 9.2
            if (localTagToUID.get(localTag) != null)
            {
                throw new MXFException(String.format("localTag = 0x%04x(%d) has already been observed", localTag, localTag));
            }

            MXFUID mxfUL = new MXFUID(byteProvider.getBytes(16));
            localTagToUID.put(localTag, mxfUL);
        }
    }

    /**
     * Getter for the unmodifiable local tag to UID map.
     *
     * @return the local tag to uID map
     */
    public Map getLocalTagToUIDMap()
    {
        return java.util.Collections.unmodifiableMap(localTagToUID);
    }

    /**
     * A method that returns a string representation of a LocalTagEntryBatch object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("================== LocalTagEntryBatch ======================\n");
        sb.append(this.header.toString());
        for (Map.Entry<Integer, MXFUID> entry : this.localTagToUID.entrySet())
        {
            int localTag = entry.getKey();
            byte[] bytes = entry.getValue().getUID();
            sb.append(String.format("localTag = 0x%04x UID = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n", localTag,
                    bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                    bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]));
        }
        return sb.toString();
    }

}
