package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstance;

@XmlRootElement(name="query-process-instance-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbQueryProcessInstanceInfo {

    @XmlElement(name="process-instance")
    private JaxbProcessInstance processInstance;
    
    @XmlElement
    private List<JaxbVariableInfo> variables = new ArrayList<JaxbVariableInfo>();

    public JaxbQueryProcessInstanceInfo() {
        // default for JAXB
    }
   
    public JaxbProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance( JaxbProcessInstance processInstance ) {
        this.processInstance = processInstance;
    }

    public List<JaxbVariableInfo> getVariables() {
        return variables;
    }

    public void setVariables( List<JaxbVariableInfo> variables ) {
        this.variables = variables;
    }
}
