package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.J2KHeaderParameters;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel.ProgressionOrder;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.JPEG2000;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * A class that models Composition with Application 2Extended constraints from
 * 2067-21 specification
 */
public class Application2E2021 extends AbstractApplicationComposition {
    public static final String APP_IDENTIFICATION = "http://www.smpte-ra.org/ns/2067-21/2021";
    private static String APP_STRING = ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE.toString();


    static class CharacteristicsSet {
        private Integer maxWidth;
        private Integer maxHeight;
        private HashSet<Colorimetry> colorSystems;
        private HashSet<Integer> bitDepths;
        private HashSet<FrameLayoutType> frameStructures;
        private HashSet<Fraction> frameRates;
        private HashSet<Sampling> samplings;
        private HashSet<Quantization> quantizations;
        private HashSet<ColorModel> colorModels;
        /* Stereoscopic images are not supported */

        public CharacteristicsSet(Integer maxWidth,
                Integer maxHeight,
                List<Colorimetry> colorSystems,
                List<Integer> bitDepths,
                List<FrameLayoutType> frameStructures,
                List<Fraction> frameRates,
                List<Sampling> samplings,
                List<Quantization> quantizations,
                List<ColorModel> colorModels) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.colorSystems = new HashSet<Colorimetry>(colorSystems);
            this.bitDepths = new HashSet<Integer>(bitDepths);
            this.frameStructures = new HashSet<FrameLayoutType>(frameStructures);
            this.frameRates = new HashSet<Fraction>(frameRates);
            this.samplings = new HashSet<Sampling>(samplings);
            this.quantizations = new HashSet<Quantization>(quantizations);
            this.colorModels = new HashSet<ColorModel>(colorModels);
        }

        public boolean has(Integer width,
                Integer height,
                Colorimetry colorSystem,
                Integer bitDepth,
                FrameLayoutType frameStructure,
                Fraction frameRate,
                Sampling sampling,
                Quantization quantization,
                ColorModel colorModel) {
            return width <= this.maxWidth &&
                    height <= this.maxHeight &&
                    this.colorSystems.contains(colorSystem) &&
                    this.bitDepths.contains(bitDepth) &&
                    this.frameStructures.contains(frameStructure) &&
                    this.frameRates.contains(frameRate) &&
                    this.samplings.contains(sampling) &&
                    this.quantizations.contains(quantization) &&
                    this.colorModels.contains(colorModel);
        }
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
            Arrays.asList(FrameLayoutType.SeparateFields),
            Arrays.asList(FPS_HD),
            Arrays.asList(Sampling.Sampling422),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV)
        ),
        new CharacteristicsSet(
            1920,
            1080,
            Arrays.asList(Colorimetry.Color1, Colorimetry.Color2, Colorimetry.Color3),
            Arrays.asList(8, 10),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_HD),
            Arrays.asList(Sampling.Sampling422),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV)
        ),
        new CharacteristicsSet(
            1920,
            1080,
            Arrays.asList(Colorimetry.Color1, Colorimetry.Color2, Colorimetry.Color3),
            Arrays.asList(8, 10),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_HD),
            Arrays.asList(Sampling.Sampling444),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV, ColorModel.RGB)
        ),
        new CharacteristicsSet(
            3840,
            2160,
            Arrays.asList(Colorimetry.Color4),
            Arrays.asList(8, 10),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_UHD),
            Arrays.asList(Sampling.Sampling422),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV)
        ),
        new CharacteristicsSet(
            3840,
            2160,
            Arrays.asList(Colorimetry.Color3),
            Arrays.asList(8, 10, 12, 16),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_UHD),
            Arrays.asList(Sampling.Sampling422),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV)
        ),
        new CharacteristicsSet(
            3840,
            2160,
            Arrays.asList(Colorimetry.Color5, Colorimetry.Color8),
            Arrays.asList(10, 12),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_UHD),
            Arrays.asList(Sampling.Sampling422),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV)
        ),
        new CharacteristicsSet(
            3840,
            2160,
            Arrays.asList(Colorimetry.Color7),
            Arrays.asList(10, 12, 16),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_UHD),
            Arrays.asList(Sampling.Sampling422),
            Arrays.asList(Quantization.QE1),
            Arrays.asList(ColorModel.YUV)
        ),
        new CharacteristicsSet(
            4096,
            3112,
            Arrays.asList(Colorimetry.Color3),
            Arrays.asList(8, 10, 12, 16),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_4K),
            Arrays.asList(Sampling.Sampling444),
            Arrays.asList(Quantization.QE1, Quantization.QE2),
            Arrays.asList(ColorModel.RGB)
        ),
        new CharacteristicsSet(
            4096,
            3112,
            Arrays.asList(Colorimetry.Color5, Colorimetry.Color8),
            Arrays.asList(10, 12),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_4K),
            Arrays.asList(Sampling.Sampling444),
            Arrays.asList(Quantization.QE1, Quantization.QE2),
            Arrays.asList(ColorModel.RGB)
        ),
        new CharacteristicsSet(
            4096,
            3112,
            Arrays.asList(Colorimetry.Color6, Colorimetry.Color7),
            Arrays.asList(10, 12, 16),
            Arrays.asList(FrameLayoutType.FullFrame),
            Arrays.asList(FPS_4K),
            Arrays.asList(Sampling.Sampling444),
            Arrays.asList(Quantization.QE1, Quantization.QE2),
            Arrays.asList(ColorModel.RGB)
        )
    };

    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>(){{
        add("SignalStandard");
        add("ActiveFormatDescriptor");
        add("VideoLineMap");
        add("AlphaTransparency");
        add("PixelLayout");
        add("ActiveHeight");
        add("ActiveWidth");
        add("ActiveXOffset");
        add("ActiveYOffset");
    }});

    public Application2E2021(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public Application2E2021(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType,
            Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);

        try {
            CompositionImageEssenceDescriptorModel imageDescriptorModel = getCompositionImageEssenceDescriptorModel();

            if (imageDescriptorModel != null) {

                imfErrorLogger.addAllErrors(imageDescriptorModel.getErrors());

                Application2Composition.validateGenericPictureEssenceDescriptor(
                    imageDescriptorModel,
                    ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE,
                    imfErrorLogger
                );

                Application2E2021.validateImageCharacteristics(imageDescriptorModel, imfErrorLogger);

            }
        } catch (Exception e) {
            imfErrorLogger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "Exception in validating EssenceDescriptors in APPLICATION_2E_COMPOSITION_TYPE: %s ",
                            e.getMessage()));
        }
    }

    /* Validate codestream parameters against constraints listed in SMPTE ST 2067-21:2023 Annex I */

    public static boolean validateHTConstraints(J2KHeaderParameters p,
                                     IMFErrorLogger logger) {
        boolean isValid = true;

        if (p == null) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "APP2.HT: Missing or incomplete JPEG 2000 Sub-descriptor");
            return false;
        }

        if (p.xosiz != 0 || p.yosiz != 0 || p.xtosiz != 0 || p.ytosiz != 0) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid XOsiz, YOsiz, XTOsiz or YTOsiz");
            isValid = false;
        }

        if (p.xtsiz < p.xsiz || p.ytsiz < p.ysiz) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid XTsiz or XYsiz");
            isValid = false;
        }

        /* components constraints */

        if (p.csiz.length <= 0 || p.csiz.length > 4) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Invalid number (%d) of components", p.csiz.length));
            isValid = false;
        }

        /* x sub-sampling */
        if (p.csiz[0].xrsiz != 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 1");
            isValid = false;
        }
        if (p.csiz.length > 1 && p.csiz[1].xrsiz != 1 &&
            (p.csiz.length <= 2 || p.csiz[1].xrsiz != 2 || p.csiz[2].xrsiz != 2)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 2");
            isValid = false;
        }
        if (p.csiz.length > 2 && p.csiz[2].xrsiz != 1 && (p.csiz[1].xrsiz != 2 || p.csiz[2].xrsiz != 2)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 3");
            isValid = false;
        }
        if (p.csiz.length > 3 && p.csiz[3].xrsiz != 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: invalid horizontal sub-sampling for component 4");
            isValid = false;
        }

        /* y sub-sampling and sample width */
        if (p.csiz[0].ssiz > 15 || p.csiz[0].ssiz < 7) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("APP2.HT: Invalid bit depth (%d)", p.csiz[0].ssiz + 1));
            isValid = false;
        }
        for (int i = 0; i < p.csiz.length; i++) {
            if (p.csiz[i].yrsiz != 1) {
                logger.addError(
                        IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("APP2.HT: invalid vertical sub-sampling for component %d", i));
                isValid = false;
            }
            if (p.csiz[i].ssiz != p.csiz[0].ssiz) {
                logger.addError(
                        IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        "APP2.HT: all components must have the same bit depth");
                isValid = false;
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
            return false;
        }

        if ((p.cap.ccap[0] & 0b1111000000000000) != 0) {
            /* Bits 12-15 of Ccap15 shall be 0 */
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Bits 12-15 of Ccap15 shall be 0");
            isValid = false;
        }

        boolean isHTREV = (p.cap.ccap[0] & 0b100000) == 0;

        /* COD */

        if (p.cod == null) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "APP2.HT: Missing COD marker");
            return false;
        }

        /* no scod constraints */

        /* code-block style */
        if (p.cod.cbStyle != 0b01000000) {
            /* bad code-block style */
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid default code-block style");
            isValid = false;
        }

        /* progression order - RPCL is not required, but ST 2067-21:2023 Annex I Note 3 implies a preference */
        if (p.cod.progressionOrder != ProgressionOrder.RPCL.value())
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                "APP2.HT: JPEG 2000 progression order is not RPCL");

        /* resolution layers */
        if (p.cod.numDecompLevels == 0) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "APP2.HT: Number of decomposition levels must be greater than 0");
            isValid = false;
        }


        long maxSz = Math.max(p.xsiz, p.ysiz);
        if ((maxSz <= 2048 && p.cod.numDecompLevels > 5) ||
            (maxSz <= 4096 && p.cod.numDecompLevels > 6) ||
            (maxSz <= 8192 && p.cod.numDecompLevels > 7)) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "APP2.HT: Invalid number of decomposition levels");
            isValid = false;
        }

        /* number of layers */

        if (p.cod.numLayers != 1) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                String.format("APP2.HT: Number of layers (%d) is not 1", p.cod.numLayers));
            isValid = false;
        }

        /* code-block sizes */

        if (p.cod.ycb < 5 || p.cod.ycb > 6) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                String.format("APP2.HT: Invalid vertical code-block size (ycb = %d)", p.cod.ycb));
            isValid = false;
        }

        if (p.cod.xcb < 5 || p.cod.xcb > 7) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                String.format("APP2.HT: Invalid horizontal code-block size (xcb = %d)", p.cod.xcb));
            isValid = false;
        }


        /* transformation */

        boolean isReversibleFilter = (p.cod.transformation == 1);

        if (isHTREV && !isReversibleFilter) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "APP2.HT: 9-7 irreversible filter is used but HTREV is signaled in CAP");
            isValid = false;
        }

        /* precinct size */

        if (p.cod.precinctSizes.length == 0 || p.cod.precinctSizes[0] != 0x77) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "APP2.HT: Invalid precinct sizes");
            isValid = false;
        }

        for (int i = 1; i < p.cod.precinctSizes.length; i++)
            if (p.cod.precinctSizes[i] != 0x88) {
                logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Invalid precinct sizes");
                isValid = false;
                break;
            }

        /* magbp - calculation according to ITU-T T.814 */


        int maxB = p.csiz[0].ssiz + 2;
        if (isReversibleFilter) {
            maxB += 2 + p.cod.multiComponentTransform;
            if (p.cod.numDecompLevels > 5)
                maxB += 1;
        } else if (p.cod.multiComponentTransform == 1 && p.csiz[0].ssiz > 9) {
            maxB += 1;
        }

        int codestreamB = (p.cap.ccap[0] & 0b11111) + 8;

        /*
        *   NOTE: The Parameter B constraints in ST 2067-21:2023 are arguably too narrow, and existing implementations do violate them under certain circumstances.
        *   Since practical issues are not expected from software decoders otherwise, an ERROR is currently returned only for values that exceed the max value (21)
        *   allowed for any configuration by ST 2067-21:2023. A WARNING is provided for values that exceed the limit stated in ST 2067-21:2023, but not 21.
        *
        *   TODO: This should be revisited as more implementations become available. Discussion for reference: https://github.com/SMPTE/st2067-21/issues/7
        */

        if (codestreamB > 21) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "APP2.HT: Parameter B has exceeded its limit to an extent that decoder issues are to be expected");
            isValid = false;
        } else if (codestreamB > maxB) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                "APP2.HT: Parameter B has exceeded its limits");
        }

        return isValid;
    }

    /**
     * @deprecated Instead use {@link #isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor, IMFErrorLogger logger)}
     */
    @Deprecated
    public static boolean isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor) {
        return isValidJ2KProfile(imageDescriptor, new com.netflix.imflibrary.IMFErrorLoggerImpl());
    }

    public static boolean isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor,
                                            IMFErrorLogger logger) {
        UL essenceCoding = imageDescriptor.getPictureEssenceCodingUL();
        Integer width = imageDescriptor.getStoredWidth();
        Integer height = imageDescriptor.getStoredHeight();

        if (JPEG2000.isAPP2HT(essenceCoding))
            return validateHTConstraints(imageDescriptor.getJ2KHeaderParameters(), logger);

        if (JPEG2000.isIMF4KProfile(essenceCoding))
            return width > 2048 && width <= 4096 && height > 0 && height <= 3112;

        if (JPEG2000.isIMF2KProfile(essenceCoding))
            return width > 0 && width <= 2048 && height > 0 && height <= 1556;

        if (JPEG2000.isBroadcastProfile(essenceCoding))
            return width > 0 && width <= 3840 && height > 0 && height <= 2160;

        return false;
    }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageDescriptor,
            IMFErrorLogger logger) {

        // J2K profiles
        if (!isValidJ2KProfile(imageDescriptor, logger)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "Invalid JPEG 2000 profile");
        }

        boolean isValid = false;

        for (CharacteristicsSet imgCharacteristicsSet : IMAGE_CHARACTERISTICS) {
            isValid = imgCharacteristicsSet.has(
                imageDescriptor.getStoredWidth(),
                imageDescriptor.getStoredHeight(),
                imageDescriptor.getColor(),
                imageDescriptor.getPixelBitDepth(),
                imageDescriptor.getFrameLayoutType(),
                imageDescriptor.getSampleRate(),
                imageDescriptor.getSampling(),
                imageDescriptor.getQuantization(),
                imageDescriptor.getColorModel()
            );

            if (isValid)
                break;
        }

        if (!isValid) {
            logger.addError(
                IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                String.format(
                "Invalid image characteristics per %s",
                APP_STRING
                )
            );
        }
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE;
    }

}
