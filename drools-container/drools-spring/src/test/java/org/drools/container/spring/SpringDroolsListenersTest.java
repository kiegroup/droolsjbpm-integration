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

import org.drools.ClockType;
import org.drools.Person;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseConfiguration.AssertBehaviour;
import org.drools.SessionConfiguration;
import org.drools.agent.impl.KnowledgeAgentImpl;
import org.drools.common.InternalRuleBase;
import org.drools.conf.EventProcessingOption;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.drools.grid.GridNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ResourceChangeScannerImpl;
import org.drools.io.impl.UrlResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.jbpm.process.instance.impl.humantask.HumanTaskHandler;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.security.KeyStoreSpi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SpringDroolsListenersTest {

    static ClassPathXmlApplicationContext context = null;
    @BeforeClass
	public static void runBeforeClass() {
         context = new ClassPathXmlApplicationContext( "org/drools/container/spring/listeners.xml" );
    }

    @Test
    public void testStatefulAgendaEventListener() throws Exception {

        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean( "statefulSession" );
        assertEquals(1, statefulKnowledgeSession.getAgendaEventListeners().size());
        assertTrue(statefulKnowledgeSession.getAgendaEventListeners().toArray()[0] instanceof MockAgendaEventListener);

        List<String> list = new ArrayList<String>();
        statefulKnowledgeSession.setGlobal( "list", list );
        statefulKnowledgeSession.insert(new Person( "Darth", "Cheddar", 50 ));
        statefulKnowledgeSession.fireAllRules();
    }

    @Test
    public void testStatefulProcessEventListener() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean( "statefulSession" );
        assertEquals(1, statefulKnowledgeSession.getProcessEventListeners().size());
        assertTrue(statefulKnowledgeSession.getProcessEventListeners().toArray()[0] instanceof MockProcessEventListener);
    }

    @Test
    public void testStatefullWMEventListener() throws Exception {
        StatefulKnowledgeSession statefulKnowledgeSession = (StatefulKnowledgeSession) context.getBean( "statefulSession" );
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
        assertTrue(statefulKnowledgeSession.getWorkingMemoryEventListeners().toArray()[0] instanceof MockWorkingMemoryEventListener);
    }

    @Test
    public void testStatelessAgendaEventListener() throws Exception {
        StatelessKnowledgeSessionImpl statelessKnowledgeSession = (StatelessKnowledgeSessionImpl) context.getBean( "statelessSession" );
        assertEquals(1, statelessKnowledgeSession.getAgendaEventListeners().size());
        assertTrue(statelessKnowledgeSession.getAgendaEventListeners().toArray()[0] instanceof MockAgendaEventListener);

        List<String> list = new ArrayList<String>();
        statelessKnowledgeSession.setGlobal( "list", list );
        statelessKnowledgeSession.execute(new Person( "Darth", "Cheddar", 50 ));
    }

    @Test
    public void testStatelessProcessEventListener() throws Exception {
        StatelessKnowledgeSessionImpl kstateless = (StatelessKnowledgeSessionImpl) context.getBean( "statelessSession" );
        assertEquals(1, kstateless.getProcessEventListeners().size());
        assertTrue(kstateless.getProcessEventListeners().toArray()[0] instanceof MockProcessEventListener);
    }

    @Test
    public void testStatelessWMEventListener() throws Exception {
        StatelessKnowledgeSessionImpl kstateless = (StatelessKnowledgeSessionImpl) context.getBean( "statelessSession" );
        assertEquals(1, kstateless.getWorkingMemoryEventListeners().size());
        assertTrue(kstateless.getWorkingMemoryEventListeners().toArray()[0] instanceof MockWorkingMemoryEventListener);
    }

    @Test
    public void testStatefulWithNestedBeanAndType() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean( "statefulWithNestedBeanAndType" );
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    @Test
    public void testStatefulWithNestedBeanAndNoType() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean( "statefulWithNestedBeanAndNoType" );
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    @Test
    public void testStatefulWithRefAndNoType() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean( "statefulWithRefAndNoType" );
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    @Test
    public void testStatefulWithRefAndType() throws Exception {
        StatefulKnowledgeSessionImpl statefulKnowledgeSession = (StatefulKnowledgeSessionImpl) context.getBean( "statefulWithRefAndType" );
        assertEquals(1, statefulKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    //stateless
    @Test
    public void testStatelessWithNestedBeanAndType() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean( "statelessWithNestedBeanAndType" );
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    @Test
    public void testStatelessWithNestedBeanAndNoType() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean( "statelessWithNestedBeanAndNoType" );
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    @Test
    public void testStatelessWithRefAndNoType() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean( "statelessWithRefAndNoType" );
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());
    }

    @Test
    public void testStatelessWithRefAndType() throws Exception {
        StatelessKnowledgeSession statelessKnowledgeSession = (StatelessKnowledgeSession) context.getBean( "statelessWithRefAndType" );
        assertEquals(1, statelessKnowledgeSession.getWorkingMemoryEventListeners().size());
    }
}
