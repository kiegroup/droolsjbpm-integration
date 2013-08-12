package org.kie.services.remote.basic.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.jbpm.console.ng.pr.model.ProcessInstanceSummary;

@XmlRootElement(name="process-instance-summary")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProcessInstanceSummary extends AbstractJaxbObject<ProcessInstanceSummary> {

    @XmlElement
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement(name="process-name")
    @XmlSchemaType(name="string")
    private String processName;

    @XmlElement(name="process-version")
    @XmlSchemaType(name="string")
    private String processVersion;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer state;

    @XmlElement(name="start-time")
    @XmlSchemaType(name="string")
    private String startTime;

    @XmlElement(name="deployment-id")
    @XmlSchemaType(name="string")
    private String deploymentId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String initiator;

    public JaxbProcessInstanceSummary() {
        super(ProcessInstanceSummary.class);
    }
    
    public JaxbProcessInstanceSummary(ProcessInstanceSummary summary) { 
        super(summary, ProcessInstanceSummary.class);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public Integer getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
    
    
}
