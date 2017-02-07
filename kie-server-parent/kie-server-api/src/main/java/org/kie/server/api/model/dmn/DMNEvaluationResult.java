package org.kie.server.api.model.dmn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.dmn.core.api.DMNContext;
import org.kie.dmn.core.api.DMNDecisionResult;
import org.kie.dmn.core.api.DMNDecisionResult.DecisionEvaluationStatus;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.api.DMNMessage;
import org.kie.dmn.core.api.DMNMessage.Severity;
import org.kie.dmn.core.impl.DMNDecisionResultImpl;
import org.kie.dmn.core.api.DMNResult;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-evaluation-result")
public class DMNEvaluationResult implements DMNResult {

    @XmlElement(name="model-namespace")
    private String namespace;

    @XmlElement(name="model-name")
    private String modelName;

    @XmlElement(name="decision-name")
    private String decisionName;

    @XmlElementWrapper(name="dmn-context")
    private Map<String, Object> dmnContext = new HashMap<>();

    @XmlElementWrapper(name="messages")
    private List<DMNMessage> messages = new ArrayList<>();
    
    @XmlElementWrapper(name="decision-results")
    private Map<String, DMNDecisionResult> decisionResults = new HashMap<>();
    
    public DMNEvaluationResult() {
        // no-arg constructor for marshalling
    }
    
    public DMNEvaluationResult(DMNResult dmnResult) {
        // TODO review not possible as impossible to serialize DMN nodes
        // this.setDmnContext( dmnResult.getContext().getAll() );
        this.setMessages( dmnResult.getMessages() );
        this.setDecisionResults( dmnResult.getDecisionResults() );
    }
    
    public DMNEvaluationResult(String namespace, String modelName, DMNResult dmnResult) {
        this(dmnResult);
        this.namespace = namespace;
        this.modelName = modelName;
    }
    
    public DMNEvaluationResult(String namespace, String modelName, String decisionName, DMNResult dmnResult) {
        this(namespace, modelName, dmnResult);
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
    public void setMessages(List<DMNMessage> messages) {
        this.messages = messages;
    }
    public void setDecisionResults(List<DMNDecisionResult> decisionResults) {
        for ( DMNDecisionResult dr : decisionResults ) {
            System.out.println(dr);
            System.out.println(dr.getDecisionId());
            System.out.println(dr.getDecisionName());
            System.out.println(dr.getResult());
            this.decisionResults.put(dr.getDecisionId(), dr);
        }
        unwireDecisionResults();
    }

    private void unwireDecisionResults() {
        for ( DMNDecisionResult dr : decisionResults.values() ) {
            if ( dr instanceof DMNDecisionResultImpl ) {
                DMNDecisionResultImpl i = (DMNDecisionResultImpl) dr;
                i.setResult(null);
            }
        }
    }
    
    // TODO review
    private void rewireDecisionResults() {
        for ( DMNDecisionResult dr : decisionResults.values() ) {
            if ( dr.getEvaluationStatus().equals( DecisionEvaluationStatus.SUCCEEDED ) && dr instanceof DMNDecisionResultImpl ) {
                DMNDecisionResultImpl i = (DMNDecisionResultImpl) dr;
                i.setResult( dmnContext.get( i.getDecisionId() ) );
            }
        }
    }

    @Override
    public DMNContext getContext() {
        // TODO rewiew, this means the DMNContext returned is detached from the internal context here.
        DMNContext res = DMNFactory.newContext();
        for ( Entry<String, Object> e : dmnContext.entrySet() ) {
            res.set(e.getKey(), e.getValue());
        }
        return res;
    }

    @Override
    public List<DMNMessage> getMessages() {
        return this.messages;
    }

    @Override
    public List<DMNMessage> getMessages(Severity... sevs) {
        return this.messages.stream()
                .filter( m -> Arrays.asList(sevs).stream().anyMatch( f -> f.equals(m.getSeverity())) )
                .collect(Collectors.toList());
    }
    @Override
    public boolean hasErrors() {
        return messages.stream().anyMatch( m -> DMNMessage.Severity.ERROR.equals( m.getSeverity() ) );
    }

    @Override
    public List<DMNDecisionResult> getDecisionResults() {
        rewireDecisionResults();
        return new ArrayList<>( decisionResults.values() );
    }

    @Override
    public DMNDecisionResult getDecisionResultByName( String name ) {
        rewireDecisionResults();
        return decisionResults.values().stream().filter( dr -> dr.getDecisionName().equals( name ) ).findFirst().get();
    }

    @Override
    public DMNDecisionResult getDecisionResultById( String id ) {
        rewireDecisionResults();
        return decisionResults.get( id );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DMNEvaluationResult [namespace=").append(namespace).append(", modelName=").append(modelName).append(", decisionName=").append(decisionName).append(", dmnContext=").append(dmnContext).append(", messages=").append(messages).append(", decisionResults=").append(decisionResults).append("]");
        return builder.toString();
    }
     
    
    
}
