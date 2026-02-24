package com.netflix.imflibrary.st2067_202;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.netflix.imflibrary.st2067_202.ISXDDataEssenceDescriptor.IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL;
import static com.netflix.imflibrary.st2067_202.ISXDDataEssenceDescriptor.UTF8_TEXT_DATA_ESSENCE_CODING_LABEL;

public class IMFISXDConstraintsChecker {

    public static List<ErrorLogger.ErrorObject> checkISXDVirtualTrack(Composition.EditRate compositionEditRate,
                                                                     Composition.VirtualTrack virtualTrack,
                                                                     Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if (!virtualTrack.getSequenceType().equals("ISXDSequence")) return imfErrorLogger.getErrors();

        List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
        for(IMFBaseResourceType baseResource : virtualTrackResourceList) {
            IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(baseResource);
            DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
            if (!domNodeObjectModel.getLocalName().equals("ISXDDataEssenceDescriptor")) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                "an ISXD VirtualTrack Resource does not have an ISXDDataEssenceDescriptor but %s",
                        imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getLocalName()));
            } else {
                // this is actually a ISXDDataEssenceDescriptor, so check the descriptor constraints
                if (domNodeObjectModel.getChildrenDOMNodes().size() == 0) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                            "an ISXD VirtualTrack Resource has no SubDescriptor, but a ContainerConstraintsSubDescriptor shall be present per ST 379-2.", imfTrackFileResourceType.getSourceEncoding()));
                }

                if (!domNodeObjectModel.getFieldAsUL("ContainerFormat").equalsWithMask(IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL, 0b1111111011111111)) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by " +
                                    "an ISXD VirtualTrack Resource does not use the correct Essence Container UL: %s vs. %s",
                            imfTrackFileResourceType.getSourceEncoding(), domNodeObjectModel.getFieldAsUL("ContainerFormat"), IMF_ISXD_ESSENCE_FRAME_WRAPPED_CONTAINER_UL));
                }
            }

            // Per ST 2067-202 Section 6: The Edit Rate of an ISXD Virtual Track shall be equal to the Edit Rate of the Main Image Virtual Track as defined in SMPTE ST 2067-2.
            if (imfTrackFileResourceType.getEditRate().getNumerator() * compositionEditRate.getDenominator() !=
                    imfTrackFileResourceType.getEditRate().getDenominator() * compositionEditRate.getNumerator()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The EditRate %s/%s of resource %s is not equal to the EditRate of the Main Image Virtual Track %s/%s",
                        imfTrackFileResourceType.getEditRate().getNumerator(), imfTrackFileResourceType.getEditRate().getDenominator(), imfTrackFileResourceType.getId(), compositionEditRate.getNumerator(), compositionEditRate.getDenominator()));
            }
        }
        return imfErrorLogger.getErrors();
    }
}
