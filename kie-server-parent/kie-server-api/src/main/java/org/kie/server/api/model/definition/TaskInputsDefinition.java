package org.kie.server.api.model.definition;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-task-inputs")
public class TaskInputsDefinition {

    @XmlElementWrapper(name="inputs")
    private Map<String, String> taskInputs;

    public TaskInputsDefinition() {
        this(new HashMap<String, String>());
    }

    public TaskInputsDefinition(Map<String, String> taskInputs) {
        this.taskInputs = taskInputs;
    }

    public Map<String, String> getTaskInputs() {
        return taskInputs;
    }

    public void setTaskInputs(Map<String, String> taskInputs) {
        this.taskInputs = taskInputs;
    }
}
