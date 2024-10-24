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
import static com.netflix.imflibrary.app.IMPAnalyzer.analyzeFile;

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
    public void IMPAnalyzerTestTimedText() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/TimedTextImageAndTextProfile/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 8);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTestMissingFilesAndAssetMapEntries() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/MissingFilesAndAssetMapEntries/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 4);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("PKL.*")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                        Assert.assertTrue(e.getValue().get(0).getErrorDescription().contains("Failed to get path for Asset with ID = bc1a3912-a90a-41b2-b16c-0915a2a1e088"));
                        Assert.assertTrue(e.getValue().get(1).getErrorDescription().contains("Cannot find asset with path"));
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
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
                        Assert.assertTrue(e.getValue().get(0).getErrorDescription().contains("AssetMap references PKL with ID f5e93462-aed2-44ad-a4ba-2adb65823e7d, but PKL contains ID f5e93462-aed2-44ad-a4ba-2adb65823e7c"));
                    }
                    else if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                        Assert.assertTrue(e.getValue().get(0).getErrorDescription().contains("UUID 0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca84 in the CPL is not same as UUID 0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85 of the CPL in the AssetMap"));
                    }
                    else if (e.getKey().matches("OPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                        Assert.assertTrue(e.getValue().get(0).getErrorDescription().contains("UUID 8cf83c32-4949-4f00-b081-01e12b18932e in the OPL is not same as UUID 8cf83c32-4949-4f00-b081-01e12b18932f of the OPL in the AssetMap"));
                        Assert.assertTrue(e.getValue().get(1).getErrorDescription().contains("Failed to get application composition with ID = 0eb3d1b9-b77b-4d3f-bbe5-7c69b15dca85"));
                    } else if (e.getKey().matches("MERIDIAN_Netflix_Photon_161006_00.mxf.*")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                        Assert.assertTrue(e.getValue().get(0).getErrorDescription().contains("UUID 61d91654-2650-4abf-abbc-ad2c7f640bf8 in the MXF file is not same as UUID 61d91654-2650-4abf-abbc-ad2c7f640bf9 of the MXF file in the AssetMap"));
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTestMimeTypeErrors() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/WrongXmlMimeTypes/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 2);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("PKL.*")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                        Assert.assertTrue(e.getValue().get(0).getErrorDescription().contains("Packing List does not contain any assets of type"));
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );
    }

    @Test
    public void IMPAnalyzerAnalyzeCPLValid() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/PHDR/CPL_3aa56098-7709-4673-9266-7f0c70ba10d8.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 0);

    }

    @Test
    public void IMPAnalyzerAnalyzeCPLError() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application2Extended/CPL_BLACKL_202_1080p_REC709_178_ENG_fe8cf2f4-1bcd-4145-8f72-6775af4038c4_JPEG2000SubDescriptorError.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 4);
    }

    @Test
    public void IMPAnalyzerAnalyzePKLValid() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/PHDR/PKL_913ef906-893f-4851-8664-2d053bd2ec95.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 0);

    }

    @Test
    public void IMPAnalyzerAnalyzePKLError() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/BadXML/PKL_d54a68ed-332e-4ddd-b163-c0317abb1e52.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 2);
    }

    @Test
    public void IMPAnalyzerAnalyzeAssetMapValid() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/PHDR/ASSETMAP.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 0);

    }

    @Test
    public void IMPAnalyzerAnalyzeAssetMapError() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/BadXML/ASSETMAP.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 4);
    }

    @Test
    public void IMPAnalyzerAnalyzeOPLValid() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/OPL/OPL_8cf83c32-4949-4f00-b081-01e12b18932f_simple.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 0);

    }

    @Test
    public void IMPAnalyzerAnalyzeOPLError() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/BadXML/OPL_8cf83c32-4949-4f00-b081-01e12b18932f_simple.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 2);
    }

    @Test
    public void IMPAnalyzerAnalyzeUnknownFileError() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/BadXML/CPL_67be5fc8-87f1-4172-8d52-819ca14c7a20.xml");
        List<ErrorLogger.ErrorObject> errorList = analyzeFile(inputFile);
        Assert.assertEquals(errorList.size(), 1);
        Assert.assertTrue(errorList.get(0).getErrorDescription().contains("Unknown AssetType"));

    }

}
