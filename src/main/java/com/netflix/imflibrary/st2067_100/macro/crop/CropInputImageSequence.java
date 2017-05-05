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

package com.netflix.imflibrary.st2067_100.macro.crop;

import com.netflix.imflibrary.st2067_100.macro.Sequence;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CropInputImageSequence extends Sequence {
    private final MXFRectangle referenceRectangle;
    private final RectanglePadding inset;

    public CropInputImageSequence(String annotaion, String handle, MXFRectangle referenceRectangle, RectanglePadding inset) {
        super(annotaion, handle);
        this.referenceRectangle = referenceRectangle;
        this.inset = inset;
    }

    public MXFRectangle getReferenceRectangle() {
        return referenceRectangle;
    }

    public RectanglePadding getInset() {
        return inset;
    }
}
