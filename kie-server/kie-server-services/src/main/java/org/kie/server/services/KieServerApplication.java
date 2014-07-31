package org.kie.server.services;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.ws.rs.core.Application;

import org.kie.server.services.rest.KieServerRestImpl;

public class KieServerApplication extends Application {
    
    private final Set<Object> instances = new CopyOnWriteArraySet<Object>() {
        private static final long serialVersionUID = 1763183096852523317L;
    {
       add(new KieServerRestImpl()); 
    }};
    
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }
    
    @Override
    public Set<Object> getSingletons() {
        return instances;
    }

}
