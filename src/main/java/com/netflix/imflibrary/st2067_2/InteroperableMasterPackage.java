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

import java.util.Collections;
import java.util.List;

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
