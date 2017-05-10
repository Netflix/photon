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

package com.netflix.imflibrary.st2067_100.macro.preset;

import com.netflix.imflibrary.st2067_100.macro.Macro;
import com.netflix.imflibrary.st2067_100.macro.Sequence;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;

@Immutable
public class PresetMacro extends Macro {
    private final String preset;

    public PresetMacro(String name, String annotaion, String preset) {
        super(name, annotaion, new ArrayList<Sequence>(), new ArrayList<Sequence>());
        this.preset = preset;
    }

    public String getPreset() {
        return preset;
    }
}
