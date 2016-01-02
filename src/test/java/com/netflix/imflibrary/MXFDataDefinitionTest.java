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

@Test(groups = "unit")
public class MXFDataDefinitionTest
{
    @Test
    public void testGetDataDefinition()
    {
        Assert.assertEquals(MXFDataDefinition.getDataDefinition(new MXFUID(MXFUID.picture_essence_track)), MXFDataDefinition.PICTURE);
        Assert.assertEquals(MXFDataDefinition.getDataDefinition(new MXFUID(MXFUID.sound_essence_track)), MXFDataDefinition.SOUND);
        Assert.assertEquals(MXFDataDefinition.getDataDefinition(new MXFUID(MXFUID.data_essence_track)), MXFDataDefinition.DATA);
        byte[] bytes = new byte[16];
        Assert.assertEquals(MXFDataDefinition.getDataDefinition(new MXFUID(bytes)), MXFDataDefinition.OTHER);
    }
}
