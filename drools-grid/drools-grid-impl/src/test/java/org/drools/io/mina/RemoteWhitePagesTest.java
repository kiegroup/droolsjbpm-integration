package org.drools.io.mina;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
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
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesRemoteConfiguration;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

public class RemoteWhitePagesTest {

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

    
    }

    @After
    public void tearDown() {
        
        server.stop();
        
    }
    
    @Test
    public void test1() {

        Map<String, GridServiceDescription> coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );

        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        GridImpl grid1 = new GridImpl("peer", new ConcurrentHashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        GridPeerServiceConfiguration coreSeviceConf = new CoreServicesLookupConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        MultiplexSocketServiceConfiguration socketConf = new MultiplexSocketServiceConfiguration( new MultiplexSocketServerImpl( "127.0.0.1",
                                                                                                                          new MinaAcceptorFactoryService(),
                                                                                                                          l,
                                                                                                                          grid1 ) );
        conf.addConfiguration( socketConf );

        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
        wplConf.setWhitePages( new JpaWhitePages( Persistence.createEntityManagerFactory( "org.drools.grid" ) ) );
        conf.addConfiguration( wplConf );

        socketConf.addService( WhitePages.class.getName(), wplConf.getWhitePages(), 8000 );

        conf.configure( grid1 );

        GridImpl grid2 = new GridImpl( new ConcurrentHashMap<String, Object>() );
        conf = new GridPeerConfiguration();

        //coreServicesMap = Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );
        coreSeviceConf = new CoreServicesLookupConfiguration( coreServicesMap );
        conf.addConfiguration( coreSeviceConf );

        GridPeerServiceConfiguration wprConf = new WhitePagesRemoteConfiguration( );
        conf.addConfiguration( wprConf );

        conf.configure( grid2 );

        WhitePages wp = grid2.get( WhitePages.class );

        wp.create( "sa1", "grid0" );
        wp.create( "sa2", "grid0" );
        wp.create( "sa3", "grid0" );

        GridServiceDescription<String> gs1 = wp.lookup( "sa1" );

        gs1.addAddress( "pa1" ).setObject( "v1" );
        gs1.addAddress( "pa2" ).setObject( "v2" );

        gs1 = wp.lookup( "sa1" );
        assertEquals( 2,
                      gs1.getAddresses().size() );
        assertEquals( "v1",
                      gs1.getAddresses().get( "pa1" ).getObject() );
        assertEquals( "v2",
                      gs1.getAddresses().get( "pa2" ).getObject() );

        gs1.removeAddress( "pa2" );

        gs1 = wp.lookup( "sa1" );
        assertEquals( 1,
                      gs1.getAddresses().size() );
        assertEquals( "v1",
                      gs1.getAddresses().get( "pa1" ).getObject() );

        wp.remove( "sa1" );

        assertNull( wp.lookup( "sa1" ) );

        GridServiceDescription gs2 = wp.lookup( "sa2" );
        assertNotNull( gs2 );
        grid1.get( SocketService.class ).close();

    }
}
