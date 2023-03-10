/*
 *
 * Copyright 2016 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.utils.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A class that models an IMF Composition Playlist structure.
 */
@Immutable
public final class IMFCompositionPlaylistType {
    private final UUID id;
    private final Composition.EditRate editRate;
    private final String annotation;
    private final String issuer;
    private final String creator;
    private final String contentOriginator;
    private final String contentTitle;
    private final List<IMFSegmentType> segmentList;
    private final List<IMFEssenceDescriptorBaseType> essenceDescriptorList;
    private final IMFErrorLogger imfErrorLogger;
    private final String coreConstraintsSchema;
    private final Set<String> applicationIdSet;

    /**
     * @deprecated
     * This constructor is the legacy constructor, it uses a single String for the application id
     * but a CPL may declare that it conforms to multiple application ids.
     * The constructor using a Set should be preferred.
     */
    @Deprecated
    public IMFCompositionPlaylistType(String id,
                                      List<Long> editRate,
                                      String annotation,
                                      String issuer,
                                      String creator,
                                      String contentOriginator,
                                      String contentTitle,
                                      List<IMFSegmentType> segmentList,
                                      List<IMFEssenceDescriptorBaseType> essenceDescriptorList,
                                      String coreConstraintsSchema,
                                      String applicationId)
    {
        this(id, editRate, annotation, issuer, creator, contentOriginator, contentTitle, segmentList, essenceDescriptorList, coreConstraintsSchema, (applicationId == null ? new HashSet<>() : new HashSet<String>(Arrays.asList(applicationId))));
    }

    public IMFCompositionPlaylistType(String id,
                                   List<Long> editRate,
                                   String annotation,
                                   String issuer,
                                   String creator,
                                   String contentOriginator,
                                   String contentTitle,
                                   List<IMFSegmentType> segmentList,
                                   List<IMFEssenceDescriptorBaseType> essenceDescriptorList,
                                   String coreConstraintsSchema,
                                   @Nonnull Set<String> applicationIds)
    {
        this.id                = UUIDHelper.fromUUIDAsURNStringToUUID(id);
        Composition.EditRate rate = null;
        imfErrorLogger = new IMFErrorLoggerImpl();
        try
        {
            rate = new Composition.EditRate(editRate);
        }
        catch(IMFException e)
        {
            imfErrorLogger.addAllErrors(e.getErrors());
        }

        this.editRate          = rate;
        this.annotation        = annotation;
        this.issuer            = issuer;
        this.creator           = creator;
        this.contentOriginator = contentOriginator;
        this.contentTitle      = contentTitle;
        this.segmentList       = Collections.unmodifiableList(segmentList);
        this.essenceDescriptorList  = Collections.unmodifiableList(essenceDescriptorList);
        this.coreConstraintsSchema = coreConstraintsSchema;
        this.applicationIdSet = Collections.unmodifiableSet(applicationIds);

        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("Failed to create IMFBaseResourceType", imfErrorLogger);
        }
    }

    private static final Set<String> supportedCPLSchemaURIs = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-3/2013");
        add("http://www.smpte-ra.org/schemas/2067-3/2016");
    }});

    @Nonnull
    private static final String getCompositionNamespaceURI(ResourceByteRangeProvider resourceByteRangeProvider, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {

        String result = "";

        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, exception.getMessage()));
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, exception.getMessage()));
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, exception.getMessage()));
                }
            });
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for (String cplNamespaceURI : supportedCPLSchemaURIs) {
                nodeList = document.getElementsByTagNameNS(cplNamespaceURI, "CompositionPlaylist");
                if (nodeList != null && nodeList.getLength() == 1) {
                    result = cplNamespaceURI;
                    break;
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            String message = String.format("Error occurred while trying to determine the Composition Playlist " +
                    "Namespace URI, XML document appears to be invalid. Error Message : %s", e.getMessage());
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        if (result.isEmpty()) {
            String message = String.format("Please check the CPL document and namespace URI, currently we only " +
                    "support the following schema URIs %s", Utilities.serializeObjectCollectionToString
                    (supportedCPLSchemaURIs));
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                    .ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
        return result;
    }


    /**
     * A method that confirms if the inputStream corresponds to a Composition document instance.
     *
     * @param resourceByteRangeProvider corresponding to the Composition XML file.
     * @return a boolean indicating if the input file is a Composition document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isCompositionPlaylist(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = null;
            for (String cplNamespaceURI : supportedCPLSchemaURIs) {
                nodeList = document.getElementsByTagNameNS(cplNamespaceURI, "CompositionPlaylist");
                if (nodeList != null
                        && nodeList.getLength() == 1) {
                    return true;
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            return false;
        }

        return false;
    }

    public static IMFCompositionPlaylistType getCompositionPlayListType(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException
    {
        // Determine which version of the CPL namespace is being used
        String cplNamespace = getCompositionNamespaceURI(resourceByteRangeProvider, imfErrorLogger);

        if (cplNamespace.equals("http://www.smpte-ra.org/schemas/2067-3/2013"))
        {
            org.smpte_ra.schemas._2067_3._2013.CompositionPlaylistType jaxbCpl
                    = CompositionModel_st2067_2_2013.unmarshallCpl(resourceByteRangeProvider, imfErrorLogger);

            return CompositionModel_st2067_2_2013.getCompositionPlaylist(jaxbCpl, imfErrorLogger);
        }
        else if (cplNamespace.equals("http://www.smpte-ra.org/schemas/2067-3/2016"))
        {
            org.smpte_ra.schemas._2067_3._2016.CompositionPlaylistType jaxbCpl
                    = CompositionModel_st2067_2_2016.unmarshallCpl(resourceByteRangeProvider, imfErrorLogger);

            return CompositionModel_st2067_2_2016.getCompositionPlaylist(jaxbCpl, imfErrorLogger);
        }
        else
        {
            String message = String.format("Please check the CPL document and namespace URI, currently we " +
                    "only support the following schema URIs %s", supportedCPLSchemaURIs);
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                    .IMFErrors.ErrorLevels.FATAL, message);
            throw new IMFException(message, imfErrorLogger);
        }
    }

    /**
     * Getter for the Composition Playlist ID
     * @return a string representing the urn:uuid of the Composition Playlist
     */
    public UUID getId(){
        return this.id;
    }

    /**
     * Getter for the EditRate of the Composition Playlist
     * @return a Composition.EditRate object of the Composition Playlist
     */
    public Composition.EditRate getEditRate(){
        return this.editRate;
    }

    /**
     * Getter for the Composition Playlist annotation
     * @return a string representing annotation of the Composition Playlist
     */
    public String getAnnotation(){
        return this.annotation;
    }

    /**
     * Getter for the Composition Playlist issuer
     * @return a string representing issuer of the Composition Playlist
     */
    public String getIssuer(){
        return this.issuer;
    }

    /**
     * Getter for the Composition Playlist creator
     * @return a string representing creator of the Composition Playlist
     */
    public String getCreator(){
        return this.creator;
    }

    /**
     * Getter for the Composition Playlist contentOriginator
     * @return a string representing contentOriginator of the Composition Playlist
     */
    public String getContentOriginator(){
        return this.contentOriginator;
    }

    /**
     * Getter for the Composition Playlist contentTitle
     * @return a string representing contentTitle of the Composition Playlist
     */
    public String getContentTitle(){
        return this.contentTitle;
    }

    /**
     * Getter for the SegmentList of the Composition Playlist
     * @return a string representing the SegmentList of the Composition Playlist
     */
    public List<IMFSegmentType> getSegmentList(){
        return this.segmentList;
    }

    /**
     * Getter for the EssenceDescriptorlist of the Composition Playlist
     * @return a string representing the EssenceDescriptorlist of the Composition Playlist
     */
    public List<IMFEssenceDescriptorBaseType> getEssenceDescriptorList(){
        return this.essenceDescriptorList;
    }

    /**
     * Getter for the CoreConstraints URI corresponding to this CompositionPlaylist
     *
     * @return the uri for the CoreConstraints schema for this CompositionPlaylist
     */
    @Nonnull public String getCoreConstraintsSchema() {
        return this.coreConstraintsSchema;
    }

    /**
     * Getter for the ApplicationIdentification corresponding to this CompositionPlaylist
     *
     * @return a string representing ApplicationIdentification for this CompositionPlaylist
     *
     * @deprecated
     * A CPL may declare multiple Application identifiers, the getter that returns a Set should be used instead.
     */
    @Deprecated
    public String getApplicationIdentification() {
        if (this.applicationIdSet.size() > 0) {
            return this.applicationIdSet.iterator().next();
        } else {
            return "";
        }
    }

    /**
     * Getter for the ApplicationIdentification Set corresponding to this CompositionPlaylist
     *
     * @return a set of all the strings representing ApplicationIdentification for this CompositionPlaylist
     */
    public Set<String> getApplicationIdentificationSet() {
        return this.applicationIdSet;
    }
}
