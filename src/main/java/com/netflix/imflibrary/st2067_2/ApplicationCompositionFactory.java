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

package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a factory method to construct ApplicationComposition based on CPL ApplicationIdentification.
 */
public class ApplicationCompositionFactory {
    private static final Set<String> namespacesApplication2Composition = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-20/2013");
        add("http://www.smpte-ra.org/schemas/2067-20/2016");
    }});

    private static final Set<String> namespacesApplication2EComposition = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-21/2014");
        add("http://www.smpte-ra.org/schemas/2067-21/2016");
    }});

    private static final Set<String> namespacesApplication4Composition = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/schemas/2067-40/2016");
    }});

    private static final Set<String> namespacesApplication5Composition = Collections.unmodifiableSet(new HashSet<String>() {{
        add("http://www.smpte-ra.org/ns/2067-50/2017");
    }});

    public enum ApplicationCompositionType {
        APPLICATION_2_COMPOSITION_TYPE(Application2Composition.class,          namespacesApplication2Composition),
        APPLICATION_2E_COMPOSITION_TYPE(Application2ExtendedComposition.class, namespacesApplication2EComposition),
        APPLICATION_4_COMPOSITION_TYPE(Application4Composition.class,          namespacesApplication4Composition),
        APPLICATION_5_COMPOSITION_TYPE(Application5Composition.class,          namespacesApplication5Composition),
        APPLICATION_UNSUPPORTED_COMPOSITION_TYPE(ApplicationUnsupportedComposition.class, Collections.unmodifiableSet(new HashSet<>()));
        private Set<String> nameSpaceSet;
        private Class<?> clazz;

        ApplicationCompositionType(Class<?> clazz, Set<String> nameSpaceSet) {
            this.nameSpaceSet = nameSpaceSet;
            this.clazz = clazz;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public Set<String> getNameSpaceSet() {
            return nameSpaceSet;
        }

        public static ApplicationCompositionType fromApplicationID(String applicationIdentification) {

            for(ApplicationCompositionType applicationCompositionType : ApplicationCompositionType.values()) {
                if(applicationCompositionType.getNameSpaceSet().contains(applicationIdentification)) {
                    return applicationCompositionType;
                }
            }

            return APPLICATION_UNSUPPORTED_COMPOSITION_TYPE;
        }
    }

    public static ApplicationComposition getApplicationComposition(File inputFile, IMFErrorLogger imfErrorLogger) throws IOException {
        return getApplicationComposition(new FileByteRangeProvider(inputFile), imfErrorLogger);
    }

    public static ApplicationComposition getApplicationComposition(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        return getApplicationComposition(resourceByteRangeProvider, imfErrorLogger, new HashSet<>());
    }

    public static ApplicationComposition getApplicationComposition(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger, Set<String> homogeneitySelectionSet) throws IOException {
        ApplicationComposition composition = null;
        Class<?> clazz = null;

        try {
            IMFCompositionPlaylistType imfCompositionPlaylistType = IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, imfErrorLogger);
            if (imfCompositionPlaylistType.getApplicationIdentificationSet().size() == 0) {
                clazz = Application2ExtendedComposition.class;
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("Missing ApplicationIdentification in CPL"));
                Constructor<?> constructor = clazz.getConstructor(IMFCompositionPlaylistType.class, Set.class);
                composition = (ApplicationComposition) constructor.newInstance(imfCompositionPlaylistType, homogeneitySelectionSet);
                imfErrorLogger.addAllErrors(composition.getErrors());
            } else {
                for (String applicationIdentification : imfCompositionPlaylistType.getApplicationIdentificationSet()) {
                    ApplicationCompositionType applicationCompositionType = ApplicationCompositionType.fromApplicationID(applicationIdentification);
                    clazz = applicationCompositionType.getClazz();
                    Constructor<?> constructor = clazz.getConstructor(IMFCompositionPlaylistType.class, Set.class);
                    composition = (ApplicationComposition) constructor.newInstance(imfCompositionPlaylistType, homogeneitySelectionSet);
                    imfErrorLogger.addAllErrors(composition.getErrors());
                }
            }
        }
        catch(IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
            return null;
        }
        catch(NoSuchMethodException|IllegalAccessException|InstantiationException| InvocationTargetException e){
            if(e instanceof InvocationTargetException ) {
                Throwable ex = InvocationTargetException.class.cast(e).getTargetException();
               if(ex instanceof IMFException) {
                   imfErrorLogger.addAllErrors(IMFException.class.cast(ex).getErrors());
                   return null;
               }
            }

            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format(String.format("No matching constructor for class %s", clazz != null ? clazz.getSimpleName(): "ApplicationComposition")));
            return null;
        }

        return composition;
    }
}