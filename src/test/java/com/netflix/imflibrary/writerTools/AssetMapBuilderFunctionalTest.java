/*
 *
 * Copyright 2015 Netflix, Inc.
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
package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFAuthoringException;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.Utilities;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import testUtils.TestHelper;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A test for the AssetMapBuilder
 */

@Test(groups = "functional")
public class AssetMapBuilderFunctionalTest {

    @Test
    public void assetMapBuilderTest() throws IOException, SAXException, JAXBException, URISyntaxException {

        File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/ASSETMAP.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        AssetMap assetMap = new AssetMap(resourceByteRangeProvider, imfErrorLogger);

        /**
         * Build the AssetMapBuilder's AssetList
         */
        List<AssetMap.Asset> assets = assetMap.getAssetList();
        List<AssetMapBuilder.Asset> assetMapBuilderAssets = new ArrayList<>();
        for(AssetMap.Asset asset : assets){
            String annotationText = (asset.isPackingList() ? "PKL" : "Netflix Asset");
            String language = "en";

            AssetMapBuilder.Chunk chunk = new AssetMapBuilder.Chunk(asset.getPath().toString(), Long.valueOf(10L)); //All assets will have a length of 10 bytes perhaps okay for a functional test.
            List<AssetMapBuilder.Chunk> chunks = new ArrayList<AssetMapBuilder.Chunk>() {{ add(chunk);}};
            AssetMapBuilder.Asset assetMapBuilderAsset = new AssetMapBuilder.Asset(asset.getUUID(), AssetMapBuilder.buildAssetMapUserTextType_2007(annotationText, language), asset.isPackingList(), chunks);
            assetMapBuilderAssets.add(assetMapBuilderAsset);
        }

        org.smpte_ra.schemas.st0429_9_2007.AM.UserText annotationText = AssetMapBuilder.buildAssetMapUserTextType_2007("Photon AssetMapBuilder", "en");
        org.smpte_ra.schemas.st0429_9_2007.AM.UserText creator = AssetMapBuilder.buildAssetMapUserTextType_2007("Netflix", "en");
        XMLGregorianCalendar issueDate = IMFUtils.createXMLGregorianCalendar();
        org.smpte_ra.schemas.st0429_9_2007.AM.UserText issuer = AssetMapBuilder.buildAssetMapUserTextType_2007("Netflix", "en");

        /**
         * Create a temporary working directory under home
         */
        String path = System.getProperty("user.home") + File.separator + "IMFDocuments";
        File tempDir = new File(path);

        if(!(tempDir.exists() || tempDir.mkdirs())){
            throw new IOException("Could not create temporary directory");
        }

        IMFErrorLogger assetMapBuilderErrorLogger = new IMFErrorLoggerImpl();
        List<ErrorLogger.ErrorObject> errors = new AssetMapBuilder(assetMap.getUUID(), annotationText, creator, issueDate, issuer, assetMapBuilderAssets, tempDir, assetMapBuilderErrorLogger).build();

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the AssetMap. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()))));
        }

        File assetMapFile = null;
        for(File file : tempDir.listFiles()){
            if(file.getName().contains("AssetMap-")){
                assetMapFile = file;
            }
        }
        if(assetMapFile == null){
            throw new IMFAuthoringException(String.format("AssetMap file does not exist in the working directory %s, IMP is incomplete", tempDir.getAbsolutePath()));
        }
        Assert.assertTrue(assetMapFile.length() > 0);

        List<ErrorLogger.ErrorObject> assetMapValidationErrors = IMPValidator.validateAssetMap(new PayloadRecord(new FileByteRangeProvider(assetMapFile).getByteRangeAsBytes(0, assetMapFile.length()-1), PayloadRecord.PayloadAssetType.AssetMap, 0L, 0L));
        Assert.assertTrue(assetMapValidationErrors.size() == 0);

        //Destroy the temporary working directory
        tempDir.delete();
    }
}
