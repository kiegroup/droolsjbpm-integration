package org.kie.services.client.serialization.jaxb.impl;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.ProcessInstance;

@XmlRootElement(name="processInstance")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={JaxbProcess.class})
public class JaxbProcessInstance implements ProcessInstance {

    @XmlElement
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement
    @XmlSchemaType(name="string")
    private String processName;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer state; 

    @XmlElement
    private JaxbProcess process;
    
    @XmlElement(name="potentialOwner")
    private List<String> eventTypes;
    
    @Override
    public String getProcessId() {
        return processId;
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public String getProcessName() {
        return processName;
    }
    
    @Override
    public int getState() {
        return state;
    }
    
    @Override
    public Process getProcess() {
        return process;
    }

    @Override
    public String[] getEventTypes() {
        return eventTypes.toArray(new String[eventTypes.size()]);
    }

    public JaxbProcessInstance() { 
        // Default Constructor
    }
    
    public JaxbProcessInstance(ProcessInstance processInstance) { 
        this.eventTypes = Arrays.asList(processInstance.getEventTypes());
        this.id = processInstance.getId();
        this.process = new JaxbProcess(processInstance.getProcess());
        this.processId = processInstance.getProcessId();
        this.processName = processInstance.getProcessName();
        this.state = processInstance.getState();
    }
    
    @Override
    public void signalEvent(String type, Object event) {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + ProcessInstance.class.getSimpleName() + " implementation.");
    }

}
