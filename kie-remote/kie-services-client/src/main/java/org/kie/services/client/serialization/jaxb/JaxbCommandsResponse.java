package org.kie.services.client.serialization.jaxb;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExecutionResultsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskSummaryListResponse;

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
            @XmlElement(name = "execution-results", type = JaxbExecutionResultsResponse.class),
            @XmlElement(name = "primitive", type = JaxbPrimitiveResponse.class),
            @XmlElement(name = "process-instance", type = JaxbProcessInstanceResponse.class),
            @XmlElement(name = "task", type = JaxbTaskResponse.class),
            @XmlElement(name = "task-summary-list", type = JaxbTaskSummaryListResponse.class),
            @XmlElement(name = "exception", type = JaxbExceptionResponse.class)
            })
    private List<JaxbCommandResponse<?>> responses;

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

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public Integer getVersion() {
        return version;
    }

    public void addException(Exception exception, int i, Command<?> cmd) {
        lazyInitResponseList();
        this.responses.add(new JaxbExceptionResponse(exception, i, cmd));
    }

    public void addResult(Object result, int i, Command<?> cmd) {
        lazyInitResponseList();
        boolean unknownResultType = false;
        if (result instanceof ProcessInstance) {
            this.responses.add(new JaxbProcessInstanceResponse((ProcessInstance) result, i, cmd));
        } else if (result instanceof Task) {
            this.responses.add(new JaxbTaskResponse((Task) result, i, cmd));
        } else if (result instanceof List) {
        	List<?> list = (List<?>) result;
        	boolean tasks = true;
        	for (Object o: list) {
        		if (!(o instanceof TaskSummary)) {
        			tasks = false;
        		}
            }
        	// TODO what about other types?
        	if (tasks) {
        		this.responses.add(new JaxbTaskSummaryListResponse((List<TaskSummary>) result, i, cmd));
        	} else {
                unknownResultType = true;
            }
        } else if (result.getClass().isPrimitive()) {
            this.responses.add(new JaxbPrimitiveResponse(result, i, cmd));
        } else {
            unknownResultType = true;
        }
        if (unknownResultType) {
            throw new UnsupportedOperationException(result.getClass().getSimpleName() + " is an unsupported response type.");
        }
    }
    
    public List<JaxbCommandResponse<?>> getResponses() {
        lazyInitResponseList();
        return responses;
    }

    private void lazyInitResponseList() { 
        if( this.responses == null ) { 
            this.responses = new ArrayList<JaxbCommandResponse<?>>();
        }
    }
}
