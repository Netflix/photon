package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.CoreConstraints;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.*;
import jakarta.annotation.Nonnull;

import java.util.*;

import static com.netflix.imflibrary.st2067_2.IMFCoreConstraintsChecker.checkVirtualTrackResourceList;


abstract public class IMFCPLValidator implements ConstraintsValidator {

    private static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-3/2013");
        add("http://www.smpte-ra.org/schemas/2067-3/2016");
    }});


    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {
        return List.of();
    }

    @Override
    public List<ErrorLogger.ErrorObject> validateCompositionConstraints(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, @Nonnull List<PayloadRecord> headerPartitionPayloads) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        // iterate over virtual tracks
        for (Map.Entry<UUID, ? extends Composition.VirtualTrack> virtualTrackEntry : imfCompositionPlaylist.getVirtualTrackMap().entrySet()) {
            Composition.VirtualTrack virtualTrack = virtualTrackEntry.getValue();

            // retrieve sequence namespace associated with the virtual track
            String virtualTrackSequenceNamespace = imfCompositionPlaylist.getSequenceNamespaceForVirtualTrackID(virtualTrack.getTrackID());

            // skip all virtual tracks that don't fall under CPL namespace
            if (!supportedCPLSchemaURIs.contains(virtualTrackSequenceNamespace)) {
                continue;
            }

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();
            imfErrorLogger.addAllErrors(checkVirtualTrackResourceList(virtualTrack.getTrackID(), virtualTrackResourceList));
        }


        // MARKER TRACK VALIDATION, CONTENT KIND VALUES, ETC

        return imfErrorLogger.getErrors();
    }


}
