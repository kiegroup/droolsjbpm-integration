/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.ConnectorException;
import org.drools.grid.ExecutionNode;
import org.drools.grid.internal.GenericMessageHandlerImpl;
import org.drools.grid.internal.NodeData;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.TaskServerInstance;
import org.drools.grid.services.configuration.ExecutionEnvironmentConfiguration;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.GridTopologyConfiguration;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.configuration.TaskServerInstanceConfiguration;
import org.drools.grid.services.factory.GridTopologyFactory;
import org.drools.grid.task.CommandBasedServicesWSHumanTaskHandler;
import org.drools.grid.task.HumanTaskService;
import org.drools.grid.task.TaskServerMessageHandlerImpl;
import org.drools.grid.task.responseHandlers.BlockingTaskOperationMessageResponseHandler;
import org.drools.grid.task.responseHandlers.BlockingTaskSummaryMessageResponseHandler;
import org.drools.io.impl.ClassPathResource;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import org.drools.services.task.MockUserInfo;
import org.drools.task.Group;
import org.drools.task.Status;
import org.drools.task.User;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.TaskService;
import org.drools.task.service.TaskServiceSession;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

/**
 *
 * @author salaboy
 */
public class RegisterTaskTest {

    private GridTopology grid;
    private MinaAcceptor serverTask;
    private MinaAcceptor serverNode;
    private HumanTaskService client;
    private EntityManagerFactory emf;
    private TaskService taskService;
    private TaskServiceSession taskSession;
    protected Map<String, User> users;
    protected Map<String, Group> groups;
    protected static final int DEFAULT_WAIT_TIME = 5000;
    protected static final int MANAGER_COMPLETION_WAIT_TIME = DEFAULT_WAIT_TIME;
    protected static final int MANAGER_ABORT_WAIT_TIME = DEFAULT_WAIT_TIME;
    protected CommandBasedServicesWSHumanTaskHandler handler;

    public RegisterTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws InterruptedException, IOException {

        //Task Related Stuff
        // Use persistence.xml configuration
        emf = Persistence.createEntityManagerFactory("org.drools.task");

        taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        taskSession = taskService.createSession();
        MockUserInfo userInfo = new MockUserInfo();
        taskService.setUserinfo(userInfo);
        Map vars = new HashedMap();

        Reader reader = null;

        try {
            reader = new InputStreamReader(new ClassPathResource("org/drools/task/LoadUsers.mvel").getInputStream());
            users = (Map<String, User>) eval(reader, vars);
            for (User user : users.values()) {
                taskSession.addUser(user);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            reader = null;
        }

        try {
            reader = new InputStreamReader(new ClassPathResource("org/drools/task/LoadGroups.mvel").getInputStream());
            groups = (Map<String, Group>) eval(reader, vars);
            for (Group group : groups.values()) {
                taskSession.addGroup(group);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }


        // Human task Server configuration
        SocketAddress htAddress = new InetSocketAddress("127.0.0.1", 9123);
        SocketAcceptor htAcceptor = new NioSocketAcceptor();

        htAcceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new TaskServerMessageHandlerImpl(taskService,
                SystemEventListenerFactory.getSystemEventListener())));
        this.serverTask = new MinaAcceptor(htAcceptor, htAddress);
        this.serverTask.start();
        Thread.sleep(5000);
        // End Execution Server

        //Execution Node related stuff

        System.out.println("Server 1 Starting!");
        // the servers should be started in a different machine (jvm or physical) or in another thread
        SocketAddress address = new InetSocketAddress("127.0.0.1", 9124);
        NodeData nodeData = new NodeData();
        // setup Server
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new GenericMessageHandlerImpl(nodeData,
                SystemEventListenerFactory.getSystemEventListener())));
        serverNode = new MinaAcceptor(acceptor, address);
        serverNode.start();
        System.out.println("Server 1 Started! at = " + address.toString());

        Thread.sleep(5000);

    }

    @After
    public void tearDown() throws InterruptedException, ConnectorException, RemoteException {
        client.disconnect();

        grid.dispose();



        handler.dispose();
        Assert.assertEquals(0, serverNode.getCurrentSessions());
        serverNode.stop();
        System.out.println("Execution Server Stopped!");
        Assert.assertEquals(0, serverTask.getCurrentSessions());
        serverTask.stop();
        System.out.println("Task Server Stopped!");

        taskSession.dispose();
        emf.close();





    }

    @Test
    public void MinaTaskTest() throws InterruptedException, ConnectorException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration("MyTopology");
        gridTopologyConfiguration.addTaskServerInstance(new TaskServerInstanceConfiguration("MyMinaTask", new MinaProvider("127.0.0.1", 9123)));
        gridTopologyConfiguration.addExecutionEnvironment(new ExecutionEnvironmentConfiguration("MyMinaExecutionEnv1", new MinaProvider("127.0.0.1", 9124)));


        grid = GridTopologyFactory.build(gridTopologyConfiguration);


        Assert.assertNotNull(grid);


        TaskServerInstance taskServer = grid.getTaskServerInstance("MyMinaTask");



        Assert.assertNotNull(taskServer);

        client = (HumanTaskService) taskServer.getTaskClient();
        Assert.assertNotNull(client);


        //Create a task to test the HT client. For that we need to have a ksession with a workitem that creates it
        ExecutionEnvironment ee = grid.getExecutionEnvironment("MyMinaExecutionEnv1");
        Assert.assertNotNull(ee);

        // Give me an ExecutionNode in the selected environment
        // For the Mina we have just one Execution Node per server instance
        ExecutionNode node = ee.getExecutionNode();

        Assert.assertNotNull(node);
        KnowledgeBuilder kbuilder =
                node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();


        KnowledgeBase kbase =
                node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        Assert.assertNotNull(ksession);

        handler = new CommandBasedServicesWSHumanTaskHandler(ksession);
        handler.setAddress("127.0.0.1", 9123);

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskName", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("Comment", task.getDescription());
        Assert.assertEquals(Status.Reserved, task.getStatus());
        Assert.assertEquals("Darth Vader", task.getActualOwner().getId());

        System.out.println("Starting task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        client.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        System.out.println("Completing task " + task.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        client.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(15000);
        System.out.println("Completed task " + task.getId());
        Assert.assertTrue(manager.waitTillCompleted(DEFAULT_WAIT_TIME));
        Thread.sleep(500);


    }

    private Object eval(Reader reader, Map<String, Object> vars) {
        try {
            return eval(toString(reader), vars);
        } catch (IOException e) {
            throw new RuntimeException("Exception Thrown", e);
        }
    }

    private String toString(Reader reader) throws IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer(1024);
        while ((charValue = reader.read()) != -1) {
            // result = result + (char) charValue;
            sb.append((char) charValue);
        }
        return sb.toString();
    }

    private Object eval(String str, Map<String, Object> vars) {
        ExpressionCompiler compiler = new ExpressionCompiler(str.trim());

        ParserContext context = new ParserContext();
        context.addPackageImport("org.drools.task");
        context.addPackageImport("org.drools.task.service");
        context.addPackageImport("org.drools.task.query");
        context.addPackageImport("java.util");

        vars.put("now", new Date());
        return MVEL.executeExpression(compiler.compile(context), vars);
    }
}

class TestWorkItemManager implements WorkItemManager {

    private volatile boolean completed;
    private volatile boolean aborted;
    private volatile Map<String, Object> results;

    public synchronized boolean waitTillCompleted(long time) {
        if (!isCompleted()) {
            try {
                wait(time);
            } catch (InterruptedException e) {
                // swallow and return state of completed
            }
        }

        return isCompleted();
    }

    public synchronized boolean waitTillAborted(long time) {
        if (!isAborted()) {
            try {
                wait(time);
            } catch (InterruptedException e) {
                // swallow and return state of aborted
            }
        }

        return isAborted();
    }

    public void abortWorkItem(long id) {
        setAborted(true);
    }

    public synchronized boolean isAborted() {
        return aborted;
    }

    private synchronized void setAborted(boolean aborted) {
        this.aborted = aborted;
        notifyAll();
    }

    public void completeWorkItem(long id, Map<String, Object> results) {
        this.results = results;
        setCompleted(true);
    }

    private synchronized void setCompleted(boolean completed) {
        this.completed = completed;
        notifyAll();
    }

    public synchronized boolean isCompleted() {
        return completed;
    }

    public WorkItem getWorkItem(long id) {
        return null;
    }

    public Set<WorkItem> getWorkItems() {
        return null;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void internalAbortWorkItem(long id) {
    }

    public void internalAddWorkItem(WorkItem workItem) {
    }

    public void internalExecuteWorkItem(WorkItem workItem) {
    }

    public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
    }
}
