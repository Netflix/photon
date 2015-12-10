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
import java.io.*;

/**
 * This class is an implementation of {@link com.netflix.imflibrary.utils.ResourceByteRangeProvider} - the underlying
 * resource is a file. Unless the underlying file is changed externally, this can be considered to be an immutable
 * implementation
 */
@Immutable
public final class FileByteRangeProvider implements ResourceByteRangeProvider
{
    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 1024;

    private final File resourceFile;
    private final long fileSize;

    /**
     * Constructor for a FileByteRangeProvider
     * @param resourceFile whose data will be read by this data provider
     */
    public FileByteRangeProvider(File resourceFile)
    {
        this.resourceFile = resourceFile;
        this.fileSize = this.resourceFile.length();
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
     * A method to obtain bytes in the inclusive range [start, endOfFile] as a file
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range from rangeStart through end of file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getByteRange(long rangeStart, File workingDirectory) throws IOException
    {
        return this.getByteRange(rangeStart, this.fileSize - 1, workingDirectory);
    }

    /**
     * A method to obtain bytes in the inclusive range [start, end] as a file
     *
     * @param rangeStart zero indexed inclusive start offset; range from [0, (resourceSize -1)] inclusive
     * @param rangeEnd zero indexed inclusive end offset; range from [0, (resourceSize -1)] inclusive
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getByteRange(long rangeStart, long rangeEnd, File workingDirectory) throws IOException
    {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        ResourceByteRangeProvider.Utilities.validateRangeRequest(this.fileSize, rangeStart, rangeEnd);

        File rangeFile = new File(workingDirectory, "range");

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.resourceFile));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(rangeFile)))
        {
            long numBytesSkipped = 0;
            while (numBytesSkipped < rangeStart)
            {
                numBytesSkipped += bis.skip(rangeStart - numBytesSkipped);
            }

            long totalNumberOfBytesRead = 0;
            byte[] bytes = new byte[BUFFER_SIZE];
            while (totalNumberOfBytesRead < (rangeEnd - rangeStart + 1))
            {
                int numBytesToRead = (int)Math.min(BUFFER_SIZE, rangeEnd - rangeStart + 1 - totalNumberOfBytesRead);
                int numBytesRead = bis.read(bytes, 0, numBytesToRead);
                if (numBytesRead == EOF)
                {
                    throw new EOFException();
                }
                bos.write(bytes, 0, numBytesRead);
                totalNumberOfBytesRead += numBytesRead;
            }
        }

        return rangeFile;
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
    public byte[] getByteRangeAsBytes(long rangeStart, long rangeEnd) throws IOException
    {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        ResourceByteRangeProvider.Utilities.validateRangeRequest(this.fileSize, rangeStart, rangeEnd);
        if((rangeEnd - rangeStart + 1) > Integer.MAX_VALUE){
                throw new IOException(String.format("Number of bytes requested = %d is greater than %d", (rangeEnd - rangeStart + 1), Integer.MAX_VALUE));
        }

        int totalNumBytesToRead = (int)(rangeEnd - rangeStart + 1);
        byte[] bytes = new byte[totalNumBytesToRead];
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.resourceFile)))
        {
            long bytesSkipped = bis.skip(rangeStart);
            if(bytesSkipped != rangeStart){
                throw new IOException(String.format("Could not skip %d bytes of data, possible truncated data", rangeStart));
            }

            int totalNumBytesRead = 0;
            while (totalNumBytesRead < totalNumBytesToRead)
            {
                int numBytesRead;
                numBytesRead = bis.read(bytes, totalNumBytesRead, totalNumBytesToRead - totalNumBytesRead);
                if (numBytesRead != -1)
                {
                    totalNumBytesRead += numBytesRead;
                }
                else
                {
                    throw new EOFException(String.format("Tried to read %d bytes from input stream, which ended after reading %d bytes",
                            totalNumBytesToRead, totalNumBytesRead));
                }

            }
        }

        return bytes;
    }
}
