/*
 *
 * Copyright 2024 RheinMain University of Applied Sciences, Wiesbaden, Germany.
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

package com.netflix.imflibrary.st0377_41;

/**
 * This enum lists the MCA Use Class Vocabulary defined in the following specification:
 * ST 377-41:2021 Table 3
 */
public enum MCAUseClass {
	FCMP("Finished Composite"),
	ICMP("Intermediary Composite"),
	SMPL("Simplified"),
	SING("Singular"),
	Unknown("Unknown");

    String description;
    MCAUseClass(String description) {
        this.description = description;
    }

    /**
     * Getter for description for the audio content kind
     * @return a String describing the audio content kind
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method map audio content kind symbol to corresponding enum
     * @param value a Symbol representing audio content kind
     * @return AudioContentKind enumeration for the symbol if present otherwise returns Unknown enumeration
     */
    public static MCAUseClass getValueFromSymbol(String value) {
        if(value == null) {
            return Unknown;
        }
        try {
            return MCAUseClass.valueOf(value);
        } catch(IllegalArgumentException e) {
            return Unknown;
        }
    }
}
