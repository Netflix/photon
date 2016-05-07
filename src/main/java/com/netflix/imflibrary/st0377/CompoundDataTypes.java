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

package com.netflix.imflibrary.st0377;

import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.MXFPropertyPopulator;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.List;

/**
 * This interface provides a namespace for defining object models for various Compound Data Types defined in st377-1:2011
 */
public interface CompoundDataTypes
{
    /**
     * This class corresponds to an object model for "Array" and "Batch" compound data types defined in st377-1:2011
     */
    final class MXFCollections
    {
        //prevent instantiation
        private MXFCollections()
        {
        }

        /**
         * Object model corresponding to a Header that represents a collection of MXF metadata sets
         */
        @Immutable
        @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
        public static final class Header
        {
            @MXFProperty(size=4) private final Long numberOfElements = null;
            @MXFProperty(size=4) private final Long sizeOfElement = null;

            /**
             * Instantiates a new collection Header.
             *
             * @param byteProvider the mxf byte provider
             * @throws IOException the iO exception
             */
            public Header(ByteProvider byteProvider) throws IOException
            {
                MXFPropertyPopulator.populateField(byteProvider, this, "numberOfElements");
                MXFPropertyPopulator.populateField(byteProvider, this, "sizeOfElement");
            }

            /**
             * Getter for the number of elements in the MXF collection of metadata sets
             *
             * @return the number of elements
             */
            public long getNumberOfElements()
            {
                return this.numberOfElements;
            }

            /**
             * Getter for the size of the header representing the collection of MXF metadata sets.
             *
             * @return the size of element
             */
            public long getSizeOfElement()
            {
                return this.sizeOfElement;
            }

            /**
             * A method that returns a string representation of a MXF Collection header object
             *
             * @return string representing the object
             */
            public String toString()
            {
                return String.format("numberOfElements = %d, sizeOfElement = %d%n", this.numberOfElements, this.sizeOfElement);
            }
        }

        /**
         * Object model for a generic MXF metadata set collection.
         * @param <E>  the type parameter
         */
        @Immutable
        public static final class MXFCollection<E>
        {
            private final Header header;
            private final List<E> entries;
            private final String name;

            /**
             * Instantiates a new MXF metadata set collection.
             *
             * @param header the header
             * @param entries the entries
             * @param name the name
             */
            public MXFCollection(Header header, List<E> entries, String name)
            {
                this.header = header;
                this.entries = java.util.Collections.unmodifiableList(entries);
                this.name = name;
            }

            /**
             * Getter for the number of MXF metadata sets in the collection
             *
             * @return the int
             */
            public int size()
            {
                return this.entries.size();
            }

            /**
             * Getter for an unmodifiable list of MXF metadata sets
             *
             * @return the entries
             */
            public List<E> getEntries()
            {
                return java.util.Collections.unmodifiableList(this.entries);
            }

            /**
             * A method that returns a string representation of a MXF collection of metadata sets object
             *
             * @return string representing the object
             */
            public String toString()
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("================== %s ======================%n", this.name));
                sb.append(this.header.toString());
                for (E entry : entries)
                {
                    if (entry instanceof byte[])
                    {
                        byte[] bytes = (byte[])entry;
                        sb.append(String.format("0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                                bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]));
                    }
                    else if (entry instanceof Number)
                    {
                        sb.append(String.format("0x%x(%d)%n", entry, entry));
                    }
                    else if (entry instanceof InterchangeObject.InterchangeObjectBO.StrongRef
                                || entry instanceof UL){
                        sb.append(String.format("%s%n", entry.toString()));
                    }
                    else
                    {
                        sb.append(entry.toString());
                    }
                }

                return sb.toString();
            }

        }

    }

    /**
     * This class corresponds to an object model for "Rational" compound data type defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    final class Rational
    {
        @MXFProperty(size=4) private final Long numerator = null;
        @MXFProperty(size=4) private final Long denominator = null;

        /**
         * Instantiates a new Rational.
         *
         * @param byteProvider the mxf byte provider
         * @throws IOException the iO exception
         */
        public Rational(ByteProvider byteProvider) throws IOException
        {
            MXFPropertyPopulator.populateField(byteProvider, this, "numerator");
            MXFPropertyPopulator.populateField(byteProvider, this, "denominator");
        }

        /**
         * Getter for the numerator of the rational
         *
         * @return the numerator
         */
        public long getNumerator()
        {
            return this.numerator;
        }

        /**
         * Getter for the denominator of the rational
         *
         * @return the denominator
         */
        public long getDenominator()
        {
            return this.denominator;
        }

        /**
         * A method to compare 2 rationals, returns true if the rationals match or false if they do not
         * Note : If the object that was passed in is not an instance of rational this method will return
         * false
         * @param other the object that this rational object should be compared with
         * @return result of comparing this rational object with the object that was passed in
         */
        public boolean equals(Object other)
        {
            if (!(other instanceof Rational))
            {
                return false;
            }

            Rational otherObject = (Rational)other;
            return (this.numerator != null) && (this.numerator.equals(otherObject.numerator))
                    && (this.denominator != null) && (this.denominator.equals(otherObject.denominator));
        }

        /**
         * A method that returns the sum of hashes corresponding to the numerator and denominator of this rational
         * @return the sum of hashes corresponding to the numerator and denominator of this rational
         */
        public int hashCode()
        {
            return numerator.hashCode() + denominator.hashCode();
        }

        /**
         * A method that returns a string representation of a Rational object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("numerator = %d, denominator = %d%n", this.numerator, this.denominator));
            return sb.toString();
        }
    }

    /**
     * This class corresponds to an object model for "Timestamp" compound data type defined in st377-1:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    final class Timestamp
    {
        @MXFProperty(size=2) private final Short year = null;
        @MXFProperty(size=1) private final Short month = null;
        @MXFProperty(size=1) private final Short day = null;
        @MXFProperty(size=1) private final Short hour = null;
        @MXFProperty(size=1) private final Short minute = null;
        @MXFProperty(size=1) private final Short second = null;
        @MXFProperty(size=1) private final Short msecByFour = null;

        /**
         * Instantiates a new Timestamp.
         *
         * @param byteProvider the input sequence of bytes
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public Timestamp(ByteProvider byteProvider) throws IOException
        {
            MXFPropertyPopulator.populateField(byteProvider, this, "year");
            MXFPropertyPopulator.populateField(byteProvider, this, "month");
            MXFPropertyPopulator.populateField(byteProvider, this, "day");
            MXFPropertyPopulator.populateField(byteProvider, this, "hour");
            MXFPropertyPopulator.populateField(byteProvider, this, "minute");
            MXFPropertyPopulator.populateField(byteProvider, this, "second");
            MXFPropertyPopulator.populateField(byteProvider, this, "msecByFour");
        }

        /**
         * A method that returns a string representation of a Timestamp object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("year = %d, month = %d, day = %d, hour = %d, minute = %d, second = %d, msec = %d%n",
                    this.year, this.month, this.day, this.hour, this.minute, this.second, this.msecByFour*4));
            return sb.toString();
        }

    }
}
