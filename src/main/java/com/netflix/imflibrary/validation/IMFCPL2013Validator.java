package com.netflix.imflibrary.validation;

/**
 * Collection of properties and validations specific to ST 2067-3:2013.
 */
public class IMFCPL2013Validator extends IMFCPLValidator {

    @Override
    public String getConstraintsSpecification() {
        return "SMPTE ST 2067-3:2013 IMF Composition Playlist";
    }
}
