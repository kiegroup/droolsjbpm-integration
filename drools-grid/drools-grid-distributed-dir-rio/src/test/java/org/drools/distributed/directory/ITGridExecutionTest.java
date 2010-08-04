package org.drools.distributed.directory;

import org.drools.grid.ConnectorException;
import org.drools.grid.AcceptorService;
import junit.framework.Assert;
import net.jini.core.lookup.ServiceItem;
import org.drools.SystemEventListenerFactory;
import org.drools.distributed.directory.impl.DistributedRioDirectoryConnector;

import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.distributed.DistributedRioNodeConnector;
import org.junit.After;




import org.junit.Before;
import org.junit.runner.RunWith;
import org.rioproject.test.RioTestRunner;
import org.rioproject.test.SetTestManager;
import org.rioproject.test.TestManager;

@RunWith(RioTestRunner.class)
public class ITGridExecutionTest extends ExecutionNodeBaseTest {

    AcceptorService server;
    @SetTestManager
    static TestManager testManager;

    @Before
    public void setUp() throws Exception {
        
        Assert.assertNotNull(testManager);

//
        DirectoryNodeService directoryService = (DirectoryNodeService) testManager.waitForService(DirectoryNodeService.class);
        Assert.assertNotNull(directoryService);


        connection.addDirectoryNode(new DistributedRioDirectoryConnector("directory1",
                                SystemEventListenerFactory.getSystemEventListener(), 
                                directoryService));

        ExecutionNodeService executionNode = (ExecutionNodeService) testManager.waitForService(ExecutionNodeService.class);
        ServiceItem[] nodeServiceItems = testManager.getServiceItems(ExecutionNodeService.class);
        System.out.println("ExecutionNodes Items =" + nodeServiceItems.length);

        for (int i = 0; i < nodeServiceItems.length; i++) {
            if (nodeServiceItems[i].service instanceof ExecutionNodeService) {
                connection.addExecutionNode(new DistributedRioNodeConnector("node"+i, 
                            SystemEventListenerFactory.getSystemEventListener(),
                            (ExecutionNodeService) nodeServiceItems[i].service));
            }
        }

        //Get an execution node using the default/round robin strategy
        node = connection.getExecutionNode();
        System.out.println("ExecutionNode = "+ node);

        directory = connection.getDirectoryNode().get(DirectoryNodeService.class);
        System.out.println("Directory Node = "+directory);


    }
    @After
    public void tearDown() throws ConnectorException  {
        connection.dispose();
    }
}
