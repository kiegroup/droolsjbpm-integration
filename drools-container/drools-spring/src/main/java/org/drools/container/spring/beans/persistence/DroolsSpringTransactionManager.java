package org.drools.container.spring.beans.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.persistence.session.TransactionManager;
import org.drools.persistence.session.TransactionSynchronization;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DroolsSpringTransactionManager
    implements
    TransactionManager {

    private AbstractPlatformTransactionManager ptm;

    TransactionDefinition                      td                 = new DefaultTransactionDefinition();
    TransactionStatus                          currentTransaction = null;

    public DroolsSpringTransactionManager(AbstractPlatformTransactionManager ptm) {
        this.ptm = ptm;
    }
    
    private boolean localTransaction;

    public void begin() {
        if ( getStatus() == TransactionManager.STATUS_NO_TRANSACTION ) {
            // If there is no transaction then start one, we will commit within the same Command
            // it seems in spring calling getTransaction is enough to begin a new transaction
            currentTransaction = this.ptm.getTransaction( td );
            localTransaction = true;
        } else {
            localTransaction = false;   
        }
    }

    public void commit() {
        if ( this.localTransaction ) {
            // if we didn't begin this transaction, then do nothing
            this.localTransaction = false;
            this.ptm.commit( currentTransaction );
        }
    }

    public void rollback() {
        this.localTransaction = false;        
        this.ptm.rollback( currentTransaction );
    }

    /**
     * Borrowed from Seam
     * @author Michael Youngstrom
     */
    public int getStatus() {
        if ( ptm == null ) {
            return TransactionManager.STATUS_NO_TRANSACTION;
        }
        if ( TransactionSynchronizationManager.isActualTransactionActive() ) {
            TransactionStatus transaction = null;
            try {
                if ( currentTransaction == null ) {
                    transaction = ptm.getTransaction( td );
                    if ( transaction.isNewTransaction() ) {
                        return TransactionManager.STATUS_COMMITTED;
                    }
                } else {
                    transaction = currentTransaction;
                }
                // If SynchronizationManager thinks it has an active transaction but
                // our transaction is a new one
                // then we must be in the middle of committing
                if ( transaction.isCompleted() ) {
                    if ( transaction.isRollbackOnly() ) {
                        return TransactionManager.STATUS_ROLLEDBACK;
                    }
                    return TransactionManager.STATUS_COMMITTED;
                } else {
                    if ( transaction.isRollbackOnly() ) {
                        return TransactionManager.STATUS_ROLLEDBACK;
                    }
                    return TransactionManager.STATUS_ACTIVE;
                }
            } finally {
                if ( currentTransaction == null ) {
                    ptm.commit( transaction );
                }
            }
        }
        return TransactionManager.STATUS_NO_TRANSACTION;
    }

    public void registerTransactionSynchronization(TransactionSynchronization ts) {
        TransactionSynchronizationManager.registerSynchronization( new SpringTransactionSynchronizationAdapter( ts ) );
    }
}
