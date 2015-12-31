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

import com.netflix.imflibrary.exceptions.IMFException;

import java.net.URI;
import java.util.UUID;

public final class IMPAsset
{
    private final UUID uuid;
    private final URI uri;

    public IMPAsset(UUID uuid, URI uri)
    {
        if (!uri.isAbsolute())
        {//TODO: add error messaging
            throw new IMFException("");
        }
        this.uuid = uuid;
        this.uri = uri;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("IMPAsset{");
        sb.append("uuid=").append(uuid);
        sb.append(", uri=").append(uri);
        sb.append('}');
        return sb.toString();
    }
}
