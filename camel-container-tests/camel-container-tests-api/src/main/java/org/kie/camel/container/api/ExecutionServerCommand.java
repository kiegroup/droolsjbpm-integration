package org.kie.camel.container.api;

import java.util.HashMap;
import java.util.Map;

public class ExecutionServerCommand {

    private String client;
    private String operation;
    private String bodyParam;
    private Map<String, String> parameters = new HashMap<>();
    private Object body;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getBodyParam() {
        return bodyParam;
    }

    public void setBodyParam(String bodyParam) {
        this.bodyParam = bodyParam;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
