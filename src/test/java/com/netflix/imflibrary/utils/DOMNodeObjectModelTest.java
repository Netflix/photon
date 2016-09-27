package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PrimerPack;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.writerTools.RegXMLLibHelper;
import com.sandflow.smpte.klv.Triplet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import testUtils.TestHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of tests for DOMNodeObjectModel
 */
@Test(groups = "unit")
public class DOMNodeObjectModelTest {

    private ByteProvider getByteProvider(ResourceByteRangeProvider resourceByteRangeProvider, KLVPacket.Header header) throws IOException {
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize());
        ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
        return byteProvider;
    }

    public List<DOMNodeObjectModel> setUp(File inputFile) throws IOException, ParserConfigurationException {

        IMFErrorLogger imfErrorLogger1 = new IMFErrorLoggerImpl();
        List<Node> essenceDescriptorNodes1 = new ArrayList<>();
        List<DOMNodeObjectModel> essenceDescriptorDOMNodeObjectModels1 = new ArrayList<>();
        ResourceByteRangeProvider resourceByteRangeProvider1 = new FileByteRangeProvider(inputFile);
        byte[] bytes1 = resourceByteRangeProvider1.getByteRangeAsBytes(0, resourceByteRangeProvider1.getResourceSize() - 1);
        ByteProvider byteProvider1 = new ByteArrayDataProvider(bytes1);
        HeaderPartition headerPartition1 = new HeaderPartition(byteProvider1,
                0L,
                bytes1.length,
                imfErrorLogger1);

        List<InterchangeObject.InterchangeObjectBO> essenceDescriptors1 = headerPartition1.getEssenceDescriptors();
        for (InterchangeObject.InterchangeObjectBO essenceDescriptor : essenceDescriptors1) {
            List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
            List<InterchangeObject.InterchangeObjectBO> subDescriptors = headerPartition1.getSubDescriptors(essenceDescriptor);
            for (InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors) {
                if (subDescriptorBO != null) {
                    subDescriptorHeaders.add(subDescriptorBO.getHeader());
                }
            }
                /*Create a dom*/
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();

            //DocumentFragment documentFragment = this.getEssenceDescriptorAsDocumentFragment(document, headerPartitionTuple, essenceDescriptorHeader, subDescriptorHeaders);
            PrimerPack primerPack = headerPartition1.getPrimerPack();

            RegXMLLibHelper regXMLLibHelper = new RegXMLLibHelper(primerPack.getHeader(), getByteProvider(resourceByteRangeProvider1, primerPack.getHeader()));

            Triplet essenceDescriptorTriplet = regXMLLibHelper.getTripletFromKLVHeader(essenceDescriptor.getHeader(), getByteProvider(resourceByteRangeProvider1, essenceDescriptor.getHeader()));
            //DocumentFragment documentFragment = this.regXMLLibHelper.getDocumentFragment(essenceDescriptorTriplet, document);
            /*Get the Triplets corresponding to the SubDescriptors*/
            List<Triplet> subDescriptorTriplets = new ArrayList<>();
            for (KLVPacket.Header subDescriptorHeader : subDescriptorHeaders) {
                subDescriptorTriplets.add(regXMLLibHelper.getTripletFromKLVHeader(subDescriptorHeader, this.getByteProvider(resourceByteRangeProvider1, subDescriptorHeader)));
            }
            DocumentFragment documentFragment = regXMLLibHelper.getEssenceDescriptorDocumentFragment(essenceDescriptorTriplet, subDescriptorTriplets, document);
            Node node = documentFragment.getFirstChild();
            essenceDescriptorNodes1.add(node);
            essenceDescriptorDOMNodeObjectModels1.add(new DOMNodeObjectModel(node));
        }
        return essenceDescriptorDOMNodeObjectModels1;
    }

    @Test
    public void domNodeObjectModelEquivalenceNegativeTest() throws IOException, ParserConfigurationException {
        File inputFile1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");
        File inputFile2 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG51.mxf.hdr");

        List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
        domNodeObjectModels.addAll(setUp(inputFile1));
        domNodeObjectModels.addAll(setUp(inputFile2));

        Set<String> ignoreSet = new HashSet<>();
        ignoreSet.add("InstanceUID");
        DOMNodeObjectModel curr = domNodeObjectModels.get(0).createDOMNodeObjectModelIgnoreSet(domNodeObjectModels.get(0), ignoreSet);
        boolean result = true;
        for(int i=1; i<domNodeObjectModels.size(); i++){
            result &= curr.equals(domNodeObjectModels.get(i).createDOMNodeObjectModelIgnoreSet(domNodeObjectModels.get(i), ignoreSet));
        }
        Assert.assertTrue(result == false);
    }

    @Test
    public void domNodeObjectModelEquivalencePositiveTest() throws IOException, ParserConfigurationException {
        File inputFile1 = TestHelper.findResourceByPath("TestIMP/Netflix_Sony_Plugfest_2015/Netflix_Plugfest_Oct2015_ENG20.mxf.hdr");

        List<DOMNodeObjectModel> domNodeObjectModels = new ArrayList<>();
        domNodeObjectModels.addAll(setUp(inputFile1));
        domNodeObjectModels.addAll(setUp(inputFile1));

        Set<String> ignoreSet = new HashSet<>();
        ignoreSet.add("InstanceUID");
        DOMNodeObjectModel curr = domNodeObjectModels.get(0).createDOMNodeObjectModelIgnoreSet(domNodeObjectModels.get(0), ignoreSet);
        boolean result = true;
        for(int i=1; i<domNodeObjectModels.size(); i++){
            result &= curr.equals(domNodeObjectModels.get(i).createDOMNodeObjectModelIgnoreSet(domNodeObjectModels.get(i), ignoreSet));
        }
        Assert.assertTrue(result == true);
    }
}
