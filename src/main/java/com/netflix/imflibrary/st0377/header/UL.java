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

import com.netflix.imflibrary.MXFUid;

import java.util.Arrays;

/**
 * Object model corresponding to a Universal Label defined in st377-1:2011
 */
public class UL {

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
    public MXFUid getULAsMXFUid(){
        return new MXFUid(this.ul);
    }

    /**
     * Getter for the ul byte[] representing this UL
     * @return byte[] representation of a UL
     */
    public byte[] getULAsBytes(){
        return Arrays.copyOf(this.ul, this.ul.length);
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
}
