/*
 *
 * Copyright 2015 Media-IO, France.
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
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.utils.Fraction;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
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
 * A class that models Composition with Application TSP 2121 constraints from TSP 2121-1 specification
 */
public class ApplicationTsp2121Composition extends AbstractApplicationComposition {
    public static final Set<Fraction>resolutionsSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(1920, 1080)); add(new Fraction(3840, 2160)); }});
    public static final Set<Fraction>sampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24)); add(new Fraction(25)); add(new Fraction(30)); add(new Fraction(50)); add(new Fraction(60));
        add(new Fraction(24000, 1001)); add(new Fraction(30000, 1001)); add(new Fraction(60000, 1001)); }});
    public static final Set<Integer>bitDepthsSupported = Collections.unmodifiableSet(new HashSet<Integer>() {{
        add(10); add(12); }});
    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
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

    public ApplicationTsp2121Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public ApplicationTsp2121Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);

        try
        {
            CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = getCompositionImageEssenceDescriptorModel();

            if (imageEssenceDescriptorModel != null)
            {
                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                ApplicationTsp2121Composition.validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_TSP_2121_COMPOSITION_TYPE,
                        imfErrorLogger);
                ApplicationTsp2121Composition.validateImageCharacteristics(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_TSP_2121_COMPOSITION_TYPE,
                        imfErrorLogger);
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in APPLICATION_TSP_2121_COMPOSITION_TYPE: %s ", e.getMessage()));
        }
    }

    public static void validateGenericPictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                               ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                               IMFErrorLogger
            imfErrorLogger)
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

        CodingEquation codingEquation = imageEssenceDescriptorModel.getCodingEquation();
        if(codingEquation.equals(CodingEquation.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid CodingEquation as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(color.equals(Colorimetry.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s)-CodingEquation(%s) combination as per %s",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), transferCharacteristic.name(), codingEquation.name(), applicationCompositionType.toString()));
        }

        UL essenceContainerFormatUL = imageEssenceDescriptorModel.getEssenceContainerFormatUL();
        if(essenceContainerFormatUL != null) {
            UL proresUL = UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.0D.01.03.01.02.1C.01.00");
            if (!essenceContainerFormatUL.equals(proresUL)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid UL %s indicated by the ContainerFormat as per %s",
                                imageEssenceDescriptorID.toString(), essenceContainerFormatUL.toString(), applicationCompositionType.toString()));
            }
        }
   }

    public static void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                    ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                    IMFErrorLogger imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( !colorModel.equals(ColorModel.RGB) && !colorModel.equals(ColorModel.YUV)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
            return;
        }

        //storedWidth
        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        //storedHeight
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if (!resolutionsSupported.contains(new Fraction(storedWidth, storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedHeight, applicationCompositionType.toString()));
        }

        //ComponentDepth
        Integer componentDepth = imageEssenceDescriptorModel.getComponentDepth();
        if (!bitDepthsSupported.contains(componentDepth)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid ComponentDepth(%d) as per %s",
                            imageEssenceDescriptorID.toString(), componentDepth, applicationCompositionType.toString()));
        }

        //PixelBitDepth
        Integer pixelBitDepth = imageEssenceDescriptorModel.getPixelBitDepth();
        if (!bitDepthsSupported.contains(pixelBitDepth)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid PixelBitDepth(%d) as per %s",
                            imageEssenceDescriptorID.toString(), pixelBitDepth, applicationCompositionType.toString()));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame) && !frameLayoutType.equals(FrameLayoutType.SeparateFields)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), applicationCompositionType.toString()));
        }
        else {
            //SampleRate
            Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
            if (!sampleRateSupported.contains(sampleRate)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has Invalid SampleRate(%s) for frame structure %s as per %s",
                                imageEssenceDescriptorID.toString(), sampleRate.toString(), frameLayoutType.name(), applicationCompositionType.toString()));
            }
        }

        //Sampling
        Sampling sampling = imageEssenceDescriptorModel.getSampling();
        if(frameLayoutType.equals(FrameLayoutType.SeparateFields) && !sampling.equals(Sampling.Sampling422) ) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid combination of FrameLayOut(%s) for Sampling(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), sampling.name(), applicationCompositionType.toString()));
        }

        //Quantization
        Quantization quantization = imageEssenceDescriptorModel.getQuantization();
        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if((sampling.equals(Sampling.Sampling422) &&
                !(quantization.equals(Quantization.QE1) && colorModel.equals(ColorModel.YUV))) ||
                (quantization.equals(Quantization.QE2) &&
                        !(colorModel.equals(ColorModel.RGB) && color.equals(Colorimetry.Color3)))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid combination of quantization(%s)-Sampling(%s)-colorModel(%s)-color(%s) as per %s",
                            imageEssenceDescriptorID.toString(), quantization.name(), sampling.name(), colorModel.name(), color.name(), applicationCompositionType.toString()));
        }
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_TSP_2121_COMPOSITION_TYPE;
    }

}
