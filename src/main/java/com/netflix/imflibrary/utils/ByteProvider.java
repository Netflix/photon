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

package com.netflix.imflibrary.utils;

import java.io.IOException;

/**
 * This interface is the supertype for classes representing an input sequence of bytes
 */
public interface ByteProvider
{

    /**
     * Getter for the raw bytes that this ByteProvider encapsulates
     *
     * @param totalNumBytesToRead the total num bytes to read
     * @return byte[] containing next totalNumBytesToRead number of bytes
     * @throws java.io.IOException the iO exception
     */
    public byte[] getBytes(int totalNumBytesToRead) throws IOException;

    /**
     * A method that lets the caller skip bytes in the encapsulated data
     *
     * @param totalNumBytesToSkip the total num bytes to skip from the current position
     * @throws java.io.IOException the iO exception
     */
    public void skipBytes(long totalNumBytesToSkip) throws IOException;

}
