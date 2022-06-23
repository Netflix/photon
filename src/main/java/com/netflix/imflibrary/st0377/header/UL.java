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
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.exceptions.IMFException;

import java.util.Arrays;

/**
 * Object model corresponding to a Universal Label defined in st377-1:2011
 */
public class UL {

    private static final String UL_as_a_URN_PREFIX = "urn:smpte:ul:";

    private final byte[] ul;

    /**
     * Constructor for a UL
     * @param ul byte array corresponding to the Universal Label bytes
     */
    public UL(byte[] ul){
        this.ul = Arrays.copyOf(ul, ul.length);
    }

    /**
     * MXFUid representation of a UL
     * @return The UL represented as a MXFUId
     */
    public MXFUID getULAsMXFUid(){
        return new MXFUID(this.ul);
    }

    /**
     * Getter for the ul byte[] representing this UL
     * @return byte[] representation of a UL
     */
    public byte[] getULAsBytes(){
        return Arrays.copyOf(this.ul, this.ul.length);
    }

    /**
     * Returns the value of a byte of the UL
     * @param index Index of a byte within the UL, with 0 corresponding to the first byte
     * @return byte `index` of the UL
     */
    public byte getByte(int index){
        return this.ul[index];
    }

    /**
     * Getter for UL length
     * @return length of the UL in bytes
     */
    public int getLength(){
        return this.ul.length;
    }

    /**
     * toString() method
     * @return string representation of the UL object
     */
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("0x"));
        for(byte b : this.ul) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    /**
     * toStringBytes() method
     * @return string representation of the UL object with a "." separation between bytes
     */
    public String toStringBytes(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%02x", this.ul[0]));
        for(int i = 1; i < this.ul.length; i++) {
            stringBuilder.append(String.format(".%02x", this.ul[i]));
        }
        return stringBuilder.toString();
    }

    /**
     * A helper method to return the UUID without the "urn:uuid:" prefix
     * @param ULasURN a urn:uuid type
     * @return a UL without the "urn:smpte:ul:" prefix
     */
    public static UL fromULAsURNStringToUL(String ULasURN)
    {
        if (!ULasURN.startsWith(UL.UL_as_a_URN_PREFIX))
        {
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.UUID_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.NON_FATAL, String.format("Input UUID %s " +
                    "does not start with %s", ULasURN, UL
                    .UL_as_a_URN_PREFIX));
            throw new IMFException(String.format("Input UUID %s does not start with %s", ULasURN, UL
                    .UL_as_a_URN_PREFIX), imfErrorLogger);
        }
        String ulValue = ULasURN.split(UL.UL_as_a_URN_PREFIX)[1].replace(".", "");
        byte[] bytes = new byte[16];
        for( int i =0; i < 16; i++) {
            bytes[i] = (byte)Integer.parseInt(ulValue.substring(i*2, i*2+2), 16);
        }
        return new UL(bytes);
    }

    /**
     * Compares this UL to another UL, ignoring specific bytes based on a mask
     *
     * @param ul Other UL to compare
     * @param byteMask 16-bit mask, where octet[n] of the UL is ignored if bit[15 - n] is 0
     * @return true if the ULs are equal
     */
    public boolean equalsWithMask(UL ul, int byteMask) {

        for (int i = 0; i < 15; i++) {
            if ((byteMask & 0x8000) != 0 && this.ul[i] != ul.ul[i])
                return false;

            byteMask = byteMask << 1;
        }

        return true;
    }

    /**
     * A Java compliant implementation of the hashCode() method
     *
     * @return integer containing the hash code corresponding to this object
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(ul);
    }

    /**
     * Compares this object to the specified object
     *
     * @param  other the object to be compared
     *
     * @return  true if the objects are the same; false otherwise
     */
    public boolean equals(Object other) {
        if ((null == other) || (other.getClass() != this.getClass()))
            return false;
        UL id = (UL)other;
        return Arrays.equals(this.ul, id.getULAsBytes());
    }

}
