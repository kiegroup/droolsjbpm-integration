package org.drools.grid.service.directory.impl;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;

public class JpaWhitePages
    implements
    WhitePages,
    MessageReceiverHandlerFactoryService {
    private EntityManagerFactory emf;

    public JpaWhitePages(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public GridServiceDescription create( String serviceDescriptionId, String ownerGridId ) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        GridServiceDescription gsd = new GridServiceDescriptionImpl( serviceDescriptionId, ownerGridId );
        em.persist( gsd );
        em.getTransaction().commit();
        em.close();
        return new GridServiceDescriptionJpa( gsd,
                                              emf );
    }

    public GridServiceDescription lookup( String serviceDescriptionId ) {
        EntityManager em = this.emf.createEntityManager();
        GridServiceDescription gsd = em.find( GridServiceDescriptionImpl.class,
                                                                          serviceDescriptionId );
        em.close();
        return ( gsd == null ) ? null : new GridServiceDescriptionJpa( gsd,
                                                                       emf );
    }

    public void remove( String serviceDescriptionId ) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        GridServiceDescription<WhitePages> gsd = em.find( GridServiceDescriptionImpl.class,
                                                          serviceDescriptionId );
        for ( Address address : gsd.getAddresses().values() ) { // because JPA won't cascade delete to orphans
            em.remove( address );
        }
        em.remove( gsd );
        em.getTransaction().commit();
        em.close();
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
        this.emf.close();
    }
}