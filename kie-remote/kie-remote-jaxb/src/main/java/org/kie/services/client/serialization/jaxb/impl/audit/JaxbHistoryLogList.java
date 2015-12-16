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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;

@XmlRootElement(name="log-instance-list")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("rawtypes")
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbHistoryLogList implements JaxbCommandResponse<List<Object>>, JaxbPaginatedList<Object> {

    @XmlElements({ 
        @XmlElement(name = "process-instance-log", type = JaxbProcessInstanceLog.class),
        @XmlElement(name = "node-instance-log", type = JaxbNodeInstanceLog.class),
        @XmlElement(name = "variable-instance-log", type = JaxbVariableInstanceLog.class),
    })
    @JsonTypeInfo(use=Id.CLASS, include=As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @Type(value=JaxbProcessInstanceLog.class),
            @Type(value=JaxbNodeInstanceLog.class),
            @Type(value=JaxbVariableInstanceLog.class)
    })
    private List<AbstractJaxbHistoryObject> historyLogList;

    @XmlAttribute
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "command-name")
    @XmlSchemaType(name = "string")
    private String commandName;
   
    @XmlElement(name="page-number")
    @XmlSchemaType(name="int")
    private Integer pageNumber;
    
    @XmlElement(name="page-size")
    @XmlSchemaType(name="int")
    private Integer pageSize;
    
    public JaxbHistoryLogList() { 
        // Default constructor
    }
    
    public JaxbHistoryLogList( List<? extends Object> logList ) { 
        initialize(logList);
    }
    
    private void initialize( List<? extends Object> logList ) { 
        this.historyLogList = new ArrayList<AbstractJaxbHistoryObject>();
        if( logList == null || logList.size() == 0 ) { 
            return;
        }
        for( Object logObj : logList ) { 
            if( logObj instanceof ProcessInstanceLog ) { 
                this.historyLogList.add(new JaxbProcessInstanceLog((ProcessInstanceLog) logObj));
            } else if( logObj instanceof NodeInstanceLog ) { 
                this.historyLogList.add(new JaxbNodeInstanceLog((NodeInstanceLog) logObj));
            } else if( logObj instanceof VariableInstanceLog ) { 
                this.historyLogList.add(new JaxbVariableInstanceLog((VariableInstanceLog) logObj));
            } else { 
                throw new IllegalArgumentException(logObj.getClass().getSimpleName() + " is not an acceptable object (list) for this constructor.");
            }
        }
    }
    
    public List<AbstractJaxbHistoryObject> getHistoryLogList() {
        lazyInitResponseList();
        return historyLogList;
    }
    
    public void setHistoryLogList(List<AbstractJaxbHistoryObject> list) {
        this.historyLogList = list;
    }

    private void lazyInitResponseList() { 
        if( this.historyLogList == null ) { 
            this.historyLogList = new ArrayList<AbstractJaxbHistoryObject>();
        }
    }

    /**
     * {@link JaxbCommandResponse} methods
     */
    
    @Override
    public Integer getIndex() {
        return index;
    }

    @Override
    public void setIndex(Integer index) {
       this.index = index; 
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String cmdName) {
        this.commandName = cmdName;
    }

    @Override
    @JsonIgnore
    public List<Object> getResult() {
        lazyInitResponseList();
        List<Object> results = new ArrayList<Object>();
        for( AbstractJaxbHistoryObject<Object> jaxbHistLog : this.historyLogList ) { 
            results.add(jaxbHistLog );
        }
        return results;
    }

    @Override
    public void setResult(List<Object> result) {
        initialize(result);;
    }

    @Override
    public void addContents(List<Object> contentList) {
        initialize(contentList);
    }
 
    @JsonTypeInfo(use=Id.CLASS, include=As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @Type(value=JaxbProcessInstanceLog.class),
            @Type(value=JaxbNodeInstanceLog.class),
            @Type(value=JaxbVariableInstanceLog.class)
    })
    public List<AbstractJaxbHistoryObject> getList() { 
        return historyLogList;
    }
    
    public void setList(List<AbstractJaxbHistoryObject> historyLogList) { 
        this.historyLogList = historyLogList;
    }

    @Override
    public Integer getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public void setPageNumber(Integer page) {
        this.pageNumber = page;
    }

    @Override
    public Integer getPageSize() {
        return this.pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}
