package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface ConstraintsValidator {

    /**
     * Method that provides information about the specification that defines the constraints being checked by the
     * implementing class.
     * @return a String identifying the constraints specification implemented in this class.
     */
    String getConstraintsSpecification();

    /**
     * This method validates the input CPL and takes any associated MXF header metadata into account that is passed along.
     * @param IMFCompositionPlaylist the IMF Composition Playlist object to validate
     * @param headerPartitionPayloads
     * @return a list of errors encountered while validating the input CPL
     */
    List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist IMFCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads);

    /**
     * A static factory method that instantiates and returns the validator object associated with the provided namespace
     * @param headerPartition the namespace URI for which a validator object is requested. Such namespace URI typically
     *                     represents one of the following:
     *                     - a CPL schema namespace URI
     *                     - the XML namespace for a CPL sequence
     *                     - an ApplicationID
     * @param indexPartitionPayloads
     * @return an object that implements the ConstraintsValidator interface, or null if no matching class is registered.
     */
    List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads);
}
