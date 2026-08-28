package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.utils.ByteArrayByteRangeProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Test(groups = "unit")
public class IMFCPLValidatorTest
{
    private static final String MARKER_CPL = "TestIMP/Netflix_Sony_Plugfest_2015/" +
            "CPL_BLACKL_202_HD_REC709_178_LAS_8fad47bb-ab01-4f0d-a08c-d1e6c6cb62b4_corrected.xml";
    private static final String FIRST_MARKER_LABEL = "<Label>FFCL</Label>";


    @Test
    public void invalidCPLfragmentedVirtulTrack() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_fragmented_virtual_track.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void invalidCPLfragmentedVirtulTrack_02() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_fragmented_virtual_track_02.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void invalidCPLdanglingED() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_dangling_ed.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
        logger.addAllErrors(imfCompositionPlaylist.getErrors());
        logger.addAllErrors(IMPValidator.validateComposition(imfCompositionPlaylist, null));

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertNotEquals(logger.getErrors().size(), 0);
    }

    @Test
    public void invalidStandardMarkerLabelIsReported() throws IOException
    {
        List<ErrorLogger.ErrorObject> markerErrors = markerLabelErrors(
                validateFirstMarker("<Label>Mark5</Label>"));

        Assert.assertEquals(markerErrors.size(), 1);
        Assert.assertEquals(markerErrors.get(0).getErrorCode(), IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR);
        Assert.assertEquals(markerErrors.get(0).getErrorLevel(), IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL);
        Assert.assertTrue(markerErrors.get(0).getErrorDescription().contains("Mark5"));
        Assert.assertTrue(markerErrors.get(0).getErrorDescription().contains(
                "http://www.smpte-ra.org/schemas/2067-3/2013#standard-markers"));
    }

    @Test
    public void unsupportedMarkerResourceIsReportedWithoutThrowing() throws IOException
    {
        String cpl = markerCpl();
        int markerSequenceIndex = cpl.indexOf("<cc:MarkerSequence");
        Assert.assertTrue(markerSequenceIndex >= 0, "Unable to locate the MarkerSequence in the CPL fixture");
        int resourceListStart = cpl.indexOf("<ResourceList>", markerSequenceIndex);
        int resourceListEnd = cpl.indexOf("</ResourceList>", resourceListStart);
        Assert.assertTrue(resourceListStart >= 0, "Unable to locate the marker ResourceList in the CPL fixture");
        Assert.assertTrue(resourceListEnd >= 0, "Unable to locate the end of the marker ResourceList in the CPL fixture");

        String unsupportedMarkerResourceList = "<ResourceList>" +
                "<Resource xsi:type=\"TrackFileResourceType\">" +
                "<Id>urn:uuid:41301aa3-2b05-4650-a4d2-9b3a8c618589</Id>" +
                "<IntrinsicDuration>5971</IntrinsicDuration>" +
                "<SourceEncoding>urn:uuid:79c477f4-6e31-4a7e-9bf8-eef60cb73c1b</SourceEncoding>" +
                "<TrackFileId>urn:uuid:cccad3b4-3b2e-4994-90f6-2779a587f51c</TrackFileId>" +
                "</Resource>" +
                "</ResourceList>";
        String cplWithUnsupportedMarkerResource = cpl.substring(0, resourceListStart) +
                unsupportedMarkerResourceList +
                cpl.substring(resourceListEnd + "</ResourceList>".length());
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(
                new ByteArrayByteRangeProvider(cplWithUnsupportedMarkerResource.getBytes(StandardCharsets.UTF_8)));
        Assert.assertTrue(imfCompositionPlaylist.getErrors().stream().anyMatch(error ->
                error.getErrorDescription().contains("Unsupported Resource type in Marker Sequence")));

        List<ErrorLogger.ErrorObject> errors = IMPValidator.validateComposition(imfCompositionPlaylist, null);
        Assert.assertTrue(errors.stream().anyMatch(error ->
                error.getErrorCode() == IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR &&
                        error.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.FATAL &&
                        error.getErrorDescription().contains("does not have any associated resources")));
    }

    @Test
    public void unknownMarkerScopeIsReportedAsWarning() throws IOException
    {
        String scope = "https://example.com/custom-markers";
        List<ErrorLogger.ErrorObject> markerErrors = markerLabelErrors(
                validateFirstMarker(String.format("<Label scope=\"%s\">Mark5</Label>", scope)));

        Assert.assertEquals(markerErrors.size(), 1);
        Assert.assertEquals(markerErrors.get(0).getErrorCode(), IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR);
        Assert.assertEquals(markerErrors.get(0).getErrorLevel(), IMFErrorLogger.IMFErrors.ErrorLevels.WARNING);
        Assert.assertTrue(markerErrors.get(0).getErrorDescription().contains(scope));
    }

    @DataProvider(name = "validMarkerLabels")
    public Object[][] validMarkerLabels()
    {
        return new Object[][] {
                {"<Label>FFOC</Label>"},
                {"<Label scope=\"http://www.smpte-ra.org/schemas/2067-3/2016#standard-markers\">FFDC</Label>"},
                {"<Label scope=\"http://www.smpte-ra.org/schemas/2067-3/2020#standard-markers\">FFEI</Label>"}
        };
    }

    @Test(dataProvider = "validMarkerLabels")
    public void validStandardMarkerLabelIsAccepted(String labelElement) throws IOException
    {
        Assert.assertTrue(markerLabelErrors(validateFirstMarker(labelElement)).isEmpty());
    }

    private static List<ErrorLogger.ErrorObject> validateFirstMarker(String labelElement) throws IOException
    {
        String cpl = markerCpl();
        int markerLabelIndex = cpl.indexOf(FIRST_MARKER_LABEL);
        Assert.assertTrue(markerLabelIndex >= 0, "Unable to locate the marker label in the CPL fixture");

        String updatedCpl = cpl.substring(0, markerLabelIndex) + labelElement +
                cpl.substring(markerLabelIndex + FIRST_MARKER_LABEL.length());
        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(
                new ByteArrayByteRangeProvider(updatedCpl.getBytes(StandardCharsets.UTF_8)));

        return IMPValidator.validateComposition(imfCompositionPlaylist, null);
    }

    private static String markerCpl() throws IOException
    {
        Path inputFile = TestHelper.findResourceByPath(MARKER_CPL);
        return Files.readString(inputFile, StandardCharsets.UTF_8);
    }

    private static List<ErrorLogger.ErrorObject> markerLabelErrors(List<ErrorLogger.ErrorObject> errors)
    {
        return errors.stream()
                .filter(error -> error.getErrorDescription().contains("Marker Label"))
                .collect(Collectors.toList());
    }

}
