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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

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
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.karaf.itest.model.Person;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;
import org.osgi.framework.Constants;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieSpringjBPMPersistenceKarafIntegrationTest extends AbstractKieSpringKarafIntegrationTest {

    private static final transient Logger logger = LoggerFactory.getLogger(KieSpringjBPMPersistenceKarafIntegrationTest.class);

    private static final String SPRING_XML_LOCATION = "/org/kie/karaf/itest/kie-beans-persistence.xml";
    private static final String SPRING_APPLICATION_CONTEXT_ID = "." + ApplicationContext.class.getName();
    private static final String DRL_LOCATION = "/drl_kiesample/Hal1.drl";

    // this is the way to get actual Spring Application Context through "bridging" Blueprint Container
    @Inject
    @Filter(value = "(osgi.blueprint.container.symbolicname=Test-Kie-Spring-Bundle)", timeout = 120000)
    private BlueprintContainer container;

    @Before
    public void init() {
        applicationContext = (ConfigurableApplicationContext) container.getComponentInstance(SPRING_APPLICATION_CONTEXT_ID);
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
    public void testJbpmRuntimeManagerWithPersistence() throws Exception {
        refresh();
        Properties props = new Properties();
        props.setProperty("krisv", "IT");
        props.setProperty("john", "HR");
        props.setProperty("mary", "PM");
        EntityManagerFactory emf = (EntityManagerFactory) applicationContext.getBean("myEmf");
        PlatformTransactionManager txManager = (PlatformTransactionManager) applicationContext.getBean("txManager");
        ContextClassLoaderUtils.doWithClassLoader(this.getClass().getClassLoader(), new Callable<Object>() {
            @Override
            public Object call() {
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

                return null;
            }
        });
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
        Server server = startH2Server();
        final String jdbcDriverPath = System.getProperty("jdbc.driver.path");
        final List<Option> configurationOptions = getDefaultOptions();
        if (jdbcDriverPath != null && !"".equals(jdbcDriverPath)) {
            try {
                configurationOptions.add(wrappedBundle(new File(jdbcDriverPath).toURI().toURL().toString()));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error parsing jdbc driver path", e);
            }
        }
        return configurationOptions.toArray(new Option[]{});
    }

    private static List<Option> getDefaultOptions() {
        final List<Option> options = new ArrayList<>();
        // Install Karaf Container
        options.add(getKarafDistributionOption());

        // Don't bother with local console output as it just ends up cluttering the logs
        options.add(configureConsole().ignoreLocalConsole());
        // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
        options.add(logLevel(LogLevelOption.LogLevel.WARN));

        // Option to be used to do remote debugging
        //  options.add(debugConfiguration("5005", true));

        // Load Kie-Spring
        options.add(loadKieFeatures("jbpm-spring-persistent"));
        options.add(features(getFeaturesUrl("org.apache.karaf.features", "spring-legacy", getKarafVersion()), "aries-blueprint-spring"));

        // Create a bundle with META-INF/spring/kie-beans.xml - this should be processed automatically by Spring
        options.add(streamBundle(bundle()
                                         .set(Constants.BUNDLE_MANIFESTVERSION, "2")
                                         .add(Person.class)
                                         .add("META-INF/spring/kie-beans-persistence.xml",
                                              SimpleKieSpringKarafIntegrationTest.class.getResource(SPRING_XML_LOCATION))
                                         .add("META-INF/persistence.xml",
                                              SimpleKieSpringKarafIntegrationTest.class.getResource("/META-INF/persistence.xml"))
                                         .add("drl_kiesample/Hal1.drl",
                                              KieSpringDependencyKarafIntegrationTest.class.getResource(DRL_LOCATION))
                                         .add("META-INF/JBPMorm.xml",
                                              SimpleKieSpringKarafIntegrationTest.class.getResource("/META-INF/JBPMorm.xml"))
                                         .add("META-INF/TaskAuditorm.xml",
                                              SimpleKieSpringKarafIntegrationTest.class.getResource("/META-INF/TaskAuditorm.xml"))
                                         .add("META-INF/Taskorm.xml",
                                              SimpleKieSpringKarafIntegrationTest.class.getResource("/META-INF/Taskorm.xml"))
//                        .set(Constants.IMPORT_PACKAGE, "org.kie.osgi.spring," +
//                                "org.kie.api," +
//                                "org.kie.api.runtime," +
//                                "org.springframework.jdbc.datasource," +
//                                "*")
                                         .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                                         .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Kie-Spring-Bundle")
                                         .build()).start());

        return options;
    }
}
