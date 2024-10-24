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

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.annotations.MXFProperty;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;

/**
 * Object model corresponding to JPEG2000PictureComponent as defined in ISO/IEC 15444-1 Annex A.5.1
 */
@Immutable
public final class JPEG2000PictureComponent {

    private final JPEG2000PictureComponentBO componentBO;

    /**
     * Constructor for a JPEG2000PictureComponent object
     * @param componentBO the parse JPEG2000PictureComponent object
     */
    public JPEG2000PictureComponent(JPEG2000PictureComponentBO componentBO){
        this.componentBO = componentBO;
    }

    /**
     * Object corresponding to a parsed JPEG2000PictureComponent as defined in ISO/IEC 15444-1 Annex A.5.1
     */
    @Immutable
    public static final class JPEG2000PictureComponentBO{
        @MXFProperty(size=1) protected final Short sSiz;
        @MXFProperty(size=1) protected final Short xrSiz;
        @MXFProperty(size=1) protected final Short yrSiz;

        public Short getSSiz() {
            return sSiz;
        }

        public Short getXrSiz() {
            return xrSiz;
        }

        public Short getYrSiz() {
            return yrSiz;
        }

        /**
         * Instantiates a new parsed JPEG2000PictureComponent object
         *
         * @param bytes the byte array corresponding to the 3 fields
         * @throws IOException - any I/O related error will be exposed through an IOException
         */

        public JPEG2000PictureComponentBO(byte[] bytes)
                throws IOException
        {
            sSiz = (short)((int)bytes[0] & 0xFF);
            xrSiz = (short)((int)bytes[1] & 0xFF);
            yrSiz = (short)((int)bytes[2] & 0xFF);
        }

    }
}
