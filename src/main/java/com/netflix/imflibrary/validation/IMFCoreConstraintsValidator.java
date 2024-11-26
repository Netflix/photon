package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFBaseResourceType;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.IMFCoreConstraintsChecker;
import com.netflix.imflibrary.utils.ErrorLogger;
import jakarta.annotation.Nonnull;

import java.util.*;

import static com.netflix.imflibrary.st2067_2.IMFCoreConstraintsChecker.checkVirtualTrackResourceList;


abstract public class IMFCoreConstraintsValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {
        return List.of();
    }


    protected List<ErrorLogger.ErrorObject> validateVirtualTracks(@Nonnull IMFCompositionPlaylist imfCompositionPlaylist, String namespaceURI) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        // iterate over virtual tracks
        for (Map.Entry<UUID, ? extends Composition.VirtualTrack> virtualTrackEntry : imfCompositionPlaylist.getVirtualTrackMap().entrySet()) {
            Composition.VirtualTrack virtualTrack = virtualTrackEntry.getValue();

            // retrieve sequence namespace associated with the virtual track
            String virtualTrackSequenceNamespace = imfCompositionPlaylist.getSequenceNamespaceForVirtualTrackID(virtualTrack.getTrackID());

            // skip all virtual tracks that don't fall under CPL namespace
            if (!namespaceURI.equals(virtualTrackSequenceNamespace)) {
                continue;
            }

            // todo: this needs cleanup, should move version specific checks into subclasses, general checks here, and probably just call checkVirtualTrackHomogeneity()
            imfErrorLogger.addAllErrors(IMFCoreConstraintsChecker.checkVirtualTracks(imfCompositionPlaylist));

        }

        return imfErrorLogger.getErrors();
    }

}
