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

package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2FileSet;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class represents the concept of an IMF delivery as defined in Section 8 of st2067-2:2015. Informally, an IMF delivery
 * corresponds to a single AssetMap document and one or more Interoperable Master Packages.
 */
@Immutable
public final class IMPDelivery
{
    private static final Logger logger = LoggerFactory.getLogger(IMPDelivery.class);

    private final AssetMap assetMap;
    private final List<InteroperableMasterPackage> interoperableMasterPackages = new ArrayList<>();
    private final IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

    /**
     * Constructor for an IMPDelivery object for deliveries that are based on Basic Map Profile v2 (Annex A st0429-9:2014)
     * @param basicMapProfileV2FileSet a single mapped file set that is compliant with Basic Map Profile v2 (Annex A st0429-9:2014)
     * @throws IOException - forwarded from {@link BasicMapProfileV2FileSet#BasicMapProfileV2FileSet(BasicMapProfileV2MappedFileSet, com.netflix.imflibrary.IMFErrorLogger)  BasicMapProfilev2FileSet} constructor
     * @throws SAXException - forwarded from {@link BasicMapProfileV2FileSet#BasicMapProfileV2FileSet(BasicMapProfileV2MappedFileSet, com.netflix.imflibrary.IMFErrorLogger) BasicMapProfilev2FileSet}  constructor
     * @throws JAXBException - forwarded from {@link BasicMapProfileV2FileSet#BasicMapProfileV2FileSet(BasicMapProfileV2MappedFileSet, com.netflix.imflibrary.IMFErrorLogger) BasicMapProfilev2FileSet}  constructor
     * @throws URISyntaxException - forwarded from {@link BasicMapProfileV2FileSet#BasicMapProfileV2FileSet(BasicMapProfileV2MappedFileSet, com.netflix.imflibrary.IMFErrorLogger) BasicMapProfilev2FileSet} constructor
     */
    public IMPDelivery(BasicMapProfileV2FileSet basicMapProfileV2FileSet) throws IOException, SAXException, JAXBException, URISyntaxException
    {
        this.assetMap = basicMapProfileV2FileSet.getAssetMap();

        List<AssetMap.Asset> packingListAssets = this.assetMap.getPackingListAssets();
        for (AssetMap.Asset packingListAsset : packingListAssets)
        {
            URI absolutePackingListURI = basicMapProfileV2FileSet.getAbsoluteAssetMapURI().resolve(packingListAsset.getPath());
            PackingList packingList = new PackingList(new File(absolutePackingListURI));

            List<IMPAsset> referencedAssets = new ArrayList<>();
            for (PackingList.Asset referencedAsset : packingList.getAssets())
            {
                UUID referencedAssetUUID = referencedAsset.getUUID();
                referencedAssets.add(new IMPAsset(basicMapProfileV2FileSet.getAbsoluteAssetMapURI().resolve(this.assetMap.getPath(referencedAssetUUID)),
                        referencedAsset));

            }
            interoperableMasterPackages.add(new InteroperableMasterPackage(packingList, absolutePackingListURI, referencedAssets));
        }
    }

    /**
     * Getter for a list of IMPs contained in this delivery
     * @return a list of type {@link com.netflix.imflibrary.st2067_2.InteroperableMasterPackage} corresponding to IMPs
     * contained in this delivery
     */
    public List<InteroperableMasterPackage> getInteroperableMasterPackages()
    {
        return Collections.unmodifiableList(this.interoperableMasterPackages);
    }

    /**
     * Checks if the IMF delivery is valid. An IMF delivery is considered valid if all associated IMPs are valid
     * @return true if this delivery is valid, false otherwise
     */
    public boolean isValid()
    {
        for (InteroperableMasterPackage interoperableMasterPackage : this.interoperableMasterPackages)
        {
            if (!interoperableMasterPackage.isValid())
            {
                return false;
            }
        }

        return true;
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

        BasicMapProfileV2MappedFileSet basicMapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(rootFile);
        BasicMapProfileV2FileSet basicMapProfileV2FileSet = new BasicMapProfileV2FileSet(basicMapProfileV2MappedFileSet);
        IMPDelivery impDelivery = new IMPDelivery(basicMapProfileV2FileSet);

        logger.warn(impDelivery.toString());

    }
}
