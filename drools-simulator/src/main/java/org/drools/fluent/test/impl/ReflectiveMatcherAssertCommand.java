package org.drools.fluent.test.impl;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.fluent.test.ReflectiveMatcherAssert;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

public class ReflectiveMatcherAssertCommand  implements GenericCommand<Void>  {

    private ReflectiveMatcherAssert matcherAssert;
    
    public ReflectiveMatcherAssertCommand() {
        
    }
    
    public ReflectiveMatcherAssertCommand(ReflectiveMatcherAssert matcherAssert) {
        this.matcherAssert = matcherAssert;
    }

    
    public ReflectiveMatcherAssert getMatcherAssert() {
        return matcherAssert;
    }

    public void setMatcherAssert(ReflectiveMatcherAssert matcherAssert) {
        this.matcherAssert = matcherAssert;
    }

    public Void execute(Context context) {
        matcherAssert.eval( context );        
        return null;
    }

}
