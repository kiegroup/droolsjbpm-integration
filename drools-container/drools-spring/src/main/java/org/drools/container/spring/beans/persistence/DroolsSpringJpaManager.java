/*
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

import org.drools.persistence.PersistenceContext;
import org.drools.persistence.jpa.JpaPersistenceContext;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.jbpm.persistence.JpaProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DroolsSpringJpaManager
    implements
    ProcessPersistenceContextManager {

    Logger                       logger = LoggerFactory.getLogger( getClass() );

    Environment                  env;

    private EntityManagerFactory emf;

    private EntityManager        appScopedEntityManager;

    private boolean              internalAppScopedEntityManager;
    
    private boolean isJTA;

    public DroolsSpringJpaManager(Environment env) {
        this.env = env;
        this.emf = (EntityManagerFactory) env.get( EnvironmentName.ENTITY_MANAGER_FACTORY );
        
        isJTA = true;
        Boolean bool = (Boolean) env.get("IS_JTA_TRANSACTION");
        if (bool != null) {
        	isJTA = bool.booleanValue();
        }

        getApplicationScopedPersistenceContext(); // we create this on initialisation so that we own the EMF reference
                                                  // otherwise Spring will close it after the transaction finishes
    }

    public PersistenceContext getApplicationScopedPersistenceContext() {
        if ( this.appScopedEntityManager == null ) {
            // Use the App scoped EntityManager if the user has provided it, and it is open.
            this.appScopedEntityManager = (EntityManager) this.env.get( EnvironmentName.APP_SCOPED_ENTITY_MANAGER );
            if ( this.appScopedEntityManager != null && !this.appScopedEntityManager.isOpen() ) {
                throw new RuntimeException( "Provided APP_SCOPED_ENTITY_MANAGER is not open" );
            }

            if ( this.appScopedEntityManager == null ) {
                EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource( this.emf );
                if ( emHolder == null ) {
                    this.appScopedEntityManager = this.emf.createEntityManager();
                    emHolder = new EntityManagerHolder( this.appScopedEntityManager );
                    TransactionSynchronizationManager.bindResource( this.emf,
                                                                    emHolder );
                    internalAppScopedEntityManager = true;
                } else {
                    this.appScopedEntityManager = emHolder.getEntityManager();
                }

                this.env.set( EnvironmentName.APP_SCOPED_ENTITY_MANAGER,
                              emHolder.getEntityManager() );
            }
        }
        if ( TransactionSynchronizationManager.isActualTransactionActive() && isJTA) {
            this.appScopedEntityManager.joinTransaction();
        }
        return new JpaPersistenceContext( this.appScopedEntityManager, isJTA );
    }

    public PersistenceContext getCommandScopedPersistenceContext() {
        return  new JpaPersistenceContext( (EntityManager) this.env.get( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER ), isJTA );
    }

    public void beginCommandScopedEntityManager() {
        EntityManager cmdScopedEntityManager = (EntityManager) env.get( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER );
        if ( cmdScopedEntityManager == null || !cmdScopedEntityManager.isOpen() ) {
            EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource( "cmdEM" );
            EntityManager em = null;
            if ( emHolder == null ) {
                em = this.emf.createEntityManager();
                emHolder = new EntityManagerHolder( em );
                TransactionSynchronizationManager.bindResource( "cmdEM",
                                                                emHolder );
            } else {
                em = emHolder.getEntityManager();
            }
            this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER,
                          em );
        }

        if (isJTA) {
	        this.getCommandScopedPersistenceContext().joinTransaction();
	        this.appScopedEntityManager.joinTransaction();
        }
    }

    public void endCommandScopedEntityManager() {
        if ( TransactionSynchronizationManager.hasResource( "cmdEM" ) ) {
            TransactionSynchronizationManager.unbindResource( "cmdEM" );
            if ( this.env.get( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER ) != null ) {
                getCommandScopedPersistenceContext().close();
            }

        }
    }

    public void dispose() {
        logger.trace( "Disposing DroolsSpringJpaManager" );
        if ( internalAppScopedEntityManager ) {
            //TransactionSynchronizationManager.unbindResource( "appEM" );
            TransactionSynchronizationManager.unbindResource( this.emf );
            if ( this.appScopedEntityManager != null && this.appScopedEntityManager.isOpen() ) {
                this.appScopedEntityManager.close();
                this.internalAppScopedEntityManager = false;
                this.env.set( EnvironmentName.APP_SCOPED_ENTITY_MANAGER,
                              null );
                this.appScopedEntityManager = null;
            }
            this.endCommandScopedEntityManager();
        }
    }

    public ProcessPersistenceContext getProcessPersistenceContext() {
        return new JpaProcessPersistenceContext( appScopedEntityManager );
    }

}
