package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.exceptions.IMFException;

import java.net.URI;
import java.util.UUID;

/**
 * User: rpuri@netflix.com
 * Date: 12/29/15
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
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
