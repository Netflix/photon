package com.netflix.imflibrary.imp_validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.RepeatableInputStream;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A class that can validate IMP assets to confirm if the delivery is valid.
 */
public final class IMFMasterPackage {

    private final List<InputStream> packingListStreams = new ArrayList<>();
    private final List<InputStream> assetMapStreams = new ArrayList<>();
    private final List<InputStream> compositionPlaylistStreams = new ArrayList<>();
    private final Integer numberOfAssets;
    private static final String assetMapFileNamePattern = "^ASSETMAP\\.xml$";
    private static final String packingListFileNamePattern = "i)PKL";
    private static final String compositionPlaylistFileNamePattern = "i)CPL";
    private static final String xmlExtension = "(.*)(\\.)((X|x)(M|m)(L|l))";
    private static final Logger logger = LoggerFactory.getLogger(IMFMasterPackage.class);


    /**
     * A constructor that models an IMF Master Package as an object.
     * @param inputStreams - list of inputStreams corresponding to the files that are a part of the IMP delivery
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public IMFMasterPackage(List<InputStream> inputStreams) throws IOException, SAXException, JAXBException, URISyntaxException{
        this.numberOfAssets = inputStreams.size();
        for(InputStream inputStream : inputStreams){
            InputStream repeatableInputStream = new RepeatableInputStream(inputStream);
            if (isFileOfSupportedSchema(repeatableInputStream, AssetMap.supportedAssetMapSchemaURIs, "AssetMap")) {
                assetMapStreams.add(repeatableInputStream);
            } else if (isFileOfSupportedSchema(repeatableInputStream, PackingList.supportedPKLSchemaURIs, "PackingList")) {
                packingListStreams.add(repeatableInputStream);
            } else if (isFileOfSupportedSchema(repeatableInputStream, CompositionPlaylist.supportedCPLSchemaURIs, "CompositionPlaylist")) {
                compositionPlaylistStreams.add(repeatableInputStream);
            }
        }
        this.validate();
    }

    /**
     * A template method that performs structural validation of an IMP delivery, implying the AssetMap and PackingList are
     * inspected.
     * @return boolean result of IMP validation
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    private boolean validate() throws IOException, SAXException, JAXBException, URISyntaxException {
        boolean result = true;
        this.resetAllInputStreams();

        if(assetMapStreams.size() > 1){
            throw new IMFException(String.format("If the IMP has an AssetMap exactly one is expected, however %d are present in the IMP", assetMapStreams.size()));
        }

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        AssetMap assetMap = new AssetMap(this.assetMapStreams.get(0), imfErrorLogger);
        this.resetAssetMapInputStreams();

        if(packingListStreams.size() != 1
                || assetMap.getPackingListAssets().size() != 1){
            throw new IMFException(String.format("Exactly one PackingList is expected, %d were detected however AssetMap references %d packing lists in the IMP", packingListStreams.size(), assetMap.getPackingListAssets().size()));
        }
        PackingList packingList = new PackingList(this.packingListStreams.get(0));
        this.resetPackingListInputStreams();

        /*PKL validation*/
        if(packingList.getAssets().size() != this.numberOfAssets){
            throw new IMFException(String.format("Packing list references %d assets, however %d assets were present in the IMP delivery.", packingList.getAssets().size(), this.numberOfAssets));
        }

        if(assetMap.getAssetList().size() != packingList.getAssets().size()){
            throw new IMFException(String.format("Every asset referenced in the PackingList should have an entry in the AssetMap, this does not seem to be the case since PKL has %d assets and AssetMap has %d assets", packingList.getAssets().size(), assetMap.getAssetList().size()));
        }

        List<UUID> assetUUIDsAssetMap = new ArrayList<>();
        assetUUIDsAssetMap.add(assetMap.getUUID());//Add the AssetMap's UUID to the list since that should be present in the PKL's asset list
        for(AssetMap.Asset asset : assetMap.getAssetList()){
            assetUUIDsAssetMap.add(asset.getUUID());
        }
        //Sort the UUIDs in the AssetMap
        assetUUIDsAssetMap.sort(new Comparator<UUID>() {
                                    @Override
                                    public int compare(UUID o1, UUID o2) {
                                        return o1.compareTo(o2);
                                    }
                                });

        List<UUID> assetUUIDsPackingList = new ArrayList<>();
        for(PackingList.Asset asset : packingList.getAssets()){
            assetUUIDsPackingList.add(asset.getUUID());
        }

        //Sort the UUIDs in the PackingList
        assetUUIDsPackingList.sort(new Comparator<UUID>() {
            @Override
            public int compare(UUID o1, UUID o2) {
                return o1.compareTo(o2);
            }
        });

        if(assetUUIDsAssetMap != assetUUIDsPackingList){
            throw new IMFException(String.format("Asset/s in AssetMap and PackingList have different UUIDs, this is invalid per IMF requirements"));
        }

        List<AssetMap.Asset>packingListAssets = assetMap.getPackingListAssets();
        if(!packingListAssets.get(0).getUUID().equals(packingList.getUUID())){
            throw new IMFException(String.format("Packing list UUID %s is different from what is referenced in the AssetMap %s", UUIDHelper.fromUUIDAsURNStringToUUID(packingList.getUUID().toString()), UUIDHelper.fromUUIDAsURNStringToUUID(packingListAssets.get(0).getUUID().toString())));
        }

        return result;
    }

    /**
     * A getter for the AssetMap object model corresponding to the AssetMap file in the IMF Master Package
     * @return the object model corresponding to the AssetMap document
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public AssetMap getAssetMap() throws IOException, SAXException, JAXBException, URISyntaxException{
        this.resetAssetMapInputStreams();
        AssetMap assetMap = new AssetMap(this.assetMapStreams.get(0), new IMFErrorLoggerImpl());
        this.resetAssetMapInputStreams();
        return assetMap;
    }

    /**
     * A getter for the PackingList object model corresponding to the PackingList file in the IMF Master Package
     * @return the object model corresponding to the PackingList document
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public PackingList getPackingList() throws IOException, SAXException, JAXBException, URISyntaxException{
        this.resetPackingListInputStreams();
        PackingList packingList = new PackingList(this.packingListStreams.get(0));
        this.resetPackingListInputStreams();
        return packingList;
    }

    /**
     * A getter for a list of CompositionPlaylist objects corresponding to the CompositionPlaylist files in the IMF Master Package
     * @return the object model corresponding to the PackingList document
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public List<CompositionPlaylist> getCompositionPlayLists() throws IOException, SAXException, JAXBException, URISyntaxException{
        this.resetCompositionPlaylistInputStreams();
        List<CompositionPlaylist> compositionPlaylists = new ArrayList<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        for(InputStream inputStream : compositionPlaylistStreams){
            compositionPlaylists.add(new CompositionPlaylist(inputStream, imfErrorLogger));
        }
        this.resetCompositionPlaylistInputStreams();
        return compositionPlaylists;
    }

    private boolean isFileOfSupportedSchema(InputStream inputStream, List<String> supportedSchemaURIs, String tagName) throws IOException {
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            inputStream.reset();

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

    private void resetAllInputStreams() throws IOException {
        this.resetAssetMapInputStreams();
        this.resetPackingListInputStreams();
        this.resetCompositionPlaylistInputStreams();
    }

    private void resetAssetMapInputStreams() throws IOException {
        for(InputStream inputStream : this.assetMapStreams){
            inputStream.reset();
        }
    }

    private void resetPackingListInputStreams() throws IOException {
        for(InputStream inputStream : this.packingListStreams){
            inputStream.reset();
        }
    }

    private void resetCompositionPlaylistInputStreams() throws IOException {
        for(InputStream inputStream : this.compositionPlaylistStreams){
            inputStream.reset();
        }
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
        List<InputStream> inputStreams = new ArrayList<>();
        for(String string : args) {
            inputStreams.add(new FileInputStream(new File(string)));
        }

        IMFMasterPackage imfMasterPackage = new IMFMasterPackage(inputStreams);
        if(imfMasterPackage.validate()){
            logger.info(String.format("IMF Master package has been validated"));
        }
        else{
            logger.error(String.format("IMF Master package has invalid assets"));
        }
        for(InputStream inputStream : inputStreams){
            inputStream.close();
        }
        System.exit(0);
    }
}
