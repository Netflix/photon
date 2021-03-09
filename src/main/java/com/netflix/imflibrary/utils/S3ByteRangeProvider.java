/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.imflibrary.utils;

import com.amazonaws.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class S3ByteRangeProvider implements ResourceByteRangeProvider {

    private final S3Locator locator;
    private final long resourceSize;

    public S3ByteRangeProvider(S3Locator locator) {
        this.locator = locator;
        this.resourceSize = locator.length();
    }

    @Override
    public long getResourceSize() {
        return resourceSize;
    }

    @Override
    public byte[] getByteRangeAsBytes(long rangeStart, long rangeEnd) throws IOException {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        ResourceByteRangeProvider.Utilities.validateRangeRequest(this.resourceSize, rangeStart, rangeEnd);
        if ((rangeEnd - rangeStart + 1) > Integer.MAX_VALUE) {
            throw new IOException(String.format("Number of bytes requested %d is too large.", (rangeEnd - rangeStart + 1)));
        }
        return locator.readBytes(rangeStart, rangeEnd);
    }

    @Override
    public InputStream getByteRangeAsStream(long rangeStart, long rangeEnd) throws IOException {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        ResourceByteRangeProvider.Utilities.validateRangeRequest(this.resourceSize, rangeStart, rangeEnd);
        return locator.getByteRangeAsStream(rangeStart, rangeEnd);
    }

    @Override
    public File getByteRange(long rangeStart, long rangeEnd, File workingDirectory) throws IOException {
        final File rangeFile = new File(workingDirectory, "range");
        try (InputStream is = getByteRangeAsStream(rangeStart, rangeEnd);
             FileOutputStream fos = new FileOutputStream(rangeFile)) {
            IOUtils.copy(is, fos);
            return rangeFile;
        }
    }
}
