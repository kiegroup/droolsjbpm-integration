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

    Logger logger = LoggerFactory.getLogger(getClass());
    private AbstractPlatformTransactionManager ptm;

    TransactionDefinition td = new DefaultTransactionDefinition();
    ThreadLocal<TransactionStatus> currentTransaction = new ThreadLocal<>();

    public KieSpringTransactionManager(AbstractPlatformTransactionManager ptm) {
        this.ptm = ptm;
    }

    public boolean begin() {
        try {
            // RHBPMS-4621 - transaction can be marked as rollback
            // and still be associated with current thread
            // See WFLY-4327
            if (getStatus() == TransactionManager.STATUS_ROLLEDBACK) {
                logger.warn("Cleaning up rolledback transaction");
                rollback(true);
            }
            if (getStatus() == TransactionManager.STATUS_NO_TRANSACTION) {
                // If there is no transaction then start one, we will commit within the same Command
                // it seems in spring calling getTransaction is enough to begin a new transaction
                currentTransaction.set(this.ptm.getTransaction(td));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.warn("Unable to begin transaction",
                    e);
            throw new RuntimeException("Unable to begin transaction",
                    e);
        }
    }

    public void commit(boolean transactionOwner) {
        if (!transactionOwner) {
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            logger.warn("transaction could not be commited as status is {}; check tx reaper timeout", getStatus());
            return;
        }

        try {
            // if we didn't begin this transaction, then do nothing
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
        if (transactionOwner) {
            try {
                this.ptm.rollback(currentTransaction.get());
            } catch (Exception e) {
                logger.warn("Unable to rollback transaction", e);
                throw new RuntimeException("Unable to rollback transaction", e);
            } finally {
                cleanupTransaction();
            }
        }
    }

    private void cleanupTransaction() {
        if (TransactionSynchronizationManager.hasResource(KieSpringTransactionManager.RESOURCE_CONTAINER)) {
            TransactionSynchronizationManager.unbindResource(KieSpringTransactionManager.RESOURCE_CONTAINER);
        }
        TransactionSynchronizationManager.clear();
        currentTransaction.remove();
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
                    transaction = ptm.getTransaction(td);
                    if (transaction.isNewTransaction()) {
                        return TransactionManager.STATUS_COMMITTED;
                    }
                } else {
                    transaction = currentTransaction.get();
                }
                logger.debug("Current TX: " + transaction);
                // If SynchronizationManager thinks it has an active transaction but
                // our transaction is a new one
                // then we must be in the middle of committing
                if (transaction.isCompleted()) {
                    if (transaction.isRollbackOnly()) {
                        return TransactionManager.STATUS_ROLLEDBACK;
                    }
                    return TransactionManager.STATUS_COMMITTED;
                } else {
                    // Using the commented-out code in means that if rollback with this manager,
                    //  I always have to catch and check the exception 
                    //  because ROLLEDBACK can mean both "rolled back" and "rollback only".
                    // if ( transaction.isRollbackOnly() ) {
                    //     return TransactionManager.STATUS_ROLLEDBACK;
                    // }

                    return TransactionManager.STATUS_ACTIVE;
                }
            } finally {
                if (currentTransaction.get() == null && transaction != null) {
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
