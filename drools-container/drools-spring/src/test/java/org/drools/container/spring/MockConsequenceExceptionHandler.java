package org.drools.container.spring;

import org.drools.runtime.rule.impl.DefaultConsequenceExceptionHandler;
import org.kie.runtime.rule.Activation;
import org.kie.runtime.rule.WorkingMemory;

public class MockConsequenceExceptionHandler extends DefaultConsequenceExceptionHandler {

    @Override
    public void handleException(Activation activation,
                                WorkingMemory workingMemory,
                                Exception exception) {
        super.handleException( activation,
                               workingMemory,
                               exception );
    }

}
