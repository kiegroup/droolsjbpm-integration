package org.kie.server.api.model.definition;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-definitions")
public class ProcessDefinitionList {

    @XmlElement(name="processes")
    private ProcessDefinition[] processes;

    public ProcessDefinitionList() {
    }

    public ProcessDefinitionList(ProcessDefinition[] processes) {
        this.processes = processes;
    }

    public ProcessDefinitionList(List<ProcessDefinition> processes) {
        this.processes = processes.toArray(new ProcessDefinition[processes.size()]);
    }

    public ProcessDefinition[] getProcesses() {
        return processes;
    }

    public void setProcesses(ProcessDefinition[] processes) {
        this.processes = processes;
    }
}
