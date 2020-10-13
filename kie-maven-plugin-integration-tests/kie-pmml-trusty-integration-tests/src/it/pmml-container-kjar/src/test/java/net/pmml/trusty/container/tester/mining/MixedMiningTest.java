/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.pmml.trusty.container.tester.mining;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.pmml.api.runtime.PMMLRuntime;
import net.pmml.trusty.container.tester.AbstractPMMLTest;

@RunWith(Parameterized.class)
public class MixedMiningTest extends AbstractPMMLTest {

    public static final String MODEL_NAME = "MixedMining";
    public static final String FILE_NAME = "miningmodelmixed/MiningModel_Mixed.pmml";

    public static final Collection<Object[]> DATA = Arrays.asList(new Object[][]{
            {"red", "classA", 25.0, "ASTRONAUT", "AP", true, 5, 0, 0, 1.9149999999999991},
            {"blue", "classA", 2.3, "PROGRAMMER", "KN", true, 2, -5, 0, 6.081666666666666},
            {"yellow", "classC", 333.56, "INSTRUCTOR", "TN", false, 2, 7, 0, -13.334999999999999},
            {"orange", "classB", 0.12, "ASTRONAUT", "KN", true, 10, 7, 0, 6.581666666666666},
            {"green", "classC", 122.12, "TEACHER", "TN", false, 10, -7, 10, 26.081666666666667},
            {"green", "classB", 11.33, "INSTRUCTOR", "AP", false, 2, -5, 0, 14.748333333333333},
            {"orange", "classB", 423.2, "SKYDIVER", "KN", true, 5, 0, 0, 10.248333333333333},
    });
    private static PMMLRuntime localPMMLRuntime;

    static {
        localPMMLRuntime = getPMMLRuntime(MODEL_NAME, FILE_NAME);
    }

    public MixedMiningTest(String categoricalX,
                           String categoricalY,
                           double age,
                           String occupation,
                           String residenceState,
                           boolean validLicense,
                           double input1,
                           double input2,
                           double input3,
                           double expectedResult) {
        super(localPMMLRuntime);
        abstractPMMLExecutor = new MixedMiningExecutor(categoricalX,
                                                       categoricalY,
                                                       age,
                                                       occupation,
                                                       residenceState,
                                                       validLicense,
                                                       input1,
                                                       input2,
                                                       input3,
                                                       expectedResult);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return DATA;
    }
}
