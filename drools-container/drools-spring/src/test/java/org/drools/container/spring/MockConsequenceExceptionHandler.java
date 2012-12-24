package org.drools.container.spring;

import org.drools.runtime.rule.impl.DefaultConsequenceExceptionHandler;
import org.kie.runtime.rule.Match;
import org.kie.runtime.rule.Session;


public class MockConsequenceExceptionHandler extends DefaultConsequenceExceptionHandler {

    @Override
    public void handleException(Match activation,
                                Session workingMemory,
                                Exception exception) {
        super.handleException( activation,
                               workingMemory,
                               exception );
    }

}
