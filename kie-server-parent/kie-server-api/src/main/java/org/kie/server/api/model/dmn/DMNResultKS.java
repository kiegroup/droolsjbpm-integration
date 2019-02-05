package org.kie.server.api.model.dmn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNMessage.Severity;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.marshalling.json.JSONMarshaller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-evaluation-result")
@XStreamAlias("dmn-evaluation-result")
public class DMNResultKS implements DMNResult {

    @XmlElement(name = "model-namespace")
    @XStreamAlias("model-namespace")
    private String namespace;

    @XmlElement(name = "model-name")
    @XStreamAlias("model-name")
    private String modelName;

    @XmlElement(name = "decision-name")
    @XStreamImplicit(itemFieldName = "decision-name")
    @JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
    private List<String> decisionNames = new ArrayList<>();

    @XmlElement(name = "dmn-context")
    @XStreamAlias("dmn-context")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    @JsonSerialize(using = JSONMarshaller.PassThruSerializer.class)
    private Map<String, Object> dmnContext = new HashMap<>();

    // concrete implementation of DMNMessage and DMNDecisionResult are needed in order to have proper marshalling
    @XmlElementWrapper(name = "messages")
    @XStreamAlias("messages")
    private List<DMNMessageKS> messages = new ArrayList<>();

    @XmlElement(name = "decision-results")
    @XStreamAlias("decision-results")
    private Map<String, DMNDecisionResultKS> decisionResults = new HashMap<>();

    public DMNResultKS() {
        // no-arg constructor for marshalling
    }

    public DMNResultKS(DMNResult dmnResult) {
        this.setDmnContext(dmnResult.getContext().getAll());
        this.setMessages(dmnResult.getMessages());
        this.setDecisionResults(dmnResult.getDecisionResults());
    }

    public DMNResultKS(String namespace, String modelName, DMNResult dmnResult) {
        this(dmnResult);
        this.namespace = namespace;
        this.modelName = modelName;
    }

    public DMNResultKS(String namespace, String modelName, List<String> decisionNames, DMNResult dmnResult) {
        this(namespace, modelName, dmnResult);
        this.decisionNames = decisionNames;
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

    public List<String> getDecisionNames() {
        return decisionNames;
    }

    public void setDecisionNames(List<String> decisionNames) {
        this.decisionNames = decisionNames;
    }

    public Map<String, Object> getDmnContext() {
        return dmnContext;
    }

    public void setDmnContext(Map<String, Object> dmnContext) {
        this.dmnContext = new HashMap<>();
        for (Entry<String, Object> kv : dmnContext.entrySet()) {
            this.dmnContext.put(kv.getKey(), stubDMNResult(kv.getValue()));
        }
    }

    public void setMessages(List<DMNMessage> messages) {
        // wrap for serialization:
        for (DMNMessage m : messages) {
            this.messages.add(DMNMessageKS.of(m));
        }
    }

    public void setDecisionResults(List<DMNDecisionResult> decisionResults) {
        for (DMNDecisionResult dr : decisionResults) {
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
        private Deque<ScopeReference> stack = new LinkedList<>();

        private MapBackedDMNContext() {
            // intentional
        }

        static MapBackedDMNContext of(Map<String, Object> ctx) {
            MapBackedDMNContext result = new MapBackedDMNContext();
            result.ctx = ctx;
            return result;
        }

        @Override
        public DMNContext clone() {
            return of(this.ctx);
        }

        @Override
        public Object set(String name, Object value) {
            return getCurrentEntries().put(name, value);
        }

        @Override
        public Object get(String name) {
            return getCurrentEntries().get(name);
        }

        private Map<String, Object> getCurrentEntries() {
            if (stack.isEmpty()) {
                return ctx;
            } else {
                return stack.peek().getRef(); // Intentional, symbol resolution in scope should limit at the top of the stack (for DMN semantic).
            }
        }

        @Override
        public void pushScope(String name, String namespace) {
            Map<String, Object> scopeRef = (Map<String, Object>) getCurrentEntries().computeIfAbsent(name, s -> new LinkedHashMap<String, Object>());
            stack.push(new ScopeReference(name, namespace, scopeRef));
        }

        @Override
        public void popScope() {
            stack.pop();
        }

        @Override
        public Optional<String> scopeNamespace() {
            if (stack.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(stack.peek().getNamespace());
            }
        }

        @Override
        public Map<String, Object> getAll() {
            return getCurrentEntries();
        }

        @Override
        public boolean isDefined(String name) {
            return getCurrentEntries().containsKey(name);
        }

        public static class ScopeReference {

            private final String name;
            private final String namespace;
            private final Map<String, Object> ref;

            public ScopeReference(String name, String namespace, Map<String, Object> ref) {
                super();
                this.name = name;
                this.namespace = namespace;
                this.ref = ref;
            }

            public String getName() {
                return name;
            }

            public String getNamespace() {
                return namespace;
            }

            public Map<String, Object> getRef() {
                return ref;
            }
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
                .filter(m -> Arrays.asList(sevs).stream().anyMatch(f -> f.equals(m.getSeverity())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasErrors() {
        return messages.stream().anyMatch(m -> DMNMessage.Severity.ERROR.equals(m.getSeverity()));
    }

    @Override
    public List<DMNDecisionResult> getDecisionResults() {
        return new ArrayList<>(decisionResults.values());
    }

    @Override
    public DMNDecisionResult getDecisionResultByName(String name) {
        return decisionResults.values().stream().filter(dr -> dr.getDecisionName().equals(name)).findFirst().get();
    }

    @Override
    public DMNDecisionResult getDecisionResultById(String id) {
        return decisionResults.get(id);
    }

    @Override
    public String toString() {
        return new StringBuilder("DMNResultKS [")
                .append("namespace=").append(namespace)
                .append(", modelName=").append(modelName)
                .append(", decisionNames=").append(decisionNames)
                .append(", dmnContext=").append(dmnContext)
                .append(", messages=").append(messages)
                .append(", decisionResults=").append(decisionResults)
                .append("]").toString();
    }

    public static Object stubDMNResult(Object result) {
        if (result instanceof DMNContext) {
            Map<String, Object> stubbedContextValues = new HashMap<>();
            for (Entry<String, Object> kv : ((DMNContext) result).getAll().entrySet()) {
                stubbedContextValues.put(kv.getKey(), stubDMNResult(kv.getValue()));
            }
            return MapBackedDMNContext.of(stubbedContextValues);
        } else if (result instanceof Map<?, ?>) {
            Map<Object, Object> stubbedValues = new HashMap<>();
            for (Entry<?, ?> kv : ((Map<?, ?>) result).entrySet()) {
                stubbedValues.put(kv.getKey(), stubDMNResult(kv.getValue()));
            }
            return stubbedValues;
        } else if (result instanceof List<?>) {
            List<?> stubbedValues = ((List<?>) result).stream().map(DMNResultKS::stubDMNResult).collect(Collectors.toList());
            return stubbedValues;
        } else if (result instanceof Set<?>) {
            Set<?> stubbedValues = ((Set<?>) result).stream().map(DMNResultKS::stubDMNResult).collect(Collectors.toSet());
            return stubbedValues;
        } else if (result != null && result.getClass().getPackage().getName().startsWith("org.kie.dmn")) {
            return DMNNodeStub.of(result);
        }
        return result;
    }
}
