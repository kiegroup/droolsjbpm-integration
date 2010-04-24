package org.drools.container.spring.beans.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.base.MapGlobalResolver;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.SingleSessionCommandService;
import org.drools.command.runtime.process.GetProcessInstanceCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.container.spring.beans.JPASingleSessionCommandService;
import org.drools.io.impl.ClassPathResource;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.persistence.processinstance.variabletypes.VariableInstanceInfo;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class VariablePersistenceStrategyTest {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	private static final Logger log = LoggerFactory.getLogger(JPASingleSessionCommandServiceFactoryTest.class);
	private static Server h2Server;
    
    private ClassPathXmlApplicationContext ctx;
    
    @BeforeClass
    public static void startH2Database() throws Exception {
    	DeleteDbFiles.execute("", "DroolsFlow", true);
    	h2Server = Server.createTcpServer(new String[0]);
    	h2Server.start();
    }
    
    @AfterClass
    public static void stopH2Database() throws Exception {
    	log.info("stoping database");
    	h2Server.stop();
    	DeleteDbFiles.execute("", "DroolsFlow", true);
    }

    @Before
    public void createSpringContext() {
    	try {
			log.info("creating spring context");
			PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
			Properties properties = new Properties();
			properties.setProperty("temp.dir", TMPDIR);
			configurer.setProperties(properties);
			ctx = new ClassPathXmlApplicationContext();
			ctx.addBeanFactoryPostProcessor(configurer);
			ctx.setConfigLocation("org/drools/container/spring/beans/persistence/beansVarPersistence.xml");
			ctx.refresh();
		} catch (Exception e) {
			log.error("can't create spring context", e);
			throw new RuntimeException(e);
		}
    }
    
    @After
    public void destrySpringContext() {
    	log.info("destroy spring context");
    	ctx.destroy();
    }
    
    @Test
    public void testPersistenceVariables() {
    	log.info("---> get bean jpaSingleSessionCommandService");
        JPASingleSessionCommandService jpaService = (JPASingleSessionCommandService) ctx.getBean("jpaSingleSessionCommandService");
        
        log.info("---> create new SingleSessionCommandService");
        SingleSessionCommandService service = jpaService.createNew();
        
        int sessionId = service.getSessionId();
        log.info("---> created SingleSessionCommandService id: " + sessionId);

        StartProcessCommand startProcessCommand = new StartProcessCommand("com.sample.ruleflow");
        log.info("### Starting process ###");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("x", "SomeString");
        parameters.put("y", new MyEntity("This is a test Entity with annotation in fields"));
        parameters.put("m", new MyEntityMethods("This is a test Entity with annotations in methods"));
        parameters.put("f", new MyEntityOnlyFields("This is a test Entity with annotations in fields and without accesors methods"));
        parameters.put("z", new MyVariableSerializable("This is a test SerializableObject"));
        startProcessCommand.setParameters(parameters);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) service.execute( startProcessCommand );
        log.info( "Started process instance {}", processInstance.getId() );
    
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();
        
        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("myEmf");
        
        List<?> result = emf.createEntityManager().createQuery("select i from VariableInstanceInfo i").getResultList();
        assertEquals(5, result.size());
        log.info("### Retrieving process instance ###");
        
        service = jpaService.load(sessionId);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = (WorkflowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        
        assertNotNull( processInstance );
        assertEquals("SomeString", processInstance.getVariable("x"));
        assertEquals("This is a test Entity with annotation in fields", ((MyEntity) processInstance.getVariable("y")).getTest());
        assertEquals("This is a test Entity with annotations in methods", ((MyEntityMethods) processInstance.getVariable("m")).getTest());
        assertEquals("This is a test Entity with annotations in fields and without accesors methods", ((MyEntityOnlyFields) processInstance.getVariable("f")).test);
        assertEquals("This is a test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("z")).getText());
        assertNull(processInstance.getVariable("a"));
        assertNull(processInstance.getVariable("b"));
        assertNull(processInstance.getVariable("c"));
   
        service.dispose();
        
//        log.info("### Completing first work item ###");
//        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
//
//        workItem = handler.getWorkItem();
//        assertNotNull( workItem );
//        
//        log.info("### Retrieving variable instance infos ###");
//        result = emf.createEntityManager().createQuery("select i from VariableInstanceInfo i").getResultList();
//        assertEquals(8, result.size());
//        for (Object o: result) {
//        	assertTrue(VariableInstanceInfo.class.isAssignableFrom(o.getClass()));
//        	log.info(o);
//        }
//        
//        log.info("### Retrieving process instance ###");
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(id, kbase, null, env);
//		processInstance = (WorkflowProcessInstance)
//			ksession.getProcessInstance(processInstance.getId());
//		assertNotNull(processInstance);
//        assertEquals("SomeString", processInstance.getVariable("x"));
//        assertEquals("This is a test Entity with annotation in fields", ((MyEntity) processInstance.getVariable("y")).getTest());
//        assertEquals("This is a test Entity with annotations in methods", ((MyEntityMethods) processInstance.getVariable("m")).getTest());
//        assertEquals("This is a test Entity with annotations in fields and without accesors methods", ((MyEntityOnlyFields) processInstance.getVariable("f")).test);
//        assertEquals("This is a test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("z")).getText());
//        assertEquals("Some new String", processInstance.getVariable("a"));
//        assertEquals("This is a new test Entity", ((MyEntity) processInstance.getVariable("b")).getTest());
//        assertEquals("This is a new test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("c")).getText());
//        log.info("### Completing second work item ###");
//		ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
//
//        workItem = handler.getWorkItem();
//        assertNotNull(workItem);
//        
//        result = emf.createEntityManager().createQuery("select i from VariableInstanceInfo i").getResultList();
//        assertEquals(8, result.size());
//        
//        log.info("### Retrieving process instance ###");
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(id, kbase, null, env);
//        processInstance = (WorkflowProcessInstance)
//        	ksession.getProcessInstance(processInstance.getId());
//        assertNotNull(processInstance);
//        assertEquals("SomeString", processInstance.getVariable("x"));
//        assertEquals("This is a test Entity with annotation in fields", ((MyEntity) processInstance.getVariable("y")).getTest());
//        assertEquals("This is a test Entity with annotations in methods", ((MyEntityMethods) processInstance.getVariable("m")).getTest());
//        assertEquals("This is a test Entity with annotations in fields and without accesors methods", ((MyEntityOnlyFields) processInstance.getVariable("f")).test);
//        assertEquals("This is a test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("z")).getText());
//        assertEquals("Some changed String", processInstance.getVariable("a"));
//        assertEquals("This is a changed test Entity", ((MyEntity) processInstance.getVariable("b")).getTest());
//        assertEquals("This is a changed test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("c")).getText());
//        log.info("### Completing third work item ###");
//        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
//
//        workItem = handler.getWorkItem();
//        assertNull(workItem);
//        
//        result = emf.createEntityManager().createQuery("select i from VariableInstanceInfo i").getResultList();
//        //This was 6.. but I change it to 0 because all the variables will go away with the process instance..
//        //we need to change that to leave the variables there??? 
//        assertEquals(0, result.size());
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(id, kbase, null, env);
//        processInstance = (WorkflowProcessInstance)
//			ksession.getProcessInstance(processInstance.getId());
//        assertNull(processInstance);
    }
    
//    public void testPersistenceVariablesWithTypeChange() {
//        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//        kbuilder.add( new ClassPathResource( "VariablePersistenceStrategyProcessTypeChange.rf" ), ResourceType.DRF );
//        for (KnowledgeBuilderError error: kbuilder.getErrors()) {
//        	log.info(error);
//        }
//        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
//
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.persistence.jpa" );
//        Environment env = KnowledgeBaseFactory.newEnvironment();
//        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY, emf );
//
//        env.set( EnvironmentName.GLOBALS, new MapGlobalResolver() );
//
//        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
//        int id = ksession.getId();
//
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("x", "SomeString");
//        parameters.put("y", new MyEntity("This is a test Entity with annotation in fields"));
//        parameters.put("m", new MyEntityMethods("This is a test Entity with annotations in methods"));
//        parameters.put("f", new MyEntityOnlyFields("This is a test Entity with annotations in fields and without accesors methods"));
//        parameters.put("z", new MyVariableSerializable("This is a test SerializableObject"));
//        ProcessInstance processInstance = ksession.startProcess( "com.sample.ruleflow", parameters );
//
//        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
//        WorkItem workItem = handler.getWorkItem();
//        assertNotNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNotNull( processInstance );
//        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
//
//        workItem = handler.getWorkItem();
//        assertNotNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNotNull( processInstance );
//        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
//
//        workItem = handler.getWorkItem();
//        assertNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNull( processInstance );
//    }
//    
//    public void testPersistenceVariablesSubProcess() {
//        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//        kbuilder.add( new ClassPathResource( "VariablePersistenceStrategySubProcess.rf" ), ResourceType.DRF );
//        for (KnowledgeBuilderError error: kbuilder.getErrors()) {
//        	log.info("{}", error);
//        }
//        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
//
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.persistence.jpa" );
//        Environment env = KnowledgeBaseFactory.newEnvironment();
//        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY, emf );
//
//        env.set( EnvironmentName.GLOBALS, new MapGlobalResolver() );
//
//        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
//        int id = ksession.getId();
//
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("x", "SomeString");
//        parameters.put("y", new MyEntity("This is a test Entity with annotation in fields"));
//        parameters.put("m", new MyEntityMethods("This is a test Entity with annotations in methods"));
//        parameters.put("f", new MyEntityOnlyFields("This is a test Entity with annotations in fields and without accesors methods"));
//        parameters.put("z", new MyVariableSerializable("This is a test SerializableObject"));
//        ProcessInstance processInstance = ksession.startProcess( "com.sample.ruleflow", parameters );
//
//        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
//        WorkItem workItem = handler.getWorkItem();
//        assertNotNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNotNull( processInstance );
//        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
//
//        workItem = handler.getWorkItem();
//        assertNotNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNotNull( processInstance );
//        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
//
//        workItem = handler.getWorkItem();
//        assertNotNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNotNull( processInstance );
//        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
//
//        workItem = handler.getWorkItem();
//        assertNull( workItem );
//
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( id, kbase, null, env );
//        processInstance = ksession.getProcessInstance( processInstance.getId() );
//        assertNull( processInstance );
//    }
//    
}