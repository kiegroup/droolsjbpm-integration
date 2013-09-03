package org.kie.services.client.serialization.jaxb.impl;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;

@XmlRootElement(name="process-instance")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProcessInstanceResponse extends AbstractJaxbCommandResponse<ProcessInstance> implements ProcessInstance {

    @XmlElement(name="process-id")
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer state; 

    @XmlElement(name="event-types")
    private List<String> eventTypes;
    
    public JaxbProcessInstanceResponse() { 
        // Default Constructor
    }

    public JaxbProcessInstanceResponse(ProcessInstance processInstance, int i, Command<?> cmd) { 
        super(i, cmd);
        initialize(processInstance);
    }

    public JaxbProcessInstanceResponse(ProcessInstance processInstance) { 
        initialize(processInstance);
    }

    public JaxbProcessInstanceResponse(ProcessInstance processInstance, HttpServletRequest request) { 
        initialize(processInstance);
        this.url = getUrl(request);
        this.status = JaxbRequestStatus.SUCCESS;
    }

    protected void initialize(ProcessInstance processInstance) { 
        if( processInstance != null ) { 
            this.eventTypes = Arrays.asList(processInstance.getEventTypes());
            this.id = processInstance.getId();
            this.processId = processInstance.getProcessId();
            this.state = processInstance.getState();
        }
    }


    
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
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + ProcessInstance.class.getSimpleName() + " implementation.");
    }
    
    @Override
    public int getState() {
        return state;
    }
    
    @Override
    public Process getProcess() {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + ProcessInstance.class.getSimpleName() + " implementation.");
    }

    @Override
    public String[] getEventTypes() {
        return eventTypes.toArray(new String[eventTypes.size()]);
    }

    @Override
    public void signalEvent(String type, Object event) {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + ProcessInstance.class.getSimpleName() + " implementation.");
    }

    @Override
    public ProcessInstance getResult() {
        return this;
    }

    public JaxbRequestStatus getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String toString() {
        final StringBuilder b = new StringBuilder( "ProcessInstance " );
        b.append( this.id );
        b.append( " [processId=" );
        b.append( this.processId );
        b.append( ",state=" );
        b.append( this.state );
        b.append( "]" );
        return b.toString();
    }

    @Override
    public void setResult(ProcessInstance result) {
        initialize(result);
    }
    
}
