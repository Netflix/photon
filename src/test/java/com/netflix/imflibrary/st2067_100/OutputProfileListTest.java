package com.netflix.imflibrary.st2067_100;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_100.macro.preset.PresetMacro;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.nio.file.Path;

@Test(groups = "unit")
public class OutputProfileListTest
{
    @Test
    public void testSimpleOutputProfileList() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/OPL/OPL_8cf83c32-4949-4f00-b081-01e12b18932f_simple.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        OutputProfileList outputProfileList = OutputProfileList.getOutputProfileListType(new FileByteRangeProvider(inputFile), imfErrorLogger);

        Assert.assertEquals(outputProfileList.getCompositionPlaylistId().toString(), "0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85");
        Assert.assertEquals(outputProfileList.getAnnotation().toString(), "OPL Example");
        Assert.assertEquals(outputProfileList.getId().toString(), "8cf83c32-4949-4f00-b081-01e12b18932f");
        Assert.assertEquals(((PresetMacro)outputProfileList.getMacroMap().entrySet().iterator().next().getValue()).getPreset(), "playback_cpl");
        Assert.assertEquals(outputProfileList.getErrors().size(), 0);

        Assert.assertEquals(outputProfileList.getMacroMap().size(), 1);
    }

    @Test
    public void testOutputProfileList() throws Exception
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/OPL/OPL_8cf83c32-4949-4f00-b081-01e12b18932f.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        OutputProfileList outputProfileList = OutputProfileList.getOutputProfileListType(new FileByteRangeProvider(inputFile), imfErrorLogger);

        Assert.assertEquals(outputProfileList.getCompositionPlaylistId().toString(), "0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85");
        Assert.assertEquals(outputProfileList.getAliasMap().size(), 3);
        Assert.assertEquals(outputProfileList.getAnnotation().toString(), "OPL Example");
        Assert.assertEquals(outputProfileList.getId().toString(), "8cf83c32-4949-4f00-b081-01e12b18932f");
        Assert.assertEquals(outputProfileList.getErrors().size(), 0);

        Assert.assertEquals(outputProfileList.getMacroMap().size(), 5);
    }

    @Test
    public void testOutputProfileListOnCOmposition() throws Exception
    {
        Path inputFileCPL = TestHelper.findResourceByPath("TestIMP/OPL/CPL_0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85.xml");
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFileCPL);

        Path inputFileOPL = TestHelper.findResourceByPath("TestIMP/OPL/OPL_8cf83c32-4949-4f00-b081-01e12b18932f.xml");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        OutputProfileList outputProfileList = OutputProfileList.getOutputProfileListType(new FileByteRangeProvider(inputFileOPL), imfErrorLogger);
        outputProfileList.applyOutputProfileOnComposition(imfCompositionPlaylist);

        Assert.assertEquals(outputProfileList.getCompositionPlaylistId().toString(), "0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85");
        Assert.assertEquals(outputProfileList.getAliasMap().size(), 3);
        Assert.assertEquals(outputProfileList.getAnnotation().toString(), "OPL Example");
        Assert.assertEquals(outputProfileList.getId().toString(), "8cf83c32-4949-4f00-b081-01e12b18932f");

        Assert.assertEquals(outputProfileList.getMacroMap().size(), 5);

        Assert.assertEquals(outputProfileList.getHandleMapWithApplicationComposition(imfCompositionPlaylist, imfErrorLogger).size(), 28);
        Assert.assertEquals(outputProfileList.getErrors().size(), 0);
    }
}
