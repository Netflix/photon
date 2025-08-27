package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;

@Test(groups = "unit")
public class IMFCPLValidatorTest
{

    @Test
    public void invalidCPLfragmentedVirtulTrack() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_fragmented_virtual_track.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void invalidCPLfragmentedVirtulTrack_02() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_fragmented_virtual_track_02.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void invalidCPLdanglingED() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_dangling_ed.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

}
