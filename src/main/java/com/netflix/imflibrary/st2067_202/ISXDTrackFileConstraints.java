package com.netflix.imflibrary.st2067_202;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.header.*;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class ISXDTrackFileConstraints {

    private static final String IMF_ISXD_EXCEPTION_PREFIX = "IMF ISXD check: ";

    // Prevent instantiation
    public ISXDTrackFileConstraints() {}

    public static void checkCompliance(IMFConstraints.HeaderPartitionIMF headerPartitionIMF, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        HeaderPartition headerPartition = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        List<UL> partitionPackEssenceContainerULs = headerPartition.getPartitionPack().getEssenceContainerULs();

        // Ensure that Essence Container UL in the Partition Pack is the one for ISXD
        if (!partitionPackEssenceContainerULs.contains(ISXDDataEssenceDescriptor.getEssenceContainerUL())) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                    String.format("The MXF Partition Pack does not contain the ISXD Data Essence Container UL."));
        }

        Preface preface = headerPartition.getPreface();

        // Ensure that Essence Container UL in the Preface is the one for ISXD
        List<UL> prefaceEssenceContainerULs = preface.getEssenceContainerULs();
        if (!prefaceEssenceContainerULs.contains(ISXDDataEssenceDescriptor.getEssenceContainerUL())) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                    String.format("The MXF Preface does not contain the ISXD Data Essence Container UL."));
        }

        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage;
        filePackage = (SourcePackage) genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();

        for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {
            Sequence sequence = timelineTrack.getSequence();
            if (sequence == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                if (genericDescriptor instanceof ISXDDataEssenceDescriptor) { // Support for st2067-202

                    ISXDDataEssenceDescriptor isxdEssenceDescriptor = (ISXDDataEssenceDescriptor) genericDescriptor;

                    // Section 9.1
                    if (!isxdEssenceDescriptor.getEssenceContainerUL().equals(ISXDDataEssenceDescriptor.IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("The MXF ISXDEssenceDescriptor does not contain the ISXD Data Essence Container UL, but %s.", isxdEssenceDescriptor.getEssenceContainerUL().toString()));
                    }

                    // Section 9.3
                    if (!isxdEssenceDescriptor.getDataEssenceCoding().equals(ISXDDataEssenceDescriptor.UTF8_TEXT_DATA_ESSENCE_CODING_LABEL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("Data Essence Coding shall be %s but is %s.", ISXDDataEssenceDescriptor.UTF8_TEXT_DATA_ESSENCE_CODING_LABEL.toString(), isxdEssenceDescriptor.getDataEssenceCoding().toString()));
                    }
                }
            }
        }
    }

    public static void checkIndexEditRate(IMFConstraints.HeaderPartitionIMF headerPartitionIMF, IndexTableSegment indexTableSegment, IMFErrorLogger imfErrorLogger) {
        HeaderPartition headerPartition = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        Preface preface = headerPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage;
        filePackage = (SourcePackage) genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();

        for (TimelineTrack timelineTrack : filePackage.getTimelineTracks()) {
            Sequence sequence = timelineTrack.getSequence();
            if (sequence == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX + String.format("TimelineTrack with instanceUID = %s in the IMFTrackFile represented by ID %s has no sequence.",
                        timelineTrack.getInstanceUID(), packageID.toString()));
            } else {
                GenericDescriptor genericDescriptor = filePackage.getGenericDescriptor();
                if (genericDescriptor instanceof ISXDDataEssenceDescriptor) { // Support for st2067-201
                    ISXDDataEssenceDescriptor isxdEssenceDescriptor = (ISXDDataEssenceDescriptor) genericDescriptor;

                    // Section 5.7
                    if (timelineTrack.getEditRateNumerator() != indexTableSegment.getIndexEditRate().getNumerator() || timelineTrack.getEditRateDenominator() != indexTableSegment.getIndexEditRate().getDenominator()) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("Timeline Track Edit Rate %d/%d does not match Index Table Segment Index Edit Rate %d/%d in the IMFTrackFile represented by ID %s.", timelineTrack.getEditRateNumerator(), timelineTrack.getEditRateDenominator(), indexTableSegment.getIndexEditRate().getNumerator(), indexTableSegment.getIndexEditRate().getDenominator(), packageID.toString()));
                    }


                }
            }
        }
    }
}
