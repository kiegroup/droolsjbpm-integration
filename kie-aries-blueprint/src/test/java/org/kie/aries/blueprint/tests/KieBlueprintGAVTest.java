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

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.drools.example.api.namedkiesession.Message;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieSession;
import org.kie.aries.blueprint.KieBlueprintContainer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KieBlueprintGAVTest {

    static BlueprintContainerImpl container = null;

    @BeforeClass
    public static void setup() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(KieBlueprintListenerTest.class.getResource("/org/kie/aries/blueprint/gav.xml"));
        container = new KieBlueprintContainer(ClassLoader.getSystemClassLoader(), urls);
    }

    @Test
    public void testKieBase() throws Exception {
        KieBase kbase = (KieBase) container.getComponentInstance("kbase1");
        assertNotNull(kbase);
    }

    @Test
    public void testReleaseId() throws Exception {
        ReleaseId releaseId = (ReleaseId) container.getComponentInstance("rId");
        assertNotNull(releaseId);
    }

    @Test
    public void testKieSessionRef() throws Exception {
        KieSession ksession = (KieSession) container.getComponentInstance("ksession1");
        assertNotNull(ksession);
    }

    @Test
    public void testKSessionExecution() throws Exception {
        KieSession kSession = (KieSession) container.getComponentInstance("ksession1");
        assertNotNull(kSession);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        kSession.setGlobal("out", ps);
        kSession.insert(new Message("Dave", "Hello, HAL. Do you read me, HAL?"));
        kSession.fireAllRules();
        ps.close();

        String lineSeparator = System.getProperty("line.separator");
        String actual = new String(baos.toByteArray());
        String expected = "" +
                          "Dave: Hello, HAL. Do you read me, HAL?" +lineSeparator +
                          "HAL: Dave. I read you."+lineSeparator;
        assertEquals(expected, actual);
    }

    @AfterClass
    public static void tearDown() {
        container.destroy();
    }

}
