package org.drools.fluent.test.impl;

import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.test.ReflectiveMatcherAssert;
import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.*;

public class AbstractFluentTest<P> {

    private InternalSimulation sim;
    
    public AbstractFluentTest() {
    }
      
    public InternalSimulation getSim() {
        return sim;
    }

    public void setSim(InternalSimulation sim) {
        this.sim = sim;
    }

    public <T> P test(String reason,
                      T actual,
                      Matcher<T> matcher) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> P test(T actual,
                      Matcher<T> matcher) {
        assertThat( actual, matcher);
        return (P) this;
    }
    
    public <T> P test(String text) {
        MVELTestCommand testCmd = new MVELTestCommand();
        testCmd.setText( text );
        
        sim.addCommand( testCmd );
        
        return (P) this;
    }    
    
    public <T> P test(ReflectiveMatcherAssert matcherAssert) {
        ReflectiveMatcherAssertCommand matcherCmd = new ReflectiveMatcherAssertCommand( matcherAssert );
        sim.addCommand( matcherCmd );
        
        return ( P ) this;
    }
}
