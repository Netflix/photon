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

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Arrays;

/**
 * This class is a non-thread-safe implementation of {@link com.netflix.imflibrary.utils.ByteProvider}. The underlying input
 * sequence of bytes is sourced from an array of bytes. While this implementation could be enhanced to make it thread-safe, it is
 * difficult to envision an application scenario where an input stream could be shared meaningfully among multiple callers
 */
@NotThreadSafe
public final class ByteArrayDataProvider implements ByteProvider {

    private final byte[] bytes;
    private int position = 0;


    /**
     * Instantiates a new MXF byte array data provider.
     *
     * @param bytes the input stream
     */
    public ByteArrayDataProvider(byte[] bytes)
    {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Getter for the raw bytes from the byte[] that this data provider encapsulates
     *
     * @param totalNumBytesToRead the total num bytes to read
     * @return byte[] containing next totalNumBytesToRead number of bytes
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public byte[] getBytes(int totalNumBytesToRead) throws IOException
    {
        if ((this.position + totalNumBytesToRead) > bytes.length)
        {
            throw new IOException(String.format("Cannot read %d bytes from zero-index position %d as total length = %d",
                    totalNumBytesToRead, this.position, bytes.length));
        }
        this.position += totalNumBytesToRead;
        return Arrays.copyOfRange(this.bytes, this.position - totalNumBytesToRead, this.position);
    }

    /**
     * A method that lets the caller skip bytes in the encapsulated byte[]
     *
     * @param totalNumBytesToSkip the total num bytes to skip from the current position
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public void skipBytes(long totalNumBytesToSkip) throws IOException
    {
        if ((this.position + totalNumBytesToSkip) > bytes.length)
        {
            throw new IOException(String.format("Cannot skip %d bytes from zero-index position %d as total length = %d",
                    totalNumBytesToSkip, this.position, bytes.length));
        }

        this.position += totalNumBytesToSkip;
    }
}
