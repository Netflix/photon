package com.netflix.imflibrary.app;

import com.netflix.imflibrary.app.IMPAnalyzer.ApplicationSet;
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
public class IMPAnalyzerTestApp5Errors
{
    @Test
    public void IMPAnalyzerTestApp5Errors() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application5/PhotonApp5TestDiscontinuityAndVideoLineMapError/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile, ApplicationSet.APPLICATION_5_SET);
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
                {
                	if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }
}
