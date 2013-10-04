package org.jbpm.simulation.impl;

import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.process.core.validation.ProcessValidator;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;

public class SimulationProcessValidator implements ProcessValidator {

    @Override
    public ProcessValidationError[] validateProcess(Process process) {
        ProcessValidationError[] errors = new ProcessValidationError[0];
        // TODO add simulation related validation options
        return errors;
    }

    @Override
    public boolean accept(Process process, Resource resource) {
        if (process != null && RuleFlowProcess.RULEFLOW_TYPE.equals(process.getType())
                && resource.getSourcePath() != null
                && resource.getSourcePath().matches(".+\\.bpsim\\.bpmn[2]?$")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean compilationSupported() {
        return false;
    }

}
