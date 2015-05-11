package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.type.JaxbBoolean;
import org.kie.server.api.model.type.JaxbByte;
import org.kie.server.api.model.type.JaxbCharacter;
import org.kie.server.api.model.type.JaxbDouble;
import org.kie.server.api.model.type.JaxbFloat;
import org.kie.server.api.model.type.JaxbInteger;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.api.model.type.JaxbShort;
import org.kie.server.api.model.type.JaxbString;

@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceResponse<T> {
    public static enum ResponseType {
        SUCCESS, FAILURE;
    }
    
    @XmlAttribute
    private ServiceResponse.ResponseType type;
    @XmlAttribute
    private String msg;
    @XmlElements({

        // types model
        @XmlElement(name = "boolean-type", type = JaxbBoolean.class),
        @XmlElement(name = "byte-type", type = JaxbByte.class),
        @XmlElement(name = "char-type", type = JaxbCharacter.class),
        @XmlElement(name = "double-type", type = JaxbDouble.class),
        @XmlElement(name = "float-type", type = JaxbFloat.class),
        @XmlElement(name = "int-type", type = JaxbInteger.class),
        @XmlElement(name = "long-type", type = JaxbLong.class),
        @XmlElement(name = "short-type", type = JaxbShort.class),
        @XmlElement(name = "string-type", type = JaxbString.class),
        @XmlElement(name = "map-type", type = JaxbMap.class),
        @XmlElement(name = "list-type", type = JaxbList.class),
        //kie server model
        @XmlElement(name = "kie-server-info", type = KieServerInfo.class),
        @XmlElement(name = "kie-container", type = KieContainerResource.class),
        @XmlElement(name = "results", type = String.class),
        @XmlElement(name = "kie-containers", type = KieContainerResourceList.class),
        @XmlElement(name = "kie-scanner", type = KieScannerResource.class),
        @XmlElement(name = "release-id", type = ReleaseId.class),
        // definition model
        @XmlElement(name = "process-associated-entities", type = AssociatedEntitiesDefinition.class),
        @XmlElement(name = "process-definition", type = ProcessDefinition.class),
        @XmlElement(name = "process-service-tasks", type = ServiceTasksDefinition.class),
        @XmlElement(name = "process-task-inputs", type = TaskInputsDefinition.class),
        @XmlElement(name = "process-task-outputs", type = TaskOutputsDefinition.class),
        @XmlElement(name = "user-task-definition", type = UserTaskDefinition.class),
        @XmlElement(name = "user-task-definitions", type = UserTaskDefinitionList.class),
        @XmlElement(name = "process-variables", type = VariablesDefinition.class),
        @XmlElement(name = "process-subprocesses", type = SubProcessesDefinition.class)
    })
    private T result;
    
    
    public ServiceResponse() {
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg) {
        this.type = type;
        this.msg = msg;
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg, T result ) {
        this.type = type;
        this.msg = msg;
        this.result = result;
    }
    
    public ServiceResponse.ResponseType getType() {
        return type;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setType(ServiceResponse.ResponseType type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public T getResult() {
        return result;
    }
    
    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ServiceResponse[" + type + ", msg='" + msg + "']";
    }
}