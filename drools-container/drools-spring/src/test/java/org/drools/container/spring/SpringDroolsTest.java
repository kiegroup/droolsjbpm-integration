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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.ClockType;
import org.drools.Person;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseConfiguration.AssertBehaviour;
import org.drools.SessionConfiguration;
import org.drools.common.InternalRuleBase;
import org.kie.KieBase;
import org.kie.conf.EventProcessingOption;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.kie.definition.KiePackage;
import org.kie.io.Resource;
import org.drools.grid.GridNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.impl.UrlResource;
import org.drools.io.internal.InternalResource;
import org.kie.runtime.KieSession;
import org.kie.runtime.StatelessKieSession;
import org.kie.runtime.process.WorkItemHandler;
import org.jbpm.process.instance.impl.humantask.HumanTaskHandler;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDroolsTest {

    @Test
    public void testNoConnection() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

        GridNode node1 = (GridNode) context.getBean( "node1" );
        assertNotNull( node1 );

        GridNode node2 = (GridNode) context.getBean( "node2" );
        assertNotNull( node2 );
    }

    @Test
    public void testEncoding() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/resourceWithEncoding.xml" );

        KieBase kbase = (KieBase) context.getBean("kbase");
        assertNotNull( kbase );
        for (KiePackage pkg : kbase.getKiePackages()) {
            assertEquals("sample acçéntèd rule", pkg.getRules().iterator().next().getName());
        }

    }

    @Test
    public void testNoNodeKSessions() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/no-node-beans.xml" );

        List<String> list = new ArrayList<String>();
        StatelessKieSession kstateless = (StatelessKieSession) context.getBean( "ksession1" );
        kstateless.setGlobal( "list",
                              list );
        kstateless.execute( new Person( "Darth",
                                        "Cheddar",
                                        50 ) );
        assertEquals( 1,
                      list.size() );

        list = new ArrayList<String>();
        KieSession kstateful = ((KieSession) context.getBean( "ksession2" ));
        kstateful.setGlobal( "list",
                             list );
        kstateful.insert( new Person( "Darth",
                                      "Cheddar",
                                      50 ) );
        kstateful.fireAllRules();
        assertEquals( 1,
                      list.size() );
    }

    @Test
    public void testSimpleKSessions() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

        List<String> list = new ArrayList<String>();
        StatelessKieSession kstateless = (StatelessKieSession) context.getBean( "ksession1" );
        kstateless.setGlobal( "list",
                              list );
        kstateless.execute( new Person( "Darth",
                                        "Cheddar",
                                        50 ) );
        assertEquals( 2,
                      list.size() );

        list = new ArrayList<String>();
        KieSession kstateful = ((KieSession) context.getBean( "ksession2" ));
        kstateful.setGlobal( "list",
                             list );
        kstateful.insert( new Person( "Darth",
                                      "Cheddar",
                                      50 ) );
        kstateful.fireAllRules();
        assertEquals( 2,
                      list.size() );
    }

//    @Test
//    public void testAgents() throws Exception {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/kagents-beans.xml" );
//
//        ResourceChangeScannerImpl scanner = (ResourceChangeScannerImpl) ResourceFactory.getResourceChangeScannerService();
//        assertEquals( 5,
//                      scanner.getInterval() );
//
//        KnowledgeBaseImpl kbase1 = (KnowledgeBaseImpl) context.getBean( "kbase1" );
//        KnowledgeBaseImpl kbase2 = (KnowledgeBaseImpl) context.getBean( "kbase2" );
//
//        KnowledgeAgentImpl kagent1 = (KnowledgeAgentImpl) context.getBean( "kagent1" );
//        assertSame( kagent1.getKnowledgeBase(),
//                    kbase1 );
//        assertEquals( 0,
//                      kagent1.getResourceDirectories().size() );
//        assertFalse( kagent1.isNewInstance() );
//        assertFalse( kagent1.isUseKBaseClassLoaderForCompiling() );
//
//        KnowledgeAgentImpl kagent2 = (KnowledgeAgentImpl) context.getBean( "kagent2" );
//        assertSame( kagent2.getKnowledgeBase(),
//                    kbase2 );
//        assertEquals( 1,
//                      kagent2.getResourceDirectories().size() );
//        assertFalse( kagent2.isNewInstance() );
//        assertFalse( kagent2.isUseKBaseClassLoaderForCompiling() );
//
//        KnowledgeAgentImpl kagent3 = (KnowledgeAgentImpl) context.getBean( "kagent3" );
//        assertTrue( kagent3.isNewInstance() );
//        assertTrue( kagent3.isUseKBaseClassLoaderForCompiling() );
//
//        StatelessKnowledgeSessionImpl ksession1 = (StatelessKnowledgeSessionImpl) context.getBean( "ksession1" );
//        assertSame( kbase1.getRuleBase(),
//                    ksession1.getRuleBase() );
//        assertSame( kagent1,
//                    ksession1.getKnowledgeAgent() );
//
//        StatefulKnowledgeSessionImpl ksession2 = (StatefulKnowledgeSessionImpl) context.getBean( "ksession2" );
//        assertSame( kbase1.getRuleBase(),
//                    ksession2.getRuleBase() );
//
//        StatelessKnowledgeSessionImpl ksession3 = (StatelessKnowledgeSessionImpl) context.getBean( "ksession3" );
//        assertSame( kagent2,
//                    ksession3.getKnowledgeAgent() );
//        assertSame( kbase2.getRuleBase(),
//                    ksession3.getRuleBase() );
//
//    }

    @Test
    public void testNode() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

        GridNode node = (GridNode) context.getBean( "node1" );
        List<String> list = new ArrayList<String>();
        StatelessKieSession kstateless = node.get( "stateless1",
                                                   StatelessKieSession.class );
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
        KieSession kstateful = node.get( "ksession2",
                                         KieSession.class );
        kstateful.setGlobal( "list",
                             list );
        kstateful.insert( new Person( "Darth",
                                      "Cheddar",
                                      50 ) );
        kstateful.fireAllRules();
        assertEquals( 2,
                      list.size() );
    }

    @Test
    public void testConfiguration() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/session-conf-beans.xml" );
        KnowledgeBaseImpl kbase1 = (KnowledgeBaseImpl) context.getBean( "kbase1" );
        RuleBaseConfiguration rconf = ((InternalRuleBase) kbase1.getRuleBase()).getConfiguration();
        assertTrue( rconf.isAdvancedProcessRuleIntegration() );
        assertFalse( rconf.isMultithreadEvaluation() );
        assertEquals( EventProcessingOption.STREAM,
                      rconf.getEventProcessingMode() );
        assertEquals( AssertBehaviour.IDENTITY,
                      rconf.getAssertBehaviour() );
        assertEquals( "org.drools.container.spring.MockConsequenceExceptionHandler",
                      rconf.getConsequenceExceptionHandler() );

        KnowledgeBaseImpl kbase2 = (KnowledgeBaseImpl) context.getBean( "kbase2" );
        rconf = ((InternalRuleBase) kbase2.getRuleBase()).getConfiguration();
        assertFalse( rconf.isAdvancedProcessRuleIntegration() );
        assertFalse( rconf.isMultithreadEvaluation() );
        assertEquals( 3,
                      rconf.getMaxThreads() );
        assertEquals( EventProcessingOption.CLOUD,
                      rconf.getEventProcessingMode() );
        assertEquals( AssertBehaviour.EQUALITY,
                      rconf.getAssertBehaviour() );

        StatefulKnowledgeSessionImpl ksession1 = (StatefulKnowledgeSessionImpl) context.getBean( "ksession1" );
        SessionConfiguration sconf = ksession1.session.getSessionConfiguration();
        assertTrue( sconf.isKeepReference() );
        assertEquals( ClockType.REALTIME_CLOCK,
                      sconf.getClockType() );
        Map<String, WorkItemHandler> wih = sconf.getWorkItemHandlers();
        assertEquals( 4,
                      wih.size() );
        assertTrue( wih.containsKey( "wih1" ) );
        assertTrue( wih.containsKey( "wih2" ) );
        assertTrue( wih.containsKey( "Human Task" ) );
        assertTrue( wih.containsKey( "MyWork" ) );
        assertNotSame( wih.get( "wih1" ),
                       wih.get( "wih2" ) );
        assertEquals( HumanTaskHandler.class,
                      wih.get( "wih1" ).getClass() );
        assertEquals( HumanTaskHandler.class,
                      wih.get( "wih2" ).getClass() );

        StatefulKnowledgeSessionImpl ksession2 = (StatefulKnowledgeSessionImpl) context.getBean( "ksession2" );
        sconf = ksession2.session.getSessionConfiguration();
        assertFalse( sconf.isKeepReference() );
        assertEquals( ClockType.PSEUDO_CLOCK,
                      sconf.getClockType() );

    }

    @Test
    public void testResourceAuthenication() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );

        //Secure Resource
        DroolsResourceAdapter resourceAdapter = (DroolsResourceAdapter) context.getBean( "secureResource" );
        assertNotNull( resourceAdapter );

        Resource resource = resourceAdapter.getDroolsResource();
        assertTrue( resource instanceof UrlResource );

        UrlResource ur = (UrlResource) resource;

        assertEquals( "enabled",
                      ur.getBasicAuthentication() );
        assertEquals( "someUser",
                      ur.getUsername() );
        assertEquals( "somePassword",
                      ur.getPassword() );

        //Insecure Resource
        resourceAdapter = (DroolsResourceAdapter) context.getBean( "insecureResource" );
        assertNotNull( resourceAdapter );

        resource = resourceAdapter.getDroolsResource();
        assertTrue( resource instanceof UrlResource );

        ur = (UrlResource) resource;

        assertEquals( "disabled",
                      ur.getBasicAuthentication() );
        assertEquals( "",
                      ur.getUsername() );
        assertEquals( "",
                      ur.getPassword() );
    }
    
    @Test
    public void testResourceNameAndDescription() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
        
        DroolsResourceAdapter resource = (DroolsResourceAdapter)context.getBean("secureResource");
        assertNotNull(resource);
        InternalResource secureResource = (InternalResource) resource.getDroolsResource();
        
        assertEquals("/someDRLResource.drl", secureResource.getSourcePath());
        assertNull(secureResource.getDescription());
        
        resource = (DroolsResourceAdapter)context.getBean("resourceWithNameAndDescription");
        assertNotNull( resource );
        InternalResource resourceWithNameAndDescription = (InternalResource) resource.getDroolsResource();
        
        assertEquals("A Name", resourceWithNameAndDescription.getSourcePath());
        assertEquals("A Description", resourceWithNameAndDescription.getDescription());
        
    }
}
