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

public final class IMFAssetMapObjectFieldsFactory {

    /*Prevent instantiation*/
    private IMFAssetMapObjectFieldsFactory() {

    }

    /**
     * A factory method that constructs a 2007 Schema compliant PackingListType object and recursively constructs all of its constituent fields.
     * Note: Fields that are either Java primitives, wrapperTypes or a subclass of Collection
     * are not constructed by this method for the reason that the field's accessor methods would do so.
     *
     * @return A 2007 schema compliant AssetMapType object
     */
    public static org.smpte_ra.schemas._429_9._2007.am.AssetMapType constructAssetMapType_2007() {
        org.smpte_ra.schemas._429_9._2007.am.AssetMapType amType_2007 = new org.smpte_ra.schemas._429_9._2007.am.AssetMapType();
        IMFDocumentsObjectFieldsFactory.constructObjectFields(amType_2007);
        return amType_2007;
    }
}
