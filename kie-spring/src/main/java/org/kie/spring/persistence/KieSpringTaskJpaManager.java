package org.kie.spring.persistence;

import javax.persistence.EntityManager;

import org.drools.persistence.PersistenceContext;
import org.drools.persistence.jpa.JpaPersistenceContext;
import org.jbpm.persistence.JpaProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContextManager;
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

        getApplicationScopedPersistenceContext(); // we create this on initialisation so that we own the EMF reference
        // otherwise Spring will close it after the transaction finishes
    }

    public PersistenceContext getApplicationScopedPersistenceContext() {

        return new JpaPersistenceContext(getApplicationScopedEntityManager(), isJTA);
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

            TransactionSynchronizationManager.unbindResource("cmdEM");
            if (this.env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER) != null) {
                getPersistenceContext().close();
            }
        }

        if (TransactionSynchronizationManager.hasResource(KieSpringTransactionManager.RESOURCE_CONTAINER)) {
            TransactionSynchronizationManager.unbindResource(KieSpringTransactionManager.RESOURCE_CONTAINER);
        }
    }

}
