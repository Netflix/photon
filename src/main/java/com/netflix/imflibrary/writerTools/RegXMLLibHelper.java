/*
 *
 *  * Copyright 2015 Netflix, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *         http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.netflix.imflibrary.writerTools;

import com.sandflow.smpte.klv.Group;
import com.sandflow.smpte.klv.LocalSet;
import com.sandflow.smpte.klv.LocalTagRegister;
import com.sandflow.smpte.klv.MemoryTriplet;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.mxf.PrimerPack;
import com.sandflow.smpte.mxf.Set;
import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
import com.netflix.imflibrary.MXFKLVPacket;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.utils.ByteProvider;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;


import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;

/**
 * A utility class that provides the methods to obtain a RegXML representation of a MXF metadata set
 */
public final class RegXMLLibHelper {

    private final MetaDictionaryCollection metaDictionaryCollection;
    private final LocalTagRegister localTagRegister;

    /**
     * Constructor for the RegXMLLibHelper
     *
     * @param primerPack the Triplet representing the primer pack
     * @param primerPackByteProvider the data provider for the primer pack
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public RegXMLLibHelper(MXFKLVPacket.Header primerPack, ByteProvider primerPackByteProvider) throws IOException{

        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("reference-registers/Elements.xml");
            Reader elementsRegister = new InputStreamReader(in, Charset.forName("UTF-8"));

            in = ClassLoader.getSystemResourceAsStream("reference-registers/Types.xml");
            Reader typesRegister = new InputStreamReader(in, Charset.forName("UTF-8"));

            in = ClassLoader.getSystemResourceAsStream("reference-registers/Groups.xml");
            Reader groupsRegister = new InputStreamReader(in, Charset.forName("UTF-8"));

            /*in = ClassLoader.getSystemResourceAsStream("reference-registers/Labels.xml");
            Reader labelsRegister = new InputStreamReader(in, Charset.forName("UTF-8"));*/

            ElementsRegister ereg = ElementsRegister.fromXML(elementsRegister);
            GroupsRegister greg = GroupsRegister.fromXML(groupsRegister);
            TypesRegister treg = TypesRegister.fromXML(typesRegister);

            this.metaDictionaryCollection = fromRegister(treg, greg, ereg);
            this.localTagRegister = PrimerPack.createLocalTagRegister(this.getTripletFromKLVHeader(primerPack, primerPackByteProvider));
            in.close();
        }
        catch (Exception e){
            throw new IOException(String.format("Unable to load resources corresponding to registers"));
        }
    }

    /**
     * A utility method that provides an XML Document fragment representing an MXF KLV triplet
     * @param triplet the KLV triplet that needs to be serialized to an XML document fragment
     * @param document the XML document that the document fragment is associated with
     * @return An XML DOM DocumentFragment
     * @throws MXFException if any error occurs while trying to create the document fragment
     */

    public DocumentFragment getDocumentFragment(Triplet triplet, Document document) throws MXFException {
        try {
            HashMap<UUID, Set> setResolver = new HashMap<>();
            Group group = LocalSet.fromTriplet(triplet, this.localTagRegister);
            Set set = Set.fromGroup(group);
            setResolver.put(set.getInstanceID(), set);
            FragmentBuilder fragmentBuilder = new FragmentBuilder(this.metaDictionaryCollection, setResolver);
            return fragmentBuilder.fromTriplet(group, document);
        }
        catch (FragmentBuilder.RuleException | KLVException | ParserConfigurationException e){
            throw new MXFException(String.format("Could not generate MXFFragment for the KLV Set"));
        }
    }

    /**
     * A utility method that provides an XML Document fragment representing an Essence Descriptor in the MXF file
     * @param essenceDescriptorTriplet - a KLV triplet corresponding to an Essence Descriptor
     * @param document an XML document
     * @param subDescriptorTriplets list of triplets corresponding to the subdescriptors referred by the essenceDescriptor
     * @return An XML DOM DocumentFragment
     * @throws MXFException if any error occurs while trying to create the document fragment
     */

    public DocumentFragment getEssenceDescriptorDocumentFragment(Triplet essenceDescriptorTriplet, List<Triplet> subDescriptorTriplets, Document document) throws MXFException {
        try {
            HashMap<UUID, Set> setResolver = new HashMap<>();
            Group group = LocalSet.fromTriplet(essenceDescriptorTriplet, this.localTagRegister);
            Set set = Set.fromGroup(group);
            setResolver.put(set.getInstanceID(), set);
            /*Add all the subdescriptors into the setResolver*/
            for(Triplet subDescriptorTriplet:subDescriptorTriplets){
                Group subDescriptorGroup = LocalSet.fromTriplet(subDescriptorTriplet, this.localTagRegister);
                Set subDescriptorSet = Set.fromGroup(subDescriptorGroup);
                setResolver.put(subDescriptorSet.getInstanceID(), subDescriptorSet);
            }
            FragmentBuilder fragmentBuilder = new FragmentBuilder(this.metaDictionaryCollection, setResolver);
            return fragmentBuilder.fromTriplet(group, document);
        }
        catch (FragmentBuilder.RuleException | KLVException | ParserConfigurationException e){
            throw new MXFException(String.format("Could not generate MXFFragment for the KLV Set"));
        }
    }

    /**
     * A utility method that provides an in-memory triplet representing an MXF KLV packet
     * @param header - the MXF KLV header corresponding to an MXF KLV packet
     * @param byteProvider - data provider for the MXF KLV packet
     * @return A memory triplet representing the MXF KLV packet
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public MemoryTriplet getTripletFromKLVHeader(MXFKLVPacket.Header header, ByteProvider byteProvider) throws IOException {
        UL key = new UL(byteProvider.getBytes(MXFKLVPacket.KEY_FIELD_SIZE));
        MXFKLVPacket.LengthField lengthField = MXFKLVPacket.getLength(byteProvider);
        if(lengthField.value != header.getVSize()){
            throw new MXFException(String.format("KLVPacket length %d read from the bitstream does not match the size of the value %d", lengthField.value, header.getVSize()));
        }
        if (header.getVSize() > Integer.MAX_VALUE) {
            throw new MXFException(String.format("Essence Descriptors that are larger than %d bytes are not supported", Integer.MAX_VALUE));
        }
        byte[] value = byteProvider.getBytes((int) header.getVSize());
        return new MemoryTriplet(key, value);
    }
}
