package com.netflix.imflibrary.app;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = "unit")
public class MXFEssenceReaderTest
{
    @Test
    public void MXFAudioEssenceReaderTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        MXFEssenceReader mxfEssenceReader = new MXFEssenceReader(workingDirectory, resourceByteRangeProvider);
        Assert.assertTrue(mxfEssenceReader.toString().length() > 0);
        Assert.assertEquals(mxfEssenceReader.getPartitionPacks().size(), 4);
        Assert.assertEquals(mxfEssenceReader.getEssenceTypes().size(), 1);
        Assert.assertEquals(mxfEssenceReader.getEssenceTypes().get(0), HeaderPartition.EssenceTypeEnum.MainAudioEssence);
        Assert.assertEquals(mxfEssenceReader.getEssenceDescriptors().size(), 1);
        Assert.assertEquals(mxfEssenceReader.getEssenceDescriptorsDOMNodes().size(), 1);
    }

    @Test(expectedExceptions = MXFException.class, expectedExceptionsMessageRegExp = "randomIndexPackSize = .*")
    public void badRandomIndexPackLength() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf");
        File workingDirectory = Files.createTempDirectory(null).toFile();
        ResourceByteRangeProvider resourceByteRangeProvider = mock(ResourceByteRangeProvider.class);
        when(resourceByteRangeProvider.getResourceSize()).thenReturn(16L);
        when(resourceByteRangeProvider.getByteRange(anyLong(), anyLong(), any(File.class))).thenReturn(inputFile);
        MXFEssenceReader mxfEssenceReader = new MXFEssenceReader(workingDirectory, resourceByteRangeProvider);
        mxfEssenceReader.getRandomIndexPack();
    }
}
