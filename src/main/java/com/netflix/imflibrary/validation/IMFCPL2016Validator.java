package com.netflix.imflibrary.validation;

/**
 * Collection of properties and validations specific to ST 2067-3:2016.
 */
public class IMFCPL2016Validator extends IMFCPLValidator {

    @Override
    public String getConstraintsSpecification() {
        return "SMPTE ST 2067-3:2016 IMF Composition Playlist";
    }

}
