package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;

import java.util.List;


public class IMFCPL2016Validator implements ConstraintsValidator {

    private static final String applicationCompositionType = "IMF Composition Playlist 2016";

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(IMFCompositionPlaylist IMFCompositionPlaylist) {

        // MARKER TRACK VALIDATION, CONTENT KIND VALUES, ETC

        return List.of();
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateTrackFileConstraints(IMFTrackFileReader imfTrackFileReader) {
        return List.of();
    }
}
