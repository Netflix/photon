package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.CoreConstraints;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;

@Test(groups = "unit")
public class IMFApp2E5EDonstraintsValidatorTest
{

    @Test
    public void InvalidCPL_BadPrecinctSize() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E5ED/CPL_ASC-STEM2_HTJ2K_12BIT_LOSSLESS.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertTrue(imfCompositionPlaylist.getApplicationIdSet().contains(IMFApp2E5EDConstraintsValidator.applicationIdentification));

        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void ValidCPL_BadPrecinctSize() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E5ED/CPL_ASC-STEM2_J2K_12BIT_LOSSLESS.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertTrue(imfCompositionPlaylist.getApplicationIdSet().contains(IMFApp2E5EDConstraintsValidator.applicationIdentification));

        Assert.assertEquals(logger.getErrors().size(), 0);
    }


}
