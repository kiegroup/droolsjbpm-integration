package org.kie.services.client.serialization.jaxb.impl.deploy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;


@XmlRootElement(name="deployment-job-result")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDeploymentJobResult {

    @XmlElement
    @XmlSchemaType(name="string")
    private String operation;
    
    @XmlElement(name="deployment-unit")
    private JaxbDeploymentUnit deploymentUnit;
    
    @XmlElement
    @XmlSchemaType(name="boolean")
    private boolean success;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String explanation;
    
    public JaxbDeploymentJobResult() { 
        // default
    }
    
    public JaxbDeploymentJobResult(String explanation, boolean success, JaxbDeploymentUnit depUnit, String operation ) { 
        this.explanation = explanation;
        this.success = success;
        this.deploymentUnit = depUnit;
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public JaxbDeploymentUnit getDeploymentUnit() {
        return deploymentUnit;
    }

    public void setDeploymentUnit(JaxbDeploymentUnit depUnit ) {
        this.deploymentUnit = depUnit;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
}
