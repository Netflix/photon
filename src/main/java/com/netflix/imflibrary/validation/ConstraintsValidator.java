package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface ConstraintsValidator {

    String getConstraintsSpecification();

    List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist IMFCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads);

}
