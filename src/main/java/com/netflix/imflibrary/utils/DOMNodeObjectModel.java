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
package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.header.UL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that implements the logic of representing a DOM Node into a Hierarchical Hash Map
 */
public class DOMNodeObjectModel {
    /*See definitions of NodeType in package org.w3c.dom.Node*/
    @Nonnull
    private final Node node;
    @Nonnull
    private final Short nodeType;
    /*Node's Local Name*/
    @Nonnull
    private final String localName;
    @Nonnull
    private final String localNamespaceURI;
    /*List of child ElementDOMNodes*/
    private final Map<DOMNodeObjectModel, Integer> childrenDOMNodes = new LinkedHashMap<>();
    /*Store for the Key-Value pairs corresponding of the Text Nodes of this ElementDOMNode*/
    private final Map<DOMNodeElementTuple, Map<String, Integer>> fields = new HashMap<>();
    private final Map<String, Map<String, Integer>> fieldsLocalNameMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DOMNodeObjectModel.class);
    private final IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
    /**
     * A constructor for the object model of a DOM Node.
     * @param node the DOM Node whose object model is desired.
     */
    public DOMNodeObjectModel(@Nonnull Node node){
        this.node = node;
        this.nodeType = node.getNodeType();
        this.localName = node.getLocalName();
        this.localNamespaceURI = node.getNamespaceURI();
        if(this.localName == null){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                            .IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("DOM Node Local Name is not set for a node of type %d", node.getNodeType()));
            return;
        }
        Node child = node.getFirstChild();
        switch(child.getNodeType()){
            case Node.ELEMENT_NODE:
                while(child != null) {
                    Node grandChild = child.getFirstChild();
                    if (grandChild != null){
                        if(grandChild.getNodeType() == Node.TEXT_NODE) {
                            DOMNodeElementTuple domNodeElementTuple = new DOMNodeElementTuple(child.getNamespaceURI(), child.getLocalName());
                            Map<String, Integer> values = fields.get(domNodeElementTuple);
                            Map<String, Integer> fieldsLocalNameValues = fieldsLocalNameMap.get(domNodeElementTuple.getLocalName());
                            if (values == null) {
                                values = new HashMap<String, Integer>();
                                fields.put(domNodeElementTuple, values);
                            }
                            if(fieldsLocalNameValues == null){
                                fieldsLocalNameValues = new HashMap<String, Integer>();
                                fieldsLocalNameMap.put(domNodeElementTuple.getLocalName(), fieldsLocalNameValues);
                            }
                            Integer count = 0;
                            if(values.containsKey(child.getFirstChild().getNodeValue())) {
                                count = values.get(child.getFirstChild().getNodeValue());
                            }
                            values.put(child.getFirstChild().getNodeValue(), count+1);
                            Integer localNameCount = 0;
                            if(fieldsLocalNameValues.containsKey(domNodeElementTuple.getNamespaceURI())){
                                localNameCount = fieldsLocalNameValues.get(domNodeElementTuple.getNamespaceURI());
                            }
                            fieldsLocalNameValues.put(domNodeElementTuple.getNamespaceURI(), localNameCount+1);
                        } else {
                            Integer count = 0;
                            DOMNodeObjectModel domNode = new DOMNodeObjectModel(child);
                            if(childrenDOMNodes.containsKey(domNode))
                            {
                                count = childrenDOMNodes.get(domNode);
                            }
                            childrenDOMNodes.put(domNode, count+1);
                            imfErrorLogger.addAllErrors(domNode.getErrors());
                        }
                    }
                    child = child.getNextSibling();
                }
                break;
            case Node.COMMENT_NODE:
                //Ignore comment nodes
                break;
            default:
                String message = String.format("Unsupported DOM Node type  %d ", child.getNodeType());
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger
                        .IMFErrors.ErrorLevels.FATAL,
                        message);
                throw new IMFException(message, imfErrorLogger);
        }
    }

    private DOMNodeObjectModel(Node node, String localName, String localNamespaceURI, short nodeType, Map<DOMNodeObjectModel, Integer> childrenDOMNodes, Map<DOMNodeElementTuple, Map<String,
            Integer>> fields,
                               Map<String, Map<String, Integer>> fieldsLocalNamesMap){
        this.node = node;
        this.localName = localName;
        this.localNamespaceURI = localNamespaceURI;
        this.nodeType = nodeType;
        this.childrenDOMNodes.putAll(childrenDOMNodes);
        this.fields.putAll(fields);
        this.fieldsLocalNameMap.putAll(fieldsLocalNamesMap);
    }

    /**
     * A static factory method that will create a DOMNodeObjectModel without the fields that were set to be ignored
     * @param domNodeObjectModel a DOMNodeObjectModel object to derive the statically constructed model from
     * @param ignoreSet a non-null, empty or non-empty set of strings representing the local names of the DOM Node elements
     *                  that should be excluded in from the newly minted DOMNodeObjectModel
     * @return a DOMNodeObjectModel that excludes the elements indicated in the ignore set.
     */
    public static DOMNodeObjectModel createDOMNodeObjectModelIgnoreSet(DOMNodeObjectModel domNodeObjectModel, @Nonnull Set<String> ignoreSet){


        Map<DOMNodeElementTuple, Map<String, Integer>> thisFields = new HashMap<>();
        for(Map.Entry<DOMNodeElementTuple, Map<String, Integer>> entry : domNodeObjectModel.getFields().entrySet()){
            if(!ignoreSet.contains(entry.getKey().getLocalName())){
                thisFields.put(entry.getKey(), entry.getValue());
            }
        }


        Map<String, Map<String, Integer>> thisFieldsLocalNamesMap = new HashMap<>();
        for(Map.Entry<String, Map<String, Integer>> entry : domNodeObjectModel.getFieldsLocalNameMap().entrySet()){
            if(!ignoreSet.contains(entry.getKey())){
                thisFieldsLocalNamesMap.put(entry.getKey(), entry.getValue());
            }
        }

        Map<DOMNodeObjectModel, Integer> childrenDOMNodes = new HashMap<>();
        for(Map.Entry<DOMNodeObjectModel, Integer> entry : domNodeObjectModel.getChildrenDOMNodes().entrySet()){
            if(!ignoreSet.contains(entry.getKey().getLocalName()))
            {
                DOMNodeObjectModel child = entry.getKey().createDOMNodeObjectModelIgnoreSet(entry.getKey(), ignoreSet);
                if (child.getChildrenDOMNodes().size() > 0
                        || child.getFields().size() > 0) {
                    childrenDOMNodes.put(child, entry.getValue());
                }
            }
        }
        return new DOMNodeObjectModel(domNodeObjectModel.getNode(), domNodeObjectModel.getLocalName(), domNodeObjectModel.getLocalNamespaceURI(),
                domNodeObjectModel.getNodeType(), Collections.unmodifiableMap(childrenDOMNodes), Collections.unmodifiableMap(thisFields), Collections.unmodifiableMap(thisFieldsLocalNamesMap));
    }

    /**
     * A static factory method that will create a DOMNodeObjectModel with the selected fields only
     * @param domNodeObjectModel a DOMNodeObjectModel object to derive the statically constructed model from
     * @param selectionSet a non-null, empty or non-empty set of strings representing the local names of the DOM Node elements
     *                  that should be included in the newly minted DOMNodeObjectModel
     * @return a DOMNodeObjectModel that contains only the elements indicated in the selection set.
     */
    public static DOMNodeObjectModel createDOMNodeObjectModelSelectionSet(DOMNodeObjectModel domNodeObjectModel, @Nonnull Set<String> selectionSet){


        Map<DOMNodeElementTuple, Map<String, Integer>> thisFields = new HashMap<>();
        for(Map.Entry<DOMNodeElementTuple, Map<String, Integer>> entry : domNodeObjectModel.getFields().entrySet()){
            if(selectionSet.contains(entry.getKey().getLocalName())){
                thisFields.put(entry.getKey(), entry.getValue());
            }
        }


        Map<String, Map<String, Integer>> thisFieldsLocalNamesMap = new HashMap<>();
        for(Map.Entry<String, Map<String, Integer>> entry : domNodeObjectModel.getFieldsLocalNameMap().entrySet()){
            if(selectionSet.contains(entry.getKey())){
                thisFieldsLocalNamesMap.put(entry.getKey(), entry.getValue());
            }
        }

        Map<DOMNodeObjectModel, Integer> childrenDOMNodes = new HashMap<>();
        for(Map.Entry<DOMNodeObjectModel, Integer> entry : domNodeObjectModel.getChildrenDOMNodes().entrySet()){
            if(selectionSet.contains(entry.getKey().getLocalName()))
            {
                DOMNodeObjectModel child = entry.getKey().createDOMNodeObjectModelSelectionSet(entry.getKey(), selectionSet);
                if (child.getChildrenDOMNodes().size() > 0
                        || child.getFields().size() > 0) {
                    childrenDOMNodes.put(child, entry.getValue());
                }
            }
        }
        return new DOMNodeObjectModel(domNodeObjectModel.getNode(), domNodeObjectModel.getLocalName(), domNodeObjectModel.getLocalNamespaceURI(),
                domNodeObjectModel.getNodeType(), Collections.unmodifiableMap(childrenDOMNodes), Collections.unmodifiableMap(thisFields), Collections.unmodifiableMap(thisFieldsLocalNamesMap));
    }

    /**
     * A static factory method that will create a DOMNodeObjectModel without the fields that were set to be ignored
     * @param domNodeObjectModel a DOMNodeObjectModel object to derive the statically constructed model from
     * @return a DOMNodeObjectModel that excludes the elements indicated in the ignore set.
     */
    private static DOMNodeObjectModel createDOMNodeObjectModelWOFullyQualifiedFields(DOMNodeObjectModel domNodeObjectModel){

        Set<Map.Entry<DOMNodeElementTuple, Map<String, Integer>>> entries = domNodeObjectModel.getFields().entrySet();
        Iterator<Map.Entry<DOMNodeElementTuple, Map<String, Integer>>> iterator = entries.iterator();
        Map<DOMNodeElementTuple, Map<String, Integer>> thisFields = new HashMap<>();

        while(iterator.hasNext()){
            Map.Entry<DOMNodeElementTuple, Map<String, Integer>> entry = iterator.next();
            DOMNodeElementTuple newKey = new DOMNodeElementTuple("", entry.getKey().getLocalName());
            thisFields.put(newKey, entry.getValue());
        }

        Map<DOMNodeObjectModel, Integer> childrenDOMNodes = new HashMap<>();
        Iterator<Map.Entry<DOMNodeObjectModel, Integer>> childEntriesIterator = domNodeObjectModel.getChildrenDOMNodes().entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<DOMNodeObjectModel, Integer> entry = childEntriesIterator.next();
            DOMNodeObjectModel child = DOMNodeObjectModel.createDOMNodeObjectModelWOFullyQualifiedFields(entry.getKey());
            childrenDOMNodes.put(child, entry.getValue());
        }
        return new DOMNodeObjectModel(domNodeObjectModel.getNode(), domNodeObjectModel.getLocalName(), domNodeObjectModel.getLocalNamespaceURI(),
                domNodeObjectModel.getNodeType(), Collections.unmodifiableMap(childrenDOMNodes), Collections.unmodifiableMap(thisFields), Collections.unmodifiableMap(domNodeObjectModel.getFieldsLocalNameMap()));
    }

    /**
     * A stateless method that can find a matching DOMNodeObjectModel given a Reference DOMNodeObjectModel and a list
     * of DOMNodeObjectModel objects
     * @param reference a DOMNodeObjectModel whose matching object needs to be found in the list
     * @param models a list of DOMNodeObjectModel objects exactly one of which should match the reference
     * @return a DOMNodeObjectModel corresponding to the DOMNodeObjectModel in the list that matches the reference
     */
    @Nullable
    public static DOMNodeObjectModel getMatchingDOMNodeObjectModel(DOMNodeObjectModel reference, Collection<DOMNodeObjectModel> models){

        DOMNodeObjectModel refDOMNodelObjectModelWONamespaceURI = DOMNodeObjectModel.createDOMNodeObjectModelWOFullyQualifiedFields(reference);
        for(DOMNodeObjectModel model : models){
            DOMNodeObjectModel modelWONamespaceURI = DOMNodeObjectModel.createDOMNodeObjectModelWOFullyQualifiedFields(model);
            if(refDOMNodelObjectModelWONamespaceURI.equals(modelWONamespaceURI)){
                return modelWONamespaceURI;
            }
        }
        return null;
    }


    /**
     * A method to log errors related to NamespaceURI inconsistencies for DOMNode elements
     * @param reference a DOMNodeObjectModel to compare against
     * @param other a DOMNodeObjectModel object which needs to be compared against the reference
     * @return a list of errors related to NamespaceURI mismatches between the reference and other DOMNodeObjectModel object
     */
    public static List<ErrorLogger.ErrorObject> getNamespaceURIMismatchErrors(DOMNodeObjectModel reference, DOMNodeObjectModel other){

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(reference == null
                || other == null){
            return imfErrorLogger.getErrors();
        }
        Iterator<Map.Entry<String, Map<String, Integer>>> fieldsLocalNamesIterator = reference.getFieldsLocalNameMap().entrySet().iterator();
        while(fieldsLocalNamesIterator.hasNext()){
            Map.Entry<String, Map<String, Integer>> entry = fieldsLocalNamesIterator.next();
            Map<String, Integer> thisFieldsLocalNameValues = entry.getValue();
            Map<String, Integer> otherFieldsLocalNameValues = other.getFieldsLocalNameMap().get(entry.getKey());
            if(!thisFieldsLocalNameValues.equals(otherFieldsLocalNameValues)){
                if(otherFieldsLocalNameValues != null) {
                    Iterator<Map.Entry<String, Integer>> iterator = thisFieldsLocalNameValues.entrySet().iterator();
                    StringBuilder stringBuilder1 = new StringBuilder();
                    while(iterator.hasNext()){
                        Map.Entry<String, Integer> thisEntry = iterator.next();
                        stringBuilder1.append(String.format("NameSpaceURI %s, %d times", thisEntry.getKey(), thisEntry.getValue()));
                    }

                    Iterator<Map.Entry<String, Integer>> iterator2 = otherFieldsLocalNameValues.entrySet().iterator();
                    StringBuilder stringBuilder2 = new StringBuilder();
                    while(iterator2.hasNext()){
                        Map.Entry<String, Integer> thisEntry = iterator2.next();
                        stringBuilder2.append(String.format("NameSpaceURI %s, %d times", thisEntry.getKey(), thisEntry.getValue()));
                    }
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("The DOMNodeElement represented by the local name %s, has namespace URI inconsistencies " +
                                            "in one DOM Node it appears with the %s " +
                                            ", in the other DOM Node it appears with the %s"
                                    , entry.getKey()
                                    , stringBuilder1.toString()
                                    , stringBuilder2.toString()));
                }
                else{
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("The DOMNodeElement represented by the local name %s, is absent in the other DOMNodeObjectModel", entry.getKey()));
                }
            }
        }
        Iterator<Map.Entry<DOMNodeObjectModel, Integer>>iterator = reference.getChildrenDOMNodes().entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<DOMNodeObjectModel, Integer>entry = iterator.next();
            Set<DOMNodeObjectModel>otherModels = other.getChildrenDOMNodes().keySet();
            DOMNodeObjectModel matchingDOMNodeObjectModel = DOMNodeObjectModel.getMatchingDOMNodeObjectModel(entry.getKey(), otherModels);
            imfErrorLogger.addAllErrors(DOMNodeObjectModel.getNamespaceURIMismatchErrors(entry.getKey(), matchingDOMNodeObjectModel));

        }
        return imfErrorLogger.getErrors();
    }

    /**
     * A getter for the DOM node represented by this DOMNodeObjectModel
     * @return a org.w3c.dom node
     */
    public Node getNode(){
        return this.node;
    }

    /**
     * A getter for the DOM node type represented by this object model.
     * @return a short representing the DOM Node type as defined in org.w3c.dom.Node
     */
    public short getNodeType(){
        return this.nodeType;
    }

    /**
     * A getter for the DOM Node local name represented by this object model.
     * @return a string representing the DOM Node's local name field.
     */
    public String getLocalName(){
        return this.localName;
    }

    /**
     * A getter for the DOM Node namespace URI represented by this object model.
     * @return a string representing the DOM Node's namespace URI field.
     */
    public String getLocalNamespaceURI(){
        return this.localNamespaceURI;
    }

    /**
     * A getter for the Fields represented in the DOMNodeObjectModel
     * @return a map of Key, Value pairs corresponding to the fields on the DOM Node
     */
    public Map<DOMNodeElementTuple, Map<String, Integer>> getFields(){
        return Collections.unmodifiableMap(this.fields);
    }

    /**
     * A getter for the Fields Local Name Map represented in the DOMNodeObjectModel
     * @return a map of Key, Value pairs corresponding to the fieldsLocalName and the corresponding NamespaceURIs
     */
    public Map<String, Map<String, Integer>> getFieldsLocalNameMap(){
        return Collections.unmodifiableMap(this.fieldsLocalNameMap);
    }

    /**
     * A getter for the Fields represented in the DOMNodeObjectModel
     * @return a map of Key, Value pairs corresponding to the fields on the DOM Node
     */
    public Map<DOMNodeObjectModel, Integer> getChildrenDOMNodes(){
        return Collections.unmodifiableMap(this.childrenDOMNodes);
    }


    /**
     * A getter for the list of errors that occurred while constructing this DOMNodeObjectModel
     * @return an unmodifiable list of Errors
     */
    public List<ErrorLogger.ErrorObject> getErrors(){
        return imfErrorLogger.getErrors();
    }

    /**
     * A method to compare 2 DOMObjectNodeModel objects to verify if 2 DOM Nodes have the same
     * content.
     * @param other the node to compare with.
     * @return boolean returns true if the 2 DOMNodeObjectModels have the same content.
     */
    @Override
    public boolean equals(Object other){

        if(other == null
                || this.getClass() != other.getClass()){
            return false;
        }

        DOMNodeObjectModel otherDOMNodeObjectModel = (DOMNodeObjectModel) other;

        if(this.nodeType.equals(otherDOMNodeObjectModel.nodeType) &&
            this.fields.equals(otherDOMNodeObjectModel.fields) &&
            this.childrenDOMNodes.equals(otherDOMNodeObjectModel.childrenDOMNodes)) {
            return true;
        }
        return false;
    }

    /**
     * A method to remove nodes from a DOMNodeObjectNodel that are also present in another DOMNodeObjectModel.
     * @param other DoMNodeObjectModel
     * @return DOMNodeObjectModel this DOMNodeObjectModel without any common nodes from other DoMNodeObjectModel
     */
    public DOMNodeObjectModel removeNodes(Object other){

        if(other == null
                || this.getClass() != other.getClass()){
            return createDOMNodeObjectModelIgnoreSet(this, new HashSet<>());
        }


        DOMNodeObjectModel otherDOMNodeObjectModel = (DOMNodeObjectModel) other;

        if(!this.localName.equals(otherDOMNodeObjectModel.localName)) {
            return createDOMNodeObjectModelIgnoreSet(this, new HashSet<>());
        }

        Map<DOMNodeElementTuple, Map<String, Integer>> thisFields = new HashMap<>();
        thisFields.putAll(this.fields);
        thisFields.entrySet().removeAll(otherDOMNodeObjectModel.fields.entrySet());

        Map<DOMNodeObjectModel, Integer> thisChildrenDOMNodes = new HashMap<>();
        Map<DOMNodeObjectModel, Integer> outChildrenDOMNodes = new HashMap<>();
        Map<DOMNodeObjectModel, Integer> otherChildrenDOMNodes = new HashMap<>();

        thisChildrenDOMNodes.putAll(this.childrenDOMNodes);
        thisChildrenDOMNodes.entrySet().removeAll(otherDOMNodeObjectModel.childrenDOMNodes.entrySet());

        otherChildrenDOMNodes.putAll(otherDOMNodeObjectModel.childrenDOMNodes);
        otherChildrenDOMNodes.entrySet().removeAll(this.childrenDOMNodes.entrySet());

        while(!thisChildrenDOMNodes.isEmpty()) {
            Map.Entry<DOMNodeObjectModel, Integer> entry = thisChildrenDOMNodes.entrySet().iterator().next();
            Integer minDiffCount = entry.getKey().getFieldCount();
            Map.Entry<DOMNodeObjectModel, Integer> matchingEntry = null;
            for(Map.Entry<DOMNodeObjectModel, Integer> otherEntry: otherChildrenDOMNodes.entrySet()) {
                DOMNodeObjectModel diffDOMNodeObjectModel = entry.getKey().removeNodes(otherEntry.getKey());
                if(diffDOMNodeObjectModel.getFieldCount() < minDiffCount) {
                    matchingEntry = otherEntry;
                    minDiffCount = diffDOMNodeObjectModel.getFieldCount();
                }
            }
            if(matchingEntry != null) {
                Integer remaining = entry.getValue() - matchingEntry.getValue();
                Integer count = remaining >= 0 ? matchingEntry.getValue() : entry.getValue();

                if(remaining == 0) {
                    otherChildrenDOMNodes.remove(matchingEntry.getKey());
                    thisChildrenDOMNodes.remove(entry.getKey());
                }
                else if(remaining > 0) {
                    otherChildrenDOMNodes.remove(matchingEntry.getKey());
                    thisChildrenDOMNodes.put(entry.getKey(), remaining);
                } else {
                    otherChildrenDOMNodes.put(matchingEntry.getKey(), -remaining);
                    thisChildrenDOMNodes.remove(entry.getKey());
                }

                if(!entry.getKey().equals(matchingEntry.getKey())) {
                    DOMNodeObjectModel diffDOMNodeObjectModel = entry.getKey().removeNodes(matchingEntry.getKey());
                    if(outChildrenDOMNodes.containsKey(diffDOMNodeObjectModel)) {
                        count += outChildrenDOMNodes.get(diffDOMNodeObjectModel);
                    }
                    outChildrenDOMNodes.put(diffDOMNodeObjectModel, count);
                }
            } else {
                Integer count = entry.getValue();
                if(outChildrenDOMNodes.containsKey(entry.getKey())) {
                    count += outChildrenDOMNodes.get(entry.getKey());
                }
                outChildrenDOMNodes.put(entry.getKey(), count);
                thisChildrenDOMNodes.remove(entry.getKey());
            }
        }


        return new DOMNodeObjectModel(this.getNode(), this.getLocalName(), this.getLocalNamespaceURI(),
                this.getNodeType(), Collections.unmodifiableMap(outChildrenDOMNodes), Collections.unmodifiableMap
                (thisFields), Collections.unmodifiableMap(this.fieldsLocalNameMap));
    }


    /**
     * A method that returns total number of fields in a DOMNodeObjectModel.
     * @return Integer total number of fields .
     */
    public Integer getFieldCount(){
        Integer entryCount = 0;

        entryCount += this.fields.size();

        for(Map.Entry<DOMNodeObjectModel, Integer> entry: this.childrenDOMNodes.entrySet()) {
            entryCount += entry.getKey().getFieldCount();
        }

        return entryCount;
    }

    /**
     * A Java compliant implementation of the hashCode() method
     * @return integer containing the hash code corresponding to this object
     */
    @Override
    public int hashCode(){
        int hash = 1;
        hash = hash * 31 + localName.hashCode();
        hash = hash * 31 + this.nodeType.hashCode();
        hash = hash * 31 + this.fields.hashCode();
        hash = hash * 31 + this.childrenDOMNodes.hashCode();
        return hash;
    }

    /**
     * A method that returns a string representation of a DOMNodeObjectModel object
     *
     * @return string representing the object
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<%s xmlns=\"%s\">", this.localName, this.localNamespaceURI)+"\n");
        for(Map.Entry<DOMNodeElementTuple, Map<String, Integer>> entry : fields.entrySet()){
            Map<String, Integer> values = (Map<String, Integer>)entry.getValue();
            Iterator<Map.Entry<String, Integer>> iterator = values.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, Integer> fieldValuesEntry = iterator.next();
                for(Integer i = 0; i< fieldValuesEntry.getValue(); i++) {
                    sb.append(String.format("  <%s>%s</%s xmlns=\"%s\">", entry.getKey().getLocalName(), fieldValuesEntry.getKey(), entry.getKey().getLocalName(),
                            entry.getKey().getNamespaceURI())+ "\n");
                }
            }
        }

        for(Map.Entry<DOMNodeObjectModel, Integer> domEntry : this.childrenDOMNodes.entrySet()) {
            for(Integer i = 0; i < domEntry.getValue(); i++) {
                String domNodeString = domEntry.getKey().toString();
                sb.append(domNodeString.replaceAll("(?m)(^)", "  "));
            }
        }
        sb.append(String.format("</%s>", this.localName) + "\n");
        return sb.toString();
    }

    private String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    /**
     * A method to obtain DOMNodeObjectModel with given LocalName
     * @param name the LocalName of the DOMNodeObjectModel
     * @return Returns DOMNodeObjectModel
     */
    public List<DOMNodeObjectModel> getDOMNodes(String name) {
        List<DOMNodeObjectModel> domNodeObjectModelList = new ArrayList<>();
        this.getChildrenDOMNodes().entrySet()
                .stream()
                .filter(e -> e.getKey().getLocalName().equals(name))
                .forEach( e ->
                {
                    for(int i =0; i <e.getValue(); i++)
                    {
                        domNodeObjectModelList.add(e.getKey());
                    }
                });
        return Collections.unmodifiableList(domNodeObjectModelList);
    }

    /**
     * A method to obtain DOMNodeObjectModel with given LocalName
     * @param name the LocalName of the DOMNodeObjectModel
     * @return Returns DOMNodeObjectModel
     */
    public DOMNodeObjectModel getDOMNode(String name) {
        List<DOMNodeObjectModel> domNodeObjectModelList = getDOMNodes(name);
        if(domNodeObjectModelList.size() > 0) {
            return domNodeObjectModelList.get(0);
        }
        else
            return null;
    }

    /**
     * A method to obtain value of a field within DOMNodeObjectModel as a String
     * @param name the LocalName for the field
     * @return Returns field value as a String
     */
    @Nullable
    public String getFieldAsString(String name) {
        try {
            Map<String, Integer> map = this.getFields().entrySet().stream().filter(e -> e.getKey().getLocalName().equals(name)).findFirst().get().getValue();
            if (map.size() >= 1) {
                Map.Entry<String, Integer> entry = map.entrySet().iterator().next();
                return entry.getKey();
            }
        }
        catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * A method to obtain value of a field within DOMNodeObjectModel as a UL
     * @param name the LocalName for the field
     * @return Returns field value as a UL
     */
    @Nullable
    public UL getFieldAsUL(String name) {
        String value = getFieldAsString(name);
        try {
            if(value != null)
            return UL.fromULAsURNStringToUL(value);
        }
        catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * A method to obtain value of a field within DOMNodeObjectModel as a Long
     * @param name the LocalName for the field
     * @return Returns field value as a Long
     */
    @Nullable
    public Long getFieldAsLong(String name) {
        String value = getFieldAsString(name);
        try {
            if(value != null)
                return Long.valueOf(value);
        }
        catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * A method to obtain value of a field within DOMNodeObjectModel as an Integer
     * @param name the LocalName for the field
     * @return Returns field value as an Integer
     */
    @Nullable
    public Integer getFieldAsInteger(String name) {
        String value = getFieldAsString(name);
        try {
            if(value != null)
                return Integer.valueOf(value);
        }
        catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * A method to obtain value of a field within DOMNodeObjectModel as a Fraction
     * @param name the LocalName for the field
     * @return Returns fields value as a Fraction
     */
    @Nullable
    public  Fraction getFieldAsFraction(String name) {
        String value = getFieldAsString(name);
        try {
            if(value != null)
                return Fraction.valueOf(value);
        }
        catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * A method to obtain set of String values for a field within DOMNodeObjectModel
     * @param name the LocalName for the field
     * @return Returns a set of field values
     */
    @Nullable
    public Set<String> getFieldsAsStringRecursive(String name) {
        Set<String> values = new HashSet<>();
        getFieldsAsStringRecursive(values, name);
        return values;
    }

    /**
     * A method to obtain set of String values for a field within DOMNodeObjectModel
     * @param name the LocalName for the field
     * @return Returns a set of field values
     */
    @Nullable
    void getFieldsAsStringRecursive(Set<String> values, String name) {
        try {
            Set<String> matchingValues = this.getFields().entrySet().stream().filter(e -> e.getKey().getLocalName().equals(name)).map(Map.Entry::getValue).map(Map::keySet).flatMap(Set::stream).collect(Collectors.toSet());
            values.addAll(matchingValues);
            for(DOMNodeObjectModel domNodeObjectModel:  this.getChildrenDOMNodes().keySet()) {
                domNodeObjectModel.getFieldsAsStringRecursive(values, name);
            }
            return;
        }
        catch(Exception e) {
            return;
        }
    }

    /**
     * A method to obtain set of Ul values for a field within DOMNodeObjectModel
     * @param name the LocalName for the field
     * @return Returns set of UL values
     */
    @Nullable
    public Set<UL> getFieldsAsUL(String name) {
        Set<String> values = getFieldsAsStringRecursive(name);
        try {
            Set<UL> uls = new HashSet<>();
            for(String value: values) {
                uls.add(UL.fromULAsURNStringToUL(value));
            }
            return uls;
        }
        catch(Exception e) {
            return null;
        }
    }


    /**
     * A method to obtain set of UUID values for a field within DOMNodeObjectModel
     * @param name the LocalName for the field
     * @return Returns set of UUID values
     */
    @Nullable
    public Set<UUID> getFieldsAsUUID(String name) {
        Set<String> values = getFieldsAsStringRecursive(name);
        try {
            Set<UUID> uuids = new HashSet<>();
            for(String value: values) {
                uuids.add(UUIDHelper.fromUUIDAsURNStringToUUID(value));
            }
            return uuids;
        }
        catch(Exception e) {
            return null;
        }
    }


    /**
     * A thin class modeling a DOM Node Element Key
     */
    public static class DOMNodeElementTuple {
        private final String localName;
        private final String namespaceURI;

        private DOMNodeElementTuple(@Nonnull String namespaceURI, @Nonnull String localName){
            this.namespaceURI = namespaceURI;
            this.localName = localName;
        }

        /**
         * A getter for the local name property of the fully qualified DOMNode element
         * @return string representing the local name property of a DOMNode element
         */
        public String getLocalName(){
            return this.localName;
        }

        /**
         * A getter for the namespaceURI property of the fully qualified DOMNode element
         * @return string representing the namespaceURI property of a DOMNode element
         */
        public String getNamespaceURI(){
            return this.namespaceURI;
        }

        /**
         * Overriding the equals method of Object to provide a specific implementation for this class
         * @param other the object to compared with
         * @return a boolean result of the comparison, false if the passed in object is null or not
         * of DOMNodeElementTuple type, or if the local name and namespaceURI are not equal to this object.
         */
        @Override
        public boolean equals(Object other){
            if(other == null
                    || other.getClass() != this.getClass()){
                return false;
            }
            DOMNodeElementTuple otherDOMNodeElementTuple = DOMNodeElementTuple.class.cast(other);
            boolean result = true;
            result &= this.localName.equals(otherDOMNodeElementTuple.getLocalName());
            result &= this.namespaceURI.equals(otherDOMNodeElementTuple.getNamespaceURI());
            return result;
        }

        /**
         * A Java compliant implementation of the hashCode() method
         * @return integer containing the hash code corresponding to this object
         */
        @Override
        public int hashCode(){
            int hash = 1;
            hash = hash * 31 + this.localName.hashCode(); /*LocalName can be used since it is non-null*/
            hash = hash * 31
                    + this.namespaceURI.hashCode();/*Another field that is indicated to be non-null*/
            return hash;
        }

        /**
         * toString() method of DOMNodeElementTuple
         * @return a string representing the contents of the DOMNodeElementTuple object
         */
        @Override
        public String toString(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("%nLocalName : %s", this.getLocalName()));
            stringBuilder.append(String.format("%nNamespaceURI : %s", this.getNamespaceURI()));
            return stringBuilder.toString();
        }
    }

}
