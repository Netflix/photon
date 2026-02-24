package com.netflix.imflibrary.st2067_202;

import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzeDelivery;

@Test(groups = "unit")
public class IMPAnalyzerTest
{
    @Test
    public void IMPAnalyzerTest() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/ISXD/CompleteIMP/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzeDelivery(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_ISXD_TEST_1.xml")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else if (e.getKey().matches("ISXD_TEST_1_01_dovi_isxd.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                        for (ErrorLogger.ErrorObject error : e.getValue()) {
                            Assert.assertFalse(error.getErrorDescription().contains("MXF ISXDEssenceDescriptor does not contain a Data Essence Coding UL"),
                                    "Version byte of DataEssenceCodingUL is known to be incorrect, but should be ignored: " + error.getErrorDescription());
                        }
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzer2Test() throws IOException
    {
        // this IMP was modified to reference a SOUND Track File from a ISXD Virtual Track
        Path inputFile = TestHelper.findResourceByPath("TestIMP/ISXD/CompleteIMP2/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzeDelivery(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_ISXD_TEST_1.xml")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("ISXD_TEST_1_01_EN_20_A.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzer3Test() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/ISXD/CompleteIMP3/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzeDelivery(inputFile);
        Assert.assertEquals(errorMap.size(), 4);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_cbbadf7d-5378-4680-8643-d1fdcfde1588.xml")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else if (e.getKey().matches("DOLBY_0e1f214d-d63d-478c-bebe-7ea03e9197c6.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                        for (ErrorLogger.ErrorObject error : e.getValue()) {
                            Assert.assertFalse(error.getErrorDescription().contains("MXF ISXDEssenceDescriptor does not contain a Data Essence Coding UL"),
                                    "Version byte of DataEssenceCodingUL is known to be incorrect, but should be ignored: " + error.getErrorDescription());
                        }
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

 }
