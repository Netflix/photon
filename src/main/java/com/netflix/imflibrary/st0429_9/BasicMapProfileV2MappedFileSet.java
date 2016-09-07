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
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
     * Constructor for a MappedFileSet object from a file representing the root of a directory tree
     * @param rootFile the directory which serves as the tree root of the Mapped File Set
     * @throws IOException - forwarded from {@link AssetMap#AssetMap(java.io.File) AssetMap} constructor
     */
    public BasicMapProfileV2MappedFileSet(File rootFile) throws IOException
    {
        imfErrorLogger = new IMFErrorLoggerImpl();
        if (!rootFile.isDirectory())
        {
            String message = String.format("Root file %s corresponding to the mapped file set is not a " +
                    "directory", rootFile.getAbsolutePath());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, imfErrorLogger);
        }

        FilenameFilter filenameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File rootFile, String name)
            {
                return name.equals(BasicMapProfileV2MappedFileSet.ASSETMAP_FILE_NAME);
            }
        };

        File[] files = rootFile.listFiles(filenameFilter);
        if ((files == null) || (files.length != 1))
        {
            String message = String.format("Found %d files with name %s in mapped file set rooted at %s, " +
                    "exactly 1 is allowed", (files == null) ? 0 : files.length, BasicMapProfileV2MappedFileSet
                    .ASSETMAP_FILE_NAME, rootFile.getAbsolutePath());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors
                            .ErrorLevels.FATAL,
                    message);
            throw new IMFException(message, imfErrorLogger);
        }

        this.assetMap = new AssetMap(files[0]);
        this.absoluteAssetMapURI = files[0].toURI();
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
