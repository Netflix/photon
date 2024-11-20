package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.*;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


abstract public class IMFCPLValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {
        return List.of();
    }

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
