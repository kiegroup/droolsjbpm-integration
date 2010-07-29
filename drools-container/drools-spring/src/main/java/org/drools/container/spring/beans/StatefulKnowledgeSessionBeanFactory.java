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

package org.drools.container.spring.beans;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.command.Command;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.persistence.processinstance.VariablePersistenceStrategyFactory;
import org.drools.persistence.processinstance.persisters.VariablePersister;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.springframework.transaction.PlatformTransactionManager;

public class StatefulKnowledgeSessionBeanFactory extends AbstractKnowledgeSessionBeanFactory {
    private StatefulKnowledgeSession ksession;

    private JpaConfiguration         jpaConfiguration;

    public Class<StatefulKnowledgeSession> getObjectType() {
        return StatefulKnowledgeSession.class;
    }

    public JpaConfiguration getJpaConfiguration() {
        return jpaConfiguration;
    }

    public void setJpaConfiguration(JpaConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }

    @Override
    protected CommandExecutor getCommandExecutor() {
        return ksession;
    }

    @Override
    protected void internalAfterPropertiesSet() {
        if ( getConf() != null && getWorkItems() != null && !getWorkItems().isEmpty() ) {
            Map<String, WorkItemHandler> map = ((SessionConfiguration) getConf()).getWorkItemHandlers();
            map.putAll( getWorkItems() );
        }
        
        if ( jpaConfiguration != null ) {
            if ( !jpaConfiguration.getVariablePersisters().isEmpty() ) {
                for ( Map.Entry<String, Class< ? extends VariablePersister>> entry : jpaConfiguration.getVariablePersisters().entrySet() ) {
                    VariablePersistenceStrategyFactory.getVariablePersistenceStrategy().setPersister( entry.getKey(),
                                                                                                      entry.getValue().getName() );
                }
            }
            
            Environment env = KnowledgeBaseFactory.newEnvironment();
            env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                     jpaConfiguration.getEntityManagerFactory() );
            env.set( EnvironmentName.TRANSACTION_MANAGER,
                     jpaConfiguration.getPlatformTransactionManager() );
            
            
            if ( jpaConfiguration.getId() >= 0 ) {
                ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( jpaConfiguration.getId(),
                                                                             getKbase(),
                                                                             getConf(),
                                                                             env );
            } else {
                ksession = JPAKnowledgeService.newStatefulKnowledgeSession( getKbase(),
                                                                            getConf(),
                                                                            env );
            }
        } else {
            ksession = getKbase().newStatefulKnowledgeSession( getConf(), null);
        }
        
        if ( getBatch() != null && !getBatch().isEmpty()) {
            for ( Command<?> cmd : getBatch() ) {
                ksession.execute( cmd );
            }
        }
    }

    public static class JpaConfiguration {
        private EntityManagerFactory       emf;

        private PlatformTransactionManager tm;

        private int                        id = -1;
        
        private Map<String, Class< ? extends VariablePersister>> variablePersisters;        

        public EntityManagerFactory getEntityManagerFactory() {
            return this.emf;
        }

        public void setEntityManagerFactory(EntityManagerFactory emf) {
            this.emf = emf;
        }

        public PlatformTransactionManager getPlatformTransactionManager() {
            return this.tm;
        }

        public void setPlatformTransactionManager(PlatformTransactionManager tm) {
            this.tm = tm;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
        
        public Map<String, Class< ? extends VariablePersister>> getVariablePersisters() {
            if ( variablePersisters == null ) {
                variablePersisters = new HashMap<String, Class< ? extends VariablePersister>>();
            }
            return variablePersisters;
        }

        public void setVariablePersisters(Map<String, Class< ? extends VariablePersister>> variablePersisters) {
            this.variablePersisters = variablePersisters;
        }        
    }
}
