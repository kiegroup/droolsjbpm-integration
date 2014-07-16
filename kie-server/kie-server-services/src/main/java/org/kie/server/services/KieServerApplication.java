package org.kie.server.services;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.ws.rs.core.Application;

import org.kie.server.services.impl.KieServerImpl;

public class KieServerApplication extends Application {
    
    private final Set<Class<?>> classes = new CopyOnWriteArraySet<Class<?>>() {
        private static final long serialVersionUID = 1763183096852523317L;
    {
       add(KieServerImpl.class); 
    }};
    
    private final Set<Object> instances = new CopyOnWriteArraySet<Object>() {
        private static final long serialVersionUID = 1763183096852523317L;
    {
       add(new KieServerImpl()); 
    }};
    
    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
    
    @Override
    public Set<Object> getSingletons() {
        return instances;
    }

}
