/*
 * Copyright 2015 JBoss Inc
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
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name="node-instance-log")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbNodeInstanceLog extends AbstractJaxbHistoryObject<NodeInstanceLog> implements NodeInstanceLog, JaxbCommandResponse<NodeInstanceLog> {

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
    private Date date;
    
    @XmlElement
    @XmlSchemaType(name = "int")
    private Integer type;
 
    @XmlElement(name="node-instance-id")
    @XmlSchemaType(name = "string")
    private String nodeInstanceId;
    
    @XmlElement(name="node-id")
    @XmlSchemaType(name = "string")
    private String nodeId;    
    
    @XmlElement(name="node-name")
    @XmlSchemaType(name = "string")
    private String nodeName;
    
    @XmlElement(name="node-type")
    @XmlSchemaType(name = "string")
    private String nodeType;    
    
    @XmlElement(name="work-item-id")
    @XmlSchemaType(name = "long")
    private Long workItemId;
    
    @XmlElement
    @XmlSchemaType(name = "string")
    private String connection;
    
    @XmlElement(name="external-id")
    @XmlSchemaType(name = "string")
    private String externalId;
     
    public JaxbNodeInstanceLog() { 
        super(NodeInstanceLog.class);
    }
    
    public JaxbNodeInstanceLog(NodeInstanceLog origLog) { 
       super(origLog, NodeInstanceLog.class);
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getNodeInstanceId() {
        return nodeInstanceId;
    }

    public void setNodeInstanceId(String nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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

    public NodeInstanceLog getResult() {
        return this;
    }

    public void setResult(NodeInstanceLog result) { 
        initialize(result);
    }
}
