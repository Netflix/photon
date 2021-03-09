/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.imflibrary.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Proxy Interface for Files, S3 Objects etc..
 */
public interface Locator {

    /**
     * A Filter for locators.
     */
    @FunctionalInterface
    public static interface Filter {

        /**
         * Checks to see if the locator should be accepted by this filter.
         *
         * @param locator the locator to check
         *
         * @return true if this locator matches the test.
         */
        boolean accept(Locator locator);
    }

    /**
     * A Configuration interface. Allows configuring extra properties for
     * Locators.
     */
    public static interface Configuration {

        /**
         * Returns the value for a configuration property.
         *
         * @param name The name of the property to get the value for.
         *
         * @return the value for a configuration property.
         */
        String getValue(String name);
    }

    /**
     * Reads <code>length</code> bytes from an <code>InputStream</code> and
     * returns a byte array containing the bytes read.
     *
     * @param length The number of bytes to read
     * @param is     The stream to read from
     *
     * @return a byte array containing the bytes read from the InputStream
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    static byte[] toByteArray(int length, final InputStream is) throws IOException {
        int n = 0;
        int offset = 0;
        byte[] b = new byte[length];
        while (length > 0 && (n = is.read(b, offset, length)) != -1) {
            offset += n;
            length -= n;
        }
        return b;
    }

    /**
     * Returns a new Locator instance.
     *
     * @param location      the location to proxy.
     * @param configuration locator configuration.
     *
     * @return Either a <code>FileLocator</code> or <code>S3Locator</code>
     */
    public static Locator of(String location, Configuration configuration) {
        if (location.startsWith("s3://") || location.startsWith("http://") || location.startsWith("https://")) {
            return new S3Locator(location, configuration);
        }
        return new FileLocator(location, configuration);
    }

    /**
     * Returns a new Locator instance from a URI.
     *
     * @param uri           An absolute, hierarchical URI with a scheme equal to
     *                      <tt>"file"</tt> or <tt>"s3"</tt>
     * @param configuration locator configuration.
     *
     * @return Either a <code>FileLocator</code> or <code>S3Locator</code>
     *
     * @throws NullPointerException If <tt>uri</tt> is <tt>null</tt>
     * @throws IllegalArgumentException If the preconditions on the parameter do
     *                                  not hold
     */
    public static Locator of(URI uri, Configuration configuration) {
        if ("s3".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
            return new S3Locator(uri, configuration);
        }
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new FileLocator(uri, configuration);
        }
        throw new IllegalArgumentException("URI scheme is not \"file\" or \"s3\"");
    }

    /**
     * Obtain all the locators from the main arguments, storing any options in a
     * Configuration object.
     *
     * @param args             The command line arguments.
     * @param numberOfLocators Consumer to call with the number of locators
     *                         found.
     *
     * @return An array containing the Locators
     */
    public static Locator[] all(final String[] args, Consumer<Integer> numberOfLocators) {
        final Map<String, String> values = new HashMap<>();
        final List<String> names = new LinkedList<>();
        int i = 0;
        for (String a : args) {
            if (a.startsWith("--")) {
                int idx = a.indexOf('=');
                if (idx > 0) {
                    values.put(a.substring(2, idx), a.substring(idx + 1));
                } else {
                    values.put(a, null);
                }                
            } else {
                names.add(a);
                i++;
            }
        }
        Configuration config = new Configuration() {
            @Override
            public String getValue(String name) {
                return values.get(name);
            }
        };
        numberOfLocators.accept(i);
        Locator[] locators = new Locator[i];
        i = 0;
        for (String n : names) {
            locators[i++] = of(n, config);
        }
        return locators;
    }

    /**
     * Obtain the first locator from the main arguments, storing any options in
     * a Configuration object.
     *
     * @param args             The command line arguments.
     * @param numberOfLocators Consumer to call with the number of locators
     *                         found.
     *
     * @return The Locator of the 1st argument that is not an option.
     */
    public static Locator first(final String[] args, Consumer<Integer> numberOfLocators) {
        return all(args, numberOfLocators)[0];
    }

    /**
     * Return the configuration that was passed into the constructor.
     *
     * @return the configuration.
     */
    public Configuration getConfiguration();

    /**
     * Returns the name or directory for this location. This is just the last
     * name in the locations name sequence. If the location's name is empty,
     * then an empty string is returned.
     *
     * @return The name or directory for this location.
     */
    public String getName();

    /**
     * Tests whether this location exists.
     *
     * @return <code>true</code> if and if this location exists;
     *         <code>false</code> otherwise
     */
    public boolean exists();

    /**
     * Returns the ResourceByteRangeProvider representation for this Locator.
     *
     * @return the ResourceByteRangeProvider representation for this Locator
     */
    public ResourceByteRangeProvider getResourceByteRangeProvider();

    /**
     * The absolute pathname string for this location.
     *
     * @return The absolute pathname string for this location.
     */
    public String getAbsolutePath();

    /**
     * Tests whether this location is a Directory.
     *
     * @return <code>true</code> if and only if the location exists <em>and</em>
     *         is a directory; <code>false</code> otherwise
     */
    public boolean isDirectory();

    /**
     * Returns an array of Locators denoting the locations in the directory as
     * denoted by this location.
     *
     * @param filter A Filter (pass in null for no filtering to occur).
     *
     * @return An array of Locators.
     */
    public Locator[] listFiles(Filter filter);

    /**
     * Constructs a suitable URI that represents this location.
     *
     * @return A suitable URI that represents this location
     */
    public URI toURI();

    /**
     * Constructs a new Locator from a parent Locator being this and a child
     * pathname string.
     *
     * @param child The child pathname string
     *
     * @return A new locator.
     */
    public Locator getChild(String child);

    /**
     * Returns a new Locator from the parent folder of this Locator or
     * <code>null</code> if this locator does not have a parent folder.
     *
     * @return The parent as a Locator, or <code>null</code> is this locator
     *         does not have a parent.
     */
    public Locator getParent();

    /**
     * Reads all the bytes from this locator.
     *
     * @return a byte array containing the bytes read from the Locator.
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public byte[] readAllBytes() throws IOException;

    /**
     * Reads a range of bytes from this locator and returns a byte array
     * containing the bytes read.
     *
     * @param start The start position (inclusive)
     * @param end   The end position (inclusive)
     *
     * @return a byte array containing the bytes read
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public byte[] readBytes(long start, long end) throws IOException;

    /**
     * Returns the length of the object denoted by this locator.
     *
     * @return The length, in bytes of the object denoted by this locator.
     */
    public long length();

    /**
     * Creates the directory named by this locator.
     *
     * @return <code>true</code> if and only if the directory was created;
     *         <code>false</code> otherwise
     */
    public boolean mkdir();

    /**
     * Converts this locator into a pathname string
     *
     * @return The string form of this locator.
     */
    public String getPath();
}
