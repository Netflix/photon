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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private final List<DOMNodeObjectModel> childrenDOMNodes = new ArrayList<>();
    /*Store for the Key-Value pairs corresponding of the Text Nodes of this ElementDOMNode*/
    private final Map<String, List<String>> fields = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DOMNodeObjectModel.class);
    private final IMFErrorLogger imfErrorLogger;

    /**
     * A constructor for the object model of a DOM Node.
     * @param node the DOM Node whose object model is desired.
     */
    public DOMNodeObjectModel(Node node){
        imfErrorLogger = new IMFErrorLoggerImpl();
        this.nodeType = node.getNodeType();
        this.localName = node.getLocalName();
        Node child = node.getFirstChild();
        switch(child.getNodeType()){
            case Node.ELEMENT_NODE:
                while(child != null) {
                    Node grandChild = child.getFirstChild();
                    if (grandChild != null){
                        if(grandChild.getNodeType() == Node.TEXT_NODE) {
                            List<String> values = fields.get(child.getLocalName());
                            if (values == null) {
                                values = new ArrayList<>();
                                fields.put(child.getLocalName(), values);
                            }
                            values.add(child.getFirstChild().getNodeValue());
                        } else {
                            childrenDOMNodes.add(new DOMNodeObjectModel(child));
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
    public Map<String, List<String>> getFields(){
        return Collections.unmodifiableMap(this.fields);
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

        /**
         * The following loops construct a Set that supports the retainAll operation. This is required since
         * the getFields() method returns an unmodifiable map, whereas retainAll() is a modifying call on a Map's entry set.
         */
        Set<Map.Entry<String, List<String>>> fieldsSet = new HashSet<>();
        for(Map.Entry<String, List<String>> entry : this.getFields().entrySet()){
            fieldsSet.add(entry);
        }

        Set<Map.Entry<String, List<String>>> otherFieldsSet = new HashSet<>();
        for(Map.Entry<String, List<String>> entry : otherDOMNodeObjectModel.getFields().entrySet()){
            otherFieldsSet.add(entry);
        }

        if(!fieldsSet.equals(otherFieldsSet)){
            return false;
        }

        if(this.childrenDOMNodes.size() != otherDOMNodeObjectModel.childrenDOMNodes.size()){
            return false;
        }

        for(DOMNodeObjectModel child : this.childrenDOMNodes){
            boolean intermediateResult = false;
            for(DOMNodeObjectModel otherChild : otherDOMNodeObjectModel.childrenDOMNodes){
                if(otherChild.getNodeType() == child.getNodeType()
                        && otherChild.getLocalName().equals(child.getLocalName())){
                    intermediateResult |= child.equals(otherChild);
                }
            }
            if(!intermediateResult){
                return false;
            }
        }
        return true;
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
                + this.nodeType.hashCode();/*Another field that is indicated to be non-null*/
        return hash;
    }


    /**
     * A method that will determine if 2 DOMNodeObjectModel objects are equivalent, i.e. the fields are same and have the same value
     * @return a boolean representing the equivalence check
     */
    public boolean equivalent(DOMNodeObjectModel other){
        boolean result = true;
        Set<Map.Entry<String, List<String>>> fieldsEntries = this.fields.entrySet();
        Iterator<Map.Entry<String, List<String>>> iterator = fieldsEntries.iterator();
        while(iterator.hasNext()){
            Map.Entry<String, List<String>> entry = iterator.next();
            String field = entry.getKey();
            if(!field.equals("InstanceUID")) {
                //The following logic is required to normalize the 2 list of values since we cannot
                //assume any ordering of values for a particular key.
                List<String> thisFieldValues = entry.getValue();
                List<String> otherFieldValues = other.fields.get(entry.getKey());
                if(otherFieldValues == null){
                    return false;
                }
                if(thisFieldValues.size() != otherFieldValues.size()){
                    return false;
                }

                thisFieldValues.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                otherFieldValues.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                result &= thisFieldValues.equals(otherFieldValues);
            }
        }

        if(this.childrenDOMNodes.size() != other.childrenDOMNodes.size()){
            return false;
        }

        boolean intermediateResult = true;
        for(DOMNodeObjectModel child : this.childrenDOMNodes){
            boolean areChildNodesEquivalent = false;
            for(DOMNodeObjectModel otherChild : other.childrenDOMNodes){
                areChildNodesEquivalent |= child.equivalent(otherChild);
            }
            intermediateResult &= areChildNodesEquivalent;
        }
        result &= intermediateResult;

        return result;
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
        for(DOMNodeObjectModel domNodeObjectModel : this.childrenDOMNodes) {
            sb.append(domNodeObjectModel.toString());
        }
        for(Map.Entry entry : fields.entrySet()){
            sb.append(String.format("%s%n", entry.toString()));
        }
        return sb.toString();
    }
}
