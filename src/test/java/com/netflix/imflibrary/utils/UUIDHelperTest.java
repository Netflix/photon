package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.IMFException;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class UUIDHelperTest
{
    @Test(expectedExceptions = IMFException.class)
    public void testUUIDHelperBadInput()
    {
        UUIDHelper.fromUUIDAsURNToUUID("682feecb-7516-4d93-b533-f40d4ce60539");
    }
}
