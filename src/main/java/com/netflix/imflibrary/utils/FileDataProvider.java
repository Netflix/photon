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

/**
 * This class is an non-thread-safe implementation of {@link com.netflix.imflibrary.utils.ByteProvider}. The underlying input
 * sequence of bytes is sourced from a file. While this implementation could be enhanced to make it thread-safe, it is
 * difficult to envision an application scenario where an input stream could be shared meaningfully among multiple callers
 */
@NotThreadSafe
public final class FileDataProvider implements ByteProvider {

    private final File inputFile;
    private Long position = 0L;

    /**
     * Instantiates a new FileDataProvider object
     *
     * @param file the input file
     */
    public FileDataProvider(File file)
    {
        this.inputFile = file;
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
        byte[] bytes = new byte[totalNumBytesToRead];
        Integer bytesRead = 0;
        Integer totalBytesRead = 0;
        try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(this.inputFile))) {
            skipBytes(inputStream, this.position); //reach current position
            while (bytesRead != -1
                    && totalBytesRead < totalNumBytesToRead) {
                bytesRead = inputStream.read(bytes, totalBytesRead, totalNumBytesToRead - totalBytesRead);
                if (bytesRead != -1) {
                    totalBytesRead += bytesRead;
                }
            }
        }
        if(totalBytesRead < totalNumBytesToRead) {
            throw new IOException(String.format("Could not read %d bytes of data, only read %d bytes of data, possible truncated data", totalNumBytesToRead, totalBytesRead));
        }
        this.position += totalBytesRead;
        return bytes;
    }

    /**
     * A method that lets the caller skip bytes in the encapsulated resource in this case a file
     *
     * @param totalNumBytesToSkip the total num bytes to skip from the current position in the file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public void skipBytes(long totalNumBytesToSkip) throws IOException
    {
        try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(this.inputFile)))
        {
            skipBytes(inputStream, this.position + totalNumBytesToSkip);
            this.position += totalNumBytesToSkip;
        }
    }

    private void skipBytes(InputStream inputStream, long totalNumBytesToSkip) throws IOException
    {
        long bytesSkipped = 0;
        long totalBytesSkipped = 0;
        while ((bytesSkipped != -1) && (totalBytesSkipped < totalNumBytesToSkip))
        {
            bytesSkipped = inputStream.skip(totalNumBytesToSkip - totalBytesSkipped);
            {
                totalBytesSkipped += bytesSkipped;
            }
        }
        if(totalBytesSkipped != totalNumBytesToSkip){
            throw new IOException(String.format("Could not skip %d bytes of data, possible truncated data", totalNumBytesToSkip));
        }

    }
}
