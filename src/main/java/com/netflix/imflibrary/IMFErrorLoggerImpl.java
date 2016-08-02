/*
 *
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary;

import com.netflix.imflibrary.utils.ErrorLogger;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An non-thread-safe implementation of the IMFErrorLogger interface
 */
@NotThreadSafe
public final class IMFErrorLoggerImpl implements IMFErrorLogger //This is really a logging aggregator
{
    private final List<ErrorLogger.ErrorObject> errorObjects;

    /**
     * Instantiates a new IMF error logger impl object
     */
    public IMFErrorLoggerImpl()
    {
        this.errorObjects = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * A method to add error objects to a persistent list
     *
     * @param errorCode - error code corresponding to the error
     * @param errorLevel - error level of the error
     * @param errorDescription - the error description
     */
    public void addError(IMFErrors.ErrorCodes errorCode, IMFErrors.ErrorLevels errorLevel, String errorDescription)
    {
        this.errorObjects.add(new ErrorLogger.ErrorObject(errorCode, errorLevel, errorDescription));
    }

    /**
     * A method to add an error object to a persistent list
     *
     * @param errorObject - error object to be added to a persistent list
     */
    public void addError(ErrorObject errorObject)
    {
        this.errorObjects.add(errorObject);
    }

    /**
     * A method to add an error object to a persistent list
     *
     * @param errorObjects - a list of error objects to be added to a persistent list
     */
    public void addAllErrors(List<ErrorObject> errorObjects)
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
        return Collections.unmodifiableList(this.errorObjects);
    }

    /**
     * Getter for the list of errors filtered by the ErrorLevel monitored by this ErrorLogger implementation
     * @param errorLevel to be filtered
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(IMFErrors.ErrorLevels errorLevel) throws IllegalArgumentException
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
    public List<ErrorLogger.ErrorObject> getErrors(IMFErrors.ErrorLevels errorLevel, int startIndex, int endIndex) throws IllegalArgumentException
    {
        validateRangeRequest(startIndex, endIndex);
        return Collections.unmodifiableList(this.errorObjects.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL)).collect(Collectors.toList()));
    }

    /**
     * Getter for the list of errors filtered by the ErrorCode monitored by this ErrorLogger implementation
     * @param errorCode to be filtered
     * @return a list of errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(IMFErrors.ErrorCodes errorCode) throws IllegalArgumentException
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
    public List<ErrorLogger.ErrorObject> getErrors(IMFErrors.ErrorCodes errorCode, int startIndex, int endIndex) throws IllegalArgumentException
    {
        validateRangeRequest(startIndex, endIndex);
        return Collections.unmodifiableList(this.errorObjects.stream().filter(e -> e.getErrorCode().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL)).collect(Collectors.toList()));
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

        if (rangeEnd > (this.errorObjects.size() - 1))
        {
            throw new IllegalArgumentException(String.format("rangeEnd = %d is not <= (resourceSize -1) = %d", rangeEnd, (this.errorObjects.size()-1)));
        }
    }

}
