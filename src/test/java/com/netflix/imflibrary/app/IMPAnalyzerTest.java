package com.netflix.imflibrary.app;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0429_9.AssetMap;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzePackage;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = "unit")
public class IMPAnalyzerTest
{
    @Test
    public void IMPAnalyzerTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 6);
        errorMap.entrySet().stream().forEach( e ->
                Assert.assertEquals(e.getValue().size(), 0)
        );

    }
}
