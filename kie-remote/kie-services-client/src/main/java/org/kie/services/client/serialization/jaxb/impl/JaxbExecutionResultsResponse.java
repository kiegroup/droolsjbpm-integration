package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;

public class JaxbExecutionResultsResponse extends AbstractJaxbCommandResponse<ExecutionResults> {

    @XmlElements({
        @XmlElement(name="results", type=ExecutionResultImpl.class)
    })
    private ExecutionResults results;
    
    public JaxbExecutionResultsResponse(ExecutionResults results, int i, Command<?> cmd) { 
        super(i, cmd);
        this.results = results;
    }
    
    @Override
    public ExecutionResults getResult() {
        return results;
    }
}
