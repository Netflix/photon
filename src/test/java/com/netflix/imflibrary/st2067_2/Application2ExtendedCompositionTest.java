package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;

@Test(groups = "unit")
public class Application2ExtendedCompositionTest
{
    @Test
    public void app2ExtendedCompositionCDCIPositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void app2ExtendedCompositionRGBPositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void app2ExtendedCompositionInterlacePositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_Interlace.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void app2ExtendedCompositionInterlaceErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_InterlaceError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 3);
    }

    @Test
    public void app2ExtendedCompositionRGBErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85_Error.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 10);
    }

    @Test
    public void app2ExtendedCompositionColorSpaceErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_ColorSpaceError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void app2ExtendedCompositionColorErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_ColorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2ExtendedCompositionQuantizationErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_QuantizationError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 3);
    }

    @Test
    public void app2ExtendedCompositionSamplingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SamplingError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void app2ExtendedCompositionSubDescriptorsErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SubDescriptorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 6);
    }

    @Test
    public void app2ExtendedCompositionJPEG2000SubDescriptorErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_JPEG2000SubDescriptorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 6);
    }

    @Test
    public void app2ExtendedCompositionJ2CLayoutErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_J2CLayoutError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 6);
    }

    @Test
    public void app2ExtendedCompositionRGBAComponentError1Test() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_RGBAComponentError1.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 6);
    }

    @Test
    public void app2ExtendedCompositionRGBAComponentError2Test() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_RGBAComponentError2.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 16);
    }

}
