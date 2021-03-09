/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.exceptions;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.utils.ErrorLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Unchecked exception class that is used when a fatal error occurs in processing related to IMF layer
 */
public class IMFException extends RuntimeException
{
    private final IMFErrorLogger errorLogger;

    public IMFException(String s)
    {
        super(s);
        this.errorLogger = null;
    }

    public IMFException(Throwable t)
    {
        super(t);
        this.errorLogger = null;
    }

    public IMFException(String s, Throwable t)
    {
        super(s, t);
        this.errorLogger = null;
    }

    public IMFException(String s, @Nonnull IMFErrorLogger errorLogger)
    {
        super(s);
        this.errorLogger = errorLogger;
    }

    public IMFException(String s, Throwable t, @Nonnull IMFErrorLogger errorLogger)
    {
        super(s, t);
        this.errorLogger = errorLogger;
    }

    public List<ErrorLogger.ErrorObject> getErrors()
    {
        List errorList = new ArrayList<ErrorLogger.ErrorObject>();
        if(this.errorLogger != null)
        {
            errorList.addAll(this.errorLogger.getErrors());
        }
        // Need to handle cause error. If the xsi:type is not specified correctly
        // (missing namespace for instance), JAXB may try and create the abstract class instead,
        // which will fail. This exception is not being recorded; Photon was completing 
        // successfully and reporting no issues.
        if (this.getCause() != null)
        {
            String message = this.getCause().getMessage();
            if (message != null && message.contains("Unable to create an instance "))
            {
                message += "; Check tags with xsi:type has the right namespace and declared type";
            }
            errorList.add(new ErrorLogger.ErrorObject(
                IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR,
                IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                message
            ));
        }

        return Collections.unmodifiableList(errorList);
    }
}
