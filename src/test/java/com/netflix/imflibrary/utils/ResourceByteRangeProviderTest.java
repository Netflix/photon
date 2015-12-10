/*
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
 */

package com.netflix.imflibrary.utils;

import org.testng.annotations.Test;

public class ResourceByteRangeProviderTest
{
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "rangeStart = .*")
    public void negativeRangeStartTest()
    {
        ResourceByteRangeProvider.Utilities.validateRangeRequest(1L, -1L, 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "rangeStart = .*")
    public void rangeStartGreaterThanRangeEndTest()
    {
        ResourceByteRangeProvider.Utilities.validateRangeRequest(1L, 2L, 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "rangeEnd = .*")
    public void invalidRangeEndTest()
    {
        ResourceByteRangeProvider.Utilities.validateRangeRequest(1L, 1L, 2L);
    }
}
