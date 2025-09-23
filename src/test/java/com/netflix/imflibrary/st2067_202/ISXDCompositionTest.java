package com.netflix.imflibrary.st2067_202;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;

@Test(groups = "unit")
public class ISXDCompositionTest {

    @Test
    public void compositionNotHomogeneousTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/ISXD/CPL_ISXD_TEST_NamespaceUriMismatch.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
        Assert.assertTrue(imfErrorLogger.getErrors().get(0).getErrorDescription().contains("not homogeneous"));
    }

    @Test
    public void compositionEditRateMismatchTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/ISXD/CPL_ISXD_TEST_EditRateMismatch.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        imfErrorLogger.addAllErrors(imfCompositionPlaylist.getErrors());
        imfErrorLogger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
        Assert.assertTrue(imfErrorLogger.getErrors().get(0).getErrorDescription().contains("not equal"));
    }

}
