package org.kie.services.remote.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.kie.services.remote.rest.exception.RestOperationException;
import org.kie.services.remote.util.ExecuteAndSerializeCommand;

/**
 * This class is used by both the {@link RuntimeResource} and {@link TaskResource} to do the core operations on
 * the Deployment/Runtime's {@link KieSession} and {@link TaskService}.
 * </p>
 * It contains the necessary logic to do the following:
 * <ul>
 * <li>Retrieve the KieSession or TaskService</li>
 * <li>Execute the submitted command</li>
 * </ul>
 */
@RequestScoped
public class RestProcessRequestBean {

    /* KIE processing */
    @Inject
    private DeploymentInfoBean runtimeMgrMgr;

    @Inject
    private TaskService taskService;

    /**
     * Executes a command on the {@link KieSession} from the proper {@link RuntimeManager}. This method
     * ends up synchronizing around the retrieved {@link KieSession} in order to avoid race-conditions.
     * 
     * @param cmd The command to be executed.
     * @param deploymentId The id of the runtime.
     * @param processInstanceId The process instance id, if available.
     * @return The result of the {@link Command}.
     */
    public Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId, String errorMsg) {
        Object result = null;
        RuntimeEngine runtimeEngine = null;
        try {
            runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, processInstanceId);
            KieSession kieSession = runtimeEngine.getKieSession();
            result = kieSession.execute(cmd);
        } catch (RuntimeException re) {
            throw re;
        } finally {
            runtimeMgrMgr.disposeRuntimeEngine(runtimeEngine);
        }
        return result;
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
    public Object doTaskOperationOnDeployment(TaskCommand<?> cmd, String deploymentId, Long processInstanceId, String errorMsg) {
        Object result = null;
        RuntimeEngine engine = null;
        try {
            if( deploymentId != null ) { 
                RuntimeEngine runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, processInstanceId);
                result = ((InternalTaskService) runtimeEngine.getTaskService()).execute(cmd);
            } else {
                engine = runtimeMgrMgr.getRuntimeEngineForTaskCommand(
                        cmd, 
                        taskService,
                        AcceptedCommands.TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.contains(cmd.getClass()));
                if (engine != null) {
                    result = ((InternalTaskService) engine.getTaskService()).execute(cmd);
                } else {
                    result = ((InternalTaskService) taskService).execute(cmd);
                }
            }
        } catch (PermissionDeniedException pde) {
            throw RestOperationException.conflict(pde.getMessage(), pde);
        } catch (RuntimeException re) {
            throw re;
        } finally {
            runtimeMgrMgr.disposeRuntimeEngine(engine);
        }
        return result;
    }
    
    /**
     * Executes a command on the {@link TaskService} (without synchronizing around the {@link KieSession})
     * @param cmd The command to be executed. 
     * @param errorMsg The error message to be attached to any exceptions thrown. 
     * @return The result of the completed command. 
     */
    public Object doTaskOperationOnDeployment(TaskCommand<?> cmd, String errorMsg) {
        return doTaskOperationOnDeployment(cmd, null, null, errorMsg);
    }


    /**
     * Intended for <i>read-only</i> {@link TaskCommand}s on the {@link TaskService}.<ul>
     * <li>This should be used when the {@link TaskCommand}
     * returns an object instance that is also a (persistent) entity, and thus should also be serialized within a
     * transaction.</li>
     * </ul>
     * @param cmd The {@link TaskCommand} to be executed. 
     * @param errorMsg The error message that should be associated with any eventual errors or exceptions. 
     * @return The result of the {@link TaskCommand}, possibly null.
     */
    public Object doNonDeploymentTaskOperationAndSerializeResult(TaskCommand<?> cmd, String errorMsg) {
        return doNonDeploymentTaskOperation(new ExecuteAndSerializeCommand(cmd), errorMsg);
    }
   
    /**
     * Intended for {@link TaskCommand}'s which do <i>not</i> modify {@link Task}s. This should be used: 
     * <ul>
     * <li>when retrieving an object instance</li>
     * <li>when deleting task history entities</li>
     * </ul> 
     * @param cmd The {@link TaskCommand} to be executed. 
     * @param errorMsg The error message that should be associated with any eventual errors or exceptions. 
     * @return The result of the {@link TaskCommand}, possibly null.
     */
    public Object doNonDeploymentTaskOperation(TaskCommand<?> cmd, String errorMsg) {
        Object result = null;
        try {
            result = ((InternalTaskService) taskService).execute(cmd);
        } catch (PermissionDeniedException pde) {
            throw RestOperationException.conflict(pde.getMessage(), pde);
        } catch (RuntimeException re) {
            throw re;
        }
        return result;
    }
    
    public Object getVariableObjectInstanceFromRuntime(String deploymentId, long processInstanceId, String varName) { 
        String errorMsg = "Unable to retrieve variable '" + varName + "' from process instance " + processInstanceId;
        Object procVar = null;
        RuntimeEngine runtimeEngine = null;
        try {
            runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, processInstanceId);
            KieSession kieSession = runtimeEngine.getKieSession();
            ProcessInstance procInst = kieSession.getProcessInstance(processInstanceId);
            if( procInst == null ) { 
                throw RestOperationException.notFound("Process instance " + processInstanceId + " could not be found!");
            }
            procVar = ((WorkflowProcessInstanceImpl) procInst).getVariable(varName);
            if( procVar == null ) { 
                throw RestOperationException.notFound("Variable " + varName + " does not exist in process instance " + processInstanceId + "!");
            }
        } catch (RuntimeException re) {
            throw RestOperationException.internalServerError(errorMsg, re);
        } finally {
            runtimeMgrMgr.disposeRuntimeEngine(runtimeEngine);
        }
        return procVar;
    }
}
