package org.drools.grid.io.impl;

import java.io.Serializable;
import java.util.List;

public class CommandImpl
    implements
    Serializable {
    private String       name;
    private List<Object> arguments;

    public CommandImpl() {
        
    }
    
    public CommandImpl(String name,
                       List<Object> arguments) {
        super();
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<Object> getArguments() {
        return arguments;
    }

}
