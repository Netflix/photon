package com.netflix.imflibrary.validation;

public class IMFCPL2016Validator extends IMFCPLValidator {

    private static final String applicationCompositionType = "IMF Composition Playlist 2016";

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
    }

}
