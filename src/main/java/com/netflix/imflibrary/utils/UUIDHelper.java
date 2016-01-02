package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.IMFException;

import java.util.UUID;

public final class UUIDHelper
{
    private static final String UUID_as_a_URN_PREFIX = "urn:uuid:";
    private UUIDHelper()
    {
        //to prevent instantiation
    }

    public static UUID fromUUIDAsURNStringToUUID(String UUIDasURN)
    {
        if (!UUIDasURN.startsWith(UUIDHelper.UUID_as_a_URN_PREFIX))
        {
            throw new IMFException(String.format("Input UUID %s does not start with %s", UUIDasURN, UUIDHelper.UUID_as_a_URN_PREFIX));
        }

        return UUID.fromString(UUIDasURN.split(UUIDHelper.UUID_as_a_URN_PREFIX)[1]);

    }
}
