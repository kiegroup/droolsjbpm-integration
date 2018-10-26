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
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.compiler.ExecModelCompilerOption;
import org.kie.firealarm.Fire;
import org.kie.firealarm.Room;
import org.kie.firealarm.Sprinkler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class KProjectTest {

    @Test
    public void testKJar() throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession("FireAlarmKBase.session");

        Room room = new Room("101");
        kSession.insert(room);
        Sprinkler sprinkler = new Sprinkler(room);
        kSession.insert(sprinkler);
        Fire fire = new Fire(room);
        FactHandle fireFH = kSession.insert(fire);

        int rules = kSession.fireAllRules();
        assertEquals(2, rules);

        kSession.delete(fireFH);
        rules = kSession.fireAllRules();
        assertEquals(3, rules);
    }

    @Test
    public void testSolutionCase1() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("org/kie/example/0020-vacation-days.dmn", this.getClass());
        DMNModel dmnModel = runtime.getModel("https://www.drools.org/kie-dmn", "0020-vacation-days");
        assertThat(dmnModel, notNullValue());

        DMNContext context = DMNFactory.newContext();

        context.set("Age", 16);
        context.set("Years of Service", 1);

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);

        DMNContext result = dmnResult.getContext();

        assertThat(result.get("Total Vacation Days"), is(BigDecimal.valueOf(27)));
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
