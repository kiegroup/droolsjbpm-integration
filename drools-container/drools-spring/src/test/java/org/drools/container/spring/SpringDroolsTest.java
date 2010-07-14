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
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.core.util.FileManager;
import org.drools.grid.ExecutionNode;
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
        URL url = getClass().getResource( "testSpring.drl" );
        File file = new File( url.getFile() );
//        File dir = new File( file, "temp" );
//        dir.mkdirs()
        File dir = new File( file.getParentFile(), "temp/drl" );
        dir.mkdirs();
        
        File drlFile = new File( dir, "testSpring.drl");
        System.out.println( drlFile );
        drlFile.createNewFile();
        
        
        StreamUtils.copy( url.openStream(), new FileOutputStream( drlFile ) );
        System.out.println( url );
         assertTrue( drlFile.exists() );
//        
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
//
//        List<String> list = new ArrayList<String>();
//        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean( "ksession1" );
//        kstateless.setGlobal( "list",
//                              list );
//        kstateless.execute( new Person( "Darth",
//                                        "Cheddar",
//                                        50 ) );
//        assertEquals( 2,
//                      list.size() );
//
//        list = new ArrayList<String>();
//        StatefulKnowledgeSession kstateful = ((StatefulKnowledgeSession) context.getBean( "ksession2" ));
//        kstateful.setGlobal( "list",
//                             list );
//        kstateful.insert( new Person( "Darth",
//                                      "Cheddar",
//                                      50 ) );
//        kstateful.fireAllRules();
//        assertEquals( 2,
//                      list.size() );
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
