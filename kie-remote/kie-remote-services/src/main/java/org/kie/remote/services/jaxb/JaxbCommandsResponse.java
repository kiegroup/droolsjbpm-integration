package org.kie.remote.services.jaxb;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.drools.core.common.DefaultFactHandle;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.event.AuditEvent;
import org.jbpm.services.task.commands.GetTaskContentCommand;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.JaxbStringListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItemResponse;
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
            @XmlElement(name = "work-item", type = JaxbWorkItemResponse.class),
            @XmlElement(name = "variables", type = JaxbVariablesResponse.class),
            @XmlElement(name = "other", type = JaxbOtherResponse.class),
            @XmlElement(name = "history-log-list", type = JaxbHistoryLogList.class),
            @XmlElement(name = "proc-inst-log", type = JaxbProcessInstanceLog.class),
            @XmlElement(name = "node-inst-log", type = JaxbNodeInstanceLog.class),
            @XmlElement(name = "var-inst-log", type = JaxbVariableInstanceLog.class)
            })
    private List<JaxbCommandResponse<?>> responses;

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
            if (((JaxbContent) result).getId() == -1) {
                this.responses.add(new JaxbTaskContentResponse(((JaxbContent) result).getContentMap(), i, cmd));
            } else {
                this.responses.add(new JaxbContentResponse((JaxbContent) result, i, cmd));
            }
        } else if (List.class.isInstance(result)) { 
            // Neccessary to determine return type of empty lists
            Class listType = getListType(cmd);
            if( listType == null ) { 
                unknownResultType = true;
            } else if( listType.equals(TaskSummary.class) ) { 
                this.responses.add(new JaxbTaskSummaryListResponse((List<TaskSummary>) result, i, cmd));
            } else if( listType.equals(Long.class) ) {
                this.responses.add(new JaxbLongListResponse((List<Long>)result, i, cmd));
            } else if( listType.equals(String.class) ) {
                this.responses.add(new JaxbStringListResponse((List<String>)result, i, cmd));
            } else if( listType.equals(ProcessInstance.class) ) {
               this.responses.add(new JaxbProcessInstanceListResponse((List<ProcessInstance>) result, i, cmd));
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
           this.responses.add(new JaxbWorkItemResponse((WorkItem) result, i, cmd));
        } else if( result instanceof ProcessInstanceLog ) { 
            this.responses.add(new JaxbProcessInstanceLog((ProcessInstanceLog) result));
        } else if( result instanceof NodeInstanceLog ) { 
            this.responses.add(new JaxbNodeInstanceLog((NodeInstanceLog) result));
        } else if( result instanceof VariableInstanceLog ) { 
            this.responses.add(new JaxbVariableInstanceLog((VariableInstanceLog) result));
        } else if( result instanceof DefaultFactHandle ) { 
           this.responses.add(new JaxbOtherResponse(result, i, cmd));
        } else if( cmd instanceof GetTaskContentCommand ) { 
           this.responses.add(new JaxbTaskContentResponse((Map<String, Object>) result, i, cmd));
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

    static Class getListType( Command cmd ) {
        Class cmdClass = cmd.getClass();
        Type genSuper = cmdClass.getGenericSuperclass();
        if( genSuper != null ) {
            if( genSuper instanceof ParameterizedType ) {
                return getClassFromParameterizedListCmd(genSuper, cmdClass);
            }
        }
        Type [] genInts = cmdClass.getGenericInterfaces();
        if( genInts.length > 0 ) { 
           if( genInts[0] instanceof ParameterizedType ) { 
                return getClassFromParameterizedListCmd(genInts[0], cmdClass);
           }
        }
        throw new IllegalStateException("No list type could be found for " + cmd.getClass().getSimpleName() );
    }
    
    private static Class getClassFromParameterizedListCmd(Type genericIntOrSuper, Class cmdClass) { 
        Type[] listTypes = ((ParameterizedType) genericIntOrSuper).getActualTypeArguments();
        if( listTypes.length > 0 ) {
            if( listTypes[0] instanceof ParameterizedType ) {
                Type rawType = ((ParameterizedType) listTypes[0]).getRawType();
                if( Collection.class.isAssignableFrom((Class) rawType) ) { 
                    Type[] returnTypeParamTypes = ((ParameterizedType) listTypes[0]).getActualTypeArguments();
                    if( returnTypeParamTypes.length > 0 ) {
                        return (Class) returnTypeParamTypes[0];
                    }
                } 
            } 
        }
        throw new IllegalStateException("No list type could be found for " + cmdClass.getSimpleName() );
    }
}
