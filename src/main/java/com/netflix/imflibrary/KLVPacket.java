/*
 *
 *  * Copyright 2015 Netflix, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *         http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.netflix.imflibrary;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * An object model representing a KLV packet defined in st336:2007 and utility methods to check for KLV packet definitions
 */
public final class KLVPacket
{
    /**
     * The constant BYTE_ORDER.
     */
//smpte st 377-1:2011, section 6.4.2
    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    /**
     * The constant KEY_FIELD_SIZE.
     */
//smpte st 377-1:2011, section 6.3.8
    public static final int KEY_FIELD_SIZE = 16;
    /**
     * The constant LENGTH_FIELD_SUFFIX_MAX_SIZE.
     */
//we are currently using long for storing value of length, below allows a value of the order of 2^63 (i.e., 8 * 10^18)
    public static final int LENGTH_FIELD_SUFFIX_MAX_SIZE = 8;

    private static final byte[] KLV_FILL_ITEM_KEY      = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x00, 0x03, 0x01, 0x02, 0x10, 0x01, 0x00, 0x00, 0x00};
    private static final byte[] KLV_FILL_ITEM_KEY_MASK = {   1,    1,     1,    1,    1,    1,   1,    0,    1,    1,    1,     1,    1,   1,    1,    1};

    private static final byte[] PHDR_IMAGE_METADATA_ITEM_KEY = {0x06, 0x0E, 0x2B, 0x34, 0x01, 0x02, 0x01, 0x05, 0x0E, 0x09, 0x06, 0x07, 0x01, 0x01, 0x01, 0x03};
    private static final byte[] GENERIC_STREAM_PARTITION_DATA_ELEMENT_KEY = {0x06, 0x0E, 0x2B, 0x34, 0x01, 0x01, 0x01, 0x0C, 0x0D, 0x01, 0x05, 0x09, 0x01, 0x00, 0x00, 0x00};

    /**
     * Checks if the key corresponding to the KLV packet is a KLV fill item key
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isKLVFillItem(byte[] key)
    {
        for (int i=0; i< KLVPacket.KEY_FIELD_SIZE; i++)
        {
            if((KLVPacket.KLV_FILL_ITEM_KEY_MASK[i] != 0) && (KLVPacket.KLV_FILL_ITEM_KEY[i] != key[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the key corresponding to the KLV packet is a PHDR image metadata item key
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isPHDRImageMetadataItemKey(byte[] key)
    {
        return Arrays.equals(key, KLVPacket.PHDR_IMAGE_METADATA_ITEM_KEY);
    }

    /**
     * Checks if the key corresponding to the KLV packet is a Generic Stream Partition Data Element key.
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isGenericStreamPartitionDataElementKey(byte[] key)
    {
        return Arrays.equals(key, KLVPacket.GENERIC_STREAM_PARTITION_DATA_ELEMENT_KEY);
    }


    //prevent instantiation
    private KLVPacket()
    {
    }

    /**
     * Gets the length field of the KLV packet
     *
     * @param byteProvider the mxf byte provider
     * @return the length
     * @throws IOException the iO exception
     */
    public static LengthField getLength(ByteProvider byteProvider) throws IOException
    {

        //read one byte
        int value = byteProvider.getBytes(1)[0];
        if ((value >> 7) == 0)
        {//MSB equals 0
            return new LengthField(value, 1);
        }
        else
        {//MSB equals 1
            //smpte st 336:2007, annex K
            if ((value & 0xFF) == 0xFF)
            {//forbidden value
                throw new MXFException("First byte of length field in KLV item equals 0xFF");
            }

            if ((value & 0xFF) == 0x80)
            {//non-deterministic length
                throw new MXFException("First byte of length field in KLV item equals 0x80");
            }

            int numBytesToRead = value & 0x7F;
            if (numBytesToRead > KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE)
            {
                throw new MXFException(String.format("Size of length field = %d is greater than max size = %d",
                        numBytesToRead, KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE));
            }

            byte[] byteArray = byteProvider.getBytes(numBytesToRead);
            long length = 0;
            for (long b : byteArray)
            {
                length <<= 8;
                length += (b & 0xFF);
            }
            if (length < 0)
            {
                throw new MXFException(String.format("Size of length field = 0x%x is greater than max supported size = 0x%x",
                        length, Long.MAX_VALUE));
            }

            return new LengthField(length, 1 + numBytesToRead);

        }

    }

    /**
     * A class that represents the length field of a KLV packet
     */
    public static final class LengthField
    {
        /**
         * The Value.
         */
        public final long value;
        /**
         * The Size of length field.
         */
        public final long sizeOfLengthField;

        /**
         * Instantiates a new Length field.
         *
         * @param value the value
         * @param sizeOfLengthField the size of length field
         */
        public LengthField(long value, long sizeOfLengthField)
        {
            this.value = value;
            this.sizeOfLengthField = sizeOfLengthField;
        }
    }

    /**
     * A class that represents a KLV packet header
     */
    @Immutable
    public static final class Header
    {
        private final byte[] key;
        private final long length;
        private final long sizeOfLengthField;
        private final long byteOffset;

        /**
         * Instantiates a new Header.
         *
         * @param byteProvider the mxf byte provider
         * @param byteOffset corresponding to the MXF KLV header
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public Header(ByteProvider byteProvider, long byteOffset) throws IOException
        {
            this.key = byteProvider.getBytes(KLVPacket.KEY_FIELD_SIZE);
            LengthField lengthField = KLVPacket.getLength(byteProvider);
            this.length = lengthField.value;
            this.sizeOfLengthField = lengthField.sizeOfLengthField;
            this.byteOffset = byteOffset;
        }

        /**
         * Getter for the key of the KLV packet
         *
         * @return a copy of the key array
         */
        public byte[] getKey()
        {
            return Arrays.copyOf(this.key, this.key.length);
        }

        /**
         * Getter for the value field of the KLV packet
         *
         * @return the size of the value field
         */
        public long getVSize()
        {
            return this.length;
        }

        /**
         * Getter for the size of the length field of the KLV packet
         *
         * @return the size of the length field
         */
        public long getLSize()
        {
            return this.sizeOfLengthField;
        }

        /**
         * Gets the byteOffset in the underlying resource.
         *
         * @return the byteOffset
         */
        public long getByteOffset()
        {
            return this.byteOffset;
        }

        /**
         * Getter for the size of the Key and Length fields of the KLV packet
         *
         * @return the kL size
         */
        public long getKLSize()
        {
            return KLVPacket.KEY_FIELD_SIZE + this.sizeOfLengthField;
        }

        /**
         * Checks if the category designator in the key of this KLV packet is set to the value corresponding to
         * dictionaries as defined in st336:2007
         *
         * @return the boolean
         */
        public boolean categoryDesignatorIsDictionaries()
        {
            return (this.key[4] == 0x01);
        }

        /**
         * Checks if the category designator in the key of this KLV packet is set to the value corresponding to
         * groups as defined in st336:2007
         *
         * @return the boolean
         */
        public boolean categoryDesignatorIsGroups()
        {
            return (this.key[4] == 0x02);
        }

        /**
         * Checks if the category designator in the key of this KLV packet is set to the value corresponding to
         * wrappers and containers as defined in st336:2007
         *
         * @return the boolean
         */
        public boolean categoryDesignatorIsWrappersAndContainers()
        {
            return (this.key[4] == 0x03);
        }

        /**
         * Gets the Set/Pack kind
         *
         * @return the pack kind interpreted as an Integer
         */
        public Integer getSetOrPackKindKey(){
            Byte setOrPackKind = this.key[13];
            return setOrPackKind.intValue();
        }

        /**
         * Gets the RegistryDesignator value in the key
         *
         * @return the pack kind interpreted as an Integer
         */
        public Integer getRegistryDesignator(){
            Byte registryDesignator = this.key[5]; //byte-6 of the 16 byte UL identifies the registry designator
            return registryDesignator.intValue();
        }

        /**
         * Checks if the category designator in the key corresponds to labels
         *
         * @return the boolean
         */
        public boolean categoryDesignatorIsLabels()
        {
            return (this.key[4] == 0x04);
        }

        /**
         * A method that returns a string representation of a KLV packet header object
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("================== KLVPacket Header ======================");
            sb.append("\n");
            sb.append(String.format("key = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.key[0], this.key[1], this.key[2], this.key[3], this.key[4], this.key[5], this.key[6], this.key[7],
                    this.key[8], this.key[9], this.key[10], this.key[11], this.key[12], this.key[13], this.key[14], this.key[15]));
            sb.append(String.format("length = %d%n", this.length));
            return sb.toString();
        }

    }
}

