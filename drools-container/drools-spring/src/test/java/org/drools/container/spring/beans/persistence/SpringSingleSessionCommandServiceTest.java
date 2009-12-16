package org.drools.container.spring.beans.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.command.runtime.process.GetProcessInstanceCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.ProcessBuilder;
import org.drools.definition.KnowledgePackage;
import org.drools.definitions.impl.KnowledgePackageImp;
import org.drools.helper.FileHelper;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.persistence.processinstance.JPAProcessInstanceManagerFactory;
import org.drools.persistence.processinstance.JPASignalManagerFactory;
import org.drools.persistence.processinstance.JPAWorkItemManagerFactory;
import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.process.core.timer.Timer;
import org.drools.rule.Package;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.ruleflow.instance.RuleFlowProcessInstance;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.impl.DroolsConsequenceAction;
import org.drools.workflow.core.node.ActionNode;
import org.drools.workflow.core.node.EndNode;
import org.drools.workflow.core.node.StartNode;
import org.drools.workflow.core.node.SubProcessNode;
import org.drools.workflow.core.node.TimerNode;
import org.drools.workflow.core.node.WorkItemNode;
import org.drools.workflow.instance.node.SubProcessNodeInstance;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

public class SpringSingleSessionCommandServiceTest {
	private static final Logger log = LoggerFactory.getLogger(SpringSingleSessionCommandServiceTest.class);
	private static Server h2Server;
    
    private ClassPathXmlApplicationContext ctx;
    private EntityManagerFactory emf;
    private PlatformTransactionManager txManager;
    
    @BeforeClass
    public static void startH2Database() throws Exception {
    	File dbDir = new File(System.getProperty("java.io.tmpdir") + "/_droolsFlowDB");
    	if (dbDir.exists()) {
    		log.info("database exists, deleting before run tests, db location: {}", dbDir.getAbsolutePath());
    		FileHelper.remove(dbDir);
    	}
    	dbDir.mkdirs();
    	log.info("creating database on: {}", dbDir );
    	h2Server = Server.createTcpServer(new String[] {"-baseDir " + dbDir.getAbsolutePath()});
    	h2Server.start();
    }
    
    @AfterClass
    public static void stopH2Database() {
    	log.info("stoping database");
    	h2Server.stop();
    	File dbDir = new File(System.getProperty("java.io.tmpdir") + "/_droolsFlowDB");
    	log.info("deleting database: {}", dbDir.getAbsoluteFile());
    	FileHelper.remove(dbDir);
    }

    @Before
    public void createSpringContext() {
    	log.info("creating spring context");
    	ctx = new ClassPathXmlApplicationContext("org/drools/container/spring/beans/persistence/beans.xml");
    	emf = (EntityManagerFactory) ctx.getBean("myEmf");
    	txManager = (PlatformTransactionManager) ctx.getBean("txManager");
    }
    
    @After
    public void destrySpringContext() {
    	log.info("destroy spring context");
    	ctx.destroy();
    }
    
	private SpringSingleSessionCommandService buildCommandService(KnowledgeBase kbase) {
		Environment env = KnowledgeBaseFactory.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		env.set(EnvironmentName.TRANSACTION_MANAGER, txManager);
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SpringSingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                JPAProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                JPAWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                JPASignalManagerFactory.class.getName() );
        SessionConfiguration config = new SessionConfiguration( properties );

        return new SpringSingleSessionCommandService( kbase, config, env );
	}
	
	private SpringSingleSessionCommandService buildCommandService(int sessionId, KnowledgeBase kbase) {
		Environment env = KnowledgeBaseFactory.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		env.set(EnvironmentName.TRANSACTION_MANAGER, txManager);
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SpringSingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                JPAProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                JPAWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                JPASignalManagerFactory.class.getName() );
        SessionConfiguration config = new SessionConfiguration( properties );

        return new SpringSingleSessionCommandService(sessionId, kbase, config, env );
	}
    
    @Test
    public void testPersistenceWorkItems() throws Exception {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessWorkItems();
        kbase.addKnowledgePackages( kpkgs );

        SpringSingleSessionCommandService service = buildCommandService(kbase);

        int sessionId = service.getSessionId();

        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        log.info( "Started process instance {}", processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        Assert.assertNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNull( processInstance );
        service.dispose();
    }

    @Test
    public void testPersistenceWorkItemsUserTransaction() throws Exception {
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessWorkItems();
        kbase.addKnowledgePackages( kpkgs );

        SpringSingleSessionCommandService service = buildCommandService(kbase);
        int sessionId = service.getSessionId();

        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        log.info( "Started process instance {}", processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service =buildCommandService(sessionId, kbase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        Assert.assertNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNull( processInstance );
        service.dispose();
    }

    private Collection<KnowledgePackage> getProcessWorkItems() {
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
        ProcessBuilder processBuilder = new ProcessBuilder( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( new KnowledgePackageImp( packageBuilder.getPackage() ) );
        return list;
    }

    @Test
    public void testPersistenceSubProcess() {
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        Package pkg = getProcessSubProcess();
        ruleBase.addPackage( pkg );

        SpringSingleSessionCommandService service = buildCommandService( new KnowledgeBaseImpl( ruleBase ));
        int sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) service.execute( startProcessCommand );
        log.info( "Started process instance {}", processInstance.getId() );
        long processInstanceId = processInstance.getId();

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        Assert.assertNotNull( workItem );
        service.dispose();

        service = buildCommandService(sessionId, new KnowledgeBaseImpl( ruleBase ));
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        processInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );

        Collection<NodeInstance> nodeInstances = processInstance.getNodeInstances();
        Assert.assertEquals( 1,
                      nodeInstances.size() );
        SubProcessNodeInstance subProcessNodeInstance = (SubProcessNodeInstance) nodeInstances.iterator().next();
        long subProcessInstanceId = subProcessNodeInstance.getProcessInstanceId();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( subProcessInstanceId );
        RuleFlowProcessInstance subProcessInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( subProcessInstance );
        service.dispose();

        service = buildCommandService(sessionId, new KnowledgeBaseImpl( ruleBase ));
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        service.dispose();

        service = buildCommandService(sessionId, new KnowledgeBaseImpl( ruleBase ));
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( subProcessInstanceId );
        subProcessInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        Assert.assertNull( subProcessInstance );

        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        processInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        Assert.assertNull( processInstance );
        service.dispose();
    }

    private Package getProcessSubProcess() {
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
        ProcessBuilder processBuilder = new ProcessBuilder( packageBuilder );
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
    	KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer();
        kbase.addKnowledgePackages( kpkgs );

        SpringSingleSessionCommandService service = buildCommandService(kbase);
        int sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        log.info( "Started process instance {}", processInstance.getId() );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNotNull( processInstance );
        service.dispose();

        service = buildCommandService(sessionId, kbase);
        Thread.sleep( 3000 );
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNull( processInstance );
    }

    private List<KnowledgePackage> getProcessTimer() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
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
        ProcessBuilder processBuilder = new ProcessBuilder( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( new KnowledgePackageImp( packageBuilder.getPackage() ) );
        return list;
    }

    @Test
    public void testPersistenceTimer2() throws Exception {

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer2();
        kbase.addKnowledgePackages( kpkgs );

        SpringSingleSessionCommandService service = buildCommandService(kbase);
        int sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        log.info( "Started process instance {}", processInstance.getId() );

        Thread.sleep( 2000 );

        service = buildCommandService(sessionId, kbase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        Assert.assertNull( processInstance );
    }

    private List<KnowledgePackage> getProcessTimer2() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
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
        ProcessBuilder processBuilder = new ProcessBuilder( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( new KnowledgePackageImp( packageBuilder.getPackage() ) );
        return list;
    }
}
