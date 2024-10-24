package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;

@Test(groups = "unit")
public class Application2CompositionTest
{
    @Test
    public void app2CompositionCDCIPositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    @Ignore
    public void app2CompositionRGBPositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void app2CompositionInterlacePositiveTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_Interlace.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 0);
    }

    @Test
    public void app2CompositionInterlaceErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_InterlaceError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    @Ignore
    public void app2CompositionRGBErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85_Error.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 7);
    }

    @Test
    public void app2CompositionColorSpaceErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_ColorSpaceError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void app2CompositionColorErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_ColorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2CompositionQuantizationErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_QuantizationError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void app2CompositionSamplingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SamplingError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 3);
    }

    @Test
    public void app2CompositionSubDescriptorsErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SubDescriptorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void app2CompositionJPEG2000SubDescriptorMissingComponentDepthErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SubDescriptorError_componentDepth_missing.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 6);
    }

    @Test
    public void app2CompositionJPEG2000SubDescriptorComponentDepthPixelDepthMismatchErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SubDescriptorError_componentDepth_mismatch.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void app2CompositionJPEG2000SubDescriptorErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_JPEG2000SubDescriptorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void app2CompositionJ2CLayoutErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_J2CLayoutError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void app2CompositionRGBAComponentError1Test() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_RGBAComponentError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 5);
    }

    @Test
    public void app2CompositionRGBAComponentError2Test() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_RGBAError1.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 10);
    }

    @Test
    public void app2CompositionPictureEssenceCodingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_BadEssenceCoding.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void app2CompositionStoredWidthErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_StoredWidthError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void app2CompositionEssenceCodingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_EssenceCodingError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }
}
