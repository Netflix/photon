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

package com.netflix.imflibrary;

import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.J2KExtendedCapabilities;
import com.netflix.imflibrary.st0377.header.JPEG2000PictureComponent;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that provides methods for populating fields with MXF metadata sets
 */
public final class MXFPropertyPopulator
{
    //to prevent instantiation
    private MXFPropertyPopulator()
    {
    }

    /**
     * An overloaded method that populates fields within a MXF metadata set
     *
     * @param byteProvider the mxf byte provider
     * @param object the object
     * @param fieldName the field name
     * @throws IOException the iO exception
     */
    public static void populateField(ByteProvider byteProvider, Object object, String fieldName) throws IOException
    {
        int byteArraySize = getFieldSizeInBytes(object, fieldName);
        doPopulateField(byteArraySize, byteProvider, object, fieldName);
    }

    /**
     * An overloaded method that populates fields within a MXF metadata set
     *
     * @param fieldSize the field size
     * @param byteProvider the mxf byte provider
     * @param object the object
     * @param fieldName the field name
     * @throws IOException the iO exception
     */
    public static void populateField(int fieldSize, ByteProvider byteProvider, Object object, String fieldName) throws IOException
    {
        doPopulateField(fieldSize, byteProvider, object, fieldName);
    }

    @SuppressWarnings("PMD.NcssMethodCount")
    private static void doPopulateField(int byteArraySize, ByteProvider byteProvider, Object object, String fieldName) throws IOException
    {

        try
        {
            Field field = getField(object.getClass(), fieldName);
            field.setAccessible(true);
            if (field.getType() == byte[].class)
            {
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, byteArray);
            }
            else if (field.getType() == InterchangeObject.InterchangeObjectBO.StrongRef.class)
            {
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, new InterchangeObject.InterchangeObjectBO.StrongRef(byteArray));
            }
            else if(field.getType() == UL.class){
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, new UL(byteArray));
            }
            else if (field.getType() == String.class)
            {
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                Charset charset = getFieldCharset(object, fieldName);
                field.set(object, getString(byteArray, charset));
            }
            else if (field.getType() == CompoundDataTypes.Rational.class)
            {
                CompoundDataTypes.Rational rational = new CompoundDataTypes.Rational(byteProvider);
                field.set(object, rational);
            }
            else if (field.getType() == CompoundDataTypes.Timestamp.class)
            {
                CompoundDataTypes.Timestamp timestamp = new CompoundDataTypes.Timestamp(byteProvider);
                field.set(object, timestamp);
            }
            else if (field.getType() == J2KExtendedCapabilities.class) {
                J2KExtendedCapabilities j2KExtendedCapabilities = new J2KExtendedCapabilities(byteProvider);
                field.set(object, j2KExtendedCapabilities);
            }
            else if (field.getType() == CompoundDataTypes.MXFCollections.MXFCollection.class)
            {
                CompoundDataTypes.MXFCollections.Header cHeader = new CompoundDataTypes.MXFCollections.Header(byteProvider);
                ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
                if(parameterizedType.getActualTypeArguments().length > 1)
                {
                    throw new MXFException(String.format("Found %d type arguments, however only 1 is supported at this time",
                            parameterizedType.getActualTypeArguments().length));
                }
                if ((parameterizedType.getActualTypeArguments()[0] == byte[].class) ||
                        (parameterizedType.getActualTypeArguments()[0].toString().equals("byte[]")))
                {
                    List<byte[]> cList = new ArrayList<>();
                    for (long i=0; i<cHeader.getNumberOfElements(); i++)
                    {
                        cList.add(byteProvider.getBytes((int)cHeader.getSizeOfElement()));
                    }
                    field.set(object, new CompoundDataTypes.MXFCollections.MXFCollection<>(cHeader, cList, fieldName));
                }
                else if (parameterizedType.getActualTypeArguments()[0] == Short.class) {
                    List<Short> cList = new ArrayList<>();
                    for (long i=0; i <cHeader.getNumberOfElements(); i++)
                    {
                        cList.add(getShort(byteProvider.getBytes(2), KLVPacket.BYTE_ORDER));
                    }
                    field.set(object, new CompoundDataTypes.MXFCollections.MXFCollection<>(cHeader, cList, fieldName));
                }
                else if (parameterizedType.getActualTypeArguments()[0] == Integer.class)
                {
                    List<Integer> cList = new ArrayList<>();
                    for (long i=0; i<cHeader.getNumberOfElements(); i++)
                    {
                        cList.add(getInt(byteProvider.getBytes(4), KLVPacket.BYTE_ORDER));
                    }
                    field.set(object, new CompoundDataTypes.MXFCollections.MXFCollection<>(cHeader, cList, fieldName));
                }
                else if (parameterizedType.getActualTypeArguments()[0] == InterchangeObject.InterchangeObjectBO.StrongRef.class){
                    List<InterchangeObject.InterchangeObjectBO.StrongRef> cList = new ArrayList<>();
                    for (long i=0; i<cHeader.getNumberOfElements(); i++)
                    {
                        cList.add(new InterchangeObject.InterchangeObjectBO.StrongRef(byteProvider.getBytes((int)cHeader.getSizeOfElement())));
                    }
                    field.set(object, new CompoundDataTypes.MXFCollections.MXFCollection<>(cHeader, cList, fieldName));
                }
                else if (parameterizedType.getActualTypeArguments()[0] == UL.class){
                    List<UL> cList = new ArrayList<>();
                    for (long i=0; i<cHeader.getNumberOfElements(); i++)
                    {
                        cList.add(new UL(byteProvider.getBytes((int)cHeader.getSizeOfElement())));
                    }
                    field.set(object, new CompoundDataTypes.MXFCollections.MXFCollection<>(cHeader, cList, fieldName));
                }
                else if (parameterizedType.getActualTypeArguments()[0] == JPEG2000PictureComponent.JPEG2000PictureComponentBO.class){
                    List<JPEG2000PictureComponent.JPEG2000PictureComponentBO> cList = new ArrayList<>();
                    for (long i=0; i<cHeader.getNumberOfElements(); i++)
                    {
                        cList.add(new JPEG2000PictureComponent.JPEG2000PictureComponentBO(byteProvider.getBytes((int)cHeader.getSizeOfElement())));
                    }
                    field.set(object, new CompoundDataTypes.MXFCollections.MXFCollection<>(cHeader, cList, fieldName));
                }
                else
                {
                    throw new MXFException(String.format("Found unsupported type argument = %s", parameterizedType.getActualTypeArguments()[0].toString()));
                }
            }
            else if (field.getType() == Float.class)
            {
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getFloat(byteArray, KLVPacket.BYTE_ORDER));
            }
            else if ((field.getType() == Long.class) && (byteArraySize == 8))
            {// long
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getLong(byteArray, KLVPacket.BYTE_ORDER));
            }
            else if ((field.getType() == Long.class) && (byteArraySize == 4))
            {// unsigned int
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getUnsignedIntAsLong(byteArray, KLVPacket.BYTE_ORDER));
            }
            else if ((field.getType() == Integer.class) && (byteArraySize == 4))
            {//signed int
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getInt(byteArray, KLVPacket.BYTE_ORDER));
            }
            else if ((field.getType() == Integer.class) && (byteArraySize == 2))
            {//unsigned short
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getUnsignedShortAsInt(byteArray, KLVPacket.BYTE_ORDER));
            }
            else if ((field.getType() == Short.class) && (byteArraySize == 2))
            {//signed short
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getShort(byteArray, KLVPacket.BYTE_ORDER));
            }
            else if ((field.getType() == Short.class) && (byteArraySize == 1))
            {//unsigned byte
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getUnsignedByteAsShort(byteArray));
            }
            else if ((field.getType() == Byte.class) && (byteArraySize == 1))
            {//signed byte
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getByte(byteArray));
            }
            else if ((field.getType() == Boolean.class) && (byteArraySize == 1))
            {//boolean byte
                byte[] byteArray = byteProvider.getBytes(byteArraySize);
                field.set(object, getBooleanFromByte(byteArray));
            }
            else
            {
                throw new MXFException(String.format("unknown type = %s, size = %d combination encountered for field %s",
                        field.getType().toString(), byteArraySize, fieldName));
            }

        }
        catch(NoSuchFieldException e)
        {
            throw new MXFException(e);
        }
        catch(SecurityException e)
        {
            throw new MXFException(e);
        }
        catch(IllegalAccessException e)
        {
            throw new MXFException(e);
        }
        catch(IllegalArgumentException e)
        {
            throw new MXFException(e);
        }

    }

    /**
     * Getter for a string representing the byte[]
     *
     * @param byteArray the byte array
     * @param charset the charset
     * @return the string
     */
    public static String getString(byte[] byteArray, Charset charset)
    {
        return new String(byteArray, charset);
    }

    /**
     * A utility method for converting a byte[] to a float value
     *
     * @param byteArray the byte array
     * @param byteOrder the byte order
     * @return the float
     */
    public static Float getFloat(byte[] byteArray, ByteOrder byteOrder)
    {
        return ByteBuffer.wrap(byteArray).order(byteOrder).getFloat();
    }

    /**
     * A utility method for converting a byte[] to a long value
     *
     * @param byteArray the byte array
     * @param byteOrder the byte order
     * @return the long
     */
    public static Long getLong(byte[] byteArray, ByteOrder byteOrder)
    {
        return ByteBuffer.wrap(byteArray).order(byteOrder).getLong();
    }

    /**
     * A utility method to convert an unsigned integer to a long
     *
     * @param byteArray the byte array
     * @param byteOrder the byte order
     * @return the unsigned int as long
     */
    public static Long getUnsignedIntAsLong(byte[] byteArray, ByteOrder byteOrder)
    {
        long value;
        long firstByte  = (0xFFL & ((long)byteArray[0]));
        long secondByte = (0xFFL & ((long)byteArray[1]));
        long thirdByte  = (0xFFL & ((long)byteArray[2]));
        long fourthByte = (0xFFL & ((long)byteArray[3]));
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
        {
            value = ((firstByte) | (secondByte << 8) | (thirdByte << 16) | (fourthByte << 24)) & 0xFFFFFFFFL;
        }
        else
        {
            value = ((firstByte << 24) | (secondByte << 16) | (thirdByte << 8) | (fourthByte )) & 0xFFFFFFFFL;
        }
        return value;
    }

    /**
     * A utility method for converting a byte[] to an integer
     *
     * @param byteArray the byte array
     * @param byteOrder the byte order
     * @return the int
     */
    public static Integer getInt(byte[] byteArray, ByteOrder byteOrder)
    {
        return ByteBuffer.wrap(byteArray).order(byteOrder).getInt();
    }

    /**
     * A utility method to convert a byte[] to an unsigned short
     *
     * @param byteArray the byte array
     * @param byteOrder the byte order
     * @return the unsigned short as int
     */
    public static Integer getUnsignedShortAsInt(byte[] byteArray, ByteOrder byteOrder)
    {
        int value;
        int firstByte  = (0xFF & ((int)byteArray[0]));
        int secondByte = (0xFF & ((int)byteArray[1]));
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
        {
            value = (firstByte | (secondByte << 8)) & 0xFFFF;
        }
        else
        {
            value = ((firstByte << 8) | secondByte) & 0xFFFF;
        }
        return value;
    }

    /**
     * A utility method to convert a byte[] to a short
     *
     * @param byteArray the byte array
     * @param byteOrder the byte order
     * @return the short
     */
    public static Short getShort(byte[] byteArray, ByteOrder byteOrder)
    {
        return ByteBuffer.wrap(byteArray).order(byteOrder).getShort();
    }

    /**
     * Gets unsigned byte as short
     *
     * @param byteArray the byte array
     * @return the unsigned byte as short
     */
    public static Short getUnsignedByteAsShort(byte[] byteArray)
    {
        return (short)(((int)byteArray[0]) & (0xFF));
    }

    /**
     * Gets a byte from a byte[]
     *
     * @param byteArray the byte array
     * @return the byte
     */
    public static Byte getByte(byte[] byteArray)
    {
        return ByteBuffer.wrap(byteArray).get();
    }

    /**
     * Gets boolean from byte[]
     *
     * @param byteArray the byte array
     * @return the boolean from byte
     */
    public static Boolean getBooleanFromByte(byte[] byteArray)
    {
        byte value = ByteBuffer.wrap(byteArray).order(KLVPacket.BYTE_ORDER).get();
        return ((value != 0));
    }

    /**
     * Gets the size of a field in a MXF metadata set in bytes
     *
     * @param object the object
     * @param fieldName the field name
     * @return the field size in bytes
     */
    public static int getFieldSizeInBytes(Object object, String fieldName)
    {
        int fieldSizeInBytes;
        try
        {

            Field field = getField(object.getClass(), fieldName);
            if (field.isAnnotationPresent(MXFProperty.class))
            {
                fieldSizeInBytes = field.getAnnotation(MXFProperty.class).size();

            }
            else
            {
                throw new MXFException(String.format("field %s is not annotated with %s", fieldName, MXFProperty.class.getSimpleName()));
            }
        }
        catch(NoSuchFieldException e)
        {
            throw new MXFException(e);
        }
        return fieldSizeInBytes;
    }

    /**
     * Gets the charset corresponding to a MXF metadata set field
     *
     * @param object the object
     * @param fieldName the field name
     * @return the field charset
     */
    public static Charset getFieldCharset(Object object, String fieldName)
    {
        Charset fieldCharset;
        try
        {

            Field field = getField(object.getClass(), fieldName);
            if (field.isAnnotationPresent(MXFProperty.class))
            {
                fieldCharset = Charset.forName(field.getAnnotation(MXFProperty.class).charset());
            }
            else
            {
                throw new MXFException(String.format("field %s is not annotated with %s", fieldName, MXFProperty.class.getSimpleName()));
            }
        }
        catch(NoSuchFieldException e)
        {
            throw new MXFException(e);
        }
        return fieldCharset;
    }

    private static Field getField(Class aClass, String fieldName) throws NoSuchFieldException
    {
        try
        {
            return aClass.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            Class superClass = aClass.getSuperclass();
            if (superClass == null)
            {
                throw e;
            }
            else
            {
                return getField(superClass, fieldName);
            }
        }
    }

    /**
     * Gets a list of UIDs that the Metadata set depends on
     *
     * @param interchangeObjectBO the interchange object bO
     * @return the dependent uI ds
     */
    public static List<MXFUID> getDependentUIDs(InterchangeObject.InterchangeObjectBO interchangeObjectBO)
    {
        List<MXFUID> dependentUIDs = new ArrayList<>();
        Class aClass = interchangeObjectBO.getClass();
        while (aClass != null)
        {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields)
            {
                field.setAccessible(true);
                if (field.isAnnotationPresent(MXFProperty.class))
                {
                    boolean depends = field.getAnnotation(MXFProperty.class).depends();
                    if (depends)
                    {
                        try
                        {
                            Object object = field.get(interchangeObjectBO);
                            if (object != null)
                            {
                                if (object instanceof CompoundDataTypes.MXFCollections.MXFCollection)
                                {
                                    CompoundDataTypes.MXFCollections.MXFCollection<Object> collection = (CompoundDataTypes.MXFCollections.MXFCollection<Object>) object;
                                    if(collection.getEntries().get(0) instanceof InterchangeObject.InterchangeObjectBO.StrongRef) {
                                        CompoundDataTypes.MXFCollections.MXFCollection<InterchangeObject.InterchangeObjectBO.StrongRef> collectionStrongRefs = (CompoundDataTypes.MXFCollections.MXFCollection<InterchangeObject.InterchangeObjectBO.StrongRef>) object;
                                        for (InterchangeObject.InterchangeObjectBO.StrongRef entry : collectionStrongRefs.getEntries()) {
                                            dependentUIDs.add(entry.getInstanceUID());
                                        }
                                    }
                                    else if(collection.getEntries().get(0) instanceof UL){
                                        CompoundDataTypes.MXFCollections.MXFCollection<UL> collectionULs = (CompoundDataTypes.MXFCollections.MXFCollection<UL>) object;
                                        for (UL entry : collectionULs.getEntries()) {
                                            dependentUIDs.add(entry.getULAsMXFUid());
                                        }
                                    }
                                }
                                else if(object instanceof InterchangeObject.InterchangeObjectBO.StrongRef){
                                    InterchangeObject.InterchangeObjectBO.StrongRef strongRef = (InterchangeObject.InterchangeObjectBO.StrongRef) object;
                                    dependentUIDs.add(strongRef.getInstanceUID());
                                }
                                else if(object instanceof UL){
                                    UL ul = (UL)object;
                                    dependentUIDs.add(ul.getULAsMXFUid());
                                }
                                else
                                {
                                    byte[] bytes = (byte[]) object;
                                    dependentUIDs.add(new MXFUID(bytes));
                                }
                            }

                        }
                        catch(IllegalAccessException e)
                        {
                            throw new MXFException(e);
                        }

                    }
                }
            }

            aClass = aClass.getSuperclass();
        }

        return dependentUIDs;

    }

}
