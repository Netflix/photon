package com.netflix.imflibrary.imp_validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents a CompositionPlaylistRecord that embodies a CPL XML document along with
 * references to Byte Range providers one for every essence that is a part of every virtual track
 * of the composition.
 */
public final class CompositionPlaylistRecord {

    private final File cplXMLFile;
    private final CompositionPlaylist compositionPlaylist;
    private final Map<UUID, ResourceByteRangeProvider> imfEssenceMap;

    private CompositionPlaylistRecord(@Nonnull File cplXMLFile, @Nonnull CompositionPlaylist compositionPlaylist, @Nonnull Map<UUID, ResourceByteRangeProvider> imfEssenceMap){
        this.cplXMLFile = cplXMLFile;
        this.compositionPlaylist = compositionPlaylist;
        this.imfEssenceMap = imfEssenceMap;
    }

    /**
     * This class is a builder for CompositionPlaylistRecord objects
     */
    public static final class CompositionPlaylistRecordBuilder {

        /**
         * A builder method for the CompositionPlaylistRecord object.
         *
         * @param cplXMLFile - File handle to a CompositionPlaylist XML document.
         * @param imfEssenceMap - a map of <UUID, ResourceByteRangeProvider> UUIDs identifying an IMFEssence and a ResourceByteRangeProvider
         *                      corresponding to the essence for deeper inspection.
         * @return A composition playlist record
         * @throws IOException - any I/O related error is exposed through an IOException.
         * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
         * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
         * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
         * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
         * @throws IMFException - any non compliant CPL documents will be signalled through an IMFException
         */
        @Nonnull
        public static CompositionPlaylistRecord build(@Nonnull File cplXMLFile, @Nonnull Map<UUID, ResourceByteRangeProvider> imfEssenceMap) throws IOException, SAXException, JAXBException, URISyntaxException, IMFException {
            CompositionPlaylist compositionPlaylist = null;
            IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
            if (CompositionPlaylist.isCompositionPlaylist(cplXMLFile)) {
                compositionPlaylist = new CompositionPlaylist(cplXMLFile, imfErrorLogger);
                return new CompositionPlaylistRecord(cplXMLFile, compositionPlaylist, imfEssenceMap);
            } else {
                throw new IMFException(String.format("CPL document is not compliant with the supported CPL schemas"));
            }
        }
    }
}
