package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel.J2KHeaderParameters;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.JPEG2000;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;

/**
 * A class that models Composition with Application 2Extended constraints from
 * 2067-21 specification
 */
public class Application2E2021 extends AbstractApplicationComposition {
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

    private static boolean isValidHT(CompositionImageEssenceDescriptorModel imageDescriptor) {
        J2KHeaderParameters p = imageDescriptor.getJ2KHeaderParameters();

        if (p.xosiz != 0 || p.yosiz != 0 || p.xtosiz != 0 || p.ytosiz != 0)
            return false;

        return true;
    }

    public static boolean isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor) {
        UL essenceCoding = imageDescriptor.getPictureEssenceCodingUL();
        Integer width = imageDescriptor.getStoredWidth();
        Integer height = imageDescriptor.getStoredHeight();

        if (JPEG2000.isAPP2HT(essenceCoding))
            return isValidHT(imageDescriptor);

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
        if (!isValidJ2KProfile(imageDescriptor)) {
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
