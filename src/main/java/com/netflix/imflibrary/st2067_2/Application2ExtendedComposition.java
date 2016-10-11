package com.netflix.imflibrary.st2067_2;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that models Composition with Application 2Extended constraints from 2067-21 specification
 */
public class Application2ExtendedComposition extends AbstractApplicationComposition {
    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
        add("SignalStandard");
        add("Active Format Descriptor");
        add("Video Line Map");
        add("Alpha Transparency");
        add("PixelLayout");
    }});

    public Application2ExtendedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        super(imfCompositionPlaylistType, ignoreSet);
    }
}
