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

package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.exceptions.MXFException;
import org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType;

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

public final class IMFCPLFactory {

    /*Prevent instantiation*/
    private IMFCPLFactory (){

    }

    /**
     * A factory method that constructs a CompositionPlaylistType object and  recursively constructs all of its constituent fields.
     * Note: Fields that are either Java primitives, wrapperTypes or a subclass of Collection
     * are not constructed by this method for the reason that the field's accessor methods would do so.
     *
     * @return A CompositionPlaylistType object
     */
    public static CompositionPlaylistType constructCompositionPlaylistType(){
        CompositionPlaylistType cplType = new CompositionPlaylistType();
        IMFCPLFactory.constructObjectFields(cplType);
        return cplType;
    }

    private static void constructObjectFields(Object object) {
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            Object value = null;
            for (Field field : fields) {
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
        Set<Class<?>> setOfPrimitiveTypes = getJavaPrimitiveTypes();
        boolean result = false;
        for(Class clazz: setOfPrimitiveTypes){
            if(clazz.getSimpleName().equals(type.getSimpleName())){
                result = true;
                break;
            }
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
        Set<Class<?>> setOfWrapperTypes = getJavaWrapperTypes();
        boolean result = false;
        for(Class clazz: setOfWrapperTypes){
            if(clazz.getSimpleName().equals(type.getSimpleName())){
                result = true;
                break;
            }
        }
        return result;
    }
}
