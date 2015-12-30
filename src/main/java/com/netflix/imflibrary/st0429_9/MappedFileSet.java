package com.netflix.imflibrary.st0429_9;

import com.netflix.imflibrary.exceptions.IMFException;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * User: rpuri@netflix.com
 * Date: 12/29/15
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
public final class MappedFileSet
{

    private static final String ASSETMAP_FILE_NAME = "ASSETMAP.xml";
    private final AssetMap assetMap;
    private final URI assetMapURI;

    public MappedFileSet(File rootFile) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {
        if (!rootFile.isDirectory())
        {
            throw new IMFException(String.format("Root file %s corresponding to the mapped file set is not a directory", rootFile.getAbsolutePath()));
        }

        FilenameFilter filenameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File rootFile, String name)
            {
                return name.equals(MappedFileSet.ASSETMAP_FILE_NAME);
            }
        };

        File[] files = rootFile.listFiles(filenameFilter);
        if ((files == null) || (files.length != 1))
        {
            throw new IMFException(String.format("Found %d files with name %s in mapped file set rooted at %s, " +
                    "exactly 1 is allowed", (files == null) ? 0 : files.length, MappedFileSet.ASSETMAP_FILE_NAME, rootFile.getAbsolutePath()));
        }

        this.assetMap = new AssetMap(files[0]);
        this.assetMapURI = files[0].toURI();
    }

    public AssetMap getAssetMap()
    {
        return this.assetMap;
    }

    public URI getAssetMapURI()
    {
        return this.assetMapURI;
    }
}
