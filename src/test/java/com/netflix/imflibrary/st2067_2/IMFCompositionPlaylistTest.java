package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import jakarta.annotation.Nonnull;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import testUtils.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Test(groups = "unit")
public class IMFCompositionPlaylistTest
{
    @Test
    public void validCplPositiveTest() throws IOException {

        // basic tests for entire public interface
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7.xml");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(resourceByteRangeProvider));
        Assert.assertEquals(IMFCompositionPlaylist.validateCompositionPlaylistSchema(resourceByteRangeProvider).size(), 0);

        IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);

        Assert.assertEquals(imfCompositionPlaylist.getErrors().size(), 0);
        Assert.assertEquals(imfCompositionPlaylist.getUUID().toString(), "46154ef9-7b54-45eb-a85c-00efcb0d47a7");
        Assert.assertEquals(imfCompositionPlaylist.getIssuer(), "Blackmagic Design");
        Assert.assertEquals(imfCompositionPlaylist.getAnnotation(), "SolLevante_DoVi_IAB");
        Assert.assertEquals(imfCompositionPlaylist.getCreator(), "Blackmagic Design DaVinci Resolve Studio 17.0.0.0039");
        Assert.assertEquals(imfCompositionPlaylist.getContentOriginator(), "Netflix");
        Assert.assertEquals(imfCompositionPlaylist.getContentTitle(), "SolLevante_DoVi_IAB");

        Assert.assertEquals(imfCompositionPlaylist.getEditRate(), new Composition.EditRate(24L, 1L));

        Iterator<String> iterator = imfCompositionPlaylist.getApplicationIdSet().iterator();
        String appId = iterator.next();
        Assert.assertEquals(imfCompositionPlaylist.getApplicationIdSet().size(), 1);
        Assert.assertEquals(appId, "http://www.smpte-ra.org/ns/2067-21/2020");

        Assert.assertEquals(imfCompositionPlaylist.getSequenceNamespaceSet().size(), 2);
        Assert.assertTrue(imfCompositionPlaylist.getSequenceNamespaceSet().contains("http://www.smpte-ra.org/ns/2067-2/2020"));
        Assert.assertTrue(imfCompositionPlaylist.getSequenceNamespaceSet().contains("http://www.smpte-ra.org/ns/2067-201/2019"));

        Assert.assertEquals(imfCompositionPlaylist.getCplSchema(), "http://www.smpte-ra.org/schemas/2067-3/2016");
        Assert.assertEquals(imfCompositionPlaylist.getCoreConstraintsSchema(), "http://www.smpte-ra.org/ns/2067-2/2020");

        List<IMFSegmentType> segmentList = imfCompositionPlaylist.getSegmentList();
        Assert.assertEquals(segmentList.size(), 1);

        Iterator<IMFSegmentType> segmentIterator = segmentList.iterator();
        IMFSegmentType segment = segmentIterator.next();
        List<IMFSequenceType> sequenceList = segment.getSequenceList();
        Assert.assertEquals(sequenceList.size(), 2);
        Iterator<IMFSequenceType> sequenceIterator = sequenceList.iterator();
        IMFSequenceType sequence_1 = sequenceIterator.next();
        IMFSequenceType sequence_2 = sequenceIterator.next();
        Assert.assertEquals(sequence_1.getType(), "MainImageSequence");
        Assert.assertEquals(sequence_2.getType(), "IABSequence");

        String iabTrackFileID = "c2cff494-d954-4ad2-b310-688fab51c53a";
        String iabVirtualTrackID = "2c342efc-5509-458a-8369-2c777a0b9eb3";
        String imageVirtualTrackID = "f193815e-8bae-4834-b1dd-80f12186cb55";

        String sequenceNamespaceFromTF = imfCompositionPlaylist.getSequenceNamespaceForTrackFileID(UUID.fromString(iabTrackFileID));
        String sequenceNamespaceFromVT = imfCompositionPlaylist.getSequenceNamespaceForVirtualTrackID(UUID.fromString(iabVirtualTrackID));
        String sequenceTypeFromVT = imfCompositionPlaylist.getSequenceTypeForVirtualTrackID(UUID.fromString(iabVirtualTrackID));

        Assert.assertEquals(sequenceNamespaceFromTF, "http://www.smpte-ra.org/ns/2067-201/2019");
        Assert.assertEquals(sequenceNamespaceFromVT, "http://www.smpte-ra.org/ns/2067-201/2019");
        Assert.assertEquals(sequenceTypeFromVT, "IABSequence");

        Assert.assertEquals(imfCompositionPlaylist.getVirtualTrackMap().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getEssenceVirtualTracks().size(), 2);
        Assert.assertNull(imfCompositionPlaylist.getMarkerVirtualTrack());
        Assert.assertNotNull(imfCompositionPlaylist.getVideoVirtualTrack());
        Assert.assertEquals(imfCompositionPlaylist.getVideoVirtualTrack().getTrackID().toString(), imageVirtualTrackID);
        Assert.assertEquals(imfCompositionPlaylist.getAudioVirtualTracks().size(), 0);
        Assert.assertEquals(imfCompositionPlaylist.getVirtualTracks().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getAudioVirtualTracksMap().size(), 0);

        Assert.assertEquals(IMFCompositionPlaylist.getResourceIdTuples(imfCompositionPlaylist.getVirtualTracks()).size(), 2);
        Assert.assertEquals(IMFCompositionPlaylist.getVirtualTrackResourceIDs(imfCompositionPlaylist.getVideoVirtualTrack()).size(), 1);
        Assert.assertEquals(imfCompositionPlaylist.getTrackFileResources().size(), 2);

        Assert.assertNotNull(imfCompositionPlaylist.getEssenceDescriptor(UUID.fromString(iabTrackFileID)));
        Assert.assertEquals(imfCompositionPlaylist.getEssenceDescriptorList().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getEssenceDescriptorListMap().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getEssenceDescriptorDomNodeMap().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getEssenceDescriptorIdsSet().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getResourceEssenceDescriptorIdsSet().size(), 2);
        Assert.assertEquals(imfCompositionPlaylist.getCompositionImageEssenceDescriptorModels().size(), 1);

        Assert.assertNotNull(imfCompositionPlaylist.getExtensionProperties());
    }


    @Test
    public void invalidCplSchemaTest() throws IOException {

        // basic tests for entire public interface
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_schemaError.xml");

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        Assert.assertTrue(IMFCompositionPlaylist.isCompositionPlaylist(resourceByteRangeProvider));

        List<ErrorLogger.ErrorObject> errors = IMFCompositionPlaylist.validateCompositionPlaylistSchema(resourceByteRangeProvider);
        Assert.assertEquals(errors.size(), 1);
        Assert.assertTrue(errors.get(0).getErrorDescription().contains("Invalid content was found starting with element"));
    }

    @Test
    public void invalidCplSchemaExceptionTest() throws IOException {

        // basic tests for entire public interface
        Path inputFile = TestHelper.findResourceByPath
                ("TestIMP/Application2E2020/CPL_46154ef9-7b54-45eb-a85c-00efcb0d47a7_schemaError.xml");

        try {
            IMFCompositionPlaylist imfCompositionPlaylist = new IMFCompositionPlaylist(inputFile);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IMFException.class);
            Assert.assertTrue(e.getMessage().contains("Invalid content was found starting with element"));
        }
    }

}
