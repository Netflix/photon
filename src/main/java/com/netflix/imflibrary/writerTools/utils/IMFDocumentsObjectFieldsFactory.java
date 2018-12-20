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

package com.netflix.imflibrary.writerTools.utils;

import com.netflix.imflibrary.exceptions.MXFException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that implements helper methods to construct object fields within IMF documents such as AssetMap, PackingList,
 * CompositionPlaylist etc.
 */
public final class IMFDocumentsObjectFieldsFactory {

    private static final Set<Class<?>> setOfWrapperTypes = new HashSet<Class<?>>()
    {
        {
            /* Java Wrapper types */
            add(Boolean.class);
            add(Character.class);
            add(Byte.class);
            add(Short.class);
            add(Integer.class);
            add(Long.class);
            add(Float.class);
            add(Double.class);
            add(Void.class);
            add(Enum.class);
            add(BigInteger.class);
        }
    };

    private static final Set<Class<?>> setOfPrimitiveTypes = new HashSet<Class<?>>()
    {
        {

            /* Java Primitive types */
            add(boolean.class);
            add(char.class);
            add(byte.class);
            add(short.class);
            add(int.class);
            add(long.class);
            add(float.class);
            add(double.class);
            add(void.class);
            add(byte[].class);
        }
    };

    /**
     * To prevent instantiation
     */
    private IMFDocumentsObjectFieldsFactory(){

    }

    public static void constructObjectFields(Object object) {
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            Object value = null;
            for (Field field : fields) {
                // Skip synthetic fields. They don't need to be recreated
                if (field.isSynthetic())
                    continue;

                field.setAccessible(true);
                boolean isPrimitiveType = isJavaPrimitiveType(field.getType());
                boolean isJavaWrapperType = isJavaWrapperType(field.getType());
                if(!(XMLGregorianCalendar.class.isAssignableFrom(field.getType())
                        || Collection.class.isAssignableFrom(field.getType())
                        || isPrimitiveType
                        || isJavaWrapperType)) {
                    value = constructObjectByName(field.getType());
                }
                else if(XMLGregorianCalendar.class.isAssignableFrom(field.getType())){
                    value = DatatypeFactory.newInstance().newXMLGregorianCalendar();
                    continue;
                }
                else if(Collection.class.isAssignableFrom(field.getType())){
                    /**
                     * Types that wrap a collection provide access to the collection through
                     * an accessor hence negating the need to construct the collection.
                     */
                    continue;
                }
                else if(isPrimitiveType || isJavaWrapperType){
                    /**
                     * Field is a Java primitive/wrapper type, skip construction
                     */
                    continue;
                }
                /* Construct the fields of the object just constructed unless it is a JAVA primitive or String */
                if(!(/*field.getType().isPrimitive()*/
                        getJavaPrimitiveTypes().contains(field.getType())
                                || field.getType().equals(String.class))){
                    //Not one of the primitive types and not a string either
                    constructObjectFields(value);
                }
                field.set(object, value);
            }
        }
        catch(IllegalAccessException | DatatypeConfigurationException e){
            throw new MXFException(String.format("Error occurred while trying to construct %s", object.getClass().getSimpleName()));
        }
    }

    private static Object constructObjectByName(Class clazz) throws MXFException{
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return clazz.cast(constructor.newInstance());
        }
        catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e){
            throw new MXFException(String.format("Error occurred while trying to construct %s", clazz.getSimpleName()));
        }
    }

    private static Set<Class<?>> getJavaPrimitiveTypes(){

        Set<Class<?>> ret = new HashSet<Class<?>>();

        /* Java Primitive types */
        ret.add(boolean.class);
        ret.add(char.class);
        ret.add(byte.class);
        ret.add(short.class);
        ret.add(int.class);
        ret.add(long.class);
        ret.add(float.class);
        ret.add(double.class);
        ret.add(void.class);
        ret.add(byte[].class);

        return ret;
    }

    private static boolean isJavaPrimitiveType(Class type){
        boolean result = false;
        if(setOfPrimitiveTypes.contains(type)){
            result = true;
        }
        return result;
    }

    private static Set<Class<?>> getJavaWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();

        /* Java Wrapper types */
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(Enum.class);
        ret.add(BigInteger.class);

        return ret;
    }

    private static boolean isJavaWrapperType(Class type){
        boolean result = false;
        if(setOfWrapperTypes.contains(type)){
            result = true;
        }
        return result;
    }
}
