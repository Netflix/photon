/*
 *
 * Copyright 2016 Netflix, Inc.
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

package com.netflix.imflibrary.st2067_100.macro;

import org.smpte_ra.schemas._2067_102._2014.REC709FullRGB10ColorEncodingType;
import org.smpte_ra.schemas._2067_102._2014.REC709RGB10ColorEncodingType;
import org.smpte_ra.schemas._2067_102._2014.REC709RGB8ColorEncodingType;
import org.smpte_ra.schemas._2067_102._2014.REC709YCrCb8ColorEncodingType;

public enum ColorEncoding {
    REC709RGB10ColorEncoding(REC709RGB10ColorEncodingType.class.getName()),
    REC709YCrCb8ColorEncoding(REC709YCrCb8ColorEncodingType.class.getName()),
    REC709FullRGB10ColorEncoding(REC709FullRGB10ColorEncodingType.class.getName()),
    REC709RGB8ColorEncoding(REC709RGB8ColorEncodingType.class.getName());

    private final String value;

    ColorEncoding(String value) {
        this.value = value;
    }

    public static ColorEncoding fromValue(String value) {
        for (ColorEncoding colorEncodingEnum: ColorEncoding.values()) {
            if (colorEncodingEnum.value.equals(value)) {
                return colorEncodingEnum;
            }
        }
        throw new IllegalArgumentException(value);
    }

    public String getValue() {
        return value;
    }
}
