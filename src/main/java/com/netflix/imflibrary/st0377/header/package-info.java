/*
 *
 * Copyright 2015 Netflix, Inc.
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

/**
 * Classes in this package correspond to object model for respective Structural Metadata sets defined
 * in st377-1:2011. All concrete classes in this package have a static nested class. Objects of the
 * respective static nested classes store literal values encountered in the MXF file bitstream. Objects
 * of the nesting classes realize the object model as well as relationships between different Structural
 * Metadata sets defined in st377-1:2011.
 *
 * For example, the "Preface" Structural Metadata set in the MXF file refers to a "ContentStorage"
 * Structural Metadata set by storing the value of the instance UID associated with the latter.
 * Correspondingly, {@link com.netflix.imflibrary.st0377.header.Preface.PrefaceBO} contains
 * {@code com.netflix.imflibrary.st0377.header.Preface.PrefaceBO.content_storage} field that holds the
 * same value as {@code com.netflix.imflibrary.st0377.header.ContentStorage.ContentStorageBO.instance_uid}.
 * The corresponding nesting {@link com.netflix.imflibrary.st0377.header.Preface} object then holds a
 * reference to the corresponding {@link com.netflix.imflibrary.st0377.header.ContentStorage} object
 *
 */
package com.netflix.imflibrary.st0377.header;
