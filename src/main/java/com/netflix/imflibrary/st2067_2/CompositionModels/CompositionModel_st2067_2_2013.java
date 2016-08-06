package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.UUIDHelper;


import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * A class that models aspects of a Composition such as a VirtualTrack, TrackResources etc. compliant with the 2013 CompositionPlaylist specification st2067-3:2013.
 */
public final class CompositionModel_st2067_2_2013 {

    //To prevent instantiation
    private CompositionModel_st2067_2_2013(){

    }

    /**
     * A stateless method that reads and parses all the virtual tracks of a Composition
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return a map containing mappings of a UUID to the corresponding VirtualTrack
     */
    public static IMFCompositionPlaylistType getCompositionPlaylist (@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<IMFSegmentType> segmentList = new ArrayList<IMFSegmentType>();
        for (org.smpte_ra.schemas.st2067_2_2013.SegmentType segment : compositionPlaylistType.getSegmentList().getSegment())
        {
            List<IMFSequenceType> sequenceList = new ArrayList<IMFSequenceType>();
            org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence;
            for (Object object : segment.getSequenceList().getAny())
            {
                if(!(object instanceof JAXBElement)){
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, "Unsupported sequence type or schema");
                    continue;
                }
                JAXBElement jaxbElement = (JAXBElement)(object);
                String name = jaxbElement.getName().getLocalPart();
                sequence = (org.smpte_ra.schemas.st2067_2_2013.SequenceType)(jaxbElement).getValue();
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
                    for (org.smpte_ra.schemas.st2067_2_2013.BaseResourceType resource : sequence.getResourceList().getResource())
                    {
                        BaseResourceType baseResource;
                        if(resource instanceof  org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType)
                        {

                            org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResource =
                                    (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType) resource;

                            baseResource = new IMFTrackFileResource_st2067_2_2013(
                                    trackFileResource.getId(),
                                    trackFileResource.getTrackFileId(),
                                    trackFileResource.getEditRate(),
                                    trackFileResource.getIntrinsicDuration(),
                                    trackFileResource.getEntryPoint(),
                                    trackFileResource.getSourceDuration(),
                                    trackFileResource.getRepeatCount(),
                                    trackFileResource.getSourceEncoding()
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
            for (org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType essenceDescriptor : compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor()) {
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
