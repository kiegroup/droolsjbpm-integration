package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;

@XmlRootElement(name="execution-results")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbExecutionResultsResponse extends AbstractJaxbCommandResponse<ExecutionResults> {

    @XmlElements({
        @XmlElement(name="results", type=ExecutionResultImpl.class)
    })
    private ExecutionResults results;
    
    public void setResults(ExecutionResults results) {
        this.results = results;
    }

    public JaxbExecutionResultsResponse() {
    }
    
    public JaxbExecutionResultsResponse(ExecutionResults results, int i, Command<?> cmd) { 
        super(i, cmd);
        this.results = results;
    }
    
    @Override
    public ExecutionResults getResult() {
        return results;
    }

    @Override
    public void setResult(ExecutionResults result) {
        this.results = result;
    }
}
