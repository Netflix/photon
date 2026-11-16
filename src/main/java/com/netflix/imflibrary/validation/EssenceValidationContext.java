/*
 *
 * Copyright 2026 Netflix, Inc.
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

package com.netflix.imflibrary.validation;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes in which context an MXF Track File is validated. The MXF header metadata alone often cannot identify
 * the governing specification (for example IMF Application 2E and 4 both use JPEG 2000 based Track Files), so the
 * validators to apply are selected from what the Composition says about how the Track File is used:
 * <ul>
 *     <li>the Application Identification(s) of the Composition</li>
 *     <li>the namespace of the Virtual Track sequence that references the Track File (which also identifies the
 *     core constraints edition, or the plug-in, for the essence kind)</li>
 * </ul>
 * Essence kind specific checks (audio, text, ...) are only run when a validator selected by the context provides them.
 */
public final class EssenceValidationContext {

    private static final EssenceValidationContext NONE = new EssenceValidationContext(Collections.emptySet(), null);

    private final Set<String> applicationIdentifications;
    private final String sequenceNamespace;

    private EssenceValidationContext(Set<String> applicationIdentifications, String sequenceNamespace) {
        this.applicationIdentifications = Collections.unmodifiableSet(new LinkedHashSet<>(applicationIdentifications));
        this.sequenceNamespace = sequenceNamespace;
    }

    /**
     * @return a context that selects no validators, i.e. only essence-agnostic structural checks apply
     */
    public static EssenceValidationContext none() {
        return NONE;
    }

    public static EssenceValidationContext forSequenceNamespace(@Nullable String sequenceNamespace) {
        return new EssenceValidationContext(Collections.emptySet(), sequenceNamespace);
    }

    public static EssenceValidationContext of(@Nullable Set<String> applicationIdentifications, @Nullable String sequenceNamespace) {
        return new EssenceValidationContext(applicationIdentifications == null ? Collections.emptySet() : applicationIdentifications, sequenceNamespace);
    }

    @Nonnull
    public Set<String> getApplicationIdentifications() {
        return applicationIdentifications;
    }

    @Nullable
    public String getSequenceNamespace() {
        return sequenceNamespace;
    }
}
