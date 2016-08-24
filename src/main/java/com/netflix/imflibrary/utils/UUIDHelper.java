package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;

import java.util.UUID;

public final class UUIDHelper
{
    private static final String UUID_as_a_URN_PREFIX = "urn:uuid:";
    private UUIDHelper()
    {
        //to prevent instantiation
    }

    /**
     * A helper method to return the UUID without the "urn:uuid:" prefix
     * @param UUIDasURN a urn:uuid type
     * @return a UUID without the "urn:uuid:" prefix
     */
    public static UUID fromUUIDAsURNStringToUUID(String UUIDasURN)
    {
        if (!UUIDasURN.startsWith(UUIDHelper.UUID_as_a_URN_PREFIX))
        {
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_UUID_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, String.format("Input UUID %s " +
                    "does not start with %s", UUIDasURN, UUIDHelper
                    .UUID_as_a_URN_PREFIX));
            throw new IMFException(String.format("Input UUID %s does not start with %s", UUIDasURN, UUIDHelper
                    .UUID_as_a_URN_PREFIX), imfErrorLogger);
        }

        return UUID.fromString(UUIDasURN.split(UUIDHelper.UUID_as_a_URN_PREFIX)[1]);

    }

    /**
     * A helper method to return a UUID with the "urn:uuid:" prefix
     * @param uuid the UUID type
     * @return a UUID with the "urn:uuid:" prefix as a String
     */
    public static String fromUUID(UUID uuid){
        return "urn:uuid:" + uuid.toString();
    }
}
