package com.netflix.imflibrary.st0429_8;

import com.netflix.imflibrary.IMFErrorLoggerImpl;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.util.UUID;

@Test(groups="unit")
public class PackingListTest
{
    @Test
    public void testPackingList() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("test_mapped_file_set/PKL_51edd4be-4506-494d-a58e-516553055c33.xml");
        PackingList packingList = new PackingList(inputFile, new IMFErrorLoggerImpl());
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
}
