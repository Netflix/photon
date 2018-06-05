package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.*;


import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.Colorimetry.*;

/**
 * A class that models Composition with Application 5 constraints from 2067-50 specification
 */
public class Application5Composition extends AbstractApplicationComposition {
    public static final Integer MAX_RGB_IMAGE_FRAME_WIDTH = Integer.MAX_VALUE; //TODO: 2067-50 specifies 2^32-1, would require using Long instead of Integer
    public static final Integer MAX_RGB_IMAGE_FRAME_HEIGHT = Integer.MAX_VALUE; //TODO: 2067-50 specifies 2^32-1, would require using Long instead of Integer
    public static final Map<Colorimetry, Set<Integer>>colorToBitDepthMap = Collections.unmodifiableMap(new HashMap<Colorimetry, Set<Integer>>() {{
        put(Colorimetry.Unknown, new HashSet<Integer>(){{ }});
        put(Colorimetry.Color_App5_AP0, new HashSet<Integer>(){{ add(16); }});
    }});
    public static final Set<Integer>bitDepthsSupported = Collections.unmodifiableSet(new HashSet<Integer>() {{
        add(16); }});

    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
   /*     add("SignalStandard"); //TODO SignalStandard shall not be present
        add("ActiveFormatDescriptor"); //TODO ActiveFormatDescriptor shall not be present
        add("VideoLineMap"); //TODO Shall be present and equal to {00h, 00h} per 2065-5
        add("AlphaTransparency");  //TODO AlphaTransparency shall not be present
        add("PixelLayout"); //TODO PixelLayout shall be present per 2065-5 */
    }});

    public Application5Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        super(imfCompositionPlaylistType, ignoreSet);

        try
        {
            CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = getCompositionImageEssenceDescriptorModel();

            if (imageEssenceDescriptorModel != null)
            {

                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                Application5Composition.validateGenericPictureEssenceDescriptor(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE,
                        imfErrorLogger);
                Application5Composition.validateImageCharacteristics(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE,
                        imfErrorLogger);
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in APPLICATION_5_COMPOSITION_TYPE: %s ", e.getMessage()));
        }
    }

    public static void validateGenericPictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                               ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                               IMFErrorLogger
            imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();
        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( !colorModel.equals(ColorModel.RGB)) {
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

        Integer displayWidth = imageEssenceDescriptorModel.getDisplayWidth();
        Integer displayHeight = imageEssenceDescriptorModel.getDisplayHeight();
        if ((displayWidth <= 0) || (displayHeight <= 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid displayWidth(%d) or displayHeight(%d) item as per %s",
                            imageEssenceDescriptorID.toString(), displayWidth, displayHeight, applicationCompositionType.toString()));
        }

        Integer displayXOffset = imageEssenceDescriptorModel.getDisplayXOffset();
        Integer displayYOffset = imageEssenceDescriptorModel.getDisplayYOffset();
        if ((displayXOffset == null) || (displayYOffset == null)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has no displayXOffset or displayYOffset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        if( imageEssenceDescriptorModel.getAspectRatio() == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall have an Aspect Ratio item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        ColorPrimaries colorPrimaries = imageEssenceDescriptorModel.getColorPrimaries();
        if(!colorPrimaries.equals(ColorPrimaries.ACES)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        TransferCharacteristic transferCharacteristic = imageEssenceDescriptorModel.getTransferCharacteristic();
        if(!transferCharacteristic.equals(TransferCharacteristic.Linear)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid TransferCharacteristic as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        Colorimetry color = imageEssenceDescriptorModel.getColor();
        if(!color.equals(Colorimetry.Color_App5_AP0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid ColorPrimaries(%s) as per %s",
                            imageEssenceDescriptorID.toString(), colorPrimaries.name(), applicationCompositionType.toString()));
        }

        UL essenceContainerFormatUL = imageEssenceDescriptorModel.getEssenceContainerFormatUL();
        UL MXFGCFrameWrappedACESPictures = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.0d010301.02190100"); // MXF-GC Frame-wrapped ACES Pictures per 2065-5
        if(essenceContainerFormatUL == null) {
	            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
	                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
	                    String.format("EssenceDescriptor with ID %s does not contain a ContainerFormat as per %s",
	                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        } else  {
	        if (!essenceContainerFormatUL.equals(MXFGCFrameWrappedACESPictures)) {
	            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
	                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
	                    String.format("EssenceDescriptor with ID %s has invalid ContainerFormat(%s) as per %s",
	                            imageEssenceDescriptorID.toString(), essenceContainerFormatUL.toString(), applicationCompositionType.toString()));
	        }
	    }
        Integer offset = imageEssenceDescriptorModel.getSampledXOffset();
        if((offset != null) && (offset != 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has invalid SampledXOffset (%d) as per %s",
                            imageEssenceDescriptorID.toString(), offset, applicationCompositionType.toString()));
        }

        offset = imageEssenceDescriptorModel.getSampledYOffset();
        if((offset != null) && (offset != 0)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has an invalid SampledYOffset (%d) item as per %s",
                            imageEssenceDescriptorID.toString(), offset, applicationCompositionType.toString()));
        }
        
        //The following items shall not be present per 2065-5 Table 10 
        if(imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a StoredF2Offset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getDisplayF2Offset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a DisplayF2Offset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getActiveFormatDescriptor() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Active Format Descriptor item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getAlphaTransparency() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Alpha Transparency as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getImageAlignmentOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Image Alignment Offset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        if(imageEssenceDescriptorModel.getStoredOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a StoredF2Offset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getImageStartOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Image Start Offset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getImageEndOffset() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Image End Offset item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getFieldDominance() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Field Dominance item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
        
        if(imageEssenceDescriptorModel.getCodingEquations() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Coding Equations item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
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
                    String.format("EssenceDescriptor with ID %s has Invalid color model as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
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
        if (sampleRate.equals(new Fraction(0))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s has no SampleRate per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE;
    }

}
