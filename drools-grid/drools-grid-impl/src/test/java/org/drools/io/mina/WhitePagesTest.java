package org.drools.io.mina;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.kie.SystemEventListener;
import org.kie.SystemEventListenerFactory;

import static org.junit.Assert.*;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.SocketService;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.service.directory.impl.CoreServicesLookupConfiguration;
import org.drools.grid.io.impl.MultiplexSocketServiceConfiguration;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesRemoteConfiguration;
import org.drools.grid.timer.impl.SchedulerLocalConfiguration;
import org.drools.time.SchedulerService;

public class WhitePagesTest {

    @Test
    public void test1() throws Exception {
        Map<String, GridServiceDescription> coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );

        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        GridImpl grid1 = new GridImpl("peer1", new ConcurrentHashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        GridPeerServiceConfiguration coreSeviceConf = new CoreServicesLookupConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        MultiplexSocketServiceConfiguration socketConf = new MultiplexSocketServiceConfiguration( new MultiplexSocketServerImpl( "127.0.0.1",
                                                                                                                          new MinaAcceptorFactoryService(),
                                                                                                                          l,
                                                                                                                          grid1) );
        conf.addConfiguration( socketConf );

        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wplConf );

        socketConf.addService( WhitePages.class.getName(), wplConf.getWhitePages(), 8000 );

        conf.configure( grid1 );

        GridImpl grid2 = new GridImpl( "peer2", new ConcurrentHashMap<String, Object>() );
        conf = new GridPeerConfiguration();

        //coreServicesMap = Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );
        coreSeviceConf = new CoreServicesLookupConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        GridPeerServiceConfiguration wprConf = new WhitePagesRemoteConfiguration( );
        conf.addConfiguration( wprConf );

        conf.configure( grid2 );

        WhitePages wpClient = grid2.get( WhitePages.class );

        GridServiceDescription test1Gsd = wpClient.create( "test:string@domain1", "grid0" );

        GridServiceDescription testGsd_2 = wpClient.lookup( "test:string@domain1" );
        assertEquals( test1Gsd,
                      testGsd_2 );
        assertNotSame( test1Gsd,
                       testGsd_2 );

        WhitePages localWhitePages = grid1.get( WhitePages.class );
        GridServiceDescription testGsd_3 = localWhitePages.lookup( "test:string@domain1" );

        assertEquals( test1Gsd,
                      testGsd_3 );
        assertNotSame( test1Gsd,
                       testGsd_3 );

        grid1.get( SocketService.class ).close();
    }

    @Test
    public void testWhitePagesLookupServices() {
        Map<String, GridServiceDescription> coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );

        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        GridImpl grid1 = new GridImpl( new ConcurrentHashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        GridPeerServiceConfiguration coreSeviceConf = new CoreServicesLookupConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wplConf );

        //Create a Local Scheduler
        GridPeerServiceConfiguration schlConf = new SchedulerLocalConfiguration( "myLocalSched1" );
        conf.addConfiguration( schlConf );

        //Create a Local Scheduler
        GridPeerServiceConfiguration schlConf2 = new SchedulerLocalConfiguration( "myLocalSched2" );
        conf.addConfiguration( schlConf2 );

        conf.configure( grid1 );

        WhitePages wplocal = grid1.get( WhitePages.class );
        assertNotNull( wplocal );
        GridServiceDescription schedulersgsd = wplocal.lookup( "scheduler:" + "myLocalSched1" + SchedulerService.class.getName() );

        assertNotNull( schedulersgsd );

    }

}
