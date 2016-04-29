package com.netflix.imflibrary.imp_validation.cpl;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.imp_validation.DOMNodeObjectModel;
import com.netflix.imflibrary.reader_interfaces.MXFEssenceReader;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
     * @return boolean to indicate of the CompositionPlaylist is conformant or not
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public boolean isCompositionPlaylistConformed(CompositionPlaylistRecord compositionPlaylistRecord) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {

        /*
         * The algorithm for conformance checking a CompositionPlaylist (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list is referenced through its id element
         * by at least one TrackFileResource within the Virtual tracks in the CompositionPlaylist (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the CompositionPlaylist are present in
         * the physical essence files referenced by the resources of a virtual track and are equal.
         */
        CompositionPlaylist compositionPlaylist = compositionPlaylistRecord.getCompositionPlaylist();
        Map<UUID, List<Node>> eDLMap = getCPLEssenceDescriptorListMap(compositionPlaylist);

        /**
         * Get the complete list of SourceEncoding elements in all the TrackFileResources in the CPL.
         */
        List<CompositionPlaylist.VirtualTrack>virtualTracks =  CompositionPlaylistHelper.getVirtualTracks(compositionPlaylistRecord);
        Set<UUID> resourceSourceEncodingElementsSet = getResourceEssenceDescriptorIdsSet(compositionPlaylistRecord.getCompositionPlaylist());
        Map<UUID, ResourceByteRangeProvider>sourceEncodingElementByteRangeProviderMap = new HashMap<>();/*Map containing <SourceEncodingElement, ResourceByteRangeProvider> entries*/

        /**
         * Construct a set of source encoding element UUIDs by checking exhaustively every TrackFileResource of every VirtualTrack in the CPL
         * and a map of <SourceEncodingElement/ID ResourceByteRangeProvider> for every TrackFileResource in every VirtualTrack in the CPL
         */
        for(CompositionPlaylist.VirtualTrack virtualTrack : virtualTracks){
            List<CompositionPlaylistHelper.ResourceIdTuple> resourceIdTuples = CompositionPlaylistHelper.getVirtualTrackResourceIDs(virtualTrack);
            Map<UUID, ResourceByteRangeProvider> imfEssenceMap = compositionPlaylistRecord.getImfEssenceMap();
            for(CompositionPlaylistHelper.ResourceIdTuple resourceIdTuple : resourceIdTuples){
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
        Map<UUID, List<Node>> essenceDescriptorMap = new LinkedHashMap<>();/*Map <sourceEncodingElement List<Node>> from the physical essence files*/
        for(Map.Entry entry : sourceEncodingElementByteRangeProviderMap.entrySet()){
            MXFEssenceReader mxfEssenceReader = new MXFEssenceReader(sourceEncodingElementByteRangeProviderMap.get(entry.getKey()));
            if(essenceDescriptorMap.get(entry.getKey()) == null) {
                essenceDescriptorMap.put((UUID) entry.getKey(), mxfEssenceReader.getEssenceDescriptorsDOMNodes());
            }
        }
        return compareEssenceDescriptors(essenceDescriptorMap, eDLMap);
    }

    /**
     * This method can be used to determine if a CompositionPlaylist is conformant. Conformance checks
     * perform deeper inspection of the CompositionPlaylist and the EssenceDescriptors corresponding to the
     * resources referenced by the CompositionPlaylist.
     * @param compositionPlaylist corresponding to the CompositionPlaylist payload
     * @param headerPartitions list of HeaderPartitions corresponding to the IMF essences referenced in the CompositionPlaylist
     * @return boolean to indicate of the CompositionPlaylist is conformant or not
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public boolean isCompositionPlaylistConformed(CompositionPlaylist compositionPlaylist, List<HeaderPartition> headerPartitions) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException{
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
        if(!getEssenceDescriptorIdsSet(compositionPlaylist).equals(getResourceEssenceDescriptorIdsSet(compositionPlaylist))){
            result = false;
            throw new IMFException(String.format("At least one of the EssenceDescriptors in the EssenceDescriptorList is not referenced by a TrackFileResource or there is at least one TrackFileResource that is not referenced by a EssenceDescriptor in the EssenceDescriptorList"));
        }
        /*The following check verifies 3) from above.*/
        result = compareEssenceDescriptors(getCPLEssenceDescriptorListMap(compositionPlaylist), getResourcesEssenceDescriptorMap(compositionPlaylist, headerPartitions));
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

    private Map<UUID, List<Node>> getCPLEssenceDescriptorListMap(CompositionPlaylist compositionPlaylist){
        List<EssenceDescriptorBaseType> essenceDescriptorList = compositionPlaylist.getCompositionPlaylistType().getEssenceDescriptorList().getEssenceDescriptor();
        Map<UUID, List<Node>> eDLMap = new HashMap<>();/*Map <sourceEncodingElement List<Node>> from the EssenceDescriptorList*/
        for(EssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptorList){
            List<Node> essenceDescriptorNodes = new ArrayList<>();
            for(Object object : essenceDescriptorBaseType.getAny()){
                essenceDescriptorNodes.add((Node) object);
            }
            UUID sourceEncodingElement = UUIDHelper.fromUUIDAsURNStringToUUID(essenceDescriptorBaseType.getId());
            /*Construct a map <SourceEncodingElement/ID  DOM Node> corresponding to every EssenceDescriptor in the any list of this EssenceDescriptorBaseType in the EssenceDescriptorList*/
            eDLMap.put(sourceEncodingElement, essenceDescriptorNodes);
        }
        return eDLMap;
    }

    private Set<UUID> getResourceEssenceDescriptorIdsSet (CompositionPlaylist compositionPlaylist) throws IOException, SAXException, JAXBException, URISyntaxException{
        List<CompositionPlaylist.VirtualTrack> virtualTracks = new ArrayList<>(compositionPlaylist.getVirtualTrackMap().values());
        LinkedHashSet<UUID> resourceSourceEncodingElementsSet = new LinkedHashSet<>();
        for(CompositionPlaylist.VirtualTrack virtualTrack : virtualTracks){
            List<CompositionPlaylistHelper.ResourceIdTuple> resourceIdTuples = CompositionPlaylistHelper.getVirtualTrackResourceIDs(virtualTrack);
            for(CompositionPlaylistHelper.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                /*Construct a set of SourceEncodingElements corresponding to every TrackFileResource of this VirtualTrack*/
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        return resourceSourceEncodingElementsSet;
    }

    private Map<UUID, List<Node>> getResourcesEssenceDescriptorMap(CompositionPlaylist compositionPlaylist, List<HeaderPartition> headerPartitions){
        Map<UUID, List<Node>> resourcesEssenceDescriptorMap = new LinkedHashMap<>();

        return resourcesEssenceDescriptorMap;
    }


    private boolean compareEssenceDescriptors(Map<UUID, List<Node>> essenceDescriptorMap, Map<UUID, List<Node>> eDLMap){

        /**
         * An exhaustive compare of the eDLMap and essenceDescriptorMap is required to ensure that the essence descriptors
         * in the EssenceDescriptorList and the EssenceDescriptors in the physical essence files corresponding to the
         * same source encoding element as indicated in the TrackFileResource and EDL are a good match.
         */
        Map<UUID, List<DOMNodeObjectModel>> virtualTracksEssenceDescriptorsMap = getEssenceDescriptorsObjectModel(essenceDescriptorMap);/*A map of <SourceEncodingElement/ID List<DOMNodeObjectModel>> corresponding to every EssenceDescriptor in that essence*/
        Map<UUID, List<DOMNodeObjectModel>> cplEDLEssenceDescriptorsMap = getEssenceDescriptorsObjectModel(eDLMap);/*A map of <SourceEncodingElement/ID List<DOMNodeObjectModel>> corresponding to every EssenceDescriptor identified by the SourceEncodingElement in the EDL*/

        /**
         * We now have the DOMObjectModel for every EssenceDescriptor in the EssenceDescriptorList in the CPL and
         * the essence descriptor in each of the essences referenced from the track file resource within each
         * virtual track.
         */
        for(Map.Entry entry : cplEDLEssenceDescriptorsMap.entrySet()){

            if(cplEDLEssenceDescriptorsMap.get(entry.getKey()).size() > 0){ /*Check if the list of EssenceDescriptors corresponding to this ID is non-empty*/
                List<DOMNodeObjectModel> cplEDLList = cplEDLEssenceDescriptorsMap.get(entry.getKey());/*List of all the EssenceDescriptors identified by the SourceEncodingElement in the EDL*/
                List<DOMNodeObjectModel> virtualTrackEDList = virtualTracksEssenceDescriptorsMap.get(entry.getKey());/*List of all the EssenceDescriptors in the Essence identified by the SourceEncodingElement in the EDL*/
                for(DOMNodeObjectModel domNodeObjectModel : cplEDLList){/*This assumes that there could be multiple EssenceDescriptors in the EDL identified by the same ID*/
                    boolean intermediateResult = false;
                    /**
                     * Find atleast one EssenceDescriptor in the Essence that matches every EssenceDescriptor identified by
                     * this source encoding element ID in the EssenceDescriptorList.
                     */
                    for(DOMNodeObjectModel otherDomNodeObjectModel : virtualTrackEDList) {
                        intermediateResult |= domNodeObjectModel.equals(otherDomNodeObjectModel);
                        if(intermediateResult){
                            break;
                        }
                    }
                    if(!intermediateResult) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Map<UUID, List<DOMNodeObjectModel>> getEssenceDescriptorsObjectModel(Map<UUID, List<Node>> map){
        Map<UUID, List<DOMNodeObjectModel>> essenceDescriptorsMap = new LinkedHashMap<>();
        for(Map.Entry entry : map.entrySet()){
            List<Node> cplEssenceDescriptorNodes = (List<Node>) entry.getValue(); /*List of nodes corresponding to this essence's essence descriptors*/
            /**
             * Every essence descriptor in the CPL's EDL would be represented using a map as its object model.
             * In this case the implementation allows for multiple essence descriptors of the same type in the EDL, for e.g.
             * multiple CDCIPictureEssenceDescriptors in a single essence.
             */
            List<DOMNodeObjectModel> essenceDescriptorsNodeList = new ArrayList<>();
            for(Node node : cplEssenceDescriptorNodes){
                DOMNodeObjectModel DOMNodeObjectModel = CompositionPlaylistHelper.getObjectModelForDOMNode(node);
                essenceDescriptorsNodeList.add(DOMNodeObjectModel);
            }
            essenceDescriptorsMap.put((UUID)entry.getKey(), essenceDescriptorsNodeList);
        }
        return essenceDescriptorsMap;
    }

}
