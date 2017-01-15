package com.netflix.imflibrary;

import com.netflix.imflibrary.utils.ErrorLogger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by schakrovorthy on 1/14/17.
 */
public final class DCPErrorLoggerImpl implements DCPErrorLogger //This is really a logging aggregator
{
    private final Set<ErrorObject> errorObjects;

    /**
     * Instantiates a new IMF error logger impl object
     */
    public DCPErrorLoggerImpl()
    {
        this.errorObjects = Collections.synchronizedSet(new HashSet<ErrorObject>());
    }

    /**
     * A method to add error objects to a persistent list
     *
     * @param errorCode - error code corresponding to the - error cannot be null
     * @param errorLevel - error level of the error - cannot be null
     * @param errorDescription - the error description - cannot be null
     */
    public void addError(@Nonnull DCPErrors.ErrorCodes errorCode, @Nonnull DCPErrors.ErrorLevels errorLevel, @Nonnull String errorDescription)
    {
        this.errorObjects.add(new ErrorLogger.ErrorObject(errorCode, errorLevel, errorDescription));
    }

    /**
     * A method to add an error object to a persistent list
     *
     * @param errorObject - error object to be added to a persistent list - cannot be null
     */
    public void addError(@Nonnull ErrorObject errorObject)
    {
        this.errorObjects.add(errorObject);
    }

    /**
     * A method to add an error object to a persistent list
     *
     * @param errorObjects - a list of error objects to be added to a persistent list - cannot be null
     */
    public void addAllErrors(@Nonnull List<ErrorObject> errorObjects)
    {
        this.errorObjects.addAll(errorObjects);
    }

    /**
     * Getter for the number of errors that were detected while reading the MXF file
     * @return integer representing the number of errors
     */
    public int getNumberOfErrors()
    {
        return this.errorObjects.size();
    }

    /**
     * Getter for the list of errors monitored by this ErrorLogger implementation
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors()
    {
        List<ErrorObject> errors = new ArrayList<>(this.errorObjects);
        return Collections.unmodifiableList(errors);
    }

    /**
     * Getter for the list of errors filtered by the ErrorLevel monitored by this ErrorLogger implementation
     * @param errorLevel to be filtered
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorLevels errorLevel) throws IllegalArgumentException
    {
        return getErrors(errorLevel, 0, this.errorObjects.size());
    }

    /**
     * Getter for the list of errors in a specified range of errors filtered by the ErrorLevel monitored by this ErrorLogger implementation
     * @param errorLevel to be filtered
     * @param startIndex the start index (inclusive) within the list of errors
     * @param endIndex the last index (exclusive) within the list of errors
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorLevels errorLevel, int startIndex, int endIndex) throws IllegalArgumentException
    {
        validateRangeRequest(startIndex, endIndex);
        List<ErrorObject> errors = new ArrayList<>(this.errorObjects);
        return Collections.unmodifiableList(errors.subList(startIndex, endIndex).stream().filter(e -> e.getErrorLevel().equals(DCPErrorLogger.DCPErrors.ErrorLevels.FATAL)).collect(Collectors.toList()));
    }

    /**
     * Getter for the list of errors filtered by the ErrorCode monitored by this ErrorLogger implementation
     * @param errorCode to be filtered
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorCodes errorCode) throws IllegalArgumentException
    {
        return getErrors(errorCode, 0 , this.errorObjects.size());
    }

    /**
     * Getter for the list of errors in a specified range of errors filtered by the ErrorLevel monitored by this ErrorLogger implementation
     * @param errorCode to be filtered
     * @param startIndex the start index (inclusive) within the list of errors
     * @param endIndex the last index (exclusive) within the list of errors
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorCodes errorCode, int startIndex, int endIndex) throws IllegalArgumentException
    {
        validateRangeRequest(startIndex, endIndex);
        List<ErrorObject> errors = new ArrayList<>(this.errorObjects);
        return Collections.unmodifiableList(errors.subList(startIndex, endIndex).stream().filter(e -> e.getErrorCode().equals(DCPErrorLogger.DCPErrors.ErrorLevels.FATAL)).collect(Collectors.toList()));
    }

    private void validateRangeRequest(int rangeStart, int rangeEnd) throws IllegalArgumentException {

        if (rangeStart < 0)
        {
            throw new IllegalArgumentException(String.format("rangeStart = %d is < 0", rangeStart));
        }

        if (rangeStart > rangeEnd)
        {
            throw new IllegalArgumentException(String.format("rangeStart = %d is not <= %d rangeEnd", rangeStart, rangeEnd));
        }

        if (rangeEnd > (this.errorObjects.size()))
        {
            throw new IllegalArgumentException(String.format("rangeEnd = %d is not <= (resourceSize) = %d", rangeEnd, (this.errorObjects.size())));
        }
    }

    public Boolean hasFatalErrors()
    {
        return (getErrors(DCPErrors.ErrorLevels.FATAL).size() > 0);
    }

    public Boolean hasFatalErrors(int startIndex, int endIndex) {
        return (getErrors(DCPErrors.ErrorLevels.FATAL, startIndex, endIndex).size() > 0);

    }

    public List<ErrorObject> translateIMFErrors(List<ErrorObject> imfErrors){
        List<ErrorObject> errors = new ArrayList<>();
        for(ErrorObject errorObject : imfErrors){
            DCPErrors.ErrorCodes dcpErrorCode = this.getDCPErrorCode(errorObject.getErrorCode());
            ErrorObject dcpErrorObject = new ErrorObject(dcpErrorCode, errorObject.getErrorLevel(), errorObject.getErrorDescription());
            errors.add(dcpErrorObject);
        }
        return errors;
    }

    private DCPErrors.ErrorCodes getDCPErrorCode(Enum errorCode){
        IMFErrorLogger.IMFErrors.ErrorCodes imfErrorCode = (IMFErrorLogger.IMFErrors.ErrorCodes) errorCode;
        switch (imfErrorCode){
            case IMF_ESSENCE_METADATA_ERROR:
                return DCPErrors.ErrorCodes.DCP_ESSENCE_METADATA_ERROR;
            case IMF_ESSENCE_COMPONENT_ERROR:
                return DCPErrors.ErrorCodes.DCP_ESSENCE_COMPONENT_ERROR;
            case IMF_CPL_ERROR:
                return DCPErrors.ErrorCodes.DCP_CPL_ERROR;
            case IMF_PKL_ERROR:
                return DCPErrors.ErrorCodes.DCP_PKL_ERROR;
            case IMF_AM_ERROR:
                return DCPErrors.ErrorCodes.DCP_AM_ERROR;
            case IMF_CORE_CONSTRAINTS_ERROR:
                return DCPErrors.ErrorCodes.DCP_CORE_CONSTRAINTS_ERROR;
            case IMF_CORE_CONSTRAINTS_ESSENCE_DESCRIPTOR_LIST_MISSING:
                return DCPErrors.ErrorCodes.DCP_CORE_CONSTRAINTS_ESSENCE_DESCRIPTOR_LIST_MISSING;
            case IMF_MASTER_PACKAGE_ERROR:
                return DCPErrors.ErrorCodes.DCP_MASTER_PACKAGE_ERROR;
            case IMP_VALIDATOR_PAYLOAD_ERROR:
                return DCPErrors.ErrorCodes.DCP_PACKAGE_VALIDATOR_PAYLOAD_ERROR;
            case UUID_ERROR:
                return DCPErrors.ErrorCodes.UUID_ERROR;
            case URI_ERROR:
                return DCPErrors.ErrorCodes.URI_ERROR;
            case INTERNAL_ERROR:
                return DCPErrors.ErrorCodes.INTERNAL_ERROR;
            case APPLICATION_COMPOSITION_ERROR:
                return DCPErrors.ErrorCodes.APPLICATION_COMPOSITION_ERROR;
            case SMPTE_REGISTER_PARSING_ERROR:
                return DCPErrors.ErrorCodes.SMPTE_REGISTER_PARSING_ERROR;
            default:
                return DCPErrors.ErrorCodes.INTERNAL_ERROR;
        }
    }
}
