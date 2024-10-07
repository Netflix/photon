package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.J2KHeaderParameters;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.JPEG2000PictureSubDescriptor;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.Application2E2021;
import com.netflix.imflibrary.st2067_2.CompositionImageEssenceDescriptorModel;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylistType;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;

@Test(groups = "unit")
public class J2KHeaderParametersTest {

    @Test
    public void testParsedMXFJ2KParametersMatchParsedCPLParameters() throws IOException {
        // First, get the parsed MXF JPEG2000PictureSubDescriptor...
        File inputMXFFile = TestHelper.findResourceByPath("TestIMP/HT/IMP/VIDEO_6ed567b7-c030-46d6-9c1c-0f09bab4b962.mxf");
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        HeaderPartition headerPartition = HeaderPartition.fromFile(inputMXFFile, imfErrorLogger);
        GenericPictureEssenceDescriptor pictureEssenceDescriptor = ((GenericPictureEssenceDescriptor)((SourcePackage) headerPartition.getSourcePackages().get(0)).getGenericDescriptor());
        GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO descriptorBO = (GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO) TestHelper.getValue(pictureEssenceDescriptor, "rgbaPictureEssenceDescriptorBO");
        InterchangeObject.InterchangeObjectBO jpeg2000SubDescriptor = headerPartition.getSubDescriptors(descriptorBO).get(0);
        JPEG2000PictureSubDescriptor.JPEG2000PictureSubDescriptorBO jpeg2000PictureSubDescriptorBO = (JPEG2000PictureSubDescriptor.JPEG2000PictureSubDescriptorBO) jpeg2000SubDescriptor;

        // Next, get the parsed CPL Descriptor...
        File inputCPLFile = TestHelper.findResourceByPath("TestIMP/HT/IMP/CPL_67be5fc8-87f1-4172-8d52-819ca14c7a20.xml");
        FileByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputCPLFile);
        IMFErrorLogger logger = new IMFErrorLoggerImpl();
        IMFCompositionPlaylistType imfCompositionPlaylistType = IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, logger);
        Application2E2021 app = new Application2E2021(imfCompositionPlaylistType);
        CompositionImageEssenceDescriptorModel image = app.getCompositionImageEssenceDescriptorModel(); // Calls 'J2KHeaderParameters.fromDOMNode(...)

        J2KHeaderParameters fromMXF = J2KHeaderParameters.fromJPEG2000PictureSubDescriptorBO(jpeg2000PictureSubDescriptorBO);
        J2KHeaderParameters fromCPL = image.getJ2KHeaderParameters();

        // ...and validate that they are the same.
        Assert.assertTrue(fromCPL.equals(fromMXF));
    }

}
