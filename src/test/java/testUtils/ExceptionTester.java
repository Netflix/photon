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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.netflix.imflibrary.exceptions.TestException;
import java.lang.reflect.Constructor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

@SuppressFBWarnings("REC_CATCH_EXCEPTION")
public class ExceptionTester<T extends Throwable> {

    public ExceptionTester(Class<T> clazz) {
        assertNotNull(clazz, "Class must not be null");

        assertTrue(Throwable.class.isAssignableFrom(clazz));
        TestException nestedException = new TestException("nested");
        String message = new String("ExceptionTester");

        try {
            Constructor<T> ctor = clazz.getConstructor();
            try {
                T ex = ctor.newInstance();
                assertNull(ex.getMessage());
                assertNull(ex.getCause());
            } catch (Exception e) {
                fail("We expect this operation to succeed, but it threw an exception - signature: no parameters", e);
            }
        } catch (NoSuchMethodException e) {         // NOPMD
            // We don't have one of these constructors, that's ok
        }

        try {
            Constructor<T> ctor = clazz.getConstructor(Throwable.class);
            try {
                T ex = ctor.newInstance(nestedException);
                assertSame(ex.getCause(), nestedException);
                assertEquals(ex.getMessage(), nestedException.toString());
            } catch (Exception e) {
                fail("We expect this operation to succeed, but it threw an exception - signature(nested_exception)", e);
            }
        } catch (NoSuchMethodException e) {         // NOPMD
            // We don't have one of these constructors, that's ok
        }

        try {
            Constructor<T> ctor = clazz.getConstructor(String.class);
            try {
                T ex = ctor.newInstance(message);
                assertEquals(ex.getMessage(), message);
                assertNull(ex.getCause());
            } catch (Exception e) {
                fail("We expect this operation to succeed, but it threw an exception - signature(message)", e);
            }
        } catch (NoSuchMethodException e) {         // NOPMD
            // We don't have one of these constructors, that's ok
        }

        try {
            Constructor<T> ctor = clazz.getConstructor(String.class, Throwable.class);
            try {
                T ex = ctor.newInstance(message, nestedException);
                assertEquals(ex.getMessage(), message);
                assertSame(ex.getCause(), nestedException);
            } catch (Exception e) {
                fail("We expect this operation to succeed, but it threw an exception - signature(nested_exception, message", e);
            }
        } catch (NoSuchMethodException e) {         // NOPMD
            // We don't have one of these constructors, that's ok
        }
    }
}
