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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.InternalKieSpringUtils;
import org.kie.spring.beans.Person;
import org.kie.spring.mocks.MockAgendaEventListener;
import org.kie.spring.mocks.MockProcessEventListener;
import org.kie.spring.mocks.MockWorkingMemoryEventListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KieSpringListenersTest {

    static ApplicationContext context = null;
    List<Person> list = new ArrayList<Person>();
    static int counterFromListener = 0;

    @BeforeClass
    public static void runBeforeClass() {
        ReleaseId releaseId = new ReleaseIdImpl("sample-group","test-spring","0001");
        context = InternalKieSpringUtils.getSpringContext(releaseId,
                KieSpringListenersTest.class.getResource("/org/kie/spring/listeners.xml"),
                new File(KieSpringListenersTest.class.getResource("/").getFile()));
    }

    @Before
    public void clearGlobalList() {
        list.clear();
        counterFromListener = 0;
    }

    /*
    This method is called from the listeners
        MockWorkingMemoryEventListener.objectInserted
        MockAgendaEventListener.beforeActivationFired
     */
    public static void incrementValueFromListener() {
        counterFromListener++;
    }


    @Test
    public void testStatefulAgendaEventListenerEmbedded() throws Exception {
        KieSession kSession = (KieSession) context.getBean("ksession1");
        assertTrue(kSession.getAgendaEventListeners().size() > 0);
        boolean mockAgendaEventListenerFound = false;
        for (AgendaEventListener listener : kSession.getAgendaEventListeners()) {
            if (listener instanceof MockAgendaEventListener) {
                mockAgendaEventListenerFound = true;
                break;
            }
        }
        assertTrue(mockAgendaEventListenerFound);
    }

    @Test
    public void testStatefulAgendaEventListener() throws Exception {
        KieSession kSession = (KieSession) context.getBean("ksession2");
        assertTrue(kSession.getAgendaEventListeners().size() > 0);
        boolean mockAgendaEventListenerFound = false;
        for (AgendaEventListener listener : kSession.getAgendaEventListeners()) {
            if (listener instanceof MockAgendaEventListener) {
                mockAgendaEventListenerFound = true;
                break;
            }
        }
        assertTrue(mockAgendaEventListenerFound);
    }

    @Test
    public void testStatefulProcessEventListener() throws Exception {
        KieSession kSession = (KieSession) context.getBean("ksession2");
        assertTrue(kSession.getProcessEventListeners().size() > 0);
        boolean mockProcessEventListenerFound = false;
        for (ProcessEventListener listener : kSession.getProcessEventListeners()) {
            if (listener instanceof MockProcessEventListener) {
                mockProcessEventListenerFound = true;
                break;
            }
        }
        assertTrue(mockProcessEventListenerFound);
    }

    @Test
    public void testStatefulWMEventListener() throws Exception {
        KieSession kSession = (KieSession) context.getBean("ksession2");
        assertTrue(kSession.getWorkingMemoryEventListeners().size() > 0);
        boolean mockWMEventListenerFound = false;
        for (WorkingMemoryEventListener listener : kSession.getWorkingMemoryEventListeners()) {
            if (listener instanceof MockWorkingMemoryEventListener) {
                mockWMEventListenerFound = true;
                break;
            }
        }
        assertTrue(mockWMEventListenerFound);

        kSession.insert(new Person());
        kSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertTrue(counterFromListener > 0);
    }

    @Test
    public void testStatelessWithGroupedListeners() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessWithGroupedListeners");
        assertEquals(1, StatelessKieSession.getWorkingMemoryEventListeners().size());

        StatelessKieSession.execute(new Person());
        // this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }
}
