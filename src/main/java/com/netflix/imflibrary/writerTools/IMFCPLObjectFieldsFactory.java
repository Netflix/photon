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

package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.writerTools.utils.IMFDocumentsObjectFieldsFactory;

public final class IMFCPLObjectFieldsFactory {

    /*Prevent instantiation*/
    private IMFCPLObjectFieldsFactory(){

    }

    /**
     * A factory method that constructs a 2013 Schema compliant CompositionPlaylistType object and recursively constructs all of its constituent fields.
     * Note: Fields that are either Java primitives, wrapperTypes or a subclass of Collection
     * are not constructed by this method for the reason that the field's accessor methods would do so.
     *
     * @return A 2013 schema compliant CompositionPlaylistType object
     */
    public static org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType constructCompositionPlaylistType_2013(){
        org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType cplType_2013 = new org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType();
        IMFDocumentsObjectFieldsFactory.constructObjectFields(cplType_2013);
        return cplType_2013;
    }

    /**
     * A factory method that constructs a 2016 Schema compliant CompositionPlaylistType object and recursively constructs all of its constituent fields.
     * Note: Fields that are either Java primitives, wrapperTypes or a subclass of Collection
     * are not constructed by this method for the reason that the field's accessor methods would do so.
     *
     * @return A 2016 schema compliant CompositionPlaylistType object
     */
    public static org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType constructCompositionPlaylistType_2016() {
        org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType cplType_2016 = new org.smpte_ra.schemas.st2067_2_2016.CompositionPlaylistType();
        IMFDocumentsObjectFieldsFactory.constructObjectFields(cplType_2016);
        return cplType_2016;
    }
}
