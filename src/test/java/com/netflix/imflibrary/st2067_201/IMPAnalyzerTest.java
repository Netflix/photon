package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
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
                        // The IAB Track File reports the "should be present" warnings for the recommended items it
                        // omits (5.6 Reference items and 5.10.3 MCA items). Assert each specifically rather than a count.
                        TestHelper.assertHasError(e.getValue(), "is missing the Reference Image Edit Rate item");
                        TestHelper.assertHasError(e.getValue(), "is missing the Reference Audio Alignment Level item");
                        TestHelper.assertHasError(e.getValue(), "is missing MCAContent");
                        TestHelper.assertHasError(e.getValue(), "is missing MCAUseClass");
                        TestHelper.assertHasError(e.getValue(), "is missing MCATitle");
                        TestHelper.assertHasError(e.getValue(), "is missing MCATitleVersion");
                    } else if (e.getKey().matches("CPL_.*\\.xml")) {
                        // The CPL-only IAB checks report the absent MCA Content and MCA Use Class items (5.10.3),
                        // matching the MXF path.
                        TestHelper.assertHasError(e.getValue(), "is missing MCAContent");
                        TestHelper.assertHasError(e.getValue(), "is missing MCAUseClass");
                    } else {
                        // The ASSETMAP, PKL and audio/video Track Files validate cleanly.
                        TestHelper.assertNoErrorAtOrAbove(e.getValue(), IMFErrorLogger.IMFErrors.ErrorLevels.WARNING);
                    }
                }
        );

    }
}
