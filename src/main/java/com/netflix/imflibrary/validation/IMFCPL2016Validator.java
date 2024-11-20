package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.List;


public class IMFCPL2016Validator extends IMFCPLValidator {

    private static final String applicationCompositionType = "IMF Composition Playlist 2016";

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        imfErrorLogger.addAllErrors(validateCommonConstraints(imfCompositionPlaylist));

        imfErrorLogger.addAllErrors(IMPValidator.validateEssenceDescriptorsMatch(imfCompositionPlaylist, headerPartitionPayloads));

        // MARKER TRACK VALIDATION, CONTENT KIND VALUES, ETC

        return imfErrorLogger.getErrors();
    }

}
