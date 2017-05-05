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

package com.netflix.imflibrary.st2067_100.macro.scale;

import com.netflix.imflibrary.st2067_100.macro.Sequence;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ScaleOutputImageSequence extends Sequence {
    private final int width;
    private final int height;
    private final String boundaryCondition;
    private final ScaleAlgorithm scalingAlgorithmType;

    public ScaleOutputImageSequence(String annotation, String handle, int width, int height, String boundaryCondition, ScaleAlgorithm scalingAlgorithmType) {
        super(annotation, handle);
        this.width = width;
        this.height = height;
        this.boundaryCondition = boundaryCondition;
        this.scalingAlgorithmType = scalingAlgorithmType;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getBoundaryCondition() {
        return boundaryCondition;
    }

    public ScaleAlgorithm getScalingAlgorithmType() {
        return scalingAlgorithmType;
    }
}
