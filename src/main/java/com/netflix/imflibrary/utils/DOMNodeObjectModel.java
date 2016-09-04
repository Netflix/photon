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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that implements the logic of representing a DOM Node into a Hierarchical Hash Map
 */
public class DOMNodeObjectModel {
    /*See definitions of NodeType in package org.w3c.dom.Node*/
    @Nonnull
    private final Short nodeType;
    /*Node's Local Name*/
    @Nonnull
    private final String localName;
    /*List of child ElementDOMNodes*/
    private final Map<DOMNodeObjectModel, Integer> childrenDOMNodes = new HashMap<>();
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
        this.nodeType = node.getNodeType();
        this.localName = node.getLocalName();
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

    private DOMNodeObjectModel(String localName, short nodeType, Map<DOMNodeObjectModel, Integer> childrenDOMNodes, Map<DOMNodeElementTuple, Map<String, Integer>> fields, Map<String, Map<String, Integer>> fieldsLocalNamesMap){
        this.localName = localName;
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

        Set<Map.Entry<DOMNodeElementTuple, Map<String, Integer>>> entries = domNodeObjectModel.getFields().entrySet();
        Iterator<Map.Entry<DOMNodeElementTuple, Map<String, Integer>>> iterator = entries.iterator();
        Map<DOMNodeElementTuple, Map<String, Integer>> thisFields = new HashMap<>();

        while(iterator.hasNext()){
            Map.Entry<DOMNodeElementTuple, Map<String, Integer>> entry = iterator.next();
            if(!ignoreSet.contains(entry.getKey().getLocalName())){
                thisFields.put(entry.getKey(), entry.getValue());
            }
        }

        Set<Map.Entry<String, Map<String, Integer>>> fieldsLocalNameEntries = domNodeObjectModel.getFieldsLocalNameMap().entrySet();
        Iterator<Map.Entry<String, Map<String, Integer>>> iteratorFieldsLocalName = fieldsLocalNameEntries.iterator();
        Map<String, Map<String, Integer>> thisFieldsLocalNamesMap = new HashMap<>();

        while(iteratorFieldsLocalName.hasNext()){
            Map.Entry<String, Map<String, Integer>> entry = iteratorFieldsLocalName.next();
            if(!ignoreSet.contains(entry.getKey())){
                thisFieldsLocalNamesMap.put(entry.getKey(), entry.getValue());
            }
        }

        Map<DOMNodeObjectModel, Integer> childrenDOMNodes = new HashMap<>();
        Set<Map.Entry<DOMNodeObjectModel, Integer>> childEntries = childrenDOMNodes.entrySet();
        Iterator<Map.Entry<DOMNodeObjectModel, Integer>> childEntriesIterator = childEntries.iterator();
        while(iterator.hasNext()){
            Map.Entry<DOMNodeObjectModel, Integer> entry = childEntriesIterator.next();
            DOMNodeObjectModel child = entry.getKey().createDOMNodeObjectModelIgnoreSet(entry.getKey(), ignoreSet);
            childrenDOMNodes.put(child, entry.getValue());
        }
        return new DOMNodeObjectModel(domNodeObjectModel.getLocalName(), domNodeObjectModel.getNodeType(), Collections.unmodifiableMap(childrenDOMNodes), Collections.unmodifiableMap(thisFields), Collections.unmodifiableMap(thisFieldsLocalNamesMap));
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
                || !other.getClass().isAssignableFrom(this.getClass())){
            return false;
        }

        DOMNodeObjectModel otherDOMNodeObjectModel = (DOMNodeObjectModel) other;

        Iterator<Map.Entry<String, Map<String, Integer>>> fieldsLocalNamesIterator = this.fieldsLocalNameMap.entrySet().iterator();
        while(fieldsLocalNamesIterator.hasNext()){
            Map.Entry<String, Map<String, Integer>> entry = fieldsLocalNamesIterator.next();
            Map<String, Integer> thisFieldsLocalNameValues = entry.getValue();
            Map<String, Integer> otherFieldsLocalNameValues = otherDOMNodeObjectModel.getFieldsLocalNameMap().get(entry.getKey());
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
                                            "in one DOM it appears with the %s " +
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

        if(this.localName.equals(otherDOMNodeObjectModel.localName) &&
            this.nodeType.equals(otherDOMNodeObjectModel.nodeType) &&
            this.fields.equals(otherDOMNodeObjectModel.fields) &&
            this.childrenDOMNodes.equals(otherDOMNodeObjectModel.childrenDOMNodes)) {
            return true;
        }
        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING, String.format("Node %s is not equal to Node %s", this.toString(), otherDOMNodeObjectModel.toString()));
        return false;
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
    }

    /**
     * A method that returns a string representation of a DOMNodeObjectModel object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("====== DOM Node Object Model ======%n"));
        sb.append(String.format("Node Type : %s%n", this.nodeType.toString()));
        sb.append(String.format("Node Local Name : %s%n", this.localName));
        for(Map.Entry<DOMNodeObjectModel, Integer> domEntry : this.childrenDOMNodes.entrySet()) {
            for(Integer i = 0; i < domEntry.getValue(); i++) {
                sb.append(domEntry.getKey().toString());
            }
        }
        for(Map.Entry entry : fields.entrySet()){
            sb.append(String.format("%s%n", entry.toString()));
        }
        return sb.toString();
    }
}
