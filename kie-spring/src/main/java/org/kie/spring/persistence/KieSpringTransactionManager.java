/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.kie.spring.persistence;

import java.util.HashMap;
import java.util.Map;

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class KieSpringTransactionManager
        implements
        TransactionManager {

    public static final String RESOURCE_CONTAINER = "org.kie.resources";

    private static final Logger logger = LoggerFactory.getLogger(KieSpringTransactionManager.class);
    private AbstractPlatformTransactionManager ptm;

    TransactionDefinition td = new DefaultTransactionDefinition();
    ThreadLocal<TransactionStatus> currentTransaction = new ThreadLocal<>();

    public KieSpringTransactionManager(AbstractPlatformTransactionManager ptm) {
        this.ptm = ptm;
    }

    public boolean begin() {
        if (currentTransaction.get() == null) {
            try {
                boolean activeTransaction = TransactionSynchronizationManager.isActualTransactionActive();
                logger.info("currentTransaction is null. Obtaining one");
                currentTransaction.set(this.ptm.getTransaction(td));
                return !activeTransaction || currentTransaction.get().isNewTransaction();
            } catch (Exception e) {
                logger.warn("Unable to begin transaction", e);
                throw new RuntimeException("Unable to begin transaction", e);
            }
        } else {
            logger.debug("current transaction is not null, reusing existing transaction");
            return false;
        }
    }

    public void commit(boolean transactionOwner) {
        if (!transactionOwner) {
            logger.debug("We are not the transaction owner, skipping commit");
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            logger.warn("transaction could not be commited as status is {}; check tx reaper timeout", getStatus());
            return;
        }

        if (currentTransaction.get() == null) {
            logger.warn("Out of order commit, no current transaction available");
            return;
        }
        try {
            this.ptm.commit(currentTransaction.get());
        } catch (Exception e) {
            logger.warn("Unable to commit transaction",
                    e);
            throw new RuntimeException("Unable to commit transaction",
                    e);
        } finally {
            cleanupTransaction();
        }
    }

    public void rollback(boolean transactionOwner) {
        if (!transactionOwner) {
            logger.debug("We are not the transaction owner, skipping commit");
            return;
        }
        if (currentTransaction.get() == null) {
            logger.warn("Out of order rollback, no current transaction available");
            return;
        }
        try {
            this.ptm.rollback(currentTransaction.get());
        } catch (Exception e) {
            logger.warn("Unable to rollback transaction", e);
            throw new RuntimeException("Unable to rollback transaction", e);
        } finally {
            cleanupTransaction();
        }
    }

    private void cleanupTransaction() {
        if (TransactionSynchronizationManager.hasResource(KieSpringTransactionManager.RESOURCE_CONTAINER)) {
            TransactionSynchronizationManager.unbindResource(KieSpringTransactionManager.RESOURCE_CONTAINER);
        }
        TransactionSynchronizationManager.clear();
        currentTransaction.remove();
        logger.debug("Transaction cleaned up");
    }

    /**
     * Borrowed from Seam
     */
    public int getStatus() {
        if (ptm == null) {
            return TransactionManager.STATUS_NO_TRANSACTION;
        }
        logger.debug("Current TX name (According to TransactionSynchronizationManager) : " + TransactionSynchronizationManager.getCurrentTransactionName());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionStatus transaction = null;
            try {
                if (currentTransaction.get() == null) {
                    logger.debug("Get Status. Current transaction is null");
                    transaction = ptm.getTransaction(td);
                    // If SynchronizationManager thinks it has an active transaction but
                    // our transaction is a new one
                    // then we must be in the middle of committing

                    if (transaction.isNewTransaction()) {
                        logger.debug("Get Status. Commited transation");
                        return TransactionManager.STATUS_COMMITTED;
                    }
                } else {
                    transaction = currentTransaction.get();
                }
                logger.debug("Current TX: " + transaction);
                if (transaction.isCompleted()) {
                    if (transaction.isRollbackOnly()) {
                        logger.debug("Get Status. Rolled back transation");
                        return TransactionManager.STATUS_ROLLEDBACK;
                    }
                    logger.debug("Get Status. Commited transaction");
                    return TransactionManager.STATUS_COMMITTED;
                } else {
                    logger.debug("Get Status. Active transaction");
                    return TransactionManager.STATUS_ACTIVE;
                }
            } finally {
                if (currentTransaction == null && transaction != null) {
                    ptm.commit(transaction);
                }
            }
        }
        return TransactionManager.STATUS_NO_TRANSACTION;
    }

    public void registerTransactionSynchronization(TransactionSynchronization ts) {
        TransactionSynchronizationManager.registerSynchronization(new SpringTransactionSynchronizationAdapter(ts));
    }

    @Override
    public void putResource(Object key, Object resource) {
        Map<Object, Object> resources = (Map<Object, Object>) TransactionSynchronizationManager.getResource(RESOURCE_CONTAINER);
        if (resources == null) {
            resources = new HashMap<Object, Object>();
            TransactionSynchronizationManager.bindResource(RESOURCE_CONTAINER, resources);
        }
        resources.put(key, resource);
    }

    @Override
    public Object getResource(Object key) {
        Map<Object, Object> resources = (Map<Object, Object>) TransactionSynchronizationManager.getResource(RESOURCE_CONTAINER);
        if (resources == null) {
            return null;
        }
        return resources.get(key);
    }
}
