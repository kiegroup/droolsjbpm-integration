package org.drools.core.io.mina;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        wp.create( "ss1", "grid0" );
        wp.create( "ss2", "grid0" );
        wp.create( "ss3", "grid0" );

        GridServiceDescription<String> gs1 = wp.lookup( "ss1" );

        gs1.addAddress( "pp1" ).setObject( "v1" );
        gs1.addAddress( "pp2" ).setObject( "v2" );

        gs1 = wp.lookup( "ss1" );
        assertEquals( 2,
                      gs1.getAddresses().size() );
        assertEquals( "v1",
                      gs1.getAddresses().get( "pp1" ).getObject() );
        assertEquals( "v2",
                      gs1.getAddresses().get( "pp2" ).getObject() );

        gs1.removeAddress( "pp2" );

        gs1 = wp.lookup( "ss1" );
        assertEquals( 1,
                      gs1.getAddresses().size() );
        assertEquals( "v1",
                      gs1.getAddresses().get( "pp1" ).getObject() );

        wp.remove( "ss1" );

        assertNull( wp.lookup( "ss1" ) );

        GridServiceDescription gs2 = wp.lookup( "ss2" );
        assertNotNull( gs2 );

    }
}
