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

package com.netflix.imflibrary.st2067_2.CompositionModels;

import com.netflix.imflibrary.st2067_2.Composition;

import java.math.BigInteger;
import java.util.List;

/**
 * A class that models an IMF Base Resource.
 */
public abstract class IMFBaseResourceType {
    protected final String id;
    protected final Composition.EditRate editRate;
    protected final BigInteger intrinsicDuration;
    protected final BigInteger entryPoint;
    protected final BigInteger sourceDuration;
    protected final BigInteger repeatCount;

    public IMFBaseResourceType(String id,
                               List<Long> editRate,
                               BigInteger intrinsicDuration,
                               BigInteger entryPoint,
                               BigInteger sourceDuration,
                               BigInteger repeatCount){
        this.id = id;
        this.editRate = new Composition.EditRate(editRate);
        this.intrinsicDuration = intrinsicDuration;
        this.entryPoint = (entryPoint != null)? entryPoint: BigInteger.ZERO;
        this.sourceDuration = (sourceDuration != null) ? sourceDuration: this.intrinsicDuration.subtract(this.entryPoint);
        this.repeatCount = (repeatCount != null)? repeatCount: BigInteger.ONE;
    }

    /**
     * Getter for the Resource ID
     * @return a string representing the urn:uuid of the Resource
     */
    public String getId(){
        return this.id;
    }

    /**
     * Getter for the EditRate of the Resource
     * @return a Composition.EditRate object of the Resource
     */
    public Composition.EditRate getEditRate(){
        return this.editRate;
    }

    /**
     * Getter for the IntrinsicDuration of the Resource
     * @return a BigInteger representing the Resource's IntrinsicDuration
     */
    public BigInteger getIntrinsicDuration(){
        return this.intrinsicDuration;
    }

    /**
     * Getter for the EntryPoint of the Resource
     * @return a BigInteger representing the Resource's EntryPoint
     */
    public BigInteger getEntryPoint(){
        return this.entryPoint;
    }

    /**
     * Getter for the SourceDuration of the Resource
     * @return a BigInteger representing the Resource's SourceDuration
     */
    public BigInteger getSourceDuration(){
        return this.sourceDuration;
    }

    /**
     * Getter for the RepeatCount of the Resource
     * @return a BigInteger representing the Resource's RepeatCount
     */
    public BigInteger getRepeatCount(){
        return this.repeatCount;
    }


    /**
     * A method to determine the equivalence of any two Base Resource.
     * @param other - the object to compare against
     * @return boolean indicating if the two Base Resources are equivalent/representing the same timeline
     */
    public boolean equivalent(IMFBaseResourceType other)
    {
        if(other == null){
            return false;
        }
        boolean result = true;
        //Compare the following fields of the base resources that have to be equal
        //for the 2 resources to be considered equivalent/representing the same timeline.
        result &= editRate.equals(other.getEditRate());
        result &= entryPoint.equals(other.getEntryPoint());
        result &= intrinsicDuration.equals(other.getIntrinsicDuration());
        result &= sourceDuration.equals(other.getSourceDuration());
        result &= repeatCount.equals(other.getRepeatCount());

        return  result;
    }
}
