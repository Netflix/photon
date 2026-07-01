/*
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
 */

package testUtils;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public final class TestHelper
{
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int EOF = -1;

    private TestHelper()
    {
        //to prevent instantiation
    }

    public static Path findResourceByPath(String resourcePath) throws IOException
    {
        URL resource = TestHelper.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        }

        assertNotNull(resource, String.format("Resource %s does not exist", resourcePath));
        return Utilities.getPathFromString(resource.getPath());
    }

    public static byte[] toByteArray(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        int numBytesRead = 0;
        while((numBytesRead = inputStream.read(buffer)) != EOF)
        {
            baos.write(buffer, 0, numBytesRead);
        }

        return baos.toByteArray();
    }

    /**
     * Asserts that at least one of the reported errors has a description containing the given fragment. Prefer this
     * over checking {@code errors.size()}, so tests validate the specific errors they care about and do not break when
     * unrelated validations add or remove reported issues.
     * @param errors the reported errors
     * @param descriptionFragment a substring expected to appear in one of the error descriptions
     */
    public static void assertHasError(List<ErrorLogger.ErrorObject> errors, String descriptionFragment)
    {
        assertTrue(errors.stream().anyMatch(e -> e.getErrorDescription().contains(descriptionFragment)),
                String.format("Expected an error whose description contains \"%s\", but the reported errors were: %s", descriptionFragment, errors));
    }

    /**
     * Asserts that none of the reported errors is at or above the given severity level. Passing {@code WARNING} asserts
     * that there are no errors at all; passing {@code NON_FATAL} allows warnings but no non-fatal or fatal errors.
     * @param errors the reported errors
     * @param level the lowest severity level that is not permitted
     */
    public static void assertNoErrorAtOrAbove(List<ErrorLogger.ErrorObject> errors, IMFErrorLogger.IMFErrors.ErrorLevels level)
    {
        assertFalse(errors.stream().anyMatch(e -> e.getErrorLevel().compareTo(level) >= 0),
                String.format("Expected no error at or above %s, but the reported errors were: %s", level, errors));
    }

    public static Object getValue(Object obj, String fieldName)
    {
        try
        {
            Field field = getField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new MXFException(String.format("Parsing failure due to reflection error, reading property {}", fieldName), e);
        }
    }

    public static Field getField(Class<?> aClass, String fieldName) throws NoSuchFieldException
    {
        try
        {
            return aClass.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            Class<?> superClass = aClass.getSuperclass();
            if (superClass == null)
            {
                throw e;
            }
            else
            {
                return getField(superClass, fieldName);
            }
        }
    }
}
