package com.netflix.imflibrary.st2067_203;

import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzePackage;

@Test(groups = "unit")
public class IMPAnalyzerTest
{
    @Test
    public void IMPAnalyzerTest() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__compliant/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_3a8d80e8-80ab-4152-be80-3ba86c032ead.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__compliant.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest02() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_16bit/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_bfd4736d-7ab7-4d12-a807-9f2cf4beb587.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_16bit.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_bfd4736d-7ab7-4d12-a807-9f2cf4beb587.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest03() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_44100Hz/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_95c21bb6-d26f-4afd-94ef-62420b966eec.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_44100Hz.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_95c21bb6-d26f-4afd-94ef-62420b966eec.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_ADMAudioProgrammeID_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_55dbccfd-c530-4e4c-a26c-329354f4f50d.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 7);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_ADMAudioProgrammeID_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_55dbccfd-c530-4e4c-a26c-329354f4f50d.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_ContainerContraintsSubDescriptor_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_8174af5e-66fb-43b3-acf0-cf2666cf0067.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_ContainerContraintsSubDescriptor_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_8174af5e-66fb-43b3-acf0-cf2666cf0067.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest06() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCAContent_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_132923e2-43c6-4e2c-a68b-9c8dc20c5bcf.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCAContent_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_132923e2-43c6-4e2c-a68b-9c8dc20c5bcf.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCAContent_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_a6acdc1f-a3fc-4f12-8a52-f4b474ba24b6.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCAContent_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_a6acdc1f-a3fc-4f12-8a52-f4b474ba24b6.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCATagName_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_634aab40-cc48-49eb-81bd-8f3119387a4a.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCATagName_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_634aab40-cc48-49eb-81bd-8f3119387a4a.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCATagName_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_e00a7145-9c75-46ee-a781-4abe52aac6bc.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCATagName_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_e00a7145-9c75-46ee-a781-4abe52aac6bc.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCATagSymbol_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_f6f0f34c-c7c9-49c3-ab46-673b97838289.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCATagSymbol_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_f6f0f34c-c7c9-49c3-ab46-673b97838289.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCATagSymbol_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_764a1add-ee83-4954-86c8-c41850210e1c.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCATagSymbol_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_764a1add-ee83-4954-86c8-c41850210e1c.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest12() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCATitle_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_f1033b99-37e5-4003-8733-eb27da75a63a.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCATitle_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_f1033b99-37e5-4003-8733-eb27da75a63a.xml")) {
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
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCATitleVersion_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_609b7a45-5f0d-483a-9282-9329461a7543.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCATitleVersion_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_609b7a45-5f0d-483a-9282-9329461a7543.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest14() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCAUseClass_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_81cb7798-6ca0-4bc4-8470-4dbdef679a9f.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCAUseClass_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_81cb7798-6ca0-4bc4-8470-4dbdef679a9f.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest15() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_MCAUseClass_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_8c90fed7-7225-4d15-a611-6e49ccb78180.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 8);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_MCAUseClass_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("CPL_8c90fed7-7225-4d15-a611-6e49ccb78180.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest16() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_SADM_MGAAudioMetadataIdentifier_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_46c69227-4fd4-437c-9c9a-bda4e9e90777.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_SADM_MGAAudioMetadataIdentifier_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("CPL_46c69227-4fd4-437c-9c9a-bda4e9e90777.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest17() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_SADM_MGAMetadataIndex_wrong/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_a9b4c035-3db9-4971-b4ae-46e82742cf59.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 6);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_SADM_MGAMetadataIndex_wrong.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else if (e.getKey().matches("CPL_a9b4c035-3db9-4971-b4ae-46e82742cf59.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest18() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_SADMAudioMetadataSubDescriptor_missing/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_bb130309-e1a4-4582-8266-a51ecf11e452.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_SADMAudioMetadataSubDescriptor_missing.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_bb130309-e1a4-4582-8266-a51ecf11e452.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest19() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_audio_track_file__non_compliant_two_SADM_sections/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_9c74b53a-f31d-4189-bb50-ffddf455ed7b.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__non_compliant_two_SADM_sections.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_9c74b53a-f31d-4189-bb50-ffddf455ed7b.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest20() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_CPL__non_compliant_no_virtual_track_parameterset/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_86bf532c-cdde-46aa-9038-8e5651d0b78d.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__compliant.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_86bf532c-cdde-46aa-9038-8e5651d0b78d.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest21() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_CPL__non_compliant_unkown_operational_mode/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_4c71a219-0a14-4faa-8526-985e48098a97.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 4);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__compliant.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_4c71a219-0a14-4faa-8526-985e48098a97.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void IMPAnalyzerTest22() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/SADM/ST2067-203_CPL__non_compliant_wrong_track_id/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 5);
        errorMap.entrySet().stream().forEach( e ->
                {
                    if (e.getKey().matches("CPL_8c99c449-9100-4bcc-9c3e-ef8ea6796cde.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else if (e.getKey().matches("ST2067-203_audio_track_file__compliant.mxf")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_8c99c449-9100-4bcc-9c3e-ef8ea6796cde.xml")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

 }
