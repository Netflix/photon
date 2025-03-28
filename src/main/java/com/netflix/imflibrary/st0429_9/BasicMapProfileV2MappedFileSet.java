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

package com.netflix.imflibrary.st0429_9;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an immutable implementation of the Mapped File Set concept defined in Section A.1 in Annex A of st0429-9:2014.
 * A BasicMapProfilev2MappedFileSet object can only be constructed if the constraints specified in Section A.1 in Annex A of st0429-9:2014 are
 * satisfied
 */
@Immutable
public final class BasicMapProfileV2MappedFileSet
{

    public static final String ASSETMAP_FILE_NAME = "ASSETMAP.xml";
    private final AssetMap assetMap;
    private final URI absoluteAssetMapURI;
    private final IMFErrorLogger imfErrorLogger;
    /**
     * Constructor for a MappedFileSet object from a path representing the root of a directory tree
     * @param rootFile the directory which serves as the tree root of the Mapped File Set
     * @throws IOException - forwarded from {@link AssetMap#AssetMap(java.nio.file.Path) AssetMap} constructor
     */
    public BasicMapProfileV2MappedFileSet(Path rootFile) throws IOException
    {
        imfErrorLogger = new IMFErrorLoggerImpl();
        if (!Files.isDirectory(rootFile))
        {
            String message = String.format("Root path %s corresponding to the mapped path set is not a " +
                    "directory", rootFile.toString());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, imfErrorLogger);
        }

        DirectoryStream.Filter<Path> pathFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) {
                Path filename = entry.getFileName();
                if (filename == null) return false;
                return filename.toString().equals(BasicMapProfileV2MappedFileSet.ASSETMAP_FILE_NAME);
            }
        };


        List<Path> fileList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootFile, pathFilter)) {
            for (Path entry : stream) {
                fileList.add(entry);
            }
        }

        if (fileList.size() != 1)
        {
            String message = String.format("Found %d files with name %s in mapped path set rooted at %s, " +
                    "exactly 1 is allowed", fileList.size(), BasicMapProfileV2MappedFileSet
                    .ASSETMAP_FILE_NAME, rootFile.toString());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, imfErrorLogger);
        }

        this.assetMap = new AssetMap(fileList.get(0));
        this.absoluteAssetMapURI = fileList.get(0).toUri();
    }

    /**
     * Getter for the {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object that represents the single AssetMap document
     * corresponding to this Mapped File Set
     * @return the AssetMap object
     */
    public AssetMap getAssetMap()
    {
        return this.assetMap;
    }

    /**
     * Getter for the absolute, hierarchical URI with a scheme equal to <tt>"file"</tt> URI corresponding to the {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap}
     * object associated with this Mapped File Set
     * @return file-based URI for the AssetMap object
     */
    public URI getAbsoluteAssetMapURI()
    {
        return this.absoluteAssetMapURI;
    }

    /**
     * Getter for the errors in Composition
     *
     * @return List of errors in Composition.
     */
    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }
}
