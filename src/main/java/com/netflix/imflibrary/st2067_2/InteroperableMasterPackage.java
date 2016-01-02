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
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * This class is an immutable implementation of the concept of Interoperable Master Package (IMP) defined in Section 7.2.1
 * of st2067-2:2015. Per Section 7.2.1 of st2067-2:2015, an IMP consists of one IMF packing list (see st0429-8:2007) and all
 * the assets it references
 */
@Immutable
public final class InteroperableMasterPackage
{
    private final PackingList packingList;
    private final URI packingListURI;
    private final List<IMPAsset> referencedAssets;

    /**
     * Constructor for an InterOperableMasterPackage object from a {@link com.netflix.imflibrary.st0429_8.PackingList} object,
     * the URI corresponding to the PackingList document, and a list of type {@link com.netflix.imflibrary.st2067_2.IMPAsset} that
     * corresponds to assets referenced by this InteroperableMasterPackage object
     * @param packingList the corresponding packingList object
     * @param packingListURI an absolute URI for the PackingList document corresponding to the supplied PackingList object, construction
     *                       fails if the URI is not absolute
     * @param referencedAssets the list of type {@link com.netflix.imflibrary.st2067_2.IMPAsset} corresponding to assets
     *                         referenced by this InteroperableMasterPackage object
     */
    public InteroperableMasterPackage(PackingList packingList, URI packingListURI, List<IMPAsset> referencedAssets)
    {
        this.packingList = packingList;
        if (!packingListURI.isAbsolute())
        {
            throw new IMFException(String.format("PackingList URI = %s is not absolute", packingListURI));
        }
        this.packingListURI = packingListURI;
        this.referencedAssets = referencedAssets;
    }

    /**
     * Getter for the packing list corresponding to this IMP
     * @return the {@link com.netflix.imflibrary.st0429_8.PackingList PackingList} object corresponding to this InteroperableMasterPackage object
     */
    public PackingList getPackingList()
    {
        return this.packingList;
    }

    /**
     * Getter for the absolute URI of the packing list corresponding to this IMP
     * @return the absolute URI for the packing list corresponding to this IMP
     */
    public URI getPackingListURI()
    {
        return this.packingListURI;
    }

    /**
     * Getter for the list of all assets (other than the packing list) contained by this IMP
     * @return a list of type {@link com.netflix.imflibrary.st0429_8.PackingList.Asset Asset} corresponding to all the assets
     * contained by this IMP
     */
    public List<IMPAsset> getReferencedAssets()
    {
        return Collections.unmodifiableList(this.referencedAssets);
    }

    /**
     * Checks if this InteroperableMasterPackage object is valid. An InteroperableMasterPackage object is valid if all the
     * assets referenced by it are valid
     * @return true if this InteroperableMasterPackage object is valid, false otherwise
     */
    public boolean isValid()
    {
        for (IMPAsset asset : this.referencedAssets)
        {
            if (!asset.isValid())
            {
                return false;
            }
        }

        return true;
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
