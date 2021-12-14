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

package com.netflix.imflibrary.st2067_100;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.st2067_100.handle.Handle;
import com.netflix.imflibrary.st2067_100.handle.MCADictionaryIdHandle;
import com.netflix.imflibrary.st2067_100.handle.MCALinkIdHandle;
import com.netflix.imflibrary.st2067_100.handle.MCATagSymbolHandle;
import com.netflix.imflibrary.st2067_100.handle.MacroHandle;
import com.netflix.imflibrary.st2067_100.handle.VirtualTrackHandle;
import com.netflix.imflibrary.st2067_100.macro.Macro;
import com.netflix.imflibrary.st2067_100.macro.Sequence;
import com.netflix.imflibrary.st2067_100.macro.preset.PresetMacro;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFEssenceComponentVirtualTrack;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.ValidationEventHandlerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.concurrent.Immutable;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class that models an IMF OutputProfileList structure.
 */
@Immutable
public final class OutputProfileList {
    private final static QName  outputProfileList_QNAME             = new QName("http://www.smpte-ra.org/schemas/2067-100/2014", "OutputProfileList");
    private final static String outputProfileList_context_path      = "org.w3._2000._09.xmldsig_:" +
            "org.smpte_ra.schemas._433._2008.dcmltypes:" +
            "org.smpte_ra.schemas._2067_100._2014:" +
            "org.smpte_ra.schemas._2067_101._2014.color_schemes:" +
            "org.smpte_ra.schemas._2067_101._2014.crop_macro:" +
            "org.smpte_ra.schemas._2067_101._2014.lanczos:" +
            "org.smpte_ra.schemas._2067_101._2014.pixel_decoder:" +
            "org.smpte_ra.schemas._2067_101._2014.pixel_encoder:" +
            "org.smpte_ra.schemas._2067_101._2014.scale_macro:" +
            "org.smpte_ra.schemas._2067_102._2014:" +
            "org.smpte_ra.schemas._2067_103._2014";

    private static final String dcmlTypes_schema_path               = "org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd";
    private static final String xmldsig_core_schema_path            = "org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd";
    private static final String opl_100a_schema_path = "org/smpte_ra/schemas/st2067_100_2014/st2067-100a-2014.xsd";
    private static final String opl_101a_schema_path = "org/smpte_ra/schemas/st2067_101_2014/st2067-101a-2014.xsd";
    private static final String opl_101b_schema_path = "org/smpte_ra/schemas/st2067_101_2014/st2067-101b-2014.xsd";
    private static final String opl_101c_schema_path = "org/smpte_ra/schemas/st2067_101_2014/st2067-101c-2014.xsd";
    private static final String opl_101d_schema_path = "org/smpte_ra/schemas/st2067_101_2014/st2067-101d-2014.xsd";
    private static final String opl_101e_schema_path = "org/smpte_ra/schemas/st2067_101_2014/st2067-101e-2014.xsd";
    private static final String opl_101f_schema_path = "org/smpte_ra/schemas/st2067_101_2014/st2067-101f-2014.xsd";
    private static final String opl_102a_schema_path = "org/smpte_ra/schemas/st2067_102_2014/st2067-102a-2014.xsd";
    private static final String opl_103b_schema_path = "org/smpte_ra/schemas/st2067_103_2014/st2067-103b-2014.xsd";

    private static final Logger logger = LogManager.getLogger(OutputProfileList.class);

    private final UUID                      id;
    private final String                    annotation;
    private final UUID                      compositionPlaylistId;
    private final IMFErrorLogger            imfErrorLogger;
    private final Map<String, Macro>        macroMap;
    private final Map<String, String>       aliasMap;

    public OutputProfileList(String id,
                             String annotation,
                             String compositionPlaylistId,
                             Map<String, String> aliasMap,
                             Map<String, Macro> macroTypeMap)
    {
        this.id                = UUIDHelper.fromUUIDAsURNStringToUUID(id);
        imfErrorLogger = new IMFErrorLoggerImpl();

        this.annotation                 = annotation;
        this.compositionPlaylistId      = UUIDHelper.fromUUIDAsURNStringToUUID(compositionPlaylistId);
        this.aliasMap                   = Collections.unmodifiableMap(aliasMap);
        this.macroMap                   = Collections.unmodifiableMap(macroTypeMap);
        Map<String, Handle> handleMap   = new HashMap<>();

        for (Map.Entry<String, Macro> entry : this.macroMap.entrySet()) {
            Macro macro = entry.getValue();
            if (macro != null) {
                if(macro instanceof PresetMacro && this.macroMap.size() != 1) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger
                                    .IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("OPL with id %s contains Preset Macro with other macro types", id));
                }
                for (Sequence input : macro.getInputs()) {
                    String inputHandle = getHandle(input.getHandle());
                    if(inputHandle.startsWith("cpl/")) {
                        handleMap.put(inputHandle, new VirtualTrackHandle(inputHandle, null));
                    }
                }
            }
        }

        populateMacroHandles( handleMap);


        if(imfErrorLogger.hasFatalErrors())
        {
            throw new IMFException("Failed to create OutputProfileList", imfErrorLogger);
        }
    }

    /**
     * A method that confirms if the inputStream corresponds to a OutputProfileList document instance.
     *
     * @param resourceByteRangeProvider corresponding to the OutputProfileList XML file.
     * @return a boolean indicating if the input file is a OutputProfileList document
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static boolean isOutputProfileList(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            //obtain root node
            NodeList nodeList = document.getElementsByTagNameNS(outputProfileList_QNAME.getNamespaceURI(), outputProfileList_QNAME.getLocalPart());
            if (nodeList != null && nodeList.getLength() == 1) {
                return true;
            }
        } catch (ParserConfigurationException | SAXException e) {
            return false;
        }
        return false;
    }

    /**
     * A method to get output profile list object model from OutputProfileList document instance.
     * @param resourceByteRangeProvider corresponding to the OutputProfileList XML file.
     * @param imfErrorLogger - an object for logging errors
     * @return Output profile list object model
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public static OutputProfileList getOutputProfileListType(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        JAXBElement jaxbElement = null;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize() - 1);
             InputStream xmldsig_core_is = contextClassLoader.getResourceAsStream(xmldsig_core_schema_path);
             InputStream dcmlTypes_is = contextClassLoader.getResourceAsStream(dcmlTypes_schema_path);
             InputStream imf_opl_100a_is = contextClassLoader.getResourceAsStream(opl_100a_schema_path);
             InputStream imf_opl_101a_is = contextClassLoader.getResourceAsStream(opl_101a_schema_path);
             InputStream imf_opl_101b_is = contextClassLoader.getResourceAsStream(opl_101b_schema_path);
             InputStream imf_opl_101c_is = contextClassLoader.getResourceAsStream(opl_101c_schema_path);
             InputStream imf_opl_101d_is = contextClassLoader.getResourceAsStream(opl_101d_schema_path);
             InputStream imf_opl_101e_is = contextClassLoader.getResourceAsStream(opl_101e_schema_path);
             InputStream imf_opl_101f_is = contextClassLoader.getResourceAsStream(opl_101f_schema_path);
             InputStream imf_opl_102a_is = contextClassLoader.getResourceAsStream(opl_102a_schema_path);
             InputStream imf_opl_103b_is = contextClassLoader.getResourceAsStream(opl_103b_schema_path)
             ) {
            StreamSource[] streamSources = new StreamSource[11];
            streamSources[0] = new StreamSource(xmldsig_core_is);
            streamSources[1] = new StreamSource(dcmlTypes_is);
            streamSources[2] = new StreamSource(imf_opl_100a_is);
            streamSources[3] = new StreamSource(imf_opl_101d_is);
            streamSources[4] = new StreamSource(imf_opl_101b_is);
            streamSources[5] = new StreamSource(imf_opl_101c_is);
            streamSources[6] = new StreamSource(imf_opl_101a_is);
            streamSources[7] = new StreamSource(imf_opl_101e_is);
            streamSources[8] = new StreamSource(imf_opl_101f_is);
            streamSources[9] = new StreamSource(imf_opl_102a_is);
            streamSources[10] = new StreamSource(imf_opl_103b_is);


            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(streamSources);

            ValidationEventHandlerImpl validationEventHandlerImpl = new ValidationEventHandlerImpl(true);
            JAXBContext jaxbContext = JAXBContext.newInstance(outputProfileList_context_path);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(validationEventHandlerImpl);
            unmarshaller.setSchema(schema);

            jaxbElement = (JAXBElement) unmarshaller.unmarshal(inputStream);

            if (validationEventHandlerImpl.hasErrors()) {
                validationEventHandlerImpl.getErrors().stream()
                        .map(e -> new ErrorLogger.ErrorObject(
                                IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR,
                                e.getValidationEventSeverity(),
                                "Line Number : " + e.getLineNumber().toString() + " - " + e.getErrorMessage())
                        )
                        .forEach(imfErrorLogger::addError);

                throw new IMFException(validationEventHandlerImpl.toString(), imfErrorLogger);
            }
        } catch (SAXException | JAXBException e) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger
                            .IMFErrors.ErrorLevels.FATAL,
                    e.getMessage());
            throw new IMFException(e.getMessage(), imfErrorLogger);
        }

        org.smpte_ra.schemas._2067_100._2014.OutputProfileListType outputProfileListTypeJaxb = (org.smpte_ra.schemas._2067_100._2014.OutputProfileListType) jaxbElement.getValue();

        OutputProfileListModel_st2067_100_2014 outputProfileListModel = new OutputProfileListModel_st2067_100_2014(outputProfileListTypeJaxb, imfErrorLogger);
        return outputProfileListModel.getNormalizedOutputProfileList();
    }

    /**
     * A method to apply output profile on an application composition
     * @param applicationComposition ApplicationComposition related to this output profile
     * @return List of errors that occurred while applying output profile on the application composition
     */
    public List<ErrorLogger.ErrorObject> applyOutputProfileOnComposition(ApplicationComposition applicationComposition) {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Map<String, Handle> handleMapConformed = getHandleMapWithApplicationComposition(applicationComposition, imfErrorLogger);

        /**
         * Validate alias handles
         */
        for(String handle: this.aliasMap.values()) {
            Handle handleType = handleMapConformed.get(handle);
            if (handleType == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Invalid handle %s in alias", handle));
            }
        }

        return imfErrorLogger.getErrors();
    }

    /**
     * A method to get handle map with Application Composition applied on output profile
     * @param applicationComposition ApplicationComposition related to this output profile
     * @param imfErrorLogger logger for recording any parsing errors
     * @return Map containing a string handle to object representation of the handle
     */
    public Map<String, Handle> getHandleMapWithApplicationComposition(ApplicationComposition applicationComposition, IMFErrorLogger imfErrorLogger) {
        Map<String, Handle> handleMapConformed = new HashMap<>();

        /**
         * Add handles for CPL tracks
         */
        populateCPLVirtualTrackHandles(applicationComposition, handleMapConformed);

        /**
         * Add handles for OPL macros
         */
        populateMacroHandles(handleMapConformed);

        /**
         * Verify that input dependencies for all the macros are resolved
         */
        for(Map.Entry<String, Macro> entry: this.macroMap.entrySet()) {
            Macro macro = entry.getValue();
            for(Sequence input: macro.getInputs()) {
                Handle handleType = handleMapConformed.get(getHandle(input.getHandle()));
                if (handleType == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("Invalid handle %s in %s macro", input.getHandle(), macro.getName()));
                }
            }
        }

        return handleMapConformed;
    }


    private static Map<String, Handle> populateCPLVirtualTrackHandles(ApplicationComposition applicationComposition, Map<String, Handle> handleMap) {
        List<? extends Composition.VirtualTrack> virtualTrackList = applicationComposition.getVirtualTracks();
        for(Composition.VirtualTrack virtualTrack: virtualTrackList) {
            switch(virtualTrack.getSequenceTypeEnum()) {

                case MainImageSequence: {
                    StringBuilder handleBuilder = new StringBuilder();
                    handleBuilder.append("cpl/virtual-tracks/" + virtualTrack.getTrackID());
                    Handle handleType = new VirtualTrackHandle(handleBuilder.toString(), virtualTrack);
                    handleMap.put(handleBuilder.toString(), handleType);                }
                break;

                case MainAudioSequence: {
                    IMFEssenceComponentVirtualTrack imfEssenceComponentVirtualTrack = (IMFEssenceComponentVirtualTrack) virtualTrack;
                    for (UUID uuid : imfEssenceComponentVirtualTrack.getTrackResourceIds()) {
                        DOMNodeObjectModel domNodeObjectModel = applicationComposition.getEssenceDescriptor(uuid);
                        if (domNodeObjectModel != null) {
                            Set<UL> mcaLabelDictionaryIDs = domNodeObjectModel.getFieldsAsUL("MCALabelDictionaryID");
                            for (UL mcaLabelDictionaryID : mcaLabelDictionaryIDs) {
                                StringBuilder handleBuilder = new StringBuilder();
                                handleBuilder.append("cpl/virtual-tracks/" + virtualTrack.getTrackID());
                                handleBuilder.append("/MCADictionaryLabelID=" + mcaLabelDictionaryID.toStringBytes());
                                Handle handleType = new MCADictionaryIdHandle(handleBuilder.toString(), virtualTrack, mcaLabelDictionaryID);
                                handleMap.put(handleBuilder.toString(), handleType);
                            }

                            Set<UUID> mcaLinkIDs = domNodeObjectModel.getFieldsAsUUID("MCALinkID");
                            for (UUID mcaLinkID : mcaLinkIDs) {
                                StringBuilder handleBuilder = new StringBuilder();
                                handleBuilder.append("cpl/virtual-tracks/" + virtualTrack.getTrackID());
                                handleBuilder.append("/MCALinkID=" + mcaLinkID.toString());
                                Handle handleType = new MCALinkIdHandle(handleBuilder.toString(), virtualTrack, mcaLinkID);
                                handleMap.put(handleBuilder.toString(), handleType);
                            }

                            Set<String> mcaTagSymbols = domNodeObjectModel.getFieldsAsStringRecursive("MCATagSymbol");
                            for (String mcaTagSymbol : mcaTagSymbols) {
                                StringBuilder handleBuilder = new StringBuilder();
                                handleBuilder.append("cpl/virtual-tracks/" + virtualTrack.getTrackID());
                                handleBuilder.append("/MCATagSymbol=" + mcaTagSymbol);
                                Handle handleType = new MCATagSymbolHandle(handleBuilder.toString(), virtualTrack, mcaTagSymbol);
                                handleMap.put(handleBuilder.toString(), handleType);
                            }
                        }
                    }
                }
                break;
            }
        }
        return handleMap;
    }

    private void populateMacroHandles(Map<String, Handle> handleMap) {
        /**
         * Add handles for OPL macros
         */
        for( int iteration = 0; iteration < this.macroMap.size(); iteration++) {
            boolean bAllDependencyMet = true;
            for (Map.Entry<String, Macro> entry : this.macroMap.entrySet()) {
                Macro macro = entry.getValue();
                /* Check for all the input dependencies for the macro */
                if (macro != null && !macro.getOutputs().isEmpty() && !handleMap.containsKey(getHandle(macro.getOutputs().get(0).getHandle()))) {
                    boolean bDependencyMet = true;
                    for (Sequence input : macro.getInputs()) {
                        Handle handleType = handleMap.get(getHandle(input.getHandle()));
                        if (handleType == null) {
                            bDependencyMet = false;
                        }
                    }

                    bAllDependencyMet &= bDependencyMet;
                    /* If input dependencies are met create output handles */
                    if (bDependencyMet) {
                        for (Sequence output : macro.getOutputs()) {
                            String outputHandle = getHandle(output.getHandle());
                            handleMap.put(outputHandle, new MacroHandle(outputHandle, macro));
                        }
                    }
                }
            }
            if(bAllDependencyMet) {
                break;
            }
        }
        /**
         * Verify that input dependencies for all the macros are resolved
         */
        for(Map.Entry<String, Macro> entry: this.macroMap.entrySet()) {
            Macro macro = entry.getValue();
            for(Sequence input: macro.getInputs()) {
                Handle handleType = handleMap.get(getHandle(input.getHandle()));
                if (handleType == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("Invalid handle %s in %s macro", input.getHandle(), macro.getName()));
                }
            }
        }
        /**
         * Validate alias handles
         */
        for(String handle: this.aliasMap.values()) {
            Handle handleType = handleMap.get(handle);
            // Ignore input aliases as they are not needed for dependency resolution
            // Ignore cpl/virtual track aliases too. All track IDs are not available for OPL and hence cannot validate.
            if (handleType == null && !handle.contains("/inputs/") && !handle.startsWith("cpl/virtual-tracks/")) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Invalid handle %s in alias", handle));
            }

        }
    }

    private String getHandle(String handle) {
        if(handle.startsWith("alias/")) {
            handle = handle.replace("alias/", "");
        }
        if(this.aliasMap.containsKey(handle)) {
            handle = this.aliasMap.get(handle);
        }
        return handle;
    }

    /**
     * Getter for the OutputProfileList ID
     * @return a string representing the urn:uuid of the OutputProfileList
     */
    public UUID getId(){
        return this.id;
    }

    /**
     * Getter for the OutputProfileList annotation
     * @return a string representing annotation of the OutputProfileList
     */
    public String getAnnotation(){
        return this.annotation;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public Map<String, Macro> getMacroMap() {
        return macroMap;
    }

    public UUID getCompositionPlaylistId() {
        return compositionPlaylistId;
    }

    public List<ErrorLogger.ErrorObject> getErrors() {
        return imfErrorLogger.getErrors();
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath>%n", OutputProfileList.class.getName()));
        return sb.toString();
    }

    public static void main(String args[]) throws IOException, SAXException, JAXBException
    {
        if (args.length != 1)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.OutputProfileList, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateOPL(payloadRecord);

        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("OutputProfileList Document has %d errors and %d warnings",
                    errors.size() - warningCount, warningCount));
            for(ErrorLogger.ErrorObject errorObject : errors){
                if(errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                }
                else if(errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
        }
        else{
            logger.info("No errors were detected in the OutputProfileList Document.");
        }
    }
}
