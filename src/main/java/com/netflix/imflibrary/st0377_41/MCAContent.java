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
 * This enum lists the MCA Content Vocabulary defined in the following specification:
 * ST 377-41:2021 Table 2
 */
public enum MCAContent {
	PRM("Primary"),
	SAP("Secondary Audio Program"),
	HI("Hearing Impaired"),
	DV("Descriptive Video"),
	DX("Dialog"),
	MX("Music"),
	FX("Effects"),
	FFX("Filled Effects"),
	ME("Music and Effects"),
	OP("Optional Music and Effects"),
	MESP("Music and Effects with Optional"),
	DME("DME"),
	NDM("NDME"),
	PNA("Program Narration"),
	ONA("Optional Narration"),
	VO("Voice Over"),
	VI("Visually Impaired"),
	CM("Recorded Commentary"),
	LCM("Live Commentary"),
	MOS("Silence"),
	x("Custom"),
	Unknown("Unknown");

    String description;
    MCAContent(String description) {
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
    public static MCAContent getValueFromSymbol(String value) {
        if(value == null) {
            return Unknown;
        }
        try {
            return MCAContent.valueOf(value);
        } catch(IllegalArgumentException e) {
            return Unknown;
        }
    }
}
