/*
 *
 * Copyright 2026 Netflix, Inc.
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

package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.*;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RFC5646SpokenLanguage;
import com.netflix.imflibrary.utils.Utilities;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Essence-agnostic structural checks of an OP1A-conformant IMF Track File header partition with st2067-5:2013
 * (single package, single essence track, package UID, ...).
 * Essence kind (audio, text, ...) and application specific checks live in the validators selected by the
 * validation context, see {@link ConstraintsValidator} and {@link EssenceValidationContext}.
 */
public final class IMFTrackFileStructureConstraints {
    private static final String IMF_ESSENCE_EXCEPTION_PREFIX = "IMF Essence Component check: ";

    private IMFTrackFileStructureConstraints() {}

    public static List<ErrorLogger.ErrorObject> checkHeaderMetadata(HeaderPartition headerPartition) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            checkHeaderMetadata(headerPartitionOP1A, imfErrorLogger);
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }

    public static HeaderPartitionIMF checkHeaderMetadata(MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        int previousNumberOfErrors = imfErrorLogger.getErrors().size();

        HeaderPartition headerPartition = headerPartitionOP1A.getHeaderPartition();

        // Issue warnings if more than one Essence Container UL is present in Partition Pack or Preface
        // legacy files may contain multiple Essence Container ULs due to some unexpected behaviour in earlier versions of asdcplib:
        // https://github.com/cinecert/asdcplib/blob/9baa76f3e5b910c63e3e2355568eb409618d1a3d/CHANGES#L82
        Preface preface = headerPartition.getPreface();
        List<UL> prefaceEssenceContainerULs = preface.getEssenceContainerULs();
        if (prefaceEssenceContainerULs.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ESSENCE_EXCEPTION_PREFIX +
                    String.format("The MXF Preface is expected to contain a single Essence Container UL, but contains %d.", prefaceEssenceContainerULs.size()));
        }

        List<UL> partitionPackEssenceContainerULs = headerPartition.getPartitionPack().getEssenceContainerULs();
        if (partitionPackEssenceContainerULs.size() != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, IMF_ESSENCE_EXCEPTION_PREFIX +
                    String.format("The MXF Partition Pack is expected to contain a single Essence Container UL, but contains %d.", partitionPackEssenceContainerULs.size()));
        }

        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage;
        filePackage = (SourcePackage)genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();
        //check 'Operational Pattern' field in Preface
        byte[] bytes = preface.getOperationalPattern().getULAsBytes();
        //Section 8.3.3 st377-1:2011 and Section 5.2 st2067-5:2013
        if (OperationalPatternHelper.getPackageComplexity(bytes) != OperationalPatternHelper.PackageComplexity.SinglePackage)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String
                    .format("IMFTrackFile represented by Id %s, Lower four bits of Operational Pattern qualifier byte = 0x%x, should be = 0x01 per the definition of OperationalPattern-1A for Package Complexity.",
                    packageID.toString(), bytes[13]));
        }

        //Section 8.3.3 st377-1:2011
        if (OperationalPatternHelper.getItemComplexity(bytes) != OperationalPatternHelper.ItemComplexity.SingleItem)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String
                    .format("IMFTrackFile represented by Id %s, Lower four bits of Operational Pattern qualifier byte = 0x%x, should be = 0x01 per the definition of OperationalPattern-1A for Item Complexity.",
                    packageID.toString(), bytes[12]));
        }

        //Section 5.1.1#13 st2067-5:2014 , primary package identifier for Preface shall be set to the top-level file package
        if ((preface.getPrimaryPackage() == null) || (!preface.getPrimaryPackage().equals(preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage())))
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Primary package identifier for Preface is not set to the top-level file package in the IMFTrackFile represented by ID %s.", packageID.toString()));
        }

        //From st2067-5:2013 section 5.1.3, only one essence track shall exist in the file package
        {
            MXFUID packageUID = filePackage.getPackageUID();
            byte[] packageUID_first16Bytes_Constrained = {0x06, 0x0a, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x01, 0x0f, 0x20, 0x13, 0x00, 0x00, 0x00};
            byte[] packageUID_first16Bytes = Arrays.copyOfRange(packageUID.getUID(), 0, packageUID_first16Bytes_Constrained.length);
            boolean result = packageUID_first16Bytes[0] == packageUID_first16Bytes_Constrained[0];
            for(int i=1; i < packageUID_first16Bytes_Constrained.length ; i++){
                result &= packageUID_first16Bytes[i] == packageUID_first16Bytes_Constrained[i];
            }
            //Section 5.1.5 st2067-2:2016
            if(!result){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PackageUID in FilePackage = %s, which does not obey the constraint that the first 16 bytes = %s in the IMFTrackFile represented by ID %s.",
                        packageUID.toString(), Utilities.serializeBytesToHexString(packageUID_first16Bytes_Constrained), packageID.toString()));
            }

            int numEssenceTracks = 0;
            MXFDataDefinition filePackageMxfDataDefinition = null;
            for (TimelineTrack timelineTrack : filePackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                filePackageMxfDataDefinition = sequence.getMxfDataDefinition();
                if (!filePackageMxfDataDefinition.equals(MXFDataDefinition.OTHER))
                {
                    numEssenceTracks++;
                }
                //Section 5.1.7 st2067-2:2016
                if(timelineTrack.getOrigin() != 0){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("The origin property of a timeline track in the IMFTrackFile represented by ID %s is non-zero, only 0 is allowed.", packageID));
                }
            }
            if (numEssenceTracks != 1)
            {
                //Section 5.1.3 of SMPTE st2067-5:2013
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Number of essence tracks in FilePackage %s = %d, which is different from 1, this is invalid in the IMFTrackFile represented by ID %s.",
                        filePackage.getInstanceUID(), numEssenceTracks, packageID.toString()));
            }
        }

        // A Timeline Track without a Sequence cannot be classified, essence kind checks skip it
        for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {
            if (timelineTrack.getSequence() == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX +
                        String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                                timelineTrack.getInstanceUID(), packageID.toString()));
            }
        }

        if(imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors())){
            throw new IMFException(String.format("Found fatal errors in the in the IMFTrackFile represented by ID %s that violate the IMF Core constraints.", packageID.toString()), imfErrorLogger);
        }
        return HeaderPartitionIMF.wrap(headerPartitionOP1A);
    }
}
