package org.kie.services.client.serialization.jaxb.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.xml.AbstractJaxbHistoryObject;
import org.jbpm.process.audit.xml.JaxbNodeInstanceLog;
import org.jbpm.process.audit.xml.JaxbProcessInstanceLog;
import org.jbpm.process.audit.xml.JaxbVariableInstanceLog;

@XmlRootElement(name="log-instance-list")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("rawtypes")
public class JaxbHistoryLogList {

    @XmlElements({ 
        @XmlElement(name = "process-instance-log", type = JaxbProcessInstanceLog.class),
        @XmlElement(name = "node-instance-log", type = JaxbNodeInstanceLog.class),
        @XmlElement(name = "variable-instance-log", type = JaxbVariableInstanceLog.class),
        })
    List<AbstractJaxbHistoryObject> historyLogList;

    public JaxbHistoryLogList() { 
        // Default constructor
    }
    
    public JaxbHistoryLogList( List<?> logList ) { 
        if( logList == null || logList.size() == 0 ) { 
            return;
        }
        Object firstLogObj = logList.get(0);
        if( firstLogObj == null ) { 
            throw new IllegalArgumentException("null is not an acceptable value for the list parameter for this constuctor." );
        }
        if( firstLogObj instanceof ProcessInstanceLog ) { 
            this.historyLogList = new ArrayList<AbstractJaxbHistoryObject>();
            for( Object logObj : logList ) { 
                this.historyLogList.add(new JaxbProcessInstanceLog((ProcessInstanceLog) logObj));
            }
        } else if( firstLogObj instanceof NodeInstanceLog ) { 
            this.historyLogList = new ArrayList<AbstractJaxbHistoryObject>();
            for( Object logObj : logList ) { 
                this.historyLogList.add(new JaxbNodeInstanceLog((NodeInstanceLog) logObj));
            }
        } else if( firstLogObj instanceof VariableInstanceLog ) { 
            this.historyLogList = new ArrayList<AbstractJaxbHistoryObject>();
            for( Object logObj : logList ) { 
                this.historyLogList.add(new JaxbVariableInstanceLog((VariableInstanceLog) logObj));
            }
        } else { 
            throw new IllegalArgumentException(firstLogObj.getClass().getSimpleName() + " is not an acceptable object (list) for this constructor.");
        }
    }
    
    public List<AbstractJaxbHistoryObject> getHistoryLogList() {
        lazyInitResponseList();
        return historyLogList;
    }

    private void lazyInitResponseList() { 
        if( this.historyLogList == null ) { 
            this.historyLogList = new ArrayList<AbstractJaxbHistoryObject>();
        }
    }

}
