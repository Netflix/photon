package com.netflix.imflibrary.st2067_2;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by svenkatrav on 10/7/16.
 */
public class Application2ExtendedComposition extends Composition {

    public Application2ExtendedComposition(@Nonnull IMFCompositionPlaylistType imfCompositionPlaylistType) {
        super(imfCompositionPlaylistType, new HashSet<>());
    }
}
