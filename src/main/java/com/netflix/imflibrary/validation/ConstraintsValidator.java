package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * Interface implemented by concrete validator classes that cover specific versions of applications, plugins, core constraints or CPL specifications.
 */
public interface ConstraintsValidator {

    /**
     * Method that provides information about the specification that defines the constraints being checked by the
     * implementing class.
     * @return a String identifying the constraints specification implemented in this class, e.g. "SMPTE ST 2067-21:2020".
     */
    String getConstraintsSpecification();

    /**
     * Implementations of this method validate the relevant parts of the input CPL, typically virtual track(s) covered by
     * the implemented specification. If applicable, essence partition payloads can be provided to verify essence descriptors
     * or other aspects covered by the implemented specification.
     * @param imfCompositionPlaylist the IMF Composition Playlist object to validate
     * @param headerPartitionPayloads a list of header partition payload records
     * @return a list of errors encountered while validating the input CPL and/or the referenced essence partition payloads.
     */
    List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads);

    /**
     * Implementations of this method validate the input header/index partition payloads.
     * @param headerPartition a list of header partition payload records
     * @param indexPartitionPayloads a list of index partition payload records
     * @return a list of errors encountered while validating the input partition payloads.
     */
    List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads);
}
