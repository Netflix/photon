package com.netflix.imflibrary.st0429_9;

import com.netflix.imflibrary.exceptions.IMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * User: rpuri@netflix.com
 * Date: 12/28/15
 * Time: 10:41 AM
 * To change this template use File | Settings | File Templates.
 */
public final class BasicMapProfilev2FileSet
{
    private static final Logger logger = LoggerFactory.getLogger(BasicMapProfilev2FileSet.class);


    private static final String ASSETMAP_FILE_NAME = "ASSETMAP.xml";
    private final AssetMap assetMap;

    public BasicMapProfilev2FileSet(File rootFile) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
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
                return name.equals(BasicMapProfilev2FileSet.ASSETMAP_FILE_NAME);
            }
        };

        File[] files = rootFile.listFiles(filenameFilter);
        if ((files == null) || (files.length != 1))
        {
            throw new IMFException(String.format("Found %d files with name %s in mapped file set rooted at %s, " +
                    "exactly 1 is allowed", (files == null) ? 0 : files.length, BasicMapProfilev2FileSet.ASSETMAP_FILE_NAME, rootFile.getAbsolutePath()));
        }

        AssetMap assetMap = new AssetMap(files[0]);

        for (AssetMap.Asset asset : assetMap.getAssetList())
        {
            if (asset.getPath().isAbsolute())
            {
                throw new IMFException("");
            }
        }

        this.assetMap = assetMap;
    }

    public AssetMap getAssetMap()
    {
        return assetMap;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {
        File rootFile = new File(args[0]);

        BasicMapProfilev2FileSet basicMapProfilev2FileSet = new BasicMapProfilev2FileSet(rootFile);
        logger.warn(basicMapProfilev2FileSet.getAssetMap().toString());


    }

}
