package org.kie.services.client.serialization.jaxb.impl.process;

import static org.kie.services.client.serialization.JaxbSerializationProvider.unsupported;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.command.Command;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.remote.common.jaxb.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;

@XmlRootElement(name="process-instance")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"processName", "process", "result"})
public class JaxbProcessInstanceResponse extends AbstractJaxbCommandResponse<ProcessInstance> implements ProcessInstance {

    @XmlElement(name="process-id")
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement
    @XmlSchemaType(name="long")
    private long id;

    @XmlElement
    @XmlSchemaType(name="int")
    private int state; 

    @XmlElement(name="event-types")
    private List<String> eventTypes = new ArrayList<String>();
    
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

    public JaxbProcessInstanceResponse(ProcessInstance processInstance, String requestUrl) { 
        initialize(processInstance);
        this.url = requestUrl;
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
    
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    @Override
    public String getProcessName() {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + ProcessInstance.class.getSimpleName() + " implementation.");
    }
    
    @Override
    public Process getProcess() {
        return (Process) unsupported(ProcessInstance.class);
    }

    public String[] getEventTypes() {
        return eventTypes.toArray(new String[eventTypes.size()]);
    }

    @Override
    public void signalEvent(String type, Object event) {
        unsupported(ProcessInstance.class);
    }

    @Override
    public ProcessInstance getResult() {
        return this;
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
