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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.dmn.core.compiler.ExecModelCompilerOption;
import org.kie.firealarm.Fire;
import org.kie.firealarm.Room;
import org.kie.firealarm.Sprinkler;

import static org.junit.Assert.*;

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

    @Before
    public void before() {
        System.setProperty(ExecModelCompilerOption.PROPERTY_NAME, Boolean.toString(true));
    }

    @After
    public void after() {
        System.clearProperty(ExecModelCompilerOption.PROPERTY_NAME);
    }
}