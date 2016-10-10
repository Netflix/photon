package com.netflix.imflibrary.st2067_2;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A class that models Composition with Application 2 constraints from 2067-20 specification
 */
public class Application2Composition extends AbstractApplicationComposition {
    private static final Set<String> ignoreSet = Collections.unmodifiableSet(new HashSet<String>() {{
        add("SignalStandard");
        add("Active Format Descriptor");
        add("Video Line Map");
        add("Alpha Transparency");
        add("PixelLayout");
    }});

    public Application2Composition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        super(imfCompositionPlaylistType, ignoreSet);
    }
}
