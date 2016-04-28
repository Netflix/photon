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

package com.netflix.imflibrary.writerTools.utils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.utils.ErrorLogger;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A thread-safe implementation of the ValidationEventHandler interface for validating an XML serializing operation
 */
public class ValidationEventHandlerImpl implements ValidationEventHandler {

    private final boolean continueOnError;
    private final List<ValidationErrorObject> errors = new ArrayList<>();

    /**
     * A constructor for the ValidationEventHandlerImpl object
     * @param continueOnError - boolean that determines if XML serializing operation should be persisted in the event of an error
     */
    public ValidationEventHandlerImpl(boolean continueOnError){
        this.continueOnError = continueOnError;
    }

    /**
     * An event handler callback for the XML serializing operation
     * @param event the XML validation event that needs to be handled
     * @return boolean to indicate whether to abort/proceed when an error is encountered
     */
    public boolean handleEvent( ValidationEvent event ){
        System.out.println(String.format("%s", event.getMessage()));
        this.errors.add(new ValidationErrorObject(event.getSeverity(), event.getMessage()));
        return this.continueOnError;
    }

    /**
     * Checks if any errors occurred while serializing an XML document
     * @return boolean to signal if serializing the XML document occurred with/without errors
     */
    public boolean hasErrors(){
        return (this.errors.size() > 0);
    }

    /**
     * Checks if any errors occurred while serializing an XML document
     * @return list of ErrorObjects with errors
     */
    public List<ValidationErrorObject> getErrors(){
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * A method that returns a string representation of a ValidationEventHandlerImpl object
     *
     * @return string representing the object
     */
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(ValidationErrorObject error : errors){
            stringBuilder.append(error.getErrorMessage());
        }
        return stringBuilder.toString();
    }

    /**
     * A class that represents errors that occur while serializing an XML document
     */
    public static class ValidationErrorObject{
        private final int validationEventSeverity;
        private final String errorMessage;

        private ValidationErrorObject(int validationEventSeverity, String errorMessage){
            this.validationEventSeverity = validationEventSeverity;
            this.errorMessage = errorMessage;
        }

        /**
         * A getter for ValidationEvent error severity
         * @return a translation of the validation event error severity to IMFErrorLogger's ErrorLevel enumeration
         */
        public IMFErrorLogger.IMFErrors.ErrorLevels getValidationEventSeverity(){
            switch(this.validationEventSeverity){
                case ValidationEvent.ERROR:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL;
                case ValidationEvent.FATAL_ERROR:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.FATAL;
                case ValidationEvent.WARNING:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.WARNING;
                default:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL;
            }
        }

        /**
         * A getter for the ValidationError message
         * @return a string representing the ValidationError message
         */
        public String getErrorMessage(){
            return this.errorMessage;
        }
    }
}
