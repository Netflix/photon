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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * This class represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
public class ApplicationCompositionFactory {
    private static final Map<String, Class> supportedApplicationClassMap = Collections.unmodifiableMap(new HashMap<String, Class>() {{
        put("http://www.smpte-ra.org/schemas/2067-20/2013", Application2Composition.class);
        put("http://www.smpte-ra.org/schemas/2067-20/2016", Application2Composition.class);
        put("http://www.smpte-ra.org/schemas/2067-21/2014", Application2ExtendedComposition.class);
        put("http://www.smpte-ra.org/schemas/2067-21/2016", Application2ExtendedComposition.class);
    }});

    public static ApplicationComposition getApplicationComposition(File inputFile, IMFErrorLogger imfErrorLogger) throws IOException {
        return getApplicationComposition(new FileByteRangeProvider(inputFile), imfErrorLogger);
    }

    public static ApplicationComposition getApplicationComposition(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        ApplicationComposition composition = null;

        IMFCompositionPlaylistType imfCompositionPlaylistType = IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, imfErrorLogger);
        String applicationIdentification = imfCompositionPlaylistType.getApplicationIdentification();
        Class<?> clazz = supportedApplicationClassMap.get(applicationIdentification);

        if(clazz == null) {
            clazz = Application2ExtendedComposition.class;
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                String.format("Unsupported/Missing ApplicationIdentification %s in CPL", applicationIdentification));
        }

        try {
            Constructor<?> constructor = clazz.getConstructor(IMFCompositionPlaylistType.class);
            composition = (ApplicationComposition)constructor.newInstance(imfCompositionPlaylistType);
            imfErrorLogger.addAllErrors(composition.getErrors());
        }
        catch(NoSuchMethodException|IllegalAccessException|InstantiationException| InvocationTargetException e){
            IMFException imfException = null;
            if(e instanceof InvocationTargetException ) {
                Throwable ex = InvocationTargetException.class.cast(e).getTargetException();
               if(ex instanceof IMFException) {
                   imfException = IMFException.class.cast(ex);
                   throw imfException;
               }
            }

            if(imfException != null) {
                throw imfException;
            } else {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format(String.format("No matching constructor for class %s", clazz.getSimpleName())));
            }
        }

        return composition;
    }
}