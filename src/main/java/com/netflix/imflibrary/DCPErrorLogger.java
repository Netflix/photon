package com.netflix.imflibrary;

import com.netflix.imflibrary.utils.ErrorLogger;

import java.util.List;

/**
 * Created by schakrovorthy on 1/14/17.
 */
public interface DCPErrorLogger extends ErrorLogger
{
    /**
     * A method to add errors to a persistent list
     *
     * @param errorCode the error code
     * @param errorLevel the error level
     * @param errorDescription the error description
     */
    void addError(DCPErrors.ErrorCodes errorCode, DCPErrors.ErrorLevels errorLevel, String errorDescription);

    public void addError(ErrorObject errorObject);

    public void addAllErrors(List<ErrorObject> errorObjects);

    public List<ErrorObject> getErrors();

    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorLevels errorLevel) throws IllegalArgumentException;

    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorLevels errorLevel, int startIndex, int endIndex) throws IllegalArgumentException;

    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorCodes errorCode) throws IllegalArgumentException;

    public List<ErrorLogger.ErrorObject> getErrors(DCPErrors.ErrorCodes errorCode, int startIndex, int endIndex) throws IllegalArgumentException;

    public Boolean hasFatalErrors();

    public Boolean hasFatalErrors(int startIndex, int endIndex);

    public List<ErrorObject> translateIMFErrors(List<ErrorObject> imfErrors);

    final class DCPErrors
    {

        private DCPErrors()
        {//to prevent instantiation

        }


        /**
         * An enumeration for Error codes
         */
        public enum ErrorCodes
        {
            /**
             * The DCP_ESSENCE_METADATA_ERROR.
             */
            DCP_ESSENCE_METADATA_ERROR("IMF Essence metadata Error"),

            /**
             * The DCP_ESSENCE_COMPONENT_ERROR.
             */
            DCP_ESSENCE_COMPONENT_ERROR("IMF Essence Component Error"),

            /**
             * The DCP_CPL_ERROR.
             */
            DCP_CPL_ERROR("IMF CPL Error"),

            /**
             * The DCP_PKL_ERROR.
             */
            DCP_PKL_ERROR("IMF PKL Error"),

            /**
             * Error in processing of AssetMap or a mapped file set
             */
            DCP_AM_ERROR("IMF AssetMap Error"),

            /**
             * The DCP_CORE_CONSTRAINTS_ERROR.
             */
            DCP_CORE_CONSTRAINTS_ERROR("IMF Core Constraints Error"),

            /**
             * EssenceDescriptorList element is mssing from an IMF CPL (st2067-2:2016 Section 6.8)
             */
            DCP_CORE_CONSTRAINTS_ESSENCE_DESCRIPTOR_LIST_MISSING("IMF Core Constraints Essence Descriptor List Missing"),

            /**
             * The DCP_MASTER_PACKAGE_ERROR.
             */
            DCP_MASTER_PACKAGE_ERROR("IMF Master Package Error"),

            /**
             * The DCP_PACKAGE_VALIDATOR_PAYLOAD_ERROR.
             */
            DCP_PACKAGE_VALIDATOR_PAYLOAD_ERROR("IMP Validator Payload Error"),

            /**
             * The UUID_ERROR.
             */
            UUID_ERROR("UUID Syntax Error"),

            /**
             * The URI_ERROR.
             */
            URI_ERROR("URI Syntax Error"),

            /**
             * INTERNAL_ERROR.
             */
            INTERNAL_ERROR ("Internal processing error"),

            /**
             * APPLICATION_COMPOSITION_ERROR.
             */
            APPLICATION_COMPOSITION_ERROR ("Application Composition error"),

            /**
             * SMPTE_REGISTER_PARSING_ERROR.
             */
            SMPTE_REGISTER_PARSING_ERROR("SMPTE Register parsing error");

            private final String error;

            ErrorCodes(String error){
                this.error = error;
            }

            /**
             * A toString() method
             * @return string representation of this enumeration constant
             */
            public String toString(){
                return this.error;
            }

        }


        /**
         * An enumeration for the  Error levels
         */
        public enum ErrorLevels
        {
            /**
             * WARNING
             */
            WARNING("WARNING"),
            /**
             * The NON_FATAL.
             */
            NON_FATAL("NON FATAL"),

            /**
             * The FATAL.
             */
            FATAL("FATAL");

            private final String errorLevel;
            ErrorLevels(String errorLevel){
                this.errorLevel = errorLevel;
            }

            /**
             * A toString() method
             * @return string representation of this enumeration constant
             */
            public String toString(){
                return this.errorLevel;
            }
        }
    }
}
