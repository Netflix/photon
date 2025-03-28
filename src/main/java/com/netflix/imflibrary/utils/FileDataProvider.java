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
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This class is an non-thread-safe implementation of {@link com.netflix.imflibrary.utils.ByteProvider}. The underlying input
 * sequence of bytes is sourced from a path. While this implementation could be enhanced to make it thread-safe, it is
 * difficult to envision an application scenario where an input stream could be shared meaningfully among multiple callers
 */
@NotThreadSafe
public final class FileDataProvider implements ByteProvider {

    private final Path inputPath;
    private Long position = 0L;
    private final SeekableByteChannel inputChannel;

    /**
     * Instantiates a new FileDataProvider object
     *
     * @param path the input path
     */
    public FileDataProvider(Path path) throws IOException
    {
        this.inputPath = path;
        this.inputChannel = Files.newByteChannel(path, StandardOpenOption.READ);
    }

    /**
     * Getter for the raw bytes from the encapsulated resource in this case a file
     *
     * @param totalNumBytesToRead the total num bytes to read from current position in the file
     * @return byte[] containing next totalNumBytesToRead
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public byte[] getBytes(int totalNumBytesToRead) throws IOException
    {
        if(totalNumBytesToRead < 0){
            throw new IOException(String.format("Cannot read %d bytes, should be non-negative and non-zero", totalNumBytesToRead));
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalNumBytesToRead);

        try {
            while (totalNumBytesToRead > 0) {
                int numBytesRead = inputChannel.read(buffer);
                if (numBytesRead == -1) {
                    throw new IOException("Unexpected end of stream while reading bytes.");
                }
                totalNumBytesToRead -= numBytesRead;
                this.position += numBytesRead;
            }
        } catch (IOException e) {
            throw new IOException("Error reading bytes from input channel: " + e.getMessage(), e);
        }

        buffer.flip();
        return buffer.array();
    }

    /**
     * A method that lets the caller skip bytes in the encapsulated resource in this case a file
     *
     * @param totalNumBytesToSkip the total num bytes to skip from the current position in the file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public void skipBytes(long totalNumBytesToSkip) throws IOException
    {
        if (this.position + totalNumBytesToSkip > this.inputChannel.size()) {
            throw new IOException("Target position exceeds input size.");
        }

        try {
            inputChannel.position(totalNumBytesToSkip);
        } catch (IOException e) {
            throw new IOException("Failed to set position in input channel: " + e.getMessage(), e);
        }

        this.position += totalNumBytesToSkip;
    }
}
