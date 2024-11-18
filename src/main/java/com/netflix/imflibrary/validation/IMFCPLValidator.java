package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.IMFCompositionPlaylistUtils;

import java.util.List;


abstract public class IMFCPLValidator implements ConstraintsValidator {

    protected static List<ErrorLogger.ErrorObject> validateCommonConstraints(IMFCompositionPlaylist imfCompositionPlaylist){

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        /**
         * Check that each entry in the EssenceDescriptorList is referenced from at least one Resource.
         */
        imfCompositionPlaylist.getEssenceDescriptorIdsSet().forEach(descriptorId -> {
            if (!imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet().contains(descriptorId)) {
                //Section 6.1.10.1 st2067-3:2013
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptorID %s in the CPL " +
                        "EssenceDescriptorList is not referenced by any resource in any of the Virtual Tracks in the CPL.", descriptorId.toString()));
            }
        });


        /**
         * Check that every Resource SourceEncodingID is present in the EssenceDescriptorList.
         */
        imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet().forEach(resourceEssenceDescriptorId -> {
            if (!imfCompositionPlaylist.getEssenceDescriptorIdsSet().contains(resourceEssenceDescriptorId)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor with SourceEncodingID " +
                        "%s is missing in EssenceDescriptorList.", resourceEssenceDescriptorId.toString()));
            }
        });

        return imfErrorLogger.getErrors();
    }
}
