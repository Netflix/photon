package com.netflix.imflibrary.st0429_8;

import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.nio.file.Path;
import java.util.UUID;

@Test(groups="unit")
public class PackingListTest
{
    @Test
    public void testPackingList() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("test_mapped_file_set/PKL_51edd4be-4506-494d-a58e-516553055c33.xml");
        PackingList packingList = new PackingList(inputFile);
        Assert.assertEquals(packingList.getUUID(), UUID.fromString("51edd4be-4506-494d-a58e-516553055c33"));
        Assert.assertEquals(packingList.getAssets().size(), 3);
        Assert.assertTrue(packingList.toString().length() > 0);

        PackingList.Asset asset = packingList.getAssets().get(2);
        Assert.assertEquals(asset.getUUID(), UUID.fromString("682feecb-7516-4d93-b533-f40d4ce60539"));
        Assert.assertEquals(asset.getSize(), 2544L);
        Assert.assertEquals(asset.getType(),"text/xml");
        Assert.assertEquals(asset.getOriginalFilename(),"CPL_682feecb-7516-4d93-b533-f40d4ce60539.xml");
        Assert.assertTrue(asset.toString().length() > 0);
    }

    @Test
    public void testPackingList2016() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("PKL_2067_2_2016.xml");
        PackingList packingList = new PackingList(inputFile);
        Assert.assertEquals(packingList.getUUID(), UUID.fromString("7281a71b-0dcb-4ed7-93a4-97b7929e2a7c"));
        Assert.assertEquals(packingList.getAssets().size(), 2);
        Assert.assertTrue(packingList.toString().length() > 0);

        PackingList.Asset asset = packingList.getAssets().get(0);
        Assert.assertEquals(asset.getUUID(), UUID.fromString("88b5b453-a342-46eb-bc0a-4c9645f4d627"));
        Assert.assertEquals(asset.getSize(), 19139240035L);
        Assert.assertEquals(asset.getType(),"application/mxf");
        Assert.assertEquals(asset.getOriginalFilename(),"1.mxf");
        Assert.assertTrue(asset.toString().length() > 0);
    }
}
