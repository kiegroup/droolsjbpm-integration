/*
 * Copyright 2010 JBoss Inc
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

import org.drools.Person;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.event.rule.DebugWorkingMemoryEventListener;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.StatelessKnowledgeSession;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpringDroolsListenersTest {

    static ClassPathXmlApplicationContext context = null;
    List<Person> list = new ArrayList<Person>();
    static int counterFromListener=0;

    @BeforeClass
    public static void runBeforeClass() {
        context = new ClassPathXmlApplicationContext("org/drools/container/spring/listeners.xml");
    }

    @Before
    public void clearGlobalList(){
        list.clear();
        counterFromListener = 0;
    }

    /*
    This method is called from the listeners
        MockWorkingMemoryEventListener.objectInserted
        MockAgendaEventListener.beforeActivationFired
     */
    public static void incrementValueFromListener(){
        counterFromListener++;
    }

    //@Test
    public void testStatefulAgendaEventListener() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean("statefulSession");
        assertEquals(1, statefulKnowledgeSession.getAgendaEventListeners().size());
        assertTrue(statefulKnowledgeSession.getAgendaEventListeners().toArray()[0] instanceof MockAgendaEventListener);
    }

    @Test
    public void testStatefulProcessEventListener() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean("statefulSession");
        assertEquals(1, statefulKnowledgeSession.getProcessEventListeners().size());
        assertTrue(statefulKnowledgeSession.getProcessEventListeners().toArray()[0] instanceof MockProcessEventListener);
    }

    @Test
    public void testStatefulWMEventListener() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean("statefulSession");
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
        assertTrue(statefulKnowledgeSession.getWorkingMemoryEventListeners().toArray()[0] instanceof MockWorkingMemoryEventListener);

        statefulKnowledgeSession.setGlobal("list", list);
        statefulKnowledgeSession.insert(new Person());
        statefulKnowledgeSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }

    @Test
    public void testStatelessAgendaEventListener() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean("statelessSession");
        assertEquals(1, statelessKnowledgeSession.getAgendaEventListeners().size());
        assertTrue(statelessKnowledgeSession.getAgendaEventListeners().toArray()[0] instanceof MockAgendaEventListener);
    }

    @Test
    public void testStatelessProcessEventListener() throws Exception {
        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean("statelessSession");
        assertEquals(1, kstateless.getProcessEventListeners().size());
        assertTrue(kstateless.getProcessEventListeners().toArray()[0] instanceof MockProcessEventListener);
    }

    @Test
    public void testStatelessWMEventListener() throws Exception {
        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean("statelessSession");
        assertEquals(1, kstateless.getWorkingMemoryEventListeners().size());
        assertTrue(kstateless.getWorkingMemoryEventListeners().toArray()[0] instanceof MockWorkingMemoryEventListener);

        kstateless.setGlobal("list", list);
        kstateless.execute(new Person());
        //this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }

    @Test
    public void testStatefulWithNestedBean() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean("statefulWithNestedBean");
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());

        statefulKnowledgeSession.setGlobal("list", list);
        statefulKnowledgeSession.insert(new Person());
        statefulKnowledgeSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatefulWithRef() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean("statefulWithRef");
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());

        statefulKnowledgeSession.setGlobal("list", list);
        statefulKnowledgeSession.insert(new Person());
        statefulKnowledgeSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatefulWithDefault() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean("statefulWithDefault");
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
        assertTrue(statefulKnowledgeSession.getWorkingMemoryEventListeners().iterator().next() instanceof DebugWorkingMemoryEventListener);
    }

    @Test
    public void testStatefulWithLegacyDebugListener() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean("statefulWithDebugListener");
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
        assertTrue(statefulKnowledgeSession.getWorkingMemoryEventListeners().iterator().next() instanceof DebugWorkingMemoryEventListener);
    }

    @Test
    public void testStatefulWithGroupedListeners() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean("statefulWithGroupedListeners");
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());

        statefulKnowledgeSession.setGlobal("list", list);
        statefulKnowledgeSession.insert(new Person());
        statefulKnowledgeSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }

    //stateless
    @Test
    public void testStatelessWithNestedBean() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean("statelessWithNestedBean");
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());

        statelessKnowledgeSession.setGlobal("list", list);
        statelessKnowledgeSession.execute(new Person());
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatelessWithRef() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean("statelessWithRef");
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());

        statelessKnowledgeSession.setGlobal("list", list);
        statelessKnowledgeSession.execute(new Person());
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatelessWithMultipleSameType() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean("statelessWithMultipleSameType");
        assertEquals(2, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());

        statelessKnowledgeSession.setGlobal("list", list);
        statelessKnowledgeSession.execute(new Person());
        // this assert to show that our listener was called X number of times.
        // two working memory listeners were added!
        assertEquals(2, counterFromListener);
    }

    @Test
    public void testStatelessWithDefault() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean("statelessWithDefault");
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());
        assertTrue(statelessKnowledgeSession.getWorkingMemoryEventListeners().toArray()[0] instanceof DebugWorkingMemoryEventListener);
    }

    @Test
    public void testStatelessWithGroupedListeners() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean("statelessWithGroupedListeners");
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());

        statelessKnowledgeSession.setGlobal("list", list);
        statelessKnowledgeSession.execute(new Person());
        // this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }

    @Test
    public void testEventListenersBean() throws Exception {
        Object debugListeners = context.getBean("debugListeners");
        assertTrue(debugListeners instanceof List);
        assertEquals(3, ((List) debugListeners).size());
    }
}
