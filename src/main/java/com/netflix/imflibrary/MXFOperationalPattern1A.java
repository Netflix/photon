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

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.ContentStorage;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.MaterialPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.Sequence;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0377.header.TimelineTrack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * This class consists exclusively of static methods that help verify the compliance of an
 * MXF header partition as well as MXF partition packs (see st377-1:2011) with st378:2004
 */
public final class MXFOperationalPattern1A
{

    private static final byte[] OPERATIONAL_PATTERN1A_KEY      = {0x06, 0x0e, 0x2b, 0x34, 0x04, 0x01, 0x01, 0x00, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x01, 0x00, 0x00};
    private static final byte[] OPERATIONAL_PATTERN1A_KEY_MASK = {   1,    1,    1,    1,    1,    1,    1,    0,    1,    1,    1,    1,    1,    1,    0,    1};
    private static final double EPSILON = 0.000001;
    private static final double TOLERANCE = 1.0;
    private static final String OP1A_EXCEPTION_PREFIX = "MXF Operational Pattern 1A check: ";

    //to prevent instantiation
    private MXFOperationalPattern1A()
    {
    }

    /**
     * Checks the compliance of an MXF header partition with st378:2004. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param headerPartition the MXF header partition
     * @param imfErrorLogger - an object for logging errors
     * @return the same header partition wrapped in a HeaderPartitionOP1A object
     */
    @SuppressWarnings({"PMD.NcssMethodCount","PMD.CollapsibleIfStatements"})
    public static HeaderPartitionOP1A checkOperationalPattern1ACompliance(@Nonnull HeaderPartition headerPartition, @Nonnull IMFErrorLogger imfErrorLogger)
    {

        Preface preface = headerPartition.getPreface();
        String trackFileID_Prefix = "";
        if(preface != null) {
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage;
            filePackage = (SourcePackage) genericPackage;
            UUID packageID = filePackage.getPackageMaterialNumberasUUID();
            trackFileID_Prefix = String.format("TrackFile ID : %s - ", packageID.toString());
        }

        //Section 9.5.1 st377-1:2011
        if(preface == null){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Preface does not exist in the header partition, which is invalid."));
        }
        //Preface
        else
        {
            //check 'Operational Pattern' field in Preface
            byte[] bytes = preface.getOperationalPattern().getULAsBytes();
            for (int i=0; i< bytes.length; i++)
            {
                //An IMF track file shall conform to the OP1A requirements as mandated by Section 5.1.1 #10 st2067-5:2013
                if( (MXFOperationalPattern1A.OPERATIONAL_PATTERN1A_KEY_MASK[i] != 0) &&
                        (MXFOperationalPattern1A.OPERATIONAL_PATTERN1A_KEY[i] != bytes[i]) )
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Operational Pattern field in preface = 0x%x at position (zero-indexed) = %d, is different from expected value = 0x%x",
                            bytes[i], i, MXFOperationalPattern1A.OPERATIONAL_PATTERN1A_KEY[i]));
                }
            }

            //check number of essence containers ULs Section 9.4.3 st377-1:2011
            if (preface.getNumberOfEssenceContainerULs() < 1)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Number of EssenceContainer ULs in preface = %d, at least 1 is expected",
                        preface.getNumberOfEssenceContainerULs()));
            }
        }

        //Content Storage
        {
            //check number of Content Storage sets Section 9.5.2 st377-1:2011
            if (headerPartition.getContentStorageList().size() != 1)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Number of Content Storage sets in header partition = %d, is different from 1",
                        headerPartition.getContentStorageList().size()));
            }

            if(preface != null) {
                ContentStorage contentStorage = preface.getContentStorage();
                //Section 9.5.2 st377-1:2011
                if (contentStorage == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("No Content Storage set was found in header partition"));
                }
                //check number of Essence Container Data sets referred by Content Storage Section 9.4.3 st377-1:2011
                else if (contentStorage.getNumberOfEssenceContainerDataSets() != 1) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Number of EssenceContainerData sets referred by Content Storage = %d, is different from 1",
                            contentStorage.getNumberOfEssenceContainerDataSets()));
                }
            }
        }

        //check number of Essence Container Data sets
        {
            //Section 9.4.3 st377-1:2011
            if (headerPartition.getEssenceContainerDataList().size() != 1)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Number of EssenceContainerData sets = %d, is different from 1",
                        headerPartition.getEssenceContainerDataList().size()));
            }
        }

        //check number of Material Packages Section 5.1, Table-1 st378:2004
        if (headerPartition.getMaterialPackages().size() != 1)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Number of Material Packages in header partition = %d, is different from 1",
                    headerPartition.getMaterialPackages().size()));
        }

        //Material Package
        {
            MaterialPackage materialPackage = (MaterialPackage)headerPartition.getMaterialPackages().get(0);

            //check number of source clips per track of Material Package Section 5.1, Table-1 st378:2004
            for (TimelineTrack timelineTrack : materialPackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                //Section 9.5.3 st377-1:2011
                if (sequence == null)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("TimelineTrack with instanceUID = %s has no sequence",
                            timelineTrack.getInstanceUID()));
                }
                else if (!sequence.getMxfDataDefinition().equals(MXFDataDefinition.OTHER))
                {
                    //Section 5.1, Table-1 st378:2004
                    if (sequence.getSourceClips().size() != 1)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Material Package Sequence with UID = %s has %d source clips, exactly one is allowed",
                                sequence.getInstanceUID(), sequence.getSourceClips().size()));
                    }
                }
            }

            //check if Material Package accesses a single source package Section 5.1, Table-1 st378:2004
            MXFUID referencedSourcePackageUMID = null;
            for (TimelineTrack timelineTrack : materialPackage.getTimelineTracks())
            {
                Sequence sequence = timelineTrack.getSequence();
                if (sequence != null
                        && !sequence.getMxfDataDefinition().equals(MXFDataDefinition.OTHER))
                {
                    MXFUID thisSourcePackageUMID = sequence.getSourceClips().get(0).getSourcePackageID();
                    if (referencedSourcePackageUMID == null)
                    {
                        referencedSourcePackageUMID = thisSourcePackageUMID;
                    }
                    else if (!referencedSourcePackageUMID.equals(thisSourcePackageUMID))
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("SourceClipUID = %s refers to source package UID = %s different from other source clips that refer to source package UID = %s",
                                sequence.getInstanceUID(), thisSourcePackageUMID, referencedSourcePackageUMID));
                    }
                }
            }

            if(referencedSourcePackageUMID == null){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Invalid source package UID, perhaps because one or more timelineTrack has no sequence"));
            }
            if(preface != null) {
                //check if SourcePackageID referenced from Material Package is present in ContentStorage
                ContentStorage contentStorage = preface.getContentStorage();
                if (contentStorage == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("No Content Storage set was found in header partition"));
                } else {
                    boolean foundReferenceForReferencedSourcePackageUMIDInContentStorage = false;
                    for (SourcePackage sourcePackage : contentStorage.getSourcePackageList()) {
                        if (sourcePackage.getPackageUID().equals(referencedSourcePackageUMID)) {
                            foundReferenceForReferencedSourcePackageUMIDInContentStorage = true;
                            break;
                        }
                    }
                    if (!foundReferenceForReferencedSourcePackageUMIDInContentStorage) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Content Storage does not refer to Source Package with packageUID = %s", referencedSourcePackageUMID));
                    }

                    //check if SourcePackageID referenced from Material Package is the same as that referred by EssenceContainer Data set
                    MXFUID linkedPackageUID = contentStorage.getEssenceContainerDataList().get(0).getLinkedPackageUID();
                    if (referencedSourcePackageUMID != null
                            && !linkedPackageUID.equals(referencedSourcePackageUMID)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Package UID = %s referred by EssenceContainerData set is different from %s which is referred by Material Package",
                                linkedPackageUID, referencedSourcePackageUMID));
                    }
                }
            }
        }

        //check if all tracks across the Material Package and the top-level File Package have the same duration st377-1:2011
        double sequenceDuration = 0.0;
        {
            MaterialPackage materialPackage = (MaterialPackage)headerPartition.getMaterialPackages().get(0);
            for (TimelineTrack timelineTrack : materialPackage.getTimelineTracks())
            {

                long thisEditRateNumerator = timelineTrack.getEditRateNumerator();
                long thisEditRateDenominator = timelineTrack.getEditRateDenominator();
                if (thisEditRateNumerator == 0)
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Timeline Track %s has invalid edit rate : numerator = %d, denominator = %d",
                            timelineTrack.getInstanceUID(), thisEditRateNumerator, thisEditRateDenominator));
                }

                Sequence sequence = timelineTrack.getSequence();

                if (sequence != null
                        && !sequence.getMxfDataDefinition().equals(MXFDataDefinition.OTHER))
                {
                    double thisSequenceDuration = ((double)sequence.getDuration()*(double)thisEditRateDenominator)/(double)thisEditRateNumerator;
                    if (Math.abs(sequenceDuration) < MXFOperationalPattern1A.EPSILON)
                    {
                        sequenceDuration = thisSequenceDuration;
                    }
                    else if (Math.abs(sequenceDuration - thisSequenceDuration) > MXFOperationalPattern1A.TOLERANCE)
                    {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Material Package SequenceUID = %s is associated with duration = %f, which is different from expected value %f",
                                sequence.getInstanceUID(), thisSequenceDuration, sequenceDuration));
                    }
                }
            }

            if(preface != null
                    && preface.getContentStorage() != null) {
                SourcePackage filePackage = (SourcePackage) preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {

                    long thisEditRateNumerator = timelineTrack.getEditRateNumerator();
                    long thisEditRateDenominator = timelineTrack.getEditRateDenominator();
                    if (thisEditRateNumerator == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("Timeline Track %s has invalid edit rate : numerator = %d, denominator = %d",
                                timelineTrack.getInstanceUID(), thisEditRateNumerator, thisEditRateDenominator));
                    }

                    Sequence sequence = timelineTrack.getSequence();
                    if (sequence != null
                            && !sequence.getMxfDataDefinition().equals(MXFDataDefinition.OTHER)) {
                        double thisSequenceDuration = ((double) sequence.getDuration() * (double) thisEditRateDenominator) / (double) thisEditRateNumerator;
                        if (Math.abs(sequenceDuration) < MXFOperationalPattern1A.EPSILON) {
                            sequenceDuration = thisSequenceDuration;
                        } else if (Math.abs(sequenceDuration - thisSequenceDuration) > MXFOperationalPattern1A.TOLERANCE) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + trackFileID_Prefix + String.format("File Package SequenceUID = %s is associated with duration = %f, which is different from expected value %f",
                                    sequence.getInstanceUID(), thisSequenceDuration, sequenceDuration));
                        }
                    }
                }
            }

        }

        if(imfErrorLogger.hasFatalErrors()){
            throw new MXFException(String.format("Found fatal errors in the IMFTrackFile that violate IMF OP1A compliance"), imfErrorLogger);
        }
        return new HeaderPartitionOP1A(headerPartition);
    }

    /**
     * Checks the compliance of partition packs found in an MXF file with st378:2004. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param partitionPacks the list of partition packs for which the compliance check is performed
     */
    public static void checkOperationalPattern1ACompliance(List<PartitionPack> partitionPacks)
    {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        for(PartitionPack partitionPack : partitionPacks)
        {
            //check 'Operational Pattern' field in PartitionPack
            byte[] bytes = partitionPack.getOperationalPattern();
            for (int i=0; i< bytes.length; i++)
            {
                if( (MXFOperationalPattern1A.OPERATIONAL_PATTERN1A_KEY_MASK[i] != 0) &&
                        (MXFOperationalPattern1A.OPERATIONAL_PATTERN1A_KEY[i] != bytes[i]) )
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + String.format("Operational Pattern field in preface = 0x%x at position (zero-indexed) = %d, is different from expected value = 0x%x",
                            bytes[i], i, MXFOperationalPattern1A.OPERATIONAL_PATTERN1A_KEY[i]));
                }
            }

            //check number of essence containers
            if (partitionPack.getNumberOfEssenceContainerULs() < 1)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, MXFOperationalPattern1A.OP1A_EXCEPTION_PREFIX + String.format("Number of EssenceContainer ULs in partition pack = %d, at least 1 is expected",
                        partitionPack.getNumberOfEssenceContainerULs()));
            }
        }
        if(imfErrorLogger.hasFatalErrors()){
            throw new MXFException(String.format("Found fatal errors in the IMFTrackFile that violate IMF OP1A compliance"), imfErrorLogger);
        }
    }

    /**
     * This class wraps an MXF header partition object - wrapping can be done
     * only if the header partition object is compliant with st378:2004
     */
    public static class HeaderPartitionOP1A
    {
        private final HeaderPartition headerPartition;

        private HeaderPartitionOP1A(HeaderPartition headerPartition)
        {
            this.headerPartition = headerPartition;
        }

        /**
         * Gets the wrapped MXF header partition object
         * @return the wrapped MXF header partition object
         */
        public HeaderPartition getHeaderPartition()
        {
            return this.headerPartition;
        }

        /**
         * A method that returns a string representation of a HeaderPartitionOP1A object
         *
         * @return string representing the object
         */
        public String toString()
        {
            return this.headerPartition.toString();
        }

    }
}
