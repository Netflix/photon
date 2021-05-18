/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.imflibrary.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author rjaycocks
 */
@Test(groups = "unit")
public class LocatorTest {

    @Test
    public void shouldHandleCommandLineArgumentsForSingleLocator() {
        Locator locator = Locator.first(new String[]{
            "s3://test/test2",
            "--arg1=a2",
            "--arg2=a4"
        }, t -> {
                                    Assert.assertEquals(t.intValue(), 1);
                                });
        Assert.assertEquals(locator.getClass(), S3Locator.class);
        Assert.assertEquals(locator.getName(), "test2");
        Assert.assertEquals(locator.getConfiguration().getValue("arg1"), "a2");
        Assert.assertEquals(locator.getConfiguration().getValue("arg2"), "a4");
    }

    @Test
    public void shouldHandleCommandLineArgumentsForMultipleLocators() {
        Locator[] locators = Locator.all(new String[]{
            "s3://test/test1",
            "s3://test/test2",
            "s3://test/test3",
            "--arg1=a2",
            "--arg2=a4"
        }, t -> {
                                     Assert.assertEquals(t.intValue(), 3);
                                 });
        Assert.assertEquals(locators.length, 3);
        Assert.assertEquals(locators[0].getClass(), S3Locator.class);
        Assert.assertEquals(locators[0].getName(), "test1");
        Assert.assertEquals(locators[1].getName(), "test2");
        Assert.assertEquals(locators[2].getName(), "test3");
        Assert.assertEquals(locators[0].getConfiguration().getValue("arg1"), "a2");
        Assert.assertEquals(locators[0].getConfiguration().getValue("arg2"), "a4");
        Assert.assertEquals(locators[0].getConfiguration(), locators[0].getConfiguration());
    }

    @Test
    public void testConfigurationNullForFileLocator() {
        Locator locator = Locator.first(new String[]{
            "test1",
            "--arg1=a2",
            "--arg2=a4"
        }, t -> {
                                    Assert.assertEquals(t.intValue(), 1);
                                });
        Assert.assertEquals(locator.getClass(), FileLocator.class);
        Assert.assertEquals(locator.getName(), "test1");
        Assert.assertNull(locator.getConfiguration());
    }

    @Test
    public void testArgumentsAnyOrder() {
        Locator[] locators = Locator.all(new String[]{
            "--arg1=a2",
            "--arg2=a4",
            "s3://test/test2",
            "--arg3=a3",
            "s3://test/test3",}, t -> {
                                     Assert.assertEquals(t.intValue(), 2);
                                 });
        Assert.assertEquals(locators.length, 2);
        Assert.assertEquals(locators[0].getClass(), S3Locator.class);
        Assert.assertEquals(locators[0].getName(), "test2");
        Assert.assertEquals(locators[1].getName(), "test3");
        Assert.assertEquals(locators[0].getConfiguration().getValue("arg1"), "a2");
        Assert.assertEquals(locators[0].getConfiguration().getValue("arg2"), "a4");
        Assert.assertEquals(locators[0].getConfiguration().getValue("arg3"), "a3");
    }
}
