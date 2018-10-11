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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.firealarm.Fire;
import org.kie.firealarm.Room;
import org.kie.firealarm.Sprinkler;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class KProjectTestDMN extends BaseInterpretedVsCompiledTest {

    public KProjectTestDMN(boolean useExecModelCompiler) {
        super(useExecModelCompiler);
    }

    @Test
    public void testSolutionCase1() {
        executeTest( 16, 1, 27 );
    }

    private void executeTest( int age, int yearsService, int expectedVacationDays ) {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("0020-vacation-days.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel("https://www.drools.org/kie-dmn", "0020-vacation-days" );
        assertThat(dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();

        context.set( "Age", age );
        context.set( "Years of Service", yearsService );

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Total Vacation Days" ), is(BigDecimal.valueOf(expectedVacationDays ) ) );
    }
}
