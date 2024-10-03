package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.J2KHeaderParameters;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;

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

        /* Make sure its 2013 core constraints */
        Assert.assertEquals(applicationComposition.getCoreConstraintsSchema(), "http://www.smpte-ra.org/schemas/2067-2/2013");

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

    @Test
    public void validJ2KHeaderParameters() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2021/CPL_b2e1ace2-9c7d-4c12-b2f7-24bde303869e.xml");
        FileByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger logger = new IMFErrorLoggerImpl();
        IMFCompositionPlaylistType imfCompositionPlaylistType = IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, logger);
        Application2E2021 app = new Application2E2021(imfCompositionPlaylistType);
        CompositionImageEssenceDescriptorModel image = app.getCompositionImageEssenceDescriptorModel();

        Assert.assertNotNull(image);

        @SuppressWarnings("null")
        J2KHeaderParameters p = image.getJ2KHeaderParameters();

        Assert.assertEquals(p.rsiz, 16384);

        Assert.assertEquals(p.xsiz, 1920);
        Assert.assertEquals(p.ysiz, 1080);
        Assert.assertEquals(p.xosiz, 0);
        Assert.assertEquals(p.yosiz, 0);
        Assert.assertEquals(p.xtsiz, 1920);
        Assert.assertEquals(p.ytsiz, 1080);
        Assert.assertEquals(p.xtosiz, 0);
        Assert.assertEquals(p.ytosiz, 0);

        Assert.assertEquals(p.csiz.length, 3);
        Assert.assertEquals(p.csiz[0].ssiz, 9);
        Assert.assertEquals(p.csiz[0].yrsiz, 1);
        Assert.assertEquals(p.csiz[0].xrsiz, 1);
        Assert.assertEquals(p.csiz[1].ssiz, 9);
        Assert.assertEquals(p.csiz[1].yrsiz, 1);
        Assert.assertEquals(p.csiz[1].xrsiz, 1);
        Assert.assertEquals(p.csiz[2].ssiz, 9);
        Assert.assertEquals(p.csiz[2].yrsiz, 1);
        Assert.assertEquals(p.csiz[2].xrsiz, 1);

        Assert.assertEquals(p.cap.pcap, 131072);
        Assert.assertEquals(p.cap.ccap.length, 1);
        Assert.assertEquals(p.cap.ccap[0], 2);

        Assert.assertEquals(p.qcd.sqcd, 0x20);
        Assert.assertEquals(p.qcd.spqcd, new int[] { 0x60, 0x68, 0x68, 0x70, 0x68, 0x68, 0x70, 0x68, 0x68, 0x70, 0x68,
                0x68, 0x68, 0x60, 0x60, 0x68 });

        /* 01020001010505034001778888888888 */
        /*  */
        Assert.assertEquals(p.cod.scod, 0x01);
        Assert.assertEquals(p.cod.progressionOrder, 0x02);
        Assert.assertEquals(p.cod.numLayers, 0x0001);
        Assert.assertEquals(p.cod.multiComponentTransform, 0x01);
        Assert.assertEquals(p.cod.numDecompLevels, 0x05);
        Assert.assertEquals(p.cod.xcb, 7);
        Assert.assertEquals(p.cod.ycb, 5);
        Assert.assertEquals(p.cod.cbStyle, 0x40);
        Assert.assertEquals(p.cod.transformation, 0x01);
        Assert.assertEquals(p.cod.precinctSizes, new short[] { 0x77, 0x88, 0x88, 0x88, 0x88, 0x88 });
    }

    @Test
    public void app2ExtendedCompositionEssenceCodingErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_CodecError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

    @Test
    public void app2ExtendedCompositionJ2kProfileErrorTest() throws IOException {
        File inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_J2kProfileError.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        ApplicationCompositionFactory.getApplicationComposition(inputFile, imfErrorLogger);
        Assert.assertEquals(imfErrorLogger.getErrors().size(), 1);
    }

}
