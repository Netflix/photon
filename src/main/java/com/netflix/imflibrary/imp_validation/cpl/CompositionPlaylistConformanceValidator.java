package com.netflix.imflibrary.imp_validation.cpl;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
     * go beyond structural checks of the CompositionPlaylist such as schema validation.
     * @param compositionPlaylistRecord corresponding to the CompositionPlaylist
     * @return boolean to indicate of the CompositionPlaylist is conformant or not
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public boolean isCompositionPlaylistConformed(CompositionPlaylistRecord compositionPlaylistRecord) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {
        boolean result = true;
        /*
         * The algorithm for conformance checking a CompositionPlaylist (CPL) would be
         * 1) Verify that every EssenceDescriptor element in the EssenceDescriptor list is referenced through its id element
         * by at least one TrackFileResource within the Virtual tracks in the CompositionPlaylist (see section 6.1.10 of SMPTE st2067-3:2-13).
         * 2) Verify that all track file resources within a virtual track have a corresponding essence descriptor in the essence descriptor list.
         * 3) Verify that the EssenceDescriptors in the EssenceDescriptorList element in the CompositionPlaylist are present in
         * the physical essence files referenced by the resources of a virtual track.
         */
        CompositionPlaylist compositionPlaylist = compositionPlaylistRecord.getCompositionPlaylist();
        List<EssenceDescriptorBaseType> essenceDescriptorList = compositionPlaylist.getCompositionPlaylistType().getEssenceDescriptorList().getEssenceDescriptor();
        HashSet<UUID> essenceDescriptorIdsSet = new LinkedHashSet<>();
        for(EssenceDescriptorBaseType essenceDescriptorBaseType : essenceDescriptorList){
            essenceDescriptorIdsSet.add(UUIDHelper.fromUUIDAsURNStringToUUID(essenceDescriptorBaseType.getId()));
        }

        /**
         * Get the complete list of SourceEncoding elements in all the TrackFileResources in the CPL.
         */
        List<CompositionPlaylist.VirtualTrack>virtualTracks =  CompositionPlaylistHelper.getVirtualTracks(compositionPlaylistRecord);
        LinkedHashSet<UUID> resourceSourceEncodingElementsSet = new LinkedHashSet<>();
        for(CompositionPlaylist.VirtualTrack virtualTrack : virtualTracks){
            List<CompositionPlaylistHelper.ResourceIdTuple> resourceIdTuples = CompositionPlaylistHelper.getVirtualTrackResourceIDs(virtualTrack);
            for(CompositionPlaylistHelper.ResourceIdTuple resourceIdTuple : resourceIdTuples){
                resourceSourceEncodingElementsSet.add(resourceIdTuple.getSourceEncoding());
            }
        }
        /*The following check simultaneously verifies 1) and 2) from above.*/
        if(!essenceDescriptorIdsSet.equals(resourceSourceEncodingElementsSet)){
            throw new IMFException(String.format("At least one of the EssenceDescriptors in the EssenceDescriptorList is not referenced by a TrackFileResource or there is at least one TrackFileResource that is not referenced by a EssenceDescriptor in the EssenceDescriptorList"));
        }


        return result;
    }



}
