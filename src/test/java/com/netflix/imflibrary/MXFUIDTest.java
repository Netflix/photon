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

package com.netflix.imflibrary;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

@Test(groups = "unit")
public class MXFUIDTest
{
    @Test
    public void testGetUid()
    {
        byte[] bytes = new byte[16];
        MXFUID MXFUID = new MXFUID(bytes);
        Assert.assertTrue(MXFUID.toString().length()> 0);
        Assert.assertEquals(MXFUID.getUID(), bytes);
    }

    @Test
    public void testEquals()
    {
        byte[] bytes1 = new byte[16];
        MXFUID MXFUID1 = new MXFUID(bytes1);

        byte[] bytes2 = Arrays.copyOf(bytes1, bytes1.length);
        MXFUID MXFUID2 = new MXFUID(bytes2);
        Assert.assertTrue(MXFUID1.equals(MXFUID2));

        Assert.assertFalse(MXFUID1.equals(null));

        Assert.assertFalse(MXFUID1.equals(bytes1));
    }

    @Test
    public void testHashCode()
    {
        byte[] bytes = new byte[16];
        MXFUID MXFUID = new MXFUID(bytes);
        Assert.assertEquals(MXFUID.hashCode(), Arrays.hashCode(bytes));
    }

    @Test
    public void testToString()
    {
        byte[] bytes = new byte[16];
        MXFUID MXFUID = new MXFUID(bytes);
        Assert.assertEquals(MXFUID.toString().trim(),"0x00000000000000000000000000000000");

        bytes = new byte[32];
        MXFUID = new MXFUID(bytes);
        Assert.assertEquals(MXFUID.toString().trim(),"0x0000000000000000000000000000000000000000000000000000000000000000");

        bytes = new byte[1];
        MXFUID = new MXFUID(bytes);
        Assert.assertEquals(MXFUID.toString().trim(), Arrays.toString(bytes));


    }
}
