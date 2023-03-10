package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;

@Test(groups = "unit")
public class IMPAnalyzerTestApp2E2021
{

    @Test
    public void ValidCPL() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);
        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void InvalidCPLBadFrameStructure() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e-bad-frame-structure.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void InvalidCPLBadCodec() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e-bad-codec.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }
    
    @Test
    public void InvalidCPLBadColor() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e-bad-color.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }
}
