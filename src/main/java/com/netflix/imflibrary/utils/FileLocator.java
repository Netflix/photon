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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

/**
 * This interface is the supertype for classes representing an file locator
 */
public interface FileLocator
{

    public static FileLocator fromLocation(String location) {
        if (location.startsWith("s3://")) {
            return new S3FileLocator(location);
        }

        return new LocalFileLocator(location);
    }

    public static FileLocator fromLocation(FileLocator directoryLocator, String fileName) throws IOException {
        String directoryPath = directoryLocator.getAbsolutePath();
        if (directoryPath.charAt(directoryPath.length() - 1) != '/') {
            directoryPath += '/';
        }

        return FileLocator.fromLocation(directoryPath + fileName);
    }

    public static FileLocator fromLocation(URI location) {
        if (location.toString().startsWith("s3://")) {
            return new S3FileLocator(location);
        }

        return new LocalFileLocator(location);
    }

    /**
     * Tests whether the file denoted by this abstract pathname is a
     * directory.
     * @return <code>true</code> if and only if the file denoted by this
     *          abstract pathname exists <em>and</em> is a directory;
     *          <code>false</code> otherwise
    */
    public boolean isDirectory() throws IOException;

    public String getAbsolutePath() throws IOException;

    public FileLocator[] listFiles(FilenameFilter filenameFilter) throws IOException;

    public URI toURI() throws IOException;

    public boolean exists() throws IOException;

    public String getName() throws IOException;

    public String getPath() throws IOException;

    public ResourceByteRangeProvider getResourceByteRangeProvider();
}
