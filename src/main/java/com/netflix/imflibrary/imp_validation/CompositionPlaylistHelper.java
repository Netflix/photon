package com.netflix.imflibrary.imp_validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.smpte_ra.schemas.st2067_2_2013.TrackFileResourceType;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is an interface to a CompositionPlaylist object model.
 */
public final class CompositionPlaylistHelper {

    /**
     * A stateless helper method to retrieve the VirtualTracks referenced from within a CompositionPlaylist.
     * @param cplXMLFile - File handle to a CompositionPlaylist XML document.
     * @return A map containing UUIDs as keys and VirtualTracks as values.
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    @Nonnull
    public static Map<UUID, CompositionPlaylist.VirtualTrack> getVirtualTracks(@Nonnull File cplXMLFile) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {
        if(CompositionPlaylist.isCompositionPlaylist(cplXMLFile)){
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            CompositionPlaylist compositionPlaylist = new CompositionPlaylist(cplXMLFile, imfErrorLogger);
            return compositionPlaylist.getVirtualTrackMap();
        }
        else{
            throw new IMFException(String.format("CPL document is not compliant with the supported CPL schemas"));
        }
    }

    /**
     * A stateless helper method to retrieve the Resources referenced by a Virtual track within a CompositionPlaylist.
     * @param cplXMLFile - File handle to a CompositionPlaylist XML document.
     * @param virtualTrack - object model of an IMF virtual track {@link com.netflix.imflibrary.st2067_2.CompositionPlaylist.VirtualTrack}
     * @return A list of TrackFileResourceType objects corresponding to the virtual track in the CompositionPlaylist.
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    @Nonnull
    public static List<TrackFileResourceType> getVirtualTrackResourceList(@Nonnull File cplXMLFile,  CompositionPlaylist.VirtualTrack virtualTrack) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {
        if(CompositionPlaylist.isCompositionPlaylist(cplXMLFile)){
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            CompositionPlaylist compositionPlaylist = new CompositionPlaylist(cplXMLFile, imfErrorLogger);
            Map<UUID, List<TrackFileResourceType>> virtualTrackResourceListMap = compositionPlaylist.getVirtualTrackResourceList();
            if(virtualTrackResourceListMap.get(virtualTrack.getTrackID()) != null
                    && virtualTrackResourceListMap.get(virtualTrack.getTrackID()).size() > 0){
                return virtualTrackResourceListMap.get(virtualTrack.getTrackID());
            }
            else{
                throw new IMFException(String.format("Virtual track with ID %s, is not referenced by the CPL, or the resource list for the virtual track is empty (%d)", virtualTrack.getTrackID(), virtualTrackResourceListMap.get(virtualTrack.getTrackID()).size()));
            }
        }
        else{
            throw new IMFException(String.format("CPL document is not compliant with the supported CPL schemas"));
        }
    }

    /**
     * A stateless helper method to retrieve the UUIDs of the Track files referenced by a Virtual track within a CompositionPlaylist.
     * @param cplXMLFile - File handle to a CompositionPlaylist XML document.
     * @param virtualTrack - object model of an IMF virtual track {@link com.netflix.imflibrary.st2067_2.CompositionPlaylist.VirtualTrack}
     * @return A list of TrackFileResourceType objects corresponding to the virtual track in the CompositionPlaylist.
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    @Nonnull
    public static List<UUID> getVirtualTrackResourceIDs(@Nonnull File cplXMLFile,  CompositionPlaylist.VirtualTrack virtualTrack) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {
        if(CompositionPlaylist.isCompositionPlaylist(cplXMLFile)){
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            CompositionPlaylist compositionPlaylist = new CompositionPlaylist(cplXMLFile, imfErrorLogger);
            Map<UUID, List<TrackFileResourceType>> virtualTrackResourceListMap = compositionPlaylist.getVirtualTrackResourceList();
            if(virtualTrackResourceListMap.get(virtualTrack.getTrackID()) != null
                    && virtualTrackResourceListMap.get(virtualTrack.getTrackID()).size() > 0){
                List<TrackFileResourceType> resourceList = virtualTrackResourceListMap.get(virtualTrack.getTrackID());
                List<UUID> virtualTrackResourceIDs = new ArrayList<>();
                for(TrackFileResourceType trackFileResourceType : resourceList){
                    virtualTrackResourceIDs.add(UUIDHelper.fromUUIDAsURNStringToUUID((trackFileResourceType.getTrackFileId())));
                }
                return virtualTrackResourceIDs;
            }
            else{
                throw new IMFException(String.format("Virtual track with ID %s, is not referenced by the CPL, or the resource list (%d resource/s) for the virtual track is empty", virtualTrack.getTrackID(), virtualTrackResourceListMap.get(virtualTrack.getTrackID()).size()));
            }
        }
        else{
            throw new IMFException(String.format("CPL document is not compliant with the supported CPL schemas"));
        }
    }


    /**
     * A stateless helper method to retrieve the UUIDs of the Track files referenced by a Virtual track within a CompositionPlaylist.
     * @param virtualTrack - object model of an IMF virtual track {@link com.netflix.imflibrary.st2067_2.CompositionPlaylist.VirtualTrack}
     * @return A list of TrackFileResourceType objects corresponding to the virtual track in the CompositionPlaylist.
     * @throws IOException - any I/O related error is exposed through an IOException.
     * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    @Nonnull
    public static List<UUID> getVirtualTrackResourceIDs(@Nonnull CompositionPlaylist.VirtualTrack virtualTrack) throws IOException, IMFException, SAXException, JAXBException, URISyntaxException {

        List<TrackFileResourceType> resourceList = virtualTrack.getResourceList();
        List<UUID> virtualTrackResourceIDs = new ArrayList<>();
        if(resourceList != null
                && resourceList.size() > 0) {
            for (TrackFileResourceType trackFileResourceType : resourceList) {
                virtualTrackResourceIDs.add(UUIDHelper.fromUUIDAsURNStringToUUID((trackFileResourceType.getTrackFileId())));
            }
        }
        return virtualTrackResourceIDs;
    }
}
