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

import com.netflix.imflibrary.st2067_100.macro.Macro;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CropMacro extends Macro {
    public CropMacro(String name, String annotaion, CropInputImageSequence input, CropOutputImageSequence output) {
        super(name, annotaion, input, output);
    }

    public CropInputImageSequence getCropInputImageSequence() {
        return (CropInputImageSequence)this.getInputs().get(0);
    }

    public CropOutputImageSequence getCropOutputImageSequence() {
        return (CropOutputImageSequence)this.getOutputs().get(0);
    }
}
