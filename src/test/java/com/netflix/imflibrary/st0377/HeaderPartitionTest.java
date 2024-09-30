/*
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
 */

package com.netflix.imflibrary.st0377;

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.header.*;
import com.netflix.imflibrary.st2067_2.AudioContentKind;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HeaderPartitionTest
{
    @Test
    public void audioHeaderPartitionTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf.hdr");
        byte[] bytes = Files.readAllBytes(Paths.get(inputFile.toURI()));
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, inputFile.length(), imfErrorLogger);
        Assert.assertTrue(headerPartition.toString().length() > 0);

        Preface preface = headerPartition.getPreface();
        Assert.assertTrue(preface.toString().length() > 0);

        ContentStorage contentStorage = preface.getContentStorage();
        Assert.assertEquals(contentStorage.getGenericPackageList().size(), 2);
        Assert.assertEquals(contentStorage.getPackageInstanceUIDs().size(), 2);



        List<InterchangeObject> materialPackages = headerPartition.getMaterialPackages();
        Assert.assertEquals(materialPackages.size(), 1);
        MaterialPackage materialPackage = (MaterialPackage)materialPackages.get(0);
        Assert.assertTrue(materialPackage.toString().length() > 0);
        MXFUID materialPackageInstanceUID = new MXFUID(new byte[]{
                0x6a, (byte)0x92, (byte)0xc9, 0x01, 0x13, 0x59, 0x48, 0x42, (byte)0x86, 0x38, (byte)0xdf, (byte)0xa9, 0x3a, 0x08, (byte)0x80, 0x20});
        Assert.assertEquals(materialPackage.getInstanceUID(), materialPackageInstanceUID);
        Assert.assertSame(materialPackage, headerPartition.getMaterialPackage(materialPackageInstanceUID));

        List<GenericTrack> genericTracks = materialPackage.getGenericTracks();
        Assert.assertEquals(genericTracks.size(), 2);
        List<MXFUID> trackInstanceUIDs = materialPackage.getTrackInstanceUIDs();
        Assert.assertEquals(trackInstanceUIDs.size(), 2);
        MXFUID timeCodeTrackInstanceUID = new MXFUID(new byte[]{
                0x33, (byte)0xb6, (byte)0xc7, 0x6c, 0x1f, 0x7a, 0x45, (byte)0xf0, (byte)0x88, 0x46, 0x65, 0x7d, (byte)0xd4, (byte)0x96, 0x08, 0x01});
        Assert.assertEquals(trackInstanceUIDs.get(0), timeCodeTrackInstanceUID);
        MXFUID soundTrackInstanceUID = new MXFUID(new byte[]{
                0x5c, (byte)0xab, 0x51, (byte)0x93, (byte)0x96, (byte)0x8b, 0x4e, 0x13, (byte)0x8f, (byte)0xdb, 0x0b, (byte)0x83, (byte)0x81, 0x0a, (byte)0xc7, (byte)0x97});
        Assert.assertEquals(trackInstanceUIDs.get(1), soundTrackInstanceUID);
        List<TimelineTrack> timelineTracks = materialPackage.getTimelineTracks();
        Assert.assertEquals(timelineTracks.size(), 2);
        TimelineTrack timeCodeTrack = timelineTracks.get(0);
        Assert.assertEquals(timeCodeTrack.getInstanceUID(), timeCodeTrackInstanceUID);
        Assert.assertEquals(timeCodeTrack.getEditRateNumerator(), 24000L);
        Assert.assertEquals(timeCodeTrack.getEditRateDenominator(), 1001L);
        Sequence sequence = timelineTracks.get(1).getSequence();
        Assert.assertEquals(sequence.getInstanceUID(), timelineTracks.get(1).getSequenceUID());
        Assert.assertEquals(sequence.getNumberOfStructuralComponents(), 1);
        Assert.assertEquals(sequence.getSourceClips().size(), 1);
        SourceClip sourceClip = sequence.getSourceClips().get(0);
        Assert.assertEquals(sourceClip.getInstanceUID(), sequence.getSourceClipUID(0));
        Assert.assertEquals((long)sourceClip.getDuration(), 35232L);





        List<InterchangeObject> sourcePackages = headerPartition.getSourcePackages();
        Assert.assertEquals(sourcePackages.size(), 1);
        SourcePackage sourcePackage = (SourcePackage)sourcePackages.get(0);
        Assert.assertTrue(sourcePackage.toString().length() > 0);
        MXFUID sourcePackageInstanceUid = new MXFUID(new byte[]{
                0x54, 0x0b, 0x00, (byte)0xf2, (byte)0xb9, (byte)0x8e, 0x43, 0x7a, (byte)0x8f, (byte)0xa0, 0x3c, (byte)0xe1, (byte)0xa6, 0x1f, 0x25, (byte)0xf9
        });
        Assert.assertEquals(sourcePackage.getInstanceUID(), sourcePackageInstanceUid);
        Assert.assertSame(sourcePackage, headerPartition.getSourcePackage(sourcePackageInstanceUid));
        Assert.assertEquals(sourcePackage.getPackageUID(), new MXFUID(new byte[]{
                0x06, 0x0a, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x01, 0x0f, 0x20, 0x13, 0x00, 0x00, 0x00, (byte)0xcf, (byte)0xbc, (byte)0xf3, (byte)0xb9, 0x62, 0x50, 0x46, 0x7c, (byte)0xbd, 0x18, (byte)0x9f, 0x5d, (byte)0xe0, (byte)0xdf, (byte)0x9f, (byte)0xfb
        }));
        trackInstanceUIDs = sourcePackage.getTrackInstanceUIDs();
        Assert.assertEquals(trackInstanceUIDs.size(), 2);
        Assert.assertEquals(trackInstanceUIDs.get(0), new MXFUID(new byte[]{
                0x51, 0x0f, (byte)0x96, (byte)0xd1, (byte)0xa4, (byte)0xa4, 0x4a, (byte)0xd9, (byte)0xa9, 0x36, (byte)0xdd, 0x60, (byte)0xd3, 0x1a, (byte)0xc9, 0x2f
        }));
        Assert.assertEquals(trackInstanceUIDs.get(1), new MXFUID(new byte[]{
                0x2b, 0x71, 0x39, (byte)0xcc, 0x16, 0x3c, 0x43, (byte)0xa5, (byte)0x9d, (byte)0xc4, (byte)0xb3, 0x64, (byte)0xb6, (byte)0xa1, (byte)0xeb, (byte)0x93
        }));



        Assert.assertTrue(headerPartition.hasWaveAudioEssenceDescriptor());
        Assert.assertEquals(headerPartition.getWaveAudioEssenceDescriptors().size(), 1);
        Assert.assertTrue(headerPartition.getEssenceTypes().size() == 1);
        Assert.assertTrue(headerPartition.getEssenceTypes().get(0) == HeaderPartition.EssenceTypeEnum.MainAudioEssence);
        WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = (WaveAudioEssenceDescriptor)headerPartition.getWaveAudioEssenceDescriptors().get(0);
        Assert.assertTrue(waveAudioEssenceDescriptor.toString().length() > 0);
        Assert.assertTrue(waveAudioEssenceDescriptor.equals(waveAudioEssenceDescriptor));
        Assert.assertEquals(waveAudioEssenceDescriptor.getAudioSamplingRateNumerator(), 48000);
        Assert.assertEquals(waveAudioEssenceDescriptor.getAudioSamplingRateDenominator(), 1);
        Assert.assertEquals(waveAudioEssenceDescriptor.getChannelCount(), 2);
        Assert.assertEquals(waveAudioEssenceDescriptor.getQuantizationBits(), 24);
        Assert.assertEquals(waveAudioEssenceDescriptor.getBlockAlign(), 6);

        Assert.assertFalse(headerPartition.hasAudioChannelLabelSubDescriptors());
        Assert.assertFalse(headerPartition.hasSoundFieldGroupLabelSubDescriptor());
        Assert.assertFalse(headerPartition.hasCDCIPictureEssenceDescriptor());
        Assert.assertFalse(headerPartition.hasRGBAPictureEssenceDescriptor());
        Assert.assertEquals(headerPartition.getAudioContentKind(), AudioContentKind.Unknown);

        MXFUID essenceContainerDataInstanceUID = new MXFUID(new byte[]{
                0x46, 0x31, (byte)0x9f, 0x11, (byte)0xc9, (byte)0xb1, 0x40, (byte)0xa5, (byte)0xb9, (byte)0xb1, (byte)0x9e, 0x69, (byte)0xda, (byte)0x89, (byte)0xbe, 0x52
        });
        Assert.assertEquals(headerPartition.getPreface().getContentStorage().getEssenceContainerDataList().size(), 1);
        EssenceContainerData essenceContainerData = headerPartition.getPreface().getContentStorage().getEssenceContainerDataList().get(0);
        Assert.assertSame(essenceContainerData, headerPartition.getEssenceContainerData(essenceContainerDataInstanceUID));

        PartitionPack partitionPack = headerPartition.getPartitionPack();
        Assert.assertFalse(partitionPack.isBodyPartition());
        Assert.assertFalse(partitionPack.isFooterPartition());
        Assert.assertFalse(partitionPack.isValidFooterPartition());
        Assert.assertEquals(partitionPack.getHeaderByteCount(), 11744L);
        Assert.assertEquals(partitionPack.getIndexByteCount(), 0L);
        Assert.assertEquals(partitionPack.getPartitionDataByteOffset(), 124L);

    }

    @Test(expectedExceptions = MXFException.class, expectedExceptionsMessageRegExp = "This partition does not contain essence data")
    public void partitionPackWithNoEssenceDataTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf.hdr");
        byte[] bytes = Files.readAllBytes(Paths.get(inputFile.toURI()));
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, inputFile.length(), imfErrorLogger);
        PartitionPack partitionPack = headerPartition.getPartitionPack();
        partitionPack.getEssenceStreamSegmentStartStreamPosition();
    }

    @Test
    public void videoHeaderPartitionTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("CHIMERA_NETFLIX_2398.mxf.hdr");
        byte[] bytes = Files.readAllBytes(Paths.get(inputFile.toURI()));
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, inputFile.length(), imfErrorLogger);
        Assert.assertTrue(headerPartition.toString().length() > 0);
        Assert.assertFalse(headerPartition.hasRGBAPictureEssenceDescriptor());
        Assert.assertTrue(headerPartition.hasCDCIPictureEssenceDescriptor());
        Assert.assertEquals(headerPartition.getImageColorModel(), Colorimetry.ColorModel.YUV);
        Assert.assertEquals(headerPartition.getImageCodingEquation(), Colorimetry.CodingEquation.ITU709);
        Assert.assertEquals(headerPartition.getImageColorPrimaries(), Colorimetry.ColorPrimaries.Unknown);
        Assert.assertEquals(headerPartition.getImageTransferCharacteristic(), Colorimetry.TransferCharacteristic.ITU709);
        Assert.assertEquals(headerPartition.getImageQuantization(), Colorimetry.Quantization.QE2);
        Assert.assertEquals(headerPartition.getImageSampling(), Colorimetry.Sampling.Sampling422);
        Assert.assertEquals(headerPartition.getImagePixelBitDepth().intValue(), 10);


        Assert.assertTrue(headerPartition.getEssenceTypes().size() == 1);
        Assert.assertTrue(headerPartition.getEssenceTypes().get(0) == HeaderPartition.EssenceTypeEnum.MainImageEssence);
    }

    @Test
    public void videoHeaderPartitionTest2() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_00.mxf");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        HeaderPartition headerPartition = HeaderPartition.fromFile(inputFile, imfErrorLogger);
        Assert.assertTrue(headerPartition.toString().length() > 0);
        Assert.assertTrue(headerPartition.hasRGBAPictureEssenceDescriptor());
        Assert.assertFalse(headerPartition.hasCDCIPictureEssenceDescriptor());
        Assert.assertEquals(headerPartition.getImageColorModel(), Colorimetry.ColorModel.RGB);
        Assert.assertEquals(headerPartition.getImageCodingEquation(), Colorimetry.CodingEquation.Unknown);
        Assert.assertEquals(headerPartition.getImageColorPrimaries(), Colorimetry.ColorPrimaries.ITU2020);
        Assert.assertEquals(headerPartition.getImageTransferCharacteristic(), Colorimetry.TransferCharacteristic.SMPTEST2084);
        Assert.assertEquals(headerPartition.getImageQuantization(), Colorimetry.Quantization.QE2);
        Assert.assertEquals(headerPartition.getImageSampling(), Colorimetry.Sampling.Sampling444);
        Assert.assertEquals(headerPartition.getImagePixelBitDepth().intValue(), 12);


        Assert.assertTrue(headerPartition.getEssenceTypes().size() == 1);
        Assert.assertTrue(headerPartition.getEssenceTypes().get(0) == HeaderPartition.EssenceTypeEnum.MainImageEssence);
    }

    @Test
    public void videoHeaderPartitionTest3() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/HT/IMP/VIDEO_6ed567b7-c030-46d6-9c1c-0f09bab4b962.mxf");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        HeaderPartition headerPartition = HeaderPartition.fromFile(inputFile, imfErrorLogger);
        Assert.assertTrue(headerPartition.toString().length() > 0);
        Assert.assertTrue(headerPartition.hasRGBAPictureEssenceDescriptor());
        GenericPictureEssenceDescriptor pictureEssenceDescriptor = ((GenericPictureEssenceDescriptor)((SourcePackage) headerPartition.getSourcePackages().get(0)).getGenericDescriptor());
        GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO descriptorBO = (GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO) TestHelper.getValue(pictureEssenceDescriptor, "rgbaPictureEssenceDescriptorBO");
        InterchangeObject.InterchangeObjectBO jpeg2000SubDescriptor = headerPartition.getSubDescriptors(descriptorBO).get(0);
        JPEG2000PictureSubDescriptor.JPEG2000PictureSubDescriptorBO jpeg2000PictureSubDescriptorBO = (JPEG2000PictureSubDescriptor.JPEG2000PictureSubDescriptorBO) jpeg2000SubDescriptor;
        J2KExtendedCapabilities j2KExtendedCapabilities = (J2KExtendedCapabilities) TestHelper.getValue(jpeg2000PictureSubDescriptorBO, "j2k_extended_capabilities");
        Integer pCap = (Integer) TestHelper.getValue(j2KExtendedCapabilities, "pCap");
        List<Short> cCap = ((CompoundDataTypes.MXFCollections.MXFCollection<Short>) TestHelper.getValue(j2KExtendedCapabilities, "cCap")).getEntries();

        Assert.assertEquals(pCap, 131072);
        Assert.assertEquals(cCap.size(), 1);
        Assert.assertTrue(cCap.get(0) == 38);
    }

    @Test
    public void audioHeaderPartitionTest2() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("NMPC_6000ms_6Ch_ch_id.mxf.hdr");
        byte[] bytes = Files.readAllBytes(Paths.get(inputFile.toURI()));
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        HeaderPartition headerPartition = new HeaderPartition(byteProvider, 0L, inputFile.length(), imfErrorLogger);
        Assert.assertTrue(headerPartition.toString().length() > 0);

        Assert.assertTrue(headerPartition.hasSoundFieldGroupLabelSubDescriptor());
        Assert.assertEquals(headerPartition.getSoundFieldGroupLabelSubDescriptors().size(), 1);
        SoundFieldGroupLabelSubDescriptor soundFieldGroupLabelSubDescriptor = (SoundFieldGroupLabelSubDescriptor)headerPartition.getSoundFieldGroupLabelSubDescriptors().get(0);
        Assert.assertEquals(soundFieldGroupLabelSubDescriptor.getMCALabelDictionaryId(), new MXFUID(new byte[]{
                0x06, 0x0e, 0x2b, 0x34, 0x04, 0x01, 0x01, 0x0d, 0x03, 0x02, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00
        }));
        MXFUID soundFieldGroupMCALinkId = new MXFUID(new byte[]{
                (byte)0xe5, (byte)0x84, 0x07, 0x22, 0x47, (byte)0xb8, 0x45, 0x13, (byte)0xa0, (byte)0xb0, 0x2e, (byte)0xee, (byte)0xde, 0x20, (byte)0x92, (byte)0xfe
        });
        Assert.assertEquals(soundFieldGroupLabelSubDescriptor.getMCALinkId(), soundFieldGroupMCALinkId);

        Assert.assertTrue(headerPartition.hasAudioChannelLabelSubDescriptors());
        Assert.assertEquals(headerPartition.getAudioChannelLabelSubDescriptors().size(), 6);
        if(headerPartition.getAudioChannelLabelSubDescriptors().size() == 0){
            throw new MXFException(String.format("Asset seems to be invalid since it does not contain any AudioChannelLabelSubDescriptors"));
        }
        Assert.assertEquals(headerPartition.getAudioContentKind(), AudioContentKind.Unknown);

        AudioChannelLabelSubDescriptor audioChannelLabelSubDescriptor = (AudioChannelLabelSubDescriptor)headerPartition.getAudioChannelLabelSubDescriptors().get(0);
        Assert.assertEquals(audioChannelLabelSubDescriptor.getSoundfieldGroupLinkId(), soundFieldGroupMCALinkId);
        Assert.assertEquals(audioChannelLabelSubDescriptor.getMCALabelDictionaryId(), new MXFUID(new byte[]{
                0x06, 0x0e, 0x2b, 0x34, 0x04, 0x01, 0x01, 0x0d, 0x03, 0x02, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00
        }));
        Assert.assertEquals(audioChannelLabelSubDescriptor.getMCALinkId(), new MXFUID(new byte[]{
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    }));
    }

    @Test
    public void descriptiveMetadataHeaderPartitionTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/IAB/MXF/meridian_2398_IAB_5f.mxf");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        HeaderPartition headerPartition = HeaderPartition.fromFile(inputFile, imfErrorLogger);
        Assert.assertEquals(headerPartition.getGenericStreamIdFromGenericStreamTextBaseSetDescription("http://www.dolby.com/schemas/2018/DbmdWrapper"), 3);
    }
}
