/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.spring.jbpm.services;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.persistence.api.TransactionManager;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.kie.api.command.Command;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * Spring specialized implementation of TransactionalCommandService that allows to use SharedEntityManager and Spring TransactionManager
 */
public class SpringTransactionalCommandService extends TransactionalCommandService {

    private AbstractPlatformTransactionManager transactionManager;
    private EntityManager sharedEntityManager;

    private DefaultTransactionDefinition defTransDefinition = new DefaultTransactionDefinition();

    public SpringTransactionalCommandService(EntityManagerFactory emf, TransactionManager txm, AbstractPlatformTransactionManager transactionManager) {
        super(emf, txm);
        this.transactionManager = transactionManager;
    }

    public SpringTransactionalCommandService(EntityManagerFactory emf, TransactionManager txm, AbstractPlatformTransactionManager transactionManager, EntityManager sharedEntityManager) {
        super(emf, txm);
        this.transactionManager = transactionManager;
        this.sharedEntityManager = sharedEntityManager;
    }

    public SpringTransactionalCommandService(EntityManagerFactory emf, TransactionManager txm) {
        super(emf, txm);
    }

    public SpringTransactionalCommandService(EntityManagerFactory emf, AbstractPlatformTransactionManager transactionManager) {
        super(emf);
        this.transactionManager = transactionManager;
    }

    public SpringTransactionalCommandService(EntityManagerFactory emf, AbstractPlatformTransactionManager transactionManager, EntityManager sharedEntityManager) {
        super(emf);
        this.transactionManager = transactionManager;
        this.sharedEntityManager = sharedEntityManager;
    }


    @Override
    public <T> T execute(Command<T> command) {
        TransactionStatus status = transactionManager.getTransaction(defTransDefinition);
        try {
            T result = super.execute(command);
            transactionManager.commit(status);
            return result;
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }


    public AbstractPlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(AbstractPlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    protected EntityManager getEntityManager(Command<?> command) {
        if (sharedEntityManager != null) {
            return sharedEntityManager;
        }
        return super.getEntityManager(command);
    }
}
