package org.drools.io.mina;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.JpaWhitePages;

public class JpaWhitePagesTest extends TestCase {

    public void test1() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.grid" );
        WhitePages wp = new JpaWhitePages( emf );

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

    }
}
