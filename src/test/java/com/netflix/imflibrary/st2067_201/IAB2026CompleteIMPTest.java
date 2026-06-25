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

/**
 * Analyzes a complete IMP whose IAB Track File carries the structural metadata introduced in
 * SMPTE ST 2067-201:2026 - the IABMaxObjectCount Essence Descriptor item and a set of
 * IABChannelSubDescriptor instances. The package validates without any fatal error; the IAB Track File
 * only emits the expected "should be present" warnings (absent Reference Image Edit Rate, Reference Audio
 * Alignment Level, MCA Content, MCA Use Class, MCA Title, MCA Title Version). This guards that the new
 * descriptor/sub-descriptor parse cleanly and that the CPL essence descriptors stay equivalent to the
 * parsed MXF header metadata.
 */
@Test(groups = "unit")
public class IAB2026CompleteIMPTest {

    @Test
    public void iab2026CompleteIMPTest() throws IOException {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/IAB/CompleteIMP2026");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzeDelivery(inputFile);

        // ASSETMAP, PKL, CPL, the IAB Track File and the video Track File.
        Assert.assertEquals(errorMap.size(), 5);

        errorMap.forEach((asset, errors) -> {
            // No part of the delivery may produce a fatal error.
            long fatalCount = errors.stream()
                    .filter(e -> e.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.FATAL)
                    .count();
            Assert.assertEquals(fatalCount, 0, String.format("Unexpected fatal error(s) for %s: %s", asset, errors));

            if (asset.matches("IAB_.*\\.mxf")) {
                // Only the six non-fatal "should be present" warnings are expected.
                Assert.assertEquals(errors.size(), 6, String.format("Unexpected issues for %s: %s", asset, errors));
            } else {
                // The CPL, ASSETMAP, PKL and video Track File validate cleanly.
                Assert.assertEquals(errors.size(), 0, String.format("Unexpected issues for %s: %s", asset, errors));
            }
        });
    }
}
