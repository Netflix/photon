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

package com.netflix.imflibrary.st2067_2;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.List;

/**
 * A class that models TrackFile Resource structure of a 2013 IMF Composition Playlist.
 */
@Immutable
public final class IMFTrackFileResource_st2067_2_2013 extends IMFTrackFileResourceType {
    public IMFTrackFileResource_st2067_2_2013(String id,
                                              String trackFileId,
                                              List<Long> editRate,
                                              BigInteger intrinsicDuration,
                                              BigInteger entryPoint,
                                              BigInteger sourceDuration,
                                              BigInteger repeatCount,
                                              String sourceEncoding ){
        super(id, trackFileId, editRate, intrinsicDuration, entryPoint, sourceDuration, repeatCount, sourceEncoding);
    }
}
