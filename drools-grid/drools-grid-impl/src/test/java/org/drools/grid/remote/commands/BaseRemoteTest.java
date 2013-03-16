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

package org.drools.grid.remote.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.drools.grid.ConnectionFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.SocketService;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.io.impl.MultiplexSocketServiceConfiguration;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.CoreServicesLookupConfiguration;
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.timer.impl.CoreServicesSchedulerConfiguration;
import org.drools.core.io.impl.ByteArrayResource;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.kie.KieBaseConfiguration;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactoryService;
import org.kie.SystemEventListenerFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderError;
import org.kie.builder.KnowledgeBuilderErrors;
import org.kie.builder.KnowledgeBuilderFactoryService;
import org.kie.conf.EqualityBehaviorOption;
import org.kie.io.ResourceType;
import org.kie.runtime.StatefulKnowledgeSession;

public abstract class BaseRemoteTest {
    
    
    
    private Map<String, GridServiceDescription> coreServicesMap;
    protected Grid grid1;
    
    protected GridNode remoteN1;
    protected GridServiceDescription gsdN1;
    private Server server;
    
    @Before
    public void setUp() {
         DeleteDbFiles.execute("~", "mydb", false);

        System.out.println("Staring DB for white pages ...");
        
        try {
            
            server = Server.createTcpServer(new String[] {"-tcp","-tcpAllowOthers","-tcpDaemon","-trace"}).start(); 
        } catch (SQLException ex) {
            System.out.println("ERROR: "+ex.getMessage());
            
        }
        System.out.println("DB for white pages started! ");

        this.coreServicesMap = new HashMap();
        gsdN1 = createRemoteNode();
    }

    @After
    public void tearDown() {
        
        
        remoteN1.dispose();
        
        grid1.removeGridNode( gsdN1.getId() );
        
        grid1.get(SocketService.class).close();
        
        server.stop();
        
    }
    
    private GridServiceDescription createRemoteNode(){
        grid1 = new GridImpl("peer1", new HashMap<String, Object>() );
        configureGrid1( grid1,
                        8000,
                        new JpaWhitePages(Persistence.createEntityManagerFactory("org.drools.grid")) );

        Grid grid2 = new GridImpl("peer2", new HashMap<String, Object>() );
        configureGrid1( grid2,
                        -1,
                        grid1.get( WhitePages.class ) );

        GridNode n1 = grid1.createGridNode( "n1" );
        grid1.get( SocketService.class ).addService( "n1", 8000, n1 );
               
        GridServiceDescription<GridNode> n1Gsd = grid2.get( WhitePages.class ).lookup( "n1" );
        GridConnection<GridNode> conn = grid2.get( ConnectionFactoryService.class ).createConnection( n1Gsd );
        remoteN1 = conn.connect();
        
        return n1Gsd;
    
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
        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
        wplConf.setWhitePages( wp );
        conf.addConfiguration( wplConf );

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
    
    protected StatefulKnowledgeSession createSession(){
        KnowledgeBuilder kbuilder = remoteN1.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        assertNotNull( kbuilder );

         String rule = "package test\n"
                 + "import org.drools.grid.NodeTests.MyObject;\n"
                 + "global MyObject myGlobalObj;\n"
                 + "query getMyObjects(String n)\n"
                 + "  $mo: MyObject(name == n)\n"
                 + "end\n"
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
        KieBaseConfiguration kbaseConf = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBaseConfiguration();
        kbaseConf.setProperty(EqualityBehaviorOption.PROPERTY_NAME, "equality");
        KnowledgeBase kbase = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase(kbaseConf);

        assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        
        remoteN1.set("ksession-rules", session);
        
        return session;
    
    }

    
    protected StatefulKnowledgeSession createProcessSession(){
        KnowledgeBuilder kbuilder = remoteN1.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        assertNotNull( kbuilder );

        String process = "<definitions id=\"Definition\" "
                + "targetNamespace=\"http://www.example.org/MinimalExample\" "
                + "typeLanguage=\"http://www.java.com/javaTypes\" "
                + "expressionLanguage=\"http://www.mvel.org/2.0\" "
                + "xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
                + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xs:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\" "
                + "xmlns:tns=\"http://www.jboss.org/drools\">"
                + "<process id=\"Minimal\" name=\"Minimal Process\" tns:packageName=\"com.sample\">"
                +   "<startEvent id=\"_1\" name=\"StartProcess\"/>"
                +     "<sequenceFlow sourceRef=\"_1\" targetRef=\"_2\"/>"
                +   "<scriptTask id=\"_2\" name=\"Hello\">"
                +       "<script>System.out.println(\"Hello World\");</script>"
                +   "</scriptTask>"
                +   "<sequenceFlow sourceRef=\"_2\" targetRef=\"_3\"/>"
                +   "<endEvent id=\"_3\" name=\"EndProcess\">"
                +      "<terminateEventDefinition/>"
                +   "</endEvent>"
                + "</process>"
                + "</definitions>";
        System.out.println("Process = "+process);
         
        kbuilder.add( new ByteArrayResource( process.getBytes() ),
                      ResourceType.BPMN2 );
        
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( errors != null && errors.size() > 0 ) {
            for ( KnowledgeBuilderError error : errors ) {
                System.out.println( "Error: " + error.getMessage() );

            }
            fail("KnowledgeBase did not build");
        }
        KieBaseConfiguration kbaseConf = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBaseConfiguration();
        kbaseConf.setProperty(EqualityBehaviorOption.PROPERTY_NAME, "equality");
        KnowledgeBase kbase = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase(kbaseConf);

        assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        
        remoteN1.set("ksession-processes", session);
        
        return session;
    
    }

}
