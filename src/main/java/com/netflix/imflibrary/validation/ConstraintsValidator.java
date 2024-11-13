package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;

import java.util.List;

public interface ConstraintsValidator {

    String getConstraintsSpecification();

    List<ErrorLogger.ErrorObject> validateCompositionConstraints(IMFCompositionPlaylist IMFCompositionPlaylist);

    List<ErrorLogger.ErrorObject> validateTrackFileConstraints(IMFTrackFileReader imfTrackFileReader);
}
