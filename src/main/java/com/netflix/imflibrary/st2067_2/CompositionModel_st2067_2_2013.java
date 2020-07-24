package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.writerTools.CompositionPlaylistBuilder_2013;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class that models aspects of a Composition such as a VirtualTrack, TrackResources etc. compliant with the 2013 CompositionPlaylist specification st2067-3:2013.
 * Used for converting a specific JAXB class (org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType) into the canonical, version-independent, class IMFCompositionPlaylistType
 */
final class CompositionModel_st2067_2_2013 {

    //To prevent instantiation
    private CompositionModel_st2067_2_2013(){

    }

    /**
     * Converts a CompositionPlaylist from a JAXB object, into a version-independent model
     * @param compositionPlaylistType - a CompositionPlaylist object model
     * @param imfErrorLogger - an object for logging errors
     * @return A canonical, version-independent, instance of IMFCompositionPlaylistType
     */
    @Nonnull public static IMFCompositionPlaylistType getCompositionPlaylist (@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        // Parse each Segment
        List<IMFSegmentType> segmentList = compositionPlaylistType.getSegmentList().getSegment().stream()
                .map(segment -> parseSegment(segment, compositionPlaylistType.getEditRate(), imfErrorLogger))
                .collect(Collectors.toList());

        // Parse the EssenceDescriptors, if present
        List<IMFEssenceDescriptorBaseType> essenceDescriptorList = Collections.emptyList();
        if (compositionPlaylistType.getEssenceDescriptorList() != null)
        {
            essenceDescriptorList = compositionPlaylistType.getEssenceDescriptorList().getEssenceDescriptor().stream()
                    .map(ed -> parseEssenceDescriptor(ed, imfErrorLogger))
                    .collect(Collectors.toList());
        }

        // Parse the ApplicationIdentification values
        Set<String> applicationIDs = parseApplicationIds(compositionPlaylistType, imfErrorLogger);

        return new IMFCompositionPlaylistType( compositionPlaylistType.getId(),
                compositionPlaylistType.getEditRate(),
                (compositionPlaylistType.getAnnotation() == null ? null : compositionPlaylistType.getAnnotation().getValue()),
                (compositionPlaylistType.getIssuer() == null ? null : compositionPlaylistType.getIssuer().getValue()),
                (compositionPlaylistType.getCreator() == null ? null : compositionPlaylistType.getCreator().getValue()),
                (compositionPlaylistType.getContentOriginator() == null ? null : compositionPlaylistType.getContentOriginator().getValue()),
                (compositionPlaylistType.getContentTitle() == null ? null : compositionPlaylistType.getContentTitle().getValue()),
                segmentList,
                essenceDescriptorList,
                "org.smpte_ra.schemas.st2067_2_2013", applicationIDs
        );
    }

    @Nonnull static org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType unmarshallCpl(@Nonnull ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1))
        {
            // Validate the document against the CPL schemas, when unmarshalling
            Schema schema = CompositionModel_st2067_2_2013.ValidationSchema.INSTANCE;
            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            Unmarshaller unmarshaller = CompositionModel_st2067_2_2013.CompositionPlaylistType2013_Context.INSTANCE.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            JAXBElement<org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType> jaxbCpl
                    = unmarshaller.unmarshal(new StreamSource(inputStream), org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType.class);

            // Report any schema validation errors that occurred during unmarshalling
            if (validationEventHandlerImpl.hasErrors())
            {
                validationEventHandlerImpl.getErrors().stream()
                        .map(e -> new ErrorLogger.ErrorObject(
                                IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                                e.getValidationEventSeverity(),
                                "Line Number : " + e.getLineNumber().toString() + " - " + e.getErrorMessage()))
                        .forEach(imfErrorLogger::addError);

                throw new IMFException(validationEventHandlerImpl.toString(), imfErrorLogger);
            }

            return jaxbCpl.getValue();
        }
        catch(JAXBException e)
        {
            throw new IMFException("Error when unmarshalling org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType", e, imfErrorLogger);
        }
    }

    // Parse the list of ApplicationIdentification values
    @Nonnull private static Set<String> parseApplicationIds(@Nonnull org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType compositionPlaylistType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        if (compositionPlaylistType.getExtensionProperties() == null)
            return Collections.emptySet();

        return compositionPlaylistType.getExtensionProperties().getAny().stream()
                .filter(JAXBElement.class::isInstance).map(JAXBElement.class::cast)
                .filter(extProp -> extProp.getName().getLocalPart().equals("ApplicationIdentification")).map(JAXBElement::getValue)
                .filter(List.class::isInstance).map(appIdList -> (List<?>) appIdList)
                .findAny().orElse(Collections.emptyList()).stream().map(Object::toString).collect(Collectors.toSet());
    }

    // Converts an instance of the JAXB class org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType
    // Into a canonical, version-independent, instance of IMFEssenceDescriptorBaseType
    @Nonnull private static IMFEssenceDescriptorBaseType parseEssenceDescriptor(@Nonnull org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType essenceDescriptor, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        return new IMFEssenceDescriptorBaseType(essenceDescriptor.getId(), essenceDescriptor.getAny());
    }

    // Converts an instance of the JAXB class org.smpte_ra.schemas.st2067_2_2013.SegmentType
    // Into a canonical, version-independent, instance of IMFSegmentType
    @Nonnull private static IMFSegmentType parseSegment(@Nonnull org.smpte_ra.schemas.st2067_2_2013.SegmentType segment, @Nonnull List<Long> cplEditRate, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<IMFSequenceType> sequenceList = new ArrayList<IMFSequenceType>();

        // Parse the Marker Sequence
        org.smpte_ra.schemas.st2067_2_2013.SequenceType markerSequence = segment.getSequenceList().getMarkerSequence();
        if (markerSequence != null)
        {
            sequenceList.add(parseMarkerSequence(markerSequence, cplEditRate, imfErrorLogger));
        }

        /* Parse rest of the sequences */
        for (Object object : segment.getSequenceList().getAny())
        {
            // Ignore unrecognized Sequence types
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

            // Get the JAXB SequenceType object
            JAXBElement jaxbElement = (JAXBElement)(object);
            org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence = (org.smpte_ra.schemas.st2067_2_2013.SequenceType) jaxbElement.getValue();
            // Determine the type of Sequence being parsed
            Composition.SequenceTypeEnum sequenceType = Composition.SequenceTypeEnum.getSequenceTypeEnum(jaxbElement.getName().getLocalPart());
            // Parse the Sequence
            sequenceList.add(parseSequence(sequence, cplEditRate, sequenceType, imfErrorLogger));
        }
        return new IMFSegmentType(segment.getId(), sequenceList);
    }

    // Converts an instance of the JAXB class org.smpte_ra.schemas.st2067_2_2013.SequenceType
    // Into a canonical, version-independent, instance of IMFSequenceType
    @Nonnull private static IMFSequenceType parseMarkerSequence(@Nonnull org.smpte_ra.schemas.st2067_2_2013.SequenceType markerSequence,
                                                                @Nonnull List<Long> cplEditRate, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<IMFBaseResourceType> sequenceResources = new ArrayList<>();
        for (org.smpte_ra.schemas.st2067_2_2013.BaseResourceType resource : markerSequence.getResourceList().getResource())
        {
            if (resource instanceof org.smpte_ra.schemas.st2067_2_2013.MarkerResourceType)
            {
                try
                {
                    IMFMarkerResourceType markerResource = parseMarkerResource(
                            (org.smpte_ra.schemas.st2067_2_2013.MarkerResourceType) resource, cplEditRate, imfErrorLogger);
                    sequenceResources.add(markerResource);
                }
                catch(IMFException e)
                {
                    imfErrorLogger.addAllErrors(e.getErrors());
                }
            }
            else
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, "Unsupported Resource type in Marker Sequence");
            }
        }
        return new IMFSequenceType(markerSequence.getId(),
                markerSequence.getTrackId(),
                Composition.SequenceTypeEnum.MarkerSequence,
                sequenceResources);
    }

    // Converts an instance of the JAXB class org.smpte_ra.schemas.st2067_2_2013.SequenceType
    // Into a canonical, version-independent, instance of IMFSequenceType
    @Nonnull private static IMFSequenceType parseSequence(@Nonnull org.smpte_ra.schemas.st2067_2_2013.SequenceType sequence,
                                                          @Nonnull List<Long> cplEditRate, Composition.SequenceTypeEnum sequenceType, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        List<IMFBaseResourceType> sequenceResources = new ArrayList<>();
        for (org.smpte_ra.schemas.st2067_2_2013.BaseResourceType resource : sequence.getResourceList().getResource())
        {
            if(resource instanceof  org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType)
            {
                try
                {
                    IMFTrackFileResourceType trackFileResource = parseTrackFileResource(
                            (org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType) resource, cplEditRate, imfErrorLogger);
                    sequenceResources.add(trackFileResource);
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
        }

        return new IMFSequenceType(sequence.getId(),
                sequence.getTrackId(),
                sequenceType,
                sequenceResources);
    }

    // Converts an instance of the JAXB class org.smpte_ra.schemas.st2067_2_2013.MarkerResourceType
    // Into a canonical, version-independent, instance of IMFMarkerResourceType
    @Nonnull private static IMFMarkerResourceType parseMarkerResource(@Nonnull org.smpte_ra.schemas.st2067_2_2013.MarkerResourceType markerResource,
                                                                      @Nonnull List<Long> cplEditRate, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        // Parse each Marker within the MarkerResource
        List<IMFMarkerType> markerList = new ArrayList<IMFMarkerType>();
        for (org.smpte_ra.schemas.st2067_2_2013.MarkerType marker : markerResource.getMarker()) {
            markerList.add(new IMFMarkerType(marker.getAnnotation() == null ? null : marker
                    .getAnnotation().getValue(),
                    new IMFMarkerType.Label(marker.getLabel().getValue(), marker.getLabel().getScope()),
                    marker.getOffset()));
        }

        return new IMFMarkerResourceType(
                markerResource.getId(),
                markerResource.getEditRate().size() != 0 ? markerResource.getEditRate() : cplEditRate,
                markerResource.getIntrinsicDuration(),
                markerResource.getEntryPoint(),
                markerResource.getSourceDuration(),
                markerResource.getRepeatCount(),
                markerList);
    }

    // Converts an instance of the JAXB class org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType
    // Into a canonical, version-independent, instance of IMFTrackFileResourceType
    @Nonnull private static IMFTrackFileResourceType parseTrackFileResource(@Nonnull org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType trackFileResource,
                                                                            @Nonnull List<Long> cplEditRate, @Nonnull IMFErrorLogger imfErrorLogger)
    {
        return new IMFTrackFileResourceType(
                trackFileResource.getId(),
                trackFileResource.getTrackFileId(),
                trackFileResource.getEditRate().size() != 0 ? trackFileResource.getEditRate() : cplEditRate,
                trackFileResource.getIntrinsicDuration(),
                trackFileResource.getEntryPoint(),
                trackFileResource.getSourceDuration(),
                trackFileResource.getRepeatCount(),
                trackFileResource.getSourceEncoding(),
                trackFileResource.getHash(),
                CompositionPlaylistBuilder_2013.defaultHashAlgorithm
        );
    }

    // Singleton to allow a JAXBContext configured for 2013 CPLs to be reused and lazy-loaded
    private static class CompositionPlaylistType2013_Context
    {
        static final JAXBContext INSTANCE = createJAXBContext();
        private static JAXBContext createJAXBContext()
        {
            try
            {
                return JAXBContext.newInstance(
                        org.smpte_ra.schemas.st2067_2_2013.ObjectFactory.class);  // 2013 CPL and Core constraints
            }
            catch(JAXBException e)
            {
                throw new IMFException("Failed to create JAXBContext needed by CompositionPlaylistType (2013)", e);
            }
        }
    }

    // Singleton to allow the CPL validation Schema object to be reused and lazy-loaded
    private static class ValidationSchema
    {
        static final Schema INSTANCE = createValidationSchema();
        private static Schema createValidationSchema()
        {
            // Load all XSD schemas required to validate a CompositionPlaylist document
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream xsd_xmldsig_core = contextClassLoader.getResourceAsStream("org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd");
                 InputStream xsd_dcmlTypes = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd");
                 InputStream xsd_cpl_2013 = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_3_2013/imf-cpl.xsd");
                 InputStream xsd_core_constraints_2013 = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_2_2013/imf-core-constraints-20130620-pal.xsd");
            )
            {
                // Build a schema from all of the XSD files provided
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                return schemaFactory.newSchema(new StreamSource[]{
                        new StreamSource(xsd_xmldsig_core),
                        new StreamSource(xsd_dcmlTypes),
                        new StreamSource(xsd_cpl_2013),
                        new StreamSource(xsd_core_constraints_2013),
                });
            }
            catch(IOException | SAXException e)
            {
                throw new IMFException("Unable to create CPL validation schema", e);
            }
        }
    }
}