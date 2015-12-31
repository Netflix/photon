package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.IMFException;

public final class UUIDHelper
{
    private static final String UUID_as_a_URN_PREFIX = "urn:uuid:";
    private UUIDHelper()
    {
        //to prevent instantiation
    }

    public static String fromUUIDAsURNToUUID(String UUIDasURN)
    {
        if (!UUIDasURN.startsWith(UUIDHelper.UUID_as_a_URN_PREFIX))
        {//TODO: added error messaging
            throw new IMFException("");
        }

        return UUIDasURN.split(UUIDHelper.UUID_as_a_URN_PREFIX)[1];

    }
}
