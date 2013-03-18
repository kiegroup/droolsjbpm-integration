package org.drools.container.spring;

import org.drools.core.runtime.rule.impl.DefaultConsequenceExceptionHandler;
import org.kie.api.runtime.rule.Match;
import org.kie.api.runtime.rule.Session;


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
