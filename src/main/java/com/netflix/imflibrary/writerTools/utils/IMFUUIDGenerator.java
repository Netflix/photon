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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A class that provides the utility of maintaining a list of UUIDs currently in use and generating an UUID for use in an
 * IMF CPL document
 */
public class IMFUUIDGenerator {

    private final Set<UUID> assignedUUIDs = new HashSet<>();
    private volatile static IMFUUIDGenerator uniqueInstance;

    /**
     * This class is a singleton, hence prevent instantiation by having a private constructor
     * @return
     */
    private IMFUUIDGenerator(){

    }

    /**
     * A double checked locking implementation of the getInstance() method to return the singleton instance
     * @return the uniqueInstance of this class
     */

    public static IMFUUIDGenerator getInstance(){
        if(uniqueInstance == null){
            synchronized (IMFUUIDGenerator.class){
                if(uniqueInstance == null){
                    uniqueInstance = new IMFUUIDGenerator();
                }
            }
        }
        return uniqueInstance;
    }

    /**
     * A method that generates a UUID
     * Note: this method guarantees uniqueness only as long as the class remains loaded
     * @return string representation of the UUID
     */
    public String getUUID(){
        //Create the UUID string
        return "urn" + ":" + "uuid" + ":" + generateUUID();
    }

    private UUID generateUUID(){
        String uuidString = "";
        UUID uuid = null;
        while(!uuidString.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
                || assignedUUIDs.contains(uuid)) {
            uuid = UUID.randomUUID();
            uuidString = uuid.toString();
        }
        assignedUUIDs.add(uuid);
        return uuid;
    }
}
