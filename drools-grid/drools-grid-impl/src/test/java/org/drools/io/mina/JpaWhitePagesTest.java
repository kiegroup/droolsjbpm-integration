package org.drools.io.mina;

import java.sql.SQLException;
import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.SocketService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

public class JpaWhitePagesTest {

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
        System.out.println( "DB for white pages started! " );

       
    }

    @After
    public void tearDown() {
        
        server.stop();
        
    }
    @Test
    public void test1() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.grid" );
        WhitePages wp = new JpaWhitePages( emf );

        wp.create( "s1", "grid0" );
        wp.create( "s2", "grid0" );
        wp.create( "s3", "grid0" );

        GridServiceDescription<String> gs1 = wp.lookup( "s1" );

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

    }
}
