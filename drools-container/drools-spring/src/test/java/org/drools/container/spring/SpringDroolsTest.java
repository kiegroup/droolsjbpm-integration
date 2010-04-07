package org.drools.container.spring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.Person;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.grid.ExecutionNode;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.server.KnowledgeService;
import org.drools.server.profile.KnowledgeServiceConfiguration;
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

		ExecutionNode node = (ExecutionNode) context.getBean("node1");
		List<String> list = new ArrayList<String>();
		StatelessKnowledgeSession kstateless = (StatelessKnowledgeSession) node.get(DirectoryLookupFactoryService.class).lookup("stateless1");
		assertNotNull("can't obtain session named: stateless1" , kstateless);
		kstateless.setGlobal("list", list);
		kstateless.execute(new Person("Darth", "Cheddar", 50));
		assertEquals(2, list.size());

		list = new ArrayList<String>();
		StatefulKnowledgeSession kstateful = (StatefulKnowledgeSession) node.get(DirectoryLookupFactoryService.class).lookup("ksession2");
		kstateful.setGlobal("list", list);
		kstateful.insert(new Person("Darth", "Cheddar", 50));
		kstateful.fireAllRules();   
		assertEquals(2, list.size());
	}

	public void test3() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/beans.xml" );
		assertNotNull(context.getBean("connection1"));
		assertNotNull(context.getBean("node1"));
		KnowledgeBase bean = (KnowledgeBase) context.getBean("kbase1");
		assertNotNull(bean);
		KnowledgeServiceConfiguration kserviceConf1 = (KnowledgeServiceConfiguration)context.getBean( "service-conf-1" );
		assertNotNull(kserviceConf1);
		assertEquals("XSTREAM", kserviceConf1.getMarshaller());
		KnowledgeService kservice = (KnowledgeService)context.getBean( "service" );
		assertNotNull(kservice);
	}
}
