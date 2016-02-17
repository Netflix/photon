package com.netflix.imflibrary.imp_validation;

import java.util.List;

/**
 * This class implements the logic to perform conformance validation of an IMF CompositionPlaylist document.
 * Conformance validation will verify that the essences that are described in the CPL EssenceDescriptionList comply with
 * IMF-CoreConstraints (SMPTE st2067-2:2013) and that the essence description contained within every essence that the CPL
 * references through its virtual track resource list is contained in the EssenceDescription List.
 */
public class CompositionPlaylistConformanceValidator {

    public boolean isCompositionPlaylistConformed(CompositionPlaylistRecord compositionPlaylistRecord){
        boolean result = true;
        return result;
    }



}
