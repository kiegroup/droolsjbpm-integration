/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.aries.blueprint.tests;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.aries.blueprint.KieBlueprintContainer;
import org.kie.aries.blueprint.beans.Person;

import static org.junit.Assert.*;

public class KieBlueprintCommandsTest {

    static BlueprintContainerImpl container = null;

    @BeforeClass
    public static void runBeforeClass() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(KieBlueprintCommandsTest.class.getResource("/org/kie/aries/blueprint/beans-commands.xml"));
        container = new KieBlueprintContainer(ClassLoader.getSystemClassLoader(), urls);
    }


    @Test
    public void testKieBase() throws Exception {
        KieBase kbase = (KieBase) container.getComponentInstance("drl_kiesample");
        assertNotNull(kbase);
    }

    @Test
    public void testInsertObject() throws Exception {
        KieSession ksession = (KieSession) container.getComponentInstance("ksession2");
        assertNotNull(ksession);

        assertEquals(1, ksession.getObjects().size());
        assertTrue(ksession.getObjects().toArray()[0] instanceof Person);

        for (Object object : ksession.getObjects()) {
            if (object instanceof Person) {
                assertFalse(((Person) object).isHappy());
            }
        }

        ksession.fireAllRules();

        //if the rules have fired, then the setHappy(true) should have been called
        for (Object object : ksession.getObjects()) {
            if (object instanceof Person) {
                assertTrue(((Person) object).isHappy());
            }
        }
    }

    @Test
    public void testInsertObjectAndFireAll() throws Exception {
        KieSession ksession = (KieSession) container.getComponentInstance("ksessionForCommands");
        assertNotNull(ksession);

        assertEquals(1, ksession.getObjects().size());
        assertTrue(ksession.getObjects().toArray()[0] instanceof Person);

        //if the rules should have fired without any invoke of fireAllRules, then the setHappy(true) should have been called
        for (Object object : ksession.getObjects()) {
            if (object instanceof Person) {
                assertTrue(((Person) object).isHappy());
            }
        }
    }

    @Test
    public void testSetGlobals() throws Exception {
        KieSession ksession = (KieSession) container.getComponentInstance("ksessionForCommands");
        assertNotNull(ksession);

        assertEquals(1, ksession.getObjects().size());
        assertTrue(ksession.getObjects().toArray()[0] instanceof Person);
        Person p1 = (Person) ksession.getObjects().toArray()[0];
        assertNotNull(p1);
        //if the rules should have fired without any invoke of fireAllRules, then the setHappy(true) should have been called
        for (Object object : ksession.getObjects()) {
            if (object instanceof Person) {
                assertTrue(((Person) object).isHappy());
            }
        }

        Object list = ksession.getGlobal("persons");
        assertNotNull(list);
        assertTrue(list instanceof ArrayList);
        assertEquals(1, ((ArrayList) list).size());
        Person p = (Person) ((ArrayList) list).get(0);
        assertNotNull(p);
        assertEquals(p, p1);
    }

    @AfterClass
    public static void tearDown() {
        container.destroy();
    }

}
