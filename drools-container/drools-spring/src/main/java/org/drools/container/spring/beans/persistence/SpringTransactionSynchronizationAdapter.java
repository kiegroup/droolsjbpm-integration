package org.drools.container.spring.beans.persistence;

import org.drools.persistence.session.TransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;

public class SpringTransactionSynchronizationAdapter
    implements
    TransactionSynchronization {
    private org.drools.persistence.session.TransactionSynchronization ts;
    
    public SpringTransactionSynchronizationAdapter(org.drools.persistence.session.TransactionSynchronization ts) {
        this.ts = ts;
    }

    public void afterCommit() {
    }

    public void afterCompletion(int status) {
        switch ( status ) {
            case TransactionSynchronization.STATUS_COMMITTED : {
                this.ts.afterCompletion( TransactionManager.STATUS_COMMITTED );
                break;
            }
            case TransactionSynchronization.STATUS_ROLLED_BACK : {
                this.ts.afterCompletion( TransactionManager.STATUS_ROLLEDBACK );
                break;
            }
            default : {
                this.ts.afterCompletion( TransactionManager.STATUS_UNKNOWN );
            }
        }
    }

    public void beforeCommit(boolean readOnly) {
    }

    public void beforeCompletion() {
        this.ts.beforeCompletion();
    }

    public void resume() {
    }

    public void suspend() {
    }

}
