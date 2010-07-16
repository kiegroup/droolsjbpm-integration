package org.drools.container.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.lf5.util.StreamUtils;
import org.drools.KnowledgeBase;
import org.drools.Person;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.impl.KnowledgeAgentImpl;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.core.util.FileManager;
import org.drools.grid.ExecutionNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.ResourceChangeScanner;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ResourceChangeScannerImpl;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.server.KnowledgeService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDroolsTest extends TestCase {
   
    
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
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

//    public void FIXME_testBeansConstructions() {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
//        assertNotNull( context.getBean( "connection1" ) );
//        assertNotNull( context.getBean( "node1" ) );
//        KnowledgeBase bean = (KnowledgeBase) context.getBean( "kbase1" );
//        assertNotNull( bean );
//        KnowledgeServiceConfiguration kserviceConf1 = (KnowledgeServiceConfiguration) context.getBean( "service-conf-1" );
//        assertNotNull( kserviceConf1 );
//        assertEquals( "XSTREAM",
//                      kserviceConf1.getMarshaller() );
//        KnowledgeService kservice = (KnowledgeService) context.getBean( "service" );
//        assertNotNull( kservice );
//    }
}
