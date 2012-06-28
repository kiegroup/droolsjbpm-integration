package org.drools.grid.service.directory.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.Address;

public class AddressJpa
    implements
    Address,
    Serializable {
    private Address                        detached;

    transient private EntityManagerFactory emf;

    public AddressJpa(Address detached,
                      EntityManagerFactory emf) {
        this.detached = detached;
        this.emf = emf;
    }

    public GridServiceDescription getGridServiceDescription() {
        return this.detached.getGridServiceDescription();
    }

    public Object getObject() {
        return this.detached.getObject();
    }

    public String getTransport() {
        return this.detached.getTransport();
    }

    public void setObject(Object object) {
        EntityManager em = this.emf.createEntityManager();
        em.getTransaction().begin();
        this.detached = em.find( AddressImpl.class,
                                 ((AddressImpl) this.detached).getId() );
        this.detached.setObject( object );
        em.getTransaction().commit();
        em.close();
    }

    private Object readResolve() throws ObjectStreamException {
        return this.detached;
    }

    public String toString() {
        return "AddressJpa{" +
                "detached=" + detached +
                '}';
    }
}
