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

import static com.netflix.imflibrary.Colorimetry.*;
import static com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.FrameLayoutType;

/**
 * A class that models Composition with Application 2 constraints from 2067-20 specification
 */
public class ApplicationUnsupportedComposition extends AbstractApplicationComposition {
    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
        add("SignalStandard");
        add("ActiveFormatDescriptor");
        add("VideoLineMap");
        add("AlphaTransparency");
        add("PixelLayout");
    }});

    public ApplicationUnsupportedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {

        super(imfCompositionPlaylistType, ignoreSet);
        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                String.format("Application ID %s is not fully supported", imfCompositionPlaylistType.getApplicationIdentification()));
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_UNSUPPORTED_COMPOSITION_TYPE;
    }

}
