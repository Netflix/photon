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

import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.smpte_ra.schemas._2067_3._2013.BaseResourceType;
import org.smpte_ra.schemas._2067_3._2013.CompositionPlaylistType;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A class that provides utility methods to help with serializing an IMF CPL to an XML document
 */
public class IMFUtils {

    /**
     * Private constructor to prevent instantiation
     */
    private IMFUtils(){

    }

    /**
     * A utility method to create an XMLGregorianCalendar
     * @return the constructed XMLGregorianCalendar
     */
    @Nullable
    public static XMLGregorianCalendar createXMLGregorianCalendar(){
        XMLGregorianCalendar result = null;
        try {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            TimeZone utc = TimeZone.getTimeZone("UTC");
            GregorianCalendar now = new GregorianCalendar(utc);
            result = datatypeFactory.newXMLGregorianCalendar(now);
        }
        catch (DatatypeConfigurationException e){
            throw new IMFException("Could not create a XMLGregorianCalendar instance");
        }
        return result;
    }

    /**
     * A method that generates a CPL schema valid TimecodeStartAddress string
     * @return a string representing the time code start address compliant with its regex definition
     */
    public static String generateTimecodeStartAddress(){
        String delimiter = ":";
        String timeCodeStartAddress = "00:00:00:00";
        if(timeCodeStartAddress.matches("[0-2][0-9](:|/|;|,|\\.|\\+|\\-)[0-5][0-9](:|/|;|,|\\.|\\+|\\-)[0-5][0-9](:|/|;|,|\\.|\\+|\\-)[0-5][0-9]")){
            return timeCodeStartAddress;
        }
        else{
            throw new IMFException(String.format("Could not generate a valid TimecodeStartAddress based on input " +
                    "received"));
        }
    }

    /**
     * A method that generates a SHA-1 hash of the file.
     *
     * @param file - the file whose SHA-1 hash is to be generated
     * @return a byte[] representing the generated hash of the file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public static byte[] generateSHA1Hash(File file) throws IOException {
            ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(file);
            return IMFUtils.generateHash(resourceByteRangeProvider, "SHA-1");
    }

    /**
     * A method that generates a SHA-1 hash of the incoming resource.
     *
     * @param resourceByteRangeProvider representing the resource whose digest is to be generated
     * @return a byte[] representing the generated hash of the file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public static byte[] generateSHA1Hash(ResourceByteRangeProvider resourceByteRangeProvider) throws IOException {
        return IMFUtils.generateHash(resourceByteRangeProvider, "SHA-1");
    }

    /**
     * A method that generates a SHA-1 hash of the file and Base64 encode the result.
     *
     * @param file - the file whose SHA-1 hash is to be generated
     * @return a byte[] representing the generated base64 encoded hash of the file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public static byte[] generateSHA1HashAndBase64Encode(File file) throws IOException {
        return generateBase64Encode(generateSHA1Hash(file));
    }

    /**
     * A method to generate a Base64 encoded representation of a byte[]
     * @param bytes a byte[] that is to be Base64 encoded
     * @return a byte[] representing the Base64 encode of the input
     */
    public static byte[] generateBase64Encode(byte[] bytes){
        byte[] hashCopy = Arrays.copyOf(bytes, bytes.length);
        return Base64.getEncoder().encode(hashCopy);
    }

    /**
     * A method to generate a digest of the incoming resource for a given algorithm
     * @param resourceByteRangeProvider representing the resource whose digest is to be generated
     * @param hashAlgorithm the name of the hash algorithm
     * @return a byte[] representing the digest of the resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public static byte[] generateHash(ResourceByteRangeProvider resourceByteRangeProvider, String hashAlgorithm) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            long rangeStart = 0;
            long rangeEnd = (rangeStart + 1023 > resourceByteRangeProvider.getResourceSize() - 1)
                    ? resourceByteRangeProvider.getResourceSize() - 1
                    : rangeStart + 1023;

            int nread = 0;

            while (rangeStart < resourceByteRangeProvider.getResourceSize()
                    && rangeEnd < resourceByteRangeProvider.getResourceSize()) {
                byte[] dataBytes = resourceByteRangeProvider.getByteRangeAsBytes(rangeStart, rangeEnd);
                nread = (int) (rangeEnd - rangeStart + 1);
                md.update(dataBytes, 0, nread);
                rangeStart = rangeEnd + 1;
                rangeEnd = (rangeStart + 1023 > resourceByteRangeProvider.getResourceSize() - 1)
                        ? resourceByteRangeProvider.getResourceSize() - 1
                        : rangeStart + 1023;
            }
            byte[] mdbytes = md.digest();
            return Arrays.copyOf(mdbytes, mdbytes.length);
        }
        catch (NoSuchAlgorithmException e){
            throw new IMFException(e);
        }
    }

    /**
     * A method to cast the object that was passed in to the specified subclass safely
     *
     * @param <T> the type of BaseResource
     * @param baseResourceType - the object that needs to cast to the subclass of this type
     * @param cls the Class for the casted type
     * @return T casted type
     * @throws IMFException - a class cast failure is exposed through an IMFException
     */
    public static <T extends BaseResourceType> T safeCast(BaseResourceType baseResourceType, Class<T> cls) throws IMFException
    {
        if(baseResourceType == null){
            return null;
        }
        if(!cls.isAssignableFrom(baseResourceType.getClass()))
        {
            throw new IMFException(String.format("Unable to cast from Box type %s to %s", baseResourceType.getClass()
                    .getName(), cls.getName()));
        }

        return cls.cast(baseResourceType);
    }

    /**
     * A utility method that writes out the serialized IMF CPL document to a file
     *
     * @param compositionPlaylistType an instance of the composition playlist type
     * @param outputFile the file that the serialized XML is written to
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public static void writeCPLToFile(CompositionPlaylistType compositionPlaylistType, File outputFile) throws IOException {
        try {
            IMFCPLSerializer imfcplSerializer = new IMFCPLSerializer();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            imfcplSerializer.write(compositionPlaylistType, fileOutputStream, true);
            fileOutputStream.close();
        }
        catch (FileNotFoundException e){
            throw new IMFException(String.format("Error occurred while trying to serialize the CompositionPlaylistType, file %s not found", outputFile.getName()));
        }
        catch(SAXException | JAXBException e ){
            throw new IMFException(e);
        }
    }

}
