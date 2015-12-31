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

/**
 * An enumeration for representing the different MXF essence types
 */
@SuppressWarnings({"PMD.SingularField"})
public enum MXFDataDefinition
{
    /**
     * The PICTURE.
     */
    PICTURE(new MXFUID(MXFUID.picture_essence_track)),

    /**
     * The SOUND.
     */
    SOUND(new MXFUID(MXFUID.sound_essence_track)),

    /**
     * The DATA.
     */
    DATA(new MXFUID(MXFUID.data_essence_track)),

    /**
     * The OTHER.
     */
    OTHER(null),

    ;

    private final MXFUID mxfUL;

    private MXFDataDefinition(MXFUID mxfUL)
    {
        this.mxfUL = mxfUL;
    }

    /**
     * Getter for data definition corresponding to the Universal Label that was passed
     *
     * @param mxfUL the mxf UL
     * @return the data definition
     */
    public static MXFDataDefinition getDataDefinition(MXFUID mxfUL)
    {
        if (mxfUL.equals(PICTURE.mxfUL))
        {
            return PICTURE;
        }
        else if (mxfUL.equals(SOUND.mxfUL))
        {
            return SOUND;
        }
        else if (mxfUL.equals(DATA.mxfUL))
        {
            return DATA;
        }
        else
        {
            return OTHER;
        }
    }

}
