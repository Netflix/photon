package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.*;


import javax.annotation.Nonnull;

import org.w3c.dom.Node;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.Null;
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
    }});

    private static final Set<String> acesPictureSubDescriptorHomogeneitySelectionSet = Collections.unmodifiableSet(new HashSet<String>(){{
        add("ACESAuthoringInformation");
    }});
    
    public Application5Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public Application5Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);

        try {
            List<DOMNodeObjectModel> virtualTrackEssenceDescriptors = this.getEssenceDescriptors("RGBADescriptor").stream()
                    .distinct()
                    .collect(Collectors.toList());
            List<DOMNodeObjectModel> refAcesPictureSubDescriptors = new ArrayList<>();
            int indexFirstAcesPictureSubdescriptor = 0;
            boolean areAcesPictureSubDecriptorsHomogeneous = true;
            List<String> inhomogeneousEssenceDescriptorIds = new ArrayList<>();
            do {  // Find first appearance of an ACESPictureSubDescriptor, if any
                DOMNodeObjectModel subDescriptors = virtualTrackEssenceDescriptors.get(indexFirstAcesPictureSubdescriptor).getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
                List<DOMNodeObjectModel> acesPictureSubDescriptors = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorUL));
                for (DOMNodeObjectModel desc :  acesPictureSubDescriptors) {
                	DOMNodeObjectModel refAcesPictureSubDescriptor = desc.createDOMNodeObjectModelSelectionSet(desc, acesPictureSubDescriptorHomogeneitySelectionSet);
	                refAcesPictureSubDescriptors.add(refAcesPictureSubDescriptor);
                }
                indexFirstAcesPictureSubdescriptor++;
            } while (refAcesPictureSubDescriptors.isEmpty() && (indexFirstAcesPictureSubdescriptor < virtualTrackEssenceDescriptors.size()));

            if ((indexFirstAcesPictureSubdescriptor == 1) && (!refAcesPictureSubDescriptors.isEmpty())) { // ACESPicture SubDescriptor(s) present in first resource
                for (int i = 1; i < virtualTrackEssenceDescriptors.size(); i++) {
                    DOMNodeObjectModel subDescriptors = virtualTrackEssenceDescriptors.get(i).getDOMNode(regXMLLibDictionary.getSymbolNameFromURN(subdescriptorsUL));
                	List<DOMNodeObjectModel> other = subDescriptors.getDOMNodes(regXMLLibDictionary.getSymbolNameFromURN(acesPictureSubDescriptorUL));
                	DOMNodeObjectModel refAcesPictureSubDescriptor = virtualTrackEssenceDescriptors.get(i).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(i), acesPictureSubDescriptorHomogeneitySelectionSet);
                	if (other.size() != refAcesPictureSubDescriptors.size()) { // Number of ACESPictureSubDescriptors is different
                		areAcesPictureSubDecriptorsHomogeneous = false;
                		inhomogeneousEssenceDescriptorIds.add(virtualTrackEssenceDescriptors.get(i).getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString());
                	} else {
	                	for (DOMNodeObjectModel desc : other) {
	                    	DOMNodeObjectModel selectOther = desc.createDOMNodeObjectModelSelectionSet(desc, acesPictureSubDescriptorHomogeneitySelectionSet);
	                    	if (!refAcesPictureSubDescriptors.contains(selectOther)) { // Value of Field ACESAuthoringInformation is different
		                    	areAcesPictureSubDecriptorsHomogeneous = false;
		                		inhomogeneousEssenceDescriptorIds.add(virtualTrackEssenceDescriptors.get(i).getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString());
	                    	}
	                	}
                	}
                }
            } else if (indexFirstAcesPictureSubdescriptor > 1 )  { // Inhomogeneous: First subdescriptor does not contain an ACESPictureSubDescriptor, but others do
            	areAcesPictureSubDecriptorsHomogeneous = false;
            	DOMNodeObjectModel firstOccurence = virtualTrackEssenceDescriptors.get(indexFirstAcesPictureSubdescriptor-1);
            	if (firstOccurence != null) {
            		inhomogeneousEssenceDescriptorIds.add(firstOccurence.getFieldsAsUUID(regXMLLibDictionary.getSymbolNameFromURN(instanceID)).toString());
            	}
            }
            if (!areAcesPictureSubDecriptorsHomogeneous) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("ACESPictureSubDescriptors shall be homogeneous per Academy Digital Source Master Specification, mismatch occured in essence (sub)descriptor(s) %s.",
                        		inhomogeneousEssenceDescriptorIds.toString()));
            }

            // Validate all Essence Descriptors, because ACES sub-descriptors are not required to be homogeneous for all elements, in particular the TargetFrameSubDescriptors may differ per ST 2067-50.
            for(DOMNodeObjectModel imageEssencedescriptorDOMNode : virtualTrackEssenceDescriptors){
				CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = null;
				UUID imageEssenceDescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst()
				        .get();
				imageEssenceDescriptorModel = new CompositionImageEssenceDescriptorModel(imageEssenceDescriptorID, imageEssencedescriptorDOMNode,
				                regXMLLibDictionary);
	            if (imageEssenceDescriptorModel != null) {
	                imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
	                Application5Composition.validatePictureEssenceDescriptor(imageEssenceDescriptorModel, ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE,
	                        imfErrorLogger);
	            }
            }
        }
        catch (Exception e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Exception in validating EssenceDescriptors in APPLICATION_5_COMPOSITION_TYPE: %s ", e.getMessage()));
        }
    }

    public static void validatePictureEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel,
                                                               ApplicationCompositionFactory.ApplicationCompositionType applicationCompositionType,
                                                               IMFErrorLogger
            imfErrorLogger)
    {
        UUID imageEssenceDescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();
        ColorModel colorModel = imageEssenceDescriptorModel.getColorModel();
        if( !colorModel.equals(ColorModel.RGB)) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall be an RGBA descriptor per %s",
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

        Fraction imageAspectRatio = imageEssenceDescriptorModel.getImageAspectRatio();
        if( imageAspectRatio == null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall have an Aspect Ratio item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        } //TODO: Test if imageAspectRatio == roundToIntegralTiesToAway(DisplayWidth*pixelAspectRatio) / DisplayHeight

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

        if(imageEssenceDescriptorModel.getComponentMaxRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Component Max Ref item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        if(imageEssenceDescriptorModel.getComponentMinRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Component Min Ref item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        if(imageEssenceDescriptorModel.getAlphaMaxRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Alpha Max Ref item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        if(imageEssenceDescriptorModel.getAlphaMinRef() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain an Alpha Min Ref item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        if(imageEssenceDescriptorModel.getPalette() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Palette item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }

        if(imageEssenceDescriptorModel.getPaletteLayout() != null) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s shall not contain a Palette Layout item as per %s",
                            imageEssenceDescriptorID.toString(), applicationCompositionType.toString()));
        }
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE;
    }

}
