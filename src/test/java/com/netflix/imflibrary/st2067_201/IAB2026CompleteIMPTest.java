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
 * Alignment Level, MCA Content, MCA Use Class, MCA Title, MCA Title Version). The CPL-only IAB checks
 * mirror the MXF path (SMPTE ST 2067-201:2026, 5.10.3), so the absent MCA Content and MCA Use Class items
 * surface again as two warnings against the CPL. This guards that the new descriptor/sub-descriptor parse
 * cleanly and that the CPL essence descriptors stay equivalent to the parsed MXF header metadata.
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
            TestHelper.assertNoErrorAtOrAbove(errors, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL);

            if (asset.matches("IAB_.*\\.mxf")) {
                // The IAB Track File reports the "should be present" warnings for the recommended items it omits -
                // the 5.6 Reference items and the 5.10.3 MCA items. Assert each specifically rather than a raw count.
                TestHelper.assertHasError(errors, "is missing the Reference Image Edit Rate item");
                TestHelper.assertHasError(errors, "is missing the Reference Audio Alignment Level item");
                TestHelper.assertHasError(errors, "is missing MCAContent");
                TestHelper.assertHasError(errors, "is missing MCAUseClass");
                TestHelper.assertHasError(errors, "is missing MCATitle");
                TestHelper.assertHasError(errors, "is missing MCATitleVersion");
            } else if (asset.matches("CPL_.*\\.xml")) {
                // The CPL-only IAB checks report the absent MCA Content and MCA Use Class items (5.10.3),
                // matching the MXF path.
                TestHelper.assertHasError(errors, "is missing MCAContent");
                TestHelper.assertHasError(errors, "is missing MCAUseClass");
            } else {
                // The ASSETMAP, PKL and video Track File validate cleanly.
                TestHelper.assertNoErrorAtOrAbove(errors, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING);
            }
        });
    }
}
