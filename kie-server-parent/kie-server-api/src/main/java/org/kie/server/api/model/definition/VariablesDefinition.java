package org.kie.server.api.model.definition;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-variables")
public class VariablesDefinition {

    @XmlElementWrapper(name="variables")
    private Map<String, String> variables;

    public VariablesDefinition() {
        this(new HashMap<String, String>());
    }

    public VariablesDefinition(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
