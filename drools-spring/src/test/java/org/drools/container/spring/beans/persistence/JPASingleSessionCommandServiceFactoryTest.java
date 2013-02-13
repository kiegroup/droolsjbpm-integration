/*
 * Copyright 2010 JBoss Inc
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

import org.kie.KieBase;
import org.kie.KnowledgeBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.rule.Package;
import org.kie.persistence.jpa.KieStoreServices;
import org.kie.runtime.Environment;
import org.kie.runtime.EnvironmentName;
import org.kie.runtime.KieSession;
import org.kie.runtime.process.NodeInstance;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.jbpm.compiler.ProcessBuilderImpl;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JPASingleSessionCommandServiceFactoryTest {
    private static final String            TMPDIR = System.getProperty( "java.io.tmpdir" );
    private static final Logger            log    = LoggerFactory.getLogger( JPASingleSessionCommandServiceFactoryTest.class );
    private static Server                  h2Server;

    private ClassPathXmlApplicationContext ctx;

    @BeforeClass
    public static void startH2Database() throws Exception {
        DeleteDbFiles.execute( "",
                               "DroolsFlow",
                               true );
        h2Server = Server.createTcpServer( new String[0] );
        h2Server.start();
        try {
            log.info( "creating: {}",
                      TMPDIR + "/processWorkItems.pkg" );
            writePackage( getProcessWorkItems(),
                          new File( TMPDIR + "/processWorkItems.pkg" ) );

            log.info( "creating: {}",
                      TMPDIR + "/processSubProcess.pkg" );
            writePackage( getProcessSubProcess(),
                          new File( TMPDIR + "/processSubProcess.pkg" ) );

            log.info( "creating: {}",
                      TMPDIR + "/processTimer.pkg" );
            writePackage( getProcessTimer(),
                          new File( TMPDIR + "/processTimer.pkg" ) );

            log.info( "creating: {}",
                      TMPDIR + "/processTimer2.pkg" );
            writePackage( getProcessTimer2(),
                          new File( TMPDIR + "/processTimer2.pkg" ) );
        } catch ( Exception e ) {
            log.error( "can't create packages!",
                       e );
            throw new RuntimeException( e );
        }
    }

    @AfterClass
    public static void stopH2Database() throws Exception {
        log.info( "stopping database" );
        h2Server.stop();
        DeleteDbFiles.execute( "",
                               "DroolsFlow",
                               true );
    }

    @Before
    public void createSpringContext() {
        try {
            log.info( "creating spring context" );
            PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
            Properties properties = new Properties();
            properties.setProperty( "temp.dir",
                                    TMPDIR );
            configurer.setProperties( properties );
            ctx = new ClassPathXmlApplicationContext();
            ctx.addBeanFactoryPostProcessor( configurer );
            ctx.setConfigLocation("org/drools/container/spring/beans/persistence/beans.xml");
            ctx.refresh();
        } catch ( Exception e ) {
            log.error( "can't create spring context",
                       e );
            throw new RuntimeException( e );
        }
    }

    @After
    public void destroySpringContext() {
        log.info( "destroy spring context" );
        ctx.destroy();
    }

    @Test
    public void testPersistenceWorkItems() throws Exception {
        log.info( "---> get bean jpaSingleSessionCommandService" );
        KieSession service = (KieSession) ctx.getBean( "jpaSingleSessionCommandService" );

        log.info( "---> create new SingleSessionCommandService" );

        int sessionId = service.getId();
        log.info( "---> created SingleSessionCommandService id: " + sessionId );

        ProcessInstance processInstance = service.startProcess( "org.drools.test.TestProcess" );
        log.info( "Started process instance {}",
                  processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 ctx.getBean( "myEmf" ) );
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 ctx.getBean( "txManager" ) );

        KieStoreServices kstore = (KieStoreServices) ctx.getBean( "kstore1" );
        KieBase kbase1 = (KieBase) ctx.getBean( "kbProcessWorkItems" );
        service = kstore.loadKieSession(  sessionId,
                                          kbase1,
                                          null,
                                          env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                                       kbase1,
                                                       null,
                                                       env );
        service.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                                       env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        service.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        service.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        service.dispose();
    }

    @Test
    public void testPersistenceWorkItemsUserTransaction() throws Exception {

        KieSession service = (KieSession) ctx.getBean( "jpaSingleSessionCommandService" );

        int sessionId = service.getId();
        ProcessInstance processInstance = service.startProcess( "org.drools.test.TestProcess" );
        log.info( "Started process instance {}",
                  processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 ctx.getBean( "myEmf" ) );
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 ctx.getBean( "txManager" ) );

        KieStoreServices kstore = (KieStoreServices) ctx.getBean( "kstore1" );
        KieBase kbase1 = (KieBase) ctx.getBean( "kbProcessWorkItems" );
        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );

        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        service.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        service.getWorkItemManager().abortWorkItem( workItem.getId() );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        service.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );
        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNull( processInstance );
        service.dispose();
    }

    @SuppressWarnings("unused")
    private static Package getProcessWorkItems() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId( 3 );
        workItemNode.setName( "WorkItem1" );
        Work work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode.setWork( work );
        process.addNode( workItemNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode2 = new WorkItemNode();
        workItemNode2.setId( 4 );
        workItemNode2.setName( "WorkItem2" );
        work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode2.setWork( work );
        process.addNode( workItemNode2 );
        new ConnectionImpl( workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode2,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode3 = new WorkItemNode();
        workItemNode3.setId( 5 );
        workItemNode3.setName( "WorkItem3" );
        work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode3.setWork( work );
        process.addNode( workItemNode3 );
        new ConnectionImpl( workItemNode2,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode3,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( workItemNode3,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );

        return packageBuilder.getPackage();
    }

    public static void writePackage(Package pkg,
                                    File dest) {
        dest.deleteOnExit();
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( new FileOutputStream( dest ) );
            DroolsStreamUtils.streamOut( out,
                                         pkg );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        } finally {
            if ( out != null ) {
                try {
                    out.close();
                } catch ( IOException e ) {
                }
            }
        }
    }

    @Test
    public void testPersistenceSubProcess() {

        KieSession service = (KieSession) ctx.getBean( "jpaSingleSessionCommandService" );

        int sessionId = service.getId();

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) service.startProcess( "org.drools.test.ProcessSubProcess" );
        log.info( "Started process instance {}",
                  processInstance.getId() );
        long processInstanceId = processInstance.getId();

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 ctx.getBean( "myEmf" ) );
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 ctx.getBean( "txManager" ) );

        KieStoreServices kstore = (KieStoreServices) ctx.getBean( "kstore1" );
        KieBase kbase1 = (KieBase) ctx.getBean( "kbProcessWorkItems" );
        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );

        processInstance = (RuleFlowProcessInstance) service.getProcessInstance( processInstanceId );
        assertNotNull( processInstance );

        Collection<NodeInstance> nodeInstances = processInstance.getNodeInstances();
        assertEquals( 1,
                      nodeInstances.size() );
        SubProcessNodeInstance subProcessNodeInstance = (SubProcessNodeInstance) nodeInstances.iterator().next();
        long subProcessInstanceId = subProcessNodeInstance.getProcessInstanceId();
        RuleFlowProcessInstance subProcessInstance = (RuleFlowProcessInstance) service.getProcessInstance( subProcessInstanceId );
        assertNotNull( subProcessInstance );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        service.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        subProcessInstance = (RuleFlowProcessInstance) service.getProcessInstance( subProcessInstanceId );
        assertNull( subProcessInstance );

        processInstance = (RuleFlowProcessInstance) service.getProcessInstance( processInstanceId );
        assertNull( processInstance );
        service.dispose();
    }

    @SuppressWarnings("unused")
    private static Package getProcessSubProcess() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.ProcessSubProcess" );
        process.setName( "ProcessSubProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        SubProcessNode subProcessNode = new SubProcessNode();
        subProcessNode.setId( 3 );
        subProcessNode.setName( "SubProcess" );
        subProcessNode.setProcessId( "org.drools.test.SubProcess" );
        process.addNode( subProcessNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            subProcessNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 4 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( subProcessNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );

        process = new RuleFlowProcess();
        process.setId( "org.drools.test.SubProcess" );
        process.setName( "SubProcess" );
        process.setPackageName( "org.drools.test" );
        start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId( 3 );
        workItemNode.setName( "WorkItem1" );
        Work work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode.setWork( work );
        process.addNode( workItemNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        processBuilder.buildProcess( process,
                                     null );
        return packageBuilder.getPackage();
    }

    @Test
    public void testPersistenceTimer() throws Exception {
        log.info( "---> get bean jpaSingleSessionCommandService" );
        KieSession service = (KieSession) ctx.getBean( "jpaSingleSessionCommandService" );

        int sessionId = service.getId();
        log.info( "---> created SingleSessionCommandService id: " + sessionId );

        ProcessInstance processInstance = service.startProcess( "org.drools.test.ProcessTimer" );
        long procId = processInstance.getId();
        log.info( "---> Started ProcessTimer id: {}",
                  procId );
        
        service.dispose();
        log.info( "---> session disposed" );

        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 ctx.getBean( "myEmf" ) );
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 ctx.getBean( "txManager" ) );

        KieStoreServices kstore = (KieStoreServices) ctx.getBean( "kstore1" );
        KieBase kbase1 = (KieBase) ctx.getBean( "kbProcessWorkItems" );
        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );

        log.info( "---> load session: " + sessionId );
        processInstance = service.getProcessInstance( procId );
        log.info( "---> GetProcessInstanceCommand id: " + procId );
        assertNotNull( processInstance );

        Thread.sleep( 1000 );
        log.info( "---> session disposed" );
        service.dispose();

        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );
        log.info( "---> load session: " + sessionId );
        Thread.sleep( 3000 );

        log.info( "---> GetProcessInstanceCommand id: " + procId );
        processInstance = service.getProcessInstance( procId );
        log.info( "---> session disposed" );
        assertNull( processInstance );
    }

    @SuppressWarnings("unused")
    private static Package getProcessTimer() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.ProcessTimer" );
        process.setName( "ProcessTimer" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "2000" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            timerNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        return packageBuilder.getPackage();
    }

    @Test
    public void testPersistenceTimer2() throws Exception {
        KieSession service = (KieSession) ctx.getBean( "jpaSingleSessionCommandService" );

        int sessionId = service.getId();

        ProcessInstance processInstance = service.startProcess( "org.drools.test.ProcessTimer2" );
        log.info( "Started process instance {}",
                  processInstance.getId() );

        Thread.sleep( 2000 );

        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 ctx.getBean( "myEmf" ) );
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 ctx.getBean( "txManager" ) );

        KieStoreServices kstore = (KieStoreServices) ctx.getBean( "kstore1" );
        KieBase kbase1 = (KieBase) ctx.getBean( "kbProcessWorkItems" );
        service = kstore.loadKieSession( sessionId,
                                         kbase1,
                                         null,
                                         env );

        processInstance = service.getProcessInstance( processInstance.getId() );
        assertNull( processInstance );
    }

    @SuppressWarnings("unused")
    private static Package getProcessTimer2() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.ProcessTimer2" );
        process.setName( "ProcessTimer2" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "0" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            timerNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "try { Thread.sleep(1000); } catch (Throwable t) {} System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        return packageBuilder.getPackage();
    }
}
