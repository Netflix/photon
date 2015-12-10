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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * An object model for the OperationalPattern defined in st377-1:2011
 */
public final class OperationalPatternHelper {

    private static final int packageComplexity_key_byte_position = 13; //Byte-14 of the OperationalPattern UL identifies the OperationPattern Package Complexity
    private static final int itemComplexity_key_byte_position = 12; //Byte-13 of the OperationalPattern UL identifies the OperationPattern Package Complexity
    //Private default constructor to prevent instantiation
    private OperationalPatternHelper(){

    }

    /**
     * An enumeration representing the Package Complexity
     */
    public static enum PackageComplexity{
        SinglePackage(0x01),
        GangedPackages(0x02),
        AlternatePackages (0x03),
        Unknown(-1);

        private final int packageComplexityKey;

        private PackageComplexity (int packageComplexityKey){
            this.packageComplexityKey = packageComplexityKey;
        }

        private int getPackageComplexityKey(){
            return this.packageComplexityKey;
        }
    }

    /**
     * An enumeration representing ItemComplexity
     */
    public static enum ItemComplexity{
        SingleItem(0x01),
        PlaylistItems(0x02),
        EditItems (0x03),
        Unknown(-1);

        private final int itemComplexityKey;

        private ItemComplexity (int itemComplexityKey){
            this.itemComplexityKey = itemComplexityKey;
        }

        private int getItemComplexityKey(){
            return this.itemComplexityKey;
        }
    }

    /**
     * Getter for the Package Complexity corresponding to the UL that is passed in
     * @param ul the universal label corresponding to the operational pattern that this file complies with
     * @return returns the Package Complexity corresponding to the Operational Pattern
     */
    public static PackageComplexity getPackageComplexity(byte[] ul){
        EnumSet<PackageComplexity> enumSet = EnumSet.copyOf(Arrays.asList(PackageComplexity.values()));
        for(PackageComplexity packageComplexity : enumSet){
            if(packageComplexity.getPackageComplexityKey() == ul[packageComplexity_key_byte_position]){
                return packageComplexity;
            }
        }
        return PackageComplexity.Unknown;
    }

    /**
     * Getter for the Item Complexity corresponding to the UL that is passed in
     * @param ul the universal label corresponding to the operational pattern that this file complies with
     * @return returns the Item Complexity corresponding to this Operational Pattern
     */
    public static ItemComplexity getItemComplexity(byte[] ul){
        EnumSet<ItemComplexity> enumSet = EnumSet.copyOf(Arrays.asList(ItemComplexity.values()));
        for(ItemComplexity itemComplexity : enumSet){
            if(itemComplexity.getItemComplexityKey() == ul[itemComplexity_key_byte_position]){
                return itemComplexity;
            }
        }
        return ItemComplexity.Unknown;
    }
}
