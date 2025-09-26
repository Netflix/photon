package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel.ProgressionOrder;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;

import java.util.*;

/**
 * Collection of properties and validations specific to ST 2067-21 5th edition.
 */
public class IMFApp2E5EDConstraintsValidator extends IMFApp2EConstraintsValidator {

    private static final String applicationCompositionType = "SMPTE ST 2067-21 IMF Application #2E 5th Edition";

    public static final String applicationIdentification = "http://www.smpte-ra.org/ns/2067-21/5ED";

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
    }

    static final Fraction[] FPS_HD = {
            new Fraction(25),
            new Fraction(30),
            new Fraction(30000, 1001)
    };

    static final Fraction[] FPS_UHD = {
            new Fraction(24),
            new Fraction(24000, 1001),
            new Fraction(25),
            new Fraction(30),
            new Fraction(30000, 1001),
            new Fraction(50),
            new Fraction(60),
            new Fraction(60000, 1001)
    };

    static final Fraction[] FPS_4K = {
            new Fraction(24),
            new Fraction(24000, 1001),
            new Fraction(25),
            new Fraction(30),
            new Fraction(30000, 1001),
            new Fraction(50),
            new Fraction(60),
            new Fraction(60000, 1001),
            new Fraction(120)
    };


    /* Table 3 at SMPTE ST 2067-21:2023 */
    static final CharacteristicsSet[] IMAGE_CHARACTERISTICS = {
            new CharacteristicsSet(
                    1920,
                    1080,
                    Arrays.asList(Colorimetry.Color1, Colorimetry.Color2, Colorimetry.Color3),
                    Arrays.asList(8, 10),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.SeparateFields),
                    Arrays.asList(FPS_HD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            new CharacteristicsSet(
                    1920,
                    1080,
                    Arrays.asList(Colorimetry.Color1, Colorimetry.Color2, Colorimetry.Color3),
                    Arrays.asList(8, 10),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_HD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            new CharacteristicsSet(
                    1920,
                    1080,
                    Arrays.asList(Colorimetry.Color1, Colorimetry.Color2, Colorimetry.Color3),
                    Arrays.asList(8, 10),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_HD),
                    Arrays.asList(Colorimetry.Sampling.Sampling444),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV, Colorimetry.ColorModel.RGB)
            ),
            new CharacteristicsSet(
                    3840,
                    2160,
                    Arrays.asList(Colorimetry.Color4),
                    Arrays.asList(8, 10),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_UHD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            /* QHD */
            new CharacteristicsSet(
                    7680,
                    4320,
                    Arrays.asList(Colorimetry.Color3),
                    Arrays.asList(8, 10, 12, 16),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_UHD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            new CharacteristicsSet(
                    7680,
                    4320,
                    Arrays.asList(Colorimetry.Color5, Colorimetry.Color8),
                    Arrays.asList(10, 12),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_UHD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            new CharacteristicsSet(
                    7680,
                    4320,
                    Arrays.asList(Colorimetry.Color7),
                    Arrays.asList(10, 12, 16),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_UHD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            /* 8K */
            new CharacteristicsSet(
                    8192,
                    6224,
                    Arrays.asList(Colorimetry.Color3),
                    Arrays.asList(8, 10, 12, 16),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_4K),
                    Arrays.asList(Colorimetry.Sampling.Sampling444),
                    Arrays.asList(Colorimetry.Quantization.QE1, Colorimetry.Quantization.QE2),
                    Arrays.asList(Colorimetry.ColorModel.RGB)
            ),
            new CharacteristicsSet(
                    8192,
                    6224,
                    Arrays.asList(Colorimetry.Color5, Colorimetry.Color8),
                    Arrays.asList(10, 12),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_4K),
                    Arrays.asList(Colorimetry.Sampling.Sampling444),
                    Arrays.asList(Colorimetry.Quantization.QE1, Colorimetry.Quantization.QE2),
                    Arrays.asList(Colorimetry.ColorModel.RGB)
            ),
            new CharacteristicsSet(
                    8192,
                    6224,
                    Arrays.asList(Colorimetry.Color6, Colorimetry.Color7),
                    Arrays.asList(10, 12, 16),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_4K),
                    Arrays.asList(Colorimetry.Sampling.Sampling444),
                    Arrays.asList(Colorimetry.Quantization.QE1, Colorimetry.Quantization.QE2),
                    Arrays.asList(Colorimetry.ColorModel.RGB)
            )
    };


    @Override
    protected CharacteristicsSet[] getValidImageCharacteristicsSets() {
        return IMAGE_CHARACTERISTICS;
    }

    @Override
    protected List<ErrorLogger.ErrorObject> validateJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        UL essenceCoding = imageDescriptor.getPictureEssenceCodingUL();
        if (!essenceCoding.equalsWithMask(JPEG2000PICTURECODINGSCHEME, 0b1111111011111100)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Image codec must be JPEG 2000. Found %s instead.", essenceCoding.toString()
                    ));
            return imfErrorLogger.getErrors();
        }

        Integer width = imageDescriptor.getStoredWidth();
        Integer height = imageDescriptor.getStoredHeight();

        if (JPEG2000.isAPP2HT(essenceCoding))
            return validateHTConstraints(imageDescriptor.getJ2KHeaderParameters());

        if (JPEG2000.isIMF8KProfile(essenceCoding)) {
            if (!(width > 4096 && width <= 8192 && height > 0 && height <= 6224)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("JPEG 2000 IMF 8K Profile does not support image resolution (%d/%d)", width, height));
            }
        } else if (JPEG2000.isIMF4KProfile(essenceCoding)) {
            if (!(width > 2048 && width <= 4096 && height > 0 && height <= 3112)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("JPEG 2000 IMF 4K Profile does not support image resolution (%d/%d)", width, height));
            }
        } else if (JPEG2000.isIMF2KProfile(essenceCoding)) {
            if (!(width > 0 && width <= 2048 && height > 0 && height <= 1556)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("JPEG 2000 IMF 2K Profile does not support image resolution (%d/%d)", width, height));
            }
        } else if (JPEG2000.isBroadcastProfile(essenceCoding)) {
            if (!(width > 0 && width <= 3840 && height > 0 && height <= 2160)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("JPEG 2000 Broadcast Profile does not support image resolution (%d/%d)", width, height));
            }
        } else {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Invalid JPEG 2000 Profile: %s", essenceCoding));
        }

        return imfErrorLogger.getErrors();
    }


    /* Validate codestream parameters against constraints listed in SMPTE ST 2067-21:2023 Annex I */

    public static List<ErrorLogger.ErrorObject> validateHTConstraints(J2KHeaderParameters p) {

        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        if (p == null) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "APP2.HT: Missing or incomplete JPEG 2000 Sub-descriptor");
            return logger.getErrors();
        }

        if (p.xosiz != 0 || p.yosiz != 0 || p.xtosiz != 0 || p.ytosiz != 0) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid XOsiz, YOsiz, XTOsiz or YTOsiz");
        }

        if (p.xtsiz < p.xsiz || p.ytsiz < p.ysiz) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid XTsiz or XYsiz");
        }

        /* components constraints */
        if (p.csiz.length <= 0 || p.csiz.length > 4) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Invalid number (%d) of components", p.csiz.length));
        }

        /* x sub-sampling */
        if (p.csiz[0].xrsiz != 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 1");
        }
        if (p.csiz.length > 1 && p.csiz[1].xrsiz != 1 &&
                (p.csiz.length <= 2 || p.csiz[1].xrsiz != 2 || p.csiz[2].xrsiz != 2)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 2");
        }
        if (p.csiz.length > 2 && p.csiz[2].xrsiz != 1 && (p.csiz[1].xrsiz != 2 || p.csiz[2].xrsiz != 2)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 3");
        }
        if (p.csiz.length > 3 && p.csiz[3].xrsiz != 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 4");
        }

        /* y sub-sampling and sample width */
        if (p.csiz[0].ssiz > 15 || p.csiz[0].ssiz < 7) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Invalid bit depth (%d)", p.csiz[0].ssiz + 1));
        }
        for (int i = 0; i < p.csiz.length; i++) {
            if (p.csiz[i].yrsiz != 1) {
                logger.addError(
                        IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("APP2.HT: invalid vertical sub-sampling for component %d", i));
            }
            if (p.csiz[i].ssiz != p.csiz[0].ssiz) {
                logger.addError(
                        IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        "APP2.HT: all components must have the same bit depth");
            }
        }
        /* CAP constraints */

        /* Pcapi is 1 for i = 15, and 0 otherwise, per ST 2067-21 Annex I; therefore, pcap = 2^(32-15) = 131072 */
        if (p.cap == null || p.cap.pcap != 131072 || p.cap.ccap == null || p.cap.ccap.length != 1) {
            /* codestream shall require only Part 15 capabilities */
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: missing or invalid CAP marker");
            return logger.getErrors();
        }

        if ((p.cap.ccap[0] & 0b1111000000000000) != 0) {
            /* Bits 12-15 of Ccap15 shall be 0 */
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Bits 12-15 of Ccap15 shall be 0");
        }

        boolean isHTREV = (p.cap.ccap[0] & 0b100000) == 0;

        /* COD */

        if (p.cod == null) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "APP2.HT: Missing COD marker");
            return logger.getErrors();
        }

        /* no scod constraints */

        /* code-block style */
        if (p.cod.cbStyle != 0b01000000) {
            /* bad code-block style */
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid default code-block style");
        }

        /* progression order - RPCL is not required, but ST 2067-21:2023 Annex I Note 3 implies a preference */
        if (p.cod.progressionOrder != ProgressionOrder.RPCL.value())
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "APP2.HT: JPEG 2000 progression order is not RPCL");

        /* resolution layers */
        if (p.cod.numDecompLevels == 0) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Number of decomposition levels must be greater than 0");
        }


        long maxSz = Math.max(p.xsiz, p.ysiz);
        if ((maxSz <= 2048 && p.cod.numDecompLevels > 5) ||
                (maxSz <= 4096 && p.cod.numDecompLevels > 6) ||
                (maxSz <= 8192 && p.cod.numDecompLevels > 7)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid number of decomposition levels");
        }

        /* number of layers */

        if (p.cod.numLayers != 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Number of layers (%d) is not 1", p.cod.numLayers));
        }

        /* code-block sizes */

        if (p.cod.ycb < 5 || p.cod.ycb > 6) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Invalid vertical code-block size (ycb = %d)", p.cod.ycb));
        }

        if (p.cod.xcb < 5 || p.cod.xcb > 7) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Invalid horizontal code-block size (xcb = %d)", p.cod.xcb));
        }


        /* transformation */

        boolean isReversibleFilter = (p.cod.transformation == 1);

        if (isHTREV && !isReversibleFilter) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: 9-7 irreversible filter is used but HTREV is signaled in CAP");
        }

        /* precinct size */

        if (p.cod.precinctSizes.length == 0 || p.cod.precinctSizes[0] != 0x77) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid N_L LL band precinct sizes");
        }

        for (int i = 1; i < p.cod.precinctSizes.length; i++)
            if (p.cod.precinctSizes[i] != 0x88) {
                logger.addError(
                        IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        "APP2.HT: Invalid non-N_L LL band precinct sizes");
                break;
            }

        /* magbp */

        /* expected values per Annex of SMPTE ST 2067-21 */
        int maxB = p.csiz[0].ssiz + 2;
        if (isReversibleFilter) {
            maxB += 2 + p.cod.multiComponentTransform;
            if (p.cod.numDecompLevels > 5)
                maxB += 1;
        } else if (p.cod.multiComponentTransform == 1 && p.csiz[0].ssiz > 9) {
            maxB += 1;
        }

        /* codestream value */
        int codestreamB = (p.cap.ccap[0] & 0b11111) + 8;

        if (codestreamB > 31) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "APP2.HT: Parameter B has exceeded its limit to an extent that decoder issues are to be expected");
        } else if (codestreamB > maxB) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                    "APP2.HT: Parameter B has exceeded expected limits");
        }

        return logger.getErrors();
    }

}
