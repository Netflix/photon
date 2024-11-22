package com.netflix.imflibrary.validation;

public class IMFCPL2013Validator extends IMFCPLValidator {

    private static final String applicationCompositionType = "IMF Composition Playlist 2013";

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
    }

}
