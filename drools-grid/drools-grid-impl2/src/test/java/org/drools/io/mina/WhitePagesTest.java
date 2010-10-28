package org.drools.io.mina;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import junit.framework.Assert;

import junit.framework.TestCase;

import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.GridPeerConfiguration;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MultiplexSocketService;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.io.Connector;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.service.directory.impl.CoreServicesWhitePagesConfiguration;
import org.drools.grid.io.impl.MultiplexSocketServiceCongifuration;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.remote.mina.MinaConnector;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesRemoteConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesSocketConfiguration;
import org.drools.grid.timer.impl.SchedulerLocalConfiguration;
import org.drools.time.SchedulerService;


public class WhitePagesTest extends TestCase {

    public void test1() throws Exception {
        Map<String, GridServiceDescription> coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesWhitePages.class.getName() );

        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        GridImpl grid1 = new GridImpl( new ConcurrentHashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        GridPeerServiceConfiguration coreSeviceConf = new CoreServicesWhitePagesConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        GridPeerServiceConfiguration socketConf = new MultiplexSocketServiceCongifuration( new MultiplexSocketServerImpl( "127.0.0.1",
                                                                                                                          new MinaAcceptorFactoryService(),
                                                                                                                          l ) );
        conf.addConfiguration( socketConf );

        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wplConf );
        
        GridPeerServiceConfiguration wpsc = new WhitePagesSocketConfiguration(5012);
        conf.addConfiguration( wpsc );

        conf.configure( grid1 );
        
        

        GridImpl grid2 = new GridImpl( new ConcurrentHashMap<String, Object>() );
        conf = new GridPeerConfiguration();

        //coreServicesMap = Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesWhitePages.class.getName() );
        coreSeviceConf = new CoreServicesWhitePagesConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        Connector conn = new MinaConnector();

        ConversationManager cm = new ConversationManagerImpl( "s1",
                                                              conn,
                                                              l );

        GridPeerServiceConfiguration wprConf = new WhitePagesRemoteConfiguration( cm );
        conf.addConfiguration( wprConf );

        conf.configure( grid2 );

        WhitePages wpClient = grid2.get( WhitePages.class );

        GridServiceDescription test1Gsd  = wpClient.create( "test1@domain1" );

        GridServiceDescription testGsd_2 = wpClient.lookup( "test1@domain1" );
        assertEquals( test1Gsd,
                      testGsd_2 );
        assertNotSame( test1Gsd,
                       testGsd_2 );

        WhitePages localWhitePages = grid1.get( WhitePages.class );
        GridServiceDescription testGsd_3 = localWhitePages.lookup( "test1@domain1" );

        assertEquals( test1Gsd,
                      testGsd_3 );
        assertNotSame( test1Gsd,
                      testGsd_3 );
        conn.close();
        grid1.get(MultiplexSocketService.class).close();
    }
    
    public void testWhitePagesLookupServices(){
        Map<String, GridServiceDescription> coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesWhitePages.class.getName() );

        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        GridImpl grid1 = new GridImpl( new ConcurrentHashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        GridPeerServiceConfiguration coreSeviceConf = new CoreServicesWhitePagesConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );
        
        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wplConf );
        
        //Create a Local Scheduler
        GridPeerServiceConfiguration schlConf = new SchedulerLocalConfiguration("myLocalSched1");
        conf.addConfiguration(schlConf);
        
        //Create a Local Scheduler
        GridPeerServiceConfiguration schlConf2 = new SchedulerLocalConfiguration("myLocalSched2");
        conf.addConfiguration(schlConf2);
    
        conf.configure( grid1 );
        
        
        WhitePages wplocal= grid1.get(WhitePages.class);
        Assert.assertNotNull(wplocal);
        GridServiceDescription schedulersgsd = wplocal.lookup(SchedulerService.class.getName());
    
        Assert.assertNotNull(schedulersgsd);
  
        
        
        
        
        
    }
    
    

}
