package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfilev2FileSet;
import com.netflix.imflibrary.st0429_9.MappedFileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * User: rpuri@netflix.com
 * Date: 12/29/15
 * Time: 9:59 AM
 * To change this template use File | Settings | File Templates.
 */
public final class IMPDelivery
{
    private static final Logger logger = LoggerFactory.getLogger(IMPDelivery.class);

    private final AssetMap assetMap;
    private final List<InteroperableMasterPackage> interoperableMasterPackages = new ArrayList<>();

    //this corresponds to IMP deliveries that are based on Basic Map Profile v2 (Annex A st0429-9:2014)
    public IMPDelivery(MappedFileSet mappedFileSet) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {
        BasicMapProfilev2FileSet basicMapProfilev2FileSet = new BasicMapProfilev2FileSet(mappedFileSet);

        this.assetMap = basicMapProfilev2FileSet.getAssetMap();

        List<AssetMap.Asset> packingListAssets = this.assetMap.getPackingListAssets();
        for (AssetMap.Asset packingListAsset : packingListAssets)
        {
            URI resolvedPackingListURI = mappedFileSet.getAssetMapURI().resolve(packingListAsset.getPath());
            PackingList packingList = new PackingList(new File(resolvedPackingListURI));

            List<IMPAsset> referencedAssets = new ArrayList<>();
            for (PackingList.Asset referencedAsset : packingList.getAssets())
            {
                UUID referencedAssetUUID = referencedAsset.getUuid();
                referencedAssets.add(new IMPAsset(referencedAssetUUID,
                        mappedFileSet.getAssetMapURI().resolve(this.assetMap.getPath(referencedAssetUUID))));

            }
            interoperableMasterPackages.add(new InteroperableMasterPackage(new IMPAsset(packingList.getUuid(), resolvedPackingListURI),
                    referencedAssets));
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("IMPDelivery{");
        sb.append("assetMap=").append(assetMap);
        sb.append(",\ninteroperableMasterPackages=").append(Arrays.toString(interoperableMasterPackages.toArray()));
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, JAXBException
    {
        File rootFile = new File(args[0]);

        MappedFileSet mappedFileSet = new MappedFileSet(rootFile);
        IMPDelivery impDelivery = new IMPDelivery(mappedFileSet);

        logger.warn(impDelivery.toString());

    }
}
