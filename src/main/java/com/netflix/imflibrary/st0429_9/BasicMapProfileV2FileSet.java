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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class represents an immutable implementation of 'Basic Map Profile v2' defined in Annex A of st0429-9:2014
 * limited to the case of a single Asset Map document in the root directory of the . A BasicMapProfilev2FileSet object can only be constructed if the constraints
 * specified in Section A.1 in Annex A of st0429-9:2014 are satisfied
 */
@Immutable
public final class BasicMapProfileV2FileSet
{
    private static final Logger logger = LoggerFactory.getLogger(BasicMapProfileV2FileSet.class);
    private final BasicMapProfileV2MappedFileSet basicMapProfileV2MappedFileSet;
    private final IMFErrorLogger imfErrorLogger;
    /**
     * Constructor for a {@link BasicMapProfileV2FileSet BasicMapProfilev2FileSet} from a {@link BasicMapProfileV2MappedFileSet MappedFileSet} object. Construction
     * succeeds if the constraints specified in Section A.1 in Annex A of st0429-9:2014 are satisfied
     * @param basicMapProfileV2MappedFileSet the Mapped File Set object corresponding to this object
     * @throws IOException - forwarded from {@link BasicMapProfileV2MappedFileSet#BasicMapProfileV2MappedFileSet(java.io.File) MappedFileSet} constructor
     * @throws SAXException - forwarded from {@link BasicMapProfileV2MappedFileSet#BasicMapProfileV2MappedFileSet(java.io.File) MappedFileSet} constructor
     * @throws JAXBException - forwarded from {@link BasicMapProfileV2MappedFileSet#BasicMapProfileV2MappedFileSet(java.io.File) MappedFileSet} constructor
     * @throws URISyntaxException - forwarded from {@link BasicMapProfileV2MappedFileSet#BasicMapProfileV2MappedFileSet(java.io.File) MappedFileSet} constructor
     */
    public BasicMapProfileV2FileSet(BasicMapProfileV2MappedFileSet basicMapProfileV2MappedFileSet) throws IOException, SAXException, JAXBException, URISyntaxException
    {
        imfErrorLogger = new IMFErrorLoggerImpl();

        for (AssetMap.Asset asset : basicMapProfileV2MappedFileSet.getAssetMap().getAssetList())
        {//per Section A.2 in Annex A of st0429-9:2014, each path element value shall be a relative path reference
            if (asset.getPath().isAbsolute())
            {
                String message = String.format("%s is an absolute URI", asset.getPath());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, message);
            }
        }

        this.basicMapProfileV2MappedFileSet = basicMapProfileV2MappedFileSet;

        if (imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException(String.format("Found %d errors in AssetMap XML file", imfErrorLogger
                    .getNumberOfErrors()), imfErrorLogger);
        }
    }

    /**
     * Getter for the {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object corresponding to this object
     * @return the corresponding AssetMap object
     */
    public AssetMap getAssetMap()
    {
        return this.basicMapProfileV2MappedFileSet.getAssetMap();
    }

    /**
     * Getter for the absolute, hierarchical URI with a scheme equal to <tt>"file"</tt> URI corresponding to the {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap}
     * object associated with this Mapped File Set
     * @return file-based URI for the AssetMap object
     */
    public URI getAbsoluteAssetMapURI()
    {
        return this.basicMapProfileV2MappedFileSet.getAbsoluteAssetMapURI();
    }

    public static void main(String[] args) throws IOException, SAXException, JAXBException, URISyntaxException
    {
        File rootFile = new File(args[0]);

        BasicMapProfileV2FileSet basicMapProfileV2FileSet = new BasicMapProfileV2FileSet(new BasicMapProfileV2MappedFileSet(rootFile));
        logger.warn(basicMapProfileV2FileSet.getAssetMap().toString());
    }

}
