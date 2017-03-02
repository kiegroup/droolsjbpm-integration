package org.kie.server.api.model.dmn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNDecisionResult.DecisionEvaluationStatus;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNMessage.Severity;
import org.kie.dmn.api.core.DMNResult;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-evaluation-result")
public class DMNResultKS implements DMNResult {

    @XmlElement(name="model-namespace")
    private String namespace;

    @XmlElement(name="model-name")
    private String modelName;

    @XmlElement(name="decision-name")
    private String decisionName;

    // please note this must NOT be @XmlElement*Wrapper* otherwise it will be unable to find proper type for marshalling/unmarshalling.
    @XmlElement(name="dmn-context")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Map<String, Object> dmnContext = new HashMap<>();

    // concrete implementation of DMNMessage and DMNDecisionResult are needed in order to have proper marshalling
    @XmlElementWrapper(name="messages")
    private List<DMNMessageKS> messages = new ArrayList<>();
    
    @XmlElementWrapper(name="decision-results")
    private Map<String, DMNDecisionResultKS> decisionResults = new HashMap<>();
    
    public DMNResultKS() {
        // no-arg constructor for marshalling
    }
    
    public DMNResultKS(DMNResult dmnResult) {
        this.setDmnContext( dmnResult.getContext().getAll() );
        this.setMessages( dmnResult.getMessages() );
        this.setDecisionResults( dmnResult.getDecisionResults() );
    }
    
    public DMNResultKS(String namespace, String modelName, DMNResult dmnResult) {
        this(dmnResult);
        this.namespace = namespace;
        this.modelName = modelName;
    }
    
    public DMNResultKS(String namespace, String modelName, String decisionName, DMNResult dmnResult) {
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
        dmnContext.replaceAll( (k, v) -> stubDMNResult(v) );
        this.dmnContext = dmnContext;
    }
    public void setMessages(List<DMNMessage> messages) {
        // wrap for serialization:
        for ( DMNMessage m : messages ) {
            this.messages.add(DMNMessageKS.of(m));
        }
    }
    public void setDecisionResults(List<DMNDecisionResult> decisionResults) {
        for ( DMNDecisionResult dr : decisionResults ) {
            this.decisionResults.put(dr.getDecisionId(), DMNDecisionResultKS.of(dr));
        }
    }

    @Override
    public DMNContext getContext() {
        // TODO rewiew, this means the DMNContext returned is detached from the internal context here.
        return MapBackedDMNContext.of(dmnContext);
    }
    
    private static class MapBackedDMNContext implements DMNContext {
        
        private Map<String, Object> ctx = new HashMap<>();
        
        private MapBackedDMNContext() {
            // intentional
        }
        
        static MapBackedDMNContext of(Map<String, Object> ctx) {
            MapBackedDMNContext result = new MapBackedDMNContext();
            result.ctx = ctx;
            return result;
        }

        @Override
        public Object set(String name, Object value) {
            return ctx.put(name, value);
        }

        @Override
        public Object get(String name) {
            return ctx.get(name);
        }

        @Override
        public Map<String, Object> getAll() {
            return ctx;
        }

        @Override
        public boolean isDefined(String name) {
            return ctx.containsKey(name);
        }

        @Override
        public DMNContext clone() {
            return of(this.ctx);
        }
        
    }

    @Override
    public List<DMNMessage> getMessages() {
        List<DMNMessage> res = new ArrayList<>();
        messages.forEach(x -> res.add(x));
        return res;
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
        return new ArrayList<>( decisionResults.values() );
    }

    @Override
    public DMNDecisionResult getDecisionResultByName( String name ) {
        return decisionResults.values().stream().filter( dr -> dr.getDecisionName().equals( name ) ).findFirst().get();
    }

    @Override
    public DMNDecisionResult getDecisionResultById( String id ) {
        return decisionResults.get( id );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DMNResultKS [namespace=").append(namespace).append(", modelName=").append(modelName).append(", decisionName=").append(decisionName).append(", dmnContext=").append(dmnContext).append(", messages=").append(messages).append(", decisionResults=").append(decisionResults).append("]");
        return builder.toString();
    }
     
    public static Object stubDMNResult(Object result) {
        if ( result instanceof DMNContext ) {
            ((DMNContext) result).getAll().replaceAll( (k, v) -> stubDMNResult(v) );
            return MapBackedDMNContext.of(((DMNContext) result).getAll());
        } else if ( result instanceof Map<?, ?> ) {
            ((Map) result).replaceAll( (k, v) -> stubDMNResult(v) );
        } else if ( result instanceof List<?> ) {
            ((List<Object>) result).replaceAll( DMNResultKS::stubDMNResult );
            return result;
        } else if ( result instanceof Set<?> ) {
            Set<?> originalSet = (Set<?>) result;
            Collection mappedSet = originalSet.stream().map( DMNResultKS::stubDMNResult ).collect(Collectors.toSet());
            originalSet.clear();
            originalSet.addAll(mappedSet);
            return result;
        } else if ( result.getClass().getPackage().getName().startsWith("org.kie.dmn") ) {
            return DMNNodeStub.of(result);
        }
        return result;
    }
    
}
