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

package com.netflix.imflibrary;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * An (immutable) implementation of an MXF UID. Per section 4.1 of st0377-1:2011, a UID is "a generic term which may be
 * used to refer to a UL, UUID, UMID etc."
 */
@Immutable
public final class MXFUID
{
    /**
     * from <a href="http://www.smpte-ra.org/mdd/RP224v12-pub-20120418.xls">SMPTE Labels Registry</a>
     */
    static final byte[] picture_essence_track = {0x06, 0x0E, 0x2B, 0x34, 0x04, 0x01, 0x01, 0x01, 0x01, 0x03, 0x02, 0x02, 0x01, 0x00, 0x00, 0x00};
    /**
     * The Sound _ essence _ track.
     */
    static final byte[] sound_essence_track   = {0x06, 0x0E, 0x2B, 0x34, 0x04, 0x01, 0x01, 0x01, 0x01, 0x03, 0x02, 0x02, 0x02, 0x00, 0x00, 0x00};
    /**
     * The Data _ essence _ track.
     */
    static final byte[] data_essence_track    = {0x06, 0x0E, 0x2B, 0x34, 0x04, 0x01, 0x01, 0x01, 0x01, 0x03, 0x02, 0x02, 0x03, 0x00, 0x00, 0x00};

    private final byte[] uid;

    /**
     * Instantiates a new MXF uid.
     *
     * @param uid the uid
     */
    public MXFUID(byte[] uid)
    {
        this.uid = Arrays.copyOf(uid, uid.length);
    }

    /**
     * Getter for uid as a byte[]
     *
     * @return the byte [ ]
     */
    public byte[] getUID()
    {
        return Arrays.copyOf(this.uid, this.uid.length);
    }

    /**
     * A method that compares 2 MXF UIDs.
     * Note: this method would return true if and only if the 2 MXF UIDs match. If the object
     * passed in is not a MXF UID type then this method would return false
     * @param other the object that needs to compared with this MXFUid object
     * @return result of the comparison
     */
    public boolean equals(Object other)
    {
        if ((other != null) && (other.getClass().equals(MXFUID.class)))
        {
            return Arrays.equals(this.uid, ((MXFUID)other).uid);
        }
        else
        {
            return false;
        }
    }

    /**
     * A method to generate hash code for this MXF uid
     * @return hash code corresponding to this MXFUid
     */
    public int hashCode()
    {
        return Arrays.hashCode(this.uid);
    }

    /**
     * A method that returns a string representation of a MXFUid object
     *
     * @return string representing the object
     */
    public String toString()
    {
        if (this.uid.length == 16)
        {
            return String.format("0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
                    this.uid[0], this.uid[1], this.uid[2], this.uid[3],
                    this.uid[4], this.uid[5], this.uid[6], this.uid[7],
                    this.uid[8], this.uid[9], this.uid[10], this.uid[11],
                    this.uid[12], this.uid[13], this.uid[14], this.uid[15]);
        }
        else if (this.uid.length == 32)
        {
            return String.format("0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
                    this.uid[0], this.uid[1], this.uid[2], this.uid[3],
                    this.uid[4], this.uid[5], this.uid[6], this.uid[7],
                    this.uid[8], this.uid[9], this.uid[10], this.uid[11],
                    this.uid[12], this.uid[13], this.uid[14], this.uid[15],
                    this.uid[16], this.uid[17], this.uid[18], this.uid[19],
                    this.uid[20], this.uid[21], this.uid[22], this.uid[23],
                    this.uid[24], this.uid[25], this.uid[26], this.uid[27],
                    this.uid[28], this.uid[29], this.uid[30], this.uid[31]);
        }
        else
        {
            return Arrays.toString(this.uid);
        }
    }

}
