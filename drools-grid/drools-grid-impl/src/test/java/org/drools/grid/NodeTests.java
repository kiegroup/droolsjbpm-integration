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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.io.impl.MultiplexSocketServiceConfiguration;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.CoreServicesLookupConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesImpl;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.timer.impl.CoreServicesSchedulerConfiguration;
import org.drools.io.impl.ByteArrayResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactoryService;
import org.kie.SystemEventListenerFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderError;
import org.kie.builder.KnowledgeBuilderErrors;
import org.kie.builder.KnowledgeBuilderFactoryService;
import org.kie.builder.ResourceType;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

import static org.junit.Assert.*;

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
        this.coreServicesMap = new HashMap();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void localNodeTest() {
        Grid grid = new GridImpl( new HashMap<String, Object>() );
        GridPeerConfiguration conf = new GridPeerConfiguration();

        GridPeerServiceConfiguration wpconf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wpconf );

        conf.configure( grid );

        GridNode gnode = grid.createGridNode( "n1" );

        KnowledgeBuilder kbuilder = gnode.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
        assertNotNull( kbuilder );

        KnowledgeBase kbase = gnode.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();
        assertNotNull( kbase );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        assertNotNull( session );

        WhitePages wp = grid.get( WhitePages.class );
        GridServiceDescription gsd = wp.lookup( "n1" );
        assertNotNull(gsd);
        assertEquals( 0, gsd.getAddresses().size() );

        gnode = grid.getGridNode( gsd.getId() );
        assertNotNull( gnode );
        
        grid.removeGridNode( gsd.getId() );
        assertNull( wp.lookup( "n1" ) );
        assertNull( grid.getGridNode( gsd.getId() ) );
        
    }


    @Test
    public void remoteNodeTest() {
        Grid grid1 = new GridImpl( new HashMap<String, Object>() );
        configureGrid1( grid1,
                        8000,
                        new WhitePagesImpl() );

        Grid grid2 = new GridImpl( new HashMap<String, Object>() );
        configureGrid1( grid2,
                        -1,
                        null );

        GridNode n1 = grid1.createGridNode( "n1" );
        grid1.get( SocketService.class ).addService( "n1", 8000, n1 );
               
        GridServiceDescription<GridNode> n1Gsd = grid2.get( WhitePages.class ).lookup( "n1" );
        GridConnection<GridNode> conn = grid2.get( ConnectionFactoryService.class ).createConnection( n1Gsd );
        GridNode remoteN1 = conn.connect();

        KnowledgeBuilder kbuilder = remoteN1.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        Assert.assertNotNull( kbuilder );

        String rule = "package test\n"
                      + "rule \"test\""
                      + "  when"
                      + "  then"
                      + "      System.out.println(\"Rule Fired!\");"
                      + " end";

        kbuilder.add( new ByteArrayResource( rule.getBytes() ),
                      ResourceType.DRL );

        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( errors != null && errors.size() > 0 ) {
            for ( KnowledgeBuilderError error : errors ) {
                System.out.println( "Error: " + error.getMessage() );

            }
            fail("KnowledgeBase did not build");
        }

        KnowledgeBase kbase = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();

        Assert.assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        Assert.assertNotNull( session );

        FactHandle handle = session.insert( new MyObject("myObj1") );
        Assert.assertNotNull( handle );

        int i = session.fireAllRules();
        Assert.assertEquals( 1,
                             i );
        
        remoteN1.dispose();
         grid1.get(SocketService.class).close();

    }
    
     @Test
    public void remoteNodeRetractUpdateGlobalsTest() {
        Grid grid1 = new GridImpl( new HashMap<String, Object>() );
        configureGrid1( grid1,
                        8000,
                        new WhitePagesImpl() );

        Grid grid2 = new GridImpl( new HashMap<String, Object>() );
        configureGrid1( grid2,
                        -1,
                        null );

        GridNode n1 = grid1.createGridNode( "n1" );
        grid1.get( SocketService.class ).addService( "n1", 8000, n1 );
               
        GridServiceDescription<GridNode> n1Gsd = grid2.get( WhitePages.class ).lookup( "n1" );
        GridConnection<GridNode> conn = grid2.get( ConnectionFactoryService.class ).createConnection( n1Gsd );
        GridNode remoteN1 = conn.connect();

        KnowledgeBuilder kbuilder = remoteN1.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        Assert.assertNotNull( kbuilder );

         String rule = "package test\n"
                 + "import org.drools.grid.NodeTests.MyObject;\n"
                 + "global MyObject myGlobalObj;\n"
                 + "rule \"test\""
                 + "  when"
                 + "       $o: MyObject()"
                 + "  then"
                 + "      System.out.println(\"My Global Object -> \"+myGlobalObj.getName());"
                 + "      System.out.println(\"Rule Fired! ->\"+$o.getName());"
                 + " end";

        kbuilder.add( new ByteArrayResource( rule.getBytes() ),
                      ResourceType.DRL );

        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( errors != null && errors.size() > 0 ) {
            for ( KnowledgeBuilderError error : errors ) {
                System.out.println( "Error: " + error.getMessage() );

            }
            fail("KnowledgeBase did not build");
        }

        KnowledgeBase kbase = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();

        Assert.assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        Assert.assertNotNull( session );
        session.setGlobal("myGlobalObj", new MyObject("myGlobalObj"));

        FactHandle handle = session.insert( new MyObject("myObj1") );
        Assert.assertNotNull( handle );

        int fired = session.fireAllRules();
        Assert.assertEquals( 1,
                             fired );
        
         session.retract(handle);
         
         
         handle = session.insert(new MyObject("myObj2"));
         
         session.update(handle, new MyObject("myObj3"));
         
         fired = session.fireAllRules();
         
         remoteN1.dispose();
         grid1.get(SocketService.class).close();

    }
     
    private void configureGrid1(Grid grid,
                                int port,
                                WhitePages wp) {

        //Local Grid Configuration, for our client
        GridPeerConfiguration conf = new GridPeerConfiguration();

        //Configuring the Core Services White Pages
        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesLookupConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceWPConf );

        //Configuring the Core Services Scheduler
        GridPeerServiceConfiguration coreSeviceSchedulerConf = new CoreServicesSchedulerConfiguration();
        conf.addConfiguration( coreSeviceSchedulerConf );

        //Configuring the WhitePages 
        WhitePagesLocalConfiguration wplConf = null;
        if ( wp != null ) {
            wplConf = new WhitePagesLocalConfiguration();
            wplConf.setWhitePages( wp );
            conf.addConfiguration( wplConf );
        }

//        //Create a Local Scheduler
//        SchedulerLocalConfiguration schlConf = new SchedulerLocalConfiguration( "myLocalSched" );
//        conf.addConfiguration( schlConf );
        
        if ( port >= 0 ) {
            //Configuring the SocketService
            MultiplexSocketServiceConfiguration socketConf = new MultiplexSocketServiceConfiguration( new MultiplexSocketServerImpl( "127.0.0.1",
                                                                                                                                     new MinaAcceptorFactoryService(),
                                                                                                                                     SystemEventListenerFactory.getSystemEventListener(),
                                                                                                                                     grid) );
            socketConf.addService( WhitePages.class.getName(), wplConf.getWhitePages(), port );
//            socketConf.addService( SchedulerService.class.getName(), schlConf.getSchedulerService(), port );
                        
            conf.addConfiguration( socketConf );
        }
        conf.configure( grid );

    }

    public static class MyObject
        implements
        Serializable {
        private String name;
        public MyObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyObject other = (MyObject) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "MyObject{" + "name=" + name + '}';
        }

        
       
        
        
    }
}
