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

package com.netflix.imflibrary.st2067_100.handle;

import com.netflix.imflibrary.st2067_2.Composition;

import javax.annotation.concurrent.Immutable;

@Immutable
public class MCATagSymbolHandle extends Handle {
    private final Composition.VirtualTrack virtualTrack;
    private final String mcaTagSymbol;

    public MCATagSymbolHandle(String handle, Composition.VirtualTrack virtualTrack, String mcaTagSymbol) {
        super(handle);
        this.virtualTrack = virtualTrack;
        this.mcaTagSymbol = mcaTagSymbol;
    }

    public Composition.VirtualTrack getVirtualTrack() {
        return virtualTrack;
    }

    public String getMcaTagSymbol() {
        return mcaTagSymbol;
    }
}
