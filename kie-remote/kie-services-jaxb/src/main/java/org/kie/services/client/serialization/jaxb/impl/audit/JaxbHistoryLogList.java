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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.AbstractHistoryLogCommand;
import org.jbpm.process.audit.event.AuditEvent;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name="log-instance-list")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("rawtypes")
public class JaxbHistoryLogList implements JaxbCommandResponse<List<AuditEvent>>{

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
    
    public JaxbHistoryLogList() { 
        // Default constructor
    }
    
    public JaxbHistoryLogList( List<? extends AuditEvent> logList ) { 
        initialize(logList);
    }
    
    private void initialize( List<? extends AuditEvent> logList ) { 
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
    public List<AuditEvent> getResult() {
        List<AuditEvent> results = new ArrayList<AuditEvent>();
        for( AbstractJaxbHistoryObject<?> jaxbHistLog : this.historyLogList ) { 
            results.add(jaxbHistLog.createEntityInstance());
        }
        return results;
    }

    @Override
    public void setResult(List<AuditEvent> result) {
        initialize(result);;
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
    
}
