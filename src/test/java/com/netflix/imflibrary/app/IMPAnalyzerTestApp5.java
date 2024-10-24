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

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.st2067_2.ApplicationComposition;
import com.netflix.imflibrary.st2067_2.ApplicationCompositionFactory;
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
public class IMPAnalyzerTestApp5
{
    @Test
    public void IMPAnalyzerTestApp5() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application5/PhotonApp5Test/");
        Map<String, List<ErrorLogger.ErrorObject>> errorMap = analyzePackage(inputFile);
        Assert.assertEquals(errorMap.size(), 7);
        errorMap.entrySet().stream().forEach( e ->
                {
                	if (e.getKey().matches("CPL.*")) {
                        Assert.assertEquals(e.getValue().size(), 3);
                    } else {
                        Assert.assertEquals(e.getValue().size(), 0);
                    }
                }
        );

    }

    @Test
    public void ValidApplicationTypeCPL() throws IOException
    {
        File inputFile = TestHelper.findResourceByPath("TestIMP/Application5/PhotonApp5Test/CPL_cfad00b4-77b5-4d06-bd9d-48bc21c8fc0e.xml");
        IMFErrorLogger logger = new IMFErrorLoggerImpl();

        ApplicationComposition applicationComposition = ApplicationCompositionFactory.getApplicationComposition(inputFile, logger);

        /* Make sure its 2020 core constraints */
        Assert.assertEquals(applicationComposition.getCoreConstraintsSchema(), "http://www.smpte-ra.org/schemas/2067-2/2016");

        /* Make sure its APP #5 Composition */
        Assert.assertEquals(applicationComposition.getApplicationCompositionType(), ApplicationCompositionFactory.ApplicationCompositionType.APPLICATION_5_COMPOSITION_TYPE);

        logger.getErrors().forEach(e -> {System.out.println(e.getErrorDescription());});
        Assert.assertEquals(logger.getErrors().size(), 3);
    }



}
