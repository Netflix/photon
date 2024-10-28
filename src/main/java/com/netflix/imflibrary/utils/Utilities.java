package com.netflix.imflibrary.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
        stringBuilder.append(String.format("%n"));
        while(iterator.hasNext()){
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

    public static String getVersionString(Class<?> theClass)
    {
        String version = "0.0.0";
        if(theClass.getPackage() != null && theClass.getPackage().getImplementationVersion() != null) {
            return theClass.getPackage().getImplementationVersion();
        }
        return version;
    }

    public static void recursivelyDeleteFolder(Path folder) throws IOException
    {
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static String appendPhotonVersionString(String message)
    {
        return message + " [Photon version: " + Utilities.getVersionString(Utilities.class) + "]";
    }

    public static String getFilenameFromPath(Path path) throws IOException
    {
        if (!Files.isRegularFile(path)) {
            throw new IOException(String.format("%s is not a regular file", path));
        }

        Path filename = path.getFileName();
        if (filename == null) {
            throw new IOException(String.format("Unable to determine filename for path: %s", path));
        }

        return filename.toString();
    }

}
