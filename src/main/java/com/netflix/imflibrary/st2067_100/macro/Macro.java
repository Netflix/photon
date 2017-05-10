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

package com.netflix.imflibrary.st2067_100.macro;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class that models an OPL Macro.
 */
@Immutable
public abstract class Macro {
    private final String name;
    private final String annotaion;
    private final List<Sequence> inputs;
    private final List<Sequence> outputs;

    public Macro(String name, String annotaion, Sequence input, Sequence output) {
        this.name       = name;
        this.annotaion  = annotaion;
        this.inputs      = Collections.unmodifiableList(Arrays.asList(input));
        this.outputs     = Collections.unmodifiableList(Arrays.asList(output));
    }

    public Macro(String name, String annotaion, List<Sequence> inputs, List<Sequence> outputs) {
        this.name       = name;
        this.annotaion  = annotaion;
        this.inputs      = Collections.unmodifiableList(inputs);
        this.outputs     = Collections.unmodifiableList(outputs);
    }

    public List<Sequence> getInputs() {
        return inputs;
    }

    public List<Sequence> getOutputs() {
        return outputs;
    }

    public String getAnnotaion() {
        return annotaion;
    }

    public String getName() {
        return name;
    }
}
