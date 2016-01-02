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
     * Getter for the number of errors that were detected while reading the MXF file
     * @return integer representing the number of errors
     */
    public int getNumberOfErrors()
    {
        return this.errorObjects.size();
    }

    public List<ErrorLogger.ErrorObject> getErrors()
    {
        return Collections.unmodifiableList(this.errorObjects);
    }

}
