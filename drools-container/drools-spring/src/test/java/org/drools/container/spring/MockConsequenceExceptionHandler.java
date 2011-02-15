package org.drools.container.spring;

import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.WorkingMemory;
import org.drools.runtime.rule.impl.DefaultConsequenceExceptionHandler;

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
