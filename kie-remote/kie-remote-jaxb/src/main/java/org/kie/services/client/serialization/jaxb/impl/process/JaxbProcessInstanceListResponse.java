package org.kie.services.client.serialization.jaxb.impl.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;

@XmlRootElement(name="process-instance-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={JaxbProcessInstanceResponse.class})
@JsonIgnoreProperties({"result"})
public class JaxbProcessInstanceListResponse extends AbstractJaxbCommandResponse<List<ProcessInstance>> {

    @XmlElements({
        @XmlElement(name="process-instance",type=JaxbProcessInstance.class)
    })
    private List<JaxbProcessInstance> processInstanceList;

    public JaxbProcessInstanceListResponse() { 
        this.processInstanceList = new ArrayList<JaxbProcessInstance>();
    }
    
    public JaxbProcessInstanceListResponse(Collection<JaxbProcessInstance> processInstanceList) { 
       this.processInstanceList = new ArrayList<JaxbProcessInstance>(processInstanceList);
    }
    
    public JaxbProcessInstanceListResponse(List<ProcessInstance> processInstanceList, int i, Command<?> cmd ) { 
        super(i, cmd);
        setResult(processInstanceList);
    }
    
    public void setResult(List<ProcessInstance> result) {
        this.processInstanceList = new ArrayList<JaxbProcessInstance>();
        for( ProcessInstance procInst : result ) { 
            JaxbProcessInstance jaxbProcInst;
            if( procInst instanceof JaxbProcessInstance ) { 
                jaxbProcInst = (JaxbProcessInstance) procInst;
            } else { 
                jaxbProcInst = new JaxbProcessInstance(procInst);
            }
            this.processInstanceList.add(jaxbProcInst);
        }
    }
    
    public List<ProcessInstance> getResult() {
        List<ProcessInstance> resultList = new ArrayList<ProcessInstance>();
        resultList.addAll(this.processInstanceList);
        return resultList;
    }
}
