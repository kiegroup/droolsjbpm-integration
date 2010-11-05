/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */

package org.drools.grid;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.GridNodeLocalConfiguration;
import org.drools.grid.impl.GridNodeSocketConfiguration;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.io.impl.MultiplexSocketServiceCongifuration;
import org.drools.grid.remote.GridNodeRemoteClient;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.CoreServicesWhitePagesConfiguration;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.service.directory.impl.RegisterWhitePagesConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesSocketConfiguration;
import org.drools.grid.timer.impl.CoreServicesSchedulerConfiguration;
import org.drools.grid.timer.impl.RegisterSchedulerConfiguration;
import org.drools.grid.timer.impl.SchedulerLocalConfiguration;
import org.drools.grid.timer.impl.SchedulerSocketConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class NodeTests {

    private Map<String, GridServiceDescription> coreServicesMap;
    
    public NodeTests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

   
     @Test
     public void basicLocalNodeTest() {
     
         
         Grid grid = new GridImpl(new HashMap<String, Object>());
         GridPeerConfiguration conf = new GridPeerConfiguration();
         
         GridPeerServiceConfiguration wpconf = new WhitePagesLocalConfiguration();
         conf.addConfiguration(wpconf);
         
         conf.configure(grid);
         GridServiceDescription localExecutioNodeGSD = new GridServiceDescriptionImpl("executionnode:local@local");
         localExecutioNodeGSD.addAddress("local");
         //GridNode gnode = grid.createGridNode("executionnode:local@local");
         GridNode gnode = grid.createGridNode(localExecutioNodeGSD);
         
         KnowledgeBuilder kbuilder = gnode.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
         
         Assert.assertNotNull(kbuilder);
         
         KnowledgeBase kbase = gnode.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
         
         Assert.assertNotNull(kbase);
         
         StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
         
         Assert.assertNotNull(session);
         
         WhitePages wp = grid.get(WhitePages.class);
         GridServiceDescription gsd = wp.lookup("executionnode:local@local");
         
         gnode = grid.getGridNode(gsd.getId());
         
         
         Assert.assertNotNull(gnode);
     
     }
     
      @Test
     public void basicRemoteNodeTest() {
     
         
         coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesWhitePages.class.getName() );
          
         Grid grid1 = new GridImpl(new HashMap<String, Object>()); 
         configureGrid1(grid1, 8000);
         
         
         
          
         Grid grid2 = new GridImpl(new HashMap<String, Object>());
         GridPeerConfiguration conf = new GridPeerConfiguration();
         
         GridPeerServiceConfiguration wpconf = new WhitePagesLocalConfiguration();
         conf.addConfiguration(wpconf);
         
         conf.configure(grid2);
         
         GridServiceDescription remoteExecutioNodeGSD = new GridServiceDescriptionImpl("executionnodeclient:mynode@remote[localhost:8080]/socket");
         remoteExecutioNodeGSD.setServiceInterface(GridNode.class);
         remoteExecutioNodeGSD.setImplementedClass(GridNodeRemoteClient.class);
         Address addr = remoteExecutioNodeGSD.addAddress("socket");
         addr.setObject(new InetSocketAddress[]{new InetSocketAddress("localhost", 8000)});
         
         
         
         GridNode gnode = grid2.createGridNode(remoteExecutioNodeGSD);
         
         
         KnowledgeBuilder kbuilder = gnode.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
         
         Assert.assertNotNull(kbuilder);
         
         KnowledgeBase kbase = gnode.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
         
         Assert.assertNotNull(kbase);
         
         StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
         
         Assert.assertNotNull(session);
         
         WhitePages wp = grid2.get(WhitePages.class);
         GridServiceDescription gsd = wp.lookup("executionnodeclient:mynode@remote[localhost:8080]/socket");
         
         gnode = grid2.getGridNode(gsd.getId());
         
         
         Assert.assertNotNull(gnode);
                 
                 
     
     }
     
     
     private void configureGrid1(Grid grid, int port){
    
        //Local Grid Configuration, for our client
        GridPeerConfiguration conf = new GridPeerConfiguration();

        //Configuring the Core Services White Pages
        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesWhitePagesConfiguration(coreServicesMap);
        conf.addConfiguration(coreSeviceWPConf);

        //Configuring the Core Services Scheduler
        GridPeerServiceConfiguration coreSeviceSchedulerConf = new CoreServicesSchedulerConfiguration();
        conf.addConfiguration(coreSeviceSchedulerConf);

        //Configuring the MultiplexSocketService
        GridPeerServiceConfiguration socketConf = new MultiplexSocketServiceCongifuration(new MultiplexSocketServerImpl("127.0.0.1",
                new MinaAcceptorFactoryService(),
                SystemEventListenerFactory.getSystemEventListener()));
        conf.addConfiguration(socketConf);
        
        //Configuring the WhitePages 
        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration(wplConf);
        
        //Exposing Local WhitePages
        GridPeerServiceConfiguration wpsc = new WhitePagesSocketConfiguration(port);
        conf.addConfiguration(wpsc);
        GridPeerServiceConfiguration registerwpincore = new RegisterWhitePagesConfiguration();
        conf.addConfiguration(registerwpincore);

        //Create a Local Scheduler
        GridPeerServiceConfiguration schlConf = new SchedulerLocalConfiguration("myLocalSched");
        conf.addConfiguration(schlConf);

        //Expose it to the Grid so it can be accesed by different nodes
        // I need to use the same port to reuse the service multiplexer
        GridPeerServiceConfiguration schlsc = new SchedulerSocketConfiguration(port);
        conf.addConfiguration(schlsc);
        
        GridPeerServiceConfiguration registerschedincore = new RegisterSchedulerConfiguration();
        conf.addConfiguration(registerschedincore);
        
        
        GridPeerServiceConfiguration executionNodeLocal = new GridNodeLocalConfiguration();
        conf.addConfiguration(executionNodeLocal);
        
        GridPeerServiceConfiguration executionNodeSocket = new GridNodeSocketConfiguration(port);
        conf.addConfiguration(executionNodeSocket);
        

        conf.configure(grid);
        
    
    }
     

}