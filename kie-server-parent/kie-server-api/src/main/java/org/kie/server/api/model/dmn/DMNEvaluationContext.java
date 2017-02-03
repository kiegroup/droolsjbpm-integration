package org.kie.server.api.model.dmn;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-evaluation-context")
public class DMNEvaluationContext {

    @XmlElement(name="model-namespace")
    private String namespace;

    @XmlElement(name="model-name")
    private String modelName;

    @XmlElement(name="decision-name")
    private String decisionName;

    @XmlElementWrapper(name="dmn-context")
    private Map<String, Object> dmnContext = new HashMap<>();
    
    public DMNEvaluationContext() {
        // no-arg constructor for marshalling
    }
    
    public DMNEvaluationContext(Map<String, Object> dmnContext) {
        this.dmnContext.putAll( dmnContext );
    }
    
    public DMNEvaluationContext(String namespace, String modelName, Map<String, Object> dmnContext) {
        this(dmnContext);
        this.namespace = namespace;
        this.modelName = modelName;
    }
    
    public DMNEvaluationContext(String namespace, String modelName, String decisionName, Map<String, Object> dmnContext) {
        this(namespace, modelName, dmnContext);
        this.decisionName = decisionName;
    }

    
    public String getNamespace() {
        return namespace;
    }

    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    
    public String getModelName() {
        return modelName;
    }

    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    
    public String getDecisionName() {
        return decisionName;
    }

    
    public void setDecisionName(String decisionName) {
        this.decisionName = decisionName;
    }

    
    public Map<String, Object> getDmnContext() {
        return dmnContext;
    }

    
    public void setDmnContext(Map<String, Object> dmnContext) {
        this.dmnContext = dmnContext;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DMNEvaluationContext [namespace=").append(namespace).append(", modelName=").append(modelName).append(", decisionName=").append(decisionName).append(", dmnContext=").append(dmnContext).append("]");
        return builder.toString();
    }
    
    
}
