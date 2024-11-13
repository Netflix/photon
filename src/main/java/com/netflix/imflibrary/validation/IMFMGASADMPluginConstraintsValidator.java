package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_203.MGASADMTrackFileConstraints;
import com.netflix.imflibrary.st2067_203.IMFMGASADMConstraintsChecker;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IMFMGASADMPluginConstraintsValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateTrackFileConstraints(IMFTrackFileReader imfTrackFileReader) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = imfTrackFileReader.getHeaderPartitionIMF(imfErrorLogger);
            MGASADMTrackFileConstraints.checkCompliance(headerPartitionIMF, imfErrorLogger);
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

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        Composition.EditRate editRate = IMFCompositionPlaylist.getEditRate();
        Map<UUID, ? extends Composition.VirtualTrack> virtualTrackMap = IMFCompositionPlaylist.getVirtualTrackMap();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = IMFCompositionPlaylist.getEssenceDescriptorListMap();

        // ST 2067-203 MGASADMVirtualTrackParameterSet checks
        List<ErrorLogger.ErrorObject> errors = IMFMGASADMConstraintsChecker.checkMGASADMVirtualTrackParameterSet(IMFCompositionPlaylist);
        imfErrorLogger.addAllErrors(errors);

        errors = IMFMGASADMConstraintsChecker.checkMGASADMVirtualTrack(editRate, virtualTrackMap, essenceDescriptorListMap, Set.of());
        imfErrorLogger.addAllErrors(errors);

        return imfErrorLogger.getErrors();
    }

}
