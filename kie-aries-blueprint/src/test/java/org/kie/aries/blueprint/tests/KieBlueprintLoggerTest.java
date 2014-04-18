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

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.drools.core.audit.ThreadedWorkingMemoryFileLogger;
import org.drools.core.audit.WorkingMemoryConsoleLogger;
import org.drools.core.audit.WorkingMemoryFileLogger;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.impl.StatelessKnowledgeSessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.aries.blueprint.KieBlueprintContainer;
import org.kie.aries.blueprint.beans.Person;
import org.kie.aries.blueprint.factorybeans.KieLoggerAdaptor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore("Add when org.apache.aries.blueprint.noosgi 1.0.1 is released")
public class KieBlueprintLoggerTest {

    static BlueprintContainerImpl container = null;

    @BeforeClass
    public static void runBeforeClass() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(KieBlueprintListenerTest.class.getResource("/org/kie/aries/blueprint/loggers.xml"));
        container = new KieBlueprintContainer(ClassLoader.getSystemClassLoader(), urls);
    }

    @AfterClass
    public static void runAfterClass() {
        container.destroy();
    }

    @Test
    public void testStatelessSessionRefConsoleLogger() throws Exception {
        StatelessKieSession session = (StatelessKieSession) container.getComponentInstance("loggerSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) session;
        KieLoggerAdaptor kieLoggerAdaptor = (KieLoggerAdaptor) container.getComponentInstance("ConsoleSessionLogger");
        assertNotNull(kieLoggerAdaptor);
        assertNotNull(kieLoggerAdaptor.getRuntimeLogger());
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryConsoleLogger);
        }
    }

    @Test
    public void testStatefulKnowledgeConsoleLogger() throws Exception {
        KieSession statefulSession = (KieSession) container.getComponentInstance("ConsoleLogger-statefulSession");
        StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) statefulSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryConsoleLogger);
        }
    }

    @Test
    public void testStatefulKnowledgeFileLogger() throws Exception {
        KieSession statefulSession = (KieSession) container.getComponentInstance("FileLogger-statefulSession");
       // assertNotNull(statefulSession.getGlobals().get("persons"));
        StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) statefulSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryFileLogger);
        }
        KieLoggerAdaptor adaptor = (KieLoggerAdaptor) container.getComponentInstance("sf_fl_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());

    }

    @Test
    public void testStatefulKnowledgeThreadedFileLogger() throws Exception {
        KieSession statefulSession = (KieSession) container.getComponentInstance("ThreadedFileLogger-statefulSession");
//        assertNotNull(statefulSession.getGlobals().get("persons"));
        StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) statefulSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof ThreadedWorkingMemoryFileLogger);
        }
        KieLoggerAdaptor adaptor = (KieLoggerAdaptor) container.getComponentInstance("sf_tfl_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
    }

    @Test
    public void testStatelessKnowledgeConsoleLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) container.getComponentInstance("ConsoleLogger-statelessSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryConsoleLogger);
        }
    }

    @Test
    public void testStatelessKnowledgeFileLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) container.getComponentInstance("FileLogger-statelessSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryFileLogger);
        }
        KieLoggerAdaptor adaptor = (KieLoggerAdaptor) container.getComponentInstance("ss_fl_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
    }

    @Test
    public void testStatelessKnowledgeThreadedFileLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) container.getComponentInstance("ThreadedFileLogger-statelessSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof ThreadedWorkingMemoryFileLogger);
        }
        KieLoggerAdaptor loggerAdaptor = (KieLoggerAdaptor) container.getComponentInstance("ss_tfl_logger");
        assertNotNull(loggerAdaptor);
        assertNotNull(loggerAdaptor.getRuntimeLogger());
        loggerAdaptor.close();
    }

    @Test
    public void testSessionLoggersFromGroupAndNested() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) container.getComponentInstance("k1");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        assertEquals(2, impl.getRuleRuntimeEventListeners().size());

        List list = new ArrayList();
        statelessKnowledgeSession.setGlobal("persons", list);
        assertNotNull(statelessKnowledgeSession.getGlobals().get("persons"));
        statelessKnowledgeSession.execute(new Person("Darth", "Cheddar", 50));

        KieLoggerAdaptor adaptor = (KieLoggerAdaptor) container.getComponentInstance("k1_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
        adaptor.close();

        adaptor = (KieLoggerAdaptor) container.getComponentInstance("k1_console_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
    }

    @Test
    public void testStatelessNoNameFileLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) container.getComponentInstance("FileLogger-statelessSession-noNameLogger");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.getRuleRuntimeEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryFileLogger);
        }
    }
}
