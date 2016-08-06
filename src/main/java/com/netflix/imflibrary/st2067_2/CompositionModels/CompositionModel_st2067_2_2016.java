package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.util.*;

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
    public static CompositionPlaylistType getCompositionPlayList (@Nonnull org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<SegmentType> segmentList = new ArrayList<SegmentType>();
        for (org.smpte_ra.schemas.st2067_2_2016.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            List<SequenceType> sequenceList = new ArrayList<SequenceType>();
            org.smpte_ra.schemas.st2067_2_2016.SequenceType sequence;
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
                    List<BaseResourceType> baseResources = Collections.synchronizedList(new LinkedList<>());
                    for (org.smpte_ra.schemas.st2067_2_2016.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        BaseResourceType baseResource;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2016.TrackFileResourceType) resource;

                            baseResource = new TrackFileResource_st2067_2_2016(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding(),
                                    trackFileResource.getHashAlgorithm()
                            );
                        }
                        else
                        {
                            baseResource = new BaseResourceType(resource.getId(),
                                    resource.getEditRate(),
                                    resource.getIntrinsicDuration(),
                                    resource.getEntryPoint(),
                                    resource.getSourceDuration(),
                                    resource.getRepeatCount());
                        }
                        baseResources.add(baseResource);
                    }
                    sequenceList.add(new SequenceType(sequence.getId(),
                            sequence.getTrackId(),
                            Composition.SequenceTypeEnum.getSequenceTypeEnum(name),
                            Collections.synchronizedList(baseResources)));
                }
            }
            sequenceList = Collections.unmodifiableList(sequenceList);
            segmentList.add(new SegmentType(segment.getId(), Collections.synchronizedList(sequenceList)));
        }
        segmentList = Collections.unmodifiableList(segmentList);

        return new CompositionPlaylistType( compositionPlaylistType.getId(),
                compositionPlaylistType.getEditRate(),
                (compositionPlaylistType.getAnnotation() == null ? null : compositionPlaylistType.getAnnotation().getValue()),
                (compositionPlaylistType.getIssuer() == null ? null : compositionPlaylistType.getIssuer().getValue()),
                (compositionPlaylistType.getCreator() == null ? null : compositionPlaylistType.getCreator().getValue()),
                (compositionPlaylistType.getContentOriginator() == null ? null : compositionPlaylistType.getContentOriginator().getValue()),
                (compositionPlaylistType.getContentTitle() == null ? null : compositionPlaylistType.getContentTitle().getValue()),
                Collections.synchronizedList(segmentList));
    }
}
