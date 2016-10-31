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

package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.sandflow.smpte.klv.*;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.mxf.PrimerPack;
import com.sandflow.smpte.mxf.Set;
import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.regxml.dict.DefinitionResolver;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import com.sandflow.smpte.regxml.dict.definitions.Definition;
import com.sandflow.smpte.regxml.dict.definitions.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.RecordTypeDefinition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
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
import java.util.stream.Collectors;

import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;

/**
 * A utility class that provides the methods to obtain a RegXML representation of a MXF metadata set
 */
public final class RegXMLLibDictionary {

    private final MetaDictionaryCollection metaDictionaryCollection;

    /**
     * Constructor for the RegXMLLibHelper
     *
     * @throws IMFException - if any error occurs loading registers
     */
    public RegXMLLibDictionary() throws IMFException{

        try
        {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            InputStream in = contextClassLoader.getResourceAsStream("reference-registers/Elements.xml");
            Reader elementsRegister = new InputStreamReader(in, Charset.forName("UTF-8"));

            in = contextClassLoader.getResourceAsStream("reference-registers/Types.xml");
            Reader typesRegister = new InputStreamReader(in, Charset.forName("UTF-8"));

            in = contextClassLoader.getResourceAsStream("reference-registers/Groups.xml");
            Reader groupsRegister = new InputStreamReader(in, Charset.forName("UTF-8"));

            /*in = ClassLoader.getSystemResourceAsStream("reference-registers/Labels.xml");
            Reader labelsRegister = new InputStreamReader(in, Charset.forName("UTF-8"));*/

            ElementsRegister ereg = ElementsRegister.fromXML(elementsRegister);
            GroupsRegister greg = GroupsRegister.fromXML(groupsRegister);
            TypesRegister treg = TypesRegister.fromXML(typesRegister);

            this.metaDictionaryCollection = fromRegister(treg, greg, ereg);
            in.close();
        }
        catch (Exception e){
            throw new IMFException(String.format("Unable to load resources corresponding to registers"));
        }
    }

    /**
     * A utility method that gets Symbol name provided URN for an element
     * @return MetaDictionaryCollection
     */
    public MetaDictionaryCollection getMetaDictionaryCollection()
    {
        return this.metaDictionaryCollection;
    }

    /**
     * A utility method that gets Symbol name provided URN for an element
     * @param URN - URN of the element
     * @return Symbol name of the element
     */
    public String getSymbolNameFromURN(String URN) {
        DefinitionResolver definitionResolver = this.metaDictionaryCollection;
        Definition definition = definitionResolver.getDefinition(AUID.fromURN(URN));
        return definition != null ? definition.getSymbol( ) : null;
    }

    /**
     * A utility method that gets string representation for an enumeration value provided URN for the enumeration type and the integer enumeration value
     * @param typeURN - URN for the enumeration type
     * @param value - Integer value of the enumeration
     * @return String representation of the enumeration
     */
    public String getEnumerationNameFromValue(String typeURN, Integer value) {
        String enumName = null;
        DefinitionResolver definitionResolver = this.metaDictionaryCollection;
        Definition definition = definitionResolver.getDefinition(AUID.fromURN(typeURN));
        if(definition == null ) {
            return null;
        }else if (definition instanceof EnumerationTypeDefinition) {
            EnumerationTypeDefinition enumerationTypeDefinition = EnumerationTypeDefinition.class.cast(definition);
            List<String> enumList = enumerationTypeDefinition.getElements().stream().filter(e -> e.getValue() == value).map( e -> e.getName()).collect(Collectors.toList());
            enumName = (enumList.size() > 0)  ? enumList.get(0) : null;
        }
        return enumName;
    }

    /**
     * A utility method that gets an enumeration value provided URN for the enumeration type and the string representation of the enumeration value
     * @param typeURN - URN for the enumeration type
     * @param name - String representation of the enumeration
     * @return Enumeration value
     */
    public Integer getEnumerationValueFromName(String typeURN, String name) {
        Integer enumValue = null;
        DefinitionResolver definitionResolver = this.metaDictionaryCollection;
        Definition definition = definitionResolver.getDefinition(AUID.fromURN(typeURN));
        if(definition == null ) {
            return null;
        } else if (definition instanceof EnumerationTypeDefinition) {
            EnumerationTypeDefinition enumerationTypeDefinition = EnumerationTypeDefinition.class.cast(definition);
            List<Integer> enumList = enumerationTypeDefinition.getElements().stream().filter(e -> e.getName().equals(name)).map(e -> e.getValue()).collect(Collectors.toList());
            enumValue = (enumList.size() > 0)  ? enumList.get(0) : null;
        }
        return enumValue;
    }

    /**
     * A utility method that gets name of a field within a type provided URNs for type and field
     * @param typeURN - URN for the type
     * @param fieldURN - URN for the field
     * @return String representation of the field
     */
    public String getTypeFieldNameFromURN(String typeURN, String fieldURN) {
        String fieldName = null;
        DefinitionResolver definitionResolver = this.metaDictionaryCollection;
        Definition definition = definitionResolver.getDefinition(AUID.fromURN(typeURN));
        if(definition == null ) {
            return null;
        } else if (definition instanceof RecordTypeDefinition) {
            RecordTypeDefinition recordTypeDefinition = RecordTypeDefinition.class.cast(definition);
            List<String> enumList = recordTypeDefinition.getMembers().stream().filter(e -> e.getType().equals(AUID.fromURN(fieldURN))).map(e -> e.getName()).collect(Collectors.toList());
            fieldName = (enumList.size() > 0)  ? enumList.get(0) : null;
        }
        return fieldName;
    }
}
