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

import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.annotations.MXFField;
import com.netflix.imflibrary.MXFFieldPopulator;
import com.netflix.imflibrary.KLVPacket;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object model corresponding to a Random Index Pack defined in st377-1:2011
 */
@Immutable
@SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
public final class RandomIndexPack
{
    private static final byte[] KEY = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x11, 0x01, 0x00};
    private static final Integer RANDOM_INDEX_PACK_LENGTH_FIELD_SIZE = 4;//Size of the RandomIndexPackLengthField per SMPTE-ST0377-1:2011 see Section 12

    private final KLVPacket.Header header;
    private final Map<Long, List<Long>> partitionMap = new LinkedHashMap<Long, List<Long>>();
    @MXFField(size=4) private final Long length = null;

    /**
     * Instantiates a new Random index pack.
     *
     * @param byteProvider the mxf byte provider
     * @param byteOffset the byte offset corresponding to this RandomIndexPack
     * @param fullPackLength the full pack length
     * @throws IOException the iO exception
     */
    public RandomIndexPack(ByteProvider byteProvider, long byteOffset, long fullPackLength) throws IOException
    {
        this.header = new KLVPacket.Header(byteProvider, byteOffset);
        if (!Arrays.equals(this.header.getKey(), RandomIndexPack.KEY))
        {
            throw new MXFException(String.format("Expected random index pack key = %s, found %s", Arrays.asList(RandomIndexPack.KEY), Arrays.asList(this.header.getKey())));
        }

        if((fullPackLength - KLVPacket.KEY_FIELD_SIZE - this.header.getLSize()) != this.header.getVSize())
        {
            throw new MXFException(String.format("fullPackLength = %d is not consistent with length of length field = %d and length of value field = %d",
                    fullPackLength, this.header.getLSize(), this.header.getVSize()));
        }

        //Get the bytestream size of a BodySIDByteOffsetPair 2-tuple
        Integer bodySIDByteOffsetPairSize = 0;
        Field[] fields =  BodySIDByteOffsetPair.class.getDeclaredFields();
        for(Field field : fields){
            if(field.isAnnotationPresent(MXFField.class)){
                bodySIDByteOffsetPairSize += field.getAnnotation(MXFField.class).size();
            }
        }

        if (((fullPackLength - KLVPacket.KEY_FIELD_SIZE - this.header.getLSize() - RANDOM_INDEX_PACK_LENGTH_FIELD_SIZE)%bodySIDByteOffsetPairSize) != 0)
        {
            throw new MXFException(String.format("Length of BodySIDByteOffsetPairs portion of RandomIndexPack = %d is not a multiple of %d",
                    fullPackLength - KLVPacket.KEY_FIELD_SIZE - this.header.getLSize() - RANDOM_INDEX_PACK_LENGTH_FIELD_SIZE, bodySIDByteOffsetPairSize));
        }

        long numBodySIDByteOffsetPairs = (fullPackLength - KLVPacket.KEY_FIELD_SIZE - this.header.getLSize() - RANDOM_INDEX_PACK_LENGTH_FIELD_SIZE)/bodySIDByteOffsetPairSize;

        for (long i=0; i < numBodySIDByteOffsetPairs; i++)
        {
            BodySIDByteOffsetPair bodySIDByteOffsetPair = new BodySIDByteOffsetPair(byteProvider);
            List<Long> partitions = partitionMap.get(bodySIDByteOffsetPair.getBodySID());
            if (partitions == null)
            {
                partitions = new ArrayList<Long>();
                partitionMap.put(bodySIDByteOffsetPair.getBodySID(), partitions);
            }
            partitions.add(bodySIDByteOffsetPair.getByteOffset());
        }

        MXFFieldPopulator.populateField(byteProvider, this, "length");
        if (this.length != fullPackLength)
        {
            throw new MXFException(String.format("Observed length = %d is different from expected length = %d of RandomIndexPack",
                    this.length, fullPackLength));
        }

    }

    /**
     * Gets all the partition byte offsets in the MXF file
     *
     * @return the all partition byte offsets in no particular order in which they appear in the file
     */
    public List<Long> getAllPartitionByteOffsets()
    {
        ArrayList<Long> allPartitionByteOffsets = new ArrayList<Long>();
        /*for(Long key : this.partitionMap.keySet()){
            List<Long> partitions = this.partitionMap.get(key);
            if(partitions != null){
                allPartitionByteOffsets.addAll(partitions);
            }
        }*/
        /* According to FindBugs using an iterator over the Map's entrySet() is more efficient than keySet()
         * (WMI_WRONG_MAP_ITERATOR).
         * Since with the entrySet we get both the key and the value thereby eliminating the need to use
         * Map.get(key) to access the value corresponding to a key in the map.
         */
        Set<Map.Entry<Long, List<Long>>> entrySet = this.partitionMap.entrySet();
        for(Map.Entry<Long, List<Long>> entry : entrySet){
            List<Long> partitions = entry.getValue();
            if(partitions.size() > 0){
                allPartitionByteOffsets.addAll(partitions);
            }
        }
        return Collections.unmodifiableList(allPartitionByteOffsets);
    }

    /**
     * Getter for the length of the RandomIndex Pack
     *
     * @return the length
     */
    public Long getLength()
    {
        return this.length;
    }

    /**
     * A method that returns a string representation of a RandomIndex Pack object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("================== RandomIndexPack ======================\n");
        sb.append(this.header.toString());
        /*for (long sid : partitionMap.keySet())
        {
            sb.append(String.format("SID = %d%n", sid));
            long number = 0;
            for (long byteOffset : partitionMap.get(sid))
            {
                sb.append(String.format("%02d: byteOffset = %013d(0x%011x)%n", number++, byteOffset, byteOffset));
            }
        }*/
        /*
         * According to FindBugs using an iterator over the Map's entrySet() is more efficient than keySet()
         * (WMI_WRONG_MAP_ITERATOR).
         * Since with the entrySet we get both the key and the value thereby eliminating the need to use
         * Map.get(key) to access the value corresponding to a key in the map.
         */
        Set<Map.Entry<Long, List<Long>>> entrySet = this.partitionMap.entrySet();
        for(Map.Entry<Long, List<Long>> entry : entrySet){
            sb.append(String.format("SID = %d%n", entry.getKey()));
            long number = 0;
            for(long byteOffset : entry.getValue()){
                sb.append(String.format("%02d: byteOffset = %013d(0x%011x)%n", number++, byteOffset, byteOffset));
            }
        }
        sb.append(String.format("length = %d%n", this.length));
        return sb.toString();
    }

    /**
     * Object model representing the mapping of the ID of an Essence container segment and its corresponding byte offset
     */
    @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
    @Immutable
    public static final class BodySIDByteOffsetPair
    {
        @MXFField(size=4) private final Long bodySID = null;
        @MXFField(size=8) private final Long byteOffset = null;

        /**
         * Instantiates a new Body sID byte offset pair.
         *
         * @param byteProvider the mxf byte provider
         * @throws IOException the iO exception
         */
        BodySIDByteOffsetPair(ByteProvider byteProvider) throws IOException
        {
            MXFFieldPopulator.populateField(byteProvider, this, "bodySID");
            MXFFieldPopulator.populateField(byteProvider, this, "byteOffset");
        }

        private long getBodySID()
        {
            return this.bodySID;
        }

        private long getByteOffset()
        {
            return this.byteOffset;
        }
    }

}
