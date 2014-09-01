package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

@XmlRootElement(name="query-task-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbQueryTaskInfo {

    @XmlElement
    private JaxbTaskSummary taskSummary;
   
    @XmlElement
    private List<JaxbVariableInfo> variables;

    public JaxbQueryTaskInfo() {
        // default for JAXB
    }
    
    public JaxbTaskSummary getTaskSummary() {
        return taskSummary;
    }

    public void setTaskSummary( JaxbTaskSummary taskSummary ) {
        this.taskSummary = taskSummary;
    }

    public List<JaxbVariableInfo> getVariables() {
        return variables;
    }

    public void setVariables( List<JaxbVariableInfo> variables ) {
        this.variables = variables;
    }
    
}
