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

import com.netflix.imflibrary.app.IMPAnalyzer.ApplicationSet;
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
public class IMPAnalyzerTestApp5Errors
{
    @Test
    public void IMPAnalyzerTestApp5Errors() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application5/PhotonApp5TestDiscontinuityAndVideoLineMapError/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile, ApplicationSet.APPLICATION_5_SET);
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
                {
                	if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 5);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }
}
