package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "variable-instance-list")
public class VariableInstanceList {

    @XmlElement(name="variable-instance")
    private VariableInstance[] variableInstances;

    public VariableInstanceList() {
    }

    public VariableInstanceList(VariableInstance[] variableInstances) {
        this.variableInstances = variableInstances;
    }

    public VariableInstanceList(List<VariableInstance> variableInstances) {
        this.variableInstances = variableInstances.toArray(new VariableInstance[variableInstances.size()]);
    }

    public VariableInstance[] getVariableInstances() {
        return variableInstances;
    }

    public void setVariableInstances(VariableInstance[] variableInstances) {
        this.variableInstances = variableInstances;
    }
}
