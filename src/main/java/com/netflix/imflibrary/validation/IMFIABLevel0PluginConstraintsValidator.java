package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_201.IABTrackFileConstraints;
import com.netflix.imflibrary.st2067_201.IMFIABConstraintsChecker;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;

import java.io.IOException;
import java.util.*;

public class IMFIABLevel0PluginConstraintsValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateTrackFileConstraints(IMFTrackFileReader imfTrackFileReader) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = imfTrackFileReader.getHeaderPartitionIMF(imfErrorLogger);
            IABTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
        } catch (IOException e) {
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(
                    IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR,
                    IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Exception while retrieving Track File information for validation: %s", e)));

        }

        return imfErrorLogger.getErrors();
    }


    @Override
    public String getConstraintsSpecification() {
        return "IMF IAB Level 0 Plugin SMPTE ST2067-201";
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(IMFCompositionPlaylist IMFCompositionPlaylist) {

        Composition.EditRate editRate = IMFCompositionPlaylist.getEditRate();
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = IMFCompositionPlaylist.getVirtualTrackMap();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = IMFCompositionPlaylist.getEssenceDescriptorListMap();

        return IMFIABConstraintsChecker.checkIABVirtualTrack(editRate, virtualTrackMap, essenceDescriptorListMap, Set.of());
    }

}
