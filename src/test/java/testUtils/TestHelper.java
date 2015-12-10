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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertNotNull;

public final class TestHelper
{
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int EOF = -1;

    private TestHelper()
    {
        //to prevent instantiation
    }

    public static File findResourceByPath(String resourcePath)
    {
        URL resource = TestHelper.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        }

        assertNotNull(resource, String.format("Resource %s does not exist", resourcePath));
        return new File(resource.getPath());
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
}
