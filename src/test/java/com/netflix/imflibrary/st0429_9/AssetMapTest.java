package com.netflix.imflibrary.st0429_9;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.smpte_ra.schemas._429_9._2007.am.AssetMapType;
import org.smpte_ra.schemas._429_9._2007.am.AssetType;
import org.smpte_ra.schemas._429_9._2007.am.ChunkType;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;

@Test(groups = "unit")
public class AssetMapTest
{
    @Test
    public void testAssetMapTypeConformanceBad1()
    {
        AssetMapType assetMapType = new AssetMapType();
        //volume count is not 1
        assetMapType.setVolumeCount(new BigInteger("2"));
        List errors = AssetMap.checkConformance(assetMapType);
        Assert.assertEquals(errors.size(), 2);
    }

    @Test
    public void testAssetMapTypeConformanceBad2()
    {
        ChunkType chunkType = new ChunkType();
        AssetType.ChunkList chunkList = new AssetType.ChunkList();
        //chunklist has two chunks
        chunkList.getChunk().add(chunkType);
        chunkList.getChunk().add(chunkType);
        AssetType assetType = new AssetType();
        assetType.setChunkList(chunkList);
        AssetMapType.AssetList assetList = new AssetMapType.AssetList();
        assetList.getAsset().add(assetType);
        AssetMapType assetMapType = new AssetMapType();
        assetMapType.setAssetList(assetList);
        assetMapType.setVolumeCount(new BigInteger("1"));


        List errors = AssetMap.checkConformance(assetMapType);
        Assert.assertEquals(errors.size(), 1);

    }

    @Test
    public void testAssetMapTypeConformanceBad3()
    {
        ChunkType chunkType = new ChunkType();
        //VolumeIndex in <Chunk> is not 1
        chunkType.setVolumeIndex(new BigInteger("2"));
        AssetType.ChunkList chunkList = new AssetType.ChunkList();
        chunkList.getChunk().add(chunkType);
        AssetType assetType = new AssetType();
        assetType.setChunkList(chunkList);
        AssetMapType.AssetList assetList = new AssetMapType.AssetList();
        assetList.getAsset().add(assetType);
        AssetMapType assetMapType = new AssetMapType();
        assetMapType.setAssetList(assetList);
        assetMapType.setVolumeCount(new BigInteger("1"));


        List errors = AssetMap.checkConformance(assetMapType);
        Assert.assertEquals(errors.size(), 1);
    }

    @Test
    public void testAssetMapTypeConformanceBad4()
    {
        ChunkType chunkType = new ChunkType();
        chunkType.setVolumeIndex(new BigInteger("1"));
        //Offset in <Chunk> is not 0
        chunkType.setOffset(new BigInteger("1"));
        AssetType.ChunkList chunkList = new AssetType.ChunkList();
        chunkList.getChunk().add(chunkType);
        AssetType assetType = new AssetType();
        assetType.setChunkList(chunkList);
        AssetMapType.AssetList assetList = new AssetMapType.AssetList();
        assetList.getAsset().add(assetType);
        AssetMapType assetMapType = new AssetMapType();
        assetMapType.setAssetList(assetList);
        assetMapType.setVolumeCount(new BigInteger("1"));


        List errors = AssetMap.checkConformance(assetMapType);
        Assert.assertEquals(errors.size(), 1);
    }

    @Test
    public void testAssetMapSchemaError() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath
                ("ASSETMAP_schemaViolation.xml");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);

        try {
            AssetMap assetMap = new AssetMap(resourceByteRangeProvider);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertEquals(ex.getClass(), IMFException.class);
        }
    }

    @Test
    public void testAssetMapUnsupportedNamespaceError() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath
                ("ASSETMAP_unsupportedNamespace.xml");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);

        try {
            AssetMap assetMap = new AssetMap(resourceByteRangeProvider);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertEquals(ex.getClass(), IMFException.class);
        }
    }
}
