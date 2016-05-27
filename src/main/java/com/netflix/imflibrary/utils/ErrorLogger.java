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

package com.netflix.imflibrary.utils;

import java.util.List;

public interface ErrorLogger
{

    /**
     * Getter for the number of errors
     *
     * @return the number of errors
     */
    public int getNumberOfErrors();

    /**
     * Getter for the  error messages
     *
     * @return the errors
     */
    public List<ErrorObject> getErrors();

    /**
     * An object model representing useful information about an Error
     */
    public static final class ErrorObject
    {
        private final Enum errorCode;
        private final Enum errorLevel;
        private final String errorDescription;

        /**
         * Instantiates a new ErrorObject.
         *
         * @param errorCode the error code
         * @param errorLevel the error level
         * @param errorDescription the error description
         */
        public ErrorObject(Enum errorCode, Enum errorLevel, String errorDescription)
        {
            this.errorCode = errorCode;
            this.errorLevel = errorLevel;
            this.errorDescription = errorDescription;
        }

        /**
         * Getter for the error description
         *
         * @return the error description
         */
        public String getErrorDescription()
        {
            return this.errorDescription;
        }

        /**
         * toString() method to return a string representation of this error object
         * @return string representation of the error object
         */
        public String toString(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.errorCode.toString());
            stringBuilder.append("-");
            stringBuilder.append(this.errorLevel.toString());
            stringBuilder.append("-");
            stringBuilder.append(this.errorDescription);
            return stringBuilder.toString();
        }

    }

}
