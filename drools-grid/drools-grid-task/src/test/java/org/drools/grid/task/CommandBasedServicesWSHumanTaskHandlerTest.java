package org.drools.grid.task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.collections.map.HashedMap;

import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SystemEventListenerFactory;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.generic.NodeData;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.GenericMessageHandlerImpl;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaNodeConnector;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.grid.strategies.ReturnAlwaysTheFirstSelectionStrategy;
import org.drools.task.Group;
import org.drools.task.MockUserInfo;
import org.drools.task.User;
import org.drools.task.service.SendIcal;
import org.drools.task.service.TaskService;
import org.drools.task.service.TaskServiceSession;
import org.junit.After;
import org.junit.Before;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

public class CommandBasedServicesWSHumanTaskHandlerTest extends BaseTaskServiceTest {

    
    
    private MinaAcceptor server;
    private MinaAcceptor humanTaskServer;
    //private NodeConnector nodeConnection;
  
    protected EntityManagerFactory emf;
    
    protected static TaskService taskService;
    protected TaskServiceSession taskSession;
    protected GenericNodeConnector htMinaClient;
    protected GenericNodeConnector minaClient;
    @Before
    public void setUpTaskService() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("mail.smtp.host", "localhost");
        conf.setProperty("mail.smtp.port", "2345");
        conf.setProperty("from", "from@domain.com");
        conf.setProperty("replyTo", "replyTo@domain.com");
        conf.setProperty("defaultLanguage", "en-UK");
        SendIcal.initInstance(conf);

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
    
        
        SocketAddress address = new InetSocketAddress("127.0.0.1", 9123);
        NodeData nodeData = new NodeData();
        // Setup Execution Node Server
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new GenericMessageHandlerImpl(nodeData,
                SystemEventListenerFactory.getSystemEventListener())));
        this.server = new MinaAcceptor(acceptor, address);
        this.server.start();
        Thread.sleep(5000);
        // End Execution Server

        // Human task Server configuration
        SocketAddress htAddress = new InetSocketAddress("127.0.0.1", 9124);
        SocketAcceptor htAcceptor = new NioSocketAcceptor();

        htAcceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new TaskServerMessageHandlerImpl(taskService,
                SystemEventListenerFactory.getSystemEventListener())));
        this.humanTaskServer = new MinaAcceptor(htAcceptor, htAddress);
        this.humanTaskServer.start();
        Thread.sleep(5000);
        // End Human task Server configuration

        // setup the ht client
        NioSocketConnector htclientConnector = new NioSocketConnector();
        htclientConnector.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener()));
        htMinaClient = new MinaNodeConnector("client ht",
                htclientConnector,
                htAddress,
                SystemEventListenerFactory.getSystemEventListener());

         boolean connected = htMinaClient.connect();
            if (!connected) {
                throw new IllegalArgumentException("Could not connect task client");
            }

        // setup RemoteService client
        NioSocketConnector clientConnector = new NioSocketConnector();
        clientConnector.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener()));
        minaClient = new MinaNodeConnector("client SM",
                clientConnector,
                address,
                SystemEventListenerFactory.getSystemEventListener());



        connection.addNodeConnector(minaClient);

        node = connection.getExecutionNode(new ReturnAlwaysTheFirstSelectionStrategy(connection));
        //nodeConnection.connect();
        
        
        

        
        KnowledgeBase kbase = this.node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        int sessionId = ksession.getId();

        node.set(HumanTaskFactoryService.class, new HumanTaskServiceProviderRemoteClient(htMinaClient, sessionId));

        this.humanTaskClient = this.node.get(HumanTaskFactoryService.class).newHumanTaskService();
        

        this.handler = new CommandBasedServicesWSHumanTaskHandler(ksession);
        
        
    }
    @After
    public void tearDown() throws Exception {
        this.humanTaskClient.disconnect();
        
        for(GenericNodeConnector connector : connection.getNodeConnectors()){
            connector.disconnect();
        }
        
        this.handler.dispose();
        this.server.stop();
        this.humanTaskServer.stop();
        
        taskSession.dispose();
        emf.close();
        
       
    }

    public Object eval(Reader reader,
                       Map vars) {
        try {
            return eval(toString(reader),
                    vars);
        } catch (IOException e) {
            throw new RuntimeException("Exception Thrown",
                    e);
        }
    }

    public String toString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        int charValue;

        while ((charValue = reader.read()) != -1) {
            sb.append((char) charValue);
        }
        return sb.toString();
    }

    public Object eval(String str, Map vars) {
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
