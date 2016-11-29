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

import com.netflix.imflibrary.Colorimetry;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    public enum ApplicationCompositionType {
        APPLICATION_2_COMPOSITION_TYPE(Application2Composition.class,          namespacesApplication2Composition),
        APPLICATION_2E_COMPOSITION_TYPE(Application2ExtendedComposition.class, namespacesApplication2EComposition);
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

        public static @Nullable ApplicationCompositionType fromApplicationID(String applicationIdentification) {

            for(ApplicationCompositionType applicationCompositionType : ApplicationCompositionType.values()) {
                if(applicationCompositionType.getNameSpaceSet().contains(applicationIdentification)) {
                    return applicationCompositionType;
                }
            }

            return null;
        }
    }

    public static ApplicationComposition getApplicationComposition(File inputFile, IMFErrorLogger imfErrorLogger) throws IOException {
        return getApplicationComposition(new FileByteRangeProvider(inputFile), imfErrorLogger);
    }

    public static ApplicationComposition getApplicationComposition(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        ApplicationComposition composition = null;
        Class<?> clazz = null;

        try {
            IMFCompositionPlaylistType imfCompositionPlaylistType = IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, imfErrorLogger);
            String applicationIdentification = imfCompositionPlaylistType.getApplicationIdentification();
            ApplicationCompositionType applicationCompositionType = ApplicationCompositionType.fromApplicationID(applicationIdentification);

            if(applicationCompositionType == null) {
                clazz = Application2ExtendedComposition.class;
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Unsupported/Missing ApplicationIdentification %s in CPL", applicationIdentification));
            }
            else
            {
                clazz = applicationCompositionType.getClazz();
            }

            Constructor<?> constructor = clazz.getConstructor(IMFCompositionPlaylistType.class);
            composition = (ApplicationComposition)constructor.newInstance(imfCompositionPlaylistType);
            imfErrorLogger.addAllErrors(composition.getErrors());
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