package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0422.JP2KContentKind;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

abstract public class IMFApp2EConstraintsValidator implements ConstraintsValidator {

    protected static class CharacteristicsSet {
        private Integer maxWidth;
        private Integer maxHeight;
        private HashSet<Colorimetry> colorSystems;
        private HashSet<Integer> bitDepths;
        private HashSet<GenericPictureEssenceDescriptor.FrameLayoutType> frameStructures;
        private HashSet<Fraction> frameRates;
        private HashSet<Colorimetry.Sampling> samplings;
        private HashSet<Colorimetry.Quantization> quantizations;
        private HashSet<Colorimetry.ColorModel> colorModels;
        /* Stereoscopic images are not supported */

        public CharacteristicsSet(Integer maxWidth,
                                  Integer maxHeight,
                                  List<Colorimetry> colorSystems,
                                  List<Integer> bitDepths,
                                  List<GenericPictureEssenceDescriptor.FrameLayoutType> frameStructures,
                                  List<Fraction> frameRates,
                                  List<Colorimetry.Sampling> samplings,
                                  List<Colorimetry.Quantization> quantizations,
                                  List<Colorimetry.ColorModel> colorModels) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.colorSystems = new HashSet<Colorimetry>(colorSystems);
            this.bitDepths = new HashSet<Integer>(bitDepths);
            this.frameStructures = new HashSet<GenericPictureEssenceDescriptor.FrameLayoutType>(frameStructures);
            this.frameRates = new HashSet<Fraction>(frameRates);
            this.samplings = new HashSet<Colorimetry.Sampling>(samplings);
            this.quantizations = new HashSet<Colorimetry.Quantization>(quantizations);
            this.colorModels = new HashSet<Colorimetry.ColorModel>(colorModels);
        }

        public boolean has(Integer width,
                           Integer height,
                           Colorimetry colorSystem,
                           Integer bitDepth,
                           GenericPictureEssenceDescriptor.FrameLayoutType frameStructure,
                           Fraction frameRate,
                           Colorimetry.Sampling sampling,
                           Colorimetry.Quantization quantization,
                           Colorimetry.ColorModel colorModel) {
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

    /*
    *
    */
    protected abstract IMFApp2E2021ConstraintsValidator.CharacteristicsSet[] getValidImageCharacteristicsSets();

    /*
     *
     */
    protected abstract boolean isValidJ2KProfile(CompositionImageEssenceDescriptorModel imageDescriptor, IMFErrorLogger logger);


    @Override
    public List<ErrorLogger.ErrorObject> validateTrackFileConstraints(IMFTrackFileReader imfTrackFileReader) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = imfTrackFileReader.getHeaderPartitionIMF(imfErrorLogger);
            //IMFConstraints.checkIMFCompliance();

            //IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
        } catch (IOException e) {
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(
                    IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Exception while retrieving Track File information for validation: %s", e)));

        }


        return List.of();
    }


    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(IMFCompositionPlaylist IMFCompositionPlaylist) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            CompositionImageEssenceDescriptorModel imageDescriptorModel = IMFCompositionPlaylist.getCompositionImageEssenceDescriptorModel();

            if (imageDescriptorModel == null) {
                imfErrorLogger.addError(
                        IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        "Unable to retrieve picture descriptor from CPL for validation.");

                return imfErrorLogger.getErrors();
            }

            imfErrorLogger.addAllErrors(imageDescriptorModel.getErrors());

            validateGenericPictureEssenceDescriptor(imageDescriptorModel, imfErrorLogger);

            validateImageCharacteristics(imageDescriptorModel, imfErrorLogger);


        } catch (Exception e) {
            imfErrorLogger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format(
                            "Exception in validating EssenceDescriptors per %s: %s ",
                            getConstraintsSpecification(),
                            e.getMessage()));
        }







        return List.of();
    }


    private void validateImageCharacteristics(CompositionImageEssenceDescriptorModel imageDescriptor,
                                             IMFErrorLogger logger) {

        // J2K profiles
        if (!isValidJ2KProfile(imageDescriptor, logger)) {
            logger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    "Invalid JPEG 2000 profile");
        }

        boolean isValid = false;

        for (IMFApp2E2021ConstraintsValidator.CharacteristicsSet imgCharacteristicsSet : getValidImageCharacteristicsSets()) {
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
                    "Invalid image characteristics per " + getConstraintsSpecification()
            );
        }
    }


    private void validateGenericPictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                               IMFErrorLogger imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        Colorimetry.ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( colorModel.equals(Colorimetry.ColorModel.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has Invalid color components as per %s",
                            imageEssenceDescriptorID.toString(), getConstraintsSpecification()));
            return;
        }

        Integer componentDepth = imageEssenceDescriptorModel.getComponentDepth();
        if (colorModel.equals(Colorimetry.ColorModel.YUV) && componentDepth == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s is missing component depth required per %s",
                            imageEssenceDescriptorID.toString(), getConstraintsSpecification()));
        }

        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if ((storedWidth <= 0) || (storedHeight <= 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedWidth(%d) or storedHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, storedHeight, getConstraintsSpecification()));
        }

        Integer sampleWidth = imageEssenceDescriptorModel.getSampleWidth();
        Integer sampleHeight = imageEssenceDescriptorModel.getSampleHeight();
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) ||
                (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sampleWidth(%d) or sampleHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), sampleWidth != null ? sampleWidth : 0, sampleHeight != null ? sampleHeight : 0,
                            getConstraintsSpecification()));
        }

        if( imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid StoredOffset as per %s",
                            imageEssenceDescriptorID.toString(), getConstraintsSpecification()));
        }

        Colorimetry.ColorPrimaries colorPrimaries = imageEssenceDescriptorModel.getColorPrimaries();
        if(colorPrimaries.equals(Colorimetry.ColorPrimaries.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries as per %s",
                            imageEssenceDescriptorID.toString(), getConstraintsSpecification()));
        }

        Colorimetry.TransferCharacteristic transferCharacteristic = imageEssenceDescriptorModel.getTransferCharacteristic();
        if(transferCharacteristic.equals(Colorimetry.TransferCharacteristic.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid TransferCharacteristic as per %s",
                            imageEssenceDescriptorID.toString(), getConstraintsSpecification()));
        }

        Colorimetry.CodingEquation codingEquation = imageEssenceDescriptorModel.getCodingEquation();
        if(codingEquation.equals(Colorimetry.CodingEquation.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid CodingEquation as per %s",
                            imageEssenceDescriptorID.toString(), getConstraintsSpecification()));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(color.equals(Colorimetry.Unknown)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s)-TransferCharacteristic(%s)-CodingEquation(%s) combination as per %s",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), transferCharacteristic.name(), codingEquation.name(), getConstraintsSpecification()));
        }

        GenericPictureEssenceDescriptor.FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        UL essenceContainerFormatUL = imageEssenceDescriptorModel.getEssenceContainerFormatUL();
        if(essenceContainerFormatUL != null) {
            JP2KContentKind contentKind = JP2KContentKind.valueOf(essenceContainerFormatUL.getULAsBytes()[14]);
            if ((frameLayoutType.equals(GenericPictureEssenceDescriptor.FrameLayoutType.FullFrame) && !contentKind.equals(JP2KContentKind.P1)) ||
                    (frameLayoutType.equals(GenericPictureEssenceDescriptor.FrameLayoutType.SeparateFields) && !contentKind.equals(JP2KContentKind.I1))) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid JPEG-2000 ContentKind (%s) indicated by the ContainerFormat as per %s",
                                imageEssenceDescriptorID.toString(), contentKind, getConstraintsSpecification()));
            }
        }
    }







}
