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
public class IMFApp2E2021ConstraintsValidatorTest
{

    @Test
    public void ValidCPL() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        /* Make sure its APP2#E Composition */
        // todo:
        //Assert.assertEquals(IMFCompositionPlaylist.getApplicationCompositionType(), ApplicationCompositionFactory.ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE);

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void InvalidCPLBadFrameStructure() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e-bad-frame-structure.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void InvalidCPLBadCodec() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e-bad-codec.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }
    
    @Test
    public void InvalidCPLBadColor() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e-bad-color.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();
        
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void CoreConstraintsSchemaFromApplicationIdentification() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_3714715a-af0c-4a89-9cc9-c99f61e7eb6d_CC-Namespaces.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertNotNull(imfCompositionPlaylist);
        // the namespace uri assumed based on the application identification
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), CoreConstraints.NAMESPACE_IMF_2020);
        // a warning is raised when the assumed namespace does match the actual namespace
        Assert.assertEquals(logger.getErrors().size(), 2);
    }


}
