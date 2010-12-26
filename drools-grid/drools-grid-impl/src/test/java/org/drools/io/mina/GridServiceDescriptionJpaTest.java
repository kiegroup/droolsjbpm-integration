package org.drools.io.mina;

import java.net.InetSocketAddress;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.AddressImpl;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.service.directory.impl.WhitePagesImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GridServiceDescriptionJpaTest {
    @Test
    public void test1() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.grid" );
        WhitePages wp = new WhitePagesImpl();

        GridServiceDescription<WhitePages> gsd1 = wp.create( WhitePages.class.getName() );
        gsd1.setServiceInterface(  WhitePages.class );
        gsd1.addAddress( "socket" ).setObject( new InetSocketAddress( "127.0.0.1",
                                                                      5011 ) );
        gsd1.addAddress( "p1" ).setObject( "v1" );

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist( gsd1 );
        em.getTransaction().commit();
        em.close();

        GridServiceDescriptionImpl gsd2 = new GridServiceDescriptionImpl( "s1" );
        gsd2.setServiceInterface( WhitePages.class );
        gsd2.addAddress( "socket" ).setObject( new InetSocketAddress( "127.0.0.1",
                                                                      5012 ) );
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
                                             5011 ),
                      gsd1.getAddresses().get( "socket" ).getObject() );
        assertEquals( "v1",
                      gsd1.getAddresses().get( "p1" ).getObject() );

        GridServiceDescription<WhitePages> gsd2r = em.find( GridServiceDescriptionImpl.class,
                                                "s1" );
        assertNotNull( gsd2r );
        assertEquals( gsd2,
                      gsd2r );
        assertEquals( new InetSocketAddress( "127.0.0.1",
                                             5012 ),
                      gsd2.getAddresses().get( "socket" ).getObject() );
        assertEquals( "v2",
                      gsd2.getAddresses().get( "p2" ).getObject() );
    }
}
