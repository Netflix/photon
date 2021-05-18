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

package com.netflix.imflibrary;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.utils.Locator;
import java.net.URI;
import javax.annotation.concurrent.Immutable;

/**
 * This class represents a thin, immutable wrapper around a PackingList {@link com.netflix.imflibrary.st0429_8.PackingList.Asset Asset}. It holds
 * a reference to a PackingList Asset along-with the associated absolute URI
 */
@Immutable
public final class IMPAsset
{
    private final URI uri;
    private final PackingList.Asset asset;
    private final Locator.Configuration configuration;
    
    /**
     * Constructor for an {@link IMPAsset IMPAsset} from a PackingList Asset and its URI. Construction
     * fails if the URI is not absolute
     * @param uri the absolute URI
     * @param asset the corresponding asset
     * @param configuration the locator configuration.
     */
    public IMPAsset(URI uri, PackingList.Asset asset, Locator.Configuration configuration)
    {
        if (!uri.isAbsolute())
        {
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            String message = String.format("uri = %s is not absolute", uri);
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.URI_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        this.uri = uri;
        this.configuration = configuration;
        this.asset = asset;
    }

    /**
     * Checks if this asset is valid
     * @return true if this asset is valid, false otherwise
     */
    public boolean isValid()
    {//TODO: this implementation needs to improve
        return Locator.of(uri, configuration).exists();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("IMPAsset{");
        sb.append("uri=").append(uri);
        sb.append(", asset=").append(asset);
        sb.append('}');
        return sb.toString();
    }
}
