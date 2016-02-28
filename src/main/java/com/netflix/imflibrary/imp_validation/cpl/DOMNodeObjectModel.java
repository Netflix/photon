package com.netflix.imflibrary.imp_validation.cpl;

import com.netflix.imflibrary.exceptions.IMFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schakrovorthy on 2/26/16.
 */
public class DOMNodeObjectModel {
    /*See definitions of NodeType in package org.w3c.dom.Node*/
    private final short nodeType;
    /*Node's Local Name*/
    private final String localName;
    /*List of child ElementDOMNodes*/
    private final List<DOMNodeObjectModel> childrenDOMNodes = new ArrayList<>();
    /*Store for the Key-Value pairs corresponding of the Text Nodes of this ElementDOMNode*/
    private final Map<String, List<String>> fields = new LinkedHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DOMNodeObjectModel.class);

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

    public boolean isEquals(DOMNodeObjectModel other){
        /**
         * Compare the number of children DOM nodes and fields
         */
        boolean result = true;
        result &= (this.childrenDOMNodes.size() == other.childrenDOMNodes.size()) ? true : false;
        result &= (this.fields.size() == other.fields.size()) ? true : false;



        return  result;
    }
}
