package org.kie.server.api.model.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-subprocesses")
public class SubProcessesDefinition {

    @XmlElementWrapper(name="subprocesses")
    private Collection<String> subProcesses;

    public SubProcessesDefinition() {
        this(new ArrayList<String>());
    }

    public SubProcessesDefinition(Collection<String> subprocesses) {
        this.subProcesses = subprocesses;
    }

    public Collection<String> getSubProcesses() {
        return subProcesses;
    }

    public void setSubProcesses(Collection<String> subProcesses) {
        this.subProcesses = subProcesses;
    }
}
