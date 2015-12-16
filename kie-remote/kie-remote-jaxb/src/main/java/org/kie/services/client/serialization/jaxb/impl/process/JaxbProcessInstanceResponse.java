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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;

@XmlRootElement(name="process-instance-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"processName", "process", "result"})
public class JaxbProcessInstanceResponse extends JaxbProcessInstance implements JaxbCommandResponse<ProcessInstance> {

    // response
    
    @XmlElement
    protected JaxbRequestStatus status;
    
    @XmlElement
    @XmlSchemaType(name="anyURI")
    protected String url;
   
    // command response
    
    @XmlAttribute
    @XmlSchemaType(name="int")
    private Integer index;
    
    @XmlElement(name="command-name")
    @XmlSchemaType(name="string")
    protected String commandName;
    
    public JaxbProcessInstanceResponse() { 
        // Default Constructor
    }

    public JaxbProcessInstanceResponse(ProcessInstance processInstance, int i, Command<?> cmd) { 
        super(processInstance);
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    public JaxbProcessInstanceResponse(ProcessInstance processInstance) { 
        super(processInstance);
    }

    public JaxbProcessInstanceResponse(ProcessInstance processInstance, String requestUrl) { 
        super(processInstance);
        this.url = requestUrl;
        this.status = JaxbRequestStatus.SUCCESS;
    }

    public JaxbRequestStatus getStatus() {
        return status;
    }

    public void setStatus( JaxbRequestStatus status ) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    @Override
    public Integer getIndex() {
        return index;
    }

    @Override
    public void setIndex( Integer index ) {
        this.index = index;
    }

    @Override
    public String getCommandName() {
        return this.commandName;
    }

    @Override
    public void setCommandName( String cmdName ) {
        this.commandName = cmdName;
    }
    
    @Override
    public ProcessInstance getResult() {
        return this;
    }

    @Override
    public void setResult(ProcessInstance result) {
        initialize(result);
    }

}
