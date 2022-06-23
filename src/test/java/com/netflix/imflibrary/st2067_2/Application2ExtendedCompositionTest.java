package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

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
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
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
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void app2ExtendedCompositionRGBErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85_Error.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 7);
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
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 2);
    }

    @Test
    public void app2ExtendedCompositionSamplingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SamplingError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2ExtendedCompositionSubDescriptorsErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_SubDescriptorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2ExtendedCompositionJPEG2000SubDescriptorErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_JPEG2000SubDescriptorError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2ExtendedCompositionJ2CLayoutErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_J2CLayoutError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2ExtendedCompositionRGBAComponentError1Test() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_RGBAComponentError1.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 4);
    }

    @Test
    public void app2ExtendedCompositionRGBAComponentError2Test() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_RGBAComponentError2.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 14);
    }

    @Test
    public void app2ExtendedCompositionYUV4KTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_4k.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);

        /* Make sure its APP2#E Composition */
        Assert.assertEquals(applicationComposition.getApplicationCompositionType(), ApplicationCompositionFactory.ApplicationCompositionType.APPLICATION_2E_COMPOSITION_TYPE);

        /* Filter 4k YUV  error */
        String regex = "^.+invalid StoredWidth\\(.+\\) for ColorModel\\(YUV\\).+$";
        List filteredErrors = imfErrorLogger.getErrors().stream()
                .filter(e -> !(  e.getErrorDescription().matches(regex) &&
                        e.getErrorCode().equals(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR) &&
                        e.getErrorDescription().contains(ApplicationCompositionFactory.ApplicationCompositionType.APPLICATION_2E_COMPOSITION_TYPE.toString()))).collect(Collectors.toList());


        /* No other erros after filtering */
        Assert.assertEquals(filteredErrors.size(), 0);

        /* Verify StoredWidth is within max RGB width */
        Assert.assertTrue(applicationComposition.getCompositionImageEssenceDescriptorModel().getStoredWidth() <= Application2ExtendedComposition.MAX_RGB_IMAGE_FRAME_WIDTH);

    }

    @Test
    public void app2ExtendedCompositionPictureEssenceCodingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_EssenceCodingError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }
}
