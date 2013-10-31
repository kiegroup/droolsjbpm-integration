package org.kie.services.remote.cdi;

import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.jboss.seam.transaction.TransactionInterceptor;
import org.jboss.seam.transaction.Transactional;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;

@Transactional
@Interceptors({TransactionInterceptor.class})
public class TransactionalExecutor {

    @Inject
    private javax.persistence.EntityManager em;

    /**
     * Executes a {@link KieSession} operation within a transaction. 
     * 
     * @param kieSession The {@link KieSession} that the operation is being executed on.
     * @param cmd The {@link Command} to be executed. 
     * @return The result of the {@link Command}, possibly null.
     */
    public Object execute(KieSession kieSession, Command<?> cmd) {
        return kieSession.execute(cmd);
    }

    /**
     * Executes a {@link TaskService} operation within a transaction. 
     * </p>
     * This method should only be used for {@link TaskCommand} classes that return objects that are <b>not</b>
     * (persistence) entity instances. If the {@link TaskCommand} returns a persistence entity, such as the {@link TaskImpl}
     * or {@link ContentImpl}, then use the {@link TransactionalExecutor#executeAndSerialize(InternalTaskService, TaskCommand)} 
     * method. 
     * 
     * @param taskService The {@link TaskService} to execute the operation on.
     * @param cmd The {@link TaskCommand} to be executed. 
     * @return The result of the {@link TaskCommand}, possibly null.
     */
    public Object execute(InternalTaskService taskService, TaskCommand<?> cmd) {
        em.joinTransaction();
        return ((InternalTaskService) taskService).execute(cmd);
    }
    
    /**
     * Executes a {@link TaskService} operation within a transaction. This method also tries to make sure the object returned
     * by the {@link TaskCommand} executed is also serialized within the transaction. 
     * </p>
     * This is done in order to avoid problems with proxy collection objects inserted into the entity instances (such as 
     * {@link TaskImpl}): if these collection objects are accessed outside of a transaction, errors will occur. 
     * </p>
     * In essence, we're tightly coupling to the {@link TaskService} here, because of problems with the {@link TaskService}. 
     * If those problems are ever fixed, this method should no longer be necessary. 
     * 
     * @param taskService The {@link TaskService} on which the operation is being executed. 
     * @param cmd The {@link TaskCommand} to be executed. 
     * @return The serialized result of the {@link TaskCommand}
     */
    public Object executeAndSerialize(InternalTaskService taskService, TaskCommand<?> cmd) {
        em.joinTransaction();
        Object cmdResult =  ((InternalTaskService) taskService).execute(cmd);
        if( cmdResult == null ) { 
           return null; 
        }
        if( cmdResult instanceof Task ) { 
            cmdResult = new JaxbTask((Task) cmdResult);
        } else if( cmdResult instanceof Content ) { 
            cmdResult = new JaxbContent((Content) cmdResult);
        }
        return cmdResult;
    }
    
}
