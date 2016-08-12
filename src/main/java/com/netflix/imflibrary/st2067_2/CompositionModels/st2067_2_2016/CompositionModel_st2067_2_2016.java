package com.netflix.imflibrary.st2067_2.CompositionModels.st2067_2_2016;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.st2067_2.CompositionModels.*;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2016;


import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * A class that models aspects of a Composition such as a VirtualTrack, TrackResources etc. compliant with the 2016 CompositionPlaylist specification st2067-3:2016.
 */
public final class CompositionModel_st2067_2_2016 {

    //To prevent instantiation
    private CompositionModel_st2067_2_2016(){

    }

    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding VirtualTrack
     */
    public static IMFCompositionPlaylistType getCompositionPlaylist (@Nonnull org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<IMFSegmentType> segmentList = new ArrayList<IMFSegmentType>();
        for (org.smpte_ra.schemas.st2067_2_2016.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            List<IMFSequenceType> sequenceList = new ArrayList<IMFSequenceType>();
            org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;

            /* Parse Marker sequence */
            sequence = segment.getSequenceList().getMarkerSequence();
            if (sequence != null)
            {
                UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                /**
                 * A LinkedList seems appropriate since we want to preserve the order of the Resources referenced
                 * by a virtual track to recreate the presentation. Since the LinkedList implementation is not
                 * synchronized wrapping it around a synchronized list collection, although in this case it
                 * is perhaps not required since this method is only invoked from the context of the constructor.
                 */
                List<IMFBaseResourceType> baseResources = Collections.synchronizedList(new LinkedList<>());
                for (org.smpte_ra.schemas.st2067_2_2016.BaseResourceType resource : sequence.getResourceList().getResource()) {
                    IMFBaseResourceType baseResource = null;
                    if (resource instanceof org.smpte_ra.schemas.st2067_2_2016.MarkerResourceType) {
                        org.smpte_ra.schemas.st2067_2_2016.MarkerResourceType markerResource =
                                (org.smpte_ra.schemas.st2067_2_2016.MarkerResourceType) resource;

                        List<IMFMarkerType> markerList = new ArrayList<IMFMarkerType>();
                        for (org.smpte_ra.schemas.st2067_2_2016.MarkerType marker : markerResource.getMarker()) {
                            markerList.add(new IMFMarkerType(marker.getAnnotation().getValue(),
                                    new IMFMarkerType.Label(marker.getLabel().getValue(), marker.getLabel().getScope()),
                                    marker.getOffset()));
                        }

                        baseResource = new IMFMarkerResourceType(
                                markerResource.getId(),
                                markerResource.getEditRate().size() != 0  ? markerResource.getEditRate() : compositionPlaylistType.getEditRate(),
                                markerResource.getIntrinsicDuration(),
                                markerResource.getEntryPoint(),
                                markerResource.getSourceDuration(),
                                markerResource.getRepeatCount(),
                                markerList);
                    } else {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported Resource type in Marker Sequence");
                    }

                    if (baseResource != null) {
                        baseResources.add(baseResource);
                    }
                }
                sequenceList.add(new IMFSequenceType(sequence.getId(),
                        sequence.getTrackId(),
                        Composition.SequenceTypeEnum.MarkerSequence,
                        Collections.synchronizedList(baseResources)));
            }

            /* Parse rest of the sequences */
            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                String name = jaxbElement.getName().getLocalPart();
                sequence = (org.smpte_ra.schemas.st2067_2_2016.SequenceType)(jaxbElement).getValue();
                if (sequence != null)
                {
                    UUID uuid = UUIDHelper.fromUUIDAsURNStringToUUID(sequence.getTrackId());
                    /**
                     * A LinkedList seems appropriate since we want to preserve the order of the Resources referenced
                     * by a virtual track to recreate the presentation. Since the LinkedList implementation is not
                     * synchronized wrapping it around a synchronized list collection, although in this case it
                     * is perhaps not required since this method is only invoked from the context of the constructor.
                     */
                    List<IMFBaseResourceType> baseResources = Collections.synchronizedList(new LinkedList<>());
                    for (org.smpte_ra.schemas.st2067_2_2016.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        IMFBaseResourceType baseResource = null;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType) resource;

                            baseResource = new IMFTrackFileResourceType(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate().size() != 0  ? trackFileResource.getEditRate() : compositionPlaylistType.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding(),
                                    trackFileResource.getHash(),
                                    trackFileResource.getHashAlgorithm().getAlgorithm()
                            );
                        }
                        else
                        {
                            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported Resource type");
                        }

                        if(baseResource != null)
                        {
                            baseResources.add(baseResource);
                        }
                    }
                    sequenceList.add(new IMFSequenceType(sequence.getId(),
                            sequence.getTrackId(),
                            Composition.SequenceTypeEnum.getSequenceTypeEnum(name),
                            Collections.synchronizedList(baseResources)));
                }
            }
            sequenceList = Collections.unmodifiableList(sequenceList);
            segmentList.add(new IMFSegmentType(segment.getId(), Collections.synchronizedList(sequenceList)));
        }
        segmentList = Collections.unmodifiableList(segmentList);

        List<IMFEssenceDescriptorBaseType> essenceDescriptorList = new ArrayList<IMFEssenceDescriptorBaseType>();

        if(compositionPlaylistType.getEssenceDescriptorList() != null &&
                compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor().size() >= 1)
        {
            for (org.smpte_ra.schemas.st2067_2_2016.EssenceDescriptorBaseType essenceDescriptor : compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor()) {
                essenceDescriptorList.add(new IMFEssenceDescriptorBaseType(essenceDescriptor.getId(),
                        essenceDescriptor.getAny()));
            }
        }
        essenceDescriptorList = Collections.unmodifiableList(essenceDescriptorList);


        return new IMFCompositionPlaylistType( compositionPlaylistType.getId(),
                compositionPlaylistType.getEditRate(),
                (compositionPlaylistType.getAnnotation() == null ? null : compositionPlaylistType.getAnnotation().getValue()),
                (compositionPlaylistType.getIssuer() == null ? null : compositionPlaylistType.getIssuer().getValue()),
                (compositionPlaylistType.getCreator() == null ? null : compositionPlaylistType.getCreator().getValue()),
                (compositionPlaylistType.getContentOriginator() == null ? null : compositionPlaylistType.getContentOriginator().getValue()),
                (compositionPlaylistType.getContentTitle() == null ? null : compositionPlaylistType.getContentTitle().getValue()),
                Collections.synchronizedList(segmentList),
                Collections.synchronizedList(essenceDescriptorList));
    }
}