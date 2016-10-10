package com.netflix.imflibrary.st2067_2;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by svenkatrav on 10/7/16.
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
