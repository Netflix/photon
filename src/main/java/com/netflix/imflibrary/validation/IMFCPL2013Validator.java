package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.JPEG2000;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Fraction;

import java.util.Arrays;
import java.util.List;


public class IMFCPL2013Validator implements ConstraintsValidator {

    private static final String applicationCompositionType = "IMF Composition Playlist 2013";

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
