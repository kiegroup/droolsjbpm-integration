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
package org.drools.io.mina;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.CoreServicesLookup;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.SocketService;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.io.Connector;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.io.impl.MultiplexSocketServiceConfiguration;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.remote.mina.MinaConnector;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.CoreServicesLookupConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesClient;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesSocketConfiguration;
import org.drools.grid.time.impl.MockJob;
import org.drools.grid.timer.impl.CoreServicesSchedulerConfiguration;
import org.drools.grid.timer.impl.RegisterSchedulerConfiguration;
import org.drools.grid.timer.impl.SchedulerClient;
import org.drools.grid.timer.impl.SchedulerLocalConfiguration;
import org.drools.grid.timer.impl.SchedulerSocketConfiguration;
import org.drools.grid.util.IoUtils;
import org.drools.time.SchedulerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class RegisterServicesTest {

    private SystemEventListener                 l = SystemEventListenerFactory.getSystemEventListener();
    private Map<String, GridServiceDescription> coreServicesMap;

    public RegisterServicesTest() {
    }
    
    @Test @Ignore
    public void testDummy() {
        
    }

//    @Test
//    public void testRegisterInCoreServices() {
//
//        coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );
//
//        GridImpl grid = new GridImpl( new HashMap<String, Object>() );
//
//        //Local Grid Configuration, for our client
//        GridPeerConfiguration conf = new GridPeerConfiguration();
//
//        //Configuring the Core Services White Pages
//        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesLookupConfiguration( coreServicesMap );
//        conf.addConfiguration( coreSeviceWPConf );
//
//        //Configuring the Core Services Scheduler
//        GridPeerServiceConfiguration coreSeviceSchedulerConf = new CoreServicesSchedulerConfiguration();
//        conf.addConfiguration( coreSeviceSchedulerConf );
//
//        //Configuring the SocketService
//        MultiplexSocketServiceCongifuration socketConf = new MultiplexSocketServiceCongifuration( new MultiplexSocketServerImpl( "127.0.0.1",
//                                                                                                                          new MinaAcceptorFactoryService(),
//                                                                                                                          l,
//                                                                                                                          grid ) );
//        
//        //Configuring the WhitePages 
//        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
//        conf.addConfiguration( wplConf );
//
//        socketConf.addService( WhitePages.class.getName(), wplConf.getWhitePages(), 8000  );
//        
//        
//        conf.addConfiguration( socketConf );
//
//        //Create a Local Scheduler
//        GridPeerServiceConfiguration schlConf = new SchedulerLocalConfiguration( "myLocalSched" );
//        conf.addConfiguration( schlConf );
//
//        //Expose it to the Grid so it can be accesed by different nodes
//        // I need to use the same port to reuse the service multiplexer
//        GridPeerServiceConfiguration schlsc = new SchedulerSocketConfiguration( 5012 );
//        conf.addConfiguration( schlsc );
//
//        GridPeerServiceConfiguration registerschedincore = new RegisterSchedulerConfiguration();
//        conf.addConfiguration( registerschedincore );
//
//        conf.configure( grid );
//
//        //Local White Pages
//        WhitePages wp = grid.get( WhitePages.class );
//
//        //Local sched in Local WP
//        GridServiceDescription gsdLocalSched = wp.lookup( "scheduler:" + "myLocalSched" + SchedulerService.class.getName() );
//
//        //Get the CoreWhitePages
//        CoreServicesLookup corewp = grid.get( CoreServicesLookup.class );
//        //Get the registered Scheduler
//        GridServiceDescription gsdLocalButExposedSched = corewp.lookup( SchedulerService.class );
//        //Get the registered white pages
//        GridServiceDescription gsdLocalButExposedWp = corewp.lookup( WhitePages.class );
//
//        //The Scheduler is local = no addresses and no Data
//        assertEquals( 0,
//                             gsdLocalSched.getAddresses().size() );
//        assertNull( gsdLocalSched.getData() );
//
//        assertNotNull( gsdLocalButExposedSched.getData() );
//
//        Connector conn = new MinaConnector();
//
//        ConversationManager cm = new ConversationManagerImpl( "s1",
//                                                              conn,
//                                                              l );
//
//        SchedulerClient sched = null;
//
//        GridServiceDescription clientSched1 = wp.lookup( "scheduler:" + "myLocalSched" + SchedulerService.class.getName() );
//        sched = new SchedulerClient( grid,
//                                     clientSched1,
//                                     cm );
//        sched.scheduleJob( new MockJob(),
//                           new MockJobContext( "xxx" ),
//                           new MockTrigger( new Date( 1000 ) ) );
//
//        sched = new SchedulerClient( grid,
//                                     gsdLocalButExposedSched,
//                                     cm );
//        sched.scheduleJob( new MockJob(),
//                           new MockJobContext( "xxx" ),
//                           new MockTrigger( new Date( 1000 ) ) );
//
//        //GridServiceDescription clientSched2 = new WhitePagesClient( gsdLocalButExposedWp, cm).lookup(SchedulerService.class.getName());
//
//        conn.close();
//
//        grid.get( SocketService.class ).close();
//
//    }
}
