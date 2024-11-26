package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.*;

import static com.netflix.imflibrary.st2067_2.IMFCoreConstraintsChecker.checkVirtualTrackResourceList;

public class IMFCoreConstraints2016Validator extends IMFCoreConstraintsValidator {

    private static final String ccNamespaceURI = "http://www.smpte-ra.org/schemas/2067-2/2016";

    private static final String applicationCompositionType = "IMF Core Constraints 2016";

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
