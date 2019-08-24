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

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that implements helper methods to construct object fields within IMF documents such as AssetMap, PackingList,
 * CompositionPlaylist etc.
 */
public final class IMFDocumentsObjectFieldsFactory {

    private static final Set<Class<?>> setOfWrapperTypes = Collections.unmodifiableSet(new HashSet<Class<?>>()
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
    });

    private static final Set<Class<?>> setOfPrimitiveTypes = Collections.unmodifiableSet(new HashSet<Class<?>>()
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
    });

    /**
     * To prevent instantiation
     */
    private IMFDocumentsObjectFieldsFactory(){

    }

    public static void constructObjectFields(Object object) {
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                // Skip synthetic fields. They don't need to be recreated
                if (field.isSynthetic())
                    continue;

                Class<?> fieldType = field.getType();

                // No need to construct primitive types (int, boolean, ...)
                if (setOfPrimitiveTypes.contains(fieldType))
                    continue;

                // No need to construct wrapper types (Integer, Long, Enum, ...)
                if (setOfWrapperTypes.contains(fieldType))
                    continue;

                // No need to construct String, because it is immutable and will be set when assigned
                if (fieldType.equals(String.class))
                    continue;

                // No need to construct XMLGregorianCalendar, because an already constructed instance will be provided when assigned
                if(XMLGregorianCalendar.class.isAssignableFrom(fieldType))
                    continue;

                // Types that wrap a collection provide access to the collection through
                // an accessor hence negating the need to construct the collection.
                if(Collection.class.isAssignableFrom(fieldType))
                    continue;

                // Construct a field of a complex type, and construct that types fields recursively
                Object value = constructObjectByName(fieldType);
                constructObjectFields(value);

                // Update this field with the newly constructed object
                field.setAccessible(true);
                field.set(object, value);
            }
        }
        catch(IllegalAccessException e){
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
}
