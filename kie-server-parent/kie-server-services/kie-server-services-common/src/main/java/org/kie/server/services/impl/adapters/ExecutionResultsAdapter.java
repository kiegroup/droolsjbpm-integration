package org.kie.server.services.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.runtime.ExecutionResults;


public class ExecutionResultsAdapter extends XmlAdapter<ExecutionResultImpl, ExecutionResults>{

    @Override
    public ExecutionResults unmarshal(ExecutionResultImpl v) throws Exception {
        return v;
    }

    @Override
    public ExecutionResultImpl marshal(ExecutionResults v) throws Exception {
        return (ExecutionResultImpl)v;
    }

}
