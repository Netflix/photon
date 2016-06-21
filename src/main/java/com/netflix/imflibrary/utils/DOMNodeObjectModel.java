package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.IMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by schakrovorthy on 2/26/16.
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
    private final Map<String, List<String>> fields = new LinkedHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DOMNodeObjectModel.class);

    /**
     * A constructor for the object model of a DOM Node.
     * @param node the DOM Node whose object model is desired.
     */
    public DOMNodeObjectModel(Node node){
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
                throw new IMFException(String.format("Internal error occurred while constructing a DOM Node object model"));
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

        boolean result = fieldsSet.retainAll(otherFieldsSet);//If there is a change in FieldsSet then we want to see if there was atleast an 80% match of the fields
        if(result){
            /*long confidenceScore = Math.round(100 * (double)fieldsSet.size()/(thisFieldsSetSize > otherFieldsSetSize ? thisFieldsSetSize : otherFieldsSetSize));
            if(confidenceScore < 80){
                return false;
            }*/
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
