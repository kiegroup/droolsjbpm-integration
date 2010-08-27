/**
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.drools.ClockType;
import org.drools.Person;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseConfiguration.AssertBehaviour;
import org.drools.SessionConfiguration;
import org.drools.agent.impl.KnowledgeAgentImpl;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.common.InternalRuleBase;
import org.drools.conf.EventProcessingOption;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.drools.grid.ExecutionNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ResourceChangeScannerImpl;
import org.drools.io.impl.UrlResource;
import org.drools.process.instance.impl.humantask.HumanTaskHandler;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDroolsTest extends TestCase {
   
    
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testNoConnection() throws Exception {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

            ExecutionNode node2 = (ExecutionNode) context.getBean( "node2" );
            assertNotNull( node2 );
    }

    public void testNoNodeKSessions() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/no-node-beans.xml" );

        List<String> list = new ArrayList<String>();
        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean( "ksession1" );
        kstateless.setGlobal( "list",
                              list );
        kstateless.execute( new Person( "Darth",
                                        "Cheddar",
                                        50 ) );
        assertEquals( 1,
                      list.size() );

        list = new ArrayList<String>();
        StatefulKnowledgeSession kstateful = ((StatefulKnowledgeSession) context.getBean( "ksession2" ));
        kstateful.setGlobal( "list",
                             list );
        kstateful.insert( new Person( "Darth",
                                      "Cheddar",
                                      50 ) );
        kstateful.fireAllRules();
        assertEquals( 1,
                      list.size() );
    }

    public void testSimpleKSessions() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

        List<String> list = new ArrayList<String>();
        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean( "ksession1" );
        kstateless.setGlobal( "list",
                              list );
        kstateless.execute( new Person( "Darth",
                                        "Cheddar",
                                        50 ) );
        assertEquals( 2,
                      list.size() );

        list = new ArrayList<String>();
        StatefulKnowledgeSession kstateful = ((StatefulKnowledgeSession) context.getBean( "ksession2" ));
        kstateful.setGlobal( "list",
                             list );
        kstateful.insert( new Person( "Darth",
                                      "Cheddar",
                                      50 ) );
        kstateful.fireAllRules();
        assertEquals( 2,
                      list.size() );
    }
    
    public void testAgents() throws Exception {        
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/kagents-beans.xml" );
        
        ResourceChangeScannerImpl scanner = ( ResourceChangeScannerImpl ) ResourceFactory.getResourceChangeScannerService();
        assertEquals( 5, scanner.getInterval() );
        
        KnowledgeBaseImpl kbase1 = ( KnowledgeBaseImpl ) context.getBean( "kbase1" );
        KnowledgeBaseImpl kbase2 = ( KnowledgeBaseImpl ) context.getBean( "kbase2" );
        
        KnowledgeAgentImpl kagent1 = ( KnowledgeAgentImpl ) context.getBean( "kagent1" );
        assertSame( kagent1.getKnowledgeBase(), kbase1 );
        assertEquals( 0, kagent1.getResourceDirectories().size() );
        assertFalse( kagent1.isNewInstance() );
        
        KnowledgeAgentImpl kagent2 = ( KnowledgeAgentImpl ) context.getBean( "kagent2" );
        assertSame( kagent2.getKnowledgeBase(), kbase2 );
        assertEquals( 1, kagent2.getResourceDirectories().size() );
        assertFalse( kagent2.isNewInstance() );
        
        KnowledgeAgentImpl kagent3 = ( KnowledgeAgentImpl ) context.getBean( "kagent3" );
        assertTrue( kagent3.isNewInstance() );
        
        StatelessKnowledgeSessionImpl ksession1 = (StatelessKnowledgeSessionImpl)  context.getBean( "ksession1" );
        assertSame( kbase1.getRuleBase(), ksession1.getRuleBase() );
        assertSame( kagent1, ksession1.getKnowledgeAgent() );
        
        StatefulKnowledgeSessionImpl ksession2 = (StatefulKnowledgeSessionImpl)  context.getBean( "ksession2" );
        assertSame( kbase1.getRuleBase(), ksession2.getRuleBase() );
        
        StatelessKnowledgeSessionImpl ksession3 = (StatelessKnowledgeSessionImpl)  context.getBean( "ksession3" );
        assertSame( kagent2, ksession3.getKnowledgeAgent() );
        assertSame( kbase2.getRuleBase(), ksession3.getRuleBase() );

    }    

    public void testNode() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

        ExecutionNode node = (ExecutionNode) context.getBean( "node1" );
        List<String> list = new ArrayList<String>();
        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) node.get( DirectoryLookupFactoryService.class ).lookup( "stateless1" );
        assertNotNull( "can't obtain session named: stateless1",
                       kstateless );
        kstateless.setGlobal( "list",
                              list );
        kstateless.execute( new Person( "Darth",
                                        "Cheddar",
                                        50 ) );
        assertEquals( 2,
                      list.size() );

        list = new ArrayList<String>();
        StatefulKnowledgeSession kstateful = (StatefulKnowledgeSession) node.get( DirectoryLookupFactoryService.class ).lookup( "ksession2" );
        kstateful.setGlobal( "list",
                             list );
        kstateful.insert( new Person( "Darth",
                                      "Cheddar",
                                      50 ) );
        kstateful.fireAllRules();
        assertEquals( 2,
                      list.size() );
    }

    public void testConfiguration() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/session-conf-beans.xml" );
        KnowledgeBaseImpl kbase1 = ( KnowledgeBaseImpl ) context.getBean( "kbase1" );
        RuleBaseConfiguration rconf = ((InternalRuleBase)kbase1.getRuleBase()).getConfiguration();
        assertTrue( rconf.isAdvancedProcessRuleIntegration() );
        assertTrue( rconf.isMultithreadEvaluation() );
        assertEquals( 5, rconf.getMaxThreads() );
        assertEquals( EventProcessingOption.STREAM, rconf.getEventProcessingMode() );
        assertEquals( AssertBehaviour.IDENTITY, rconf.getAssertBehaviour() );
        assertEquals( "org.drools.container.spring.MockConsequenceExceptionHandler", rconf.getConsequenceExceptionHandler() );

        KnowledgeBaseImpl kbase2 = ( KnowledgeBaseImpl ) context.getBean( "kbase2" );
        rconf = ((InternalRuleBase)kbase2.getRuleBase()).getConfiguration();
        assertFalse( rconf.isAdvancedProcessRuleIntegration() );
        assertFalse( rconf.isMultithreadEvaluation() );
        assertEquals( 3, rconf.getMaxThreads() );
        assertEquals( EventProcessingOption.CLOUD, rconf.getEventProcessingMode() );
        assertEquals( AssertBehaviour.EQUALITY, rconf.getAssertBehaviour() );
        
        
        StatefulKnowledgeSessionImpl ksession1 = ( StatefulKnowledgeSessionImpl ) context.getBean( "ksession1" );
        SessionConfiguration sconf = ksession1.session.getSessionConfiguration();
        assertTrue( sconf.isKeepReference() );
        assertEquals( ClockType.REALTIME_CLOCK , sconf.getClockType() );
        Map<String, WorkItemHandler> wih = sconf.getWorkItemHandlers();
        assertEquals( 4, wih.size() );
        assertTrue( wih.containsKey( "wih1" ));
        assertTrue( wih.containsKey( "wih2" ));
        assertTrue( wih.containsKey( "Human Task" ));
        assertTrue( wih.containsKey( "MyWork" ));        
        assertNotSame(  wih.get( "wih1" ), wih.get( "wih2" ));
        assertEquals( HumanTaskHandler.class, wih.get( "wih1" ).getClass() );
        assertEquals( HumanTaskHandler.class, wih.get( "wih2" ).getClass() );
  
        StatefulKnowledgeSessionImpl ksession2 = ( StatefulKnowledgeSessionImpl ) context.getBean( "ksession2" );
        sconf = ksession2.session.getSessionConfiguration();
        assertFalse( sconf.isKeepReference() );
        assertEquals( ClockType.PSEUDO_CLOCK, sconf.getClockType() );        

        
    }

    public void testResourceAuthenication() throws Exception {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

            //Secure Resource
            DroolsResourceAdapter resourceAdapter = (DroolsResourceAdapter) context.getBean( "secureResource" );
            assertNotNull( resourceAdapter );

            Resource resource = resourceAdapter.getDroolsResource();
            assertTrue(resource instanceof UrlResource);

            UrlResource ur = (UrlResource)resource;

            assertEquals("enabled",ur.getBasicAuthentication());
            assertEquals("someUser",ur.getUsername());
            assertEquals("somePassword",ur.getPassword());

            //Insecure Resource
            resourceAdapter = (DroolsResourceAdapter) context.getBean( "insecureResource" );
            assertNotNull( resourceAdapter );

            resource = resourceAdapter.getDroolsResource();
            assertTrue(resource instanceof UrlResource);

            ur = (UrlResource)resource;

            assertEquals("disabled",ur.getBasicAuthentication());
            assertEquals("",ur.getUsername());
            assertEquals("",ur.getPassword());
    }
}
