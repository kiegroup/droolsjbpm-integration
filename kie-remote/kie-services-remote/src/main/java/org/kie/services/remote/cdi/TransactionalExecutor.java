package org.kie.services.remote.cdi;

import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.jboss.seam.transaction.TransactionInterceptor;
import org.jboss.seam.transaction.Transactional;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.internal.task.api.InternalTaskService;

@Transactional
@Interceptors({TransactionInterceptor.class})
public class TransactionalExecutor {

    @Inject
    private javax.persistence.EntityManager em;

    public Object execute(KieSession kieSession, Command<?> cmd) {
        return kieSession.execute(cmd);
    }

    public Object execute(InternalTaskService taskService, TaskCommand<?> cmd) {
        em.joinTransaction();
        return ((InternalTaskService) taskService).execute(cmd);
    }
}
