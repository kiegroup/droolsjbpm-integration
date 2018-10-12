/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kproject;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.compiler.ExecModelCompilerOption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class KProjectTestDMN {

    @Test
    public void testSolutionCase1() {
        executeTest(16, 1, 27);
    }

    private void executeTest(int age, int yearsService, int expectedVacationDays) {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("0020-vacation-days.dmn", this.getClass());
        DMNModel dmnModel = runtime.getModel("https://www.drools.org/kie-dmn", "0020-vacation-days");
        assertThat(dmnModel, notNullValue());

        DMNContext context = DMNFactory.newContext();

        context.set("Age", age);
        context.set("Years of Service", yearsService);

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);

        DMNContext result = dmnResult.getContext();

        assertThat(result.get("Total Vacation Days"), is(BigDecimal.valueOf(expectedVacationDays)));
    }

    @Before
    public void before() {
        System.setProperty(ExecModelCompilerOption.PROPERTY_NAME, Boolean.toString(true));
    }

    @After
    public void after() {
        System.clearProperty(ExecModelCompilerOption.PROPERTY_NAME);
    }
}
