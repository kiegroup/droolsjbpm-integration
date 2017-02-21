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
import org.springframework.transaction.support.TransactionSynchronization;

public class SpringTransactionSynchronizationAdapter
        implements
        TransactionSynchronization {
    private org.drools.persistence.api.TransactionSynchronization ts;

    public SpringTransactionSynchronizationAdapter(org.drools.persistence.api.TransactionSynchronization ts) {
        this.ts = ts;
    }

    public void afterCommit() {
    }

    public void afterCompletion(int status) {
        switch (status) {
            case TransactionSynchronization.STATUS_COMMITTED: {
                this.ts.afterCompletion(TransactionManager.STATUS_COMMITTED);
                break;
            }
            case TransactionSynchronization.STATUS_ROLLED_BACK: {
                this.ts.afterCompletion(TransactionManager.STATUS_ROLLEDBACK);
                break;
            }
            default: {
                this.ts.afterCompletion(TransactionManager.STATUS_UNKNOWN);
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

    public void flush() {
    }

}
