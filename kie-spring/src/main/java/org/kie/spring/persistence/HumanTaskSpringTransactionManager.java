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

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Stack;

public class HumanTaskSpringTransactionManager implements TransactionManager {

    Logger logger = LoggerFactory.getLogger(getClass());
    private AbstractPlatformTransactionManager ptm;

    TransactionDefinition td = new DefaultTransactionDefinition();
    Stack<TransactionStatus> currentTransaction = new Stack<TransactionStatus>();
    final boolean nestedTransactions;


    public HumanTaskSpringTransactionManager(AbstractPlatformTransactionManager ptm) {
        this.ptm = ptm;
        this.nestedTransactions = ptm.isNestedTransactionAllowed();
    }

    public boolean begin() {
        if (nestedTransactions || getStatus() != TransactionManager.STATUS_ACTIVE) {
            try {
                currentTransaction.push(this.ptm.getTransaction(td));
                return true;
            } catch (Exception e) {
                logger.warn("Unable to begin transaction", e);
                throw new RuntimeException("Unable to begin transaction", e);
            }
        }
        return false;
    }

    public void commit(boolean transactionOwner) {
        if (nestedTransactions || transactionOwner) {
            try {
                this.ptm.commit(currentTransaction.pop());
            } catch (Exception e) {
                logger.warn("Unable to commit transaction", e);
                throw new RuntimeException("Unable to commit transaction", e);
            }
        }
    }

    public void rollback(boolean transactionOwner) {
        if (nestedTransactions || transactionOwner) {
            try {
                this.ptm.rollback(currentTransaction.pop());
            } catch (Exception e) {
                logger.warn("Unable to rollback transaction",
                        e);
                throw new RuntimeException("Unable to rollback transaction",
                        e);
            }
        }
    }

    /**
     * Borrowed from Seam
     */
    public int getStatus() {
        if (ptm == null) {
            return TransactionManager.STATUS_NO_TRANSACTION;
        }

        // logger.debug( "Current TX name (According to TransactionSynchronizationManager) : " + TransactionSynchronizationManager.getCurrentTransactionName() );
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionStatus transaction = null;
            boolean commitNewTransaction = false;
            try {
                if (currentTransaction.isEmpty()) {
                    transaction = ptm.getTransaction(td);
                    currentTransaction.push(transaction);
                    commitNewTransaction = true;
                    if (transaction.isNewTransaction()) {
                        return TransactionManager.STATUS_COMMITTED;
                    }
                } else {
                    transaction = currentTransaction.peek();
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
                    if (transaction.isRollbackOnly()) {
                        return 5;
                    }
                    return TransactionManager.STATUS_ACTIVE;
                }
            } finally {
                if (commitNewTransaction) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getResource(Object key) {
        throw new UnsupportedOperationException();
    }
}
