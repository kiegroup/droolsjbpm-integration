package org.drools.grid.distributed;

import java.rmi.RemoteException;
import java.util.List;
import org.drools.grid.ConnectorException;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.AcceptorService;
import junit.framework.Assert;

import net.jini.core.lookup.ServiceItem;
import org.drools.SystemEventListenerFactory;
import org.drools.distributed.directory.impl.DistributedRioDirectoryConnector;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.distributed.impl.DistributedRioNodeConnector;
import org.junit.After;



import org.junit.Before;
import org.junit.runner.RunWith;
import org.rioproject.gnostic.Gnostic;
import org.rioproject.sla.RuleMap;
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
//        //setup all the services using the Rio Test Framework
//        //In real scenarios all this code will happen inside the connection.connect() method
//
        DirectoryNodeService directoryService = (DirectoryNodeService) testManager.waitForService(DirectoryNodeService.class);
        Assert.assertNotNull(directoryService);
        Gnostic gnostic = (Gnostic)testManager.waitForService(Gnostic.class);
//
        waitForRule(gnostic, "SLAKsessions");
        
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
                                ((ExecutionNodeService) nodeServiceItems[i].service).getId()));
            }
        }

        //Get an execution node using the default/round robin strategy
        node = connection.getExecutionNode();
        System.out.println("ExecutionNode = "+ node);


    }
    @After
    public void tearDown() throws ConnectorException  {
        connection.dispose();
    }

    private void waitForRule(Gnostic g, String rule) {
        Throwable thrown = null;
        long t0 = System.currentTimeMillis();
        try {
            while (!hasRule(g.get(), rule)) {
                sleep(500);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            thrown = e;
        }
        Assert.assertNull(thrown);
        System.out.println("Rule loaded in " + (System.currentTimeMillis() - t0) + " millis");
    }

     private boolean hasRule(List<RuleMap> ruleMaps, String rule) {
        boolean hasRule = false;
        for (RuleMap ruleMap : ruleMaps) {
            System.out.println("===> rule: " + ruleMap.getRuleDefinition().getResource());
            if (ruleMap.getRuleDefinition().getResource().indexOf(rule) != -1) {
                hasRule = true;
                break;
            }
        }
        return hasRule;
    }
    public static void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {

        }
    }

}
