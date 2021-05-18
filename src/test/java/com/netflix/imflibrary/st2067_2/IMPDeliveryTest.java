package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMPDelivery;
import com.netflix.imflibrary.InteroperableMasterPackage;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2FileSet;
import com.netflix.imflibrary.st0429_9.BasicMapProfileV2MappedFileSet;
import com.netflix.imflibrary.utils.FileLocator;
import java.io.File;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

@Test(groups = "unit")
public class IMPDeliveryTest
{
    @Test
    public void testIMPDelivery() throws Exception
    {
        File inputFile = TestHelper.findResourceByPath("test_mapped_file_set");
        BasicMapProfileV2MappedFileSet basicMapProfileV2MappedFileSet = new BasicMapProfileV2MappedFileSet(new FileLocator(inputFile));
        BasicMapProfileV2FileSet basicMapProfileV2FileSet = new BasicMapProfileV2FileSet(basicMapProfileV2MappedFileSet);
        IMPDelivery impDelivery = new IMPDelivery(basicMapProfileV2FileSet);
        Assert.assertTrue(impDelivery.toString().length() > 0);
        Assert.assertTrue(impDelivery.isValid());

        Assert.assertEquals(impDelivery.getInteroperableMasterPackages().size(), 1);
        InteroperableMasterPackage interoperableMasterPackage = impDelivery.getInteroperableMasterPackages().get(0);
        Assert.assertEquals(interoperableMasterPackage.getPackingListURI(), TestHelper.findResourceByPath("test_mapped_file_set/PKL_51edd4be-4506-494d-a58e-516553055c33.xml").toURI());
        Assert.assertEquals(interoperableMasterPackage.getReferencedAssets().size(), 3);
        Assert.assertEquals(interoperableMasterPackage.getPackingList().getAssets().size(), 3);
    }

}
