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
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test(groups = "functional")
public class PackingListBuilderFunctionalTest {

    @Test
    public void packingListBuilder_2007_Test() throws IOException, SAXException, JAXBException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/NYCbCrLT_3840x2160x23.98x10min/PKL_0429fedd-b55d-442a-aa26-2a81ec71ed05.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        PackingList packingList = new PackingList(resourceByteRangeProvider);
        List<PackingList.Asset> assets = packingList.getAssets();

        List<PackingListBuilder.PackingListBuilderAsset_2007> packingListBuilderAssets = new ArrayList<>();
        for(PackingList.Asset asset : assets){
            org.smpte_ra.schemas._429_8._2007.pkl.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2007("Netflix", "en");
            org.smpte_ra.schemas._429_8._2007.pkl.UserText originalFileName = PackingListBuilder.buildPKLUserTextType_2007(asset.getOriginalFilename(), "en");
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
        Path tempPath = Files.createTempDirectory("IMFDocuments");

        IMFErrorLogger packingListBuilderErrorLogger = new IMFErrorLoggerImpl();
        org.smpte_ra.schemas._429_8._2007.pkl.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2007("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas._429_8._2007.pkl.UserText creator = PackingListBuilder.buildPKLUserTextType_2007("Netflix", "en");
        XMLGregorianCalendar issueDate = IMFUtils.createXMLGregorianCalendar();
        org.smpte_ra.schemas._429_8._2007.pkl.UserText issuer = PackingListBuilder.buildPKLUserTextType_2007("Netflix", "en");
        new PackingListBuilder(packingList.getUUID(), issueDate, tempPath, packingListBuilderErrorLogger).buildPackingList_2007(annotationText, issuer, creator, packingListBuilderAssets);

        imfErrorLogger.addAllErrors(packingList.getErrors());
        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()))));
        }

        Path pklOutputFile = null;
        Stream<Path> filesStream = Files.list(tempPath);
        List<Path> filesList = filesStream.collect(Collectors.toList());
        for (Path path : filesList) {
            if(path.getFileName().toString().contains("PKL-")){
                pklOutputFile = path;
            }
        }

        if(pklOutputFile == null){
            throw new IMFAuthoringException(String.format("PackingList path does not exist in the working directory %s, cannot generate the rest of the documents", tempPath.toString()));
        }
        Assert.assertTrue(Files.size(pklOutputFile) > 0);

        resourceByteRangeProvider = new FileByteRangeProvider(pklOutputFile);
        List<ErrorLogger.ErrorObject> errors = IMPValidator.validatePKL(new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, Files.size(pklOutputFile)-1), PayloadRecord.PayloadAssetType.PackingList, 0L, 0L));
        Assert.assertEquals(errors.size(), 0);

        //Destroy the temporary working directory
        Utilities.recursivelyDeleteFolder(tempPath);
    }

    @Test
    public void packingListBuilder_2016_Test() throws IOException, SAXException, JAXBException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/PKL_befcd2d4-f35c-45d7-99bb-7f64b51b103c_corrected.xml");
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        PackingList packingList = new PackingList(resourceByteRangeProvider);
        imfErrorLogger.addAllErrors(packingList.getErrors());

        List<PackingList.Asset> assets = packingList.getAssets();

        List<PackingListBuilder.PackingListBuilderAsset_2016> packingListBuilderAssets = new ArrayList<>();
        for(PackingList.Asset asset : assets){
            org.smpte_ra.schemas._2067_2._2016.pkl.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2016("Netflix", "en");
            org.smpte_ra.schemas._2067_2._2016.pkl.UserText originalFileName = PackingListBuilder.buildPKLUserTextType_2016(asset.getOriginalFilename(), "en");
            org.w3._2000._09.xmldsig_.DigestMethodType hashAlgorithm = new org.w3._2000._09.xmldsig_.DigestMethodType();
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
        Path tempPath = Files.createTempDirectory("IMFDocuments");

        IMFErrorLogger packingListBuilderErrorLogger = new IMFErrorLoggerImpl();
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText annotationText = PackingListBuilder.buildPKLUserTextType_2016("Photon PackingListBuilder", "en");
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText creator = PackingListBuilder.buildPKLUserTextType_2016("Netflix", "en");
        XMLGregorianCalendar issueDate = IMFUtils.createXMLGregorianCalendar();
        org.smpte_ra.schemas._2067_2._2016.pkl.UserText issuer = PackingListBuilder.buildPKLUserTextType_2016("Netflix", "en");
        new PackingListBuilder(packingList.getUUID(), issueDate, tempPath, packingListBuilderErrorLogger).buildPackingList_2016(annotationText, issuer, creator, packingListBuilderAssets);

        if(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()).size() > 0){
            throw new IMFAuthoringException(String.format("Fatal errors occurred while generating the PackingList. Please see following error messages %s", Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, 0, imfErrorLogger.getNumberOfErrors()))));
        }

        Path pklOutputFile = null;
        Stream<Path> filesStream = Files.list(tempPath);
        List<Path> filesList = filesStream.collect(Collectors.toList());
        for (Path path : filesList) {
            if(path.getFileName().toString().contains("PKL-")){
                pklOutputFile = path;
            }
        }

        if(pklOutputFile == null){
            throw new IMFAuthoringException(String.format("PackingList path does not exist in the working directory %s, cannot generate the rest of the documents", tempPath.toString()));
        }
        Assert.assertTrue(Files.size(pklOutputFile) > 0);

        resourceByteRangeProvider = new FileByteRangeProvider(pklOutputFile);
        List<ErrorLogger.ErrorObject> pklValidationErrors = IMPValidator.validatePKL(new PayloadRecord(resourceByteRangeProvider.getByteRangeAsBytes(0, Files.size(pklOutputFile)-1), PayloadRecord.PayloadAssetType.PackingList, 0L, 0L));
        Assert.assertTrue(pklValidationErrors.size() == 0);

        //Destroy the temporary working directory
        Utilities.recursivelyDeleteFolder(tempPath);
    }
}
