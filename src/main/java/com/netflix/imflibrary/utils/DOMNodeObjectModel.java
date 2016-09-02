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
                            Map<String, Integer> values = fields.get(new DOMNodeElementTuple(child.getPrefix(), child.getLocalName()));
                            if (values == null) {
                                values = new HashMap<String, Integer>();
                                String prefix = child.getPrefix();
                                String localName = child.getLocalName();
                                DOMNodeElementTuple domNodeElementTuple = new DOMNodeElementTuple(prefix, localName);
                                fields.put(domNodeElementTuple, values);
                            }
                            Integer count = 0;
                            if(values.containsKey(child.getFirstChild().getNodeValue())) {
                                count = values.get(child.getFirstChild().getNodeValue());
                            }
                            values.put(child.getFirstChild().getNodeValue(), count+1);
                        } else {
                            Integer count = 0;
                            DOMNodeObjectModel domNode = new DOMNodeObjectModel(child);
                            if(childrenDOMNodes.containsKey(domNode))
                            {
                                count = childrenDOMNodes.get(domNode);
                            }
                            childrenDOMNodes.put(domNode, count+1);
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
    public Map<DOMNodeElementTuple, Map<String, Integer>> getFields(){
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

        if(this.localName.equals(otherDOMNodeObjectModel.localName) &&
            this.nodeType.equals(otherDOMNodeObjectModel.nodeType) &&
            this.fields.equals(otherDOMNodeObjectModel.fields) &&
            this.childrenDOMNodes.equals(otherDOMNodeObjectModel.childrenDOMNodes)) {
            return true;
        }

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
        private final String prefix;

        private DOMNodeElementTuple(String prefix, String localName){
            this.prefix = prefix;
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
         * A getter for the prefix property of the fully qualified DOMNode element
         * @return string representing the prefix property of a DOMNode element
         */
        public String getPrefix(){
            return this.prefix;
        }

        /**
         * Overriding the equals method of Object to provide a specific implementation for this class
         * @param other the object to compared with
         * @return a boolean result of the comparison, false if the passed in object is null or not
         * of DOMNodeElementTuple type, or if the local name and prefix are not equal to this object.
         */
        @Override
        public boolean equals(Object other){
            if(other == null
                    || !(this.getClass().isAssignableFrom(other.getClass()))){
                return false;
            }
            return this.localName.equals(DOMNodeElementTuple.class.cast(other).getLocalName())
                    && this.prefix.equals((DOMNodeElementTuple.class.cast(other).getPrefix());
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
