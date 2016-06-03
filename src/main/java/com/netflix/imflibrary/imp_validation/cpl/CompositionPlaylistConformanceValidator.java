package com.netflix.imflibrary.imp_validation.cpl;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.imp_validation.DOMNodeObjectModel;
import com.netflix.imflibrary.app.MXFEssenceReader;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.RegXMLLibHelper;
import com.sandflow.smpte.klv.Triplet;
import org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class implements the logic to perform conformance validation of an IMF CompositionPlaylist document.
 * Conformance validation will verify that the essences that are described in the CPL EssenceDescriptionList comply with
 * IMF-CoreConstraints (SMPTE st2067-2:2013) and that the essence description contained within every essence that the CPL
 * references through its virtual track resource list is contained in the EssenceDescription List.
 */
public class CompositionPlaylistConformanceValidator {

    /**
     * This method can be used to determine if a CompositionPlaylist is conformant. Conformance checks
     * perform deeper inspection of the CompositionPlaylist and the EssenceDescriptors corresponding to the
     * resources referenced by the CompositionPlaylist.
     * @param compositionPlaylistRecord corresponding to the CompositionPlaylist
     * @param imfErrorLogger - an error object for reporting errors
     * @return boolean to indicate of the CompositionPlaylist is conformant or not
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public boolean isCompositionPlaylistConformed(CompositionPlaylistRecord compositionPlaylistRecord, IMFErrorLogger imfErrorLogger) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {

        /*
         * The algorithm for conformance checking a CompositionPlaylist (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list is referenced through its id element
         * by at least one TrackFileResource within the Virtual tracks in the CompositionPlaylist (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the CompositionPlaylist are present in
         * the physical essence files referenced by the resources of a virtual track and are equal.
         */
        CompositionPlaylist compositionPlaylist = compositionPlaylistRecord.getCompositionPlaylist();
        Map<UUID, DOMNodeObjectModel> eDLMap = getCPLEssenceDescriptorListMap(compositionPlaylist);

        /**
         * Get the complete list of SourceEncoding elements in all the TrackFileResources in the CPL.
         */
        List<CompositionPlaylist.VirtualTrack>virtualTracks =  compositionPlaylistRecord.getCompositionPlaylist().getVirtualTracks();
        Set<UUID> resourceSourceEncodingElementsSet = getResourceEssenceDescriptorIdsSet(compositionPlaylistRecord.getCompositionPlaylist());
        Map<UUID, ResourceByteRangeProvider>sourceEncodingElementByteRangeProviderMap = new HashMap<>();/*Map containing <SourceEncodingElement, ResourceByteRangeProvider> entries*/

        /**
         * Construct a set of source encoding element UUIDs by checking exhaustively every TrackFileResource of every VirtualTrack in the CPL
         * and a map of <SourceEncodingElement/ID ResourceByteRangeProvider> for every TrackFileResource in every VirtualTrack in the CPL
         */
        for(CompositionPlaylist.VirtualTrack virtualTrack : virtualTracks){
            List<CompositionPlaylist.ResourceIdTuple> resourceIdTuples = compositionPlaylist.getVirtualTrackResourceIDs(virtualTrack);
            Map<UUID, ResourceByteRangeProvider> imfEssenceMap = compositionPlaylistRecord.getImfEssenceMap();
            for(CompositionPlaylist.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
                /*Construct a Map of <SourceEncodingElement/ID ResourceByteRangeProvider> for every TrackFileResource of this VirtualTrack*/
                sourceEncodingElementByteRangeProviderMap.put(resourceIdTuple.getSourceEncoding(), imfEssenceMap.get(resourceIdTuple.getTrackFileId()));
            }
        }
        /*The following check simultaneously verifies 1) and 2) from above.*/
        if(!getEssenceDescriptorIdsSet(compositionPlaylist).equals(resourceSourceEncodingElementsSet)){
            throw new IMFException(String.format("At least one of the EssenceDescriptors in the EssenceDescriptorList is not referenced by a TrackFileResource or there is at least one TrackFileResource that is not referenced by a EssenceDescriptor in the EssenceDescriptorList"));
        }

        /**
         * Create a HashMap <sourceEncodingElement List<DOM Nodes> exhaustively by reading all the EssenceDescriptors in every Essence referred
         * by every TrackFileResource in every Virtual Track in the CPL.
         */
        Map<UUID, List<DOMNodeObjectModel>> essenceDescriptorMap = new LinkedHashMap<>();/*Map <sourceEncodingElement List<Node>> from the physical essence files*/
        for(Map.Entry entry : sourceEncodingElementByteRangeProviderMap.entrySet()){
            MXFEssenceReader mxfEssenceReader = new MXFEssenceReader(sourceEncodingElementByteRangeProviderMap.get(entry.getKey()));
            if(essenceDescriptorMap.get(entry.getKey()) == null) {
                List<Node> essenceDescriptorsDOMNodes = mxfEssenceReader.getEssenceDescriptorsDOMNodes();
                List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
                for(Node node : essenceDescriptorsDOMNodes){
                    domNodeObjectModels.add(new DOMNodeObjectModel(node));
                }
                essenceDescriptorMap.put((UUID) entry.getKey(), domNodeObjectModels);
            }
        }
        return compareEssenceDescriptors(essenceDescriptorMap, eDLMap, imfErrorLogger);
    }

    /**
     * This method can be used to determine if a CompositionPlaylist is conformant. Conformance checks
     * perform deeper inspection of the CompositionPlaylist and the EssenceDescriptors corresponding to the
     * resources referenced by the CompositionPlaylist.
     * @param compositionPlaylist corresponding to the CompositionPlaylist payload
     * @param headerPartitionTuples list of HeaderPartitionTuples corresponding to the IMF essences referenced in the CompositionPlaylist
     * @param imfErrorLogger an error logging object
     * @return boolean to indicate of the CompositionPlaylist is conformant or not
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public boolean isCompositionPlaylistConformed(CompositionPlaylist compositionPlaylist, List<IMPValidator.HeaderPartitionTuple> headerPartitionTuples, IMFErrorLogger imfErrorLogger) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException{
        boolean result = true;
        /*
         * The algorithm for conformance checking a CompositionPlaylist (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list is referenced through its id element
         * by at least one TrackFileResource within the Virtual tracks in the CompositionPlaylist (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the CompositionPlaylist are present in
         * the physical essence files referenced by the resources of a virtual track and are equal.
         */
        /*The following check simultaneously verifies 1) and 2) from above.*/
        Set<UUID> resourceEssenceDescriptorIDsSet = getResourceEssenceDescriptorIdsSet(compositionPlaylist);
        Iterator resourceEssenceDescriptorIDs = resourceEssenceDescriptorIDsSet.iterator();
        Set<UUID> cplEssenceDescriptorIDsSet = getEssenceDescriptorIdsSet(compositionPlaylist);
        Iterator cplEssenceDescriptorIDs = cplEssenceDescriptorIDsSet.iterator();
        while(resourceEssenceDescriptorIDs.hasNext()){
            UUID resourceEssenceDescriptorUUID = (UUID) resourceEssenceDescriptorIDs.next();
            if(!cplEssenceDescriptorIDsSet.contains(resourceEssenceDescriptorUUID)) {
                result &= false;
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor ID %s referenced by a VirtualTrack Resource does not have a corresponding EssenceDescriptor in the EssenceDescriptorList in the CPL", resourceEssenceDescriptorUUID.toString()));
            }
        }

        while(cplEssenceDescriptorIDs.hasNext()){
            UUID cplEssenceDescriptorUUID = (UUID) cplEssenceDescriptorIDs.next();
            if(!resourceEssenceDescriptorIDsSet.contains(cplEssenceDescriptorUUID)) {
                result &= false;
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptorID %s in the CPL EssenceDescriptorList is not referenced by any resource in any of the Virtual tracks in the CPL, this violates the constraint in st2067-3:2013 section 6.1.10.1", cplEssenceDescriptorUUID.toString()));
            }
        }

        if(!result){
            return result;
        }

        /*The following check verifies 3) from above.*/
        result &= compareEssenceDescriptors(getResourcesEssenceDescriptorMap(compositionPlaylist, headerPartitionTuples), getCPLEssenceDescriptorListMap(compositionPlaylist), imfErrorLogger);
        return result;
    }

    private Set<UUID> getEssenceDescriptorIdsSet (CompositionPlaylist compositionPlaylist) {
        HashSet<UUID> essenceDescriptorIdsSet = new LinkedHashSet<>();
        List<EssenceDescriptorBaseType> essenceDescriptorList = compositionPlaylist.getCompositionPlaylistType().getEssenceDescriptorList().getEssenceDescriptor();
        for(EssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptorList) {
            UUID sourceEncodingElement = UUIDHelper.fromUUIDAsURNStringToUUID(essenceDescriptorBaseType.getId());
            /*Construct a set of SourceEncodingElements/IDs corresponding to every EssenceDescriptorBaseType in the EssenceDescriptorList*/
            essenceDescriptorIdsSet.add(sourceEncodingElement);
        }
        return essenceDescriptorIdsSet;
    }

    private Map<UUID, DOMNodeObjectModel> getCPLEssenceDescriptorListMap(CompositionPlaylist compositionPlaylist){
        return compositionPlaylist.getEssenceDescriptorListMap();
    }

    private Set<UUID> getResourceEssenceDescriptorIdsSet (CompositionPlaylist compositionPlaylist) throws IOException, SAXException, JAXBException, URISyntaxException{
        List<CompositionPlaylist.VirtualTrack> virtualTracks = new ArrayList<>(compositionPlaylist.getVirtualTrackMap().values());
        LinkedHashSet<UUID> resourceSourceEncodingElementsSet = new LinkedHashSet<>();
        for(CompositionPlaylist.VirtualTrack virtualTrack : virtualTracks){
            List<CompositionPlaylist.ResourceIdTuple> resourceIdTuples = compositionPlaylist.getVirtualTrackResourceIDs(virtualTrack);
            for(CompositionPlaylist.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        return resourceSourceEncodingElementsSet;
    }

    private Map<UUID, List<DOMNodeObjectModel>> getResourcesEssenceDescriptorMap(CompositionPlaylist compositionPlaylist, List<IMPValidator.HeaderPartitionTuple> headerPartitionTuples) throws IOException, SAXException, JAXBException, URISyntaxException{
        Map<UUID, List<DOMNodeObjectModel>> resourcesEssenceDescriptorMap = new LinkedHashMap<>();

        /*Create a Map of FilePackage UUID which should be equal to the TrackFileId of the resource in the CompositionPlaylist if the asset is referenced and the HeaderPartitionTuple, Map<UUID, HeaderPartitionTuple>*/
        Map<UUID, IMPValidator.HeaderPartitionTuple> resourceUUIDHeaderPartitionMap = new HashMap<>();
        for(IMPValidator.HeaderPartitionTuple headerPartitionTuple : headerPartitionTuples) {
            //validate header partition
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartitionTuple.getHeaderPartition());
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
            Preface preface = headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
            GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
            SourcePackage filePackage = (SourcePackage)genericPackage;
            UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
            resourceUUIDHeaderPartitionMap.put(packageUUID, headerPartitionTuple);
        }
        List<CompositionPlaylist.VirtualTrack> virtualTracks = new ArrayList<>(compositionPlaylist.getVirtualTrackMap().values());

        /*Go through all the Virtual Tracks in the CompositionPlaylist and construct a map of Resource Source Encoding Element and a list of DOM nodes representing every EssenceDescriptor in the HeaderPartition corresponding to that Resource*/
        for(CompositionPlaylist.VirtualTrack virtualTrack : virtualTracks){
            List<CompositionPlaylist.ResourceIdTuple> resourceIdTuples = compositionPlaylist.getVirtualTrackResourceIDs(virtualTrack);/*Retrieve a list of ResourceIDTuples corresponding to this virtual track*/
            for(CompositionPlaylist.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                IMPValidator.HeaderPartitionTuple headerPartitionTuple = resourceUUIDHeaderPartitionMap.get(resourceIdTuple.getTrackFileId());
                if(headerPartitionTuple != null){
                    /*Create a DOM Node representation of the EssenceDescriptors present in this header partition corresponding to an IMFTrackFile*/
                    List<Node> essenceDescriptorDOMNodes = getEssenceDescriptorDOMNodes(headerPartitionTuple);
                    List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
                    for(Node node : essenceDescriptorDOMNodes){
                        domNodeObjectModels.add(new DOMNodeObjectModel(node));
                    }
                    resourcesEssenceDescriptorMap.put(resourceIdTuple.getSourceEncoding(), domNodeObjectModels);
                }
            }
        }
        if(resourcesEssenceDescriptorMap.entrySet().size() == 0){
            throw new MXFException(String.format("CompositionPlaylist does not refer to a single IMFEssence represented by the HeaderPartitions that were passed in."));
        }
        return resourcesEssenceDescriptorMap;
    }

    private List<Node> getEssenceDescriptorDOMNodes(IMPValidator.HeaderPartitionTuple headerPartitionTuple) throws IOException {
        try {
            List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = headerPartitionTuple.getHeaderPartition().getEssenceDescriptors();
            List<Node> essenceDescriptorNodes = new ArrayList<>();
            for (InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors) {
                KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                List<KLVPacket.Header> subDescriptorHeaders = this.getSubDescriptorKLVHeader(headerPartitionTuple.getHeaderPartition(), essenceDescriptor);
                /*Create a dom*/
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();

                DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(document, headerPartitionTuple, essenceDescriptorHeader, subDescriptorHeaders);
                Node node = documentFragment.getFirstChild();
                essenceDescriptorNodes.add(node);
            }
            return essenceDescriptorNodes;
        }
        catch(ParserConfigurationException e){
            throw new IMFException(e);
        }
    }

    private List<KLVPacket.Header> getSubDescriptorKLVHeader(HeaderPartition headerPartition, InterchangeObject.InterchangeObjectBO essenceDescriptor) {
        List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition.getSubDescriptors(essenceDescriptor);
        for (InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors) {
            if (subDescriptorBO != null) {
                subDescriptorHeaders.add(subDescriptorBO.getHeader());
            }
        }
        return Collections.unmodifiableList(subDescriptorHeaders);
    }

    private DocumentFragment getEssenceDescriptorAsDocumentFragment(Document document, IMPValidator.HeaderPartitionTuple headerPartitionTuple, KLVPacket.Header essenceDescriptor, List<KLVPacket.Header>subDescriptors) throws MXFException, IOException {
        document.setXmlStandalone(true);

        PrimerPack primerPack = headerPartitionTuple.getHeaderPartition().getPrimerPack();
        ResourceByteRangeProvider resourceByteRangeProvider = headerPartitionTuple.getResourceByteRangeProvider();


        RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(primerPack.getHeader(), getByteProvider(resourceByteRangeProvider, primerPack.getHeader()));

        Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor, getByteProvider(resourceByteRangeProvider, essenceDescriptor));
        //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
        /*Get the Triplets corresponding to the SubDescriptors*/
        List<Triplet> subDescriptorTriplets = new ArrayList<>();
        for(KLVPacket.Header subDescriptorHeader : subDescriptors){
            subDescriptorTriplets.add(regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, this.getByteProvider(resourceByteRangeProvider, subDescriptorHeader)));
        }
        return regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document);
    }

    private ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        return byteProvider;
    }

    private boolean compareEssenceDescriptors(Map<UUID, List<DOMNodeObjectModel>> essenceDescriptorsMap, Map<UUID, DOMNodeObjectModel> eDLMap, IMFErrorLogger imfErrorLogger){

        /**
         * An exhaustive compare of the eDLMap and essenceDescriptorsMap is required to ensure that the essence descriptors
         * in the EssenceDescriptorList and the EssenceDescriptors in the physical essence files corresponding to the
         * same source encoding element as indicated in the TrackFileResource and EDL are a good match.
         */

        /**
         * The Maps passed in have the DOMObjectModel for every EssenceDescriptor in the EssenceDescriptorList in the CPL and
         * the essence descriptor in each of the essences referenced from every track file resource within each virtual track.
         */
        Iterator<Map.Entry<UUID, DOMNodeObjectModel>> eDLMapIterator = eDLMap.entrySet().iterator();
        while(eDLMapIterator.hasNext()){
            Map.Entry<UUID, DOMNodeObjectModel> entry = (Map.Entry<UUID, DOMNodeObjectModel>) eDLMapIterator.next();
            List<DOMNodeObjectModel> domNodeObjectModels = essenceDescriptorsMap.get(entry.getKey());
            if(domNodeObjectModels == null){
                //This implies we did not find a single VirtualTrack that referenced this particular EssenceDescriptor in the EDL
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Id %s in the CPL's EDL is not referenced by a single resource within any of the VirtualTracks in the CPL, this violates the constraint in st2067-3:2013 section 6.1.10.1", entry.getKey().toString()));
                return false;
            }
            DOMNodeObjectModel referenceDOMNodeObjectModel = entry.getValue();
            boolean intermediateResult = false;
            for(DOMNodeObjectModel domNodeObjectModel : domNodeObjectModels){
                intermediateResult |= referenceDOMNodeObjectModel.equals(domNodeObjectModel);
            }
            if(!intermediateResult){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("EssenceDescriptor with Id %s in the CPL's EDL doesn't match any EssenceDescriptors within the IMFTrackFile that references it", entry.getKey().toString()));
                return false;
            }
        }

        return true;
    }

}
