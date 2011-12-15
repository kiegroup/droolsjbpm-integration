package org.drools.fluent.test.impl;

import java.util.ArrayList;
import java.util.List;

import org.drools.fluent.test.ReflectiveMatcher;
import org.drools.fluent.test.ReflectiveMatcherAssert;

public class ReflectiveMatcherFactory {
    private List<String> staticImports;
    
    public ReflectiveMatcherFactory(List<String> staticImports) {
        this.staticImports = staticImports;
    }
    
    public List<String> getStaticImports() {
        return staticImports;
    }
    
    public ReflectiveMatcherAssert assertThat(String string) {
        return new ReflectiveMatcherAssertImpl(string, this);
    }      

    public ReflectiveMatcherAssert assertThat(String actual, ReflectiveMatcher matcher) {
        return new ReflectiveMatcherAssertImpl(actual, matcher, this);
    }  
    
    public static ReflectiveMatcher matcher(String name, Object object) {
        return new ReflectiveMatcherImpl(name, object);
    }    
    
    public static ReflectiveMatcher matcher(String name, ReflectiveMatcher matcher) {
        return new ReflectiveMatcherImpl(name, matcher);
    }
    
    public static ReflectiveMatcher matcher( String name, ReflectiveMatcher... matchers) {
        return new ReflectiveMatcherImpl(name, matchers);
    } 
    
}
