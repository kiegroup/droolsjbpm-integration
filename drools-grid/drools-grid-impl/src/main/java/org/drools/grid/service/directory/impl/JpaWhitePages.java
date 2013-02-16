package org.drools.grid.service.directory.impl;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaWhitePages
    implements
    WhitePages,
    MessageReceiverHandlerFactoryService {
    private EntityManagerFactory emf;

    private static Logger logger = LoggerFactory.getLogger( JpaWhitePages.class );

    public JpaWhitePages(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public GridServiceDescription create( String serviceDescriptionId, String ownerGridId ) {
        EntityManager em = null;
        GridServiceDescription gsd = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            gsd = new GridServiceDescriptionImpl( serviceDescriptionId, ownerGridId );
            em.persist( gsd );
            em.getTransaction().commit();
        } finally {
            if ( em != null && em.isOpen()) {
                em.close();
            }
        }
        return new GridServiceDescriptionJpa( gsd,
                                              emf );
    }

    public GridServiceDescription lookup( String serviceDescriptionId ) {
        GridServiceDescription gsd = null;
        EntityManager em = null;
        try {
            em = this.emf.createEntityManager();
            gsd = em.find( GridServiceDescriptionImpl.class,
                    serviceDescriptionId );
        } finally {
            if ( em.isOpen() ) {
                em.close();
            }
        }

        return ( gsd == null ) ? null : new GridServiceDescriptionJpa( gsd,
                                                                       emf );
    }

    public void remove( String serviceDescriptionId ) {
        EntityManager em = null;
        try {
            GridServiceDescription<WhitePages> gsd = null;
            if ( emf.isOpen() ) {
                em = emf.createEntityManager();
                if ( em.isOpen() ) {
                    em.getTransaction().begin();
                    gsd  = em.find( GridServiceDescriptionImpl.class,
                            serviceDescriptionId );
                    for ( Address address : gsd.getAddresses().values() ) { // because JPA won't cascade delete to orphans
                        if ( em.isOpen() ) {
                            em.remove( address );
                        }
                    }
                    em.remove( gsd );
                    em.getTransaction().commit();
                }
            }
        } catch ( PersistenceException pe ) {
            logger.warn( pe.getMessage() );
        } finally {
            if ( em != null && em.isOpen() ) {
                em.close();
            }
        }

    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new WhitePagesServer( this );
    }

    public List<GridServiceDescription> lookupServices(Class clazz) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void registerSocketService( Grid grid, String id, String ip, int port ) {
        WhitePagesImpl.doRegisterSocketService(grid, id, ip, port);
    }

    public void dispose() {
        if ( this.emf.isOpen() ) {
            this.emf.close();
        }
    }
}