package org.kie.server.api.model.definition;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-task-outputs")
public class TaskOutputsDefinition {

    @XmlElementWrapper(name="outputs")
    private Map<String, String> taskOutputs;

    public TaskOutputsDefinition() {
        this(new HashMap<String, String>());
    }

    public TaskOutputsDefinition(Map<String, String> taskOutputs) {
        this.taskOutputs = taskOutputs;
    }

    public Map<String, String> getTaskOutputs() {
        return taskOutputs;
    }

    public void setTaskOutputs(Map<String, String> taskOutputs) {
        this.taskOutputs = taskOutputs;
    }
}
