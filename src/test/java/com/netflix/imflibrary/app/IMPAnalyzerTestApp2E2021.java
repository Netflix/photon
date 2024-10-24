package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
import com.netflix.imflibrary.st2067_2.CoreConstraints;
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

        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(applicationComposition.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        /* Make sure its APP2#E Composition */
        Assert.assertEquals(applicationComposition.getApplicationCompositionType(), ApplicationCompositionFactory.ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE);

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

    @Test
    public void CoreConstraintsSchemaFromApplicationIdentification() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_3714715a-af0c-4a89-9cc9-c99f61e7eb6d_CC-Namespaces.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);
        Assert.assertNotNull(applicationComposition);
        Assert.assertEquals(applicationComposition.getCoreConstraintsSchema(), CoreConstraints.NAMESPACE_IMF_2020);
        Assert.assertEquals(logger.getErrors().size(), 0);
    }


}
