package org.drools.grid.local;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.ConnectionFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridNodeConnection;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.GridNodeImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;

public class LocalGridNodeTest {

    @Test
    public void testConnectWithId() {
        GridConnection<GridNode> connection = new LocalGridNodeConnection( "test-id" );
        GridNode gnode = connection.connect();
        assertNotNull( gnode );
    }

    @Test
    public void testConnectWithGivenGridNode() {
        GridNode gnode = new GridNodeImpl();
        GridConnection<GridNode> connection = new LocalGridNodeConnection( gnode );
        assertSame( gnode,
                    connection.connect() );
    }

    @Test
    public void testGetFactoryService() {
        GridConnection<GridNode> connection = new LocalGridNodeConnection( "test-id" );
        GridNode gnode = connection.connect();
        KnowledgeBuilderFactoryService kbfService = gnode.get( KnowledgeBuilderFactoryService.class );
        assertNotNull( kbfService );
    }

    @Test
    public void testSetObject() {
        GridConnection<GridNode> connection = new LocalGridNodeConnection( "test-id" );
        GridNode gnode = connection.connect();

        KnowledgeBaseFactoryService kbfService = gnode.get( KnowledgeBaseFactoryService.class );
        KnowledgeBase kbase = kbfService.newKnowledgeBase();
        gnode.set( "id1",
                   kbase );
        assertSame( kbase,
                    gnode.get( "id1",
                               KnowledgeBase.class ) );
    }

    @Test
    public void testNodeCreationAndWhitePagesRegistration() {
        Grid grid = new GridImpl( new HashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        //Configuring the WhitePages 
        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wplConf );

        conf.configure( grid );

        GridNode gnode = grid.createGridNode( "test1@local" );

        WhitePages pages = grid.get( WhitePages.class );
        GridServiceDescription<GridNode> gsd = pages.create( "test1@local" );
        
        GridServiceDescription<GridNode> serviceDescription = pages.lookup( "test1@local" );

        GridConnection connection = grid.get( ConnectionFactoryService.class ).createConnection( gsd );
        assertSame( gnode,
                    connection.connect() );
    }

    //    public void testWhitePagesAddRemoveAddresss() {
    //        Grid grid = new GridImpl(new HashMap<String,Object>());
    //
    //        GridNode gnode = grid.createGridNode( "test1@domain.com" );
    //        assertNotNull( gnode );
    //
    //        WhitePages pages = grid.get( WhitePages.class );
    //        GridServiceDescription serviceDescription = pages.lookup( "test1@domain.com" );
    //
    //        assertEquals( 0,
    //                      serviceDescription.getAddresses().size() );
    //
    //        Address address = new AddressImpl( "test1@domain.com",
    //                                           "socket",
    //                                           new InetSocketAddress( getLocalAddress(),
    //                                                                  9201 ) );
    //        pages.addAddress( "test1@domain.com",
    //                          address );
    //
    //        assertEquals( 1,
    //                      serviceDescription.getAddresses().size() );
    //        assertSame( address,
    //                    serviceDescription.getAddresses().get( "socket" ) );
    //
    //        pages.removeAddress( "test1@domain.com",
    //                             address );
    //        assertEquals( 0,
    //                      serviceDescription.getAddresses().size() );
    //
    //    }

    private InetAddress getLocalAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch ( UnknownHostException e ) {
            throw new RuntimeException( "Unable to lookup local address",
                                        e );
        }

    }

}
