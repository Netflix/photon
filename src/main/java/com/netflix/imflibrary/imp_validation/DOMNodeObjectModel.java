package com.netflix.imflibrary.imp_validation;

import com.netflix.imflibrary.exceptions.IMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        if(this.childrenDOMNodes.size() != otherDOMNodeObjectModel.childrenDOMNodes.size()){
            return false;
        }

        if(this.fields.size() == otherDOMNodeObjectModel.fields.size()){
            return false;
        }

        for(DOMNodeObjectModel children : this.childrenDOMNodes){
            boolean intermediateResult = false;
            for(DOMNodeObjectModel otherChildren : otherDOMNodeObjectModel.childrenDOMNodes){
                if(otherChildren.getNodeType() == children.getNodeType()
                        && otherChildren.getLocalName().equals(children.getLocalName())){
                    intermediateResult |= children.equals(otherChildren);
                }
            }
            if(!intermediateResult){
                return false;
            }
        }

        if(!this.fields.equals(otherDOMNodeObjectModel.fields)){
            return false;
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
}
