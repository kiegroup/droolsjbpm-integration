package org.kie.services.client.serialization.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

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

    @XmlElement(name = "response")
    private List<JaxbCommandsResponse> responses;

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

    public List<JaxbCommandsResponse> getResponses() {
        return responses;
    }
}
