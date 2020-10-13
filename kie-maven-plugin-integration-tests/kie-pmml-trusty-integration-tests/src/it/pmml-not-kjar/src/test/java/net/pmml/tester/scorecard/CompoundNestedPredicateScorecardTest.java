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

package net.pmml.tester.scorecard;

import java.util.Arrays;
import java.util.Collection;

import net.pmml.tester.AbstractPMMLTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.pmml.api.runtime.PMMLRuntime;

@RunWith(Parameterized.class)
public class CompoundNestedPredicateScorecardTest extends AbstractPMMLTest {

    public static final String MODEL_NAME = "CompoundNestedPredicateScorecard";
    public static final String FILE_NAME = "CompoundNestedPredicateScorecard.pmml";

    public static Collection<Object[]> DATA = Arrays.asList(new Object[][]{
            {-50.0, "classB", -8.0, "characteristic2ReasonCode", null},
            {-50.0, "classD", -8.0, "characteristic2ReasonCode", null},
            {-9.0, "classB", 75.0, "characteristic1ReasonCode", null},
            {25.4, "classB", 75.0, "characteristic1ReasonCode", null},
            {-7.0, "classA", -8.0, "characteristic2ReasonCode", null},
            {-7.0, "classC", -15.5, "characteristic1ReasonCode", "characteristic2ReasonCode"},
            {5.0, "classB", -15.5, "characteristic1ReasonCode", "characteristic2ReasonCode"},
            {7.4, "classB", -15.5, "characteristic1ReasonCode", "characteristic2ReasonCode"},
            {12.0, "classB", 75.0, "characteristic1ReasonCode", null},
            {12.0, "classD", 75.0, "characteristic1ReasonCode", null},
    });
    private static PMMLRuntime localPMMLRuntime;

    static {
        localPMMLRuntime = getPMMLRuntime(MODEL_NAME, FILE_NAME);
    }

    public CompoundNestedPredicateScorecardTest(double input1, String input2, double score,
                                                String reasonCode1, String reasonCode2) {
        super(localPMMLRuntime);
        abstractPMMLExecutor = new CompoundNestedPredicateScorecardExecutor(input1, input2, score,
                                                                            reasonCode1, reasonCode2);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return DATA;
    }
}
