package org.kie.services.remote.rest;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.persistence.SingleSessionCommandService;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.seam.transaction.DefaultTransaction;
import org.jboss.seam.transaction.SeamTransaction;
import org.jboss.solder.exception.control.ExceptionToCatch;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.remote.cdi.RuntimeManagerManager;
import org.kie.services.remote.exception.DomainNotFoundBadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by both the {@link RuntimeResource} and {@link TaskResource} to do the core operations on
 * the Deployment/Runtime's {@link KieSession} and {@link TaskService}.
 * </p>
 * It contains the necessary logic to do the following:
 * <ul>
 * <li>Retrieve the KieSession or TaskService</li>
 * <li>Execute the submitted command</li>
 * <li>Call commit on the given {@link SeamTransaction} instance</li>
 * </ul>
 * The commit on the {@link SeamTransaction} is necessary in order to avoid race-conditions
 * involving the application scoped {@link EntityManager} used in the {@link KieSession}.
 */
@RequestScoped
public class RestProcessRequestBean {

    private static final Logger logger = LoggerFactory.getLogger(RestProcessRequestBean.class);

    /* KIE processing */
    @Inject
    private RuntimeManagerManager runtimeMgrMgr;

    @Inject
    private TaskService taskService;

    /* Transaction control */
    @Inject
    @DefaultTransaction
    private SeamTransaction tx;

    @Inject
    Event<ExceptionToCatch> txExceptionEvent;

    /**
     * Executes a command on the {@link KieSession} from the proper {@link RuntimeManager}. This method
     * ends up synchronizing around the retrieved {@link KieSession} in order to avoid race-conditions.
     * 
     * @param cmd The command to be executed.
     * @param deploymentId The id of the runtime.
     * @param processInstanceId The process instance id, if available.
     * @param commit Whether or not to commit (the {@link SeamTransaction}) at after the {@link Command} has been completed.
     * @return The result of the {@link Command}.
     */
    public Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId, String errorMsg, 
            boolean commit, boolean restartTx) {
        Object result = null;
        try {
            RuntimeEngine runtimeEngine = getRuntimeEngine(deploymentId, processInstanceId);
            KieSession kieSession = runtimeEngine.getKieSession();
            SingleSessionCommandService sscs 
                = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) kieSession).getCommandService();
            synchronized (sscs) { 
                try {
                    result = kieSession.execute(cmd);
                } finally {
                    if (commit) {
                        commit(tx, txExceptionEvent, logger);
                    }
                    if(restartTx) {
                        start(tx, txExceptionEvent, logger);
                    }
                }
            }
        } catch (Exception e) {
            if( e instanceof RuntimeException ) { 
                throw (RuntimeException) e;
            } else {
                throw new InternalServerErrorException(errorMsg, e);
            }
        }
        return result;
    }

    /**
     * A variant of the above method which always calls commit once the {@link Command} has completed.
     * 
     * @param cmd The command to be executed.
     * @param deploymentId The id of the runtime.
     * @param processInstanceId The process id, if available.
     * @return The result of the executed command.
     */
    public Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId, String errorMsg) {
        return doKieSessionOperation(cmd, deploymentId, processInstanceId, errorMsg, true);
    }

    /**
     * A variant of the above method which commits but does not restart the {@link SeamTransaction} once the command has completed.
     * 
     * @param cmd The command to be executed.
     * @param deploymentId The id of the runtime.
     * @param processInstanceId The process id, if available.
     * @return The result of the executed command.
     */
    public Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId, String errorMsg, boolean commit) {
        return doKieSessionOperation(cmd, deploymentId, processInstanceId, errorMsg, commit, false);
    }
    
    /**
     * Executes a command on the injected {@link TaskService} instance.
     * </p>
     * Should be used only for commands that end up affecting the associated {@link KieSession}.
     * Since the {@link CompleteTaskCommand} ends up signalling the {@link KieSession}, we make sure to
     * first retrieve the proper {@link KieSession} in order to synchronize around it.
     * 
     * @param cmd The command to be executed.
     * @param errorMsg The message to be added to any (non-runtime) exceptions thrown. 
     * @param deploymentId The deployment id of the runtime. 
     * @return The result of the completed command.
     */
    public Object doTaskOperationOnDeployment(TaskCommand<?> cmd, String errorMsg, String deploymentId, boolean restartTx) {
        Object result = null;
        try {
            if( deploymentId != null ) { 
                RuntimeEngine runtimeEngine = getRuntimeEngine(deploymentId, null);
                KieSession kieSession = runtimeEngine.getKieSession();
                SingleSessionCommandService sscs 
                    = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) kieSession).getCommandService();
                synchronized (sscs) {
                    try {
                        ((InternalTaskService) taskService).execute(cmd);
                    } finally {
                        commit(tx, txExceptionEvent, logger);
                        if( restartTx ) { 
                            start(tx, txExceptionEvent, logger);
                        }
                    }
                }
            } else { 
                result = ((InternalTaskService) taskService).execute(cmd);
            }
        } catch (PermissionDeniedException pde) {
            throw new UnauthorizedException(pde.getMessage(), pde);
        } catch (RuntimeException re) {
            throw re;
        } catch( Exception e ) { 
            throw new InternalServerErrorException(errorMsg, e);
        } 
        return result;
    }

    /**
     * Variant of the above method which does not restart the transaction. 
     * @param cmd The {@link Command} to be executed. 
     * @param errorMsg The error message for any exception thrown. 
     * @param deploymentId The deployment id. 
     * @return The result of the given {@link Command}.
     */
    public Object doTaskOperationOnDeployment(TaskCommand<?> cmd, String errorMsg, String deploymentId) {
        return doTaskOperationOnDeployment(cmd, errorMsg, deploymentId, false);
    }
    
    /**
     * Executes a command on the {@link TaskService} (without synchronizing around the {@link KieSession})
     * @param cmd The command to be executed. 
     * @param errorMsg The error message to be attached to any exceptions thrown. 
     * @return The result of the completed command. 
     */
    public Object doTaskOperation(TaskCommand<?> cmd, String errorMsg) {
        return doTaskOperationOnDeployment(cmd, errorMsg, null);
    }

    /**
     * Retrieve the relevant {@link RuntimeEngine} instance.
     * 
     * @param deploymentId The id of the deployment for the {@link RuntimeEngine}.
     * @param processInstanceId The process instance id, if available.
     * @return The {@link RuntimeEngine} instance.
     */
    private RuntimeEngine getRuntimeEngine(String deploymentId, Long processInstanceId) {
        RuntimeManager runtimeManager = runtimeMgrMgr.getRuntimeManager(deploymentId);
        Context<?> runtimeContext;
        if (processInstanceId != null) {
            runtimeContext = new ProcessInstanceIdContext(processInstanceId);
        } else {
            runtimeContext = EmptyContext.get();
        }
        if (runtimeManager == null) {
            throw new DomainNotFoundBadRequestException("No runtime manager could be found for deployment '" + deploymentId + "'.");
        }
        return runtimeManager.getRuntimeEngine(runtimeContext);
    }

    /**
     * Commit the given {@link SeamTransaction}.
     * 
     * @param tx The {@link SeamTransaction} instance.
     * @param txExceptionEvent The CDI Event used in order to communicate with the seam-transaction framework.
     * @param logger In order to log thrown exceptions.
     */
    private static void commit(SeamTransaction tx, Event<ExceptionToCatch> txExceptionEvent, Logger logger) {
        try {
            switch (tx.getStatus()) {
            case javax.transaction.Status.STATUS_ACTIVE:
                tx.commit();
                break;
            case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
            case javax.transaction.Status.STATUS_PREPARED:
            case javax.transaction.Status.STATUS_PREPARING:
                tx.rollback();
                break;
            case javax.transaction.Status.STATUS_COMMITTED:
            case javax.transaction.Status.STATUS_COMMITTING:
            case javax.transaction.Status.STATUS_ROLLING_BACK:
            case javax.transaction.Status.STATUS_UNKNOWN:
            case javax.transaction.Status.STATUS_ROLLEDBACK:
            case javax.transaction.Status.STATUS_NO_TRANSACTION:
                break;
            }
        } catch (SystemException se) {
            logger.warn("Error commiting/rolling back the transaction", se);
            txExceptionEvent.fire(new ExceptionToCatch(se));
        } catch (HeuristicRollbackException hre) {
            logger.warn("Error committing the transaction", hre);
            txExceptionEvent.fire(new ExceptionToCatch(hre));
        } catch (RollbackException re) {
            logger.warn("Error committing the transaction", re);
            txExceptionEvent.fire(new ExceptionToCatch(re));
        } catch (HeuristicMixedException hme) {
            logger.warn("Error committing the transaction", hme);
            txExceptionEvent.fire(new ExceptionToCatch(hme));
        }
    }
    
    private static void start(SeamTransaction tx, Event<ExceptionToCatch> txExceptionEvent, Logger logger) {
        try {
            if (tx.getStatus() == Status.STATUS_ACTIVE) {
                logger.warn("Transaction was already started before the listener");
            } else {
                logger.debug("Beginning transaction");
                tx.begin();
            }
        } catch (SystemException se) {
            logger.warn("Error starting the transaction, or checking status", se);
            txExceptionEvent.fire(new ExceptionToCatch(se));
        } catch (NotSupportedException e) {
            logger.warn("Error starting the transaction", e);
            txExceptionEvent.fire(new ExceptionToCatch(e));
        }
    }
}
