package com.netflix.imflibrary.st0429_9;

import com.netflix.imflibrary.exceptions.IMFException;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetMapType;
import org.smpte_ra.schemas.st0429_9_2007.AM.AssetType;
import org.smpte_ra.schemas.st0429_9_2007.AM.ChunkType;
import org.testng.annotations.Test;

import java.math.BigInteger;

@Test(groups = "unit")
public class AssetMapTest
{
    @Test(expectedExceptions = IMFException.class)
    public void testAssetMapTypeConformanceBad1()
    {
        AssetMapType assetMapType = new AssetMapType();
        //volume count is not 1
        assetMapType.setVolumeCount(new BigInteger("2"));
        AssetMap.checkConformance(assetMapType);
    }

    @Test(expectedExceptions = IMFException.class)
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


        AssetMap.checkConformance(assetMapType);
    }

    @Test(expectedExceptions = IMFException.class)
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


        AssetMap.checkConformance(assetMapType);
    }

    @Test(expectedExceptions = IMFException.class)
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


        AssetMap.checkConformance(assetMapType);
    }
}
