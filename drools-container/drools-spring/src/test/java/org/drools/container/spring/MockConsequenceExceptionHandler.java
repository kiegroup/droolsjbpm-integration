package org.drools.container.spring;

import org.drools.runtime.rule.impl.DefaultConsequenceExceptionHandler;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

public class MockConsequenceExceptionHandler extends DefaultConsequenceExceptionHandler {

    @Override
    public void handleException(Match activation,
                                WorkingMemory workingMemory,
                                Exception exception) {
        super.handleException( activation,
                               workingMemory,
                               exception );
    }

}
