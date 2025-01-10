package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.*;

public class IMFCoreConstraints2013Validator extends IMFCoreConstraintsValidator {

    private static final String CCNamespaceURI = "http://www.smpte-ra.org/schemas/2067-2/2013";

    public static final List<String> SUPPORTED_TIMED_TEXT_SEQUENCES = Collections.unmodifiableList(Arrays.asList(
            SUBTITLES_SEQUENCE, HEARING_IMPAIRED_CAPTIONS_SEQUENCE, VISUALLY_IMPAIRED_SEQUENCE, COMMENTARY_SEQUENCE, KARAOKE_SEQUENCE));

    @Override
    public String getConstraintsSpecification() {
        return "SMPTE ST 2067-2:2013 IMF Core Constraints";
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        imfErrorLogger.addAllErrors(checkVirtualTracks(imfCompositionPlaylist));

        imfErrorLogger.addAllErrors(checkNamespaceURI(imfCompositionPlaylist));

        // check if MainAudioSequence present per Section 6.3.2 st2067-2:2016 and Section 6.9.3 st2067-3:2016
        boolean containsMainAudioSequence = imfCompositionPlaylist.getVirtualTrackMap().entrySet().stream()
                .map(Map.Entry::getValue)
                .map(virtualTrack -> imfCompositionPlaylist.getSequenceTypeForVirtualTrackID(virtualTrack.getTrackID()))
                .anyMatch(virtualTrackSequenceName -> virtualTrackSequenceName.equals(MAIN_AUDIO_SEQUENCE));

        if (!containsMainAudioSequence) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, "The Composition does not contain a single main audio sequence in its first segment, one or more are required");
        }


        return imfErrorLogger.getErrors();
    }

    @Override
    protected String getNamespaceURI() {
        return CCNamespaceURI;
    }
}
