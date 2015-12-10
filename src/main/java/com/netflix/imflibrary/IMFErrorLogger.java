/*
 *
 *  * Copyright 2015 Netflix, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *         http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.netflix.imflibrary;

import com.netflix.imflibrary.utils.ErrorLogger;

import java.util.List;

/**
 * An interface to represent errors that occurred while reading an MXF file
 */
public interface IMFErrorLogger extends ErrorLogger
{
    /**
     * A method to add errors to a persistent list
     *
     * @param errorCode the error code
     * @param errorLevel the error level
     * @param errorDescription the error description
     */
    void addError(IMFErrors.ErrorCodes errorCode, IMFErrors.ErrorLevels errorLevel, String errorDescription);

    List<ErrorObject> getErrors();

    final class IMFErrors
    {

        private IMFErrors()
        {//to prevent instantiation

        }


        /**
         * An enumeration for Error codes
         */
        public enum ErrorCodes
        {
            /**
             * The KLV_LAYER_ERROR.
             */
            KLV_LAYER_ERROR,

            /**
             * The MXF_PARTITION_FIELD_ERROR.
             */
            MXF_PARTITION_FIELD_ERROR,

            /**
             * The MXF_PARTITION_ERROR.
             */
            MXF_PARTITION_ERROR,

            /**
             * The MXF_OPERATIONAL_PATTERN_1A_ERROR.
             */
            MXF_OPERATIONAL_PATTERN_1A_ERROR,

            /**
             * The IMF_ESSENCE_COMPONENT_ERROR.
             */
            IMF_ESSENCE_COMPONENT_ERROR,

            /**
             * The IMF_CPL_ERROR.
             */
            IMF_CPL_ERROR,

            /**
             * The IMF_PKL_ERROR.
             */
            IMF_PKL_ERROR,

            /**
             * The IMF_CORE_CONSTRAINTS_ERROR.
             */
            IMF_CORE_CONSTRAINTS_ERROR,

        }


        /**
         * An enumeration for the  Error levels
         */
        public enum ErrorLevels
        {
            /**
             * The NOT_FATAL.
             */
            NOT_FATAL,

            /**
             * The FATAL.
             */
            FATAL,
        }
    }
}
