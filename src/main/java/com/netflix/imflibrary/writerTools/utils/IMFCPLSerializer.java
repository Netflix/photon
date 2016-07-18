/*
 *
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.writerTools.utils;

import org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType;

import javax.annotation.concurrent.ThreadSafe;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class assists in serializing an IMF CPL root element to an XML file
 */
@ThreadSafe
class IMFCPLSerializer {

    /**
     * A method that serializes the CompositionPlaylistType root element to an XML file
     *
     * @param cplType the composition playlist object
     * @param output stream to which the resulting serialized XML document is written to
     * @param formatted a boolean to indicate if the serialized XML should be formatted (good idea to have it set to true always)
     * @throws IOException - any I/O related error will be exposed through an IOException
     * @throws org.xml.sax.SAXException - any issues with instantiating a schema object with the schema sources will be exposed
     * through a SAXException
     * @throws javax.xml.bind.JAXBException - any issues in serializing the XML document using JAXB will be exposed through a JAXBException
     */

    public void write(CompositionPlaylistType cplType, OutputStream output, boolean formatted) throws IOException, org.xml.sax.SAXException, JAXBException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try(
                InputStream cplSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_3_2013/imf-cpl.xsd");
                InputStream dcmlSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st0433_2008/dcmlTypes/dcmlTypes.xsd");
                InputStream dsigSchemaAsAStream = contextClassLoader.getResourceAsStream("org/w3/_2000_09/xmldsig/xmldsig-core-schema.xsd");
                InputStream coreConstraintsSchemaAsAStream = contextClassLoader.getResourceAsStream("org/smpte_ra/schemas/st2067_2_2013/imf-core-constraints-20130620-pal.xsd")
        )
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
            StreamSource[] schemaSources = new StreamSource[4];
            schemaSources[0] = new StreamSource(dsigSchemaAsAStream);
            schemaSources[1] = new StreamSource(dcmlSchemaAsAStream);
            schemaSources[2] = new StreamSource(cplSchemaAsAStream);
            schemaSources[3] = new StreamSource(coreConstraintsSchemaAsAStream);
            Schema schema = schemaFactory.newSchema(schemaSources);

            JAXBContext jaxbContext = JAXBContext.newInstance("org.smpte_ra.schemas.st2067_2_2013");
            Marshaller marshaller = jaxbContext.createMarshaller();
            ValidationEventHandlerImpl validationEventHandler = new ValidationEventHandlerImpl(true);
            marshaller.setEventHandler(validationEventHandler);
            marshaller.setSchema(schema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

            /*marshaller.marshal(cplType, output);
            workaround for 'Error: unable to marshal type "CompositionPlaylistType" as an element because it is missing an @XmlRootElement annotation'
            as found at https://weblogs.java.net/blog/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always
             */
            marshaller.marshal(new JAXBElement<>(new QName("http://www.smpte-ra.org/schemas/2067-3/2013", "CompositionPlaylist"), CompositionPlaylistType.class, cplType), output);


            if(validationEventHandler.hasErrors())
            {
                throw new IOException(validationEventHandler.toString());
            }
        }
    }
}
