/*
 * Copyright 2013 JBoss Inc
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

package org.kie.spring.tests.persistence;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.base.MapGlobalResolver;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.*;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.spring.InternalKieSpringUtils;
import org.kie.spring.beans.persistence.*;
import org.kie.spring.tests.InternalKieSpringUtilsTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.*;
import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class VariablePersistenceStrategyTest {

    private static final Logger log = LoggerFactory.getLogger(JPASingleSessionCommandServiceFactoryTest.class);
    private static Server h2Server;

    private ApplicationContext ctx;

    @BeforeClass
    public static void startH2Database() throws Exception {
        DeleteDbFiles.execute("",
                "DroolsFlow",
                true);
        h2Server = Server.createTcpServer(new String[0]);
        h2Server.start();
    }

    @AfterClass
    public static void stopH2Database() throws Exception {
        log.info("stoping database");
        h2Server.stop();
        DeleteDbFiles.execute("",
                "DroolsFlow",
                true);
    }

    @Before
    public void createSpringContext() {
        try {
            log.info("creating spring context");
            ReleaseId releaseId = new ReleaseIdImpl("kie-spring-var-jpa","test-spring","0001");
            URL configFileURL =  InternalKieSpringUtilsTest.class.getResource("/org/kie/spring/persistence/persistence_var_beans.xml");
            ctx = InternalKieSpringUtils.getSpringContext(releaseId,configFileURL);
        } catch (Exception e) {
            log.error("can't create spring context",
                    e);
            throw new RuntimeException(e);
        }
    }

    @After
    public void destroySpringContext() {
        log.info("destroy spring context");
    }

    @Test
    public void testTransactionsRollback() throws Exception {
        final List<?> list = new ArrayList<Object>();
        PlatformTransactionManager txManager = (PlatformTransactionManager) ctx.getBean("txManager");

        final Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY,
                ctx.getBean("myEmf"));
        env.set(EnvironmentName.TRANSACTION_MANAGER,
                txManager);
        env.set(EnvironmentName.GLOBALS,
                new MapGlobalResolver());

        env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES,
                new ObjectMarshallingStrategy[]{new JPAPlaceholderResolverStrategy(env),
                        new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT)});

        final KieStoreServices kstore = (KieStoreServices) ctx.getBean("kstore1");
        final KieBase kbRollback = (KieBase) ctx.getBean("drl_persistence_rb");

        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        final KieSession ksession = (KieSession) txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                KieSession kNewSession = kstore.newKieSession(kbRollback,
                        null,
                        env);
                kNewSession.setGlobal("list",
                        list);
                kNewSession.insert(1);
                kNewSession.insert(2);
                return kNewSession;
            }
        });

        final int sessionId = ksession.getId();

        txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                ksession.insert(3);
                status.setRollbackOnly();
                return null;
            }
        });

        txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                ksession.fireAllRules();
                return null;
            }
        });

        assertEquals(2,
                list.size());

        txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                ksession.insert(3);
                ksession.insert(4);
                return null;
            }
        });

        txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                ksession.insert(5);
                ksession.insert(6);
                status.setRollbackOnly();
                return null;
            }
        });

        txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                ksession.fireAllRules();
                return null;
            }
        });

        assertEquals(4,
                list.size());

        ksession.dispose();

        // now load the ksession
        final KieSession ksession2 = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId,
                kbRollback,
                null,
                env);

        txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                ksession2.setGlobal("list",
                        list);
                ksession2.insert(7);
                ksession2.insert(8);
                return null;
            }
        });

        txTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                ksession2.fireAllRules();
                return null;
            }
        });

        assertEquals(6,
                list.size());
    }

    @Test
    public void testPersistenceVariables() throws NamingException,
            NotSupportedException,
            SystemException,
            IllegalStateException,
            RollbackException,
            HeuristicMixedException,
            HeuristicRollbackException {
        MyEntity myEntity = new MyEntity("This is a test Entity with annotation in fields");
        MyEntityMethods myEntityMethods = new MyEntityMethods("This is a test Entity with annotations in methods");
        MyEntityOnlyFields myEntityOnlyFields = new MyEntityOnlyFields("This is a test Entity with annotations in fields and without accesors methods");
        MyVariableSerializable myVariableSerializable = new MyVariableSerializable("This is a test SerializableObject");
        EntityManager em = ((EntityManagerFactory) ctx.getBean("myEmf")).createEntityManager();

        em.getTransaction().begin();
        em.persist(myEntity);
        em.persist(myEntityMethods);
        em.persist(myEntityOnlyFields);
        em.getTransaction().commit();
        em.close();

        log.info("---> get bean jpaSingleSessionCommandService");
        KieSession service = (KieSession) ctx.getBean("jpaSingleSessionCommandService2");

        int sessionId = service.getId();
        log.info("---> created SingleSessionCommandService id: " + sessionId);

        log.info("### Starting process ###");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("x",
                "SomeString");
        parameters.put("y",
                myEntity);
        parameters.put("m",
                myEntityMethods);
        parameters.put("f",
                myEntityOnlyFields);
        parameters.put("z",
                myVariableSerializable);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) service.startProcess("com.sample.ruleflow",
                parameters);
        log.info("Started process instance {}",
                processInstance.getId());

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull(workItem);
        service.dispose();

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("myEmf");

        //        List< ? > result = emf.createEntityManager().createQuery( "select i from VariableInstanceInfo i" ).getResultList();
        //        assertEquals( 5,
        //                      result.size() );
        log.info("### Retrieving process instance ###");

        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY,
                emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER,
                ctx.getBean("txManager"));
        env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES,
                new ObjectMarshallingStrategy[]{
                        //  new JPAPlaceholderResolverStrategy(env),
                        new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT)
                });

        KieStoreServices kstore = (KieStoreServices) ctx.getBean("kstore1");
        KieBase kbase1 = (KieBase) ctx.getBean("drl_persistence");
        service = kstore.loadKieSession(sessionId,
                kbase1,
                null,
                env);

        processInstance = (WorkflowProcessInstance) service.getProcessInstance(processInstance.getId());
        assertNotNull(processInstance);

        assertNotNull(processInstance);
        assertEquals("SomeString",
                processInstance.getVariable("x"));
        assertEquals("This is a test Entity with annotation in fields",
                ((MyEntity) processInstance.getVariable("y")).getTest());
        assertEquals("This is a test Entity with annotations in methods",
                ((MyEntityMethods) processInstance.getVariable("m")).getTest());
        assertEquals("This is a test Entity with annotations in fields and without accesors methods",
                ((MyEntityOnlyFields) processInstance.getVariable("f")).test);
        assertEquals("This is a test SerializableObject",
                ((MyVariableSerializable) processInstance.getVariable("z")).getText());
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
        //            assertTrue(VariableInstanceInfo.class.isAssignableFrom(o.getClass()));
        //            log.info(o);
        //        }
        //        
        //        log.info("### Retrieving process instance ###");
        //        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(id, kbase, null, env);
        //        processInstance = (WorkflowProcessInstance)
        //            ksession.getProcessInstance(processInstance.getId());
        //        assertNotNull(processInstance);
        //        assertEquals("SomeString", processInstance.getVariable("x"));
        //        assertEquals("This is a test Entity with annotation in fields", ((MyEntity) processInstance.getVariable("y")).getTest());
        //        assertEquals("This is a test Entity with annotations in methods", ((MyEntityMethods) processInstance.getVariable("m")).getTest());
        //        assertEquals("This is a test Entity with annotations in fields and without accesors methods", ((MyEntityOnlyFields) processInstance.getVariable("f")).test);
        //        assertEquals("This is a test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("z")).getText());
        //        assertEquals("Some new String", processInstance.getVariable("a"));
        //        assertEquals("This is a new test Entity", ((MyEntity) processInstance.getVariable("b")).getTest());
        //        assertEquals("This is a new test SerializableObject", ((MyVariableSerializable) processInstance.getVariable("c")).getText());
        //        log.info("### Completing second work item ###");
        //        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
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
        //            ksession.getProcessInstance(processInstance.getId());
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
        //            ksession.getProcessInstance(processInstance.getId());
        //        assertNull(processInstance);
    }

    //    public void testPersistenceVariablesWithTypeChange() {
    //        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    //        kbuilder.add( new ClassPathResource( "VariablePersistenceStrategyProcessTypeChange.rf" ), ResourceType.DRF );
    //        for (KnowledgeBuilderError error: kbuilder.getErrors()) {
    //            log.info(error);
    //        }
    //        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
    //        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
    //
    //        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.kie.api.persistence.jpa" );
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
    //            log.info("{}", error);
    //        }
    //        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
    //        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
    //
    //        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.kie.api.persistence.jpa" );
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
