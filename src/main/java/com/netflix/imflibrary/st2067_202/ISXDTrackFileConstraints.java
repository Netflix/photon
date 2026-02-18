package com.netflix.imflibrary.st2067_202;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.header.*;

import com.netflix.imflibrary.st379_2.ContainerConstraintsSubDescriptor;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ISXDTrackFileConstraints {

    private static final String IMF_ISXD_EXCEPTION_PREFIX = "IMF ISXD check: ";

    // Prevent instantiation
    public ISXDTrackFileConstraints() {}

    public static void checkCompliance(IMFConstraints.HeaderPartitionIMF headerPartitionIMF, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        HeaderPartition headerPartition = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        List<UL> partitionPackEssenceContainerULs = headerPartition.getPartitionPack().getEssenceContainerULs();
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors();
        Preface preface = headerPartition.getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage) genericPackage;
        UUID packageID = filePackage.getPackageMaterialNumberasUUID();

        // Ensure that Essence Container UL in the Partition Pack is the one for ISXD
        if (!partitionPackEssenceContainerULs.contains(ISXDDataEssenceDescriptor.getEssenceContainerUL())) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                    String.format("The MXF Partition Pack does not contain the ISXD Data Essence Container UL."));
        }

        // Ensure that Essence Container UL in the Preface is the one for ISXD
        List<UL> prefaceEssenceContainerULs = preface.getEssenceContainerULs();
        if (!prefaceEssenceContainerULs.contains(ISXDDataEssenceDescriptor.getEssenceContainerUL())) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                    String.format("The MXF Preface does not contain the ISXD Data Essence Container UL."));
        }

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
                    UL essenceContainerUL = isxdEssenceDescriptor.getEssenceContainerUL();
                    if (essenceContainerUL == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("The MXF ISXDEssenceDescriptor does not contain an Essence Container UL."));
                    } else if (!essenceContainerUL.equals(ISXDDataEssenceDescriptor.IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("The MXF ISXDEssenceDescriptor does not contain the ISXD Data Essence Container UL, but %s.", essenceContainerUL.toString()));
                    }

                    // Section 9.3
                    UL dataEssenceCoding = isxdEssenceDescriptor.getDataEssenceCoding();
                    if (dataEssenceCoding == null) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("The MXF ISXDEssenceDescriptor does not contain a Data Essence Coding UL."));
                    } else if (!dataEssenceCoding.equals(ISXDDataEssenceDescriptor.UTF8_TEXT_DATA_ESSENCE_CODING_LABEL)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("Data Essence Coding shall be %s but is %s.", ISXDDataEssenceDescriptor.UTF8_TEXT_DATA_ESSENCE_CODING_LABEL.toString(), dataEssenceCoding.toString()));
                    }

                    if (subDescriptors.size() == 0) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("ISXDDataEssenceDescriptor in the IMFTrackFile represented by ID %s does not have subdescriptors, but a ContainerConstraintsSubDescriptor shall be present per ST 379-2.", packageID.toString()));
                    } else {
                        // ContainerConstraintsSubDescriptor required per ST 379-2
                        List<InterchangeObject.InterchangeObjectBO> containerConstraintsSubDescriptors = subDescriptors.subList(0, subDescriptors.size()).stream().filter(interchangeObjectBO -> interchangeObjectBO.getClass().getEnclosingClass().equals(ContainerConstraintsSubDescriptor.class)).collect(Collectors.toList());
                        if (containerConstraintsSubDescriptors.isEmpty()) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("Track File with ID %s: A ContainerConstraintsSubDescriptor shall be present per ST 379-2, but is missing", packageID.toString()));
                        } else if (containerConstraintsSubDescriptors.size() != 1) {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                                    IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                    String.format("Track File with ID %s: One ContainerConstraintsSubDescriptor shall be present per ST 379-2, but %d are present", packageID.toString(), containerConstraintsSubDescriptors.size()));
                        }
                    }
                } else {
                    // Section 9.2
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                            String.format("Track File with ID %s does not contain ISXDDataEssenceDescriptor", packageID.toString()));
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
                if (genericDescriptor instanceof ISXDDataEssenceDescriptor) { // Support for st2067-202
                    ISXDDataEssenceDescriptor isxdEssenceDescriptor = (ISXDDataEssenceDescriptor) genericDescriptor;

                    // index edit rate is expected to match essence edit rate
                    if ( (indexTableSegment.getIndexEditRate().getDenominator() == 0 && indexTableSegment.getIndexEditRate().getNumerator() == 0) ||
                            (timelineTrack.getEditRateNumerator() * indexTableSegment.getIndexEditRate().getDenominator() !=
                            indexTableSegment.getIndexEditRate().getNumerator() * timelineTrack.getEditRateDenominator())) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ISXD_EXCEPTION_PREFIX +
                                String.format("Timeline Track Edit Rate %d/%d does not match Index Table Segment Index Edit Rate %d/%d in the IMFTrackFile represented by ID %s.", timelineTrack.getEditRateNumerator(), timelineTrack.getEditRateDenominator(), indexTableSegment.getIndexEditRate().getNumerator(), indexTableSegment.getIndexEditRate().getDenominator(), packageID.toString()));
                    }


                }
            }
        }
    }
}
