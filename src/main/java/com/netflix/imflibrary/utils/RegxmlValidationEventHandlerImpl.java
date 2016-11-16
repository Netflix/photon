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

import com.netflix.imflibrary.IMFErrorLogger;
import com.sandflow.util.events.Event;
import com.sandflow.util.events.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A thread-safe implementation of the ValidationEventHandler interface for validating an XML serializing operation
 */
public class RegxmlValidationEventHandlerImpl implements EventHandler {

    private final boolean continueOnError;
    private final List<ValidationErrorObject> errors = new ArrayList<>();

    /**
     * A constructor for the ValidationEventHandlerImpl object
     * @param continueOnError - boolean that determines if XML serializing operation should be persisted in the event of an error
     */
    public RegxmlValidationEventHandlerImpl(boolean continueOnError){
        this.continueOnError = continueOnError;
    }

    /**
     * An event handler callback for the XML serializing operation
     * @param event the XML validation event that needs to be handled
     * @return boolean to indicate whether to abort/proceed when an error is encountered
     */
    public boolean handle( Event event ){
        this.errors.add(new ValidationErrorObject(event.getSeverity(), event.getCode(), event.getMessage()));
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
     * @return string representing the object
     */
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(ValidationErrorObject error : errors){
            stringBuilder.append(error.toString());
        }
        return stringBuilder.toString();
    }

    /**
     * A class that represents errors that occur while serializing an XML document
     */
    public static class ValidationErrorObject{
        private final Event.Severity validationEventSeverity;
        private final Enum code;
        private final String errorMessage;

        private ValidationErrorObject(Event.Severity validationEventSeverity, Enum code, String errorMessage){
            this.validationEventSeverity = validationEventSeverity;
            this.errorMessage = errorMessage;
            this.code = code;
        }

        /**
         * A getter for ValidationEvent error severity
         * @return a translation of the validation event error severity to IMFErrorLogger's ErrorLevel enumeration
         */
        public IMFErrorLogger.IMFErrors.ErrorLevels getValidationEventSeverity(){
            switch(this.validationEventSeverity){
                case ERROR:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL;
                case FATAL:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.FATAL;
                case WARN:
                case INFO:
                default:
                    return IMFErrorLogger.IMFErrors.ErrorLevels.WARNING;
            }
        }

        /**
         * A getter for the ValidationError message
         * @return a string representing the ValidationError message
         */
        public String getErrorMessage(){
            return this.errorMessage;
        }

        /**
         * A getter for the ValidationError line number
         * @return an integer corresponding to the line number where the error occurs
         */
        public Enum getCode() { return this.code;}

        /**
         * A toString() method to return the String representation of the ValidationErrorObject
         * @return a string corresponding to the error message with details on error level, code and line number
         */
        @Override
        public String toString(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("%s - %s - %s", getValidationEventSeverity(), getCode(), getErrorMessage()));
            return stringBuilder.toString();
        }
    }
}
