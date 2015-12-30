package com.netflix.imflibrary.st2067_2;

import java.util.Collections;
import java.util.List;

/**
 * User: rpuri@netflix.com
 * Date: 12/29/15
 * Time: 10:06 AM
 * To change this template use File | Settings | File Templates.
 */
public final class InteroperableMasterPackage
{

    private final IMPAsset packingList;
    private final List<IMPAsset> referencedAssets;

    public InteroperableMasterPackage(IMPAsset packingList, List<IMPAsset> referencedAssets)
    {
        this.packingList = packingList;
        this.referencedAssets = referencedAssets;
    }

    public IMPAsset getPackingList()
    {
        return this.packingList;
    }

    public List<IMPAsset> getReferencedAssets()
    {
        return Collections.unmodifiableList(this.referencedAssets);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("InteroperableMasterPackage{");
        sb.append("packingList=").append(packingList);
        sb.append(",\nreferencedAssets=").append(referencedAssets);
        sb.append('}');
        return sb.toString();
    }
}
