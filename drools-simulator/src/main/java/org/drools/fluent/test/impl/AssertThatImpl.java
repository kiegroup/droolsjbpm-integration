package org.drools.fluent.test.impl;

import org.drools.fluent.test.ReflectiveMatcher;
import org.hamcrest.Matcher;

public class AssertThatImpl implements ReflectiveMatcher {
    
    private String name;
    
    private Object object;
    
    public AssertThatImpl() {
        
    }
    
    public AssertThatImpl(String name,
                                 Object object) {
        super();
        this.name = name;
        this.object = object;
    }   

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public <T> Matcher<T> build(Class<T> cls) {
        return null;
    }
    
}
