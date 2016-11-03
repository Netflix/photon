package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.Fraction;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.annotation.Nonnull;
import java.util.*;

import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import static com.netflix.imflibrary.st2067_2.Colorimetry.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.RGBAComponentType.*;

/**
 * A class that models Composition with Application 2 constraints from 2067-20 specification
 */
public class Application2Composition extends AbstractApplicationComposition {
    public static final Integer MAX_IMAGE_FRAME_WIDTH = 1920;
    public static final Integer MAX_IMAGE_FRAME_HEIGHT = 1080;
    public static final Set<Fraction>progressiveSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24)); add(new Fraction(25)); add(new Fraction(30)); add(new Fraction(50)); add(new Fraction(60));
        add(new Fraction(24000, 1001)); add(new Fraction(30000, 1001)); add(new Fraction(60000, 1001)); }});
    public static final Set<Fraction>interlaceSampleRateSupported = Collections.unmodifiableSet(new HashSet<Fraction>() {{
        add(new Fraction(24)); add(new Fraction(30)); add(new Fraction(30000, 1001)); }});
    public static final Set<Integer>bitDepthsSupported = Collections.unmodifiableSet(new HashSet<Integer>() {{
        add(8); add(10); }});
    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
        add("SignalStandard");
        add("ActiveFormatDescriptor");
        add("VideoLineMap");
        add("AlphaTransparency");
        add("PixelLayout");
    }});

    public Application2Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {

        super(imfCompositionPlaylistType, ignoreSet);

            DOMNodeObjectModel imageEssencedescriptorDOMNode = this.getEssenceDescriptor(
                    this.getVideoVirtualTrack().getTrackResourceIds().iterator().next());


            if (imageEssencedescriptorDOMNode != null) {
                UUID imageEssencedescriptorID = this.getEssenceDescriptorListMap().entrySet().stream().filter(e -> e.getValue().equals(imageEssencedescriptorDOMNode)).map(e -> e.getKey()).findFirst()
                        .get();
                try {
                    CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = new CompositionImageEssenceDescriptorModel( imageEssencedescriptorID, imageEssencedescriptorDOMNode,
                            regXMLLibDictionary);
                    imfErrorLogger.addAllErrors(imageEssenceDescriptorModel.getErrors());
                    validateImageEssenceDescriptor(imageEssenceDescriptorModel, imfErrorLogger);
                } catch (Exception e) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("Exception %s in validating EssenceDescriptor with ID %s as per Application#2 specification", e.getMessage(), imageEssencedescriptorID.toString()));
                }
            }
    }

    public static void validateImageEssenceDescriptor(CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel, IMFErrorLogger
            imfErrorLogger)
    {
        ColorSpace colorSpace = imageEssenceDescriptorModel.getColorSpace();
        Long storedWidth = imageEssenceDescriptorModel.getStoredWidth();
        Long storedHeight = imageEssenceDescriptorModel.getStoredHeight();
        UUID imageEssencedescriptorID = imageEssenceDescriptorModel.getImageEssencedescriptorID();

        if( colorSpace == null || (!colorSpace.equals(ColorSpace.RGB) && !colorSpace.equals(ColorSpace.YUV))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("EssenceDescriptor with ID %s invalid ColorSpace %s in Application#2E Composition",
                            imageEssencedescriptorID.toString(), colorSpace != null ? colorSpace.name() : ""));
        } else {
            //Dimension
            if ((storedWidth != null && storedWidth > MAX_IMAGE_FRAME_WIDTH) || (storedHeight != null && storedHeight > MAX_IMAGE_FRAME_HEIGHT)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                        IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("EssenceDescriptor with ID %s has invalid StoredWidth %d or StoredHeight %d as per Application#2 specification",
                                imageEssencedescriptorID.toString(), storedWidth != null ? storedWidth : 0,
                                storedHeight != null ? storedHeight : 0));
            }

            //FrameLayout
            Boolean isProgressive = true;
            FrameLayoutType frameLayoutType = imageEssenceDescriptorModel.getFrameLayoutType();
            if (frameLayoutType != null) {
                if (!frameLayoutType.equals(FrameLayoutType.FullFrame) && !frameLayoutType.equals(FrameLayoutType.SeperateFields)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s has invalid StoredWidth %d or StoredHeight %d as per Application#2 specification",
                                    imageEssencedescriptorID.toString(), storedWidth != null ? storedWidth : 0,
                                    storedHeight != null ? storedHeight : 0));
                }
                isProgressive = frameLayoutType.equals(FrameLayoutType.FullFrame);
            }

            //SampleRate
            Fraction sampleRate = imageEssenceDescriptorModel.getSampleRate();
            if (sampleRate != null) {
                Set<Fraction> frameRateSupported = isProgressive ? progressiveSampleRateSupported : interlaceSampleRateSupported;
                if (!frameRateSupported.contains(sampleRate)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("EssenceDescriptor with ID %s has Invalid SampleRate %s for %s frame structure as per Application#2 specification",
                                    imageEssencedescriptorID.toString(), sampleRate.toString(), isProgressive ? "progressive" : "interlace"));
                }
            }
        }
    }


}
