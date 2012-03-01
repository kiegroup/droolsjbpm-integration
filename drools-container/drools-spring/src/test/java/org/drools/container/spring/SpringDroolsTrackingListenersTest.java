/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.drools.container.spring;

import org.junit.BeforeClass;
import org.junit.Test;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Listener test with tracking rules
 * @author rsynek
 */
public class SpringDroolsTrackingListenersTest {

    private static ApplicationContext ctx;

    @BeforeClass
    public static void runBeforeClass() {
        ctx = new ClassPathXmlApplicationContext("org/drools/container/spring/listenersTest.xml");
    }
    
    private StatefulKnowledgeSession getSession() {
        return (StatefulKnowledgeSession) ctx.getBean("statefulSession");
    }

    /**
     * reproducer for https://bugzilla.redhat.com/show_bug.cgi?id=761427
     */
    @Test
    public void testListeners() {
        StatefulKnowledgeSession session = getSession();
        assertEquals(session.getAgendaEventListeners().size(), 1);
        session.fireAllRules();
        TrackingAgendaEventListener listener = (TrackingAgendaEventListener) session.getAgendaEventListeners().iterator().next();
        assertTrue(listener.isRuleFired("sample rule"));
    }
}
