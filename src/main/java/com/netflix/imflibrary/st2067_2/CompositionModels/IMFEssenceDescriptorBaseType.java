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

package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.utils.UUIDHelper;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAnyElement;
import java.util.List;
import java.util.UUID;

/**
 * A class that models a Composition's Essence Descriptor.
 */
@Immutable
public final class IMFEssenceDescriptorBaseType {
    protected final UUID id;
    protected final List<Object> any;

    public IMFEssenceDescriptorBaseType(String id,
                                    List<Object> any)
    {
        this.id             = UUIDHelper.fromUUIDAsURNStringToUUID(id);
        this.any            = any;
    }

    /**
     * Getter for the Sequence ID
     * @return a string representing the urn:uuid of the Essence Descriptor.
     */
    public UUID getId(){
        return this.id;
    }

    /**
     * Getter for the Any property of the Essence Descriptor
     * @return a List representing Any property of the Essence Descriptor.
     */
    public List<Object> getAny(){
        return this.any;
    }


}
