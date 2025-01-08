package com.netflix.imflibrary.st2067_201;

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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/IAB/CompleteIMP");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzeDelivery(inputFile);
        Assert.assertEquals(errorMap.size(), 6);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("meridian.*")) {
                            Assert.assertEquals(e.getValue().size(), 6);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }
}
