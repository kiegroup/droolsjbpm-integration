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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
        KieSession KieSession = (KieSession) context.getBean("statefulSession");
        assertEquals(1, KieSession.getAgendaEventListeners().size());
        assertTrue(KieSession.getAgendaEventListeners().toArray()[0] instanceof MockAgendaEventListener);
    }

    @Test
    public void testStatefulProcessEventListener() throws Exception {
        KieSession KieSession = (KieSession) context.getBean("statefulSession");
        assertEquals(1, KieSession.getProcessEventListeners().size());
        assertTrue(KieSession.getProcessEventListeners().toArray()[0] instanceof MockProcessEventListener);
    }

    @Test
    public void testStatefulWMEventListener() throws Exception {
        KieSession KieSession = (KieSession) context.getBean("statefulSession");
        assertEquals(1, KieSession.getWorkingMemoryEventListeners().size());
        assertTrue(KieSession.getWorkingMemoryEventListeners().toArray()[0] instanceof MockWorkingMemoryEventListener);

        KieSession.setGlobal("list", list);
        KieSession.insert(new Person());
        KieSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }

    @Test
    public void testStatelessAgendaEventListener() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessSession");
        assertEquals(1, StatelessKieSession.getAgendaEventListeners().size());
        assertTrue(StatelessKieSession.getAgendaEventListeners().toArray()[0] instanceof MockAgendaEventListener);
    }

    @Test
    public void testStatelessProcessEventListener() throws Exception {
        StatelessKieSession kstateless = (StatelessKieSession) context.getBean("statelessSession");
        assertEquals(1, kstateless.getProcessEventListeners().size());
        assertTrue(kstateless.getProcessEventListeners().toArray()[0] instanceof MockProcessEventListener);
    }

    @Test
    public void testStatelessWMEventListener() throws Exception {
        StatelessKieSession kstateless = (StatelessKieSession) context.getBean("statelessSession");
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
        KieSession KieSession = (KieSession) context.getBean("statefulWithNestedBean");
        assertEquals(1, KieSession.getWorkingMemoryEventListeners().size());

        KieSession.setGlobal("list", list);
        KieSession.insert(new Person());
        KieSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatefulWithRef() throws Exception {
        KieSession KieSession = (KieSession) context.getBean("statefulWithRef");
        assertEquals(1, KieSession.getWorkingMemoryEventListeners().size());

        KieSession.setGlobal("list", list);
        KieSession.insert(new Person());
        KieSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatefulWithDefault() throws Exception {
        KieSession KieSession = (KieSession) context.getBean("statefulWithDefault");
        assertEquals(1, KieSession.getWorkingMemoryEventListeners().size());
        assertTrue(KieSession.getWorkingMemoryEventListeners().iterator().next() instanceof DebugRuleRuntimeEventListener);
    }

    @Test
    public void testStatefulWithLegacyDebugListener() throws Exception {
        KieSession KieSession = (KieSession) context.getBean("statefulWithDebugListener");
        assertEquals(1, KieSession.getWorkingMemoryEventListeners().size());
        assertTrue(KieSession.getWorkingMemoryEventListeners().iterator().next() instanceof DebugRuleRuntimeEventListener);
    }

    @Test
    public void testStatefulWithGroupedListeners() throws Exception {
        KieSession KieSession = (KieSession) context.getBean("statefulWithGroupedListeners");
        assertEquals(1, KieSession.getWorkingMemoryEventListeners().size());

        KieSession.setGlobal("list", list);
        KieSession.insert(new Person());
        KieSession.fireAllRules();
        //this assert to show that our listener was called X number of times.
        // once from agenda listener, and second from working memory event listener
        assertEquals(2, counterFromListener);
    }

    //stateless
    @Test
    public void testStatelessWithNestedBean() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessWithNestedBean");
        assertEquals(1, StatelessKieSession.getWorkingMemoryEventListeners().size());

        StatelessKieSession.setGlobal("list", list);
        StatelessKieSession.execute(new Person());
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatelessWithRef() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessWithRef");
        assertEquals(1, StatelessKieSession.getWorkingMemoryEventListeners().size());

        StatelessKieSession.setGlobal("list", list);
        StatelessKieSession.execute(new Person());
        //this assert to show that our listener was called X number of times.
        assertEquals(1, counterFromListener);
    }

    @Test
    public void testStatelessWithMultipleSameType() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessWithMultipleSameType");
        assertEquals(2, StatelessKieSession.getWorkingMemoryEventListeners().size());

        StatelessKieSession.setGlobal("list", list);
        StatelessKieSession.execute(new Person());
        // this assert to show that our listener was called X number of times.
        // two working memory listeners were added!
        assertEquals(2, counterFromListener);
    }

    @Test
    public void testStatelessWithDefault() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessWithDefault");
        assertEquals(1, StatelessKieSession.getWorkingMemoryEventListeners().size());
        assertTrue(StatelessKieSession.getWorkingMemoryEventListeners().toArray()[0] instanceof DebugRuleRuntimeEventListener);
    }

    @Test
    public void testStatelessWithGroupedListeners() throws Exception {
        StatelessKieSession StatelessKieSession = (StatelessKieSession) context.getBean("statelessWithGroupedListeners");
        assertEquals(1, StatelessKieSession.getWorkingMemoryEventListeners().size());

        StatelessKieSession.setGlobal("list", list);
        StatelessKieSession.execute(new Person());
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
