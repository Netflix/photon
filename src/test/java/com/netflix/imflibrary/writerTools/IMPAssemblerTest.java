package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.app.IMPAnalyzer;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFMarkerType;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.slf4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import testUtils.TestHelper;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IMPAssemblerTest {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(IMPAssemblerTest.class);


    @DataProvider(name = "trackEntries")
    private Object[][] trackEntries() {
        return new Object[][] {
                {
                        // video & audio have all values provided by the user
                        new IMPAssembler.EssenceTrackEntry(
                                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_00.mxf"),
                                new Composition.EditRate(60000L, 1001L),
                                BigInteger.valueOf(10),
                                BigInteger.valueOf(0),
                                BigInteger.valueOf(10),
                                BigInteger.valueOf(1),
                                java.util.Base64.getDecoder().decode("fL7SnTeNskm71I4otXqr/T0D5LQ=")
                        ),
                        new IMPAssembler.EssenceTrackEntry(
                                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_ENG-51_00.mxf"),
                                new Composition.EditRate(48000L, 1L),
                                BigInteger.valueOf(8008),
                                BigInteger.valueOf(0),
                                BigInteger.valueOf(8008),
                                BigInteger.valueOf(1),
                                java.util.Base64.getDecoder().decode("X6GxGHTavnlIRLZiD7hHe5/CUh4=")
                        )

                },
                {
                        // video & audio values are left null for Photon to figure out
                        new IMPAssembler.EssenceTrackEntry(
                                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_00.mxf"),
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        new IMPAssembler.EssenceTrackEntry(
                                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_ENG-51_00.mxf"),
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                }


        };
    }

    @Test(dataProvider = "trackEntries")
    public void testAssembleIMFFromFiles(IMPAssembler.TrackEntry videoTrackEntry, IMPAssembler.TrackEntry audioTrackEntry) throws IOException, JAXBException, ParserConfigurationException, URISyntaxException, SAXException {

        List<IMPAssembler.Track> markerTrackList = new ArrayList<>();

        IMPAssembler.Track videoTrack = new IMPAssembler.Track();
        videoTrack.getTrackEntries().add(videoTrackEntry);
        videoTrack.getTrackEntries().add(videoTrackEntry);
        videoTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MainImageSequence);
        List<IMPAssembler.Track> trackList = new ArrayList<>();
        trackList.add(videoTrack);


        IMPAssembler.Track audioTrack = new IMPAssembler.Track();
        audioTrack.getTrackEntries().add(audioTrackEntry);
        audioTrack.getTrackEntries().add(audioTrackEntry);
        audioTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MainAudioSequence);
        trackList.add(audioTrack);

        IMPAssembler.SimpleTimeline simpleTimeline = new IMPAssembler.SimpleTimeline(trackList, markerTrackList, new Composition.EditRate(Arrays.asList(60000L, 1001L)));

        Path outputDirPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMPAssemblerTest");
        File outputDir = outputDirPath.toFile();
        IMPAssembler impAssembler = new IMPAssembler();
        IMPAssembler.AssembledIMPResult result = impAssembler.assembleIMFFromFiles(simpleTimeline, outputDir, true);

        // ensure there were no errors
        assert result.getErrors().isEmpty();

        // validate generated IMP
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = IMPAnalyzer.analyzePackage(outputDir);
        // ensure there are no fatal errors in the generated IMP
        for (Map.Entry<String, List<ErrorLogger.ErrorObject>> entry : errorMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            logger.info("Errors in file: {}", entry.getKey());
            for (ErrorLogger.ErrorObject errorObject : entry.getValue()) {
                logger.info("Error: code: {}, level: {}, description: {}", errorObject.getErrorCode(), errorObject.getErrorLevel(), errorObject.getErrorDescription());
                assert !errorObject.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL);
            }
        }

        assert result.getCpl().exists();
        assert result.getPkl().exists();
        assert result.getAssetMap().exists();
        for (File outputTrackFile : result.getTrackFiles()) {
            assert outputTrackFile.exists();
        }
    }

    @Test
    public void testAssembleIMPWithMarkerTrack() throws IOException, JAXBException, ParserConfigurationException, URISyntaxException, SAXException{
        List<IMPAssembler.Track> trackList = new ArrayList<>();
        List<IMPAssembler.Track> markerTrackList = new ArrayList<>();
        IMPAssembler.Track videoTrack = new IMPAssembler.Track();
        IMPAssembler.Track audioTrack = new IMPAssembler.Track();
        IMPAssembler.Track markerTrack = new IMPAssembler.Track();

        {
            IMPAssembler.TrackEntry videoTrackEntry = new IMPAssembler.EssenceTrackEntry(
                    TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_00.mxf"),
                    new Composition.EditRate(60000L, 1001L),
                    BigInteger.valueOf(10),
                    BigInteger.valueOf(0),
                    BigInteger.valueOf(10),
                    BigInteger.valueOf(1),
                    java.util.Base64.getDecoder().decode("fL7SnTeNskm71I4otXqr/T0D5LQ=")
            );
            videoTrack.getTrackEntries().add(videoTrackEntry);
            videoTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MainImageSequence);
        }
        {
            IMPAssembler.TrackEntry audioTrackEntry = new IMPAssembler.EssenceTrackEntry(
                    TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_ENG-51_00.mxf"),
                    new Composition.EditRate(48000L, 1L),
                    BigInteger.valueOf(8008),
                    BigInteger.valueOf(0),
                    BigInteger.valueOf(8008),
                    BigInteger.valueOf(1),
                    java.util.Base64.getDecoder().decode("X6GxGHTavnlIRLZiD7hHe5/CUh4=")
            );
            audioTrack.getTrackEntries().add(audioTrackEntry);
            audioTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MainAudioSequence);
        }
        {
            IMFMarkerType.Label label = new IMFMarkerType.Label("AD_MARKER", "http://www.netflix.net/schemas/IMF/ad-markers/v0");
            IMPAssembler.TrackEntry markerTrackEntry1 = new IMPAssembler.MarkerTrackEntry(null, label, BigInteger.valueOf(2));
            IMPAssembler.TrackEntry markerTrackEntry2 = new IMPAssembler.MarkerTrackEntry(null, label, BigInteger.valueOf(4));
            markerTrack.getTrackEntries().add(markerTrackEntry1);
            markerTrack.getTrackEntries().add(markerTrackEntry2);
            markerTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MarkerSequence);
        }
        trackList.add(videoTrack);
        trackList.add(audioTrack);
        markerTrackList.add(markerTrack);

        IMPAssembler.SimpleTimeline simpleTimeline = new IMPAssembler.SimpleTimeline(trackList, markerTrackList, new Composition.EditRate(Arrays.asList(60000L, 1001L)));

        Path outputDirPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMPAssemblerTest");
        File outputDir = outputDirPath.toFile();
        IMPAssembler impAssembler = new IMPAssembler();
        IMPAssembler.AssembledIMPResult result = impAssembler.assembleIMFFromFiles(simpleTimeline, outputDir, true);

        // ensure there were no errors
        assert result.getErrors().isEmpty();

        // validate generated IMP
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = IMPAnalyzer.analyzePackage(outputDir);
        // ensure there are no fatal errors in the generated IMP
        for (Map.Entry<String, List<ErrorLogger.ErrorObject>> entry : errorMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            logger.info("Errors in file: {}", entry.getKey());
            for (ErrorLogger.ErrorObject errorObject : entry.getValue()) {
                logger.info("Error: code: {}, level: {}, description: {}", errorObject.getErrorCode(), errorObject.getErrorLevel(), errorObject.getErrorDescription());
                assert !errorObject.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels.FATAL);
            }
        }

        assert result.getCpl().exists();
        assert result.getPkl().exists();
        assert result.getAssetMap().exists();
        for (File outputTrackFile : result.getTrackFiles()) {
            assert outputTrackFile.exists();
        }

    }
}