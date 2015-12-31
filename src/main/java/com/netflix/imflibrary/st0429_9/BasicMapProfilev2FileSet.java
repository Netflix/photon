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

import com.netflix.imflibrary.exceptions.IMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class represents an immutable implementation of 'Basic Map Profile v2' defined in Annex A of st0429-9:2014
 * limited to the case of a single Mapped File Set. A BasicMapProfilev2FileSet object can only be constructed if the constraints
 * specified in Section A.1 in Annex A of st0429-9:2014 are satisfied
 */
@Immutable
public final class BasicMapProfilev2FileSet
{
    private static final Logger logger = LoggerFactory.getLogger(BasicMapProfilev2FileSet.class);
    private final MappedFileSet mappedFileSet;

    /**
     * Constructor for a {@link com.netflix.imflibrary.st0429_9.BasicMapProfilev2FileSet BasicMapProfilev2FileSet} from a {@link com.netflix.imflibrary.st0429_9.MappedFileSet MappedFileSet} object. Construction
     * succeeds if the constraints specified in Section A.1 in Annex A of st0429-9:2014 are satisfied
     * @param mappedFileSet the Mapped File Set object corresponding to this object
     * @throws IOException - forwarded from {@link com.netflix.imflibrary.st0429_9.MappedFileSet#MappedFileSet(java.io.File) MappedFileSet} constructor
     * @throws SAXException - forwarded from {@link com.netflix.imflibrary.st0429_9.MappedFileSet#MappedFileSet(java.io.File) MappedFileSet} constructor
     * @throws JAXBException - forwarded from {@link com.netflix.imflibrary.st0429_9.MappedFileSet#MappedFileSet(java.io.File) MappedFileSet} constructor
     * @throws URISyntaxException - forwarded from {@link com.netflix.imflibrary.st0429_9.MappedFileSet#MappedFileSet(java.io.File) MappedFileSet} constructor
     */
    public BasicMapProfilev2FileSet(MappedFileSet mappedFileSet) throws IOException, SAXException, JAXBException, URISyntaxException
    {

        for (AssetMap.Asset asset : mappedFileSet.getAssetMap().getAssetList())
        {//per Section A.2 in Annex A of st0429-9:2014, each path element value shall be a relative path reference
            if (asset.getPath().isAbsolute())
            {//TODO: fix error message
                throw new IMFException("");
            }
        }

        this.mappedFileSet = mappedFileSet;
    }

    /**
     * Getter for the {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object corresponding to this object
     * @return the corresponding AssetMap object
     */
    public AssetMap getAssetMap()
    {
        return this.mappedFileSet.getAssetMap();
    }

    /**
     * Getter for the file-based URI corresponding to the {@link com.netflix.imflibrary.st0429_9.AssetMap AssetMap} object associated with
     * this Mapped File Set
     * @return file-based URI for the AssetMap object
     */
    public URI getAssetMapURI()
    {
        return this.mappedFileSet.getAssetMapURI();
    }

    public static void main(String[] args) throws IOException, SAXException, JAXBException, URISyntaxException
    {
        File rootFile = new File(args[0]);

        BasicMapProfilev2FileSet basicMapProfilev2FileSet = new BasicMapProfilev2FileSet(new MappedFileSet(rootFile));
        logger.warn(basicMapProfilev2FileSet.getAssetMap().toString());
    }

}
