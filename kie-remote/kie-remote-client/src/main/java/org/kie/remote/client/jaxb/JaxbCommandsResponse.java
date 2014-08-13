package org.kie.remote.client.jaxb;

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

import org.kie.api.command.Command;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.common.jaxb.JaxbRequestStatus;
import org.kie.remote.jaxb.gen.FindActiveProcessInstancesCommand;
import org.kie.remote.jaxb.gen.FindNodeInstancesCommand;
import org.kie.remote.jaxb.gen.FindProcessInstancesCommand;
import org.kie.remote.jaxb.gen.FindSubProcessInstancesCommand;
import org.kie.remote.jaxb.gen.FindVariableInstancesByNameCommand;
import org.kie.remote.jaxb.gen.FindVariableInstancesCommand;
import org.kie.remote.jaxb.gen.GetProcessIdsCommand;
import org.kie.remote.jaxb.gen.GetProcessInstancesCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsBusinessAdminCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsPotentialOwnerCommand;
import org.kie.remote.jaxb.gen.GetTaskByWorkItemIdCommand;
import org.kie.remote.jaxb.gen.GetTasksByProcessInstanceIdCommand;
import org.kie.remote.jaxb.gen.GetTasksByStatusByProcessInstanceIdCommand;
import org.kie.remote.jaxb.gen.GetTasksByVariousFieldsCommand;
import org.kie.remote.jaxb.gen.GetTasksOwnedCommand;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbStringListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskContentResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;

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
    @XmlSchemaType(name = "string")
    private String version;

    @XmlElements({ 
            @XmlElement(name = "exception", type = JaxbExceptionResponse.class),
            @XmlElement(name = "long-list", type = JaxbLongListResponse.class),
            @XmlElement(name = "string-list", type = JaxbStringListResponse.class),
            @XmlElement(name = "primitive", type = JaxbPrimitiveResponse.class),
            @XmlElement(name = "process-instance", type = JaxbProcessInstanceResponse.class),
            @XmlElement(name = "process-instance-list", type = JaxbProcessInstanceListResponse.class),
            @XmlElement(name = "task-response", type = JaxbTaskResponse.class),
            @XmlElement(name = "content-response", type = JaxbContentResponse.class ),
            @XmlElement(name = "task-content-response", type = JaxbTaskContentResponse.class ),
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
        cmdListTypes.put(GetTasksByVariousFieldsCommand.class, TaskSummary.class);
        
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
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

}
