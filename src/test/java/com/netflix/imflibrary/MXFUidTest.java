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
public class MXFUidTest
{
    @Test
    public void testGetUid()
    {
        byte[] bytes = new byte[16];
        MXFUid mxfUid = new MXFUid(bytes);
        Assert.assertTrue(mxfUid.toString().length()> 0);
        Assert.assertEquals(mxfUid.getUid(), bytes);
    }

    @Test
    public void testEquals()
    {
        byte[] bytes1 = new byte[16];
        MXFUid mxfUid1 = new MXFUid(bytes1);

        byte[] bytes2 = Arrays.copyOf(bytes1, bytes1.length);
        MXFUid mxfUid2 = new MXFUid(bytes2);
        Assert.assertTrue(mxfUid1.equals(mxfUid2));

        Assert.assertFalse(mxfUid1.equals(null));

        Assert.assertFalse(mxfUid1.equals(bytes1));
    }

    @Test
    public void testHashCode()
    {
        byte[] bytes = new byte[16];
        MXFUid mxfUid = new MXFUid(bytes);
        Assert.assertEquals(mxfUid.hashCode(), Arrays.hashCode(bytes));
    }

    @Test
    public void testToString()
    {
        byte[] bytes = new byte[16];
        MXFUid mxfUid = new MXFUid(bytes);
        Assert.assertEquals(mxfUid.toString().trim(),"uid = 0x00000000000000000000000000000000");

        bytes = new byte[32];
        mxfUid = new MXFUid(bytes);
        Assert.assertEquals(mxfUid.toString().trim(),"uid = 0x0000000000000000000000000000000000000000000000000000000000000000");

        bytes = new byte[1];
        mxfUid = new MXFUid(bytes);
        Assert.assertEquals(mxfUid.toString().trim(), Arrays.toString(bytes));


    }
}
