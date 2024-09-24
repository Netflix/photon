package com.netflix.imflibrary.app;

import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzePackage;

@Test(groups = "unit")
public class IMPAnalyzerTest
{
    @Test
    public void IMPAnalyzerTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTestPHDR() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/PHDR/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 6);
        errorMap.entrySet().stream().forEach( e ->
                {
                    Assert.assertEquals(e.getValue().size(), 0); // not expecting any errors or warnings
                }
        );

    }

    @Test
    public void IMPAnalyzerTestIDMismatches() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006_ID_MISMATCH/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL.*Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    }
                    else if (e.getKey().matches("ASSETMAP.*")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                        e.getValue().get(0).getErrorDescription().contains("ERROR-AssetMap references PKL with ID f5e93462-aed2-44ad-a4ba-2adb65823e7d, but PKL contains ID f5e93462-aed2-44ad-a4ba-2adb65823e7c");
                    }
                    else if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                        e.getValue().get(0).getErrorDescription().contains("ERROR-UUID 0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca84 in the CPL is not same as UUID 0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85 of the CPL in the AssetMap");
                    }
                    else if (e.getKey().matches("OPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                        e.getValue().get(0).getErrorDescription().contains("ERROR-UUID 8cf83c32-4949-4f00-b081-01e12b18932e in the OPL is not same as UUID 8cf83c32-4949-4f00-b081-01e12b18932f of the OPL in the AssetMap");
                        e.getValue().get(1).getErrorDescription().contains("Failed to get application composition with ID = 0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85");
                    } else if (e.getKey().matches("MERIDIAN_Netflix_Photon_161006_00.mxf.*")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                        e.getValue().get(0).getErrorDescription().contains("ERROR-UUID 61d91654-2650-4abf-abbc-ad2c7f640bf8 in the MXF file is not same as UUID 61d91654-2650-4abf-abbc-ad2c7f640bf9 of the MXF file in the AssetMap");
                    }
                }
        );

    }
}
