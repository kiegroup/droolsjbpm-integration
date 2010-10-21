package org.drools.io.mina;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Persistence;

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
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesRemoteConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesSocketConfiguration;

public class RemoteWhitePagesTest extends TestCase {

    public void test1() {
        
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

        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
        wplConf.setWhitePages( new JpaWhitePages( Persistence.createEntityManagerFactory( "org.drools.grid" ) ) );
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

        WhitePages wp = grid2.get( WhitePages.class );

        wp.create( "s1" );
        wp.create( "s2" );
        wp.create( "s3" );

        GridServiceDescription gs1 = wp.lookup( "s1" );

        gs1.addAddress( "p1" ).setObject( "v1" );
        gs1.addAddress( "p2" ).setObject( "v2" );

        gs1 = wp.lookup( "s1" );
        assertEquals( 2,
                      gs1.getAddresses().size() );
        assertEquals( "v1",
                      gs1.getAddresses().get( "p1" ).getObject() );
        assertEquals( "v2",
                      gs1.getAddresses().get( "p2" ).getObject() );

        gs1.removeAddress( "p2" );

        gs1 = wp.lookup( "s1" );
        assertEquals( 1,
                      gs1.getAddresses().size() );
        assertEquals( "v1",
                      gs1.getAddresses().get( "p1" ).getObject() );
        
        wp.remove( "s1" );
        
        assertNull( wp.lookup( "s1" ) );

        GridServiceDescription gs2 = wp.lookup( "s2" );
        assertNotNull( gs2 );
        
        conn.close();
        
        grid1.get( MultiplexSocketService.class ).close();
        
        
        
    }
}
