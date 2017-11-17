/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.kie.spring.factorybeans.helper;

import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.kie.api.command.Command;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.spring.factorybeans.KSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

public class StatefulKSessionFactoryBeanHelper extends KSessionFactoryBeanHelper {

    protected KieSession kieSession;

    public StatefulKSessionFactoryBeanHelper(KSessionFactoryBean factoryBean, KieSession kieSession) {
        super(factoryBean);
        this.kieSession = kieSession;
    }

    @Override
    public void internalAfterPropertiesSet() throws Exception {
        JpaConfiguration jpaConfiguration = factoryBean.getJpaConfiguration();
        if (jpaConfiguration != null) {
            Environment env = EnvironmentFactory.newEnvironment();
            env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, jpaConfiguration.getEntityManagerFactory());
            env.set(EnvironmentName.TRANSACTION_MANAGER, jpaConfiguration.getPlatformTransactionManager());
            env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES,
                    new ObjectMarshallingStrategy[]{new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT)});


            if (jpaConfiguration.getId() >= 0) {
                kieSession = JPAKnowledgeService.loadStatefulKnowledgeSession(jpaConfiguration.getId(),
                        kieSession.getKieBase(),
                        factoryBean.getConf(),
                        env);
            } else {
                kieSession = JPAKnowledgeService.newStatefulKnowledgeSession(kieSession.getKieBase(),
                        factoryBean.getConf(),
                        env);
            }
        }
    }

    @Override
    public void executeBatch() {
        if (factoryBean.getBatch() != null && !factoryBean.getBatch().isEmpty()) {
            for (Command<?> cmd : factoryBean.getBatch()) {
                kieSession.execute(cmd);
            }
        }
    }

    public Object internalGetObject() {
        return kieSession;
    }

    @Override
    public Object internalNewObject() {
        if (kieBase != null) {
            return kieBase.newKieSession(factoryBean.getConf(), null);
        }
        return null;
    }

    public static class JpaConfiguration {
        private EntityManagerFactory emf;
        private PlatformTransactionManager tm;
        private int id = -1;

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

    }
}
