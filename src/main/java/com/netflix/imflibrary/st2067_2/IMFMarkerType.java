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

/**
 * A class that models an IMF MarkerType.
 */
@Immutable
public final class IMFMarkerType {
    private final String annotation;
    private final IMFMarkerType.Label label;
    private final BigInteger offset;

    public IMFMarkerType(String annotation,
                         IMFMarkerType.Label label,
                         BigInteger offset)
    {
        this.annotation = annotation;
        this.label = label;
        this.offset = offset;
    }

    /**
     * Getter for the Annotation of the Marker
     * @return a Annotation of the Marker
     */
    public String getAnnotation(){
        return this.annotation;
    }

    /**
     * Getter for the Label of the Marker
     * @return a Label of the Marker
     */
    public IMFMarkerType.Label getLabel(){
        return this.label;
    }

    /**
     * Getter for the Offset of the Marker
     * @return a BigInteger representing the Marker's Offset
     */
    public BigInteger getOffset(){
        return this.offset;
    }

    /**
     * A method to determine the equivalence of any two Markers.
     * @param other - the object to compare against
     * @return boolean indicating if the two Markers are equivalent/representing the same timeline
     */
    public boolean equivalent(IMFMarkerType other)
    {
        if(other == null){
            return false;
        }
        boolean result = true;
        result &= offset.equals(other.getOffset());
        result &= label.equivalent(other.getLabel());

        return  result;
    }

    @Immutable
    public static final class Label {
        private final String value;
        private final String scope;

        public Label(String value, String scope)
        {
            this.value = value;
            this.scope = scope;
        }

        /**
         * Getter for the Value of the Label
         *
         * @return a String representing the Label value
         */
        public String getValue() {
            return value;
        }

        /**
         * Getter for the Scope of the Label
         *
         * @return a String representing the Label Scope
         */
        public String getScope() {
            return scope;
        }

        /**
         * A method to determine the equivalence of any two Labels.
         *
         * @param other - the object to compare against
         * @return boolean indicating if the two Labels are equivalent
         */
        public boolean equivalent(Label other) {
            if (other == null) {
                return false;
            }
            boolean result = true;
            result &= value.equals(other.getValue());
            result &= scope.equals(other.getScope());

            return result;
        }
    }
}
