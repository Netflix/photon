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
    private final MappedFileSet mappedFileSet;

    public BasicMapProfilev2FileSet(MappedFileSet mappedFileSet) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
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

    public AssetMap getAssetMap()
    {
        return this.mappedFileSet.getAssetMap();
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {
        File rootFile = new File(args[0]);

        BasicMapProfilev2FileSet basicMapProfilev2FileSet = new BasicMapProfilev2FileSet(new MappedFileSet(rootFile));
        logger.warn(basicMapProfilev2FileSet.getAssetMap().toString());
    }

}
