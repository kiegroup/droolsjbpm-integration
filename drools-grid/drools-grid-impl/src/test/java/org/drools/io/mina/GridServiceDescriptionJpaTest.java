package org.drools.io.mina;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.SocketService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.AddressImpl;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.service.directory.impl.WhitePagesImpl;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GridServiceDescriptionJpaTest {
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
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.grid" );
        WhitePages wp = new WhitePagesImpl();

        GridServiceDescription<WhitePages> gsd1 = wp.create( WhitePages.class.getName(), "grid0" );
        gsd1.setServiceInterface(  WhitePages.class );
        gsd1.addAddress( "socket" ).setObject( new InetSocketAddress( "127.0.0.1",
                                                                      8010 ) );
        gsd1.addAddress( "p1" ).setObject( "v1" );

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist( gsd1 );
        em.getTransaction().commit();
        em.close();

        GridServiceDescriptionImpl gsd2 = new GridServiceDescriptionImpl( "s1", "grid0" );
        gsd2.setServiceInterface( WhitePages.class );
        gsd2.addAddress( "socket" ).setObject( new InetSocketAddress( "127.0.0.1",
                                                                      8000 ) );
        gsd2.addAddress( "p2" ).setObject( "v2" );

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist( gsd2 );
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        GridServiceDescription<WhitePages> gsd1r = em.find( GridServiceDescriptionImpl.class,
                                                WhitePages.class.getName() );
        assertNotNull( gsd1r );
        assertEquals( gsd1,
                      gsd1r );
        assertEquals( new InetSocketAddress( "127.0.0.1",
                                             8010 ),
                      gsd1.getAddresses().get( "socket" ).getObject() );
        assertEquals( "v1",
                      gsd1.getAddresses().get( "p1" ).getObject() );

        GridServiceDescription<WhitePages> gsd2r = em.find( GridServiceDescriptionImpl.class,
                                                "s1" );
        assertNotNull( gsd2r );
        assertEquals( gsd2,
                      gsd2r );
        assertEquals( new InetSocketAddress( "127.0.0.1",
                                             8000 ),
                      gsd2.getAddresses().get( "socket" ).getObject() );
        assertEquals( "v2",
                      gsd2.getAddresses().get( "p2" ).getObject() );
    }
}
