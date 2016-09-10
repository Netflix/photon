package com.netflix.imflibrary.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * A stateless class that provides general utility methods
 */
public final class Utilities {

    //To prevent instantiation
    private Utilities(){

    }

    /**
     * A method for serializing an Object collection to a String
     * @param collection - of objects to be serialized to Strings
     * @return a string representation of each of the objects in the collection
     */
    public static String serializeObjectCollectionToString(Collection<? extends Object> collection){
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iterator = collection.iterator();
        while(iterator.hasNext()){
            stringBuilder.append(String.format("%n"));
            stringBuilder.append(iterator.next().toString());
            stringBuilder.append(String.format("%n"));
        }
        return stringBuilder.toString();
    }

    /**
     * A method for serializing a byte[] to a HexString
     * @param bytes - collection of bytes
     * @return a String representing the Hexadecimal representation of the bytes in the byte[]
     */
    public static String serializeBytesToHexString(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        for(int i=0; i < bytes.length; i++){
            stringBuilder.append(String.format("%02x", bytes[i]));
        }
        return stringBuilder.toString();
    }
}
