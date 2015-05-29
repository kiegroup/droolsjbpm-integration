package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-instance-list")
public class ProcessInstanceList {

    @XmlElement(name="process-instance")
    private ProcessInstance[] processInstances;

    public ProcessInstanceList() {
    }

    public ProcessInstanceList(ProcessInstance[] processInstances) {
        this.processInstances = processInstances;
    }

    public ProcessInstanceList(List<ProcessInstance> processInstances) {
        this.processInstances = processInstances.toArray(new ProcessInstance[processInstances.size()]);
    }

    public ProcessInstance[] getProcessInstances() {
        return processInstances;
    }

    public void setProcessInstances(ProcessInstance[] processInstances) {
        this.processInstances = processInstances;
    }
}
