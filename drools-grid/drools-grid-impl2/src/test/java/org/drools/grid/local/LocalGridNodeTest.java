package org.drools.grid.local;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridNodeConnection;
import org.drools.grid.GridPeerConfiguration;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.GridNodeImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;

public class LocalGridNodeTest extends TestCase {

    public void test() {

    }

    public void testConnectWithId() {
        GridNodeConnection connection = new LocalGridConnection( "test-id" );
        GridNode gnode = connection.getGridNode();
        assertNotNull( gnode );
    }

    public void testConnectWithGivenGridNode() {
        GridNode gnode = new GridNodeImpl();
        GridNodeConnection connection = new LocalGridConnection( gnode );
        assertSame( gnode,
                    connection.getGridNode() );
    }

    public void testGetFactoryService() {
        GridNodeConnection connection = new LocalGridConnection( "test-id" );
        GridNode gnode = connection.getGridNode();
        KnowledgeBuilderFactoryService kbfService = gnode.get( KnowledgeBuilderFactoryService.class );
        assertNotNull( kbfService );
    }

    public void testSetObject() {
        GridNodeConnection connection = new LocalGridConnection( "test-id" );
        GridNode gnode = connection.getGridNode();

        KnowledgeBaseFactoryService kbfService = gnode.get( KnowledgeBaseFactoryService.class );
        KnowledgeBase kbase = kbfService.newKnowledgeBase();
        gnode.set( "id1",
                   kbase );
        assertSame( kbase,
                    gnode.get( "id1",
                               KnowledgeBase.class ) );
    }

    public void testNodeCreationAndWhitePagesRegistration() {
        Grid grid = new GridImpl( new HashMap<String, Object>() );

        GridPeerConfiguration conf = new GridPeerConfiguration();

        //Configuring the WhitePages 
        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
        conf.addConfiguration( wplConf );

        conf.configure( grid );

        GridServiceDescription gsd = new GridServiceDescriptionImpl( "test1@local" );
        gsd.addAddress( "local" );
        GridNode gnode = grid.createGridNode( gsd );

        WhitePages pages = grid.get( WhitePages.class );
        GridServiceDescription serviceDescription = pages.lookup( "test1@local" );

        GridNodeConnection connection = grid.getGridNodeConnection( serviceDescription );
        connection.connect();
        assertSame( gnode,
                    connection.getGridNode() );
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
