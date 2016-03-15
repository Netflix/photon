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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This interface is a supertype of classes that represent resources to which byte range requests can be made
 */
public interface ResourceByteRangeProvider
{
    /**
     * A method that returns the size in bytes of the underlying resource
     * @return size of resource in bytes
     */
    long getResourceSize();

    /**
     * A method to obtain bytes in the inclusive range [start, end] as a file
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param rangeEnd zero indexed inclusive end offset; ranges from 0 through (resourceSize -1) both included
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    File getByteRange(long rangeStart, long rangeEnd, File workingDirectory) throws IOException;

    /**
     * A method to obtain bytes in the inclusive range [start, end] as a byte[]
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param rangeEnd zero indexed inclusive end offset; ranges from 0 through (resourceSize -1) both included
     * @return byte array containing desired byte range
     * @throws IOException
     */
    byte[] getByteRangeAsBytes(long rangeStart, long rangeEnd) throws IOException;

    /**
     * A method to obtain bytes in the inclusive range [start, end] as a byte[]
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param rangeEnd zero indexed inclusive end offset; ranges from 0 through (resourceSize -1) both included
     * @return inputStream corresponding to the desired byte range
     * @throws IOException
     */
    InputStream getByteRangeAsStream(long rangeStart, long rangeEnd) throws IOException;

    class Utilities
    {
        public static void validateRangeRequest(long resourceSize, long rangeStart, long rangeEnd)
        {
            if (rangeStart < 0)
            {
                throw new IllegalArgumentException(String.format("rangeStart = %d is < 0", rangeStart));
            }

            if (rangeStart > rangeEnd)
            {
                throw new IllegalArgumentException(String.format("rangeStart = %d is not <= %d rangeEnd", rangeStart, rangeEnd));
            }

            if (rangeEnd > (resourceSize - 1))
            {
                throw new IllegalArgumentException(String.format("rangeEnd = %d is not <= (resourceSize -1) = %d", rangeEnd, (resourceSize-1)));
            }
        }
    }


}
