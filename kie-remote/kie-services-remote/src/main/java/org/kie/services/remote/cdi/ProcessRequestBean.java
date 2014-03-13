package org.kie.services.remote.cdi;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.jbpm.services.task.commands.GetContentCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.GetTaskContentCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.exception.IllegalTaskStateException;
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
import org.kie.services.remote.exception.DeploymentNotFoundException;
import org.kie.services.remote.exception.KieRemoteServicesRuntimeException;
import org.kie.services.remote.rest.RuntimeResource;
import org.kie.services.remote.rest.TaskResource;
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
public class ProcessRequestBean {

    /* KIE processing */
    @Inject
    private DeploymentInfoBean runtimeMgrMgr;

    @Inject
    private TaskService injectedTaskService;

    // Injection methods for tests
    
    public void setRuntimeMgrMgr(DeploymentInfoBean runtimeMgrMgr) {
        this.runtimeMgrMgr = runtimeMgrMgr;
    }

    public void setInjectedTaskService(TaskService taskService) {
        this.injectedTaskService = taskService;
    }

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
        if( deploymentId == null ) {
            throw new DeploymentNotFoundException("No deployment id supplied! Could not retrieve runtime to execute " + cmd.getClass().getSimpleName());
        }
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
     * Returns the actual variable instance from the runtime (as opposed to retrieving the string value of the
     * variable via the history/audit operations. 
     * 
     * @param deploymentId The id of the runtime
     * @param processInstanceId The process instance id (required)
     * @param varName The name of the variable
     * @return The variable object instance.
     */
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

    // task operations ------------------------------------------------------------------------------------------------------------
    
    /**
     * There are 3 possibilities here: <ol>
     * <li>This is an operation that should be done on a deployment if possible, but it's an independent task.</li>
     * <li>This is an operation that should be done on a deployment, and a deployment/runtime is available.</li>
     * <li>This is an operation that does <b>not</b> modify the {@link KieSession} and should be done via the injected {@link TaskService}.</li>
     * </ol>
     * 
     * @param taskId
     * @param deploymentId
     * @param processInstanceId
     * @param cmd
     * @param onDeployment
     * @param errorMsg
     * @return
     */
    public Object doTaskOperation(Long taskId, String deploymentId, Long processInstanceId, Task task, TaskCommand<?> cmd) { 
        boolean onDeployment = false;
        if( AcceptedCommands.TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.contains(cmd.getClass()) )  {
           onDeployment = true;
        }
      
        // take care of serialization
        if( cmd instanceof GetTaskCommand 
                || cmd instanceof GetContentCommand 
                || cmd instanceof GetTaskContentCommand ) { 
           cmd = new ExecuteAndSerializeCommand(cmd); 
        }
        
        if( ! onDeployment ) { 
            return ((InternalTaskService) injectedTaskService).execute(cmd);
        } else {
            if( task == null && deploymentId == null ) { 
                if( taskId == null ) { 
                    throw new KieRemoteServicesRuntimeException("A task id should be available at this point! Please contact the developers.");
                }
                if( task == null ) {
                    task = injectedTaskService.getTaskById(taskId);
                }
                if( task == null ) { 
                    throw new KieRemoteServicesRuntimeException("Task " + taskId + " does not exist!");
                }
                deploymentId = task.getTaskData().getDeploymentId();
                if( processInstanceId == null ) { 
                    processInstanceId = task.getTaskData().getProcessInstanceId();
                }
            }
            
            if( deploymentId == null ) { 
                // This is an independent task 
                return ((InternalTaskService) injectedTaskService).execute(cmd);
            } else { 
                RuntimeEngine runtimeEngine = null;
                try { 
                    runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, processInstanceId);
                    if( runtimeEngine == null ) { 
                        throw new DeploymentNotFoundException("Unable to find deployment '" + deploymentId + "' when executing " + cmd.getClass().getSimpleName());
                    }
                    TaskService runtimeTaskService = runtimeEngine.getTaskService();
                    return ((InternalTaskService) runtimeTaskService).execute(cmd);
                } finally { 
                    if( runtimeEngine != null ) { 
                        runtimeMgrMgr.disposeRuntimeEngine(runtimeEngine);
                    }
                }
            }
        }
    }


    public Object doRestTaskOperation(Long taskId, String deploymentId, Long processInstanceId, Task task, TaskCommand<?> cmd) {
        try { 
            return doTaskOperation(taskId, deploymentId, processInstanceId, task, cmd);
        } catch (PermissionDeniedException pde) {
            throw RestOperationException.conflict(pde.getMessage(), pde);
        } catch (IllegalTaskStateException itse) {
            throw RestOperationException.conflict(itse.getMessage(), itse);
        } catch (RuntimeException re) {
            throw re;
        }
    }

}
