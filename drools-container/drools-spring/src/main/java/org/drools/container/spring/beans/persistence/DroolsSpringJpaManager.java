package org.drools.container.spring.beans.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.persistence.session.JpaManager;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DroolsSpringJpaManager
    implements
    JpaManager {
    Environment                  env;

    private EntityManagerFactory emf;

    private EntityManager        appScopedEntityManager;
    
    private boolean              internalAppScopedEntityManager;
    
    public DroolsSpringJpaManager(Environment env) {
        this.env = env;
        this.emf = ( EntityManagerFactory ) env.get( EnvironmentName.ENTITY_MANAGER_FACTORY );
        
        getApplicationScopedEntityManager(); // we create this on initialisation so that we own the EMF reference
                                             // otherwise Spring will close it after the transaction finishes
    }

    public EntityManager getApplicationScopedEntityManager() {
        if ( this.appScopedEntityManager == null ) {
            // Use the App scoped EntityManager if the user has provided it, and it is open.
            this.appScopedEntityManager = (EntityManager) this.env.get( EnvironmentName.APP_SCOPED_ENTITY_MANAGER );
            if ( this.appScopedEntityManager != null && !this.appScopedEntityManager.isOpen() ) {
                throw new RuntimeException("Provided APP_SCOPED_ENTITY_MANAGER is not open");
            }
            
            if ( this.appScopedEntityManager == null ) {
                EntityManagerHolder emHolder = ( EntityManagerHolder ) TransactionSynchronizationManager.getResource( this.emf );
                if ( emHolder == null ) {
                    this.appScopedEntityManager = this.emf.createEntityManager();
                    emHolder =  new EntityManagerHolder( this.appScopedEntityManager );
                    TransactionSynchronizationManager.bindResource( emf, 
                                                                    emHolder );                    
                } else {
                    this.appScopedEntityManager = emHolder.getEntityManager();
                }
                

                this.env.set( EnvironmentName.APP_SCOPED_ENTITY_MANAGER,
                              emHolder.getEntityManager() );
                internalAppScopedEntityManager = true;
            }          
        }
        return this.appScopedEntityManager;
    }

    public EntityManager getCommandScopedEntityManager() {
        return getApplicationScopedEntityManager();
    }

    public void beginCommandScopedEntityManager() {
        this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER,
                      this.appScopedEntityManager );
    }

    public void endCommandScopedEntityManager() {
        this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, 
                      null );
    }

    public void dispose() {
        if ( internalAppScopedEntityManager ) {
            TransactionSynchronizationManager.unbindResource( this.emf );            
            if ( this.appScopedEntityManager != null && this.appScopedEntityManager.isOpen()  ) {
                this.appScopedEntityManager.close();
                this.internalAppScopedEntityManager = false;
                this.env.set( EnvironmentName.APP_SCOPED_ENTITY_MANAGER, null );
                this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, null );
            }
        }
    }

}
