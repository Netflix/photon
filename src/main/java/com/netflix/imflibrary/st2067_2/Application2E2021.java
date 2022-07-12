package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.JPEG2000;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;

/**
 * A class that models Composition with Application 2Extended constraints from
 * 2067-21 specification
 */
public class Application2E2021 extends AbstractApplicationComposition {
    private static String APP_STRING = ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE.toString();


    public static final String SCHEMA_URI_APP2E_2021 = "http://www.smpte-ra.org/ns/2067-21/2021";

    public static final Set<Fraction> FPS_UHD = Collections.unmodifiableSet(new HashSet<Fraction>(){{
        add(new Fraction(24));
        add(new Fraction(25));
        add(new Fraction(30));
        add(new Fraction(50));
        add(new Fraction(60));
        add(new Fraction(120));
        add(new Fraction(24000,1001));
        add(new Fraction(30000,1001));
        add(new Fraction(60000,1001));
    }});

    public static final Set<Fraction> FPS_RGB = Collections.unmodifiableSet(new HashSet<Fraction>(){{
        add(new Fraction(24));
        add(new Fraction(25));
        add(new Fraction(30));
        add(new Fraction(50));
        add(new Fraction(60));
        add(new Fraction(120));
        add(new Fraction(24000,1001));
        add(new Fraction(30000,1001));
        add(new Fraction(60000,1001));
        add(new Fraction(120));
    }});

    public static final Set<Fraction> FPS_HD = Collections.unmodifiableSet(new HashSet<Fraction>(){{
        add(new Fraction(25));
        add(new Fraction(30));
        add(new Fraction(30000,1001));
    }});

    public static final Map<Colorimetry, Set<Integer>> COLOR_TO_DEPTH_HD = Collections.unmodifiableMap(new HashMap<Colorimetry,Set<Integer>>(){{
        put(Colorimetry.Color1,new HashSet<Integer>(){{add(8);add(10);}});
        put(Colorimetry.Color2,new HashSet<Integer>(){{add(8);add(10);}});
        put(Colorimetry.Color3,new HashSet<Integer>(){{add(8);add(10);}});}});

    public static final Map<Colorimetry, Set<Integer>> COLOR_TO_DEPTH_YUV = Collections.unmodifiableMap(new HashMap<Colorimetry, Set<Integer>>() {{
        put(Colorimetry.Color3, new HashSet<Integer>(){{ add(8); add(10); add(12); add(16);}});
        put(Colorimetry.Color5, new HashSet<Integer>(){{ add(10); add(12); }});
        put(Colorimetry.Color7, new HashSet<Integer>(){{ add(10); add(12); add(16); }});
        put(Colorimetry.Color8, new HashSet<Integer>(){{ add(10); add(12); }});
    }});

    public static final Map<Colorimetry, Set<Integer>> COLOR_TO_DEPTH_XVYCC = Collections.unmodifiableMap(new HashMap<Colorimetry, Set<Integer>>() {{
        put(Colorimetry.Color4, new HashSet<Integer>(){{ add(8); add(10); }});
    }});

    public static final Map<Colorimetry, Set<Integer>> COLOR_TO_DEPTH_RGB = Collections.unmodifiableMap(new HashMap<Colorimetry, Set<Integer>>() {{
        put(Colorimetry.Color3, new HashSet<Integer>(){{ add(8); add(10); add(12); add(16);}});
        put(Colorimetry.Color5, new HashSet<Integer>(){{ add(10); add(12); }});
        put(Colorimetry.Color6, new HashSet<Integer>(){{ add(10); add(12); add(16);}});
        put(Colorimetry.Color7, new HashSet<Integer>(){{ add(10); add(12); add(16); }});
        put(Colorimetry.Color8, new HashSet<Integer>(){{ add(10); add(12); }});
    }});

    private static final Set<String> ignoreSet=Collections.unmodifiableSet(new HashSet<String>(){{add("SignalStandard");add("ActiveFormatDescriptor");add("VideoLineMap");add("AlphaTransparency");add("PixelLayout");add("ActiveHeight");add("ActiveWidth");add("ActiveXOffset");add("ActiveYOffset");}});

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

    public static void validateInterlacedImageCharacteristics(CompositionImageEssenceDescriptorModel imageDescriptor,
            IMFErrorLogger logger) {

        UUID imageDescriptorID = imageDescriptor.getImageEssencedescriptorID();

        // Components
        if (!ColorModel.YUV.equals(imageDescriptor.getColorModel())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Dimensions
        if (imageDescriptor.getStoredWidth() > 1920 || imageDescriptor.getStoredWidth() < 1
                || imageDescriptor.getStoredHeight() > 1080 || imageDescriptor.getStoredHeight() < 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid image width or height per %",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Sampling
        if (!Sampling.Sampling422.equals(imageDescriptor.getSampling())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid sampling as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Quantization
        if (!Quantization.QE1.equals(imageDescriptor.getQuantization())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid quantization as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Pixel Bit Depth and Colorimetry
        Colorimetry color = imageDescriptor.getColor();
        if (!COLOR_TO_DEPTH_HD.containsKey(color)
                || !COLOR_TO_DEPTH_HD.get(color).contains(imageDescriptor.getPixelBitDepth())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s invalid pixel bit depth and colorimetry as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Sample rate
        if (!FPS_HD.contains(imageDescriptor.getSampleRate())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sample rate fas per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

    }

    public static boolean isValidYUVColor(CompositionImageEssenceDescriptorModel imageDescriptor) {
        Sampling sampling = imageDescriptor.getSampling();
        Integer depth = imageDescriptor.getPixelBitDepth();
        Colorimetry color = imageDescriptor.getColor();


        if (Sampling.Sampling422.equals(sampling)) {
            return COLOR_TO_DEPTH_YUV.containsKey(color) && COLOR_TO_DEPTH_YUV.get(color).contains(depth);
        } else if (Sampling.Sampling444.equals(sampling)) {
            return COLOR_TO_DEPTH_XVYCC.containsKey(color) && COLOR_TO_DEPTH_XVYCC.get(color).contains(depth);
        }

        return false;
    }

    public static void validateYUVImageCharacteristics(CompositionImageEssenceDescriptorModel imageDescriptor,
            IMFErrorLogger logger) {
        UUID imageDescriptorID = imageDescriptor.getImageEssencedescriptorID();

        // Dimensions
        if (imageDescriptor.getStoredWidth() > 3840 || imageDescriptor.getStoredWidth() < 1
                || imageDescriptor.getStoredHeight() > 2160 || imageDescriptor.getStoredHeight() < 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid image width or height per %",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Sampling, colorimetry, color components and pixel depth
        if (! isValidYUVColor(imageDescriptor)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid combination of sampling, colorimetry, color components and pixel depth as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Quantization
        if (!Quantization.QE1.equals(imageDescriptor.getQuantization())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid quantization as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Sample rate
        if (!FPS_UHD.contains(imageDescriptor.getSampleRate())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sample rate fas per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }
    }

    public static boolean isValidRGBColor(CompositionImageEssenceDescriptorModel imageDescriptor) {
        Integer depth = imageDescriptor.getPixelBitDepth();
        Colorimetry color = imageDescriptor.getColor();
        Quantization quantization = imageDescriptor.getQuantization();
        Fraction fps = imageDescriptor.getSampleRate();


        if (Colorimetry.Color4.equals(color)) {
            return Quantization.QE1.equals(quantization) && FPS_UHD.contains(fps) && COLOR_TO_DEPTH_XVYCC.containsKey(color) && COLOR_TO_DEPTH_XVYCC.get(color).contains(depth);
        } else {
            return (Quantization.QE2.equals(quantization) || Quantization.QE1.equals(quantization)) &&
                FPS_RGB.contains(fps) && COLOR_TO_DEPTH_RGB.containsKey(color) && COLOR_TO_DEPTH_RGB.get(color).contains(depth);
        }
    }


    public static void validateRGBImageCharacteristics(CompositionImageEssenceDescriptorModel imageDescriptor,
            IMFErrorLogger logger) {
        UUID imageDescriptorID = imageDescriptor.getImageEssencedescriptorID();

        // Dimensions
        if (imageDescriptor.getStoredWidth() > 4096 || imageDescriptor.getStoredWidth() < 1
                || imageDescriptor.getStoredHeight() > 3112 || imageDescriptor.getStoredHeight() < 1) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid image width or height per %",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Sampling
        if (!Sampling.Sampling444.equals(imageDescriptor.getSampling())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid sampling and color components as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Components
        if (!ColorModel.RGB.equals(imageDescriptor.getColorModel())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid color components as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Quantization, frame rate, colorimetry and pixel depth
        if (! isValidRGBColor(imageDescriptor)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "EssenceDescriptor with ID %s has invalid combination of quantization, frame rate, colorimetry and pixel depth as per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }

        // Sample rate
        if (!FPS_RGB.contains(imageDescriptor.getSampleRate())) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sample rate fas per %s",
                            imageDescriptorID.toString(),
                            APP_STRING));
        }
    }

    public static boolean isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor) {
        UL essenceCoding = imageDescriptor.getPictureEssenceCodingUL();
        Integer width = imageDescriptor.getStoredWidth();
        Integer height = imageDescriptor.getStoredHeight();

        if (JPEG2000.isAPP2HT(essenceCoding))
            return true;

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

        FrameLayoutType frameLayout = imageDescriptor.getFrameLayoutType();
        ColorModel color = imageDescriptor.getColorModel();

        if (FrameLayoutType.SeparateFields.equals(frameLayout)) {
            validateInterlacedImageCharacteristics(imageDescriptor, logger);
        } else if (FrameLayoutType.FullFrame.equals(frameLayout) && ColorModel.YUV.equals(color)) {
            validateYUVImageCharacteristics(imageDescriptor, logger);
        } else if (FrameLayoutType.FullFrame.equals(frameLayout) && ColorModel.RGB.equals(color)) {
            validateRGBImageCharacteristics(imageDescriptor, logger);
        } else {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "Invalid frame structure");
        }

    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_2E2021_COMPOSITION_TYPE;
    }

}
