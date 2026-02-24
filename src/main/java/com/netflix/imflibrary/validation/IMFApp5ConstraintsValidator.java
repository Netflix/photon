package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;
import com.netflix.imflibrary.Colorimetry.*;
import jakarta.annotation.Nonnull;

import java.util.*;
import java.util.stream.Collectors;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.Null;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.componentSizeUL;

/**
 * Collection of properties and validations specific to ST 2067-50.
 */
public class IMFApp5ConstraintsValidator implements ConstraintsValidator {

    static final String CONSTRAINTS_SPEC = "SMPTE ST 2067-50:2018 IMF Application #5";

    private static final Set<String> acesPictureSubDescriptorHomogeneitySelectionSet = Set.of("ACESAuthoringInformation");

    @Override
    public String getConstraintsSpecification() {
        return CONSTRAINTS_SPEC;
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        List<CompositionImageEssenceDescriptorModel> imageDescriptorModels = imfCompositionPlaylist.getCompositionImageEssenceDescriptorModels();

        if (imageDescriptorModels.isEmpty()) {
            imfErrorLogger.addError(
                    IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    "Unable to retrieve picture descriptor from CPL for validation.");

            return imfErrorLogger.getErrors();
        }

        RegXMLLibDictionary regXMLLibDictionary = new RegXMLLibDictionary();

        // validate each image descriptor individually
        imageDescriptorModels.forEach(imageDescriptorModel -> {
            imfErrorLogger.addAllErrors(imageDescriptorModel.getErrors());
            validatePictureEssenceDescriptor(imageDescriptorModel, imfErrorLogger);
            parseApp5PixelLayout(imageDescriptorModel.getImageEssencedescriptorID(), imageDescriptorModel.getImageEssencedescriptorDOMNode(), regXMLLibDictionary, imfErrorLogger);
            parseApp5SubDescriptors(imageDescriptorModel.getImageEssencedescriptorID(), imageDescriptorModel.getImageEssencedescriptorDOMNode(), regXMLLibDictionary, imfErrorLogger);
        });

        // validate descriptor homogeneity
        List<DOMNodeObjectModel> refAcesPictureSubDescriptors = new ArrayList<>();
        int indexFirstAcesPictureSubdescriptor = 0;
        boolean areAcesPictureSubDecriptorsHomogeneous = true;
        List<String> inhomogeneousEssenceDescriptorIds = new ArrayList<>();
        do {  // Find first appearance of an ACESPictureSubDescriptor, if any
            DOMNodeObjectModel subDescriptors = imageDescriptorModels.get(indexFirstAcesPictureSubdescriptor).getImageEssencedescriptorDOMNode().getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
            List<DOMNodeObjectModel> acesPictureSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorUL));
            for (DOMNodeObjectModel desc :  acesPictureSubDescriptors) {
                DOMNodeObjectModel refAcesPictureSubDescriptor = DOMNodeObjectModel.createDOMNodeObjectModelSelectionSet(desc, acesPictureSubDescriptorHomogeneitySelectionSet);
                refAcesPictureSubDescriptors.add(refAcesPictureSubDescriptor);
            }
            indexFirstAcesPictureSubdescriptor++;
        } while (refAcesPictureSubDescriptors.isEmpty() && (indexFirstAcesPictureSubdescriptor < imageDescriptorModels.size()));

        if ((indexFirstAcesPictureSubdescriptor == 1) && (!refAcesPictureSubDescriptors.isEmpty())) { // ACESPicture SubDescriptor(s) present in first resource
            for (int i = 1; i < imageDescriptorModels.size(); i++) {
                DOMNodeObjectModel subDescriptors = imageDescriptorModels.get(i).getImageEssencedescriptorDOMNode().getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
                List<DOMNodeObjectModel> other = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorUL));
                DOMNodeObjectModel refAcesPictureSubDescriptor = DOMNodeObjectModel.createDOMNodeObjectModelSelectionSet(imageDescriptorModels.get(i).getImageEssencedescriptorDOMNode(), acesPictureSubDescriptorHomogeneitySelectionSet);
                if (other.size() != refAcesPictureSubDescriptors.size()) { // Number of ACESPictureSubDescriptors is different
                    areAcesPictureSubDecriptorsHomogeneous = false;
                    inhomogeneousEssenceDescriptorIds.add(imageDescriptorModels.get(i).getImageEssencedescriptorDOMNode().getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString());
                } else {
                    for (DOMNodeObjectModel desc : other) {
                        DOMNodeObjectModel selectOther = DOMNodeObjectModel.createDOMNodeObjectModelSelectionSet(desc, acesPictureSubDescriptorHomogeneitySelectionSet);
                        if (!refAcesPictureSubDescriptors.contains(selectOther)) { // Value of Field ACESAuthoringInformation is different
                            areAcesPictureSubDecriptorsHomogeneous = false;
                            inhomogeneousEssenceDescriptorIds.add(imageDescriptorModels.get(i).getImageEssencedescriptorDOMNode().getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString());
                        }
                    }
                }
            }
        } else if (indexFirstAcesPictureSubdescriptor > 1 )  { // Inhomogeneous: First subdescriptor does not contain an ACESPictureSubDescriptor, but others do
            areAcesPictureSubDecriptorsHomogeneous = false;
            DOMNodeObjectModel firstOccurence = imageDescriptorModels.get(indexFirstAcesPictureSubdescriptor-1).getImageEssencedescriptorDOMNode();
            if (firstOccurence != null) {
                inhomogeneousEssenceDescriptorIds.add(firstOccurence.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString());
            }
        }
        if (!areAcesPictureSubDecriptorsHomogeneous) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("ACESPictureSubDescriptors shall be homogeneous per Draft Academy Digital Source Master Specification S-2018-001, mismatch occurred in essence (sub)descriptor(s) %s.",
                            inhomogeneousEssenceDescriptorIds.toString()));
        }

        return imfErrorLogger.getErrors();
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {

        /*
            Core validation is covered in IMPValidator.validateEssencePartitions().
            App5 specific validation is currently not implemented, but descriptors are indirectly validated by validateCompositionConstraints().
         */

        return List.of();
    }


    private static void validatePictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                        IMFErrorLogger imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();
        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( !colorModel.equals(ColorModel.RGB)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall be an RGBA descriptor per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
            return;
        }

        Integer storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Integer storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        if ((storedWidth <= 0) || (storedHeight <= 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid storedWidth(%d) or storedHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), storedWidth, storedHeight, CONSTRAINTS_SPEC));
        }

        Integer sampleWidth = imageEssenceDescriptorModel.getSampleWidth();
        Integer sampleHeight = imageEssenceDescriptorModel.getSampleHeight();
        if ((sampleWidth != null && !sampleWidth.equals(storedWidth)) ||
                (sampleHeight != null && !sampleHeight.equals(storedHeight))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid sampleWidth(%d) or sampleHeight(%d) as per %s",
                            imageEssenceDescriptorID.toString(), sampleWidth != null ? sampleWidth : 0, sampleHeight != null ? sampleHeight : 0,
                            CONSTRAINTS_SPEC));
        }

        Integer displayWidth = imageEssenceDescriptorModel.getDisplayWidth();
        Integer displayHeight = imageEssenceDescriptorModel.getDisplayHeight();
        if ((displayWidth <= 0) || (displayHeight <= 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid displayWidth(%d) or displayHeight(%d) item as per %s",
                            imageEssenceDescriptorID.toString(), displayWidth, displayHeight,CONSTRAINTS_SPEC));
        }

        Integer displayXOffset = imageEssenceDescriptorModel.getDisplayXOffset();
        Integer displayYOffset = imageEssenceDescriptorModel.getDisplayYOffset();
        if ((displayXOffset == null) || (displayYOffset == null)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has no displayXOffset or displayYOffset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        Fraction imageAspectRatio = imageEssenceDescriptorModel.getImageAspectRatio();
        if( imageAspectRatio == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall have an Aspect Ratio item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        } //TODO: Test if imageAspectRatio == roundToIntegralTiesToAway(DisplayWidth*pixelAspectRatio) / DisplayHeight

        ColorPrimaries colorPrimaries = imageEssenceDescriptorModel.getColorPrimaries();
        if(!colorPrimaries.equals(ColorPrimaries.ACES)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        TransferCharacteristic transferCharacteristic = imageEssenceDescriptorModel.getTransferCharacteristic();
        if(!transferCharacteristic.equals(TransferCharacteristic.Linear)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid TransferCharacteristic as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(!color.equals(Colorimetry.Color_App5_AP0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s) as per %s",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), CONSTRAINTS_SPEC));
        }

        UL essenceContainerFormatUL = imageEssenceDescriptorModel.getEssenceContainerFormatUL();
        UL MXFGCFrameWrappedACESPictures = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.0d010301.02190100"); // MXF-GC Frame-wrapped ACES Pictures per 2065-5
        if(essenceContainerFormatUL == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s does not contain a ContainerFormat as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        } else  {
            if (!essenceContainerFormatUL.equalsIgnoreVersion(MXFGCFrameWrappedACESPictures)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid ContainerFormat(%s) as per %s",
                                imageEssenceDescriptorID.toString(), essenceContainerFormatUL.toString(), CONSTRAINTS_SPEC));
            }
        }
        Integer offset = imageEssenceDescriptorModel.getSampledXOffset();
        if((offset != null) && (offset != 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid SampledXOffset (%d) as per %s",
                            imageEssenceDescriptorID.toString(), offset, CONSTRAINTS_SPEC));
        }

        offset = imageEssenceDescriptorModel.getSampledYOffset();
        if((offset != null) && (offset != 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has an invalid SampledYOffset (%d) item as per %s",
                            imageEssenceDescriptorID.toString(), offset, CONSTRAINTS_SPEC));
        }

        //FrameLayout
        FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
        if (!frameLayoutType.equals(FrameLayoutType.FullFrame)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid FrameLayout(%s) as per %s",
                            imageEssenceDescriptorID.toString(), frameLayoutType.name(), CONSTRAINTS_SPEC));
        }

        //SampleRate
        Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
        if (sampleRate.equals(new Fraction(0))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has no SampleRate per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        //The following items shall not be present per 2065-5 Table 10
        if(imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a StoredF2Offset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getDisplayF2Offset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a DisplayF2Offset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getActiveFormatDescriptor() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Active Format Descriptor item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getAlphaTransparency() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Alpha Transparency as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getImageAlignmentOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Image Alignment Offset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }
        if(imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a StoredF2Offset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getImageStartOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Image Start Offset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getImageEndOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Image End Offset item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getFieldDominance() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Field Dominance item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getCodingEquations() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Coding Equations item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getComponentMaxRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Component Max Ref item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getComponentMinRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Component Min Ref item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getAlphaMaxRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Alpha Max Ref item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getAlphaMinRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Alpha Min Ref item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getPalette() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Palette item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }

        if(imageEssenceDescriptorModel.getPaletteLayout() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Palette Layout item as per %s",
                            imageEssenceDescriptorID.toString(), CONSTRAINTS_SPEC));
        }
    }










    // FROM CompositionImageEssenceDescriptorModel.java:



    private static void parseApp5SubDescriptors(UUID imageEssencedescriptorID,
                                                DOMNodeObjectModel imageEssencedescriptorDOMNode,
                                         RegXMLLibDictionary regXMLLibDictionary,
                                         IMFErrorLogger imfErrorLogger) {
        DOMNodeObjectModel subDescriptors = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
        if (subDescriptors != null) {
            List<DOMNodeObjectModel> acesPictureSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorUL));
            List<DOMNodeObjectModel> targetFrameSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(targetFrameSubDescriptorUL));
            List<DOMNodeObjectModel> containerConstraintsSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(containerConstraintsSubDescriptorUL));
            if (!acesPictureSubDescriptors.isEmpty()) {
                for (DOMNodeObjectModel domNodeObjectModel : acesPictureSubDescriptors) {
                    String authoring_information = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(acesAuthoringInformationUL));
                    if ((authoring_information == null) || authoring_information.isEmpty()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                                String.format("ACES Picture SubDescriptor (ID %s): Optional item ACES Authoring Information is not present or empty", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString()));
                    }
                    DOMNodeObjectModel  primaries = domNodeObjectModel.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayPrimariesUL));
                    DOMNodeObjectModel  whitePoint = domNodeObjectModel.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayWhitePointChromaticityUL));
                    Integer maxLum = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayDisplayMaximumLuminanceUL));
                    Integer minLum = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(acesMasteringDisplayDisplayMinimumLuminanceUL));
                    if (!(((primaries == null) && (whitePoint == null) && (maxLum == null) && (minLum == null))
                            || ((primaries != null) && (whitePoint != null) && (maxLum != null) && (minLum != null)))) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("ACES Picture SubDescriptor (ID %s) shall have either all or none of the following elements: Mastering Display Primaries, White Point, Maximum Luminance, Minimum Luminance", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString()));
                    }
                }
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("INFO (can be ignored): EssenceDescriptor with ID %s: No ACESPictureSubDescriptor found", imageEssencedescriptorID.toString()));
            }
            if (!targetFrameSubDescriptors.isEmpty()) {
                for (DOMNodeObjectModel domNodeObjectModel : targetFrameSubDescriptors) {
                    String missing_items = "";
                    Set<UUID> targetFrameAncillaryResourceID = domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(targetFrameAncillaryResourceIDUL));
                    // Check for missing required items
                    if (targetFrameAncillaryResourceID.isEmpty()) {
                        missing_items += "TargetFrameAncillaryResourceID, ";
                    } else {
                        //TODO Check it targetFrameAncillaryResourceID belongs to an existing GSP
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                                String.format("INFO (can be ignored): Target FrameSubDescriptor (ID %s) references an Ancillary Resource (ID %s), but Ancillary Resources cannot be checked yet",
                                        domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), targetFrameAncillaryResourceID.toString()));
                    }
                    String media_type = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getSymbolNameFromURN(mediaTypeUL));
                    if (media_type == null) {
                        missing_items += "MediaType, ";
                    }
                    Long index = domNodeObjectModel.getFieldAsLong(regXMLLibDictionary.getSymbolNameFromURN(targetFrameIndexUL));
                    if (index == null) {
                        missing_items += "TargetFrameIndex, ";
                    }
                    UL transfer = domNodeObjectModel.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(targetFrameTransferCharacteristicUL));
                    if (transfer == null) {
                        missing_items += "TargetFrameTransferCharacteristic, ";
                    }
                    UL color = domNodeObjectModel.getFieldAsUL(regXMLLibDictionary.getSymbolNameFromURN(targetFrameColorPrimariesUL));
                    if (color == null) {
                        missing_items += "TargetFrameColorPrimaries, ";
                    }
                    Integer max_ref = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(targetFrameComponentMaxRefUL));
                    if (max_ref == null) {
                        missing_items += "TargetFrameComponentMaxRef, ";
                    }
                    Integer min_ref = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(targetFrameComponentMinRefUL));
                    if (min_ref == null) {
                        missing_items += "TargetFrameComponentMinRef, ";
                    }
                    Integer stream_id = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getSymbolNameFromURN(targetFrameEssenceStreamIDUL));
                    if (stream_id == null) {
                        missing_items += "TargetFrameEssenceStreamID";
                    }
                    if (!missing_items.isEmpty()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("Target FrameSubDescriptor (ID %s): is missing required item(s): %s", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), missing_items));
                    }

                    // Check if acesPictureSubDescriptorInstanceID references an existing ACESPictureSubDescriptor Instance ID
                    Set<UUID> acesPictureSubDescriptorInstanceID = domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorInstanceIDUL));
                    if (!acesPictureSubDescriptorInstanceID.isEmpty()) {
                        if (acesPictureSubDescriptors.isEmpty()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("Target FrameSubDescriptor (ID %s) references an ACESPictureSubDescriptorInstanceID (%s) but no ACESPictureSubDescriptor is present",
                                            domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), acesPictureSubDescriptorInstanceID.toString()));
                        } else {
                            if (acesPictureSubDescriptors.stream().noneMatch(e -> e.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).equals(acesPictureSubDescriptorInstanceID))) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("Target FrameSubDescriptor (ID %s) references an ACESPictureSubDescriptor but no ACESPictureSubDescriptor with Instance ID (%s) is present",
                                                domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), acesPictureSubDescriptorInstanceID.toString()));
                            }
                        }

                    }

                    if ((max_ref != null) && (min_ref != null)) {
                        if (max_ref <= min_ref) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("Target FrameSubDescriptor (ID %s): TargetFrameComponentMaxRef (%d) is less than or equal to TargetFrameComponentMaxRef (%d)", domNodeObjectModel.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString(), max_ref, min_ref));
                        }
                    }
                }
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("INFO (can be ignored): EssenceDescriptor with ID %s: No TargetFrameSubDescriptor found", imageEssencedescriptorID.toString()));
            }
            if (containerConstraintsSubDescriptors.isEmpty()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                        String.format("EssenceDescriptor with ID %s: A ContainerConstraintsSubDescriptor shall be present per ST 379-2, but is missing", imageEssencedescriptorID.toString()));
            }
        } else {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                    String.format("INFO (can be ignored): EssenceDescriptor with ID %s: No ACESPictureSubDescriptor and no TargetFrameSubDescriptor found", imageEssencedescriptorID.toString()));
        }
        return;
    }


    private void parseApp5PixelLayout(UUID imageEssencedescriptorID,
                                      DOMNodeObjectModel imageEssencedescriptorDOMNode,
                                      RegXMLLibDictionary regXMLLibDictionary,
                                      IMFErrorLogger imfErrorLogger) {
        DOMNodeObjectModel pixelLayout = imageEssencedescriptorDOMNode.getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(pixelLayoutUL));
        if (pixelLayout == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall have a Pixel Layout item",
                            imageEssencedescriptorID.toString()));
        } else {
            List<DOMNodeObjectModel> rgbaComponents = pixelLayout.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(rgbaComponentUL));
            if (rgbaComponents.size() == 0) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s is missing RGBAComponent in Pixel Layout",
                                imageEssencedescriptorID.toString()));
            } else {
                Map<RGBAComponentType, Integer> componentMap = new LinkedHashMap<>();
                List<RGBAComponentType> componentList = new ArrayList<>();
                for (DOMNodeObjectModel domNodeObjectModel : rgbaComponents) {
                    String code = domNodeObjectModel.getFieldAsString(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, codeUL));
                    if (code == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                String.format("EssenceDescriptor with ID %s has a Pixel Layout item with an RGBAComponent with missing Code",
                                        imageEssencedescriptorID.toString()));
                    } else {
                        RGBAComponentType codeValue = RGBAComponentType.valueOf(regXMLLibDictionary.getEnumerationValueFromName(rgbaComponentKindUL, code));
                        Integer pixelBitDepth = domNodeObjectModel.getFieldAsInteger(regXMLLibDictionary.getTypeFieldNameFromURN(rgbaComponentUL, componentSizeUL));
                        if (pixelBitDepth == null) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("EssenceDescriptor with ID %s has a Pixel Layout item with an RGBAComponent %s with missing Depth",
                                            imageEssencedescriptorID.toString(), code));
                        } else {
                            if ((codeValue.equals(Null) && pixelBitDepth != 0) ||
                                    (!codeValue.equals(Null) && (!pixelBitDepth.equals(253)))) { // In App#5, all components are of type HALF FLOAT
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("EssenceDescriptor with ID %s has a Pixel Layout item %s with invalid Depth value %d (expected depth value: 253)",
                                                imageEssencedescriptorID.toString(), code, pixelBitDepth));
                            }
                        }
                        if (componentMap.containsKey(codeValue)) {
                            Integer count = componentMap.get(codeValue);
                            componentMap.put(codeValue, count + 1);
                        } else {
                            componentMap.put(codeValue, 1);
                            componentList.add(codeValue);
                        }

                    }
                }
                boolean error = true;
                if (componentList.size() >= 4) { // ABGR or BGR per 2067-50 plus Null
                    if (componentList.get(0).toString().equals("Alpha")) { // ABGR per 2067-50
                        if (componentList.get(1).toString().equals("Blue") || componentList.get(2).toString().equals("Green") || componentList.get(3).toString().equals("Red") )
                            error = false;
                    } else if (componentList.get(0).toString().equals("Blue")) { // BGR per 2067-50
                        if (componentList.get(1).toString().equals("Green") || componentList.get(2).toString().equals("Red"))
                            error = false;
                    }
                }
                if (error) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s has incorrect PixelLayout %s",
                                    imageEssencedescriptorID.toString(), componentList.toString()));
                }
                componentMap.entrySet().stream().forEach(
                        e -> {
                            if (e.getKey().equals(RGBAComponentType.Null)) {
                                if (!(e.getValue().equals(5) || e.getValue().equals(4))) { // ABGR or BGR per 2067-50
                                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                            String.format("EssenceDescriptor with ID %s has invalid number of RGBAComponent %s in J2CLayout",
                                                    imageEssencedescriptorID.toString(), e.getKey()));
                                }
                            } else if (!e.getValue().equals(1)) {
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("EssenceDescriptor with ID %s has more than one RGBAComponent %s in Pixel Layout",
                                                imageEssencedescriptorID.toString(), e.getKey()));
                            }

                        }
                );
            }
        }
    }



}
