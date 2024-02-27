package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.app.IMPAnalyzer;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.slf4j.Logger;
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

    @Test
    public void testAssembleIMFFromFiles() throws IOException, JAXBException, ParserConfigurationException, URISyntaxException, SAXException {

        IMPAssembler.TrackEntry videoFile1 = new IMPAssembler.TrackEntry(
                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_00.mxf"),
                new Composition.EditRate(60000L, 1001L),
                BigInteger.valueOf(10),
                BigInteger.valueOf(0),
                BigInteger.valueOf(10),
                BigInteger.valueOf(1)
        );
        IMPAssembler.Track videoTrack = new IMPAssembler.Track();
        videoTrack.getTrackEntries().add(videoFile1);
        videoTrack.getTrackEntries().add(videoFile1);
        videoTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MainImageSequence);
        List<IMPAssembler.Track> trackList = new ArrayList<>();
        trackList.add(videoTrack);



        IMPAssembler.TrackEntry audioFile1 = new IMPAssembler.TrackEntry(
                TestHelper.findResourceByPath("TestIMP/MERIDIAN_Netflix_Photon_161006/MERIDIAN_Netflix_Photon_161006_ENG-51_00.mxf"),
                new Composition.EditRate(48000L, 1L),
                BigInteger.valueOf(8008),
                BigInteger.valueOf(0),
                BigInteger.valueOf(8008),
                BigInteger.valueOf(1)
        );

        IMPAssembler.Track audioTrack = new IMPAssembler.Track();
        audioTrack.getTrackEntries().add(audioFile1);
        audioTrack.getTrackEntries().add(audioFile1);
        audioTrack.setSequenceTypeEnum(Composition.SequenceTypeEnum.MainAudioSequence);
        trackList.add(audioTrack);

        IMPAssembler.SimpleTimeline simpleTimeline = new IMPAssembler.SimpleTimeline(trackList, new Composition.EditRate(Arrays.asList(60000L, 1001L)));



        Path outputDirPath = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "IMPAssemblerTest");
        File outputDir = outputDirPath.toFile();
        // File outputDirectory = new File("outputDirectory");
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