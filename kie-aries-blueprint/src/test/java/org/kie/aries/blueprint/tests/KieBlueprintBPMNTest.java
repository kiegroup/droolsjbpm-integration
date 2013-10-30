/*
 * Copyright 2013 JBoss Inc
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.aries.blueprint.KieBlueprintContainer;

import static org.junit.Assert.*;

@Ignore
public class KieBlueprintBPMNTest {

    static BlueprintContainerImpl container = null;

    @BeforeClass
    public static void setup() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(KieBlueprintBPMNTest.class.getResource("/org/kie/aries/blueprint/bpmn.xml"));
        container = new KieBlueprintContainer(ClassLoader.getSystemClassLoader(), urls);
    }

    @Test
    public void testEmptyRuntimeManager() throws Exception {
        Object obj = container.getComponentInstance("emptyRuntimeManager");
        assertNotNull(obj);
        assertTrue(obj instanceof RuntimeManager);
    }

    @Test
    public void testSimpleSession() throws Exception {
        Object obj = container.getComponentInstance("simpleSession");
        assertNotNull(obj);
        assertTrue(obj instanceof KieSession);
        KieSession ksession = (KieSession)obj;
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");

        ksession.startProcess("Evaluation", params);
    }

    @AfterClass
    public static void tearDown(){
        container.destroy();
    }
}
