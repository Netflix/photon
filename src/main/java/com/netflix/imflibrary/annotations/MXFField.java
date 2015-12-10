/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to describe various properties of field members in the object model for an MXF file. Different
 * field members in different classes of the object model correspond to syntax elements defined in st377-1:2011
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MXFField
{
    /**
     * Describes the size in bytes of a syntax element. Equals 0 when size is obtained from the MXF file bitstream
     */
    int size() default 0;

    /**
     * Describes the charset encoding used for String syntax element
     */
    String charset() default "US-ASCII";

    /**
     * Describes whether the value associated with a syntax element corresponds to a reference
     */
    boolean depends() default false;
}
