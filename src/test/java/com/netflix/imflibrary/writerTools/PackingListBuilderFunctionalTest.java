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

/**
 * Functional test for the PackingListBuilder
 */

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFAuthoringException;
import com.netflix.imflibrary.st0429_8.PackingList;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Test(groups = "functional")
public class PackingListBuilderFunctionalTest {

    @Test
    public void packingListBuilder_2007_Test() throws IOException, SAXException, JAXBException {
        File inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        PackingList packingList = new PackingList(resourceByteRangeProvider);
        List<PackingList.Asset> assets = packingList.getAssets();

        List<PackingListBuilder.PackingListBuilderAsset_2007> packingListBuilderAssets = new ArrayList<>();
        for(PackingList.Asset asset : assets){
            org.smpte_ra.schemas.st0429_8_2007.PKL.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2007("Netflix", "en");
            org.smpte_ra.schemas.st0429_8_2007.PKL.UserText originalFileName = PackingListBuilder.buildPKLUserTextType_2007(asset.getOriginalFilename(), "en");
            PackingListBuilder.PackingListBuilderAsset_2007 asset_2007 =
                    new PackingListBuilder.PackingListBuilderAsset_2007(asset.getUUID(),
                                                                        annotationText,
                                                                        asset.getHash(),
                                                                        asset.getSize(),
                                                                        PackingListBuilder.PKLAssetTypeEnum.getAssetTypeEnum(asset.getType()),
                                                                        originalFileName);
            packingListBuilderAssets.add(asset_2007);
        }

        /**
         * Create a temporary working directory under home
         */
        Path tempPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMFDocuments");
        File tempDir = tempPath.toFile();


        IMFErrorLogger packingListBuilderErrorLogger = new IMFErrorLoggerImpl();
        org.smpte_ra.schemas.st0429_8_2007.PKL.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2007("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas.st0429_8_2007.PKL.UserText creator = PackingListBuilder.buildPKLUserTextType_2007("Netflix", "en");
        XMLGregorianCalendar issueDate = IMFUtils.createXMLGregorianCalendar();
        org.smpte_ra.schemas.st0429_8_2007.PKL.UserText issuer = PackingListBuilder.buildPKLUserTextType_2007("Netflix", "en");
        new PackingListBuilder(packingList.getUUID(), issueDate, tempDir, packingListBuilderErrorLogger).buildPackingList_2007(annotationText, issuer, creator, packingListBuilderAssets);

        imfErrorLogger.addAllErrors(packingList.getErrors());
        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()))));
        }

        File pklOutputFile = null;
        for(File file : tempDir.listFiles()){
            if(file.getName().contains("PKL-")){
                pklOutputFile = file;
            }
        }
        if(pklOutputFile == null){
            throw new IMFAuthoringException(String.format("PackingList file does not exist in the working directory %s, cannot generate the rest of the documents", tempDir.getAbsolutePath()));
        }
        Assert.assertTrue(pklOutputFile.length() > 0);

        resourceByteRangeProvider = new FileByteRangeProvider(pklOutputFile);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKL(new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, pklOutputFile.length()-1), PayloadRecord.PayloadAssetType.PackingList, 0L, 0L));
        Assert.assertTrue(errors.size() == 0);

        //Destroy the temporary working directory
        tempDir.delete();
    }

    @Test
    public void packingListBuilder_2016_Test() throws IOException, SAXException, JAXBException {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/PKL_befcd2d4-f35c-45d7-99bb-7f64b51b103c_corrected.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        PackingList packingList = new PackingList(resourceByteRangeProvider);
        imfErrorLogger.addAllErrors(packingList.getErrors());

        List<PackingList.Asset> assets = packingList.getAssets();

        List<PackingListBuilder.PackingListBuilderAsset_2016> packingListBuilderAssets = new ArrayList<>();
        for(PackingList.Asset asset : assets){
            org.smpte_ra.schemas.st2067_2_2016.PKL.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2016("Netflix", "en");
            org.smpte_ra.schemas.st2067_2_2016.PKL.UserText originalFileName = PackingListBuilder.buildPKLUserTextType_2016(asset.getOriginalFilename(), "en");
            org.smpte_ra.schemas.st2067_2_2016.PKL.DigestMethodType hashAlgorithm = new org.smpte_ra.schemas.st2067_2_2016.PKL.DigestMethodType();
            hashAlgorithm.setAlgorithm(asset.getHashAlgorithm());
            PackingListBuilder.PackingListBuilderAsset_2016 asset_2016 =
                    new PackingListBuilder.PackingListBuilderAsset_2016(asset.getUUID(),
                            annotationText,
                            asset.getHash(),
                            hashAlgorithm,
                            asset.getSize(),
                            PackingListBuilder.PKLAssetTypeEnum.getAssetTypeEnum(asset.getType()),
                            originalFileName);
            packingListBuilderAssets.add(asset_2016);
        }

        /**
         * Create a temporary working directory under home
         */
        Path tempPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMFDocuments");
        File tempDir = tempPath.toFile();

        IMFErrorLogger packingListBuilderErrorLogger = new IMFErrorLoggerImpl();
        org.smpte_ra.schemas.st2067_2_2016.PKL.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2016("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas.st2067_2_2016.PKL.UserText creator = PackingListBuilder.buildPKLUserTextType_2016("Netflix", "en");
        XMLGregorianCalendar issueDate = IMFUtils.createXMLGregorianCalendar();
        org.smpte_ra.schemas.st2067_2_2016.PKL.UserText issuer = PackingListBuilder.buildPKLUserTextType_2016("Netflix", "en");
        new PackingListBuilder(packingList.getUUID(), issueDate, tempDir, packingListBuilderErrorLogger).buildPackingList_2016(annotationText, issuer, creator, packingListBuilderAssets);

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()))));
        }

        File pklOutputFile = null;
        for(File file : tempDir.listFiles()){
            if(file.getName().contains("PKL-")){
                pklOutputFile = file;
            }
        }
        if(pklOutputFile == null){
            throw new IMFAuthoringException(String.format("PackingList file does not exist in the working directory %s, cannot generate the rest of the documents", tempDir.getAbsolutePath()));
        }
        Assert.assertTrue(pklOutputFile.length() > 0);

        resourceByteRangeProvider = new FileByteRangeProvider(pklOutputFile);
        List<ErrorLogger.ErrorObject> pklValidationErrors = IMPValidator.validatePKL(new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, pklOutputFile.length()-1), PayloadRecord.PayloadAssetType.PackingList, 0L, 0L));
        Assert.assertTrue(pklValidationErrors.size() == 0);

        //Destroy the temporary working directory
        tempDir.delete();
    }
}
