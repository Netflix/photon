package com.netflix.imflibrary.app;

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzePackage;

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
    public void testMain() throws Exception {
        try {
            IMPAnalyzer.main(new String[]{

            });
        } catch (IllegalArgumentException ex) {
            assert ex.getMessage().contains("Usage");
        }
    }

    @Test
    public void testMainArgs() throws Exception {
        try {
            IMPAnalyzer.main(new String[] {
                    "/bad/path"
            });
        } catch (FileNotFoundException ex) {
            assert ex.getMessage().contains("does not exist");
        }
    }

    @Test
    public void testMainBadFile() throws Exception {
        try {
            IMPAnalyzer.main(new String[] {
                    TestHelper.findResourceByPath("TearsOfSteel_4k_Test_Master_Audio_002.mxf").getAbsolutePath()
            });
        } catch (IMFException ex) {
            assert ex.getMessage().contains("ChannelAssignment UL");
            assert ex.getMessage().contains("does not have a RFC5646 spoken language");
            assert ex.getMessage().contains("there are 0 AudioChannelLabelSubdescriptors");
        }
    }

    @Test
    public void testMainBadIMP() throws Exception {
        try {
            IMPAnalyzer.main(new String[] {
                    TestHelper.findResourceByPath("test_mapped_file_set").getAbsolutePath()
            });
        } catch (IMFException ex) {
            assert ex.getMessage().contains("Unsupported/Missing ApplicationIdentification");
            assert ex.getMessage().contains("is not an integer");
            assert ex.getMessage().contains("EssenceDescriptorList is either absent or empty");
            assert ex.getMessage().contains("Failed to get header partition");
            assert ex.getMessage().contains("CPL resource 93c158aa-b9e8-152e-9f17-0e9a350ff9ac is not present in the package");
        }
    }

}
