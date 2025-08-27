package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.JPEG2000;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;

import java.util.Arrays;
import java.util.List;


/**
 * Collection of properties and validations specific to ST 2067-21:2014.
 */
public class IMFApp2E2014ConstraintsValidator extends IMFApp2EConstraintsValidator {

    private static final String applicationCompositionType = "SMPTE ST 2067-21:2014 IMF Application #2E";

    private static final Fraction[] FPS_HD = {
            new Fraction(25),
            new Fraction(30),
            new Fraction(30000, 1001)
    };

    private static final Fraction[] FPS_UHD = {
            new Fraction(24),
            new Fraction(24000, 1001),
            new Fraction(25),
            new Fraction(30),
            new Fraction(30000, 1001)
    };

    /* Table 3 at SMPTE ST 2067-21:2014 */
    private static final CharacteristicsSet[] IMAGE_CHARACTERISTICS = {
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
                    Arrays.asList(Colorimetry.Color3, Colorimetry.Color4),
                    Arrays.asList(8, 10),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_UHD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            ),
            new CharacteristicsSet(
                    3840,
                    2160,
                    Arrays.asList(Colorimetry.Color5),
                    Arrays.asList(10),
                    Arrays.asList(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame),
                    Arrays.asList(FPS_UHD),
                    Arrays.asList(Colorimetry.Sampling.Sampling422),
                    Arrays.asList(Colorimetry.Quantization.QE1),
                    Arrays.asList(Colorimetry.ColorModel.YUV)
            )
    };

    @Override
    protected CharacteristicsSet[] getValidImageCharacteristicsSets() {
        return IMAGE_CHARACTERISTICS;
    }

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
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

        if (JPEG2000.isBroadcastProfile(essenceCoding)) {
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

}
