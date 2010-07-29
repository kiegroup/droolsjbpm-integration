/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
