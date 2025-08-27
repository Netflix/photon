/*
 *
 *  * Copyright 2015 Netflix, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *         http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.netflix.imflibrary.utils;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This class is an implementation of {@link com.netflix.imflibrary.utils.ResourceByteRangeProvider} - the underlying
 * resource is a path. Unless the underlying path is changed externally, this can be considered to be an immutable
 * implementation
 */
@Immutable
public final class FileByteRangeProvider implements ResourceByteRangeProvider
{
    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 1024;

    private final Path resourcePath;
    private final long fileSize;
    private final SeekableByteChannel inputChannel;


    /**
     * Constructor for a FileByteRangeProvider
     * @param resourcePath whose data will be read by this data provider
     */
    public FileByteRangeProvider(Path resourcePath) throws IOException {
        this.resourcePath = resourcePath;
        this.fileSize = Files.size(resourcePath);
        this.inputChannel = Files.newByteChannel(resourcePath, StandardOpenOption.READ);
    }

    /**
     * A method that returns the size in bytes of the underlying resource, in this case a File
     * @return the size in bytes of the underlying resource, in this case a File
     */
    public long getResourceSize()
    {
        return this.fileSize;
    }

    /**
     * A method to obtain bytes in the inclusive range [start, endOfFile] as a path
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range from rangeStart through end of file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public Path getByteRange(long rangeStart, Path workingDirectory) throws IOException
    {
        return this.getByteRange(rangeStart, this.fileSize - 1, workingDirectory);
    }

    /**
     * A method to obtain bytes in the inclusive range [start, end] as a path
     *
     * @param rangeStart zero indexed inclusive start offset; range from [0, (resourceSize -1)] inclusive
     * @param rangeEnd zero indexed inclusive end offset; range from [0, (resourceSize -1)] inclusive
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public synchronized Path getByteRange(long rangeStart, long rangeEnd, Path workingDirectory) throws IOException
    {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        try {
            ResourceByteRangeProvider.Utilities.validateRangeRequest(this.fileSize, rangeStart, rangeEnd);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid range request: " + e.getMessage(), e);
        }

        Path rangeFilePath = workingDirectory.resolve("range");

        try (SeekableByteChannel outputChannel = Files.newByteChannel(rangeFilePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            try {
                inputChannel.position(rangeStart);
            } catch (IOException e) {
                throw new IOException("Failed to set position in input channel: " + e.getMessage(), e);
            }

            long bytesToRead = rangeEnd - rangeStart + 1;
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            try {
                while (bytesToRead > 0) {
                    buffer.clear();
                    int numBytesToRead = (int) Math.min(buffer.capacity(), bytesToRead);
                    buffer.limit(numBytesToRead);

                    int numBytesRead = inputChannel.read(buffer);
                    if (numBytesRead == -1) {
                        throw new IOException("Unexpected end of stream");
                    }

                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        outputChannel.write(buffer);
                    }

                    bytesToRead -= numBytesRead;
                }
            } catch (IOException e) {
                throw new IOException("Error reading bytes from input channel: " + e.getMessage(), e);
            }
        }

        return rangeFilePath;
    }

    /**
     * This method provides a way to obtain a byte range from the resource in-memory. A limitation of this method is
     * that the total size of the byte range request is capped at 0x7fffffff (the maximum value possible for type int
     * in java)
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param rangeEnd zero indexed inclusive end offset; ranges from 0 through (resourceSize -1) both included
     * @return byte[] containing desired byte range
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public synchronized byte[] getByteRangeAsBytes(long rangeStart, long rangeEnd) throws IOException {
        // Validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        try {
            ResourceByteRangeProvider.Utilities.validateRangeRequest(this.fileSize, rangeStart, rangeEnd);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid range request: " + e.getMessage(), e);
        }

        if ((rangeEnd - rangeStart + 1) > Integer.MAX_VALUE) {
            throw new IOException(String.format("Number of bytes requested = %d is greater than %d", (rangeEnd - rangeStart + 1), Integer.MAX_VALUE));
        }

        int bytesToRead = (int) (rangeEnd - rangeStart + 1);

        try {
            inputChannel.position(rangeStart);
        } catch (IOException e) {
            throw new IOException("Failed to set position in input channel: " + e.getMessage(), e);
        }


        ByteBuffer buffer = ByteBuffer.allocate(bytesToRead);

        try {
            while (bytesToRead > 0) {
                int numBytesRead = inputChannel.read(buffer);
                if (numBytesRead == -1) {
                    throw new IOException("Unexpected end of stream while reading bytes.");
                }
                bytesToRead -= numBytesRead;
            }
        } catch (IOException e) {
            throw new IOException("Error reading bytes from input channel: " + e.getMessage(), e);
        }

        buffer.flip();
        return buffer.array();
    }

    public SeekableByteChannel getByteRangeAsStream(long rangeStart, long rangeEnd) throws IOException {
        Path tempDir = Files.createTempDirectory(null);
        Path tempFile = this.getByteRange(rangeStart, rangeEnd, tempDir);

        // Open the file as a SeekableByteChannel for reading
        return Files.newByteChannel(tempFile, StandardOpenOption.READ);
    }
}
