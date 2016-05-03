package com.netflix.imflibrary.imp_validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A class that can validate IMP assets to confirm if the delivery is valid.
 */
public final class IMFMasterPackage {

    private final List<ResourceByteRangeProvider> packingListsResourceByteRangeProviders = new ArrayList<>();
    private final List<ResourceByteRangeProvider> assetMaps = new ArrayList<>();
    private final List<ResourceByteRangeProvider> compositionPlaylists = new ArrayList<>();
    private final Integer numberOfAssets;
    private static final String assetMapFileNamePattern = "^ASSETMAP\\.xml$";
    private static final String packingListFileNamePattern = "i)PKL";
    private static final String compositionPlaylistFileNamePattern = "i)CPL";
    private static final String xmlExtension = "(.*)(\\.)((X|x)(M|m)(L|l))";
    private static final Logger logger = LoggerFactory.getLogger(IMFMasterPackage.class);
    private final IMFErrorLogger imfErrorLogger;


    /**
     * A constructor that models an IMF Master Package as an object.
     * @param resourceByteRangeProviders - list of ResourceByteRangeProvider objects corresponding to the files that are a part of the IMF Master Package
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public IMFMasterPackage(List<ResourceByteRangeProvider> resourceByteRangeProviders, IMFErrorLogger imfErrorLogger) throws IOException{
        this.imfErrorLogger = imfErrorLogger;
        this.numberOfAssets = resourceByteRangeProviders.size();
        for(ResourceByteRangeProvider resourceByteRangeProvider : resourceByteRangeProviders){
            if (isFileOfSupportedSchema(resourceByteRangeProvider, AssetMap.supportedAssetMapSchemaURIs, "AssetMap")) {
                assetMaps.add(resourceByteRangeProvider);
            } else if (isFileOfSupportedSchema(resourceByteRangeProvider, PackingList.supportedPKLSchemaURIs, "PackingList")) {
                packingListsResourceByteRangeProviders.add(resourceByteRangeProvider);
            } else if (isFileOfSupportedSchema(resourceByteRangeProvider, CompositionPlaylist.supportedCPLSchemaURIs, "CompositionPlaylist")) {
                compositionPlaylists.add(resourceByteRangeProvider);
            }
        }
        this.validateIMP(this.imfErrorLogger);
    }

    private boolean validateIMP(IMFErrorLogger imfErrorLogger) throws IOException {
        return  (this.validateAssetMapAndPKLs(imfErrorLogger) && this.validateCPLs(imfErrorLogger));
    }

    /**
     * A template method that performs structural validation of an IMP delivery, implying the AssetMap and PackingList are
     * inspected.
     * @param imfErrorLogger an object that collects the errors observed during validation
     * @return boolean result of AssetMap and PKL validation
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    private boolean validateAssetMapAndPKLs(IMFErrorLogger imfErrorLogger) throws IOException {

        if(assetMaps.size() > 1){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("If the IMP has an AssetMap exactly one is expected, however %d are present in the IMP", assetMaps.size()));
        }

        AssetMap assetMap = null;
        try {
            assetMap = new AssetMap(this.assetMaps.get(0), this.imfErrorLogger);
        }
        catch (SAXException | JAXBException | URISyntaxException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The AssetMap delivered in this package is invalid. Error %s ocurred while trying to read and parse an AssetMap document", e.getMessage()));
            return false;
        }

        if(packingListsResourceByteRangeProviders.size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_MASTER_PACKAGE_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Atleast one PackingList is expected, %d were detected", packingListsResourceByteRangeProviders.size()));
            return false;
        }

        if(assetMap.getPackingListAssets().size() == 0){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Asset map should reference atleast one PackingList, %d references found", assetMap.getPackingListAssets().size()));
            return false;
        }

        List<PackingList> packingLists = new ArrayList<>();
        try {
            for (ResourceByteRangeProvider resourceByteRangeProvider : packingListsResourceByteRangeProviders) {
                packingLists.add(new PackingList(this.packingListsResourceByteRangeProviders.get(0), this.imfErrorLogger));
            }
        }
        catch (SAXException | JAXBException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Atleast one of the PKLs delivered in this package is invalid. Error %s ocurred while trying to read and parse a PKL document", e.getMessage()));
            return false;
        }

        List<UUID> assetUUIDsAssetMapList = new ArrayList<>();
        assetUUIDsAssetMapList.add(assetMap.getUUID());//Add the AssetMap's UUID to the list since that should be present in the PKL's asset list
        for(AssetMap.Asset asset : assetMap.getAssetList()){
            assetUUIDsAssetMapList.add(asset.getUUID());
        }

/*
        //Sort the UUIDs in the AssetMap
        assetUUIDsAssetMapList.sort(new Comparator<UUID>() {
                                    @Override
                                    public int compare(UUID o1, UUID o2) {
                                        return o1.compareTo(o2);
                                    }
                                });
*/

        /* Collect all the assets in all of the PKLs that are a part of this IMP delivery */
        List<UUID> assetUUIDsPackingList = new ArrayList<>();
        for(PackingList packingList : packingLists) {
            for (PackingList.Asset asset : packingList.getAssets()) {
                assetUUIDsPackingList.add(asset.getUUID());
            }
        }

/*
        //Sort the UUIDs in the PackingList
        assetUUIDsPackingList.sort(new Comparator<UUID>() {
            @Override
            public int compare(UUID o1, UUID o2) {
                return o1.compareTo(o2);
            }
        });
*/

        /* Check to see if all the Assets referenced in the PKL are also referenced by the Asset Map */
        Set<UUID> assetUUIDsAssetMapSet = new HashSet<>(assetUUIDsAssetMapList);
        Set<UUID> assetUUIDsPKLSet = new HashSet<>(assetUUIDsPackingList);

        StringBuilder unreferencedPKLAssetsUUIDs = new StringBuilder();
        for(UUID uuid : assetUUIDsPKLSet){
            if(!assetUUIDsAssetMapSet.contains(uuid)) {
                unreferencedPKLAssetsUUIDs.append(uuid.toString());
                unreferencedPKLAssetsUUIDs.append(" ");
            }
        }

        if(!unreferencedPKLAssetsUUIDs.toString().isEmpty()){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The following UUID/s %s in the Packing list are not referenced by the AssetMap.", unreferencedPKLAssetsUUIDs.toString()));
            return false;
        }

        /* Check if all the assets in the AssetMap that are supposed to be PKLs have the same UUIDs as the PKLs themselves */
        Set<AssetMap.Asset> packingListAssetsSet = new HashSet<>(assetMap.getPackingListAssets());
        StringBuilder unreferencedPKLUUIDs = new StringBuilder();
        for(PackingList packingList : packingLists) {
            if (!packingListAssetsSet.contains(packingList.getUUID())) {
                unreferencedPKLUUIDs.append(packingList.getUUID());
            }
        }
        if(!unreferencedPKLUUIDs.toString().isEmpty()) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_AM_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The following Packing lists %s are not referenced in the AssetMap", unreferencedPKLUUIDs.toString()));
            return false;
        }
        return true;
    }

    /**
     * A template method that performs structural validation of CPLs contained within an IMP delivery.
     * @param imfErrorLogger an object that collects the errors observed during validation
     * @return boolean result of CPL validation
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    private boolean validateCPLs(IMFErrorLogger imfErrorLogger) throws IOException {
        //Validate the CompositionPlaylists
        for(ResourceByteRangeProvider resourceByteRangeProvider : this.compositionPlaylists){
            try {
                new CompositionPlaylist(resourceByteRangeProvider, imfErrorLogger);
            }
            catch (SAXException | JAXBException | URISyntaxException e){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("Atleast one of the CPLs delivered in this package is invalid"));
                return false;
            }
        }
        return true;
    }

    /**
     * A getter for the AssetMap object model corresponding to the AssetMap document in the IMF Master Package
     * @return the object model corresponding to the AssetMap document in the IMF Master Package
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public AssetMap getAssetMap() throws IOException, SAXException, JAXBException, URISyntaxException{
        AssetMap assetMap = new AssetMap(this.assetMaps.get(0), new IMFErrorLoggerImpl());
        return assetMap;
    }

    /**
     * A getter for the PackingList object model corresponding to the PackingList documents in the IMF Master Package
     * @return the object model corresponding to the PackingList document in the IMF Master Package
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public List<PackingList> getPackingLists() throws IOException, SAXException, JAXBException, URISyntaxException{
        List<PackingList> packingLists = new ArrayList<>();
        for(ResourceByteRangeProvider resourceByteRangeProvider : this.packingListsResourceByteRangeProviders) {
            packingLists.add(new PackingList(resourceByteRangeProvider, this.imfErrorLogger));
        }
        return packingLists;
    }

    /**
     * A getter for a list of CompositionPlaylist objects corresponding to the CompositionPlaylist documents in the IMF Master Package
     * @return the object model corresponding to every CompositionPlaylist document in the IMF Master Package
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public List<CompositionPlaylist> getCompositionPlayLists() throws IOException, SAXException, JAXBException, URISyntaxException{
        List<CompositionPlaylist> compositionPlaylists = new ArrayList<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        for(ResourceByteRangeProvider resourceByteRangeProvider : this.compositionPlaylists){
            compositionPlaylists.add(new CompositionPlaylist(resourceByteRangeProvider, imfErrorLogger));
        }
        return compositionPlaylists;
    }

    private boolean isFileOfSupportedSchema(ResourceByteRangeProvider resourceByteRangeProvider, List<String> supportedSchemaURIs, String tagName) throws IOException {
        try(InputStream inputStream = resourceByteRangeProvider.getByteRangeAsStream(0, resourceByteRangeProvider.getResourceSize()-1);)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            NodeList nodeList = null;
            for(String supportedSchemaURI : supportedSchemaURIs) {
                //obtain root node
                nodeList = document.getElementsByTagNameNS(supportedSchemaURI, tagName);
                if (nodeList != null
                        && nodeList.getLength() == 1)
                {
                    return true;
                }
            }
        }
        catch(ParserConfigurationException | SAXException e)
        {
            return false;
        }

        return false;
    }


    /**
     * A sample main method implementation for exercising the IMFMasterPackage methods
     * @param args list of files
     * @throws Exception any errors while performing analysis are exposed through an Exception.
     */

    public static void main(String args[]) throws Exception{
        if(args.length == 0){
            System.out.println(String.format("At least 1 file needs to be specified"));
            System.exit(-1);
        }
        List<ResourceByteRangeProvider> resourceByteRangeProviders = new ArrayList<>();
        for(String string : args) {
            resourceByteRangeProviders.add(new FileByteRangeProvider(new File(string)));
        }

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        IMFMasterPackage imfMasterPackage = new IMFMasterPackage(resourceByteRangeProviders, imfErrorLogger);
        if(imfMasterPackage.validateIMP(imfErrorLogger)){
            logger.info(String.format("IMF Master package has been validated"));
        }
        else{
            logger.error(String.format("IMF Master package has invalid assets"));
        }
        System.exit(0);
    }
}
