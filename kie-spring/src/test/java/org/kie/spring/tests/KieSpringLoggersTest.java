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

package org.kie.spring.tests;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.audit.ThreadedWorkingMemoryFileLogger;
import org.drools.core.audit.WorkingMemoryConsoleLogger;
import org.drools.core.audit.WorkingMemoryFileLogger;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.impl.StatelessKnowledgeSessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.InternalKieSpringUtils;
import org.kie.spring.beans.Person;
import org.kie.spring.factorybeans.LoggerAdaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class KieSpringLoggersTest {

    static ApplicationContext context = null;

    @BeforeClass
    public static void runBeforeClass() {
        ReleaseId releaseId = new ReleaseIdImpl("kie-spring-loggers","test-spring","0001");
        URL configFileURL =  InternalKieSpringUtilsTest.class.getResource("/org/kie/spring/loggers.xml");
        context = InternalKieSpringUtils.getSpringContext(releaseId,configFileURL);
    }

    @AfterClass
    public static void runAfterClass() {
    }

    @Test
    public void testStatelessSessionRefConsoleLogger() throws Exception {
        StatelessKieSession session = (StatelessKieSession) context.getBean("loggerSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) session;
        for (Object listener : impl.mappedWorkingMemoryListeners.values()) {
            assertTrue(listener instanceof WorkingMemoryConsoleLogger);
        }
    }

    @Test
    public void testStatefulKnowledgeConsoleLogger() throws Exception {
        KieSession statefulSession = (KieSession) context.getBean("ConsoleLogger-statefulSession");
        StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) statefulSession;
        for (Object listener : impl.session.getWorkingMemoryEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryConsoleLogger);
        }
    }

    @Test
    public void testStatefulKnowledgeFileLogger() throws Exception {
        KieSession statefulSession = (KieSession) context.getBean("FileLogger-statefulSession");
        assertNotNull(statefulSession.getGlobals().get("persons"));
        StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) statefulSession;
        for (Object listener : impl.session.getWorkingMemoryEventListeners()) {
            assertTrue(listener instanceof WorkingMemoryFileLogger);
        }
        LoggerAdaptor adaptor = (LoggerAdaptor) context.getBean("sf_fl_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());

    }

    @Test
    public void testStatefulKnowledgeThreadedFileLogger() throws Exception {
        KieSession statefulSession = (KieSession) context.getBean("ThreadedFileLogger-statefulSession");
        assertNotNull(statefulSession.getGlobals().get("persons"));
        StatefulKnowledgeSessionImpl impl = (StatefulKnowledgeSessionImpl) statefulSession;
        for (Object listener : impl.session.getWorkingMemoryEventListeners()) {
            assertTrue(listener instanceof ThreadedWorkingMemoryFileLogger);
        }
        LoggerAdaptor adaptor = (LoggerAdaptor) context.getBean("sf_tfl_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
    }

    @Test
    public void testStatelessKnowledgeConsoleLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) context.getBean("ConsoleLogger-statelessSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.mappedWorkingMemoryListeners.values()) {
            assertTrue(listener instanceof WorkingMemoryConsoleLogger);
        }
    }

    @Test
    public void testStatelessKnowledgeFileLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) context.getBean("FileLogger-statelessSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.mappedWorkingMemoryListeners.values()) {
            assertTrue(listener instanceof WorkingMemoryFileLogger);
        }
        LoggerAdaptor adaptor = (LoggerAdaptor) context.getBean("ss_fl_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
    }

    @Test
    public void testStatelessKnowledgeThreadedFileLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) context.getBean("ThreadedFileLogger-statelessSession");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.mappedWorkingMemoryListeners.values()) {
            assertTrue(listener instanceof ThreadedWorkingMemoryFileLogger);
        }
        LoggerAdaptor loggerAdaptor = (LoggerAdaptor) context.getBean("ss_tfl_logger");
        assertNotNull(loggerAdaptor);
        assertNotNull(loggerAdaptor.getRuntimeLogger());
        loggerAdaptor.close();
    }

    @Test
    public void testSessionLoggersFromGroupAndNested() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) context.getBean("k1");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        assertEquals(2, impl.mappedWorkingMemoryListeners.values().size());

        List list = new ArrayList();
        statelessKnowledgeSession.setGlobal("persons", list);
        assertNotNull(statelessKnowledgeSession.getGlobals().get("persons"));
        statelessKnowledgeSession.execute(new Person("Darth", "Cheddar", 50));

        LoggerAdaptor adaptor = (LoggerAdaptor) context.getBean("k1_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
        adaptor.close();

        adaptor = (LoggerAdaptor) context.getBean("k1_console_logger");
        assertNotNull(adaptor);
        assertNotNull(adaptor.getRuntimeLogger());
    }

    @Test
    public void testStatelessNoNameFileLogger() throws Exception {
        StatelessKieSession statelessKnowledgeSession = (StatelessKieSession) context.getBean("FileLogger-statelessSession-noNameLogger");
        StatelessKnowledgeSessionImpl impl = (StatelessKnowledgeSessionImpl) statelessKnowledgeSession;
        for (Object listener : impl.mappedWorkingMemoryListeners.values()) {
            assertTrue(listener instanceof WorkingMemoryFileLogger);
        }
    }
}
