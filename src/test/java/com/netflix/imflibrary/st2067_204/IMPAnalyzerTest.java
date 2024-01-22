package com.netflix.imflibrary.st2067_204;

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
public class IMPAnalyzerTest
{
    @Test
    public void IMPAnalyzerTest() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__compliant/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_7c943006-a4f1-409e-b46d-141cad85b232.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__compliant.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_7c943006-a4f1-409e-b46d-141cad85b232.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    public void IMPAnalyzerTest01() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_44100Hz/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_dacbd933-466c-4235-aac5-a9998cac5eba.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_44100Hz.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_dacbd933-466c-4235-aac5-a9998cac5eba.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest02() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_ADMAudioProgrammeID_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_1a857795-669a-4fac-a017-52c51f7947c0.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_ADMAudioProgrammeID_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_1a857795-669a-4fac-a017-52c51f7947c0.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest03() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_ADMSoundfieldGroupLabelSubDescriptor_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_35be4818-9e2a-473a-8107-da4ae8745dd0.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_ADMSoundfieldGroupLabelSubDescriptor_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else if (e.getKey().matches("CPL_35be4818-9e2a-473a-8107-da4ae8745dd0.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest04() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_ADM_CHNASubDescriptor_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_3e7a50cd-a4e7-4024-9143-c871701bc22c.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_ADM_CHNASubDescriptor_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_3e7a50cd-a4e7-4024-9143-c871701bc22c.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest05() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCAContent_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_92cf4649-368b-4c46-8c1f-eeb2d477c73d.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCAContent_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_92cf4649-368b-4c46-8c1f-eeb2d477c73d.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest06() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCAContent_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_50ea35b8-0ad4-4b73-8091-d045f93f7b41.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCAContent_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_50ea35b8-0ad4-4b73-8091-d045f93f7b41.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest07() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCATagName_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_d2c9eeb9-26c6-46db-8a47-d6d8a0b0314d.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCATagName_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_d2c9eeb9-26c6-46db-8a47-d6d8a0b0314d.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest08() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCATagName_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_0f6da96f-c5ba-4834-9cf1-1ec7f5fd2ce0.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCATagName_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_0f6da96f-c5ba-4834-9cf1-1ec7f5fd2ce0.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest09() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCATagSymbol_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_2e1a44fe-0d76-49ef-84aa-1e60cfef1516.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCATagSymbol_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_2e1a44fe-0d76-49ef-84aa-1e60cfef1516.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest10() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCATagSymbol_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_70f2b06b-979c-4abd-9f27-66fc454347ba.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCATagSymbol_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_70f2b06b-979c-4abd-9f27-66fc454347ba.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest11() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCATitleVersion_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_f023dfe9-3a5a-42c8-99e1-3d4f1aa5d801.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCATitleVersion_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_f023dfe9-3a5a-42c8-99e1-3d4f1aa5d801.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest12() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCAUseClass_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_cca930f9-b360-4209-99b1-3c16f89cc573.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCAUseClass_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_cca930f9-b360-4209-99b1-3c16f89cc573.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }


    @Test
    public void IMPAnalyzerTest13() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/ADM/ST2067-204_audio_track_file__non_compliant_MCAUseClass_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_f850843b-fd65-4db1-acb2-8a5eecaecd10.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-204_audio_track_file__non_compliant_MCAUseClass_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_f850843b-fd65-4db1-acb2-8a5eecaecd10.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

}
