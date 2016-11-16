/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.karaf.itest;

import org.h2.tools.Server;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.*;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieSpringjBPMPersistenceKarafIntegrationTest extends AbstractKieSpringKarafIntegrationTest {

    private static final transient Logger logger = LoggerFactory.getLogger(KieSpringjBPMPersistenceKarafIntegrationTest.class);

    @Before
    public void init() {
        applicationContext = createApplicationContext();
        assertNotNull("Should have created a valid spring context", applicationContext);
    }

    @Test
    public void testJbpmRuntimeManager() {
        refresh();
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get().newEmptyBuilder()
                .addAsset(
                        KieServices.Factory.get().getResources().newClassPathResource(
                                "Evaluation.bpmn", getClass().getClassLoader()), ResourceType.BPMN2)
                .get();
        RuntimeManager runtimeManager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
        KieSession ksession = runtimeManager.getRuntimeEngine(EmptyContext.get()).getKieSession();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());

        logger.info("Start process Evaluation (bpmn2)");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "krisv");
        params.put("reason", "Yearly performance evaluation");
        ProcessInstance processInstance =
                ksession.startProcess("com.sample.evaluation", params);
        logger.info("Started process instance " + processInstance.getId());
    }

    @Test
    public void testJbpmRuntimeManagerWithPersistence() {
        Server server = startH2Server();
        refresh();
        Properties props = new Properties();
        props.setProperty("krisv", "IT");
        props.setProperty("john", "HR");
        props.setProperty("mary", "PM");
        EntityManagerFactory emf = (EntityManagerFactory) applicationContext.getBean("myEmf");
        PlatformTransactionManager txManager = (PlatformTransactionManager) applicationContext.getBean("txManager");
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder()
                .entityManagerFactory(emf)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, txManager)
                .addAsset(
                        KieServices.Factory.get().getResources().newClassPathResource(
                                "Evaluation.bpmn", getClass().getClassLoader()), ResourceType.BPMN2)
                .userGroupCallback(new JBossUserGroupCallbackImpl(props))
                .get();
        RuntimeManager runtimeManager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();

        // start a new process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "krisv");
        params.put("reason", "Yearly performance evaluation");
        ProcessInstance processInstance =
                ksession.startProcess("com.sample.evaluation", params);
        System.out.println("Process instance " + processInstance.getId() + " started ...");

        ProcessInstance pi = ksession.getProcessInstance(processInstance.getId());
        System.out.println(pi);

        // complete Self Evaluation
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("krisv", "en-UK");
        TaskSummary task = tasks.get(0);
        System.out.println("'krisv' completing task " + task.getName() + ": " + task.getDescription());
        taskService.start(task.getId(), "krisv");

        Map<String, Object> vars = taskService.getTaskContent(task.getId());

        Map<String, Object> results = new HashMap<String, Object>();
        results.put("performance", "exceeding");
        taskService.complete(task.getId(), "krisv", results);

        // john from HR
        tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        task = tasks.get(0);
        System.out.println("'john' completing task " + task.getName() + ": " + task.getDescription());
        taskService.claim(task.getId(), "john");
        taskService.start(task.getId(), "john");
        results = new HashMap<String, Object>();
        results.put("performance", "acceptable");
        taskService.complete(task.getId(), "john", results);

        // mary from PM
        tasks = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = tasks.get(0);
        System.out.println("'mary' completing task " + task.getName() + ": " + task.getDescription());
        taskService.claim(task.getId(), "mary");
        taskService.start(task.getId(), "mary");
        results = new HashMap<String, Object>();
        results.put("performance", "outstanding");
        taskService.complete(task.getId(), "mary", results);

        System.out.println("Process instance completed");

        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();

        server.shutdown();
    }

    public static Server startH2Server() {
        try {
            // start h2 in memory database
            Server server = Server.createTcpServer(new String[0]);
            server.start();
            return server;
        } catch (Throwable t) {
            throw new RuntimeException("Could not start H2 server", t);
        }
    }

    @Configuration
    public static Option[] configure() {
        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),

                // It is really nice if the container sticks around after the test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
                //  debugConfiguration("5005", true),

                // Load Kie-Spring
                loadKieFeatures("jbpm-spring-persistent")

        };
    }

    protected OsgiBundleXmlApplicationContext createApplicationContext() {
        return new OsgiBundleXmlApplicationContext(new String[]{"org/kie/karaf/itest/kie-beans-persistence.xml"});
    }
}