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

public final class IMFPKLObjectFieldsFactory {

    /*Prevent instantiation*/
    private IMFPKLObjectFieldsFactory(){

    }

    /**
     * A factory method that constructs a 2013 Schema compliant PackingListType object and recursively constructs all of its constituent fields.
     * Note: Fields that are either Java primitives, wrapperTypes or a subclass of Collection
     * are not constructed by this method for the reason that the field's accessor methods would do so.
     *
     * @return A 2007 schema compliant PackingListType object
     */
    public static org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType constructPackingListType_2007(){
        org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType pklType_2007 = new org.smpte_ra.schemas.st0429_8_2007.PKL.PackingListType();
        IMFDocumentsObjectFieldsFactory.constructObjectFields(pklType_2007);
        return pklType_2007;
    }

    /**
     * A factory method that constructs a 2016 Schema compliant PackingListType object and recursively constructs all of its constituent fields.
     * Note: Fields that are either Java primitives, wrapperTypes or a subclass of Collection
     * are not constructed by this method for the reason that the field's accessor methods would do so.
     *
     * @return A 2016 schema compliant PackingListType object
     */
    public static org.smpte_ra.schemas.st2067_2_2016.PKL.PackingListType constructPackingListType_2016(){
        org.smpte_ra.schemas.st2067_2_2016.PKL.PackingListType pklType_2016 = new org.smpte_ra.schemas.st2067_2_2016.PKL.PackingListType();
        IMFDocumentsObjectFieldsFactory.constructObjectFields(pklType_2016);
        return pklType_2016;
    }
}
