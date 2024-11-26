package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;

import java.util.*;

public class ConstraintsValidatorUtils {

    /**
     * Checks homogeneity of a generic virtual track by collecting essence descriptors for all virtual track resources
     * and comparing the items specified in the provided homogeneity set
     *
     * @param virtualTrack the virtual track
     * @param essenceDescriptorListMap a map of essence descriptors
     * @param homogeneitySelectionSet the specific list of items for which homogeneity will be checked
     * @return a list of errors
     */
    public static List<ErrorLogger.ErrorObject> checkVirtualTrackHomogeneity(Composition.VirtualTrack virtualTrack,
                                                                             Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap,
                                                                             Set<String> homogeneitySelectionSet){

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        if (virtualTrack == null)
            return imfErrorLogger.getErrors();

        if (homogeneitySelectionSet == null || homogeneitySelectionSet.isEmpty())
            return imfErrorLogger.getErrors();

        if (essenceDescriptorListMap == null || essenceDescriptorListMap.isEmpty())
            return imfErrorLogger.getErrors();

        if (virtualTrack.getSequenceTypeEnum() == Composition.SequenceTypeEnum.MarkerSequence)
            return imfErrorLogger.getErrors();

        List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
        List<DOMNodeObjectModel> virtualTrackEssenceDescriptors = new ArrayList<>();
        for(IMFBaseResourceType imfBaseResourceType : virtualTrackResourceList){

            IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(imfBaseResourceType);
            DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
            //Section 6.8 st2067-2:2016
            if(domNodeObjectModel == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by a " +
                        "VirtualTrack Resource does not have a corresponding EssenceDescriptor in the EssenceDescriptorList in the CPL", imfTrackFileResourceType.getSourceEncoding()));
                return imfErrorLogger.getErrors();
            }

            virtualTrackEssenceDescriptors.add(domNodeObjectModel);
        }

        if( !virtualTrackEssenceDescriptors.isEmpty() ){
            boolean isVirtualTrackHomogeneous = true;
            Set<String> homogeneitySelectionSetAll = new HashSet<>(homogeneitySelectionSet);

            DOMNodeObjectModel refDOMNodeObjectModel = virtualTrackEssenceDescriptors.get(0).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(0), homogeneitySelectionSetAll);
            for (int i = 1; i < virtualTrackEssenceDescriptors.size(); i++) {
                DOMNodeObjectModel other = virtualTrackEssenceDescriptors.get(i).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(i), homogeneitySelectionSetAll);
                isVirtualTrackHomogeneous &= refDOMNodeObjectModel.equals(other);
            }
            List<DOMNodeObjectModel> modelsIgnoreSet = new ArrayList<>();
            if (!isVirtualTrackHomogeneous) {
                for(int i = 1; i< virtualTrackEssenceDescriptors.size(); i++){
                    DOMNodeObjectModel other = virtualTrackEssenceDescriptors.get(i).createDOMNodeObjectModelSelectionSet(virtualTrackEssenceDescriptors.get(i), homogeneitySelectionSetAll);
                    modelsIgnoreSet.add(other);
                    imfErrorLogger.addAllErrors(DOMNodeObjectModel.getNamespaceURIMismatchErrors(refDOMNodeObjectModel, other));
                }
                //Section 6.2 st2067-2:2016
                imfErrorLogger.addAllErrors(refDOMNodeObjectModel.getErrors());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("The VirtualTrack represented by ID %s is not homogeneous based on a comparison of the EssenceDescriptors referenced by its resources in the Essence Descriptor List, " +
                                "the EssenceDescriptors corresponding to this VirtualTrack in the EssenceDescriptorList are as follows %n%n%s", virtualTrack.getTrackID().toString(), Utilities.serializeObjectCollectionToString(modelsIgnoreSet)));
            }
        }

        return imfErrorLogger.getErrors();
    }

}
