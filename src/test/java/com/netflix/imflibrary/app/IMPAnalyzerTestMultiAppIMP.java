/*
 *
 * Copyright 2019 RheinMain University of Applied Sciences, Wiesbaden, Germany.
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
package com.netflix.imflibrary.app;

import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory.ApplicationCompositionType;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.TestHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.netflix.imflibrary.app.IMPAnalyzer.analyzePackage;

@Test(groups = "unit")
public class IMPAnalyzerTestMultiAppIMP
{
    @Test
    public void IMPAnalyzerTestMultiAppIMP() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application5/MultiAppIMP/");
        // Test against App 2/2E
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile, ApplicationCompositionType.APPLICATION_2_COMPOSITION_TYPE);
        Assert.assertEquals(errorMap.size(), 8);
        errorMap.entrySet().stream().forEach( e ->
                {
                	if (e.getKey().matches("CPL_be8dd1cf-f6e0-4455-ac6f-b22d28557755.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_eaf76289-fd79-477f-9526-e34d69a8f57a.xml")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_a74cc26b-a87d-4fde-9a28-1865a5ef33db.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else if (e.getKey().matches("CPL_a74cc26b-a87d-4fde-9a28-1865a5ef33db.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_be8dd1cf-f6e0-4455-ac6f-b22d28557755.xml")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_eaf76289-fd79-477f-9526-e34d69a8f57a.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

        // Test against App 5
        errorMap = analyzePackage(inputFile, ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE);
        Assert.assertEquals(errorMap.size(), 8);
        errorMap.entrySet().stream().forEach( e ->
                {
                	if (e.getKey().matches("CPL_be8dd1cf-f6e0-4455-ac6f-b22d28557755.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_eaf76289-fd79-477f-9526-e34d69a8f57a.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else if (e.getKey().matches("CPL_a74cc26b-a87d-4fde-9a28-1865a5ef33db.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else if (e.getKey().matches("CPL_a74cc26b-a87d-4fde-9a28-1865a5ef33db.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_be8dd1cf-f6e0-4455-ac6f-b22d28557755.xml")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_eaf76289-fd79-477f-9526-e34d69a8f57a.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );
        // Default Test
        errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 8);
        errorMap.entrySet().stream().forEach( e ->
                {
                	if (e.getKey().matches("CPL_be8dd1cf-f6e0-4455-ac6f-b22d28557755.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else if (e.getKey().matches("CPL_eaf76289-fd79-477f-9526-e34d69a8f57a.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else if (e.getKey().matches("CPL_a74cc26b-a87d-4fde-9a28-1865a5ef33db.xml")) {
                        Assert.assertEquals(e.getValue().size(), 0);
                    } else if (e.getKey().matches("CPL_a74cc26b-a87d-4fde-9a28-1865a5ef33db.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else if (e.getKey().matches("CPL_be8dd1cf-f6e0-4455-ac6f-b22d28557755.xml")) {
                        Assert.assertEquals(e.getValue().size(), 1);
                    } else if (e.getKey().matches("CPL_eaf76289-fd79-477f-9526-e34d69a8f57a.xml Virtual Track Conformance")) {
                        Assert.assertEquals(e.getValue().size(), 2);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );
    }
}
