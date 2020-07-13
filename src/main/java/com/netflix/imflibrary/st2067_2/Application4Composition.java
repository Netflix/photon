/*
 *
 * Copyright 2019 Eclair Media SAS.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.Colorimetry.ColorModel;
import com.netflix.imflibrary.Colorimetry.Quantization;
import com.netflix.imflibrary.Colorimetry.Sampling;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0422.JP2KContentKind;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.utils.Fraction;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.netflix.imflibrary.Colorimetry.CodingEquation;
import static com.netflix.imflibrary.Colorimetry.ColorModel;
import static com.netflix.imflibrary.Colorimetry.ColorPrimaries;
import static com.netflix.imflibrary.Colorimetry.Quantization;
import static com.netflix.imflibrary.Colorimetry.Sampling;
import static com.netflix.imflibrary.Colorimetry.TransferCharacteristic;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;


/**
 * A class that models Composition with Application 4 constraints from 2067-40 specification
 */
public class Application4Composition extends AbstractApplicationComposition {
    public static final Integer MAX_XYZ_IMAGE_FRAME_WIDTH = 8192;
    public static final Integer MAX_XYZ_IMAGE_FRAME_HEIGHT = 6224;
    public static final Set<Fraction>sampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(16)); add(new Fraction(200, 11)); add(new Fraction(20)); add(new Fraction(240, 11)); add(new Fraction(24)); add(new Fraction(25));
        add(new Fraction(30)); add(new Fraction(48)); add(new Fraction(50)); add(new Fraction(60)); add(new Fraction(100)); add(new Fraction(120));}});
    public static final Map<Colorimetry, Set<Integer>>colorToBitDepthMap = Collections.unmodifiableMap(new HashMap<Colorimetry, Set<Integer>>() {{
        put(Colorimetry.Unknown, new HashSet<Integer>(){{ }});
        put(Colorimetry.Color_App4_Lin, new HashSet<Integer>(){{ add(16); }});
    }});
    public static final Set<Integer>bitDepthsSupported = Collections.unmodifiableSet(new HashSet<Integer>() {{
        add(16); }});

    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
        add("SignalStandard");
        add("ActiveFormatDescriptor");
        add("VideoLineMap");
        add("AlphaTransparency");
        add("StoredF2Offset");
        add("SampledYOffset");
        add("Image Alignment Offset");
        add("Image Start Offset");
        add("Image End Offset");
        add("FieldDominance");
        add("Coding Equations");
        add("Alternative Center Cuts");
    }});

    public Application4Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public Application4Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);

        try
        {
            // Validate CPL Constraints
            validateCompositionPlaylist(imfCompositionPlaylistType, ApplicationCompositionType.APPLICATION_4_COMPOSITION_TYPE, imfErrorLogger);
            // Validate Image Essence Constraints
            CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = getCompositionImageEssenceDescriptorModel();

            if (imageEssenceDescriptorModel != null)
            {
                Application4Composition.validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_4_COMPOSITION_TYPE, imfErrorLogger);
                Application4Composition.validateImageCharacteristics(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_4_COMPOSITION_TYPE, imfErrorLogger);
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in APPLICATION_4_COMPOSITION_TYPE: %s ", e.getMessage()));
        }
    }

    public static void validateCompositionPlaylist(IMFCompositionPlaylistType imfCompositionPlaylistType,
                                                   ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                   IMFErrorLogger imfErrorLogger)
    {
        //ContentKind
        String contentKind = imfCompositionPlaylistType.getContentKind();
        if(contentKind == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("ContentKind shall be present as per %s", applicationCompositionType.toString()));
        }

        //Creator
        String creator = imfCompositionPlaylistType.getCreator();
        if(creator == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Creator shall be present as per %s", applicationCompositionType.toString()));
        }

        //Issuer
        String issuer = imfCompositionPlaylistType.getIssuer();
        if(issuer == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Issuer shall be present as per %s", applicationCompositionType.toString()));
        }

        //ContentVersionList
        if( imfCompositionPlaylistType.getContentVersionList().getContentVersions().size() == 0) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("At least one ContentVersion shall be present as per %s", applicationCompositionType.toString()));
        }

    }


    public static void validateGenericPictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                               ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                               IMFErrorLogger imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( colorModel.equals(ColorModel.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
            return;
        }

        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if ((storedWidth <= 0) || (storedHeight <= 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedWidth(%d) or storedHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, storedHeight, applicationCompositionType.toString()));
        }

        Integer sampleWidth = imageEssenceDescriptorModel.getSampleWidth();
        Integer sampleHeight = imageEssenceDescriptorModel.getSampleHeight();
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) ||
                (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sampleWidth(%d) or sampleHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), sampleWidth != null ? sampleWidth : 0, sampleHeight != null ? sampleHeight : 0,
                            applicationCompositionType.toString()));
        }

        if( imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid StoredOffset as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        ColorPrimaries colorPrimaries = imageEssenceDescriptorModel.getColorPrimaries();
        if(colorPrimaries.equals(ColorPrimaries.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        TransferCharacteristic transferCharacteristic = imageEssenceDescriptorModel.getTransferCharacteristic();
        if(transferCharacteristic.equals(TransferCharacteristic.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid TransferCharacteristic as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(color.equals(Colorimetry.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s) combination as per %s",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), transferCharacteristic.name(), applicationCompositionType.toString()));
        }

        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        UL essenceContainerFormatUL = imageEssenceDescriptorModel.getEssenceContainerFormatUL();
        if(essenceContainerFormatUL != null) {
            JP2KContentKind contentKind = JP2KContentKind.valueOf(essenceContainerFormatUL.getULAsBytes()[14]);
            if ((frameLayoutType.equals(FrameLayoutType.FullFrame) && !contentKind.equals(JP2KContentKind.P1))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid JPEG-2000 ContentKind (%s) indicated by the ContainerFormat as per %s",
                                imageEssenceDescriptorID.toString(), contentKind, applicationCompositionType.toString()));
            }
        }
   }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                    ApplicationCompositionType applicationCompositionType,
                                                    IMFErrorLogger imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( !colorModel.equals(ColorModel.RGB) ) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("XYZ EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
            return;
        }

        //storedWidth
        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        if ((storedWidth > MAX_XYZ_IMAGE_FRAME_WIDTH)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid StoredWidth(%d) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, colorModel.name(), applicationCompositionType.toString()));
        }

        //storedHeight
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if ((storedHeight > MAX_XYZ_IMAGE_FRAME_HEIGHT)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedHeight(%d) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), storedHeight, colorModel.name(), applicationCompositionType.toString()));
        }

        //PixelBitDepth
        Integer pixelBitDepth = imageEssenceDescriptorModel.getPixelBitDepth();
        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if( !bitDepthsSupported.contains(pixelBitDepth) || !colorToBitDepthMap.get(color).contains(pixelBitDepth)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid PixelBitDepth(%d) for Color(%s) as per %s",
                            imageEssenceDescriptorID.toString(), pixelBitDepth, color.name(), applicationCompositionType.toString()));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), applicationCompositionType.toString()));
        }

        //SampleRate
        Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
        Set<Fraction> frameRateSupported = sampleRateSupported;
        if (!frameRateSupported.contains(sampleRate)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid SampleRate(%s) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), sampleRate.toString(), colorModel.name(), applicationCompositionType.toString()));
        }

        //Sampling
        Sampling sampling = imageEssenceDescriptorModel.getSampling();
        if((!sampling.equals(Sampling.Sampling444))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Sampling(%s) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), sampling.name(), colorModel.name(), applicationCompositionType.toString()));
        }

        //Quantization
        Quantization quantization = imageEssenceDescriptorModel.getQuantization();
        if(!(quantization.equals(Quantization.QE2))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid Quantization(%s) for ColorModel(%s) as per %s",
                            imageEssenceDescriptorID.toString(), quantization.name(), colorModel.name(), applicationCompositionType.toString()));
        }
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_4_COMPOSITION_TYPE;
    }

}
