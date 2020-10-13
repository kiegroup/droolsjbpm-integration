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

import java.util.HashMap;
import java.util.Map;

import net.pmml.trusty.container.tester.AbstractPMMLExecutor;
import org.assertj.core.api.Assertions;
import org.kie.api.pmml.PMML4Result;
import org.kie.pmml.api.runtime.PMMLRuntime;

import static org.kie.pmml.api.enums.ResultCode.FAIL;
import static org.kie.pmml.api.enums.ResultCode.OK;

public class PredicatesMiningExecutor extends AbstractPMMLExecutor {

    private static final String MODEL_NAME = "PredicatesMining";
    private static final String TARGET_FIELD = "categoricalResult";

    private String categoricalX;
    private String categoricalY;
    private double age;
    private String occupation;
    private String residenceState;
    private boolean validLicense;
    private double variable;
    private Double expectedResult;

    public PredicatesMiningExecutor(String residenceState,
                                    boolean validLicense,
                                    String occupation,
                                    String categoricalY,
                                    String categoricalX,
                                    double variable,
                                    double age,
                                    Double expectedResult) {
        this.residenceState = residenceState;
        this.validLicense = validLicense;
        this.occupation = occupation;
        this.categoricalY = categoricalY;
        this.categoricalX = categoricalX;
        this.variable = variable;
        this.age = age;
        this.expectedResult = expectedResult;
    }

    @Override
    public void test(final PMMLRuntime pmmlRuntime) {
        final Map<String, Object> inputData = new HashMap<>();
        inputData.put("residenceState", residenceState);
        inputData.put("validLicense", validLicense);
        inputData.put("occupation", occupation);
        inputData.put("categoricalY", categoricalY);
        inputData.put("categoricalX", categoricalX);
        inputData.put("variable", variable);
        inputData.put("age", age);
        PMML4Result pmml4Result = evaluate(pmmlRuntime, inputData, MODEL_NAME);
        if (expectedResult != null) {
            Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isNotNull();
            Assertions.assertThat(pmml4Result.getResultCode()).isEqualTo(OK.getName());
        } else {
            Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isNull();
            Assertions.assertThat(pmml4Result.getResultCode()).isEqualTo(FAIL.getName());
        }
        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isEqualTo(expectedResult);
    }
}
