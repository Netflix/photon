package com.netflix.imflibrary.st2067_201;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzePackage;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileLocator;
import com.netflix.imflibrary.utils.S3Locator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.S3TestBase;
import testUtils.TestHelper;

@Test(groups = "unit")
public class IMPAnalyzerTest extends S3TestBase
{
    @Test
    public void IMPAnalyzerTest() throws IOException
    {
        FileLocator inputFile = new FileLocator(TestHelper.findResourceByPath("TestIMP/IAB/CompleteIMP"));
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
            {
                if (e.getKey().matches("CPL.*Conformance")) {
                    Assert.assertEquals(e.getValue().size(), 6);
                } else if (e.getKey().matches("meridian.*")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                } else {
                    Assert.assertEquals(e.getValue().size(), 0);
                }
            }
        );

    }

    @Test
    public void IMPAnalyzerS3Test() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/IAB/CompleteIMP");
        s3uploadFiles(inputFile, "testbucket", "TestImp/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(new S3Locator(endpoint + "/testbucket/TestImp/CompleteIMP/", configuration));
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
            {
                if (e.getKey().matches("CPL.*Conformance")) {
                    Assert.assertEquals(e.getValue().size(), 6);
                } else if (e.getKey().matches("meridian.*")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                } else {
                    Assert.assertEquals(e.getValue().size(), 0);
                }
            }
        );

    }
}
