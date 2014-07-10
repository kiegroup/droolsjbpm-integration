package org.kie.services.client.serialization.jaxb.impl.deploy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;


@XmlRootElement(name="deployment-job-result")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"jobId"})
public class JaxbDeploymentJobResult {

    /**
     * An internal field used to track the job on the server side
     */
    private transient String jobId;

    /**
     * The id of the jbpm-executor job
     */
    @XmlElement
    @XmlSchemaType(name="long")
    private volatile Long identifier;

    /**
     * The operation (deploy, undeploy) requested
     */
    @XmlElement
    @XmlSchemaType(name="string")
    private String operation;
   
    /**
     * Information about the deployment unit
     */
    @XmlElement(type = JaxbDeploymentUnit.class)
    private JaxbDeploymentUnit deploymentUnit;
   
    @XmlElement
    @XmlSchemaType(name="boolean")
    private volatile Boolean success;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String explanation;
    
    public JaxbDeploymentJobResult() { 
        // default
    }
    
    public JaxbDeploymentJobResult(String jobId, String explanation, JaxbDeploymentUnit depUnit, String operation ) {
        this.jobId = jobId;
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

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public String getJobId() {
        return jobId;
    }

}
