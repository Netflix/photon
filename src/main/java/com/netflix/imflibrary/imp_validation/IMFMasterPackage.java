package com.netflix.imflibrary.imp_validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0429_8.PackingList;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.st2067_2.CompositionPlaylist;
import com.netflix.imflibrary.utils.UUIDHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A class that can validate IMP assets to confirm if the delivery is valid.
 */
public final class IMFMasterPackage {

    private final List<File> packingLists = new ArrayList<>();
    private final List<File> assetMaps = new ArrayList<>();
    private final List<File> compositionPlaylists = new ArrayList<>();
    private final Integer numberOfAssets;
    private static final String basicMapProfilev2AssetMapNamePattern = "^ASSETMAP\\.xml$";
    private static final String packingListNamePattern = "i)PKL";
    private static final String compositionPlaylistNamePattern = "i)CPL";
    private static final String xmlExtension = "(.*)(\\.)((X|x)(M|m)(L|l))";

    /**
     * A constructor that models an IMF Master Package as an object.
     * @param files - list of files that are a part of the IMP delivery
     * @throws IOException - any I/O related error is exposed through an IOException
     * @throws SAXException - exposes any issues with instantiating a {@link javax.xml.validation.Schema Schema} object
     * @throws JAXBException - any issues in serializing the XML document using JAXB are exposed through a JAXBException
     * @throws URISyntaxException exposes any issues instantiating a {@link java.net.URI URI} object
     */
    public IMFMasterPackage(List<File> files) throws IOException, SAXException, JAXBException, URISyntaxException{
        this.numberOfAssets = files.size();
        for(File file : files){
            if(file.getName().matches(xmlExtension)){
                if (isFileOfSupportedSchema(file, AssetMap.supportedAssetMapSchemaURIs, "AssetMap")) {
                    assetMaps.add(file);
                } else if (isFileOfSupportedSchema(file, PackingList.supportedPKLSchemaURIs, "PackingList")) {
                    packingLists.add(file);
                } else if (isFileOfSupportedSchema(file, CompositionPlaylist.supportedCPLSchemaURIs, "CompositionPlaylist")) {
                    compositionPlaylists.add(file);
                }
            }
        }
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
    public boolean validate() throws IOException, SAXException, JAXBException, URISyntaxException {
        boolean result = true;

        if(assetMaps.size() > 1){
            throw new IMFException(String.format("If the IMP has an AssetMap exactly one is expected, however %d are present in the IMP", assetMaps.size()));
        }

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        AssetMap assetMap = new AssetMap(this.assetMaps.get(0), imfErrorLogger);

        if(packingLists.size() != 1
                || assetMap.getPackingListAssets().size() != 1){
            throw new IMFException(String.format("Exactly one PackingList is expected, %d were detected however AssetMap references %d packing lists in the IMP", packingLists.size(), assetMap.getPackingListAssets().size()));
        }
        PackingList packingList = new PackingList(this.packingLists.get(0));

        List<CompositionPlaylist> cpls = new ArrayList<>();
        for(File file : compositionPlaylists){
            cpls.add(new CompositionPlaylist(file, imfErrorLogger));
        }

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

    private boolean isFileOfSupportedSchema(File xmlFile, List<String>supportedSchemaURIs, String tagName) throws IOException {
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile);


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


    public static void main(String args[]) throws Exception{
        if(args.length == 0){
            System.out.println(String.format("At least 1 file needs to be specified"));
            System.exit(-1);
        }
        List<File> files = new ArrayList<>();
        for(String string : args) {
            files.add(new File(string));
        }

        IMFMasterPackage IMFMasterPackage = new IMFMasterPackage(files);
    }
}
