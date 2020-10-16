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

package net.pmml.tester.mining;

import java.util.HashMap;
import java.util.Map;

import net.pmml.tester.AbstractPMMLExecutor;
import org.assertj.core.api.Assertions;
import org.kie.api.pmml.PMML4Result;
import org.kie.pmml.api.runtime.PMMLRuntime;

public class MixedMiningExecutor extends AbstractPMMLExecutor {

    private static final String MODEL_NAME = "MixedMining";
    private static final String TARGET_FIELD = "categoricalResult";

    private String categoricalX;
    private String categoricalY;
    private double age;
    private String occupation;
    private String residenceState;
    private boolean validLicense;
    private double input1;
    private double input2;
    private double input3;
    private double expectedResult;

    public MixedMiningExecutor(String categoricalX,
                               String categoricalY,
                               double age,
                               String occupation,
                               String residenceState,
                               boolean validLicense,
                               double input1,
                               double input2,
                               double input3,
                               double expectedResult) {
        this.categoricalX = categoricalX;
        this.categoricalY = categoricalY;
        this.age = age;
        this.occupation = occupation;
        this.residenceState = residenceState;
        this.validLicense = validLicense;
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
        this.expectedResult = expectedResult;
    }

    @Override
    public void test(final PMMLRuntime pmmlRuntime) {
        final Map<String, Object> inputData = new HashMap<>();
        inputData.put("categoricalX", categoricalX);
        inputData.put("categoricalY", categoricalY);
        inputData.put("age", age);
        inputData.put("occupation", occupation);
        inputData.put("residenceState", residenceState);
        inputData.put("validLicense", validLicense);
        inputData.put("input1", input1);
        inputData.put("input2", input2);
        inputData.put("input3", input3);
        PMML4Result pmml4Result = evaluate(pmmlRuntime, inputData, MODEL_NAME);
        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isNotNull();
        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isEqualTo(expectedResult);
    }
}
