package org.kie.services.remote.war;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;

/**
 * Supplies various persistence related beans for dependencies (jbpm-kie-services, etc.) used in the war. 
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class TestPersistenceProducer {

    @PersistenceUnit(unitName="org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;
    
    private final IOService ioService = new IOServiceDotFileImpl();
    
    @Produces
    @Named("ioStrategy")
    public IOService ioService() {
        return ioService;
    }
   
    @Produces
    @RequestScoped
    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        return em;
    }
    
    public void closeEntityManager(@Disposes EntityManager em) {
        em.close();
    }
    
    @Produces
    public EntityManagerFactory createPersistenceUnit() { 
        return emf;
    }
    
}
