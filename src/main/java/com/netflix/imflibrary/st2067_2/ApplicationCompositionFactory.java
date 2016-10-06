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
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        put("http://www.smpte-ra.org/schemas/2067-20/2016", Composition.class);
        put("http://www.smpte-ra.org/schemas/2067-21/2016", Composition.class);
    }});

    public static ApplicationComposition getApplicationComposition(ResourceByteRangeProvider resourceByteRangeProvider, IMFErrorLogger imfErrorLogger) throws IOException {
        ApplicationComposition composition = null;

        IMFCompositionPlaylistType imfCompositionPlaylistType = IMFCompositionPlaylistType.getCompositionPlayListType(resourceByteRangeProvider, imfErrorLogger);
        String applicationIdentification = imfCompositionPlaylistType.getApplicationIdentification();
        Class<?> clazz = supportedApplicationClassMap.get(applicationIdentification);

        if(clazz != null)
        {
            try {
                composition = ApplicationComposition.class.cast(clazz.getConstructor(IMFCompositionPlaylistType.class)
                        .newInstance(imfCompositionPlaylistType));
            }
            catch(NoSuchMethodException|IllegalAccessException|InstantiationException| InvocationTargetException e){
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.INTERNAL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                        String.format(String.format("No matching constructor for class %s", clazz.getSimpleName())));
            }
        }
        else
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    String.format("Unsupported ApplicationIdentification %s in CPL", applicationIdentification));
        }

        return composition;
    }
}