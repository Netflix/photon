package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2016;
import org.w3c.dom.Element;


import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models aspects of a Composition such as a VirtualTrack, TrackResources etc. compliant with the 2016 CompositionPlaylist specification st2067-3:2016.
 */
final class CompositionModel_st2067_2_2016 {

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
                            markerList.add(new IMFMarkerType(marker.getAnnotation() == null ? null : marker
                                    .getAnnotation().getValue(),
                                    new IMFMarkerType.Label(marker.getLabel().getValue(), marker.getLabel().getScope()),
                                    marker.getOffset()));
                        }

                        try {
                            baseResource = new IMFMarkerResourceType(
                                    markerResource.getId(),
                                    markerResource.getEditRate().size() != 0 ? markerResource.getEditRate() : compositionPlaylistType.getEditRate(),
                                    markerResource.getIntrinsicDuration(),
                                    markerResource.getEntryPoint(),
                                    markerResource.getSourceDuration(),
                                    markerResource.getRepeatCount(),
                                    markerList);
                        }
                        catch(IMFException e)
                        {
                            imfErrorLogger.addAllErrors(e.getErrors());
                        }
                    } else {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, "Unsupported Resource type in Marker Sequence");
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
                    String details = "";
                    if(object instanceof Element)
                    {
                        Element element = Element.class.cast(object);
                        details = "Tag: " + element.getTagName() + " URI: " + element.getNamespaceURI();
                    }
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                            .IMFErrors.ErrorLevels.NON_FATAL, String.format("Unsupported sequence type or schema %s",
                            details));
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

                            try {
                                baseResource = new IMFTrackFileResourceType(
                                        trackFileResource.getId(),
                                        trackFileResource.getTrackFileId(),
                                        trackFileResource.getEditRate().size() != 0 ? trackFileResource.getEditRate() : compositionPlaylistType.getEditRate(),
                                        trackFileResource.getIntrinsicDuration(),
                                        trackFileResource.getEntryPoint(),
                                        trackFileResource.getSourceDuration(),
                                        trackFileResource.getRepeatCount(),
                                        trackFileResource.getSourceEncoding(),
                                        trackFileResource.getHash(),
                                        trackFileResource.getHashAlgorithm() == null?
                                                CompositionPlaylistBuilder_2016.defaultHashAlgorithm : trackFileResource
                                                .getHashAlgorithm().getAlgorithm()
                                );
                            }
                            catch(IMFException e)
                            {
                                imfErrorLogger.addAllErrors(e.getErrors());
                            }
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

        Set<String> applicationIDs = new LinkedHashSet<>();
        if(compositionPlaylistType.getExtensionProperties() != null) {
            for (Object object : compositionPlaylistType.getExtensionProperties().getAny()) {
                if (object instanceof JAXBElement) {
                    JAXBElement jaxbElement = (JAXBElement) (object);
                    if (jaxbElement.getName().getLocalPart().equals("ApplicationIdentification")) {
                        if (jaxbElement.getValue() instanceof List) {
                            List applicationIDList = (List) jaxbElement.getValue();
                            for(Object entry: applicationIDList) {
                                if (entry instanceof String) {
                                    applicationIDs.add(entry.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, String> contentVersionList = new HashMap<String, String>();
        for (org.smpte_ra.schemas.st2067_2_2016.ContentVersionType entry : compositionPlaylistType.getContentVersionList().getContentVersion()) {
            contentVersionList.put(entry.getId(), entry.getLabelText().getValue());
        }

        return new IMFCompositionPlaylistType( compositionPlaylistType.getId(),
                compositionPlaylistType.getEditRate(),
                (compositionPlaylistType.getAnnotation() == null ? null : compositionPlaylistType.getAnnotation().getValue()),
                (compositionPlaylistType.getIssuer() == null ? null : compositionPlaylistType.getIssuer().getValue()),
                (compositionPlaylistType.getCreator() == null ? null : compositionPlaylistType.getCreator().getValue()),
                (compositionPlaylistType.getContentOriginator() == null ? null : compositionPlaylistType.getContentOriginator().getValue()),
                (compositionPlaylistType.getContentTitle() == null ? null : compositionPlaylistType.getContentTitle().getValue()),
                (compositionPlaylistType.getContentKind() == null ? null : compositionPlaylistType.getContentKind().getValue()),
                contentVersionList,
                Collections.synchronizedList(segmentList),
                Collections.synchronizedList(essenceDescriptorList),
                "org.smpte_ra.schemas.st2067_2_2016", applicationIDs
                );
    }

    /**
     * Getter for the CoreConstraintsURI corresponding to this CompositionPlaylist
     *
     * @return the uri for the CoreConstraints schema for this CompositionPlaylist
     */
    public String getCoreConstraintsVersion() {
        return "org.smpte_ra.schemas.st2067_2_2016";
    }
}