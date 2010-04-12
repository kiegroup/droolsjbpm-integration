package org.drools.grid.distributed;

import org.drools.grid.ExecutionNodeService;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.AcceptorService;
import java.util.Iterator;
import junit.framework.Assert;

import net.jini.core.lookup.ServiceItem;
import org.drools.grid.generic.GenericNodeConnector;
import org.junit.After;




import org.junit.Before;
import org.junit.runner.RunWith;
import org.rioproject.gnostic.Gnostic;
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
        //setup all the services using the Rio Test Framework
        //In real scenarios all this code will happen inside the connection.connect() method
        ExecutionNodeService nodeService = (ExecutionNodeService) testManager.waitForService(ExecutionNodeService.class);
        ServiceItem[] nodeServiceItems = testManager.getServiceItems(ExecutionNodeService.class);
        System.out.println("Node Service Items =" + nodeServiceItems.length);
        DirectoryNodeService directoryService = (DirectoryNodeService) testManager.waitForService(DirectoryNodeService.class);
        Assert.assertNotNull(directoryService);
        Gnostic service = (Gnostic)testManager.waitForService(Gnostic.class);

        connection.addDirectoryNode(directoryService);

        for (int i = 0; i < nodeServiceItems.length; i++) {
            if (nodeServiceItems[i].service instanceof ExecutionNodeService) {
                connection.addNodeConnector((ExecutionNodeService) nodeServiceItems[i].service);
            }
        }

        //Get an execution node using the default/round robin strategy
        node = connection.getExecutionNode();
        System.out.println("Node = "+ node);


    }
    @After
    public void tearDown() throws Exception {
        Iterator<GenericNodeConnector> iterator = connection.getNodeConnectors().iterator();
        while(iterator.hasNext()){
            iterator.next().disconnect();
        }
        Iterator<DirectoryNodeService> iterator1 = connection.getDirectories().iterator();
        while(iterator.hasNext()){
            iterator.next().disconnect();
        }
    }
}
