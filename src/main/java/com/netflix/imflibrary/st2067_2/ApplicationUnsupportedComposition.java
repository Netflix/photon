package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        add("ActiveHeight");
        add("ActiveWidth");
        add("ActiveXOffset");
        add("ActiveYOffset");
    }});

    public ApplicationUnsupportedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        this(imfCompositionPlaylistType, new HashSet<>());
    }

    public ApplicationUnsupportedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType, Set<String> homogeneitySelectionSet) {

        super(imfCompositionPlaylistType, ignoreSet, homogeneitySelectionSet);
        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                String.format("Application ID %s is not fully supported", imfCompositionPlaylistType.getApplicationIdentification()));
    }

    public ApplicationCompositionType getApplicationCompositionType() {
        return ApplicationCompositionType.APPLICATION_UNSUPPORTED_COMPOSITION_TYPE;
    }

}
