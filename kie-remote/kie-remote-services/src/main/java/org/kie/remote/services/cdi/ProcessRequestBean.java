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

package org.kie.remote.services.cdi;

import static org.kie.remote.services.cdi.DeploymentInfoBean.emptyDeploymentId;
import static org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus.FAILURE;
import static org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus.PERMISSIONS_CONFLICT;
import static org.kie.services.shared.ServicesVersion.VERSION;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartCorrelatedProcessCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.command.AuditCommand;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.commands.AddTaskCommand;
import org.jbpm.services.task.commands.CancelDeadlineCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.GetContentByIdCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.GetTaskContentCommand;
import org.jbpm.services.task.commands.ProcessSubTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.internal.KieInternalServices;
import org.kie.internal.command.CorrelationKeyCommand;
import org.kie.internal.command.ProcessInstanceIdCommand;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.manager.context.CorrelationKeyContext;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.remote.services.AcceptedServerCommands;
import org.kie.remote.services.exception.DeploymentNotFoundException;
import org.kie.remote.services.exception.KieRemoteServicesDeploymentException;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.RuntimeResourceImpl;
import org.kie.remote.services.rest.TaskResourceImpl;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.RemoteServicesQueryJPAService;
import org.kie.remote.services.util.ExecuteAndSerializeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * This class is used by both the {@link RuntimeResourceImpl} and {@link TaskResourceImpl} to do the core operations on
 * the Deployment/Runtime's {@link KieSession} and {@link TaskService}.
 * </p>
 * It contains the necessary logic to do the following:
 * <ul>
 * <li>Retrieve the KieSession or TaskService</li>
 * <li>Execute the submitted command</li>
 * </ul>
 */
@ApplicationScoped
public class ProcessRequestBean {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRequestBean.class);

    /* KIE processing */

    @Inject
    private ProcessService processService;

    @Inject
    private UserTaskService userTaskService;

    /** AuditLogService **/
    private static final String PERSISTENCE_UNIT_NAME = "org.jbpm.domain";

    @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
    private EntityManagerFactory emf;

    private AuditLogService auditLogService;

    private RemoteServicesQueryJPAService queryJpaService;

    // Injection methods for tests

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

    public void setUserTaskService(UserTaskService userTaskService) {
        this.userTaskService = userTaskService;
    }

    public void setAuditLogService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void setJPAService(RemoteServicesQueryJPAService jpaService) {
        this.queryJpaService = jpaService;
    }

    // Audit Log Service logic

    @PostConstruct
    public void initAuditLogService() {
        auditLogService = new JPAAuditLogService(emf);
        queryJpaService = new RemoteServicesQueryJPAService(emf);
        if( emf == null ) {
            ((JPAAuditLogService) auditLogService).setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
            queryJpaService.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
        }
    }

    public AuditLogService getAuditLogService() {
        return auditLogService;
    }

    public RemoteServicesQueryJPAService getJPAService() {
        return queryJpaService;
    }

    // Methods used

    public <T> void processCommand(Command<T> cmd, JaxbCommandsRequest request, int i, JaxbCommandsResponse jaxbResponse) {
        String version = request.getVersion();
        if( version == null ) {
            version = "pre-6.0.3";
        }
        if( ! version.equals(VERSION) ) {
            logger.warn( "Request received from client version [{}] while server is version [{}]! THIS MAY CAUSE PROBLEMS!", version, VERSION);
        }
        jaxbResponse.setVersion(VERSION);

        String cmdName = cmd.getClass().getSimpleName();
        logger.debug("Processing command " + cmdName);
        String errMsg = "Unable to execute " + cmdName + "/" + i;

        Object cmdResult = null;
        try {
            // check that all parameters have been correctly deserialized/unmarshalled
            preprocessCommand(cmd);

            String corrKeyString = request.getCorrelationKeyString();
            List<String> correlationKeyProps = null;
            if( corrKeyString != null ) {
                String [] correlationKeyPropsArr = corrKeyString.split(":");
                if( correlationKeyPropsArr.length > 0 ) {
                    correlationKeyProps = Arrays.asList(correlationKeyPropsArr);
                }
            }

            if( cmd instanceof TaskCommand<?> ) {
                TaskCommand<?> taskCmd = (TaskCommand<?>) cmd;
                // TODO: correlation key sessions and task completion! BZ-1245616-related
                cmdResult = doTaskOperation(
                        taskCmd.getTaskId(),
                        request.getDeploymentId(),
                        request.getProcessInstanceId(),
                        null,
                        taskCmd);
            } else if( cmd instanceof AuditCommand<?>) {
                AuditCommand<?> auditCmd = ((AuditCommand<?>) cmd);
                auditCmd.setAuditLogService(getAuditLogService());
                cmdResult = auditCmd.execute(null);
            } else {
                cmdResult = doKieSessionOperation(
                        cmd,
                        request.getDeploymentId(),
                        correlationKeyProps,
                        request.getProcessInstanceId());
            }
        } catch (PermissionDeniedException pde) {
            logger.warn(errMsg, pde);
            jaxbResponse.addException(pde, i, cmd, PERMISSIONS_CONFLICT);
        } catch (Exception e) {
            logger.warn(errMsg, e);
            jaxbResponse.addException(e, i, cmd, FAILURE);
        }
        if (cmdResult != null) {
            try {
                // addResult could possibly throw an exception, which is why it's here and not above
                jaxbResponse.addResult(cmdResult, i, cmd);
            } catch (Exception e) {
                errMsg = "Unable to add result from " + cmdName + "/" + i;
                logger.error(errMsg, e);
                jaxbResponse.addException(e, i, cmd, FAILURE);
            }
        }
    };

    void preprocessCommand(Command cmd) {
       if( AcceptedServerCommands.SEND_OBJECT_PARAMETER_COMMANDS.contains(cmd.getClass()) ) {
           if( cmd instanceof CompleteWorkItemCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((CompleteWorkItemCommand) cmd).getResults());
           } else if( cmd instanceof SignalEventCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((SignalEventCommand) cmd).getEvent());
           } else if( cmd instanceof StartCorrelatedProcessCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((StartCorrelatedProcessCommand) cmd).getData());
               checkThatUserDefinedClassesWereUnmarshalled(((StartCorrelatedProcessCommand) cmd).getParameters());
           } else if( cmd instanceof StartProcessCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((StartProcessCommand) cmd).getData());
               checkThatUserDefinedClassesWereUnmarshalled(((StartProcessCommand) cmd).getParameters());
           } else if( cmd instanceof SetGlobalCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((SetGlobalCommand) cmd).getObject());
           } else if( cmd instanceof InsertObjectCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((InsertObjectCommand) cmd).getObject());
           } else if( cmd instanceof UpdateCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((UpdateCommand) cmd).getObject());
           } else if( cmd instanceof AddTaskCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((AddTaskCommand) cmd).getParams());
           } else if( cmd instanceof CompleteTaskCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((CompleteTaskCommand) cmd).getData());
           } else if( cmd instanceof FailTaskCommand ) {
               checkThatUserDefinedClassesWereUnmarshalled(((FailTaskCommand) cmd).getData());
           }
       }
    }

    void checkThatUserDefinedClassesWereUnmarshalled(Object obj) {
       if( obj != null ) {
          if( obj instanceof List ) {
             for( Object listElem : (List) obj ) {
                 verifyObjectHasBeenUnmarshalled(listElem);
             }
          } else if( obj instanceof Map ) {
              for( Object mapVal : ((Map) obj).values() ) {
                 verifyObjectHasBeenUnmarshalled(mapVal);
              }
          } else {
              verifyObjectHasBeenUnmarshalled(obj);
          }
       }
    }

    private void verifyObjectHasBeenUnmarshalled(Object obj) {
        if( Element.class.isAssignableFrom(obj.getClass()) ) {
            String typeName = ((Element) obj).getAttribute("xsi:type");
            throw new KieRemoteServicesDeploymentException("Could not unmarshall user-defined class instance parameter of type '" + typeName + "'");
        }
    }

    /**
     * Executes a command on the {@link KieSession} from the proper {@link RuntimeManager}. This method
     * ends up synchronizing around the retrieved {@link KieSession} in order to avoid race-conditions.
     *
     * @param cmd The command to be executed.
     * @param deploymentId The id of the runtim
     * @param correlationKeyProps The defining properties for a correlation key
     * @param processInstanceId The process instance id, if available (otherwise null).
     * @return The result of the {@link Command}.
     */
    public <T> T doKieSessionOperation(Command<T> cmd, String deploymentId, List<String> correlationKeyProps,  Long processInstanceId) {
        if( emptyDeploymentId(deploymentId) ) {
            throw new DeploymentNotFoundException("No deployment id supplied! Could not retrieve runtime to execute " + cmd.getClass().getSimpleName());
        }

        try {
            Context cmdContext = getDeploymentCommandContext(correlationKeyProps, processInstanceId, cmd);
            T result = processService.execute(deploymentId, cmdContext, cmd);
            return result;
        } catch (ProcessInstanceNotFoundException e) {
            throw KieRemoteRestOperationException.notFound("Process instance " + processInstanceId + " could not be found!");
        } catch (org.jbpm.services.api.DeploymentNotFoundException e) {
            throw KieRemoteRestOperationException.notFound(e.getMessage());
        }  catch (RuntimeException re) {
            throw KieRemoteRestOperationException.internalServerError(re.getMessage(), re);
        }
    }

    private Context getDeploymentCommandContext(List<String> correlationKeyProps, Long processInstanceId, Command cmd) {
        if( cmd instanceof StartCorrelatedProcessCommand || cmd instanceof StartProcessInstanceCommand ) {
            return EmptyContext.get();
        }
        // correlation key
        CorrelationKey correlationKey = null;
        if( correlationKeyProps != null && ! correlationKeyProps.isEmpty() ) {
            CorrelationKeyFactory factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
            correlationKey = factory.newCorrelationKey(correlationKeyProps);
        } else if (cmd instanceof CorrelationKeyCommand ) {
            correlationKey = ((CorrelationKeyCommand) cmd).getCorrelationKey();
        }
        if( correlationKey != null ) {
            return CorrelationKeyContext.get(correlationKey);
        }

        // process instance
        if( processInstanceId == null && cmd instanceof ProcessInstanceIdCommand ) {
            processInstanceId = ((ProcessInstanceIdCommand) cmd).getProcessInstanceId();
            if( processInstanceId != null && processInstanceId != -1L) {
               return ProcessInstanceIdContext.get(processInstanceId);
            }
        }
        if( processInstanceId != null && processInstanceId != -1L) {
            return ProcessInstanceIdContext.get(processInstanceId);
        }

        // empty
        return EmptyContext.get();
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
        try {
            Object procVar = processService.getProcessInstanceVariable(processInstanceId, varName);
            return procVar;
        } catch (ProcessInstanceNotFoundException e) {
            throw KieRemoteRestOperationException.notFound("Process instance " + processInstanceId + " could not be found!");
        } catch (org.jbpm.services.api.DeploymentNotFoundException e) {
            throw KieRemoteRestOperationException.notFound(e.getMessage());
        }  catch (RuntimeException re) {
            throw KieRemoteRestOperationException.internalServerError(re.getMessage(), re);
        }
    }

    // task operations ------------------------------------------------------------------------------------------------------------

    private static Set<Class<? extends TaskCommand>> taskDeploymentIdCommands = new HashSet<Class<? extends TaskCommand>>();
    static {
        taskDeploymentIdCommands.add(CompleteTaskCommand.class);
        taskDeploymentIdCommands.add(FailTaskCommand.class);
        taskDeploymentIdCommands.add(SkipTaskCommand.class);
    }

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
    @SuppressWarnings("unchecked")
    private <T> T doTaskOperation(Long taskId, String deploymentId, Long processInstanceId, Task task, TaskCommand<T> cmd) {
        if( emptyDeploymentId(deploymentId) && taskDeploymentIdCommands.contains(cmd.getClass()) ) {
            deploymentId = getDeploymentId(task,taskId, cmd);
        }
        // take care of serialization
        if( cmd instanceof GetTaskCommand
                || cmd instanceof GetContentByIdCommand
                || cmd instanceof GetTaskContentCommand ) {
           cmd = new ExecuteAndSerializeCommand(cmd, deploymentId);
        }

        if( cmd instanceof CompleteTaskCommand ) {
            CompleteTaskCommand completeCmd = (CompleteTaskCommand) cmd;
            String userId = completeCmd.getUserId();
            Map<String, Object> data = completeCmd.getData();

            cmd = new CompositeCommand(
                new CompleteTaskCommand(taskId, userId, data),
                new ProcessSubTaskCommand(taskId, userId, data),
                new CancelDeadlineCommand(taskId, true, true));
        }

        try {
            return userTaskService.execute(deploymentId, cmd);
        } catch (TaskNotFoundException e) {
            throw KieRemoteRestOperationException.notFound("Task " + taskId + " could not be found!");
        } catch (ProcessInstanceNotFoundException e) {
            throw KieRemoteRestOperationException.notFound("Process instance " + processInstanceId + " could not be found!");
        } catch (org.jbpm.services.api.DeploymentNotFoundException e) {
            throw KieRemoteRestOperationException.notFound(e.getMessage());
        }  catch (RuntimeException re) {
            throw KieRemoteRestOperationException.internalServerError(re.getMessage(), re);
        }
    }


    public <T> T doRestTaskOperation(Long taskId, String deploymentId, Long processInstanceId, Task task, TaskCommand<T> cmd) {
        try {
            return doTaskOperation(taskId, deploymentId, processInstanceId, task, cmd);
        } catch (PermissionDeniedException pde) {
            throw KieRemoteRestOperationException.conflict(pde.getMessage(), pde);
        }
    }

    private String getDeploymentId(Task task, Long taskId, TaskCommand cmd) {
        if( task == null ) {
            if( taskId == null ) {
                taskId = cmd.getTaskId();
            }
            task = userTaskService.getTask(taskId);
        }
        String deploymentId = null;
        if( task != null && task.getTaskData() != null ) {
            deploymentId = task.getTaskData().getDeploymentId();
        }
        return deploymentId;
    }
}
