package org.kie.services.client.serialization.jaxb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;

import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.drools.core.common.DefaultFactHandle;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.FindActiveProcessInstancesCommand;
import org.jbpm.process.audit.command.FindNodeInstancesCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.process.audit.command.FindSubProcessInstancesCommand;
import org.jbpm.process.audit.command.FindVariableInstancesByNameCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.process.audit.event.AuditEvent;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTaskAssignedAsPotentialOwnerCommand;
import org.jbpm.services.task.commands.GetTaskByWorkItemIdCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksByStatusByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbContentResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummaryListResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;

@XmlRootElement(name = "command-response")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("rawtypes")
public class JaxbCommandsResponse {

    @XmlElement(name = "deployment-id")
    @XmlSchemaType(name = "string")
    private String deploymentId;

    @XmlElement(name = "process-instance-id")
    @XmlSchemaType(name = "long")
    private Long processInstanceId;

    @XmlElement(name = "ver")
    @XmlSchemaType(name = "int")
    private Integer version;

    @XmlElements({ 
            @XmlElement(name = "exception", type = JaxbExceptionResponse.class),
            @XmlElement(name = "long-list", type = JaxbLongListResponse.class),
            @XmlElement(name = "primitive", type = JaxbPrimitiveResponse.class),
            @XmlElement(name = "process-instance", type = JaxbProcessInstanceResponse.class),
            @XmlElement(name = "process-instance-list", type = JaxbProcessInstanceListResponse.class),
            @XmlElement(name = "task-response", type = JaxbTaskResponse.class),
            @XmlElement(name = "content-response", type = JaxbContentResponse.class ),
            @XmlElement(name = "task-summary-list", type = JaxbTaskSummaryListResponse.class),
            @XmlElement(name = "work-item", type = JaxbWorkItem.class),
            @XmlElement(name = "variables", type = JaxbVariablesResponse.class),
            @XmlElement(name = "other", type = JaxbOtherResponse.class),
            @XmlElement(name = "history-log-list", type = JaxbHistoryLogList.class),
            @XmlElement(name = "proc-inst-log", type = JaxbProcessInstanceLog.class),
            @XmlElement(name = "node-inst-log", type = JaxbNodeInstanceLog.class),
            @XmlElement(name = "var-inst-log", type = JaxbVariableInstanceLog.class)
            })
    private List<JaxbCommandResponse<?>> responses;

    @XmlTransient
    private static Map<Class, Class> cmdListTypes;
    static { 
        cmdListTypes = new HashMap<Class, Class>();
        // tasksummary
        cmdListTypes.put(GetTaskAssignedAsBusinessAdminCommand.class, TaskSummary.class);
        cmdListTypes.put(GetTaskAssignedAsPotentialOwnerCommand.class, TaskSummary.class);
        cmdListTypes.put(GetTasksByStatusByProcessInstanceIdCommand.class, TaskSummary.class);
        cmdListTypes.put(GetTasksOwnedCommand.class, TaskSummary.class);
        
        // long
        cmdListTypes.put(GetTaskByWorkItemIdCommand.class, Long.class);
        cmdListTypes.put(GetTasksByProcessInstanceIdCommand.class, Long.class);
        
        // string
        cmdListTypes.put(GetProcessIdsCommand.class, String.class);
        
        // processInstance
        cmdListTypes.put(GetProcessInstancesCommand.class, ProcessInstance.class);
        
        // processInstanceLog
        cmdListTypes.put(FindProcessInstancesCommand.class, ProcessInstanceLog.class);
        cmdListTypes.put(FindActiveProcessInstancesCommand.class, ProcessInstanceLog.class);
        cmdListTypes.put(FindSubProcessInstancesCommand.class, ProcessInstanceLog.class);
        
        // variableInstanceLog
        cmdListTypes.put(FindVariableInstancesByNameCommand.class, VariableInstanceLog.class);
        cmdListTypes.put(FindVariableInstancesCommand.class, VariableInstanceLog.class);
       
        // nodeInstanceLog
        cmdListTypes.put(FindNodeInstancesCommand.class, NodeInstanceLog.class);
    }

    public JaxbCommandsResponse() {
        this.version = 1;
        // Default constructor
    }

    public JaxbCommandsResponse(JaxbCommandsRequest request) {
        super();
        this.deploymentId = request.getDeploymentId();
        this.processInstanceId = request.getProcessInstanceId();
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    private void lazyInitResponseList() { 
        if( this.responses == null ) { 
            this.responses = new ArrayList<JaxbCommandResponse<?>>();
        }
    }

    public List<JaxbCommandResponse<?>> getResponses() {
        lazyInitResponseList();
        return responses;
    }

    public void setResponses(List<JaxbCommandResponse<?>> responses) {
        this.responses = responses;
    }

    public void addException(Exception exception, int i, Command<?> cmd, JaxbRequestStatus status) {
        lazyInitResponseList();
        this.responses.add(new JaxbExceptionResponse(exception, i, cmd, status));
    }

    @SuppressWarnings("unchecked")
    public void addResult(Object result, int i, Command<?> cmd) {
        lazyInitResponseList();
        boolean unknownResultType = false;
        
        String className = result.getClass().getName();
        if (result instanceof ProcessInstance) {
            this.responses.add(new JaxbProcessInstanceResponse((ProcessInstance) result, i, cmd));
        } else if (result instanceof JaxbTask) {
            this.responses.add(new JaxbTaskResponse((JaxbTask) result, i, cmd));
        } else if (result instanceof JaxbContent) {
            this.responses.add(new JaxbContentResponse((JaxbContent) result, i, cmd));
        } else if (List.class.isInstance(result)) { 
            // Neccessary to determine return type of empty lists
            Class listType = cmdListTypes.get(cmd.getClass());
            if( listType == null ) { 
                unknownResultType = true;
            } else if( listType.equals(TaskSummary.class) ) { 
                this.responses.add(new JaxbTaskSummaryListResponse((List<TaskSummary>) result, i, cmd));
            } else if( listType.equals(Long.class) ) {
                this.responses.add(new JaxbLongListResponse((List<Long>)result, i, cmd));
            } else if( listType.equals(ProcessInstance.class) ) {
               List<JaxbProcessInstanceResponse> procInstList = new ArrayList<JaxbProcessInstanceResponse>();
               for( ProcessInstance procInst : (List<ProcessInstance>) result) { 
                   procInstList.add(new JaxbProcessInstanceResponse(procInst));
               }
               this.responses.add(new JaxbProcessInstanceListResponse(procInstList, i, cmd));
            } else if( listType.equals(ProcessInstanceLog.class) 
                    || listType.equals(NodeInstanceLog.class)
                    || listType.equals(VariableInstanceLog.class) ) {
                this.responses.add(new JaxbHistoryLogList((List<AuditEvent>) result));
            } else { 
                throw new IllegalStateException(listType.getSimpleName() + " should be handled but is not in " + this.getClass().getSimpleName() + "!" );
            }
        } else if (result.getClass().isPrimitive() 
        		|| Boolean.class.getName().equals(className)
        		|| Byte.class.getName().equals(className)
        		|| Short.class.getName().equals(className)
        		|| Integer.class.getName().equals(className)
        		|| Character.class.getName().equals(className)
        		|| Long.class.getName().equals(className)
        		|| Float.class.getName().equals(className)
        		|| Double.class.getName().equals(className) ) {
            this.responses.add(new JaxbPrimitiveResponse(result, i, cmd));
        } else if( result instanceof WorkItem ) { 
           this.responses.add(new JaxbWorkItem((WorkItem) result, i, cmd));
        } else if( result instanceof ProcessInstanceLog ) { 
            this.responses.add(new JaxbProcessInstanceLog((ProcessInstanceLog) result));
        } else if( result instanceof NodeInstanceLog ) { 
            this.responses.add(new JaxbNodeInstanceLog((NodeInstanceLog) result));
        } else if( result instanceof VariableInstanceLog ) { 
            this.responses.add(new JaxbVariableInstanceLog((VariableInstanceLog) result));
        } else if( result instanceof DefaultFactHandle ) { 
           this.responses.add(new JaxbOtherResponse(result, i, cmd));
        } 
        // Other
        else if( result instanceof JaxbExceptionResponse ) { 
           this.responses.add((JaxbExceptionResponse) result);
        } else {
            unknownResultType = true;
        }
        
        if (unknownResultType) {
            System.out.println( this.getClass().getSimpleName() + ": unknown result type " + result.getClass().getSimpleName() 
                    + " from command " + cmd.getClass().getSimpleName() + " added.");
        }
    }
}
