package org.drools.grid.service.directory.impl;


import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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

    public GridServiceDescription create(String serviceDescriptionId) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        GridServiceDescription gsd = new GridServiceDescriptionImpl( serviceDescriptionId );
        em.persist( gsd );
        em.getTransaction().commit();
        em.close();
        return new GridServiceDescriptionJpa( gsd, emf );
    }

    public GridServiceDescription lookup(String serviceDescriptionId) {
        GridServiceDescription gsd = this.emf.createEntityManager().find( GridServiceDescriptionImpl.class,
                                                                          serviceDescriptionId );
        return ( gsd == null ) ? null : new GridServiceDescriptionJpa( gsd, emf );
    }

    public void remove(String serviceDescriptionId) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        GridServiceDescription gsd = em.find( GridServiceDescriptionImpl.class, serviceDescriptionId );
        for ( Address address :gsd.getAddresses().values() ) { // because JPA won't cascade delete to orphans
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //    public GridServiceDescription create(GridServiceDescription serviceDescription) {
    //        EntityManager em = emf.createEntityManager();
    //        em.getTransaction().begin();
    //        em.persist( serviceDescription );
    //        em.getTransaction().commit();
    //        em.close();
    //    }
    //
    //    public void remove(String id) {
    //        EntityManager em = emf.createEntityManager();
    //        em.getTransaction().begin();
    //        em.remove( new GridServiceDescriptionImpl( id ) );
    //        em.getTransaction().commit();
    //        em.close();
    //    }
    //
    //    public GridServiceDescription lookup(String id) {
    //        GridServiceDescription gsd = this.emf.createEntityManager().find( GridServiceDescriptionImpl.class, id );
    //        return gsd;
    //    }
    //
    //    public void addAddress(String id,
    //                           Address address) {
    //        EntityManager em = emf.createEntityManager();
    //        
    //        GridServiceDescriptionImpl gsd = em.find( GridServiceDescriptionImpl.class, id );
    //        em.getTransaction().begin();
    //        gsd.addAddress( address );
    //        em.getTransaction().commit();
    //        em.close();
    //    }
    //
    //    public void removeAddress(String id,
    //                              String protocol) {
    //        EntityManager em = emf.createEntityManager();
    //        
    //        GridServiceDescriptionImpl gsd = em.find( GridServiceDescriptionImpl.class, id );
    //        em.getTransaction().begin();
    //        Address address = gsd.getAddresses().get( protocol );
    //        gsd.removeAddress( protocol );
    //        em.remove( address ); //because jpa does not automatically remove orphans
    //        em.getTransaction().commit();
    //        em.close();
    //    }
    //
    //    public MessageReceiverHandler getMessageReceiverHandler() {
    //        return new WhitePagesServer( this );
    //    }

}