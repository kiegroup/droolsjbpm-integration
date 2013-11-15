/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.drools.container.spring.beans.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import junit.framework.Assert;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.command.runtime.process.RegisterWorkItemHandlerCommand;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * bz839630 reproducer. Exception "Entity manager is closed" is thrown when sessions are being disposed.
 * @author rsynek
 */
public class SessionDisposeTest {

    private static List<StatefulKnowledgeSession> sessions = new ArrayList<StatefulKnowledgeSession>();
    private static Server h2Server;
    private static final Logger log = LoggerFactory.getLogger(SessionDisposeTest.class);
    private static ClassPathXmlApplicationContext ctx;
    private static KnowledgeBase kbase;
    
    @Test
    public void testHumanTask() {
        StatefulKnowledgeSession ksession = createSession();

        String processId = "eclipse.human-task";

        TestWorkItemHandler wih = TestWorkItemHandler.getInstance();
        ksession.execute(new RegisterWorkItemHandlerCommand("Human Task", wih));
        ProcessInstance pi = (ProcessInstance) ksession.execute((Command<?>) CommandFactory.newStartProcess(processId));

        Assert.assertEquals(pi.getState(), ProcessInstance.STATE_ACTIVE);
        WorkItem wi = wih.getWorkItem();

        Assert.assertEquals(wi.getParameter("ActorId"), "worker1");
        ksession.execute(new CompleteWorkItemCommand(wi.getId()));

        Assert.assertNull("process instance has not completed yet", ksession.getProcessInstance(pi.getId()));
    }

    @Test
    public void testRuleTask() {
        StatefulKnowledgeSession ksession = createSession();

        List<String> executedRules = new ArrayList<String>();
        List<Command<?>> commands = new ArrayList<Command<?>>();
        String processId = "eclipse.rule-task";

        commands.add(CommandFactory.newSetGlobal("executed", executedRules));
        commands.add(CommandFactory.newStartProcess(processId));
        commands.add(CommandFactory.newFireAllRules());

        ksession.execute(CommandFactory.newBatchExecution(commands));

        Assert.assertEquals(3, executedRules.size());
        String[] expected = new String[]{"firstRule", "secondRule", "thirdRule"};

        for (String expectedRuleName : expected) {
            Assert.assertTrue(executedRules.contains(expectedRuleName));
        }
    }
 
    protected StatefulKnowledgeSession createSession() {
        StatefulKnowledgeSession session = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, getEnvironment());
        sessions.add(session);
        return session;
    }

    @AfterClass
    public static void dispose() {
        Iterator<StatefulKnowledgeSession> it = sessions.iterator();
        while (it.hasNext()) {
            it.next().dispose();          
            it.remove();
        }
    }

    @BeforeClass
    public static void configure() {
        ctx = new ClassPathXmlApplicationContext("org/drools/container/spring/beans/persistence/beansSessionDispose.xml");

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("human-task.bpmn", SessionDisposeTest.class), ResourceType.BPMN2);
        kbuilder.add(ResourceFactory.newClassPathResource("rule-task.bpmn", SessionDisposeTest.class), ResourceType.BPMN2);
        kbuilder.add(ResourceFactory.newClassPathResource("rule-task.drl", SessionDisposeTest.class), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        kbase = kbuilder.newKnowledgeBase();
    }

    private Environment getEnvironment() {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, ctx.getBean("myEmf"));
        env.set(EnvironmentName.TRANSACTION_MANAGER, ctx.getBean("txManager"));
        return env;
    }

    @BeforeClass
    public static void startH2Database() throws Exception {
        h2Server = Server.createTcpServer(new String[0]);
        h2Server.start();
    }

    @AfterClass
    public static void stopH2Database() throws Exception {
        log.info("stopping database");
        h2Server.stop();
        DeleteDbFiles.execute("",
                "DroolsFlow",
                true);
    }
}
