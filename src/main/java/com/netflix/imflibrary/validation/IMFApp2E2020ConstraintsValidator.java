package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.JPEG2000;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;

import java.util.Arrays;
import java.util.List;


public class IMFApp2E2020ConstraintsValidator extends IMFApp2EConstraintsValidator {

    private static final String applicationCompositionType = "IMF Application #2E 2020";

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


    @Override
    protected CharacteristicsSet[] getValidImageCharacteristicsSets() {
        return IMAGE_CHARACTERISTICS;
    }

    public boolean isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor,
                                            IMFErrorLogger logger) {
        UL essenceCoding = imageDescriptor.getPictureEssenceCodingUL();
        Integer width = imageDescriptor.getStoredWidth();
        Integer height = imageDescriptor.getStoredHeight();

        if (JPEG2000.isIMF4KProfile(essenceCoding))
            return width > 2048 && width <= 4096 && height > 0 && height <= 3112;

        if (JPEG2000.isIMF2KProfile(essenceCoding))
            return width > 0 && width <= 2048 && height > 0 && height <= 1556;

        if (JPEG2000.isBroadcastProfile(essenceCoding))
            return width > 0 && width <= 3840 && height > 0 && height <= 2160;

        return false;
    }


}
