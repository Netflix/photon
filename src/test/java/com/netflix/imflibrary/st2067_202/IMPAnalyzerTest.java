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

 }
