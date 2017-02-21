/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import javax.persistence.EntityManager;

import org.drools.persistence.api.PersistenceContext;
import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.jpa.JpaPersistenceContext;
import org.jbpm.persistence.JpaProcessPersistenceContext;
import org.jbpm.persistence.api.ProcessPersistenceContext;
import org.jbpm.persistence.api.ProcessPersistenceContextManager;
import org.jbpm.services.task.persistence.JPATaskPersistenceContext;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.TaskPersistenceContextManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class KieSpringTaskJpaManager extends AbstractKieSpringJpaManager
        implements
        TaskPersistenceContextManager {


    public KieSpringTaskJpaManager(Environment env) {
        super(env);
    }

    @Override
    public TaskPersistenceContext getPersistenceContext() {
        return new JPATaskPersistenceContext((EntityManager) this.env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER), isJTA);
    }

    @Override
    public void beginCommandScopedEntityManager() {
        EntityManager cmdScopedEntityManager = getCommandScopedEntityManager();

        if (isJTA) {
            this.getPersistenceContext().joinTransaction();
        }
    }

    public void endCommandScopedEntityManager() {
        if (TransactionSynchronizationManager.hasResource("cmdEM")) {
            // Code formerly in the clearPersistenceContext method.
            EntityManager cmdScopedEntityManager = (EntityManager) this.env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
            if (cmdScopedEntityManager != null) {
                cmdScopedEntityManager.clear();
            }


            if (this.env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER) != null) {
                getPersistenceContext().close();
            }
            TransactionSynchronizationManager.unbindResource("cmdEM");
        }

        if (TransactionSynchronizationManager.hasResource(KieSpringTransactionManager.RESOURCE_CONTAINER)) {
            TransactionSynchronizationManager.unbindResource(KieSpringTransactionManager.RESOURCE_CONTAINER);
        }
    }

}
