package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.List;

public class IMFCoreConstraints2020Validator extends IMFCoreConstraintsValidator {

    private static final String ccNamespaceURI = "http://www.smpte-ra.org/ns/2067-2/2020";

    private static final String applicationCompositionType = "IMF Core Constraints 2020";

    @Override
    public String getConstraintsSpecification() {
        return applicationCompositionType;
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        imfErrorLogger.addAllErrors(validateVirtualTracks(imfCompositionPlaylist, ccNamespaceURI));

        return imfErrorLogger.getErrors();
    }

}
