package org.drools.container.spring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.Person;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.vsm.ServiceManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDroolsTest extends TestCase {
	public void test1() throws Exception {		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
		
		List<String> list = new ArrayList<String>();
		StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean( "ksession1" );
		kstateless.setGlobal( "list", list );
		kstateless.execute( new Person( "Darth", "Cheddar", 50 ) );
		assertEquals( 2, list.size() );
		
		
		list = new ArrayList<String>();
		StatefulKnowledgeSession kstateful = ((StatefulKnowledgeSession)context.getBean( "ksession2" ));
		kstateful.setGlobal( "list", list );
		kstateful.insert( new Person( "Darth", "Cheddar", 50 ) );
		kstateful.fireAllRules();	
		assertEquals( 2, list.size() );
	}
	
	public void test2() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
        
        ServiceManager sm = (ServiceManager)context.getBean( "sm1" );
        
        List<String> list = new ArrayList<String>();
        StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession)sm.lookup( "stateless1" );
        assertNotNull("can't obtain session named: stateless1" , kstateless);
        kstateless.setGlobal( "list", list );
        kstateless.execute( new Person( "Darth", "Cheddar", 50 ) );
        assertEquals( 2, list.size() );
        
        
        list = new ArrayList<String>();
        StatefulKnowledgeSession kstateful = ( StatefulKnowledgeSession ) sm.lookup( "ksession2" );
        kstateful.setGlobal( "list", list );
        kstateful.insert( new Person( "Darth", "Cheddar", 50 ) );
        kstateful.fireAllRules();   
        assertEquals( 2, list.size() );	    
	}
}
