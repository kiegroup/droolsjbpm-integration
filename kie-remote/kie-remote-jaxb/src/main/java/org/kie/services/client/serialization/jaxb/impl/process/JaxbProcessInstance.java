/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;

@XmlRootElement(name="process-instance")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbProcessInstance implements ProcessInstance {

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

    @XmlElement
    @XmlSchemaType(name="long")
    private long parentProcessInstanceId;
    
    public JaxbProcessInstance() { 
        // Default Constructor
    }

    public JaxbProcessInstance(ProcessInstance processInstance) { 
        initialize(processInstance);
    }

    public JaxbProcessInstance(ProcessInstanceLog processInstanceLog) { 
        if( processInstanceLog != null ) { 
            this.id = processInstanceLog.getProcessInstanceId();
            this.processId = processInstanceLog.getProcessId();
            this.state = processInstanceLog.getStatus();
            if ( processInstanceLog.getParentProcessInstanceId() != null ) {
                this.parentProcessInstanceId = processInstanceLog.getParentProcessInstanceId();
            }
        }
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

    public long getParentProcessInstanceId() {
        return this.parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(long parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    @Override
    public String getProcessName() {
        return unsupported(ProcessInstance.class, String.class);
    }
    
    @Override
    public Process getProcess() {
        return unsupported(ProcessInstance.class, Process.class);
    }

    public String[] getEventTypes() {
        return eventTypes.toArray(new String[eventTypes.size()]);
    }

    @Override
    public void signalEvent(String type, Object event) {
        unsupported(ProcessInstance.class, Void.class);
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

}
