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
            stringBuilder.append(iterator.next());
            stringBuilder.append("%n");
        }
        return stringBuilder.toString();
    }
}
