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

package org.kie.services.client.serialization.jaxb.impl.audit;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name="process-instance-log")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbProcessInstanceLog extends AbstractJaxbHistoryObject<ProcessInstanceLog> implements ProcessInstanceLog, JaxbCommandResponse<ProcessInstanceLog>{

    @XmlAttribute
    @XmlSchemaType(name="long")
    private Long id;
    
    @XmlElement(name="process-instance-id")
    @XmlSchemaType(name="long")
    private Long processInstanceId;
    
    @XmlElement(name="process-id")
    @XmlSchemaType(name="string")
    private String processId;
   
    @XmlElement
    @XmlSchemaType(name = "dateTime")
    private Date start;
    
    @XmlElement
    @XmlSchemaType(name = "dateTime")
    private Date end;
    
    @XmlElement(nillable=true)
    @XmlSchemaType(name = "int")
    private Integer status;
 
    @XmlElement(nillable=true, name="parent-process-instance-id")
    @XmlSchemaType(name = "long")
    private Long parentProcessInstanceId;
    
    @XmlElement(nillable=true)
    @XmlSchemaType(name = "string")
    private String outcome;    
    
    @XmlElement
    @XmlSchemaType(name = "long")
    private Long duration;
    
    @XmlElement
    @XmlSchemaType(name = "string")
    private String identity;    
    
    @XmlElement(name="process-version")
    @XmlSchemaType(name = "string")
    private String processVersion;
    
    @XmlElement(name="process-name")
    @XmlSchemaType(name = "string")
    private String processName;
    
    @XmlElement(name="external-id")
    @XmlSchemaType(name = "string")
    private String externalId;
   
    @XmlElement(name="process-instance-description")
    @XmlSchemaType(name = "string")
    private String processInstanceDescription;
    
    public JaxbProcessInstanceLog() { 
        super(ProcessInstanceLog.class);
    }
    
    public JaxbProcessInstanceLog(ProcessInstanceLog processInstanceLog) { 
        super(processInstanceLog, ProcessInstanceLog.class);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(Long parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getProcessInstanceDescription() {
        return processInstanceDescription;
    }

    public void setProcessInstanceDescription( String processInstanceDescription ) {
        this.processInstanceDescription = processInstanceDescription;
    }

    /**
     * {@link JaxbCommandResponse} fields and methods
     */
    
    @XmlAttribute
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "command-name")
    @XmlSchemaType(name = "string")
    private String commandName;
    
    public Integer getIndex() { 
        return index;
    }

    public void setIndex(Integer index) { 
        this.index = index;
    }
    
    public String getCommandName() { 
        return commandName;
    }
    
    public void setCommandName(String cmdName) {
        this.commandName = cmdName;
    }

    public ProcessInstanceLog getResult() {
        return this;
    }

    public void setResult(ProcessInstanceLog result) { 
        initialize(result);
    }
}
