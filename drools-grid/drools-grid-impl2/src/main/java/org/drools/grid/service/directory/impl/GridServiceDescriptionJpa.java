package org.drools.grid.service.directory.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.Address;

public class GridServiceDescriptionJpa
    implements
    GridServiceDescription, Serializable {
    private GridServiceDescription detached;
    
    private EntityManagerFactory   emf;

    public GridServiceDescriptionJpa(GridServiceDescription detached,
                                     EntityManagerFactory emf) {
        this.detached = detached;
        this.emf = emf;
    }

    public Map<String, Address> getAddresses() {
        EntityManager em = this.emf.createEntityManager();
        Map<String, Address> addresses = new HashMap<String, Address>();
        for ( Address address : this.detached.getAddresses().values() ) {
            addresses.put( address.getTransport(), new AddressJpa( address,
                                                                   this.emf ) );
        }
        em.close();
        return Collections.unmodifiableMap( addresses );
    }

    public Address addAddress(String transport) {
        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        this.detached = em.find( GridServiceDescriptionImpl.class, this.detached.getId() );
        Address address = this.detached.addAddress( transport );
        em.getTransaction().commit();
        em.close();
        return new AddressJpa( address, this.emf );
    }

    public void removeAddress(String transport) {
        EntityManager em = this.emf.createEntityManager();

        em.getTransaction().begin();        
        this.detached = em.find( GridServiceDescriptionImpl.class, this.detached.getId() );        
        Address address = this.detached.getAddresses().get( transport );
        this.detached.removeAddress( transport );
        em.remove( address ); //because jpa does not automatically remove orphans
        em.getTransaction().commit();
        em.close();
    }

    public String getId() {
        return this.detached.getId();
    }

    public Class getImplementedClass() {
        return this.detached.getImplementedClass();
    }

    public void setImplementedClass(Class cls) {
        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        this.detached = em.find( GridServiceDescriptionImpl.class, this.detached.getId() );
        this.detached.setImplementedClass( cls );
        em.getTransaction().commit();
        em.close();
    }

    private Object readResolve() throws ObjectStreamException {
        return this.detached;
    }

     public Serializable getData() {
        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        this.detached = em.find( GridServiceDescriptionImpl.class, this.detached.getId() );
        Serializable data = this.detached.getData();
        em.getTransaction().commit();
        em.close();
        return data;
    }

    
   @Override
    public boolean equals(Object obj) {
        //@TODO: improve equals comparision
        final GridServiceDescription other = (GridServiceDescription) obj;
        if (!this.getId().equals(other.getId() )) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.detached != null ? this.detached.hashCode() : 0);
        return hash;
    }

    public void setData(Serializable data) {
        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        this.detached = em.find( GridServiceDescriptionImpl.class, this.detached.getId() );
        this.detached.setData(data);
        em.getTransaction().commit();
        em.close();
    }

   
  
     
}
