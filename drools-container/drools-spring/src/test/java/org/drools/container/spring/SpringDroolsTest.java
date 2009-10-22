package org.drools.container.spring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.Person;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDroolsTest extends TestCase {
	public void test1() throws Exception {		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
		
		List list = new ArrayList();
		StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) context.getBean( "ksession1" );
		kstateless.setGlobal( "list", list );
		kstateless.execute( new Person( "Darth", "Cheddar", 50 ) );
		assertEquals( 2, list.size() );
		
		
		list = new ArrayList();
		StatefulKnowledgeSession kstateful = ( StatefulKnowledgeSession ) ((StatefulKnowledgeSession)context.getBean( "ksession2" ));
		kstateful.setGlobal( "list", list );
		kstateful.insert( new Person( "Darth", "Cheddar", 50 ) );
		kstateful.fireAllRules();	
		assertEquals( 2, list.size() );
	}
}
