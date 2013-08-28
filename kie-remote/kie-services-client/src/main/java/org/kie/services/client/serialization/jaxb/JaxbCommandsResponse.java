package org.kie.services.client.serialization.jaxb;

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

import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.drools.core.common.DefaultFactHandle;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTaskAssignedAsPotentialOwnerCommand;
import org.jbpm.services.task.commands.GetTaskByWorkItemIdCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksByStatusByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.*;

@SuppressWarnings("rawtypes")
@XmlRootElement(name = "command-response")
@XmlAccessorType(XmlAccessType.FIELD)
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
            @XmlElement(name = "task", type = JaxbTaskResponse.class),
            @XmlElement(name = "task-summary-list", type = JaxbTaskSummaryListResponse.class),
            @XmlElement(name = "work-item", type = JaxbWorkItem.class),
            @XmlElement(name = "other", type = JaxbOtherResponse.class),
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
        
        // processInstance
        cmdListTypes.put(GetProcessInstancesCommand.class, ProcessInstance.class);
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

    public void addException(Exception exception, int i, Command<?> cmd) {
        lazyInitResponseList();
        this.responses.add(new JaxbExceptionResponse(exception, i, cmd));
    }

    @SuppressWarnings("unchecked")
    public void addResult(Object result, int i, Command<?> cmd) {
        lazyInitResponseList();
        boolean unknownResultType = false;
        
        String className = result.getClass().getName();
        if (result instanceof ProcessInstance) {
            this.responses.add(new JaxbProcessInstanceResponse((ProcessInstance) result, i, cmd));
        } else if (result instanceof Task) {
            this.responses.add(new JaxbTaskResponse((Task) result, i, cmd));
        } else if (List.class.isInstance(result)) { 
            // Neccessary to determine return type of empty lists
            Class listType = cmdListTypes.get(cmd.getClass());
            if( listType.equals(TaskSummary.class) ) { 
                this.responses.add(new JaxbTaskSummaryListResponse((List<TaskSummary>) result, i, cmd));
            } else if( listType.equals(Long.class) ) {
                this.responses.add(new JaxbLongListResponse((List<Long>)result, i, cmd));
            } else if( listType.equals(ProcessInstance.class) ) {
               List<JaxbProcessInstanceResponse> procInstList = new ArrayList<JaxbProcessInstanceResponse>();
               for( ProcessInstance procInst : (List<ProcessInstance>) result) { 
                   procInstList.add(new JaxbProcessInstanceResponse(procInst));
               }
                this.responses.add(new JaxbProcessInstanceListResponse(procInstList, i, cmd));
            } else {
                unknownResultType = true;
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
            throw new UnsupportedOperationException("Result type " + result.getClass().getSimpleName() + " from command " + cmd.getClass().getSimpleName() + " is an unsupported response type.");
        }
    }
}
